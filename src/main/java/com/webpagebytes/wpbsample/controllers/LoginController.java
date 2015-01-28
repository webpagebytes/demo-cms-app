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
import com.webpagebytes.wpbsample.data.Session;
import com.webpagebytes.wpbsample.data.User;
import com.webpagebytes.wpbsample.utility.HashService;

public class LoginController extends BaseController {

	
	private void performValidation(HttpServletRequest request,
			Map<String, String> errors, Map<String, String> values)
	{
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		
		if (username.length() == 0 || username.length() > 255)
		{
			errors.put("username", "Error.username.length");
		} else if (!username.matches("[0-9a-zA-Z_@.-]*"))
		{
			errors.put("username", "Error.username.format");
		} 
		if (errors.containsKey("username"))
		{
			values.put("username", "");	
		} else
		{
			values.put("username", username);
		}
		
		if (password.length() == 0)
		{
			errors.put("password", "Error.password.empty");
		} else if (password.length() > 255)
		{
			errors.put("password", "Error.password.length");			
		}		
	}
	
	public void handleRequest(HttpServletRequest request,
			HttpServletResponse response, WPBModel model, WPBForward forward) throws WPBException {
		Session session = getSession(request, response);
		session.getSessionMap().clear();
		session.setUser_id(null);
		try
		{
			database.setSession(session);
		} catch (SQLException e)
		{
		    throw new WPBException("Cannot set user session", e);		
		}
		
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		
		Map<String, String> errors = new HashMap<String, String>();
		Map<String, String> values = new HashMap<String, String>();
		
		performValidation(request, errors, values);
		
		model.getCmsApplicationModel().put("errors", errors);

		if (errors.size() >0)
		{
			model.getCmsApplicationModel().put("values", values);			
			String regPageKey = model.getCmsModel().get(WPBModel.GLOBALS_KEY).get("LOGIN_PAGE_KEY");
			forward.setForwardTo(regPageKey);
			return;
		}
		
		User user = null;
		try
		{
			user = database.getUser(username);
		} catch (SQLException e)
		{
		    throw new WPBException("Cannot get user", e);	
		}
		
		if (user == null)
		{
			values.clear();
			errors.put("username", "Error.username.notfound");			
			String regPageKey = model.getCmsModel().get(WPBModel.GLOBALS_KEY).get("LOGIN_PAGE_KEY");
			forward.setForwardTo(regPageKey);
			return;
		}
		
		String hashDB = user.getPassword();
		String hashUser = "";
		try
		{
			hashUser = HashService.getHashSha1(password.getBytes());
		} catch (NoSuchAlgorithmException e)
		{
		    throw new WPBException("Cannot calculate hash", e);	
		}
		
		if (! hashDB.equals(hashUser))
		{
			values.clear();
			errors.put("username", "Error.username.notfound");			
			String regPageKey = model.getCmsModel().get(WPBModel.GLOBALS_KEY).get("LOGIN_PAGE_KEY");
			forward.setForwardTo(regPageKey);
			return;			
		}
				
		try
		{
			session.setUser_id(user.getId());
			
			session.getSessionMap().put(SESSION_LOGIN_USERID, user.getId());
			session.getSessionMap().put(SESSION_LOGIN_USERNAME, user.getUserName());
			
			model.getCmsApplicationModel().put(SESSION_LOGIN_USERID, user.getId());
			model.getCmsApplicationModel().put(SESSION_LOGIN_USERNAME, user.getUserName());
			
			database.setSession(session);		
		} catch (SQLException e)
		{
		    throw new WPBException("Cannot set session", e);
		}
		String redirectUri = model.getCmsModel().get(WPBModel.GLOBALS_KEY).get("HOME_URI");
		try
		{
			response.sendRedirect(redirectUri);
		} catch (IOException e)
		{
		    throw new WPBException("Cannot redirect to:" + redirectUri, e);
		}
	}

}
