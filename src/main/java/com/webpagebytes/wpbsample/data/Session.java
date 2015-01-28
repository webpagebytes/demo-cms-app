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
import java.util.HashMap;

public class Session {
	private String id;
	private Integer user_id;
	private Date create_timestamp;
	private HashMap<String, Object> sessionMap;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Integer getUser_id() {
		return user_id;
	}
	public void setUser_id(Integer user_id) {
		this.user_id = user_id;
	}
	public Date getCreate_timestamp() {
		return create_timestamp;
	}
	public void setCreate_timestamp(Date create_timestamp) {
		this.create_timestamp = create_timestamp;
	}

	public HashMap<String, Object> getSessionMap()
	{
		if (sessionMap == null)
		{
			sessionMap = new HashMap<String, Object>();
		}
		return sessionMap;
	}
	public void setSessionMap(HashMap<String, Object> sessionMap)
	{
		this.sessionMap = sessionMap;
	}
	
}
