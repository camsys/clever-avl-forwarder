## Clever-Avl-Forwarder

Agent that forwards data from a Clever AVL database to and AWS SQS Endpoint. Connection information is defined in a config.properties file. Configuration values listed below.

### Prereqs to run the application
- Java 8 JDK
- SQL Server Database
- AWS SQS

### Running the Application
You can build the application using maven: `mvn clean package`
You can run the jar using the following command: 
  `java -jar clever-avl-forwarder.jar`

### Configuration

A configuraiton file is necessary to run the forwarder. Create a file named `config.properties` and place it in the same directory as the jar. 

Alternatively you can run the jar with the following argument to specify the config file location:
`java -jar clever-avl-forwarder.jar --configFile=C:\clever-avl-forwarder\config.properties`

The `config.properties` file supports the following configuration values.

Config | Description | Example | Required
--- | --- | --- | ---
sql.host | Database hostname or ip | 127.0.0.1 | true
sql.portNumber | Database port | 1433 | false
sql.databaseName | Database name | sampleDatabase | false
sql.user | Database username | sa | true
sql.password | Database password | password | true
sql.refreshInterval | How often data is retreived from database. Defaults to 30. | false
aws.key | AWS IAM account key for SQS access | ABC123 | true
aws.secret | AWS IAM account secret key for SQS access | ABCDEFGHIJK123456 | true
aws.sqs.url | AWS SQS Url | https://sqs.us-east-1.amazonaws.com/123456789/sqs_endpoint | true
aws.sqs.sendThreads | Number of threads to send data to SQS | 2 | true
aws.sqs.debugFlag | Flag to show or hide SQS debug statements. Defaults to false. | true | false

### Running as a service
You can optionally run the forwarder as a service.

#### Running as a Windows Service
- Build the forwarder jar and then place it in a directory of your choice (eg. `C:\clever-avl-forwarder`)
- Download and install the latest version of the Commons Daemon: https://commons.apache.org/proper/commons-daemon/download_daemon.cgi in the directory of your choice (eg. `C:\Program Files\Java\commons-daemon-1.0.14-bin-windows`)
- Save the `services/install.sh`script and update accordingly
- Run the `install.sh` script to create the service and then start the service from the windows service manager

#### Running as a Linux Service
- Coming Soon
