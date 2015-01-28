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

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webpagebytes.cms.WPBForward;
import com.webpagebytes.cms.WPBModel;
import com.webpagebytes.cms.exception.WPBException;
import com.webpagebytes.wpbsample.data.Session;
import com.webpagebytes.wpbsample.data.Transaction;
import com.webpagebytes.wpbsample.utility.DateUtility;

public class ReportTransactionsController extends GenericController {
	
	private int getInterval(WPBModel model)
	{
		String intervalStr = model.getCmsModel().get(WPBModel.URI_PARAMETERS_KEY).get("interval");
		int interval = 1;
		try
		{
			interval = Integer.valueOf(intervalStr);
		}
		catch (NumberFormatException e)
		{
			// do nothing, go with the default
		}
		switch (interval)
		{
			case 1:
			case 7:
			case 30:
				break;
			default:
				interval = 1;
		}	
		return interval;
	}
	
	public void handleRequest(HttpServletRequest request,
			HttpServletResponse response, WPBModel model, WPBForward forward) throws WPBException {
		Session session = getSession(request, response);
		if (false == handleAuthentication(request, response, model, forward, session))
		{
			return;
		}
		Integer user_id = (Integer) session.getSessionMap().get(SESSION_LOGIN_USERID); 
		
		int interval = getInterval(model);
		
		
		Date past = DateUtility.addDays(new Date(), -interval);
		List<Date> dates = new ArrayList<Date>();
		List<String> keys = new ArrayList<String>();
		Map<String, Long> receiveAmountPerDays = new HashMap<String, Long>();
		Map<String, Integer> receiveTransactionsPerDays = new HashMap<String, Integer>();
		Map<String, Long> paidAmountPerDays = new HashMap<String, Long>();
		Map<String, Integer> paidTransactionsPerDays = new HashMap<String, Integer>();

		for(int i = 0; i<= interval-1 ; i++)
		{
			
			Date aDate = DateUtility.addDays(past, i);
			dates.add(aDate);
			receiveAmountPerDays.put(String.valueOf(i), 0L);
			receiveTransactionsPerDays.put(String.valueOf(i), 0);
			paidAmountPerDays.put(String.valueOf(i), 0L);
			paidTransactionsPerDays.put(String.valueOf(i), 0);
			keys.add(String.valueOf(i));
		}
		int maxNumTransactions = 0;
		long maxAmountPerDay = 0;
		OutputStream os = null;
		try
		{
			List<Transaction> transactions = database.getTransactionsForUser(user_id, past, 1, MAX_RECORDS);
			for(Transaction transaction: transactions)
			{
				Date aDate = transaction.getDate();
				long key = Math.abs(aDate.getTime()-past.getTime())/(1000*60*60*24);
				String keyStr = String.valueOf(key);
				if (transaction.getSource_user_id() == user_id)
				{
					// this is an outgoing transaction
					Long value = paidAmountPerDays.get(keyStr);
					if (value == null) value = 0L;
					value += transaction.getAmount();
					paidAmountPerDays.put(keyStr, value);
					
					Integer count = paidTransactionsPerDays.get(keyStr);
					if (count == null) count = 0;
					count += 1;
					paidTransactionsPerDays.put(keyStr, count);
				} else
				{
					// this is an incomming transaction		
					Long value = receiveAmountPerDays.get(keyStr);
					if (value == null) value = 0L;
					value += transaction.getAmount();
					receiveAmountPerDays.put(keyStr, value);
					
					Integer count = receiveTransactionsPerDays.get(keyStr);
					if (count == null) count = 0;
					count += 1;
					receiveTransactionsPerDays.put(keyStr, count);
				}
			}
			for(int i=0; i<keys.size(); i++)
			{
				int count = paidTransactionsPerDays.get(keys.get(i));
				if (maxNumTransactions < count)
				{
					maxNumTransactions = count;
				}
				long aValue = paidAmountPerDays.get(keys.get(i));
				if (maxAmountPerDay < aValue)
				{
					maxAmountPerDay = aValue;
				}
				count = receiveTransactionsPerDays.get(keys.get(i));
				if (maxNumTransactions < count)
				{
					maxNumTransactions = count;
				}
				aValue = receiveAmountPerDays.get(keys.get(i));
				if (maxAmountPerDay < aValue)
				{
					maxAmountPerDay = aValue;
				}
			}
			model.getCmsApplicationModel().put("interval", interval);
			model.getCmsApplicationModel().put("maxAmountPerDay", maxAmountPerDay);
			model.getCmsApplicationModel().put("maxNumTransactions", maxNumTransactions);
			model.getCmsApplicationModel().put("dates", dates);
			model.getCmsApplicationModel().put("keys", keys);
			model.getCmsApplicationModel().put("paidAmountPerDays", paidAmountPerDays);
			model.getCmsApplicationModel().put("paidTransactionsPerDays", paidTransactionsPerDays);
			model.getCmsApplicationModel().put("receiveAmountPerDays", receiveAmountPerDays);
			model.getCmsApplicationModel().put("receiveTransactionsPerDays", receiveTransactionsPerDays);
				
			
			response.setContentType("text/html");
			os = response.getOutputStream();
			String pageGuid = model.getCmsModel().get(WPBModel.URI_PARAMETERS_KEY).get("pageGuid");
			//this.getContent(model, MimeConstants.MIME_PNG, os);
			this.contentProvider.writePageContent(pageGuid, model, os);
			
		} catch (Exception e)
		{
		    throw new WPBException("Cannot generate content", e);
		}
		finally
		{
			if (os != null)
			{
				try
				{
					os.flush();
					os.close();
				} catch (IOException e)
				{
					
				}
			}
		}
		
	}
}
