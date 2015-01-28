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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webpagebytes.cms.WPBForward;
import com.webpagebytes.cms.WPBModel;
import com.webpagebytes.cms.exception.WPBException;
import com.webpagebytes.wpbsample.data.Session;

public class LoginGetController extends GenericController {

    public void handleRequest(HttpServletRequest request,
            HttpServletResponse response, WPBModel model, WPBForward forward) throws WPBException {
        Session session = getSession(request, response);
        if (false == handleAuthenticationWithoutRedirect(request, response, model, forward, session))
        {
            // user is not logged in so we will display the login page
            String pageGuid = model.getCmsModel().get(WPBModel.URI_PARAMETERS_KEY).get("pageGuid");
            forward.setForwardTo(pageGuid);           
            return;
        }
        // user is logged in so we redirect to /home
        String homeUri = model.getCmsModel().get(WPBModel.GLOBALS_KEY).get("HOME_URI");
        response.addHeader("Location", homeUri);
        response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
    }

}
