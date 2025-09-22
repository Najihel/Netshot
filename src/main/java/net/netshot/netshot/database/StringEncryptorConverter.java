/**
 * Copyright 2013-2025 Netshot
 * 
 * This file is part of Netshot project.
 * 
 * Netshot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Netshot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Netshot.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.netshot.netshot.database;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import net.netshot.netshot.Netshot;
import net.netshot.netshot.crypto.Sha2AesPasswordBasedEncryptor;

@Converter
public class StringEncryptorConverter implements AttributeConverter<String, String> {

	private static String cryptPassword;

	static {
		cryptPassword = Netshot.getConfig("netshot.db.encryptionpassword", null);
		if (cryptPassword == null) {
			// With capital P, for historical reasons
			cryptPassword = Netshot.getConfig("netshot.db.encryptionPassword", "NETSHOT");
		}
	}

	@Override
	public String convertToDatabaseColumn(String attribute) {
		Sha2AesPasswordBasedEncryptor encryptor = new Sha2AesPasswordBasedEncryptor(cryptPassword);
		return encryptor.encrypt(attribute);
	}

	@Override
	public String convertToEntityAttribute(String dbData) {
		Sha2AesPasswordBasedEncryptor encryptor = new Sha2AesPasswordBasedEncryptor(cryptPassword);
		return encryptor.decrypt(dbData);
	}

}
