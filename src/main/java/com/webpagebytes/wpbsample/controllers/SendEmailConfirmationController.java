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
import java.util.Date;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webpagebytes.cms.WPBForward;
import com.webpagebytes.cms.WPBModel;
import com.webpagebytes.cms.exception.WPBException;
import com.webpagebytes.wpbsample.data.Session;
import com.webpagebytes.wpbsample.data.User;
import com.webpagebytes.wpbsample.utility.EmailUtility;
import com.webpagebytes.wpbsample.utility.EmailUtilityFactory;

public class SendEmailConfirmationController extends GenericController {

    public void handleRequest(HttpServletRequest request,
            HttpServletResponse response, WPBModel model, WPBForward forward) throws WPBException {
        Session session = getSession(request, response);
        if (false == handleAuthentication(request, response, model, forward, session))
        {
            return;
        }
        Integer user_id = (Integer) session.getSessionMap().get(SESSION_LOGIN_USERID); 
        ByteArrayOutputStream baos_body = new ByteArrayOutputStream(1000);
        ByteArrayOutputStream baos_subject = new ByteArrayOutputStream(1000);

        try
        {
            User user = database.getUser(user_id);
            model.getCmsApplicationModel().put("user", user);
            long timeDiff = (new Date()).getTime() - user.getConfirmEmailDate().getTime();
            if (user.getConfirmEmailFlag() == 1)
            {
                // email is already confirmed, inform the user about this
                // result=1 means the email is already confirmed
                model.getCmsApplicationModel().put("result", 1);
            } else if ( timeDiff <30*60*1000 )
            {
                // result=2 it didn't pass 30 minutes till the last email sent with confirmation
                model.getCmsApplicationModel().put("result", 2);
            } else
            {
                // send the confirmation email
                user.setConfirmEmailFlag(0);
                user.setConfirmEmailDate(new Date());
                user.setConfirmEmailRandom(UUID.randomUUID().toString());
                database.updateUser(user);
               
                String bodyPageGuid = model.getCmsModel().get(WPBModel.URI_PARAMETERS_KEY).get("bodyPageGuid");;
                contentProvider.writePageContent(bodyPageGuid, model, baos_body);
                
                String subjectPageGuid = model.getCmsModel().get(WPBModel.URI_PARAMETERS_KEY).get("subjectPageGuid");;
                contentProvider.writePageContent(subjectPageGuid, model, baos_subject);
                
                EmailUtility emailUtility = EmailUtilityFactory.getInstance();
                emailUtility.sendEmail(user.getEmail(), "no-reply@webpagebytes.com", baos_subject.toString("UTF-8"), baos_body.toString("UTF-8"), null, null);

                model.getCmsApplicationModel().put("result", 0);
                
            }
            
        } catch (Exception e)
        {
            throw new WPBException("Cannot get user data", e);
        }
        String pageGuid = model.getCmsModel().get(WPBModel.URI_PARAMETERS_KEY).get("pageGuid");
        forward.setForwardTo(pageGuid);
    }
}
