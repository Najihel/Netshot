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

import org.graalvm.polyglot.Value;

import com.fasterxml.jackson.annotation.JsonView;

import jakarta.xml.bind.annotation.XmlElement;
import lombok.Getter;
import lombok.Setter;
import net.netshot.netshot.device.script.helper.JsDeviceHelper;
import net.netshot.netshot.rest.RestViews.DefaultView;

public final class AttributeDefinition {

	public enum AttributeLevel {
		DEVICE,
		CONFIG
	}

	public enum AttributeType {
		NUMERIC,
		TEXT,
		LONGTEXT,
		DATE,
		BINARY,
		BINARYFILE
	}

	@Getter(onMethod = @__({
		@XmlElement, @JsonView(DefaultView.class)
	}))
	@Setter
	private AttributeType type;

	@Getter(onMethod = @__({
		@XmlElement, @JsonView(DefaultView.class)
	}))
	@Setter
	private AttributeLevel level;

	@Getter(onMethod = @__({
		@XmlElement, @JsonView(DefaultView.class)
	}))
	@Setter
	private String name;

	@Getter(onMethod = @__({
		@XmlElement, @JsonView(DefaultView.class)
	}))
	@Setter
	private String title;

	@Getter(onMethod = @__({
		@XmlElement, @JsonView(DefaultView.class)
	}))
	@Setter
	private boolean comparable;

	@Getter(onMethod = @__({
		@XmlElement, @JsonView(DefaultView.class)
	}))
	@Setter
	private boolean searchable;

	@Getter(onMethod = @__({
		@XmlElement, @JsonView(DefaultView.class)
	}))
	@Setter
	private boolean checkable;

	@Getter
	@Setter
	private boolean dump;

	@Getter
	@Setter
	private String preDump;

	@Getter
	@Setter
	private String postDump;

	@Getter
	@Setter
	private String preLineDump;

	@Getter
	@Setter
	private String postLineDump;

	protected AttributeDefinition() {

	}

	public AttributeDefinition(AttributeType type, AttributeLevel level, String name,
		String title, boolean comparable, boolean searchable, boolean checkable) {
		super();
		this.type = type;
		this.level = level;
		this.name = name;
		this.title = title;
		this.comparable = comparable;
		this.searchable = searchable;
		this.checkable = checkable;
	}

	public AttributeDefinition(AttributeLevel level, String name, Value data) throws Exception {
		this.level = level;
		this.name = name;
		this.title = data.getMember("title").asString();
		if (!this.title.matches("^[0-9A-Za-z\\-_\\(\\)][0-9A-Za-z \\-_\\(\\)]+$")) {
			throw new IllegalArgumentException("Invalid title for item %s.");
		}
		String textType = data.getMember("type").asString();
		switch (textType) {
			case "Text":
				this.type = AttributeType.TEXT;
				break;
			case "LongText":
				this.type = AttributeType.LONGTEXT;
				break;
			case "Numeric":
				this.type = AttributeType.NUMERIC;
				break;
			case "Binary":
				this.type = AttributeType.BINARY;
				break;
			case "BinaryFile":
				this.type = AttributeType.BINARYFILE;
				break;
			default:
				throw new IllegalArgumentException("Invalid type for item %s.");
		}
		this.searchable = JsDeviceHelper.getBooleanMember(data, "searchable", false);
		this.comparable = JsDeviceHelper.getBooleanMember(data, "comparable", false);
		this.checkable = JsDeviceHelper.getBooleanMember(data, "checkable", false);
		Value dataDump = data.getMember("dump");
		if (dataDump == null) {
			this.dump = false;
		}
		else {
			if (dataDump.isBoolean()) {
				this.dump = dataDump.asBoolean();
			}
			else {
				this.dump = true;
				if (dataDump.hasMember("pre")) {
					this.preDump = dataDump.getMember("pre").asString();
				}
				if (dataDump.hasMember("preLine")) {
					this.preLineDump = dataDump.getMember("preLine").asString();
				}
				if (dataDump.hasMember("post")) {
					this.postDump = dataDump.getMember("post").asString();
				}
				if (dataDump.hasMember("postLine")) {
					this.postLineDump = dataDump.getMember("postLine").asString();
				}

			}
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AttributeDefinition [");
		if (this.type != null) {
			builder.append("type=");
			builder.append(this.type);
			builder.append(", ");
		}
		if (this.level != null) {
			builder.append("level=");
			builder.append(this.level);
			builder.append(", ");
		}
		if (this.name != null) {
			builder.append("name=");
			builder.append(this.name);
			builder.append(", ");
		}
		if (this.title != null) {
			builder.append("title=");
			builder.append(this.title);
			builder.append(", ");
		}
		builder.append("comparable=");
		builder.append(this.comparable);
		builder.append(", searchable=");
		builder.append(this.searchable);
		builder.append("]");
		return builder.toString();
	}
}
