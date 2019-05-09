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

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Parser;
import org.onebusaway.cli.CommandLineInterfaceLibrary;
import org.onebusaway.guice.jsr250.LifecycleService;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * 
 * @author bdferris
 * Modified by Khoa Tran 
 *
 */

public class CleverAvlForwarderMain {
	
	private static final String ARG_REFRESH_INTERVAL = "refreshInterval";

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

		Options options = new Options();
		buildOptions(options);
		Parser parser = new GnuParser();
		CommandLine cli = parser.parse(options, args);

		Set<Module> modules = new HashSet<Module>();
		CleverToAvlModule.addModuleAndDependencies(modules);
		
		Injector injector = Guice.createInjector(modules);
		injector.injectMembers(this);

		_lifecycleService.start();
	}

	private void printUsage() {
		CommandLineInterfaceLibrary.printUsage(getClass());
	}

	protected void buildOptions(Options options) {
		options.addOption(ARG_REFRESH_INTERVAL, true, "realtime data refresh interval");

	}
}
