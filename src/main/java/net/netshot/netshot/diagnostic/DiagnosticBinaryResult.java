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
package net.netshot.netshot.diagnostic;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import jakarta.xml.bind.annotation.XmlElement;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonView;

import lombok.Getter;
import lombok.Setter;
import net.netshot.netshot.device.Device;
import net.netshot.netshot.rest.RestViews.DefaultView;

@Entity @DiscriminatorValue("B")
public class DiagnosticBinaryResult extends DiagnosticResult {

	@Getter(onMethod=@__({
		@XmlElement, @JsonView(DefaultView.class)
	}))
	@Setter
	private Boolean assumption;

	protected DiagnosticBinaryResult() {
	}
	
	public DiagnosticBinaryResult(Device device, Diagnostic diagnostic, boolean value) {
		super(device, diagnostic);
		this.assumption = value;
	}
	
	public DiagnosticBinaryResult(Device device, Diagnostic diagnostic, String value) {
		super(device, diagnostic);
		this.assumption = true;
		if (value == null || "false".equals(value) || "FALSE".equals(value) || "False".equals(value) || "0".equals(value)) {
			this.assumption = false;
		}
	}

	@Override
	@Transient
	public String getAsText() {
		if (Boolean.TRUE.equals(assumption)) {
			return "true";
		}
		else {
			return "false";
		}
	}
	
	@Override
	@Transient
	public Object getData() {
		return getAssumption();
	}

	@Override
	public boolean valueEquals(DiagnosticResult obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof DiagnosticBinaryResult))
			return false;
		DiagnosticBinaryResult other = (DiagnosticBinaryResult) obj;
		return Objects.equals(this.assumption, other.assumption);
	}

}
