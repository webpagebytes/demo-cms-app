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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webpagebytes.cms.WPBModel;
import com.webpagebytes.cms.WPBPageModelProvider;
import com.webpagebytes.cms.exception.WPBException;
import com.webpagebytes.wpbsample.database.WPBDatabase;
import com.webpagebytes.wpbsample.database.WPBDatabaseService;
import com.webpagebytes.wpbsample.utility.DateUtility;

public class NotificationLastDayPageController  implements WPBPageModelProvider {

	private Map<String, Integer> convertMapToStringsKeys(Map<Date, Integer> map)
	{
		Map<String, Integer> result = new HashMap<String, Integer>();
		for(Date key: map.keySet())
		{
			result.put(String.valueOf(key.getTime()), map.get(key));
		}
		return result;
	}
	@Override
	public void populatePageModel(WPBModel model) throws WPBException {
		
		try
		{
			
			int interval = 7;
			List<Date> dates = new ArrayList<Date>();
			List<String> keys = new ArrayList<String>();
			Date past = DateUtility.addDays(DateUtility.getToday(), -interval);
			
			for(int i = 0; i<= interval-1 ; i++)
			{			
				Date aDate = DateUtility.addDays(past, i);
				dates.add(aDate);
				String iStr = String.valueOf(aDate.getTime());
				keys.add(iStr);
			}
			
			WPBDatabase database = WPBDatabaseService.getInstance();
			Map<Date, Integer> usersCountPerDays = database.getUsersCountFromLastDays(interval);
			
			//freemarker likes for maps only keys with strings, so we have to convert the actuals to a string based keys
			Map<Date, Integer> countTransactionsPerDay = database.getCountOperationsFromLastDays(WPBDatabase.ACCOUNT_OPERATION_PAYMENT, interval);
			Map<Date, Integer> sumTransactionsPerDay = database.getSumOperationsFromLastDays(WPBDatabase.ACCOUNT_OPERATION_PAYMENT, interval);

			Map<Date, Integer> countDepositsPerDay = database.getCountOperationsFromLastDays(WPBDatabase.ACCOUNT_OPERATION_DEPOSIT, interval);
			Map<Date, Integer> sumDepositsPerDay = database.getSumOperationsFromLastDays(WPBDatabase.ACCOUNT_OPERATION_DEPOSIT, interval);

			Map<Date, Integer> countWithdrawalsPerDay = database.getCountOperationsFromLastDays(WPBDatabase.ACCOUNT_OPERATION_WITHDRAWAL, interval);
			Map<Date, Integer> sumWithdrawalsPerDay = database.getSumOperationsFromLastDays(WPBDatabase.ACCOUNT_OPERATION_WITHDRAWAL, interval);

			model.getCmsApplicationModel().put("interval", interval);
			model.getCmsApplicationModel().put("dates", dates);
			model.getCmsApplicationModel().put("keys", keys);
			
			model.getCmsApplicationModel().put("usersCountPerDays", convertMapToStringsKeys(usersCountPerDays));
			model.getCmsApplicationModel().put("countTransactionsPerDay", convertMapToStringsKeys(countTransactionsPerDay));
			model.getCmsApplicationModel().put("sumTransactionsPerDay", convertMapToStringsKeys(sumTransactionsPerDay));
			model.getCmsApplicationModel().put("countDepositsPerDay", convertMapToStringsKeys(countDepositsPerDay));
			model.getCmsApplicationModel().put("sumDepositsPerDay", convertMapToStringsKeys(sumDepositsPerDay));
			model.getCmsApplicationModel().put("countWithdrawalsPerDay", convertMapToStringsKeys(countWithdrawalsPerDay));
			model.getCmsApplicationModel().put("sumWithdrawalsPerDay", convertMapToStringsKeys(sumWithdrawalsPerDay));			
			
			model.getCmsApplicationModel().put("yesterday", DateUtility.addDays(DateUtility.getToday(), -1));			
			
		} catch (SQLException e)
		{
		    throw new WPBException("Cannot generate content", e);
		}
		
	}

}
