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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.webpagebytes.cms.WPBForward;
import com.webpagebytes.cms.WPBModel;
import com.webpagebytes.cms.exception.WPBException;

public class ChangeLanguageController extends BaseController {

	public void handleRequest(HttpServletRequest servletRequest,
			HttpServletResponse servletResponse, WPBModel model, WPBForward forward) throws WPBException {
		
	    
	    String newLanguage = "";
		try
		{
			String referer = servletRequest.getHeader("Referer");
			String baseUrl = model.getCmsModel().get(WPBModel.REQUEST_KEY).get(WPBModel.GLOBAL_BASE_URL);
			newLanguage = model.getCmsModel().get(WPBModel.LOCALE_KEY).get(WPBModel.LOCALE_LANGUAGE_KEY);
			
			if (referer != null && baseUrl != null && referer.startsWith(baseUrl))
			{
				String uri = referer.substring(baseUrl.length());
				if (!uri.startsWith("/"))
				{
					uri = "/" + uri;
				}
				if (uri.indexOf('/') != uri.lastIndexOf('/'))
				{
					uri = uri.substring(uri.lastIndexOf('/'));
				}
				String newUrl = "." + uri;
				servletResponse.sendRedirect(newUrl);
			} else
			{	
				servletResponse.sendRedirect(baseUrl + "/" + newLanguage + "/");
			}
		} catch (IOException e)
		{
			throw new WPBException("Cannot redirect to:" + newLanguage, e);
		}
	}

}
