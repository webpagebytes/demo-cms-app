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
import com.webpagebytes.wpbsample.data.Session;
import com.webpagebytes.wpbsample.data.Transaction;
import com.webpagebytes.wpbsample.data.User;

public class GetUserTransactionsController extends GenericController {

	public void handleRequest(HttpServletRequest request,
			HttpServletResponse response, WPBModel model, WPBForward forward) throws WPBException {
		Session session = getSession(request, response);
		if (false == handleAuthentication(request, response, model, forward, session))
		{
			return;
		}
		Integer user_id = (Integer) session.getSessionMap().get(SESSION_LOGIN_USERID); 
		
		model.getCmsApplicationModel().put(SESSION_LOGIN_USERID, user_id);
		try
		{
		    User user = database.getUser(user_id);
		    model.getCmsApplicationModel().put("user", user);
	        
			int page = 1;
			String pageStr = request.getParameter("page");
			if (pageStr != null)
			{
				try
				{
					page = Integer.valueOf(pageStr);
				} catch (NumberFormatException e)
				{
					page = 1;
				}
			}
			model.getCmsApplicationModel().put("hasNextPage", 0);
			Date date = new Date(0);
			List<Transaction> transactions = database.getTransactionsForUser(user_id, date, page, PAGE_SIZE);
			if (transactions.size() == PAGE_SIZE)
			{
				model.getCmsApplicationModel().put("hasNextPage", 1);
				transactions.remove(PAGE_SIZE-1); // delete the last record
			}
			model.getCmsApplicationModel().put("transactions", transactions);
			model.getCmsApplicationModel().put("page", page);

			String pageGuid = model.getCmsModel().get(WPBModel.URI_PARAMETERS_KEY).get("pageGuid");
			forward.setForwardTo(pageGuid);
			return;
		} catch (SQLException e)
		{
		    throw new WPBException("Cannot getuser data", e);	
		}
	}
}