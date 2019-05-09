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

package org.onebusaway.forwarder.sql.connection;

/**
 * 
 * @author Khoa Tran
 *
 */

@SuppressWarnings("serial")
public class Properties extends java.util.Properties{

	public String getHost() {
		return getProperty("sql.host");
	}

	public String getDatabaseName() {
		return getProperty("sql.databaseName");
	}

    public String getInstanceName() {
        return getProperty("sql.instanceName");
    }

	public Integer getQueryTimeout() {
		try {
			return Integer.parseInt(getProperty("sql.queryTimeout"));
		} catch (NumberFormatException nfe) {
			return null;
		}
	}

	public Integer getPortNumber() {
	    try {
            return Integer.parseInt(getProperty("sql.portNumber"));
        } catch (NumberFormatException nfe) {
	        return null;
        }
	}

	public String getUser() {
		return getProperty("sql.user");
	}

	public String getPassword() {
		return getProperty("sql.password");
	}

	public Integer getRefreshInterval() {
		try {
			return Integer.parseInt(getProperty("sql.refreshInterval"));
		} catch (NumberFormatException nfe) {
			return null;
		}
	}

	public String getAwsKey() { return getProperty("aws.key"); }

	public String getAwsSecret() { return getProperty("aws.secret"); }

	public String getSqsUrl() { return getProperty("aws.sqs.url"); }

	public Integer getSendThreadsNumber() {
		try {
			return Integer.parseInt(getProperty("aws.sqs.sendThreads"));
		} catch (NumberFormatException nfe) {
			return null;
		}
	}

	public boolean getDebugFlag() {
		try {
			return Boolean.parseBoolean(getProperty("aws.sqs.debugFlag"));
		} catch (Exception e) {
			return false;
		}
	}


}
