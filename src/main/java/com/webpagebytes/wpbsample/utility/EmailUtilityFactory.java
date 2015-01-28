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

import java.util.logging.Level;
import java.util.logging.Logger;

public class EmailUtilityFactory {
	protected static EmailUtility instance;
	private static final Logger log = Logger.getLogger(EmailUtilityFactory.class.getName());
	
	public static EmailUtility getInstance()
	{
		if (instance == null)
		{
			String emailClass = SampleConfigurator.getInstance().getConfig("emailUtilityClass");
			if (null == emailClass)
			{
				log.log(Level.INFO, "No EmailUtilityClass parameter, so we go with the default (EmailUtility class)");
				instance = new EmailUtility();
				return instance;
			}
			try
			{
				instance = (EmailUtility) Class.forName(emailClass).newInstance();
			} catch (Exception e)
			{
				log.log(Level.SEVERE, "Cannot instantiate EmailUtility class for:" + emailClass);
			}
		}
		return instance;
	}
}
