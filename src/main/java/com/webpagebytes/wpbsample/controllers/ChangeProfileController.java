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
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webpagebytes.cms.WPBForward;
import com.webpagebytes.cms.WPBModel;
import com.webpagebytes.cms.exception.WPBException;
import com.webpagebytes.wpbsample.data.Session;
import com.webpagebytes.wpbsample.data.User;
import com.webpagebytes.wpbsample.utility.HashService;

public class ChangeProfileController extends GenericController {

	private void performValidation(User user, HttpServletRequest request,
			Map<String, String> errors, Map<String, String> values) throws SQLException
	{
		String email = request.getParameter("email");
		String password = request.getParameter("password");
		String receiveNewsletter = request.getParameter("receiveNewsletter");
       
        
		if (email.length() == 0 || email.length() > 255)
		{
			errors.put("email", "Error.email.length");
		} else if (! email.matches("[0-9a-zA-Z_.-]+@[0-9a-zA-Z_.-]+"))
		{
			errors.put("email", "Error.email.format");
		} else
		{
			User tempUser = database.getUserbyEmail(email);
			if (tempUser != null && !tempUser.getId().equals(user.getId()))
			{
			    //same email but different user id
				errors.put("email", "Error.email.alreadyExists");
			}
		}
		if (errors.containsKey("email"))
		{
			values.put("email", "");	
		} else
		{
			values.put("email", email);
		}

		if (password.length() == 0)
		{
			errors.put("password", "Error.password.empty");
		} else if (password.length() > 255)
		{
			errors.put("password", "Error.password.length");			
		} 		
		
		if (!(receiveNewsletter == null || receiveNewsletter.equals("1") || receiveNewsletter.equals("0")))
		{
		    errors.put("receiveNewsletter", "Error.newsLetter.value");  
		}
		if (errors.containsKey("receiveNewsletter"))
        {
            values.put("receiveNewsletter", "0");    
        } else
        {
            values.put("receiveNewsletter", receiveNewsletter);
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

			String password = request.getParameter("password");
			String newEmail = request.getParameter("email");
			String receiveNewsletterStr = request.getParameter("receiveNewsletter");
			
			Map<String, String> errors = new HashMap<String, String>();
			Map<String, String> values = new HashMap<String, String>();
			
			performValidation(user, request, errors, values);
			
			model.getCmsApplicationModel().put("errors", errors);
			model.getCmsApplicationModel().put("values", values);			
	
			if (errors.size() >0)
			{
				String regPageKey = model.getCmsModel().get(WPBModel.URI_PARAMETERS_KEY).get("pageGuid");
				forward.setForwardTo(regPageKey);
				return;
			}
								
			String pswHash = "";
			try
			{
				pswHash = HashService.getHashSha1(password.getBytes());
			}
			catch (NoSuchAlgorithmException e)
			{
			    throw new WPBException("Cannot calculate hash", e);		
			}
			
			if (! user.getPassword().equals(pswHash))
			{
				errors.put("password", "Error.password.invalid"); //errors are already in the model
				String regPageKey = model.getCmsModel().get(WPBModel.URI_PARAMETERS_KEY).get("pageGuid");
				forward.setForwardTo(regPageKey);
				return;
			}
			Integer receiveNewsLetter = 0;
			if (receiveNewsletterStr != null && receiveNewsletterStr.equals("1"))
			{
			    receiveNewsLetter = 1;
			}
			if (newEmail.compareTo(user.getEmail()) != 0)
			{
			    user.setConfirmEmailFlag(0);
			    user.setConfirmEmailRandom(UUID.randomUUID().toString());
			}
			user.setEmail(newEmail);
			
			user.setReceiveNewsletter(receiveNewsLetter);
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