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


import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webpagebytes.cms.WPBContentProvider;
import com.webpagebytes.cms.WPBForward;
import com.webpagebytes.cms.WPBModel;
import com.webpagebytes.cms.WPBRequestHandler;
import com.webpagebytes.cms.exception.WPBException;
import com.webpagebytes.wpbsample.data.*;
import com.webpagebytes.wpbsample.database.*;

public class BaseController implements WPBRequestHandler {

	protected static final String SESSION_LOGIN_USERID = "loginUserId";
	protected static final String SESSION_ACCOUNT_BALANCE = "accountBalance";
	protected static final String SESSION_LOGIN_USERNAME = "loginUserName";
	protected static final String GLOBALS_LOGIN_PAGE_URI_KEY = "LOGIN_URI";
	protected static final String SESSION_COOKIE = "wpbsession";
	protected static final int MAX_RECORDS = 10000;

	protected WPBDatabase database = WPBDatabaseService.getInstance();
	protected WPBContentProvider contentProvider;
	
	protected Session getSession(HttpServletRequest request, HttpServletResponse response) throws WPBException 
	{
		String sessionId = null;
		Cookie[] cookies = request.getCookies();
		if (cookies != null)
		{
			for (int i = 0; i< cookies.length; i++)
			{
				Cookie cookie = cookies[i];
				if (cookie.getName().equals(SESSION_COOKIE))
				{
					sessionId = cookie.getValue();
				}
			}
		};
		Session session = null;
		try
		{
			if (sessionId == null)
			{
				sessionId = UUID.randomUUID().toString();
				session = database.createSession(sessionId);
				database.setSession(session); //store it in the DB
				Cookie cookie = new Cookie(SESSION_COOKIE, sessionId);
				cookie.setHttpOnly(true);
				response.addCookie(cookie);
			} else
			{
				session = database.getSession(sessionId);
				if (session == null)
				{
					session = database.createSession(sessionId);
					database.setSession(session); //store it in the DB	
				}
			}
		} catch (SQLException e)
		{
			throw new WPBException("SQL Exception while get session", e);
		}
		return session;
	}

	public void handleRequest(HttpServletRequest arg0,
			HttpServletResponse arg1, WPBModel arg2, WPBForward arg3) throws WPBException {
		// TODO Auto-generated method stub
		
	}

	public void initialize(WPBContentProvider contentProvider) {
		// TODO Auto-generated method stub
		this.contentProvider = contentProvider;
	}

}
