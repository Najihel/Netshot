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
package net.netshot.netshot.aaa;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonView;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.netshot.netshot.Netshot;
import net.netshot.netshot.aaa.PasswordPolicy.PasswordPolicyException;
import net.netshot.netshot.crypto.Argon2idHash;
import net.netshot.netshot.crypto.Hash;
import net.netshot.netshot.rest.RestViews.DefaultView;

import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hibernate.annotations.NaturalId;

/**
 * The User class represents a Netshot user.
 */
@Entity(name = "\"user\"")
@XmlRootElement @XmlAccessorType(value = XmlAccessType.NONE)
@Table(indexes = {
		@Index(name = "usernameIndex", columnList = "username") 
})
@EqualsAndHashCode
@Slf4j
public class UiUser implements User {

	/**
	 * Exception to be thrown in case of password check failure
	 */
	static public class WrongPasswordException extends Exception {
		public WrongPasswordException(String message) {
			super(message);
		}
	}

	/** The max idle time. */
	public static int MAX_IDLE_TIME;

	/**
	 * Hash the password for future storage.
	 *
	 * @param password the plaintext password
	 * @return the hashed password
	 */
	static private String hash(String password) {
		Hash hash = new Argon2idHash(password);
		return hash.toHashString();
	}

	static {
		UiUser.MAX_IDLE_TIME = Netshot.getConfig("netshot.aaa.maxidletime", 1800, 30, Integer.MAX_VALUE);
	}

	/** The id. */
	@Getter(onMethod=@__({
		@Id,
		@GeneratedValue(strategy = GenerationType.IDENTITY),
		@XmlElement, @JsonView(DefaultView.class)
	}))
	@Setter
	private long id;

	/** The local. */
	@Getter(onMethod=@__({
		@XmlElement, @JsonView(DefaultView.class)
	}))
	@Setter
	private boolean local;

	/** The hashed password. */
	@Getter
	@Setter
	private String hashedPassword;

	/** The previous password hashes. */
	@Getter
	@Setter
	private List<String> oldHashedPasswords = new ArrayList<>();

	/** Date of last password change */
	@Getter
	@Setter
	private Date lastPasswordChangeDate;

	/** The username. */
	@Getter(onMethod=@__({
		@XmlElement, @JsonView(DefaultView.class),
		@NaturalId(mutable = true)
	}))
	@Setter
	private String username;

	/** The level. */
	@Getter(onMethod=@__({
		@XmlElement, @JsonView(DefaultView.class),
	}))
	@Setter
	private int level = User.LEVEL_ADMIN;

	/**
	 * Instantiates a new user.
	 */
	protected UiUser() {

	}

	/**
	 * Instantiates a new user.
	 *
	 * @param username the username
	 * @param local the local
	 * @param password the password
	 */
	public UiUser(String username, boolean local, String password) {
		this.username = username;
		this.local = local;
		this.setPassword(password);
	}

	public UiUser(String name, int level) {
		this.username = name;
		this.level = level;
		this.local = false;
	}

	/**
	 * Sets the password.
	 *
	 * @param password the new password
	 */
	public void setPassword(String password) {
		this.setHashedPassword(UiUser.hash(password));
		this.setLastPasswordChangeDate(new Date());
	}

	/**
	 * Sets the password if it complies with the given policy.
	 *
	 * @param password the new password
	 * @param policy the policy to check
	 */
	public void setPassword(String password, PasswordPolicy policy) throws PasswordPolicyException {
		for (Map.Entry<PasswordPolicy.CharMatch, Integer> e : policy.getMinCharMatchCounts().entrySet()) {
			if (e.getKey().countMatches(password) < e.getValue()) {
				throw new PasswordPolicyException(String.format(
					"The password doesn't match the defined policy: %s", e.getKey().getDescription()));
			}
		}
		if (this.oldHashedPasswords == null) {
			this.oldHashedPasswords = new ArrayList<>();
		}
		int size = Math.min(policy.getMaxHistory(), this.oldHashedPasswords.size());
		for (int i = 0; i < size; i++) {
			try {
				Hash oldHash = Hash.fromHashString(this.oldHashedPasswords.get(i));
				if (oldHash.check(password)) {
					throw new PasswordPolicyException("Password was already used for this account");
				}
			}
			catch (PasswordPolicyException e) {
				throw e;
			}
			catch (Exception e) {
				log.error("Error while computing/checking user password hash vs old hash", e);
			}
		}
		if (this.getHashedPassword() != null) {
			this.oldHashedPasswords.addFirst(this.getHashedPassword());
		}
		while (this.oldHashedPasswords.size() > policy.getMaxHistory()) {
			this.oldHashedPasswords.removeLast();
		}
		this.setPassword(password);
	}

	/**
	 * Check password against user hash, and against policy.
	 *
	 * @param password the password
	 * @param policy The policy to check
	 * @throws WrongPasswordException 
	 * @throws PasswordPolicyException 
	 */
	public void checkPassword(String password, PasswordPolicy policy)
			throws WrongPasswordException, PasswordPolicyException {
		try {
			Hash hash = Hash.fromHashString(this.hashedPassword);
			if (!hash.check(password)) {
				throw new WrongPasswordException("Wrong password");
			}
		}
		catch (InvalidClassException e) {
			log.error("Error while reading/checking password hash", e);
			throw new WrongPasswordException("Unable to check password");
		}
		if (policy == null) {
			return;
		}
		if (this.lastPasswordChangeDate == null) {
			return;
		}
		int days = policy.getMaxDuration();
		if (days == 0) {
			return;
		}
		Calendar lastValidCal = Calendar.getInstance();
		lastValidCal.add(Calendar.DATE, -1 * policy.getMaxDuration());
		if (this.lastPasswordChangeDate.before(lastValidCal.getTime())) {
			throw new PasswordPolicyException("Password has expired, it must be changed");
		}
	}

	/* (non-Javadoc)
	 * @see java.security.Principal#getName()
	 */
	@Override
	@Transient
	public String getName() {
		return username;
	}

	@Override
	public String toString() {
		return "User " + id + " (username '" + username + "', level " + level + (local ? "" : ", remote user") + ")";
	}

}
