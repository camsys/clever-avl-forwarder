### clever-avl-forwarder

Agent that forwards data from a Clever AVL database to and AWS SQS Endpoint. Connection information is defined in a config.properties file. Configuration values listed below.

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
