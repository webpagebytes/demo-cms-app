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

package com.webpagebytes.wpbsample.utility;

import java.io.InputStream;
import java.io.OutputStream;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;

public class SampleFopService {

	private static SampleFopService instance;
	private FopFactory fopFactory;
	private TransformerFactory transformerFactory;
	private static volatile Object lock = new Object();
	
	private SampleFopService()
	{
		transformerFactory = TransformerFactory.newInstance();
		fopFactory = FopFactory.newInstance();
	}

	public static SampleFopService getInstance()
	{
		if (instance == null)
		{
			synchronized (lock)
			{
				if (null == instance)
				{
					instance = new SampleFopService();
				}
			}
		}
		return instance;
	}
	
	public void getContent(InputStream  is, String mimeType, OutputStream os) throws Exception
	{
		Source source = null;
		try
		{	
			Transformer transformer = transformerFactory.newTransformer(); // identity transformer
			source = new StreamSource(is);		
			Fop fop = fopFactory.newFop(mimeType, os);
			Result res = new SAXResult(fop.getDefaultHandler());
			transformer.transform(source, res);			
		} catch (Exception e)
		{
			throw e;
		}
	}

}
