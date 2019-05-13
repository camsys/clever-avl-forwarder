/**
 * Copyright 2012 University of South Florida
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package org.onebusaway.forwarder;

import org.onebusaway.forwarder.models.CleverAvlData;
import org.onebusaway.forwarder.queue.SqsQueue;
import org.onebusaway.forwarder.service.CleverAvlService;
import org.onebusaway.forwarder.service.ConfigurationService;
import org.onebusaway.forwarder.sql.connection.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 
 * @author Khoa Tran
 *
 */

@Singleton
public class Forwarder {
	private static final Logger _log = LoggerFactory.getLogger(Forwarder.class);

	private ScheduledExecutorService _refreshExecutor;

	private ScheduledExecutorService _delayExecutor;

	private ConfigurationService _config;

	private CleverAvlService _cleverAvlService;

	private SqsQueue _sqsQueue;

	private Connection _conn = null;

	private int _refreshInterval = 30;

	private long _lastSuccessfulQuery;

	public Forwarder(){

	}

	@Inject
	public void setConfiguration(ConfigurationService config) {
		_config = config;
	}

	@Inject
	public void setCleverAvlService(CleverAvlService service) {
		_cleverAvlService = service;
	}

	@Inject
	public void setSQSQueue(SqsQueue queue) {
		_sqsQueue = queue;
	}


	@PostConstruct
	public void start() {
		Properties configProps = _config.getConfigProperties();

		if(configProps.getRefreshInterval() != null){
			_refreshInterval = configProps.getRefreshInterval();
		}

		_log.info("starting Avl to SQS forwarder service");
		_refreshExecutor = Executors.newSingleThreadScheduledExecutor();
		_refreshExecutor.scheduleAtFixedRate(new RefreshAvlData(), 0,
				_refreshInterval, TimeUnit.SECONDS);

		_delayExecutor = Executors.newSingleThreadScheduledExecutor();
		_delayExecutor.scheduleAtFixedRate(new DelayCheck(), _refreshInterval,
				_refreshInterval/4, TimeUnit.SECONDS);
	}

	@PreDestroy
	public void stop() {
		_log.info("stopping Avl to SQS forwarder service");
		_refreshExecutor.shutdownNow();
		_delayExecutor.shutdownNow();
	}

	private class RefreshAvlData implements Runnable {
		public void run() {
			try {
				_log.info("refreshing vehicles");
				processData();
			} catch (Exception ex) {
				_log.error("Failed to refresh AvlData: " + ex.getMessage(), ex);
			}
		}

		public void processData() throws Exception {
			List<CleverAvlData> avlDataList = _cleverAvlService.getCleverAvl();
			_lastSuccessfulQuery = System.currentTimeMillis();
			for(CleverAvlData avlData: avlDataList){
				try {
					_log.debug(avlData.toString());
					_sqsQueue.send(avlData);
				} catch (Exception jpe){
					_log.error("Unable to parse avl data " + avlData, jpe);
				}
			}
		}
	}

	private class DelayCheck implements Runnable {
		public void run() {
			long hangTime = (System.currentTimeMillis() - _lastSuccessfulQuery) / 1000;
			if (hangTime> ((_refreshInterval * 2) - (_refreshInterval / 2))) {
				// if we've reached here, the connection to the database has hung
				// we assume a service-based configuration and simply exit
				// TODO adjust network/driver timeouts instead!
				_log.error("Connection hung with delay of " + hangTime + ".  Exiting!");
				System.exit(1);
			} else {
				_log.info("hangTime:" + hangTime);
			}
		}
	}
}