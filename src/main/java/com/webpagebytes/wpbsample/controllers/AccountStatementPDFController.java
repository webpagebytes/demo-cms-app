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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.fop.apps.MimeConstants;

import com.webpagebytes.cms.WPBContentProvider;
import com.webpagebytes.cms.WPBForward;
import com.webpagebytes.cms.WPBModel;
import com.webpagebytes.cms.exception.WPBException;
import com.webpagebytes.wpbsample.data.Session;
import com.webpagebytes.wpbsample.utility.SampleFopService;

public class AccountStatementPDFController extends GenericController {


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
		
		OutputStream os = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream(10000);
		ByteArrayInputStream bis = null;
		try
		{
			model.getCmsApplicationModel().put("user_id", user_id);		
			response.setContentType("application/pdf");
			response.setHeader("Content-Disposition", "attachment; filename=wpb-demo-statements.pdf");

			SampleFopService fopService = SampleFopService.getInstance();
			String fopPageGuid = model.getCmsModel().get(WPBModel.URI_PARAMETERS_KEY).get("pageGuid");;
			contentProvider.writePageContent(fopPageGuid, model, baos);
			bis = new ByteArrayInputStream(baos.toByteArray());
			baos.close();
			os = response.getOutputStream();
			fopService.getContent(bis, MimeConstants.MIME_PDF, os);			
		} 
		catch (Exception e)
		{
		    throw new WPBException("Cannot generate pdf", e);
		}
		finally
		{
			if (os != null)
			{
				IOUtils.closeQuietly(os);
			}
			if (baos != null)
			{
				IOUtils.closeQuietly(baos);
			}
			if (bis != null)
			{
				IOUtils.closeQuietly(bis);
			}
		}
		
	}

}
