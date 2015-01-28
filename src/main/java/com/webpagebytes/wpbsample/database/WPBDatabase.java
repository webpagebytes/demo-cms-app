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

package com.webpagebytes.wpbsample.database;

import java.io.ByteArrayInputStream;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbcp.BasicDataSource;

import com.webpagebytes.wpbsample.data.Account;
import com.webpagebytes.wpbsample.data.AccountOperation;
import com.webpagebytes.wpbsample.data.DepositWithdrawal;
import com.webpagebytes.wpbsample.data.Session;
import com.webpagebytes.wpbsample.data.Transaction;
import com.webpagebytes.wpbsample.data.User;
import com.webpagebytes.wpbsample.data.DepositWithdrawal.OperationType;

public class WPBDatabase {
	private Map<String, String> dbProps = new HashMap<String, String>();
	private BasicDataSource dataSource = new BasicDataSource();

	public static final int ACCOUNT_OPERATION_DEPOSIT = 1;
	public static final int ACCOUNT_OPERATION_WITHDRAWAL = 2;
	public static final int ACCOUNT_OPERATION_PAYMENT = 3;
	private static final String CREATE_USER_STATEMENT = "insert into USERS (USERNAME, EMAIL, PASSWORD, OPEN_DATE, RECEIVENEWSLETTER, CONFIRMEMAILFLAG, CONFIRMEMAILRANDOM, CONFIRMEMAILDATE) values(?,?,?,?,?,?,?,?)";
	private static final String UPDATE_USER_STATEMENT = "update USERS SET EMAIL=?, PASSWORD=?, RECEIVENEWSLETTER=?, CONFIRMEMAILFLAG=?, CONFIRMEMAILRANDOM=?, CONFIRMEMAILDATE=? WHERE id=?";
	private static final String CREATE_ACCOUNT_STATEMENT = "insert into ACCOUNTS (USER_ID, BALANCE) values(?,?)";
	private static final String GET_USER_BY_USERNAME_STATEMENT = "select * from USERS where USERNAME=?";
	private static final String GET_USER_BY_EMAIL_STATEMENT = "select * from USERS where EMAIL=?";
	private static final String GET_USER_BY_ID_STATEMENT = "select * from USERS where ID=?";
	private static final String GET_USER_BY_CONFIRMATION_CODE = "select * from USERS where CONFIRMEMAILRANDOM=?";    
	private static final String GET_ACCOUNT_BY_ID_STATEMENT = "select * from ACCOUNTS where USER_ID=?";
	private static final String UPDATE_ACCOUNT_BY_ID_STATEMENT = "update ACCOUNTS SET BALANCE=? where USER_ID=?";
	private static final String UPDATE_ACCOUNT_FOR_ADDITION_STATEMENT = "update ACCOUNTS SET BALANCE=BALANCE+? where USER_ID=?";
	private static final String CREATE_SESSION_STATEMENT = "insert into SESSIONS (ID, CREATE_TIMESTAMP, DATA) values(?,?,?)";
	private static final String GET_SESSION_BY_ID_STATEMENT = "select * from SESSIONS where ID=?";
	private static final String UPDATE_SESSION_STATEMENT = "update SESSIONS SET DATA=? WHERE id=?";
	private static final String GET_ALL_TRANSACTIONS_FOR_USER_STATEMENT = "select t.ID, t.DATE, t.SOURCE_USER_ID, t.DESTINATION_USER_ID, t.AMOUNT, u1.USERNAME AS SOURCE_USERNAME, u2.USERNAME AS DESTINATION_USERNAME from "
																		  +" (SELECT * FROM ACCOUNTOPERATIONS AS tx WHERE ((tx.type=3) AND (tx.DATE >= ?) AND (tx.SOURCE_USER_ID=? OR tx.DESTINATION_USER_ID=?)) ORDER BY tx.DATE DESC LIMIT ?,?) as t INNER JOIN "
																		  +" USERS AS u1 ON (u1.ID = t.SOURCE_USER_ID) "
																		  +" INNER JOIN USERS AS u2 ON (u2.ID=t.DESTINATION_USER_ID) ";
	private static final String GET_ALL_ACCOUNTOPERATIONS_FOR_USER_STATEMENT = "select t.ID, t.USER_ID, t.TYPE, t.AMOUNT, t.DATE, t.SOURCE_USER_ID, t.DESTINATION_USER_ID, u1.USERNAME AS SOURCE_USERNAME, u2.USERNAME AS DESTINATION_USERNAME from "
			  +" (SELECT * FROM ACCOUNTOPERATIONS AS tx WHERE (tx.DATE >=?) AND (tx.SOURCE_USER_ID=? OR tx.DESTINATION_USER_ID=? OR tx.USER_ID=?) ORDER BY tx.DATE DESC LIMIT 0,?) as t LEFT OUTER JOIN "
			  +" USERS AS u1 ON (u1.ID = t.SOURCE_USER_ID) "
			  +" LEFT OUTER JOIN USERS AS u2 ON (u2.ID=t.DESTINATION_USER_ID) ";

	private static final String GET_ALL_DEPOSITWITHDRAWAL_FOR_USER_STATEMENT = "select * from ACCOUNTOPERATIONS AS d WHERE (d.DATE > ?) AND (d.USER_ID=? AND d.TYPE=?) ORDER BY d.DATE DESC LIMIT ?,?";
	private static final String CREATE_ACCOUNTOPERATIONS_STATEMENT = "insert into ACCOUNTOPERATIONS (USER_ID, TYPE, AMOUNT, DATE, SOURCE_USER_ID, DESTINATION_USER_ID) values(?,?,?,?,?,?)";
	private static final String GET_ACCOUNTOPERATIONS_STATEMENT = "select * from ACCOUNTOPERATIONS where ID=?";
	private static final String GET_USERS_COUNT_BY_DAYS = "select count(convert(OPEN_DATE, DATE)) AS COUNT, convert(OPEN_DATE, DATE) as d from USERS group by convert(OPEN_DATE, DATE) order by d DESC limit ?";
	private static final String GET_ACC_OPERATIONS_STATS_BY_DAYS = "SELECT convert(DATE, DATE) as D,  COUNT(*) as count, SUM(AMOUNT) as total FROM ACCOUNTOPERATIONS where type=? group by D order by D desc limit ?";
	
	public WPBDatabase(Map<String, String> dbProps)
	{
		this.dbProps.putAll(dbProps);
	    dataSource.setDriverClassName(dbProps.get(WPBDatabaseService.DB_PROPS_DRIVER_CLASS));
	    dataSource.setUrl(dbProps.get(WPBDatabaseService.DB_PROPS_CONNECTION_URL));
	    dataSource.setUsername(dbProps.get(WPBDatabaseService.DB_PROPS_USER_NAME));
	    dataSource.setPassword(dbProps.get(WPBDatabaseService.DB_PROPS_PASSWORD));
	}
	
	private Connection getConnection() throws SQLException
	{
	    return dataSource.getConnection();
	}
	
	private User getUserFromResultSet(ResultSet rs) throws SQLException
	{
		User user = new User();
		user.setId(rs.getInt(1));
		user.setUserName(rs.getString(2));
		user.setEmail(rs.getString(3));
		user.setPassword(rs.getString(4));
		user.setOpen_date(rs.getDate(5));
		user.setReceiveNewsletter(rs.getInt(6));
		user.setConfirmEmailFlag(rs.getInt(7));
		user.setConfirmEmailRandom(rs.getString(8));
		user.setConfirmEmailDate(rs.getTimestamp(9));
		return user;		
	}

	@SuppressWarnings("unchecked")
	private Session getSessionFromResultSet(ResultSet rs) throws SQLException
	{
		Session session = new Session();
		session.setId(rs.getString(1));
		session.setUser_id(rs.getInt(2));
		session.setCreate_timestamp(rs.getDate(3));
		InputStream is = rs.getBinaryStream(4);
		HashMap<String, Object> sessionMap = null;
		try
		{
			ObjectInputStream ois = new ObjectInputStream(is);
			sessionMap = (HashMap<String, Object>) ois.readObject();
		} catch (Exception e)
		{
			sessionMap = new HashMap<String,Object>();
		}
		session.setSessionMap(sessionMap);
		return session;		
	}

	public User getUser(int id) throws SQLException
	{
		Connection connection = getConnection();
		PreparedStatement statement = null;	
		try
		{
			statement = connection.prepareStatement(GET_USER_BY_ID_STATEMENT);
			statement.setInt(1, id);
			ResultSet rs = statement.executeQuery();
			if (rs.next())
			{
				User user = getUserFromResultSet(rs);
				return user;
			} else
			{
				return null;
			}
		}
		catch (SQLException e)
		{
			throw e;
		}
		finally
		{
			if (statement != null)
			{
				statement.close();
			}
			if (connection != null)
			{
				connection.close();
			}
		}
	}

	public User getUserByConfirmCode(String code) throws SQLException
    {
        Connection connection = getConnection();
        PreparedStatement statement = null; 
        try
        {
            statement = connection.prepareStatement(GET_USER_BY_CONFIRMATION_CODE);
            statement.setString(1, code);
            ResultSet rs = statement.executeQuery();
            if (rs.next())
            {
                User user = getUserFromResultSet(rs);
                return user;
            } else
            {
                return null;
            }
        }
        catch (SQLException e)
        {
            throw e;
        }
        finally
        {
            if (statement != null)
            {
                statement.close();
            }
            if (connection != null)
            {
                connection.close();
            }
        }
    }

	public User getUser(String userName) throws SQLException
	{
		Connection connection = getConnection();
		PreparedStatement statement = null;	
		try
		{
			statement = connection.prepareStatement(GET_USER_BY_USERNAME_STATEMENT);
			statement.setString(1, userName);
			ResultSet rs = statement.executeQuery();
			if (rs.next())
			{
				User user = getUserFromResultSet(rs);
				return user;
			} else
			{
				return null;
			}
		}
		catch (SQLException e)
		{
			throw e;
		}
		finally
		{
			if (statement != null)
			{
				statement.close();
			}
			if (connection != null)
			{
				connection.close();
			}
		}
	}
	
	public User getUserbyEmail(String email) throws SQLException
	{
		Connection connection = getConnection();
		PreparedStatement statement = null;	
		try
		{
			statement = connection.prepareStatement(GET_USER_BY_EMAIL_STATEMENT);
			statement.setString(1, email);
			ResultSet rs = statement.executeQuery();
			if (rs.next())
			{
				User user = getUserFromResultSet(rs);
				return user;
			} else
			{
				return null;
			}
		}
		catch (SQLException e)
		{
			throw e;
		}
		finally
		{
			if (statement != null)
			{
				statement.close();
			}
			if (connection != null)
			{
				connection.close();
			}
		}
	}

	public User createUser(User user) throws SQLException
	{
		Connection connection = getConnection();
		PreparedStatement statementUser = null;	
		PreparedStatement statementAccount = null;
		try
		{
			statementUser = connection.prepareStatement(CREATE_USER_STATEMENT);
			connection.setAutoCommit(false);
	
			statementUser.setString(1, user.getUserName());
			statementUser.setString(2, user.getEmail());
			statementUser.setString(3, user.getPassword());
			java.sql.Timestamp sqlDate = new java.sql.Timestamp(user.getOpen_date().getTime());
			statementUser.setTimestamp(4, sqlDate);
			statementUser.setInt(5, user.getReceiveNewsletter());
			statementUser.setInt(6, user.getConfirmEmailFlag());
			statementUser.setString(7, user.getConfirmEmailRandom());
			java.sql.Timestamp sqlDate1 = new java.sql.Timestamp(user.getConfirmEmailDate().getTime());
			statementUser.setTimestamp(8, sqlDate1);
			statementUser.execute();
			
			ResultSet rs = statementUser.getGeneratedKeys();
			if (rs.next())
			{
				int id = rs.getInt(1);
				user.setId(id);
			} 
			statementAccount = connection.prepareStatement(CREATE_ACCOUNT_STATEMENT);
			statementAccount.setInt(1, user.getId());
			statementAccount.setLong(2, 0L);
			statementAccount.execute();
			connection.commit();
		}
		catch (SQLException e)
		{
			if (connection != null)
			{
				connection.rollback();
			}
			throw e;
		}
		finally
		{
			if (statementUser != null)
			{
				statementUser.close();
			}
			if (statementAccount != null)
			{
				statementAccount.close();
			}
			if (connection != null)
			{
				connection.close();
			}
		}
		return user;
	}

	public DepositWithdrawal createDepositOrWithdrawal(int user_id, DepositWithdrawal.OperationType type, long amount) throws SQLException
	{
		Connection connection = getConnection();
		PreparedStatement statement = null;
		PreparedStatement statementAccount = null;
		
		DepositWithdrawal operation = new DepositWithdrawal();
		try
		{
			statement = connection.prepareStatement(CREATE_ACCOUNTOPERATIONS_STATEMENT);
			connection.setAutoCommit(false);
	
			statement.setInt(1, user_id);
			int typeOperation = ACCOUNT_OPERATION_DEPOSIT;
			if (type == OperationType.WITHDRAWAL)
			{
				typeOperation = ACCOUNT_OPERATION_WITHDRAWAL;
			}
			statement.setInt(2, typeOperation);
			
			statement.setLong(3, amount);
			Date now = new Date();
			java.sql.Timestamp sqlDate = new java.sql.Timestamp(now.getTime());
			statement.setTimestamp(4, sqlDate);
			statement.setNull(5, Types.INTEGER);
			statement.setNull(6, Types.INTEGER);
			
			statement.execute();			
			ResultSet rs = statement.getGeneratedKeys();
			if (rs.next())
			{
				operation.setId(rs.getInt(1));
				operation.setAmount(amount);
				operation.setDate(now);
				operation.setType(type);
			}
			Account account = getAccount(user_id);
			long balanceToSet = 0L;
			if (type == OperationType.DEPOSIT)
			{
				balanceToSet = account.getBalance() + amount;
			} else
			{
				balanceToSet = account.getBalance() - amount;
			}
			if (balanceToSet < 0)
			{
				throw new SQLException("Balance cannot become negative");
			}
			statementAccount = connection.prepareStatement(UPDATE_ACCOUNT_BY_ID_STATEMENT);
			statementAccount.setLong(1, balanceToSet);
			statementAccount.setInt(2, user_id);
			statementAccount.execute();
			
			connection.commit();
		}
		catch (SQLException e)
		{
			if (connection != null)
			{
				connection.rollback();
			}
			throw e;
		}
		finally
		{
			if (statement != null)
			{
				statement.close();
			}
			if (connection != null)
			{
				connection.close();
			}
		}
		return operation;
	}

	public Transaction createTransaction(int source_user_id, int destination_user_id, long amount) throws SQLException
	{
		Connection connection = getConnection();
		PreparedStatement statement = null;
		PreparedStatement statementAccountS = null;
		PreparedStatement statementAccountD = null;
		
		Transaction transaction = new Transaction();
		try
		{
			statement = connection.prepareStatement(CREATE_ACCOUNTOPERATIONS_STATEMENT);
			connection.setAutoCommit(false);
	
			statement.setInt(1, source_user_id);
			statement.setInt(2, ACCOUNT_OPERATION_PAYMENT);
			
			statement.setLong(3, amount);
			Date now = new Date();
			java.sql.Timestamp sqlDate = new java.sql.Timestamp(now.getTime());
			statement.setTimestamp(4, sqlDate);
			statement.setInt(5, source_user_id);
			statement.setInt(6, destination_user_id);
			
			statement.execute();			
			ResultSet rs = statement.getGeneratedKeys();
			if (rs.next())
			{
				transaction.setId(rs.getLong(1));
				transaction.setAmount(amount);
				transaction.setDate(now);
				transaction.setSource_user_id(source_user_id);
				transaction.setDestination_user_id(destination_user_id);
			}
			
			statementAccountS = connection.prepareStatement(UPDATE_ACCOUNT_FOR_ADDITION_STATEMENT);
			statementAccountS.setLong(1, -amount);
			statementAccountS.setInt(2, source_user_id);
			statementAccountS.execute();

			statementAccountD = connection.prepareStatement(UPDATE_ACCOUNT_FOR_ADDITION_STATEMENT);
			statementAccountD.setLong(1, amount);
			statementAccountD.setInt(2, destination_user_id);
			statementAccountD.execute();

			connection.commit();
		}
		catch (SQLException e)
		{
			if (connection != null)
			{
				connection.rollback();
			}
			throw e;
		}
		finally
		{
			if (statement != null)
			{
				statement.close();
			}
			if (statementAccountD != null)
			{
				statementAccountD.close();
			}
			if (statementAccountS != null)
			{
				statementAccountS.close();
			}
			if (connection != null)
			{
				connection.close();
			}
			
		}
		return transaction;
	}

	public void updateUser(User user) throws SQLException
	{
		Connection connection = getConnection();
		PreparedStatement statement = null;	
		try
		{
			statement = connection.prepareStatement(UPDATE_USER_STATEMENT);
			connection.setAutoCommit(true);
			statement.setString(1, user.getEmail());
			statement.setString(2, user.getPassword());
			statement.setInt(3, user.getReceiveNewsletter());
			statement.setInt(4, user.getConfirmEmailFlag());
			statement.setString(5, user.getConfirmEmailRandom());
			java.sql.Timestamp sqlDate = new java.sql.Timestamp(user.getConfirmEmailDate().getTime());
			statement.setTimestamp(6, sqlDate);
			statement.setInt(7, user.getId());
			statement.execute();
		}
		catch (SQLException e)
		{
			throw e;
		}
		finally
		{
			if (statement != null)
			{
				statement.close();
			}
			if (connection != null)
			{
				connection.close();
			}
		}
	}


	public Account getAccount(int user_id) throws SQLException
	{
		Connection connection = getConnection();
		PreparedStatement statement = null;	
		try
		{
			statement = connection.prepareStatement(GET_ACCOUNT_BY_ID_STATEMENT);
			statement.setInt(1, user_id);
			ResultSet rs = statement.executeQuery();
			if (rs.next())
			{
				Account account = new Account();
				account.setUser_id(user_id);
				account.setBalance(rs.getLong(2));
				return account;
			} else
			{
				return null;
			}
		}
		catch (SQLException e)
		{
			throw e;
		}
		finally
		{
			if (statement != null)
			{
				statement.close();
			}
			if (connection != null)
			{
				connection.close();
			}
		}
	}

	public DepositWithdrawal getDepositOrWithdrawal(long id) throws SQLException
	{
		Connection connection = getConnection();
		PreparedStatement statement = null;	
		try
		{
			statement = connection.prepareStatement(GET_ACCOUNTOPERATIONS_STATEMENT);
			statement.setLong(1, id);
			ResultSet rs = statement.executeQuery();
			if (rs.next())
			{
				DepositWithdrawal operation = new DepositWithdrawal();
				operation.setId(id);
				operation.setUser_id(rs.getInt(2));
				int type = rs.getInt(3);
				if (type == ACCOUNT_OPERATION_DEPOSIT)
				{
					operation.setType(OperationType.DEPOSIT);
				} else if (type == ACCOUNT_OPERATION_WITHDRAWAL)
				{
					operation.setType(OperationType.WITHDRAWAL);
				} else
				{
					return null;
				}
				operation.setDate(rs.getTimestamp(5));
				operation.setAmount(rs.getLong(4));
				return operation;
			} else
			{
				return null;
			}
		}
		catch (SQLException e)
		{
			throw e;
		}
		finally
		{
			if (statement != null)
			{
				statement.close();
			}
			if (connection != null)
			{
				connection.close();
			}
		}
	}

	public Transaction getTransaction(long id) throws SQLException
	{
		Connection connection = getConnection();
		PreparedStatement statement = null;	
		try
		{
			statement = connection.prepareStatement(GET_ACCOUNTOPERATIONS_STATEMENT);
			statement.setLong(1, id);
			ResultSet rs = statement.executeQuery();
			if (rs.next())
			{
				Transaction transaction = new Transaction();
				if (rs.getInt(3) != ACCOUNT_OPERATION_PAYMENT)
				{
					return null;
				}
				transaction.setId(id);
				transaction.setSource_user_id(rs.getInt(6));
				transaction.setDestination_user_id(rs.getInt(7));
				transaction.setDate(rs.getTimestamp(5));
				transaction.setAmount(rs.getLong(4));
				return transaction;
			} else
			{
				return null;
			}
		}
		catch (SQLException e)
		{
			throw e;
		}
		finally
		{
			if (statement != null)
			{
				statement.close();
			}
			if (connection != null)
			{
				connection.close();
			}
		}
	}

	public Map<Date, Integer> getUsersCountFromLastDays(int days) throws SQLException
	{
		Connection connection = getConnection();
		PreparedStatement statement = null;	
		Map<Date, Integer> result = new HashMap<Date, Integer>();
		try
		{
			statement = connection.prepareStatement(GET_USERS_COUNT_BY_DAYS);
			statement.setInt(1, days);
			ResultSet rs = statement.executeQuery();
			while (rs.next())
			{
				result.put(rs.getDate(2), rs.getInt(1));
			}
		}
		finally
		{
			if (statement != null)
			{
				statement.close();
			}
			if (connection != null)
			{
				connection.close();
			}
		}
		return result;		
	}
	
	public Map<Date, Integer> getSumOperationsFromLastDays(int type ,int days) throws SQLException
	{
		Connection connection = getConnection();
		PreparedStatement statement = null;	
		Map<Date, Integer> result = new HashMap<Date, Integer>();
		try
		{
			statement = connection.prepareStatement(GET_ACC_OPERATIONS_STATS_BY_DAYS);
			statement.setInt(1, type);
			statement.setInt(2, days);
			ResultSet rs = statement.executeQuery();
			while (rs.next())
			{
				result.put(rs.getDate(1), rs.getInt(3));
			}
		}
		finally
		{
			if (statement != null)
			{
				statement.close();
			}
			if (connection != null)
			{
				connection.close();
			}
		}
		return result;		
	}

	public Map<Date, Integer> getCountOperationsFromLastDays(int type ,int days) throws SQLException
	{
		Connection connection = getConnection();
		PreparedStatement statement = null;	
		Map<Date, Integer> result = new HashMap<Date, Integer>();
		try
		{
			statement = connection.prepareStatement(GET_ACC_OPERATIONS_STATS_BY_DAYS);
			statement.setInt(1, type);
			statement.setInt(2, days);
			ResultSet rs = statement.executeQuery();
			while (rs.next())
			{
				result.put(rs.getDate(1), rs.getInt(2));
			}
		}
		finally
		{
			if (statement != null)
			{
				statement.close();
			}
			if (connection != null)
			{
				connection.close();
			}
		}
		return result;		
	}

	public List<Transaction> getTransactionsForUser(int user_id, Date date, int page, int pageSize) throws SQLException
	{
		Connection connection = getConnection();
		List<Transaction> result = new ArrayList<Transaction>();
		PreparedStatement statement = null;	
		try
		{
			statement = connection.prepareStatement(GET_ALL_TRANSACTIONS_FOR_USER_STATEMENT);
			statement.setTimestamp(1, new Timestamp(date.getTime()));
			statement.setInt(2, user_id);
			statement.setInt(3, user_id);
			if (page<=0) page = 1;
			statement.setInt(4, (page-1)*(pageSize-1)); // the offset
			statement.setInt(5, pageSize); // how many records
			ResultSet rs = statement.executeQuery();
			while (rs.next())
			{
				Transaction transaction = new Transaction();
				transaction.setId(rs.getLong(1));
				transaction.setDate(rs.getTimestamp(2));				
				transaction.setSource_user_id(rs.getInt(3));
				transaction.setDestination_user_id(rs.getInt(4));				
				transaction.setAmount(rs.getLong(5));
				transaction.setSourceUserName(rs.getString(6));
				transaction.setDestinationUserName(rs.getString(7));
				result.add(transaction);
			} 
		}
		catch (SQLException e)
		{
			throw e;
		}
		finally
		{
			if (statement != null)
			{
				statement.close();
			}
			if (connection != null)
			{
				connection.close();
			}
		}
		return result;
	}

	public List<DepositWithdrawal> getDepositsWithdrawalsForUser(int user_id, DepositWithdrawal.OperationType type, Date date, int page, int pageSize) throws SQLException
	{
		Connection connection = getConnection();
		List<DepositWithdrawal> result = new ArrayList<DepositWithdrawal>();
		PreparedStatement statement = null;	
		try
		{
			statement = connection.prepareStatement(GET_ALL_DEPOSITWITHDRAWAL_FOR_USER_STATEMENT);
			statement.setTimestamp(1, new Timestamp(date.getTime()));
			statement.setInt(2, user_id);
			if (type == OperationType.DEPOSIT)
			{
				statement.setInt(3, ACCOUNT_OPERATION_DEPOSIT);				
			} else
			{
				statement.setInt(3, ACCOUNT_OPERATION_WITHDRAWAL); 
			}
			if (page<=0) page = 1;
			statement.setInt(4, (page-1)*(pageSize-1)); // the offset
			statement.setInt(5, pageSize); // how many records
			ResultSet rs = statement.executeQuery();
			while (rs.next())
			{
				DepositWithdrawal item = new DepositWithdrawal();
				item.setId(rs.getLong(1));
				item.setUser_id(rs.getInt(2));
				item.setType(type);
				item.setAmount(rs.getLong(4));
				item.setDate(rs.getTimestamp(5));				
				result.add(item);
			} 
		}
		catch (SQLException e)
		{
			throw e;
		}
		finally
		{
			if (statement != null)
			{
				statement.close();
			}
			if (connection != null)
			{
				connection.close();
			}
		}
		return result;
	}

	public List<AccountOperation> getAccountOperationsForUser(int user_id, Date date, int count) throws SQLException
	{
		Connection connection = getConnection();
		List<AccountOperation> result = new ArrayList<AccountOperation>();
		PreparedStatement statement = null;	
		try
		{
			statement = connection.prepareStatement(GET_ALL_ACCOUNTOPERATIONS_FOR_USER_STATEMENT);
			statement.setTimestamp(1, new Timestamp(date.getTime()));
			statement.setInt(2, user_id);
			statement.setInt(3, user_id);
			statement.setInt(4, user_id);
			statement.setInt(5, count); // how many records
			ResultSet rs = statement.executeQuery();
			while (rs.next())
			{
				AccountOperation item = new AccountOperation();
				item.setId(rs.getLong(1));
				item.setUser_id(rs.getInt(2));
				item.setType(rs.getInt(3));
				item.setAmount(rs.getLong(4));
				item.setDate(rs.getTimestamp(5));				
				item.setSource_user_id(rs.getInt(6));
				item.setDestination_user_id(rs.getInt(7));
				item.setSourceUserName(rs.getString(8));
				item.setDestinationUserName(rs.getString(9));
				result.add(item);
			} 
		}
		catch (SQLException e)
		{
			throw e;
		}
		finally
		{
			if (statement != null)
			{
				statement.close();
			}
			if (connection != null)
			{
				connection.close();
			}
		}
		return result;
	}

	public Session createSession(String sessionId) throws SQLException
	{
		Connection connection = getConnection();
		PreparedStatement statement = null;	
		try
		{
			Session session = new Session();
			statement = connection.prepareStatement(CREATE_SESSION_STATEMENT);
			connection.setAutoCommit(true);
			session.setId(sessionId);
			statement.setString(1, sessionId);
			session.setCreate_timestamp(new Date());
			java.sql.Timestamp sqlDate = new java.sql.Timestamp(session.getCreate_timestamp().getTime());
			statement.setTimestamp(2, sqlDate);
			HashMap<String, Object> sessionMap = new HashMap<String, Object>();
			setBinaryField(statement, 3, sessionMap);
			session.setSessionMap(sessionMap);
			statement.execute();
			return session; 
		}
		catch (SQLException e)
		{
			throw e;
		}
		finally
		{
			if (statement != null)
			{
				statement.close();
			}
			if (connection != null)
			{
				connection.close();
			}
		}
	}
	
	public Session getSession(String session_id) throws SQLException
	{
		Connection connection = getConnection();
		PreparedStatement statement = null;	
		try
		{
			statement = connection.prepareStatement(GET_SESSION_BY_ID_STATEMENT);
			statement.setString(1, session_id);
			ResultSet rs = statement.executeQuery();;
			if (rs.next())
			{
				Session session = getSessionFromResultSet(rs);
				return session;
			} else
			{
				return null;
			}
		}
		catch (SQLException e)
		{
			throw e;
		}
		finally
		{
			if (statement != null)
			{
				statement.close();
			}
			if (connection != null)
			{
				connection.close();
			}
		}
	}
	
	private void setBinaryField(PreparedStatement statement, int fieldIndex, Object object) throws SQLException
	{
		InputStream is = null;
		try
		{
			ByteArrayOutputStream bos = new ByteArrayOutputStream(4096);
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(object);
			is = new ByteArrayInputStream(bos.toByteArray());
		} catch (Exception e)	
		{
			
		};
		statement.setBinaryStream(fieldIndex, is);
	}
	public void setSession(Session session) throws SQLException
	{
		Connection connection = getConnection();
		PreparedStatement statement = null;	
		try
		{
			statement = connection.prepareStatement(UPDATE_SESSION_STATEMENT);
			statement.setString(2, session.getId());
			HashMap<String, Object> sessionMap = session.getSessionMap();
			setBinaryField(statement, 1, sessionMap);
			statement.execute();
		}
		catch (SQLException e)
		{
			throw e;
		}
		finally
		{
			if (statement != null)
			{
				statement.close();
			}
			if (connection != null)
			{
				connection.close();
			}
		}
	}

}
