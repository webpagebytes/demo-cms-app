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

public class ChangePasswordController extends GenericController {

	private void performValidation(HttpServletRequest request,
			Map<String, String> errors)
	{
		String oldPassword = request.getParameter("oldPassword");
		String password = request.getParameter("password");
		String password2 = request.getParameter("password2");
						

		if (oldPassword.length() == 0)
		{
			errors.put("oldPassword", "Error.password.empty");
		} else if (oldPassword.length() > 255)
		{
			errors.put("oldPassword", "Error.password.length");			
		} 
		
		if (password.length() == 0)
		{
			errors.put("password", "Error.password.empty");
		} else if (password.length() > 255)
		{
			errors.put("password", "Error.password.length");			
		} else if (! password.equals(password2))
		{
			errors.put("password", "Error.password.different");						
		}
		
		if (password2.length() == 0)
		{
			errors.put("password2", "Error.password.empty");
		}else if (password2.length() > 255)
		{
			errors.put("password2", "Error.password.length");			
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

		String oldPassword = request.getParameter("oldPassword");
		String password = request.getParameter("password");
		
		
		Map<String, String> errors = new HashMap<String, String>();
		
		performValidation(request, errors);
		
		model.getCmsApplicationModel().put("errors", errors);
		
		if (errors.size() >0)
		{
			String regPageKey = model.getCmsModel().get(WPBModel.URI_PARAMETERS_KEY).get("pageGuid");
			forward.setForwardTo(regPageKey);
			return;
		}
		
		try
		{
			User user = database.getUser(user_id);
				
			String oldPswHash = "";
			String newPasswordHash = "";
			try
			{
				oldPswHash = HashService.getHashSha1(oldPassword.getBytes());
				newPasswordHash = HashService.getHashSha1(password.getBytes());
			}
			catch (NoSuchAlgorithmException e)
			{
			    throw new WPBException("Cannot calculate hash", e);
			}
			
			if (! user.getPassword().equals(oldPswHash))
			{
				errors.put("oldPassword", "Error.password.invalid"); //errors are already in the model
				String regPageKey = model.getCmsModel().get(WPBModel.URI_PARAMETERS_KEY).get("pageGuid");
				forward.setForwardTo(regPageKey);
				return;
			}
			user.setPassword(newPasswordHash);
			database.updateUser(user);
			
		} catch (SQLException e)
		{
			throw new WPBException("Cannot update user", e);
		}
		String redirectUri = model.getCmsModel().get(WPBModel.GLOBALS_KEY).get("PROFILE_URI");
		try
		{
			response.sendRedirect(redirectUri);
		} catch (IOException e)
		{
		    throw new WPBException("Cannot redirect to:" + redirectUri, e);
		}
	}
}