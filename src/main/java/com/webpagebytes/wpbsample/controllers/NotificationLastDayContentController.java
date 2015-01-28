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

package com.webpagebytes.wpbsample.controllers;

import java.io.ByteArrayInputStream;



import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.IOUtils;
import org.apache.fop.apps.MimeConstants;

import com.webpagebytes.cms.WPBContentProvider;
import com.webpagebytes.cms.WPBForward;
import com.webpagebytes.cms.WPBModel;
import com.webpagebytes.cms.WPBRequestHandler;
import com.webpagebytes.cms.exception.WPBException;
import com.webpagebytes.wpbsample.utility.NotificationUtility;
import com.webpagebytes.wpbsample.utility.SampleFopService;

public class NotificationLastDayContentController implements WPBRequestHandler {

	private WPBContentProvider contentProvider;
	@Override
	
	
	public void handleRequest(HttpServletRequest request,
			HttpServletResponse response, WPBModel model, WPBForward forward) throws WPBException {

		// various variables used during report generation
		ByteArrayOutputStream bos_emailBody = new ByteArrayOutputStream(4096);
		ByteArrayOutputStream bos_emailAttachmentFop = new ByteArrayOutputStream(4096);
		InputStream is_emailAttachmentFop = null;
		ByteArrayOutputStream bos_emailAttachmentPdf = new ByteArrayOutputStream(4096);
		ByteArrayInputStream bis_emailAttachmentPdf = null;
		
		try
		{			
			//get the report images for users, transactions, deposits and withdrawals
			// the images and stored in a map, the image content is base64 encoded
			Map<String, String> contentImages = new HashMap<String, String>();
			NotificationUtility.fetchReportImages(contentProvider, model, contentImages);
			
			//populate the model with image values
			model.getCmsApplicationModel().put("users_img", contentImages.get(NotificationUtility.CONTENT_IMG_USERS));
			model.getCmsApplicationModel().put("transactions_img", contentImages.get(NotificationUtility.CONTENT_IMG_TRANSACTIONS));
			model.getCmsApplicationModel().put("deposits_img", contentImages.get(NotificationUtility.CONTENT_IMG_DEPOSITS));
			model.getCmsApplicationModel().put("withdrawals_img", contentImages.get(NotificationUtility.CONTENT_IMG_WITHDRAWALS));
			
			// get from the GLOBALS parameters the page guid with the email body template
			String notificationEmailPageGuid = model.getCmsModel().get(WPBModel.GLOBALS_KEY).get("NOTIFICATIONS_EMAIL_PAGE_GUID");
			
			// get from the GLOBALS parameters the page guid with the PDF report XSL-FO template
			String notificationEmailAttachmentGuid = model.getCmsModel().get(WPBModel.GLOBALS_KEY).get("NOTIFICATIONS_EMAIL_PDF_GUID");
						


			String type= model.getCmsModel().get(WPBModel.URI_PARAMETERS_KEY).get("contentType");
			if (type != null && type.equalsIgnoreCase("pdf"))
			{
				// the response is pdf
				
				// translate the attachment XSL FO template into the actual XSL FO content
				contentProvider.writePageContent(notificationEmailAttachmentGuid, model, bos_emailAttachmentFop);					

				response.setContentType("application/pdf");
				// need to convert the attachment XSL FO OutputStream into InputStream
				is_emailAttachmentFop = new ByteArrayInputStream(bos_emailAttachmentFop.toByteArray());
				
				SampleFopService fopService = SampleFopService.getInstance();
				fopService.getContent(is_emailAttachmentFop, MimeConstants.MIME_PDF, bos_emailAttachmentPdf);

				//need to convert the attachment PDF OutputStream into InputStream
				bis_emailAttachmentPdf = new ByteArrayInputStream(bos_emailAttachmentPdf.toByteArray());

				IOUtils.copy(bis_emailAttachmentPdf, response.getOutputStream());
			} else
			{
				// the response is text/html
				// translate the email body template into the actual body content
				response.setContentType("text/html");
				contentProvider.writePageContent(notificationEmailPageGuid, model, response.getOutputStream());					
			}
			
		
		} catch (Exception e)
		{
		    throw new WPBException("Cannot generate content", e);
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
	public void handleRequest1(HttpServletRequest request,
			HttpServletResponse response, WPBModel model, WPBForward forward) {
		
		OutputStream os = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream(10000);
		ByteArrayInputStream bis = null;
		
		ByteArrayOutputStream baos_users_fo = new ByteArrayOutputStream(10000);
		ByteArrayOutputStream baos_transactions_fo = new ByteArrayOutputStream(10000);
		ByteArrayOutputStream baos_deposits_fo = new ByteArrayOutputStream(10000);
		ByteArrayOutputStream baos_withdrawals_fo = new ByteArrayOutputStream(10000);

		ByteArrayOutputStream baos_users = new ByteArrayOutputStream(10000);
		ByteArrayOutputStream baos_transactions = new ByteArrayOutputStream(10000);
		ByteArrayOutputStream baos_deposits = new ByteArrayOutputStream(10000);
		ByteArrayOutputStream baos_withdrawals = new ByteArrayOutputStream(10000);

		String fopTransactionsGuid = model.getCmsModel().get(WPBModel.URI_PARAMETERS_KEY).get("pageTransactionsGuid");
		String fopUsersGuid = model.getCmsModel().get(WPBModel.URI_PARAMETERS_KEY).get("pageUsersGuid");
		String fopDepositsGuid = model.getCmsModel().get(WPBModel.URI_PARAMETERS_KEY).get("pageDepositsGuid");
		String fopWithdrawalsGuid = model.getCmsModel().get(WPBModel.URI_PARAMETERS_KEY).get("pageWithdrawalsGuid");
		
		contentProvider.writePageContent(fopUsersGuid, model, baos_users_fo);
		model.getCmsApplicationModel().put("type", "tx");
		contentProvider.writePageContent(fopTransactionsGuid, model, baos_transactions_fo);
		model.getCmsApplicationModel().put("type", "d");
		contentProvider.writePageContent(fopDepositsGuid, model, baos_deposits_fo);
		model.getCmsApplicationModel().put("type", "w");
		contentProvider.writePageContent(fopWithdrawalsGuid, model, baos_withdrawals_fo);
		
		ByteArrayInputStream bis_users = new ByteArrayInputStream(baos_users_fo.toByteArray());;
		ByteArrayInputStream bis_transactions = new ByteArrayInputStream(baos_transactions_fo.toByteArray());;
		ByteArrayInputStream bis_deposits = new ByteArrayInputStream(baos_deposits_fo.toByteArray());;
		ByteArrayInputStream bis_withdrawals = new ByteArrayInputStream(baos_withdrawals_fo.toByteArray());;
		
				
		try
		{
			SampleFopService fopService = SampleFopService.getInstance();
			
			fopService.getContent(bis_users, MimeConstants.MIME_PNG, baos_users);		
			fopService.getContent(bis_transactions, MimeConstants.MIME_PNG, baos_transactions);		
			fopService.getContent(bis_deposits, MimeConstants.MIME_PNG, baos_deposits);		
			fopService.getContent(bis_withdrawals, MimeConstants.MIME_PNG, baos_withdrawals);		
			
			String base64Users = DatatypeConverter.printBase64Binary(baos_users.toByteArray());
			String base64Transactions = DatatypeConverter.printBase64Binary(baos_transactions.toByteArray());
			String base64Deposits = DatatypeConverter.printBase64Binary(baos_deposits.toByteArray());
			String base64Withdrawals = DatatypeConverter.printBase64Binary(baos_withdrawals.toByteArray());

			model.getCmsApplicationModel().put("users_img", base64Users);
			model.getCmsApplicationModel().put("transactions_img", base64Transactions);
			model.getCmsApplicationModel().put("deposits_img", base64Deposits);
			model.getCmsApplicationModel().put("withdrawals_img", base64Withdrawals);
			
			
			
			String fopPageGuid = model.getCmsModel().get(WPBModel.URI_PARAMETERS_KEY).get("pageGuid");
			contentProvider.writePageContent(fopPageGuid, model, baos);
			bis = new ByteArrayInputStream(baos.toByteArray());
			baos.close();
			
			String contentType = model.getCmsModel().get(WPBModel.URI_PARAMETERS_KEY).get("contentType");
			if (contentType.equalsIgnoreCase("pdf"))
			{
				response.setContentType("application/pdf");
				os = response.getOutputStream();
				fopService.getContent(bis, MimeConstants.MIME_PDF, os);
			} else
			{
				response.setContentType("text/html");
				os = response.getOutputStream();
				IOUtils.copy(bis, os);				
			}
		} 
		catch (Exception e)
		{
			e.printStackTrace(System.out);
		}
		finally
		{
			IOUtils.closeQuietly(os);
			IOUtils.closeQuietly(baos);
			IOUtils.closeQuietly(bis);
			IOUtils.closeQuietly(baos_users_fo);
			IOUtils.closeQuietly(baos_transactions_fo);
			IOUtils.closeQuietly(baos_deposits_fo);
			IOUtils.closeQuietly(baos_withdrawals_fo);
			IOUtils.closeQuietly(baos_users);
			IOUtils.closeQuietly(baos_transactions);
			IOUtils.closeQuietly(baos_deposits);
			IOUtils.closeQuietly(baos_withdrawals);
		}	
	}

	@Override
	public void initialize(WPBContentProvider contentProvider) {
		// TODO Auto-generated method stub
		this.contentProvider = contentProvider;
	}

}
