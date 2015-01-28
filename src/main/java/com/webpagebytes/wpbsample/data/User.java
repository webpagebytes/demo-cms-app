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

public class User {
	private String userName;
	private Integer receiveNewsletter;
	private String email;
	private Integer id;
	private String password;
	private Date open_date;
	private Integer confirmEmailFlag;
	private String confirmEmailRandom;
	private Date confirmEmailDate;
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public Integer getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public Date getOpen_date() {
		return open_date;
	}
	public void setOpen_date(Date open_date) {
		this.open_date = open_date;
	}
    public Integer getReceiveNewsletter() {
        return receiveNewsletter;
    }
    public void setReceiveNewsletter(Integer receiveNewsletter) {
        this.receiveNewsletter = receiveNewsletter;
    }
    public Integer getConfirmEmailFlag() {
        return confirmEmailFlag;
    }
    public void setConfirmEmailFlag(Integer confirmEmailFlag) {
        this.confirmEmailFlag = confirmEmailFlag;
    }
    public String getConfirmEmailRandom() {
        return confirmEmailRandom;
    }
    public void setConfirmEmailRandom(String confirmEmailRandom) {
        this.confirmEmailRandom = confirmEmailRandom;
    }
    public Date getConfirmEmailDate() {
        return confirmEmailDate;
    }
    public void setConfirmEmailDate(Date confirmEmailDate) {
        this.confirmEmailDate = confirmEmailDate;
    }
	
}
