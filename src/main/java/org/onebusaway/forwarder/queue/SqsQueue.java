package org.onebusaway.forwarder.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.onebusaway.forwarder.models.CleverAvlData;
import org.onebusaway.forwarder.service.ConfigurationService;
import org.onebusaway.forwarder.sql.connection.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.BatchResultErrorEntry;
import com.amazonaws.services.sqs.model.SendMessageBatchRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.SendMessageBatchResult;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SqsQueue {

    private static final Logger _log = LoggerFactory.getLogger(SqsQueue.class);
    private static final int DEFAULT_SEND_THREADS = 2;
    private static final int STATUS_FREQUENCY = 10000;
    private static final long MESSAGE_SEND_WAIT_MILLISECONDS = 250;
    private static final long MESSAGE_WAIT_MILLISECONDS = 1 * 1000;
    private static final long SQS_CONNECTION_TTL = -1L; // default is -1L
    private static final int SQS_CONNECTION_TIMEOUT = 5 * 1000;  // default is 10s
    private static final long SQS_MAX_IDLE = 30 * 1000; // default is 60s
    private static final int SQS_SOCKET_TIMEOUT = 15 * 1000;  // default is 50s
    // java1.7 feature -- this needs to be large enough to accommodate REQUEST_TIMEOUT and retries
    private static final int SQS_EXECUTION_TIMEOUT = 30 * 1000; // default is 0
    // java1.7 feature
    private static final int SQS_REQUEST_TIMEOUT = 10 * 1000; // default is 0
    private static final String DNS_TTL = "60"; // seconds

    private ConfigurationService _config;

    private Properties _configProperties;

    private ArrayBlockingQueue<CleverAvlData> _sendQueue = new ArrayBlockingQueue<CleverAvlData>(
            1000000);

    private int _sendThreads = DEFAULT_SEND_THREADS;

    @Inject
    public void setConfiguration(ConfigurationService config) {
        _config = config;
    }

    @PostConstruct
    public void start(){

        _configProperties = _config.getConfigProperties();

        // set DNS TTL
        _log.debug("existing DNS TTL setting {}",
                java.security.Security.getProperty("networkaddress.cache.ttl"));
        java.security.Security.setProperty("networkaddress.cache.ttl", DNS_TTL);
        _log.debug("updated DNS TTL setting {}",
                java.security.Security.getProperty("networkaddress.cache.ttl"));

        String url = _configProperties.getSqsUrl();
        String key = _configProperties.getAwsKey();
        String secret = _configProperties.getAwsSecret();
        int sendThreads = _configProperties.getSendThreadsNumber();
        boolean debugFlag = _configProperties.getDebugFlag();

        _log.info("connecting to " + url + " via key " + key);

        if (sendThreads >= 1) {
            _log.info("overriding default send threads to " + sendThreads);
            _sendThreads = sendThreads;
        }

        AWSCredentials credentials = new BasicAWSCredentials(key, secret);
        // startup some send threads
        _log.info("starting up " + _sendThreads + " send threads");

        List<SendTask> sendTasks = new ArrayList<SendTask>();
        for (int i = 0; i < _sendThreads; i++) {
            SendTask task = new SendTask(i, credentials, url, debugFlag);
            new Thread(task).start();
            sendTasks.add(task);
        }

        new Thread(new ReaperTask(sendTasks, credentials, url,
                debugFlag)).start();
    }

    public void connect() throws Exception {
        // no op
        _log.info("connected called");
    }

    public void reconnect() throws Exception {
        // no op
        _log.info("reconnect called");
    }

    public void send(CleverAvlData data) throws Exception {

        boolean success = _sendQueue.offer(data, MESSAGE_WAIT_MILLISECONDS,
                TimeUnit.MILLISECONDS);
        if (!success)
            _log.warn("QUEUE FULL, dropped message");
    }


    /**
     * Push data out to AWS SQS. The more threads, the more total throughput up
     * until a point.
     *
     */
    private class SendTask implements Runnable {

        private static final int BATCH_SIZE = 200;
        private static final int MESSAGE_SIZE = 256 * 1024;
        private AmazonSQS sqs;
        private AWSCredentials credentials;
        private String url;
        private boolean isDebug;
        private int threadNumber;
        private long lastAttempt = 0;
        private boolean disabled = false;
        ObjectMapper _mapper = new ObjectMapper();

        public SendTask(int threadNumber, AWSCredentials credentials, String url,
                        boolean debugFlag) {
            this.threadNumber = threadNumber;
            this.credentials = credentials;
            this.url = url;
            this.isDebug = debugFlag;
        }

        public void connect() {
            _log.info("creating client");
            // fine tune the SDK for a difficult network
            ClientConfiguration config = new ClientConfiguration()
                    .withConnectionTTL(SQS_CONNECTION_TTL)
                    .withReaper(true)
                    .withConnectionTimeout(SQS_CONNECTION_TIMEOUT)
                    .withConnectionMaxIdleMillis(SQS_MAX_IDLE)
                    .withSocketTimeout(SQS_SOCKET_TIMEOUT);
                    //.withRequestTimeout(SQS_REQUEST_TIMEOUT)
                    //.withClientExecutionTimeout(SQS_EXECUTION_TIMEOUT);

            sqs = new AmazonSQSClient(credentials, config);

            Region usEast1 = Region.getRegion(Regions.US_EAST_1);
            sqs.setRegion(usEast1);
            _log.info("client creation complete");
        }

        public long getLastAttemptTimestamp() {
            return lastAttempt;
        }

        public void disable() {
            disabled = true;
        }

        public void run() {
            connect();

            int ticker = 0;

            while (!Thread.interrupted() && !disabled) {
                try {
                    List<CleverAvlData> messages = new ArrayList<CleverAvlData>(BATCH_SIZE);
                    // grab up to 10 messages if available
                    int messageCount = _sendQueue.drainTo(messages, BATCH_SIZE);
                    long start = System.currentTimeMillis();
                    lastAttempt = start; // update our health

                    if (messageCount == 0) {
                        sleep(MESSAGE_SEND_WAIT_MILLISECONDS);
                        continue; // try again
                    }

                    String data = _mapper.writeValueAsString(messages);

                    ticker += messageCount;
                    SendMessageBatchRequest batchRequest = new SendMessageBatchRequest().withQueueUrl(url);
                    List<SendMessageBatchRequestEntry> entries = new ArrayList<SendMessageBatchRequestEntry>(
                            messageCount);

                    // serialize the data into the entries list
                    int batchEntryIndex = 0;

                    _log.debug("message size=" + data.length()/1024 + " KB from " + messages.size() + " messages");
                    entries.add(new SendMessageBatchRequestEntry().withId(
                            Integer.toString(batchEntryIndex)).withMessageBody(data));


                    batchRequest.setEntries(entries);

                    long processing = System.currentTimeMillis();
                    /*
                     * send synchronously!  This method blocks!
                     * Reaper currently monitors stuck for stuck connections
                     * Newer versions of SDK contain execution timeout
                     */
                    SendMessageBatchResult result = sqs.sendMessageBatch(batchRequest);
                    long sqsSendTime = System.currentTimeMillis() - processing;
                    if (sqsSendTime > SQS_EXECUTION_TIMEOUT) {
                        _log.error("sendMessageBatch took too long, " + sqsSendTime
                                + "ms exceeded " + SQS_EXECUTION_TIMEOUT + "ms");
                    }
                    if (isDebug) {
                        logFailed(result.getFailed());
                    }
                    _log.trace("sent {}  entries", messageCount);

                    if (ticker >= STATUS_FREQUENCY) {
                        if (isDebug && threadNumber == 0) {
                            long latency = System.currentTimeMillis() - messages.get(0).getVehicleTime().getTime();
                            _log.debug("last messages (" + messageCount + ") "
                                    + "processed in " + (processing - start) + " ms, written in "
                                    + (System.currentTimeMillis() - start)
                                    + " ms had latency of "
                                    + latency
                                    + " ms, sqs queue size is " + _sendQueue.size());
                        }
                        ticker = 0;
                    }
                } catch (Exception any) {
                    _log.error("exception sending: ", any);
                }
            }
            _log.error("SqsQueue Thread {} Exiting!", threadNumber);
        }

        private void logFailed(List<BatchResultErrorEntry> failed) {
            if (failed == null)
                return;
            for (BatchResultErrorEntry entry : failed) {
                _log.debug("failed transaction: {}", entry.toString());
            }
        }

        private void sleep(long messageWaitMilliseconds) {
            try {
                Thread.sleep(messageWaitMilliseconds);
            } catch (InterruptedException e) {
            }
        }

    }

    /**
     * check on the health of threads, and replace if thread has aged.
     *
     */
    public class ReaperTask implements Runnable {

        private static final long THREAD_TIMEOUT = 30 * 1000; // 30 seconds
        private static final int REAPER_INTERVAL = 30; // 30 seconds
        private List<SendTask> tasks;
        private AWSCredentials credentials;
        private String url;
        private boolean isDebug;

        public ReaperTask(List<SendTask> task,
                          AWSCredentials credentials, String url, boolean debugFlag) {
            this.tasks = task;
            this.credentials = credentials;
            this.url = url;
            this.isDebug = debugFlag;

        }

        private void sleep(int sleepTime) {
            try {
                Thread.sleep(sleepTime * 1000);
            } catch (InterruptedException e) {
                return;
            }

        }

        public void run() {
            sleep(REAPER_INTERVAL);

            while (!Thread.interrupted()) {
                _log.debug("reaper running");

                // check on the send tasks
                List<SendTask> newTasks = new ArrayList<SendTask>();
                for (int i = tasks.size() - 1; i >= 0; i--) {
                    long delta = System.currentTimeMillis()
                            - tasks.get(i).getLastAttemptTimestamp();
                    if (delta > THREAD_TIMEOUT) {
                        // we haven't heard from this thread in a while, consider it dead
                        tasks.get(i).disable();
                        _log.error(
                                "reaping thread {} as its been {} ms since we've heard from it",
                                i, delta);
                        tasks.remove(i);
                        SendTask newTask = new SendTask(i, credentials, url, isDebug);
                        new Thread(newTask).start();
                        newTasks.add(newTask);
                    }
                }
                tasks.addAll(newTasks);
                _log.debug("reaper complete with {} new threads created",
                        newTasks.size());

                // sleep
                sleep(REAPER_INTERVAL);
            }
            _log.info("reaper exiting while");
        }

    }
}
