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

import java.io.ByteArrayOutputStream;



import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webpagebytes.cms.WPBForward;
import com.webpagebytes.cms.WPBModel;
import com.webpagebytes.cms.exception.WPBException;
import com.webpagebytes.wpbsample.data.Session;
import com.webpagebytes.wpbsample.data.User;
import com.webpagebytes.wpbsample.utility.EmailUtility;
import com.webpagebytes.wpbsample.utility.EmailUtilityFactory;
import com.webpagebytes.wpbsample.utility.HashService;

public class RegisterController extends BaseController {
    private static final Logger log = Logger.getLogger(RegisterController.class.getName());
    
	private void performValidation(HttpServletRequest request,
			Map<String, String> errors, Map<String, String> values)
	{
		String username = request.getParameter("username");
		String email = request.getParameter("email");
		String password = request.getParameter("password");
		String password2 = request.getParameter("password2");
		String receiveNewsletter = request.getParameter("receiveNewsletter");
		
		if (username.length() == 0 || username.length() > 255)
		{
			errors.put("username", "Error.username.length");
		} else if (!username.matches("[0-9a-zA-Z_@.-]*"))
		{
			errors.put("username", "Error.username.format");
		} 

		try
		{
			User user = database.getUser(username);
			if (user != null)
			{
				errors.put("username", "Error.username.alreadyExists");
			}
		} catch (SQLException e)
		{
			errors.put("username", "Error.username.alreadyExists");			
		}

		if (errors.containsKey("username"))
		{
			values.put("username", "");	
		} else
		{
			values.put("username", username);
		}
		
		if (email.length() == 0 || email.length() > 255)
		{
			errors.put("email", "Error.email.length");
		} else if (! email.matches("[0-9a-zA-Z_.-]+@[0-9a-zA-Z_.-]+"))
		{
			errors.put("email", "Error.email.format");
		}
		
		try
		{
			User user = database.getUserbyEmail(email);
			if (user != null)
			{
				errors.put("email", "Error.email.alreadyExists");
			}
		} catch (SQLException e)
		{
			errors.put("email", "Error.email.alreadyExists");			
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
		session.getSessionMap().clear();
		session.setUser_id(null);
		try
		{
			database.setSession(session);
		} catch (SQLException e)
		{
		    throw new WPBException("Cannot set session", e);	
		}
		
		String username = request.getParameter("username");
		String email = request.getParameter("email");
		String password = request.getParameter("password");
		String receiveNewsletterStr = request.getParameter("receiveNewsletter");
		
		Map<String, String> errors = new HashMap<String, String>();
		Map<String, String> values = new HashMap<String, String>();
		
		performValidation(request, errors, values);
		
		model.getCmsApplicationModel().put("errors", errors);

		if (errors.size() >0)
		{
			model.getCmsApplicationModel().put("values", values);			
			String regPageKey = model.getCmsModel().get(WPBModel.GLOBALS_KEY).get("REG_PAGE_KEY");
			forward.setForwardTo(regPageKey);
			return;
		}
		
		User user = new User();
		user.setUserName(username);
		user.setEmail(email.toLowerCase());
		String pswHash = "";
		try
		{
			pswHash = HashService.getHashSha1(password.getBytes());
			user.setPassword(pswHash);
		}
		catch (NoSuchAlgorithmException e)
		{
		    throw new WPBException("Cannot calculate hash", e);
		}
		user.setOpen_date(new Date());
		Integer receiveNewletter = 0;
        if (receiveNewsletterStr != null && receiveNewsletterStr.equals("1"))
        {
            receiveNewletter = 1;
        }
        user.setReceiveNewsletter(receiveNewletter);
        user.setConfirmEmailFlag(0);
        user.setConfirmEmailRandom(UUID.randomUUID().toString());
        user.setConfirmEmailDate(new Date());
		try
		{
		    log.log(Level.INFO, "Create user:" + user.getUserName() + " , confirmation: " + user.getConfirmEmailRandom());
			user = database.createUser(user);
			session.setUser_id(user.getId());
			session.getSessionMap().put(SESSION_LOGIN_USERID, user.getId());
			session.getSessionMap().put(SESSION_LOGIN_USERNAME, user.getUserName());	
			model.getCmsApplicationModel().put(SESSION_LOGIN_USERID, user.getId());
			model.getCmsApplicationModel().put(SESSION_LOGIN_USERNAME, user.getUserName());
			model.getCmsApplicationModel().put("user", user);
			
			String emailBody = model.getCmsModel().get(WPBModel.URI_PARAMETERS_KEY).get("bodyPageGuid");
			String emailSubject = model.getCmsModel().get(WPBModel.URI_PARAMETERS_KEY).get("subjectPageGuid");
            
			ByteArrayOutputStream bosBody = new ByteArrayOutputStream();
			ByteArrayOutputStream bosSubject = new ByteArrayOutputStream();
            
			contentProvider.writePageContent(emailBody, model, bosBody);
			contentProvider.writePageContent(emailSubject, model, bosSubject);
            
			EmailUtility emailUtility = EmailUtilityFactory.getInstance();
			emailUtility.sendEmail(user.getEmail(), "no-reply@webpagebytes.com", bosSubject.toString("UTF-8"), bosBody.toString("UTF-8"), null, null);
			
			database.setSession(session);		
		} catch (Exception e)
		{
		    throw new WPBException("Cannot update data", e);
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
