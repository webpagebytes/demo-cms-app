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
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webpagebytes.cms.WPBForward;
import com.webpagebytes.cms.WPBModel;
import com.webpagebytes.cms.exception.WPBException;
import com.webpagebytes.wpbsample.data.Account;
import com.webpagebytes.wpbsample.data.DepositWithdrawal;
import com.webpagebytes.wpbsample.data.Session;
import com.webpagebytes.wpbsample.data.User;
import com.webpagebytes.wpbsample.data.DepositWithdrawal.OperationType;
import com.webpagebytes.wpbsample.utility.HashService;

public class PerformWithdrawalController extends GenericController {

private void performValidation(HttpServletRequest request, String passwordHash, Long maxWithdrawal,
		Map<String, String> errors, Map<String, String> values)
{
	String amountStr = request.getParameter("amount");
	String password = request.getParameter("password");
	
	Long amount = 0L;
	try
	{
		amount = Long.valueOf(amountStr);
	} catch (NumberFormatException e)
	{
		errors.put("amount", "Error.amount.format");
	}
	if (amount > maxWithdrawal)
	{
		errors.put("amount", "Error.amount.toolarge");
	} else if (amount == 0L)
	{
		errors.put("amount", "Error.amount.zeronotallowed");	
	}
	if (errors.containsKey("amount"))
	{
		values.put("amount", "");	
	} else
	{
		values.put("amount", amountStr);
	}
	
	if (password.length() == 0)
	{
		errors.put("password", "Error.password.empty");
	} else 
	{
		try
		{
			String enteredHash = HashService.getHashSha1(password.getBytes());
			if (!enteredHash.equals(passwordHash))
			{
				errors.put("password", "Error.password.invalid");
			}
		} catch (NoSuchAlgorithmException e)
		{
			
		}		
	}
}

public void handleRequest(HttpServletRequest request,
		HttpServletResponse response, WPBModel model, WPBForward forward) throws WPBException {
	Session session = getSession(request, response);
	if (false == handleAuthentication(request, response, model, forward, session))
	{
		return;
	}
	Integer user_id = (Integer) session.getSessionMap().get(SESSION_LOGIN_USERID); 
	
	try
	{
		User user = database.getUser(user_id);
		Account account = database.getAccount(user_id);
		Map<String, String> errors = new HashMap<String, String>();
		Map<String, String> values = new HashMap<String, String>();
		performValidation(request, user.getPassword(), account.getBalance(), errors, values);
		
		model.getCmsApplicationModel().put("errors", errors);
		model.getCmsApplicationModel().put("account", account);
		
		if (errors.size() >0)
		{
			model.getCmsApplicationModel().put("values", values);			
			String pageGuid = model.getCmsModel().get(WPBModel.URI_PARAMETERS_KEY).get("pageGuid");
			forward.setForwardTo(pageGuid);
			return;
		}

		Long amount = Long.valueOf(request.getParameter("amount"));
		DepositWithdrawal operation = database.createDepositOrWithdrawal(user_id, OperationType.WITHDRAWAL, amount);
		String successUri = model.getCmsModel().get(WPBModel.URI_PARAMETERS_KEY).get("successUri");
		try
		{
			successUri = successUri.concat("?id=").concat(String.valueOf(operation.getId()));
			response.sendRedirect(successUri);
		} catch (IOException e)
		{
		    throw new WPBException("Cannot redirect to:" + successUri, e);
		}
	} catch (SQLException e)
	{
	    throw new WPBException("Cannot update data", e);
	}
}

}
