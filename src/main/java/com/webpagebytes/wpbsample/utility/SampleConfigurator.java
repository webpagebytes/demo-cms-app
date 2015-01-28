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

package com.webpagebytes.wpbsample.utility;

import java.io.FileInputStream;

import java.io.IOException;
import java.util.Properties;

public class SampleConfigurator {

	private Properties properties;
	private static SampleConfigurator instance;
	
	private SampleConfigurator() {};
	
	public static void initialize(String configPath) throws IOException
	{
		Properties properties = new Properties();
		properties.load(new FileInputStream(configPath));
		SampleConfigurator instance = new SampleConfigurator();
		instance.properties = properties;
		SampleConfigurator.instance = instance;
	}
	public static SampleConfigurator getInstance()
	{
		return instance;
	}
	public String getConfig(String key)
	{
		if (properties.containsKey(key))
			return properties.get(key).toString();
		else
			return "";
	}

}
