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

import com.sendgrid.*;
import com.sendgrid.SendGrid.Email;

public class SendGridEmailUtility extends EmailUtility {
	private static final Logger log = Logger.getLogger(SendGridEmailUtility.class.getName());
	
	public void sendEmail(String to, String from, String subject, String body, String attachmentName, InputStream attachmentIs)
	{
	
		String sendgrid_username = SampleConfigurator.getInstance().getConfig("sendGridUserName");
		String sendgrid_password = SampleConfigurator.getInstance().getConfig("sendGridPassword");

		SendGrid sendgrid = new SendGrid(sendgrid_username, sendgrid_password);
		Email email = new Email();
		email.addTo(to);
		email.setFrom(from);
		email.setSubject(subject);
		email.setHtml(body);
		try
		{
			if (attachmentName != null && attachmentIs != null)
			{
				email.addAttachment(attachmentName, attachmentIs);
			}
			sendgrid.send(email);
		} catch (Exception e)
		{
			log.log(Level.SEVERE, "error sending email with sendgrid", e);
			// TBD
		}
	}
}
