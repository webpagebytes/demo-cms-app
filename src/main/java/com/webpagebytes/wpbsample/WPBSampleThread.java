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

package com.webpagebytes.wpbsample;

import java.io.ByteArrayInputStream;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.fop.apps.MimeConstants;

import com.webpagebytes.cms.WPBContentProvider;
import com.webpagebytes.cms.WPBContentService;
import com.webpagebytes.cms.WPBModel;
import com.webpagebytes.wpbsample.utility.SampleFopService;
import com.webpagebytes.wpbsample.utility.SampleConfigurator;

public class WPBSampleThread implements Runnable {

	private static final Logger log = Logger.getLogger(WPBSampleThread.class.getName());
	private WPBContentService contentService;
	public WPBSampleThread(WPBContentService contentService)
	{
		this.contentService = contentService;
	}
	@Override
	public void run() {
		try
		{			
			log.log(Level.INFO, "Execution of backgroud thread started");
			
			WPBContentProvider contentProvider = contentService.getContentProvider();
			
			WPBModel configModel = contentService.createModel();
			
			String  maxRunsStr = configModel.getCmsModel().get(WPBModel.GLOBALS_KEY).get("TEST_RUNS_PDF_GENERATION");;
			String user_name = configModel.getCmsModel().get(WPBModel.GLOBALS_KEY).get("TEST_USERNAME_PDF_GENERATION");
			if (null == maxRunsStr || user_name == null)
			{
				return;
			}
			
			long t0 = System.currentTimeMillis();
			String basePath = SampleConfigurator.getInstance().getConfig("storageDir");
			int i = 0;
			int max_runs = 0;
			try
			{
				max_runs = Integer.valueOf(maxRunsStr);
			} catch (NumberFormatException e) {};
			
			log.log(Level.INFO, "Execution of backgroud thread to generate PDF's for user " + user_name);
			
			while (i< max_runs)
			{
				WPBModel model = contentService.createModel();
				String pageGuid = model.getCmsModel().get(WPBModel.GLOBALS_KEY).get("STATEMENT_FOP_PAGE_GUID");
				if (pageGuid != null)
				{
					model.getCmsApplicationModel().put("user_name", user_name);		
					ByteArrayOutputStream bos = new ByteArrayOutputStream(4096);
					contentProvider.writePageContent(pageGuid, model, bos);	
					ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
					
					String filePath = basePath + File.separator + java.util.UUID.randomUUID().toString() + ".pdf";
					FileOutputStream fos = new FileOutputStream(filePath);
					SampleFopService fopService = SampleFopService.getInstance();
					fopService.getContent(bis, MimeConstants.MIME_PDF, fos);
					log.log(Level.INFO, "Generated file:" + filePath);
					fos.close();
					bos.close();
					bis.close();
				}
				i = i + 1;
			}
			long t1 = System.currentTimeMillis();
			log.log(Level.INFO, "Execution of backgroud thread took (milliseconds)" + (t1-t0));
			
		} catch (Exception e)
		{
			log.log(Level.SEVERE, "Exception running the sample backgroud thread", e);
		}
		
	}

}
