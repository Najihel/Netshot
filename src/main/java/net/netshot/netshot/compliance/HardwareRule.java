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
package net.netshot.netshot.compliance;

import java.util.Date;
import java.util.regex.PatternSyntaxException;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonView;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;
import net.netshot.netshot.device.Device;
import net.netshot.netshot.device.DeviceDriver;
import net.netshot.netshot.device.DeviceGroup;
import net.netshot.netshot.device.Module;
import net.netshot.netshot.rest.RestViews.DefaultView;

/**
 * The Class HardwareRule.
 */
@Entity
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class HardwareRule {

	/** The id. */
	@Getter(onMethod = @__({
		@Id, @GeneratedValue(strategy = GenerationType.IDENTITY),
		@XmlAttribute, @JsonView(DefaultView.class)
	}))
	@Setter
	private long id;

	/** The device class. */
	@Getter(onMethod = @__({
		@XmlElement, @JsonView(DefaultView.class)
	}))
	@Setter
	private String driver;

	/** The target group. */
	@Getter(onMethod = @__({
		@ManyToOne,
		@XmlElement, @JsonView(DefaultView.class),
		@OnDelete(action = OnDeleteAction.SET_NULL)
	}))
	@Setter
	private DeviceGroup targetGroup;

	/** The family. */
	@Getter(onMethod = @__({
		@XmlElement, @JsonView(DefaultView.class)
	}))
	@Setter
	private String family = "";

	/** The family reg exp. */
	@Getter(onMethod = @__({
		@XmlElement, @JsonView(DefaultView.class)
	}))
	@Setter
	private boolean familyRegExp;

	/** The part number. */
	@Getter(onMethod = @__({
		@XmlElement, @JsonView(DefaultView.class)
	}))
	@Setter
	private String partNumber;

	/** The part number reg exp. */
	@Getter(onMethod = @__({
		@XmlElement, @JsonView(DefaultView.class)
	}))
	@Setter
	private boolean partNumberRegExp;

	/** The end of sale. */
	@Getter(onMethod = @__({
		@XmlElement, @JsonView(DefaultView.class)
	}))
	@Setter
	private Date endOfSale;

	/** The end of life. */
	@Getter(onMethod = @__({
		@XmlElement, @JsonView(DefaultView.class)
	}))
	@Setter
	private Date endOfLife;

	/**
	 * Instantiates a new hardware rule.
	 */
	protected HardwareRule() {

	}

	public HardwareRule(String driver,
		DeviceGroup targetGroup, String family, boolean familyRegExp,
		String partNumber, boolean partNumberRegExp, Date endOfSale,
		Date endOfLife) {
		this.driver = driver;
		this.targetGroup = targetGroup;
		this.family = family;
		this.familyRegExp = familyRegExp;
		this.partNumber = partNumber;
		this.partNumberRegExp = partNumberRegExp;
		this.endOfSale = endOfSale;
		this.endOfLife = endOfLife;
	}

	/**
	 * Gets the device type.
	 *
	 * @return the device type
	 */
	@Transient
	@XmlElement
	@JsonView(DefaultView.class)
	public String getDeviceType() {
		DeviceDriver deviceDriver = DeviceDriver.getDriverByName(driver);
		if (deviceDriver == null) {
			return "";
		}
		return deviceDriver.getDescription();
	}

	/**
	 * Check.
	 *
	 * @param device the device
	 */
	public void check(Device device) {
		if (targetGroup != null && !targetGroup.getCachedDevices().contains(device)) {
			return;
		}
		if (driver != null && !driver.equals(device.getDriver())) {
			return;
		}
		if (familyRegExp) {
			try {
				if (!device.getFamily().matches(family)) {
					return;
				}
			}
			catch (PatternSyntaxException e) {
				return;
			}
		}
		else {
			if (!"".equals(family) && !device.getFamily().equals(family)) {
				return;
			}
		}

		for (Module module : device.getModules()) {
			if (partNumberRegExp) {
				try {
					if (!module.getPartNumber().matches(partNumber)) {
						continue;
					}
				}
				catch (PatternSyntaxException e) {
					return;
				}
			}
			else {
				if (!"".equals(partNumber) && !module.getPartNumber().equals(partNumber)) {
					continue;
				}
			}
			if (endOfLife != null && (device.getEolDate() == null || endOfLife.before(device.getEolDate()))) {
				device.setEolDate(endOfLife);
				device.setEolModule(module);
			}
			if (endOfSale != null && (device.getEosDate() == null || endOfSale.before(device.getEosDate()))) {
				device.setEosDate(endOfSale);
				device.setEosModule(module);
			}
		}
	}

	@Override
	public String toString() {
		return "Hardware compliance rule " + id;
	}
}
