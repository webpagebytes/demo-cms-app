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

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmailUtility {
	private static final Logger log = Logger.getLogger(EmailUtility.class.getName());
	
	public void sendEmail(String to, String from, String subject, String body, String attachmentName, InputStream attachmentIs)
	{
		log.log(Level.INFO, String.format("EmailUtility send email to: %s, from: %s, subject: %s", to, from, subject));
	}
	
}
