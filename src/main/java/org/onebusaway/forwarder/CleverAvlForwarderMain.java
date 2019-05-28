/**
 * Copyright (C) 2012 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.forwarder;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Parser;
import org.onebusaway.cli.CommandLineInterfaceLibrary;
import org.onebusaway.forwarder.dao.CleverAvlDao;
import org.onebusaway.forwarder.service.ConfigurationService;
import org.onebusaway.guice.jsr250.LifecycleService;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author lcaraballo
 *
 */

public class CleverAvlForwarderMain {

	private static final Logger _log = LoggerFactory.getLogger(CleverAvlForwarderMain.class);

	private static final String CONFIG_FILE = "configFile";
	private static final String SQL_QUERY_FILE = "sqlQueryFile";

	public static void main(String[] args) throws Exception {
		CleverAvlForwarderMain m = new CleverAvlForwarderMain();
		m.run(args);
	}

	private Forwarder _provider;

	private LifecycleService _lifecycleService;

	
	@Inject
  	public void setProvider(Forwarder provider) {
    	_provider = provider;
  	}


	@Inject
	public void setLifecycleService(LifecycleService lifecycleService) {
		_lifecycleService = lifecycleService;
	}

	public void run(String[] args) throws Exception {

		if (CommandLineInterfaceLibrary.wantsHelp(args)) {
			printUsage();
			System.exit(-1);
		}

		Options options = new Options();
		buildOptions(options);
		Parser parser = new GnuParser();
		CommandLine cli = parser.parse(options, args);

		Set<Module> modules = new HashSet<Module>();
		CleverToAvlModule.addModuleAndDependencies(modules);
		
		Injector injector = Guice.createInjector(modules);
		injector.injectMembers(this);

		if(cli.hasOption(CONFIG_FILE)){
			String configFile = cli.getOptionValue(CONFIG_FILE);
			ConfigurationService configService = injector.getInstance(ConfigurationService.class);
			configService.setConfigFile(configFile);
		}

		if(cli.hasOption(SQL_QUERY_FILE)){
			String sqlQueryFile = cli.getOptionValue(SQL_QUERY_FILE);
			try{
				String sqlQuery = new String(Files.readAllBytes(Paths.get(sqlQueryFile)));
				CleverAvlDao cleverAvlDao = injector.getInstance(CleverAvlDao.class);
				cleverAvlDao.setCleverAvlQuery(sqlQuery);
			} catch (Exception e){
				_log.error("Unable to load SQL Query file", e);
			}
		}

		_lifecycleService.start();
	}

	public void stop(){
		System.exit(1);
	}

	private void printUsage() {
		CommandLineInterfaceLibrary.printUsage(getClass());
	}

	protected void buildOptions(Options options) {
		options.addOption(CONFIG_FILE, true, "configuration file location");
		options.addOption(SQL_QUERY_FILE, true, "custom sql query file location");
	}
}
