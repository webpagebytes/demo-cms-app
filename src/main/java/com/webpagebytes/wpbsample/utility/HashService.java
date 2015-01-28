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

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashService {

	public static String getHashSha1(byte[] data) throws NoSuchAlgorithmException
	{
		   MessageDigest crypt = MessageDigest.getInstance("SHA-1");
		   crypt.reset();
		   crypt.update(data);
		   return new BigInteger(1, crypt.digest()).toString(16);
	}
}
