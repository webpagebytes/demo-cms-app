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
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webpagebytes.cms.WPBForward;
import com.webpagebytes.cms.WPBModel;
import com.webpagebytes.cms.exception.WPBException;
import com.webpagebytes.wpbsample.data.Session;

public class LogoutController extends BaseController {

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
		    throw new WPBException("Cannot reset session", e);
		}
		
			
		String redirectUri = model.getCmsModel().get(WPBModel.GLOBALS_KEY).get(GLOBALS_LOGIN_PAGE_URI_KEY);
		try
		{
			response.sendRedirect(redirectUri);
		} catch (IOException e)
		{
		    throw new WPBException("Cannot redirect to:" + redirectUri, e);		
		}
	}

}
