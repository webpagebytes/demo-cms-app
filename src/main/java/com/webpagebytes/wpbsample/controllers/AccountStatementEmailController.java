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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.webpagebytes.cms.WPBContentProvider;
import com.webpagebytes.cms.WPBForward;
import com.webpagebytes.cms.WPBModel;
import com.webpagebytes.cms.exception.WPBException;
import com.webpagebytes.wpbsample.data.Session;
import com.webpagebytes.wpbsample.data.User;
import com.webpagebytes.wpbsample.utility.EmailUtility;
import com.webpagebytes.wpbsample.utility.EmailUtilityFactory;

public class AccountStatementEmailController  extends GenericController {


    public void initialize(WPBContentProvider contentProvider) {
        // TODO Auto-generated method stub
        super.initialize(contentProvider);
    }

    public void handleRequest(HttpServletRequest request,
            HttpServletResponse response, WPBModel model, WPBForward forward) throws WPBException {
        Session session = getSession(request, response);
        if (false == handleAuthentication(request, response, model, forward, session))
        {
            return;
        }
        Integer user_id = (Integer) session.getSessionMap().get(SESSION_LOGIN_USERID); 
        
        ByteArrayOutputStream baos_body = new ByteArrayOutputStream(2000);
        ByteArrayOutputStream baos_subject = new ByteArrayOutputStream(1000);
        
        model.getCmsApplicationModel().put("date_now", new Date());
        // result is the operation result, 0 = success, 1 the email was not sent, 2 (but TBD) email address is not verified
        model.getCmsApplicationModel().put("result", "1");

        try
        {
            User user = database.getUser(user_id);
            
            if (user.getConfirmEmailFlag() != 1)
            {
                // we should never get here, do not send email if the account is not confirmed  
                throw new WPBException("email not confirmed");
            }
            model.getCmsApplicationModel().put("user", user);
            model.getCmsApplicationModel().put("user_id", user_id);     
            String bodyPageGuid = model.getCmsModel().get(WPBModel.URI_PARAMETERS_KEY).get("bodyPageGuid");;
            contentProvider.writePageContent(bodyPageGuid, model, baos_body);
            
            String subjectPageGuid = model.getCmsModel().get(WPBModel.URI_PARAMETERS_KEY).get("subjectPageGuid");;
            contentProvider.writePageContent(subjectPageGuid, model, baos_subject);
            
            EmailUtility emailUtility = EmailUtilityFactory.getInstance();
            emailUtility.sendEmail(user.getEmail(), "no-reply@webpagebytes.com", baos_subject.toString("UTF-8"), baos_body.toString("UTF-8"), null, null);

            model.getCmsApplicationModel().put("result", "0");
            
            String resultPageGuid = model.getCmsModel().get(WPBModel.URI_PARAMETERS_KEY).get("resultPageGuid");;
            forward.setForwardTo(resultPageGuid);
          } 
        catch (Exception e)
        {
            throw new WPBException("Cannot generate email content", e);
        }
        finally
        {
            IOUtils.closeQuietly(baos_subject);
            IOUtils.closeQuietly(baos_body);
        }
        
    }
}