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


import java.util.Map;






import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webpagebytes.cms.WPBForward;
import com.webpagebytes.cms.WPBModel;
import com.webpagebytes.cms.exception.WPBException;
import com.webpagebytes.wpbsample.data.Session;

public class GenericController extends BaseController {
	protected static final int PAGE_SIZE = 11;

	protected boolean handleAuthentication(HttpServletRequest request,
			HttpServletResponse response, WPBModel model, WPBForward forward, Session session) {
		Map<String, Object> sessionMap = session.getSessionMap();
		Integer userId = (Integer) sessionMap.get(SESSION_LOGIN_USERID); 
		String userName = (String) sessionMap.get(SESSION_LOGIN_USERNAME);
		if (userId == null)
		{
			// user is not logged in, redirect to Login page
			String regPageUri = model.getCmsModel().get(WPBModel.GLOBALS_KEY).get(GLOBALS_LOGIN_PAGE_URI_KEY);
			response.addHeader("Location", regPageUri);
			response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
			return false;
		} else
		{
			model.getCmsApplicationModel().put(SESSION_LOGIN_USERID, userId);	
			model.getCmsApplicationModel().put(SESSION_LOGIN_USERNAME, userName);
		}
		return true;	
	}

    protected boolean handleAuthenticationWithoutRedirect(HttpServletRequest request,
            HttpServletResponse response, WPBModel model, WPBForward forward, Session session) {
        Map<String, Object> sessionMap = session.getSessionMap();
        Integer userId = (Integer) sessionMap.get(SESSION_LOGIN_USERID); 
        String userName = (String) sessionMap.get(SESSION_LOGIN_USERNAME);
        if (userId == null)
        {
            return false;
        } else
        {
            model.getCmsApplicationModel().put(SESSION_LOGIN_USERID, userId);   
            model.getCmsApplicationModel().put(SESSION_LOGIN_USERNAME, userName);
        }
        return true;    
    }

	public void handleRequest(HttpServletRequest request,
			HttpServletResponse response, WPBModel model, WPBForward forward) throws WPBException {
		Session session = getSession(request, response);
		if (false == handleAuthentication(request, response, model, forward, session))
		{
			return;
		}
		String pageGuid = model.getCmsModel().get(WPBModel.URI_PARAMETERS_KEY).get("pageGuid");
		forward.setForwardTo(pageGuid);
	}
	
}
