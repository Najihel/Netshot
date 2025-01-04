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

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import net.netshot.netshot.Netshot;

@Converter
public class StringEncryptorConverter implements AttributeConverter<String, String> {

	private StandardPBEStringEncryptor encryptor;

	public StringEncryptorConverter() {
		this.encryptor = new StandardPBEStringEncryptor();
		String cryptPassword = Netshot.getConfig("netshot.db.encryptionpassword", null);
		if (cryptPassword == null) {
			cryptPassword = Netshot.getConfig("netshot.db.encryptionPassword", "NETSHOT"); // Historical reasons
		}
		this.encryptor.setPassword(cryptPassword);
	}

	@Override
	public String convertToDatabaseColumn(String attribute) {
		return this.encryptor.encrypt(attribute);
	}

	@Override
	public String convertToEntityAttribute(String dbData) {
		return this.encryptor.decrypt(dbData);
	}
	
}
