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

import java.io.ByteArrayInputStream;


import java.io.ByteArrayOutputStream;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.IOUtils;
import org.apache.fop.apps.MimeConstants;

import com.webpagebytes.cms.WPBContentProvider;
import com.webpagebytes.cms.WPBModel;

public class NotificationUtility {
public static final String OUTPUT_STREAM_BODY = "os_body";
public static final String OUTPUT_STREAM_ATTACHMENT = "os_attachment";
public static final String CONTENT_IMG_USERS = "users_img";
public static final String CONTENT_IMG_TRANSACTIONS = "transactions_img";
public static final String CONTENT_IMG_DEPOSITS = "deposits_img";
public static final String CONTENT_IMG_WITHDRAWALS = "withdrawals_img";


public static void fetchReportImages(WPBContentProvider contentProvider, WPBModel model, Map<String, String> content)
{
	ByteArrayOutputStream baos_users_fo = new ByteArrayOutputStream(10000);
	ByteArrayOutputStream baos_transactions_fo = new ByteArrayOutputStream(10000);
	ByteArrayOutputStream baos_deposits_fo = new ByteArrayOutputStream(10000);
	ByteArrayOutputStream baos_withdrawals_fo = new ByteArrayOutputStream(10000);

	ByteArrayOutputStream baos_users = new ByteArrayOutputStream(10000);
	ByteArrayOutputStream baos_transactions = new ByteArrayOutputStream(10000);
	ByteArrayOutputStream baos_deposits = new ByteArrayOutputStream(10000);
	ByteArrayOutputStream baos_withdrawals = new ByteArrayOutputStream(10000);

	String fopTransactionsGuid = model.getCmsModel().get(WPBModel.GLOBALS_KEY).get("imageReportTransactionsGuid");
	String fopUsersGuid = model.getCmsModel().get(WPBModel.GLOBALS_KEY).get("imageReportUsersGuid");
	String fopDepositsGuid = model.getCmsModel().get(WPBModel.GLOBALS_KEY).get("imageReportDepositsGuid");
	String fopWithdrawalsGuid = model.getCmsModel().get(WPBModel.GLOBALS_KEY).get("imageReportWithdrawalsGuid");
	
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

		content.put(CONTENT_IMG_USERS, base64Users);
		content.put(CONTENT_IMG_TRANSACTIONS, base64Transactions);
		content.put(CONTENT_IMG_DEPOSITS, base64Deposits);
		content.put(CONTENT_IMG_WITHDRAWALS, base64Withdrawals);
	} 
	catch (Exception e)
	{
		e.printStackTrace(System.out);
	}
	finally
	{
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

}
