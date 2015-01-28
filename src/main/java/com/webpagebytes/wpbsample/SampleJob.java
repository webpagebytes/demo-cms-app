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

package com.webpagebytes.wpbsample;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.fop.apps.MimeConstants;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.webpagebytes.cms.WPBContentService;
import com.webpagebytes.cms.WPBContentServiceFactory;
import com.webpagebytes.cms.WPBContentProvider;
import com.webpagebytes.cms.WPBModel;
import com.webpagebytes.wpbsample.utility.DateUtility;
import com.webpagebytes.wpbsample.utility.EmailUtility;
import com.webpagebytes.wpbsample.utility.EmailUtilityFactory;
import com.webpagebytes.wpbsample.utility.NotificationUtility;
import com.webpagebytes.wpbsample.utility.SampleFopService;

public class SampleJob implements Job {
	private static final Logger log = Logger.getLogger(SampleJob.class.getName());

	public void execute(JobExecutionContext context)
			throws JobExecutionException {		 
				log.log(Level.INFO, "Sample quartz scheduler started");	

				
				// create the email subject value
				Date yesterday = DateUtility.addDays(DateUtility.getToday(), -1);
				String subject = String.format("Sample webpagebytes application report(%d/%d/%d)", 1900+yesterday.getYear(), yesterday.getMonth()+1, yesterday.getDate());

				// various variables used during report generation
				ByteArrayOutputStream bos_emailBody = new ByteArrayOutputStream(4096);
				ByteArrayOutputStream bos_emailAttachmentFop = new ByteArrayOutputStream(4096);
				InputStream is_emailAttachmentFop = null;
				ByteArrayOutputStream bos_emailAttachmentPdf = new ByteArrayOutputStream(4096);
				ByteArrayInputStream bis_emailAttachmentPdf = null;
				
				try
				{
					// get the content provider instance
					WPBContentService contentService = WPBContentServiceFactory.getInstance();
					WPBContentProvider contentProvider = contentService.getContentProvider();
					
					// create the cmd model 
					WPBModel model = contentService.createModel();
					
					//get the report images for users, transactions, deposits and withdrawals
					// the images and stored in a map, the image content is base64 encoded
					Map<String, String> contentImages = new HashMap<String, String>();
					NotificationUtility.fetchReportImages(contentProvider, model, contentImages);
					
					//populate the model with image values
					model.getCmsApplicationModel().put("users_img", contentImages.get(NotificationUtility.CONTENT_IMG_USERS));
					model.getCmsApplicationModel().put("transactions_img", contentImages.get(NotificationUtility.CONTENT_IMG_TRANSACTIONS));
					model.getCmsApplicationModel().put("deposits_img", contentImages.get(NotificationUtility.CONTENT_IMG_DEPOSITS));
					model.getCmsApplicationModel().put("withdrawals_img", contentImages.get(NotificationUtility.CONTENT_IMG_WITHDRAWALS));

					// get from the GLOBALS parameters the user who will receive the email notification
					String notificationEmailAddress = model.getCmsModel().get(WPBModel.GLOBALS_KEY).get("NOTIFICATIONS_EMAIL_ADDRESS");
					
					// get from the GLOBALS parameters the page guid with the email body template
					String notificationEmailPageGuid = model.getCmsModel().get(WPBModel.GLOBALS_KEY).get("NOTIFICATIONS_EMAIL_PAGE_GUID");
					
					// get from the GLOBALS parameters the page guid with the PDF report XSL-FO template
					String notificationEmailAttachmentGuid = model.getCmsModel().get(WPBModel.GLOBALS_KEY).get("NOTIFICATIONS_EMAIL_PDF_GUID");
					
					if (notificationEmailAddress == null || notificationEmailPageGuid == null || notificationEmailAttachmentGuid == null)
					{
						// if any of these is not set then do not sent the email
							log.log(Level.WARNING, "Sample scheduler not properly configured, will not send any email");
							return;
					}
					
					// translate the email body template into the actual body content
					contentProvider.writePageContent(notificationEmailPageGuid, model, bos_emailBody);					
		
					// translate the attachment XSL FO template into the actual XSL FO content
					contentProvider.writePageContent(notificationEmailAttachmentGuid, model, bos_emailAttachmentFop);					
					
					// need to convert the attachment XSL FO OutputStream into InputStream
					is_emailAttachmentFop = new ByteArrayInputStream(bos_emailAttachmentFop.toByteArray());
					
					SampleFopService fopService = SampleFopService.getInstance();
					fopService.getContent(is_emailAttachmentFop, MimeConstants.MIME_PDF, bos_emailAttachmentPdf);
					
					//need to convert the attachment PDF OutputStream into InputStream
					bis_emailAttachmentPdf = new ByteArrayInputStream(bos_emailAttachmentPdf.toByteArray());
					
					// now we have the email subject, email body, email attachment, we can sent it
					if (notificationEmailAddress.length() > 0)
					{
						EmailUtility emailUtility = EmailUtilityFactory.getInstance();
						emailUtility.sendEmail(notificationEmailAddress, "no-reply@webpagebytes.com", 
								subject,
								bos_emailBody.toString("UTF-8"),
								"report.pdf",
								bis_emailAttachmentPdf);
					}
					log.log(Level.INFO, "Sample quartz scheduler completed with success");	
				
				} catch (Exception e)
				{
					log.log(Level.SEVERE, "Exception on quartz scheduler", e);	
				}
				finally 
				{
					IOUtils.closeQuietly(bos_emailBody);
					IOUtils.closeQuietly(bos_emailAttachmentFop);
					IOUtils.closeQuietly(is_emailAttachmentFop);
					IOUtils.closeQuietly(bis_emailAttachmentPdf);
					IOUtils.closeQuietly(bos_emailAttachmentPdf);
				}
				
			}
		 
}
