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

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webpagebytes.cms.WPBForward;
import com.webpagebytes.cms.WPBModel;
import com.webpagebytes.cms.exception.WPBException;
import com.webpagebytes.wpbsample.data.Account;
import com.webpagebytes.wpbsample.data.AccountOperation;
import com.webpagebytes.wpbsample.data.Session;

public class HomeController extends GenericController {

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
		    model.getCmsApplicationModel().put("user_id", user_id);
			Account account = database.getAccount(user_id);
			model.getCmsApplicationModel().put("account", account);
			
			Date date = new Date(0);
			List<AccountOperation> accountOperations = database.getAccountOperationsForUser(user_id, date, 10);
			model.getCmsApplicationModel().put("accountOperations", accountOperations);
			
		} catch (SQLException e)
		{
		    throw new WPBException("Cannot get user data", e);
		}
		String pageGuid = model.getCmsModel().get(WPBModel.URI_PARAMETERS_KEY).get("pageGuid");
		forward.setForwardTo(pageGuid);
	}

}
