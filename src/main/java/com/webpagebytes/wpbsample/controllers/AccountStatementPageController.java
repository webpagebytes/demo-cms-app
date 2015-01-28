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

import java.util.Date;
import java.util.List;
import com.webpagebytes.cms.WPBModel;
import com.webpagebytes.cms.WPBPageModelProvider;
import com.webpagebytes.cms.exception.WPBException;
import com.webpagebytes.wpbsample.data.Account;
import com.webpagebytes.wpbsample.data.DepositWithdrawal;
import com.webpagebytes.wpbsample.data.Transaction;
import com.webpagebytes.wpbsample.data.User;
import com.webpagebytes.wpbsample.database.WPBDatabase;
import com.webpagebytes.wpbsample.database.WPBDatabaseService;

public class AccountStatementPageController implements WPBPageModelProvider {

	protected WPBDatabase database = WPBDatabaseService.getInstance();
	
	@Override
	public void populatePageModel(WPBModel model) throws WPBException {
		User user = null;
		Integer user_id = (Integer) model.getCmsApplicationModel().get("user_id");
		try
		{
			if (null == user_id)
			{
				String user_name = (String) model.getCmsApplicationModel().get("user_name");
				user = database.getUser(user_name);
			} else
			{
				user = database.getUser(user_id);
			}
			model.getCmsApplicationModel().put("user", user);
			model.getCmsApplicationModel().put("date_now", new Date());
			
			Account account = database.getAccount(user_id);
			model.getCmsApplicationModel().put("account", account);
			
			Date date = new Date(0);
			List<Transaction> transactions = database.getTransactionsForUser(user_id, date, 1, BaseController.MAX_RECORDS);
			model.getCmsApplicationModel().put("transactions", transactions);
			
			List<DepositWithdrawal> deposits = database.getDepositsWithdrawalsForUser(user_id, DepositWithdrawal.OperationType.DEPOSIT, date, 1, BaseController.MAX_RECORDS);
			model.getCmsApplicationModel().put("deposits", deposits);
			
			List<DepositWithdrawal> withdrawals = database.getDepositsWithdrawalsForUser(user_id, DepositWithdrawal.OperationType.WITHDRAWAL, date, 1, BaseController.MAX_RECORDS);
			model.getCmsApplicationModel().put("withdrawals", withdrawals);
		} catch (SQLException e)
		{
		    throw new WPBException("Cannot get data", e);
		}
		
	}


	
}
