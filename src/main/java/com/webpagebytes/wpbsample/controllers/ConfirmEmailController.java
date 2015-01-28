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
import com.webpagebytes.wpbsample.data.User;

public class ConfirmEmailController extends BaseController {

    
   
    public void handleRequest(HttpServletRequest request,
            HttpServletResponse response, WPBModel model, WPBForward forward) throws WPBException {
        
        String code = request.getParameter("code");
               
        try
        {
        if (code != null && code.length()>0)
        {
            model.getCmsApplicationModel().put("code", code);
            User user = null;
            user = database.getUserByConfirmCode(code);
            
            if (null == user)
            {
                // cosider that result=1 means the code does not match any user 
                model.getCmsApplicationModel().put("result", "1");
            } else if (user.getConfirmEmailFlag() == 1)
            {
                // cosider that result=2 means the code was relready used 
                model.getCmsApplicationModel().put("result", "2");                
            } else if (user.getConfirmEmailFlag() == 0)
            {
                user.setConfirmEmailFlag(1);
                database.updateUser(user);
                
                // cosider that result=0 means the happy case 
                model.getCmsApplicationModel().put("user", user);
                model.getCmsApplicationModel().put("result", "0");           
            }
            
        } else
        {
            // cosider that result=3 means there is no code 
            model.getCmsApplicationModel().put("result", "3");                
        }
        } catch (Exception e)
        {
            throw new WPBException("Cannot get user", e);            
        }
        
        String pageGuid = model.getCmsModel().get(WPBModel.URI_PARAMETERS_KEY).get("pageGuid");
        forward.setForwardTo(pageGuid);
       
    }
}