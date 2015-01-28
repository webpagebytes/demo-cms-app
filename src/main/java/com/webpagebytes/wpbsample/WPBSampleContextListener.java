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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import static org.quartz.TriggerBuilder.*;
import static org.quartz.CronScheduleBuilder.*;
import static org.quartz.JobBuilder.*;

import com.webpagebytes.cms.WPBContentServiceFactory;
import com.webpagebytes.wpbsample.utility.PathUtility;
import com.webpagebytes.wpbsample.utility.SampleConfigurator;
import com.webpagebytes.wpbsample.utility.SampleFopService;

public class WPBSampleContextListener implements ServletContextListener {

	private static final Logger log = Logger.getLogger(WPBSampleContextListener.class.getName());
	
	@Override
	public void contextDestroyed(ServletContextEvent servletContext) {
		
		
	}

	@Override
	public void contextInitialized(ServletContextEvent servletContext) {
		
    	log.log(Level.INFO, "WBPBSampleContextListener context initialized");
    	String sampleConfigPath = servletContext.getServletContext().getInitParameter("sampleConfigurationPath");
    	sampleConfigPath = PathUtility.safePath(sampleConfigPath);
		if (null == sampleConfigPath)
		{
			throw new RuntimeException("cannot get wpbSampleConfigPath value from context patameters");
		}
		try
		{
			log.log(Level.INFO, "config path for sample app: " + sampleConfigPath);
			SampleConfigurator.initialize(sampleConfigPath);
		}catch (IOException e)
		{
			throw new RuntimeException("cannot initialize WPBSampleConfigurator with file " + sampleConfigPath);
		}
        //speed up the creation of SampleFopService instance;
		SampleFopService.getInstance();
		
		WPBSampleThread thread = new WPBSampleThread(WPBContentServiceFactory.getInstance());
		(new Thread(thread)).run();
		
		// now schedule the job
		try
		{
			SchedulerFactory sf = new StdSchedulerFactory();
			Scheduler schedule = sf.getScheduler();
			
			JobDetail job = newJob(SampleJob.class)
				        .withIdentity("sampleJob", "sampleGroup")
				        .build();
			Trigger trigger = newTrigger()
				    .withIdentity("sampleTrigger", "sampleGroup")
				    .withSchedule(cronSchedule("0 0 0 1/1 * ? *"))
				    .forJob("sampleJob", "sampleGroup")
				    .build();
			schedule.scheduleJob(job, trigger);
			schedule.start();
		} catch (Exception e)
		{
			log.log(Level.SEVERE, "cannot start job scheduler ", e);
		}
		
		
	}

}
