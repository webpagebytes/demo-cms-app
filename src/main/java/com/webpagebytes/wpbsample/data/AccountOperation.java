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

package com.webpagebytes.wpbsample.data;

import java.util.Date;

public class AccountOperation {
	private long id;
	private int type;
	private int user_id;
	private long amount;
	private Date date;
	int source_user_id;
	int destination_user_id;
	String sourceUserName;
	String destinationUserName;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getUser_id() {
		return user_id;
	}
	public void setUser_id(int user_id) {
		this.user_id = user_id;
	}
	public long getAmount() {
		return amount;
	}
	public void setAmount(long amount) {
		this.amount = amount;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public int getSource_user_id() {
		return source_user_id;
	}
	public void setSource_user_id(int source_user_id) {
		this.source_user_id = source_user_id;
	}
	public int getDestination_user_id() {
		return destination_user_id;
	}
	public void setDestination_user_id(int destination_user_id) {
		this.destination_user_id = destination_user_id;
	}
	public String getSourceUserName() {
		return sourceUserName;
	}
	public void setSourceUserName(String sourceUserName) {
		this.sourceUserName = sourceUserName;
	}
	public String getDestinationUserName() {
		return destinationUserName;
	}
	public void setDestinationUserName(String destinationUserName) {
		this.destinationUserName = destinationUserName;
	}

	
}
