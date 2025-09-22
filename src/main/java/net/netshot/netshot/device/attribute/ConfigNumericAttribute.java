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
package net.netshot.netshot.device.attribute;

import com.fasterxml.jackson.annotation.JsonView;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Getter;
import lombok.Setter;
import net.netshot.netshot.device.Config;
import net.netshot.netshot.rest.RestViews.DefaultView;

@Entity
@DiscriminatorValue("N")
public final class ConfigNumericAttribute extends ConfigAttribute {

	@Getter(onMethod = @__({
		@XmlElement, @JsonView(DefaultView.class)
	}))
	@Setter
	private Double number;

	protected ConfigNumericAttribute() {
	}

	public ConfigNumericAttribute(Config config, String name, double value) {
		super(config, name);
		this.number = value;
	}

	@Override
	@Transient
	public String getAsText() {
		if (this.number == null) {
			return "";
		}
		return this.number.toString();
	}

	@Override
	@Transient
	public Object getData() {
		return getNumber();
	}

	@Override
	public boolean valueEquals(ConfigAttribute obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ConfigNumericAttribute)) {
			return false;
		}
		ConfigNumericAttribute other = (ConfigNumericAttribute) obj;
		if (this.number == null) {
			if (other.number != null) {
				return false;
			}
		}
		else if (!this.number.equals(other.number)) {
			return false;
		}
		return true;
	}

}
