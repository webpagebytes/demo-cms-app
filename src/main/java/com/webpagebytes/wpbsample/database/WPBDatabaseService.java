/*
 *   Copyright 2015 Webpagebytes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.webpagebytes.wpbsample.database;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.webpagebytes.wpbsample.utility.SampleConfigurator;

public class WPBDatabaseService {
	private static final Logger log = Logger.getLogger(WPBDatabaseService.class.getName());
	
	public static final String DB_PROPS_CONNECTION_URL = "db_connectionUrl";
	public static final String DB_PROPS_DRIVER_CLASS = "db_driverClass";
	public static final String DB_PROPS_USER_NAME = "db_userName";
	public static final String DB_PROPS_PASSWORD = "db_password";
	
	private static WPBDatabase database;
	private static final Object lock = new Object(); 
	public static WPBDatabase getInstance()
	{
		if (database == null)
		{
			synchronized (lock)
			{
				if (database == null) {
					SampleConfigurator configurator = SampleConfigurator.getInstance();
					Map<String, String> dbProps = new HashMap<String, String>();
					dbProps.put(DB_PROPS_DRIVER_CLASS, configurator.getConfig(DB_PROPS_DRIVER_CLASS) );
					dbProps.put(DB_PROPS_CONNECTION_URL, configurator.getConfig(DB_PROPS_CONNECTION_URL));
					dbProps.put(DB_PROPS_USER_NAME, configurator.getConfig(DB_PROPS_USER_NAME));
					dbProps.put(DB_PROPS_PASSWORD, configurator.getConfig(DB_PROPS_PASSWORD));					
					log.log(Level.INFO, " opening DB for " + configurator.getConfig(DB_PROPS_CONNECTION_URL));
					database = new WPBDatabase(dbProps);
				}
			}
		}
		return database;
	}
}
