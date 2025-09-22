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
package net.netshot.netshot.crypto;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import lombok.extern.slf4j.Slf4j;

/**
 * Legacy MD5/DES-based password based encryption/decryption util class.
 */
@Slf4j
public final class Md5DesPasswordBasedEncryptor extends PasswordBasedEncryptor {

	private static final String DEFAULT_ALGORITHM = "PBEWithMD5AndDES";
	private static final Charset MESSAGE_CHARSET = StandardCharsets.UTF_8;
	private static final Charset ENCRYPTED_CHARSET = StandardCharsets.US_ASCII;

	private static final int DEFAULT_SALT_SIZE = 8;
	private static final int DEFAULT_KEY_ITERATIONS = 1000;

	private int saltSize = DEFAULT_SALT_SIZE;
	private int keyIterations = DEFAULT_KEY_ITERATIONS;

	public Md5DesPasswordBasedEncryptor(String password) {
		super(password);
	}

	private byte[] generateSalt() {
		SecureRandom secureRandom = new SecureRandom();
		byte[] salt = new byte[this.saltSize];
		secureRandom.nextBytes(salt);
		return salt;
	}

	@Override
	public String encrypt(String input) {
		return this.encrypt(input, this.generateSalt());
	}

	public String encrypt(String input, byte[] salt) {
		if (input == null) {
			return null;
		}

		try {
			final byte[] inputBytes = input.getBytes(MESSAGE_CHARSET);
			Cipher cipher = Cipher.getInstance(DEFAULT_ALGORITHM);
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DEFAULT_ALGORITHM);

			PBEKeySpec keySpec = new PBEKeySpec(this.password.toCharArray());
			SecretKey key = keyFactory.generateSecret(keySpec);
			PBEParameterSpec paramSpec = new PBEParameterSpec(salt, this.keyIterations);

			cipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
			byte[] encrypted = cipher.doFinal(inputBytes);
			byte[] saltedEncrypted = new byte[salt.length + encrypted.length];
			System.arraycopy(salt, 0, saltedEncrypted, 0, salt.length);
			System.arraycopy(encrypted, 0, saltedEncrypted, salt.length, encrypted.length);
			return new String(Base64.getEncoder().encode(saltedEncrypted), ENCRYPTED_CHARSET);
		}
		catch (Exception e) {
			log.error("Unable to encrypt data with given arguments", e);
			throw new IllegalArgumentException("Unable to encrypt data with given arguments", e);
		}
	}

	public String decrypt(byte[] inputBytes, byte[] salt) {

		try {
			Cipher cipher = Cipher.getInstance(DEFAULT_ALGORITHM);
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DEFAULT_ALGORITHM);

			PBEKeySpec keySpec = new PBEKeySpec(this.password.toCharArray());
			SecretKey key = keyFactory.generateSecret(keySpec);
			PBEParameterSpec paramSpec = new PBEParameterSpec(salt, this.keyIterations);

			cipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
			byte[] decrypted = cipher.doFinal(inputBytes);
			return new String(decrypted, MESSAGE_CHARSET);
		}
		catch (Exception e) {
			log.error("Unable to decrypt data with given arguments", e);
			throw new IllegalArgumentException("Unable to decrypt data with given arguments", e);
		}
	}

	@Override
	public String decrypt(String input) {
		if (input == null) {
			return null;
		}
		byte[] inputBytes = Base64.getDecoder().decode(input.getBytes(ENCRYPTED_CHARSET));
		byte[] salt = new byte[this.saltSize];
		System.arraycopy(inputBytes, 0, salt, 0, this.saltSize);
		inputBytes = Arrays.copyOfRange(inputBytes, this.saltSize, inputBytes.length);
		return this.decrypt(inputBytes, salt);
	}

}
