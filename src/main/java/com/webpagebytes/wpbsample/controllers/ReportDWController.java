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
import com.webpagebytes.wpbsample.data.DepositWithdrawal;
import com.webpagebytes.wpbsample.data.Session;
import com.webpagebytes.wpbsample.data.DepositWithdrawal.OperationType;
import com.webpagebytes.wpbsample.utility.DateUtility;

public class ReportDWController extends GenericController {
	
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
		Map<String, Long> amountDepositsPerDays = new HashMap<String, Long>();
		Map<String, Integer> numDepositsPerDays = new HashMap<String, Integer>();
		Map<String, Long> amountWithdrawalsPerDays = new HashMap<String, Long>();
		Map<String, Integer> numWithdrawalsPerDays = new HashMap<String, Integer>();

		for(int i = 0; i<= interval-1 ; i++)
		{
			
			Date aDate = DateUtility.addDays(past, i);
			dates.add(aDate);
			String iStr = String.valueOf(i);
			amountDepositsPerDays.put(iStr, 0L);
			numDepositsPerDays.put(iStr, 0);
			amountWithdrawalsPerDays.put(iStr, 0L);
			numWithdrawalsPerDays.put(iStr, 0);
			keys.add(iStr);
		}
		int maxNumOperations = 0;
		long maxAmountPerDay = 0;
		OutputStream os = null;
		try
		{
			List<DepositWithdrawal> operations = database.getDepositsWithdrawalsForUser(user_id, DepositWithdrawal.OperationType.DEPOSIT, past, 1, MAX_RECORDS);
			List<DepositWithdrawal> withdrawals = database.getDepositsWithdrawalsForUser(user_id, DepositWithdrawal.OperationType.WITHDRAWAL, past, 1, MAX_RECORDS);
			operations.addAll(withdrawals);
			for(DepositWithdrawal operation: operations)
			{
				Date aDate = operation.getDate();
				long key = Math.abs(aDate.getTime()-past.getTime())/(1000*60*60*24);
				String keyStr = String.valueOf(key);
				if (operation.getType() == OperationType.DEPOSIT)
				{
					
					Long value = amountDepositsPerDays.get(keyStr);
					if (value == null) value = 0L;
					value += operation.getAmount();
					amountDepositsPerDays.put(keyStr, value);
					
					Integer count = numDepositsPerDays.get(keyStr);
					if (count == null) count = 0;
					count += 1;
					numDepositsPerDays.put(keyStr, count);
				} else
				{
					
					Long value = amountWithdrawalsPerDays.get(keyStr);
					if (value == null) value = 0L;
					value += operation.getAmount();
					amountWithdrawalsPerDays.put(keyStr, value);
					
					Integer count = numWithdrawalsPerDays.get(keyStr);
					if (count == null) count = 0;
					count += 1;
					numWithdrawalsPerDays.put(keyStr, count);
				}
			}
			for(int i=0; i<keys.size(); i++)
			{
				int count = numDepositsPerDays.get(keys.get(i));
				if (maxNumOperations < count)
				{
					maxNumOperations = count;
				}
				long aValue = amountDepositsPerDays.get(keys.get(i));
				if (maxAmountPerDay < aValue)
				{
					maxAmountPerDay = aValue;
				}
				count = numWithdrawalsPerDays.get(keys.get(i));
				if (maxNumOperations < count)
				{
					maxNumOperations = count;
				}
				aValue = amountWithdrawalsPerDays.get(keys.get(i));
				if (maxAmountPerDay < aValue)
				{
					maxAmountPerDay = aValue;
				}
			}
			model.getCmsApplicationModel().put("interval", interval);
			model.getCmsApplicationModel().put("maxAmountPerDay", maxAmountPerDay);
			model.getCmsApplicationModel().put("maxNumOperations", maxNumOperations);
			model.getCmsApplicationModel().put("dates", dates);
			model.getCmsApplicationModel().put("keys", keys);
			model.getCmsApplicationModel().put("amountDepositsPerDays", amountDepositsPerDays);
			model.getCmsApplicationModel().put("amountWithdrawalsPerDays", amountWithdrawalsPerDays);
			model.getCmsApplicationModel().put("numDepositsPerDays", numDepositsPerDays);
			model.getCmsApplicationModel().put("numWithdrawalsPerDays", numWithdrawalsPerDays);
				
			
			response.setContentType("text/html");
			os = response.getOutputStream();
			String pageGuid = model.getCmsModel().get(WPBModel.URI_PARAMETERS_KEY).get("pageGuid");
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
