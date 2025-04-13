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
package net.netshot.netshot;

import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import net.netshot.netshot.device.Config;
import net.netshot.netshot.device.Device;
import net.netshot.netshot.device.DeviceGroup;
import net.netshot.netshot.device.Domain;
import net.netshot.netshot.device.DynamicDeviceGroup;
import net.netshot.netshot.device.Module;
import net.netshot.netshot.device.Network4Address;
import net.netshot.netshot.device.Network6Address;
import net.netshot.netshot.device.NetworkInterface;
import net.netshot.netshot.device.Device.NetworkClass;
import net.netshot.netshot.device.attribute.ConfigAttribute;
import net.netshot.netshot.device.attribute.ConfigLongTextAttribute;
import net.netshot.netshot.device.attribute.ConfigTextAttribute;
import net.netshot.netshot.device.attribute.DeviceAttribute;
import net.netshot.netshot.device.attribute.DeviceBinaryAttribute;
import net.netshot.netshot.device.attribute.DeviceNumericAttribute;
import net.netshot.netshot.device.attribute.DeviceTextAttribute;
import net.netshot.netshot.device.attribute.AttributeDefinition.AttributeType;
import net.netshot.netshot.device.credentials.DeviceCredentialSet;
import net.netshot.netshot.device.credentials.DeviceSshAccount;
import net.netshot.netshot.diagnostic.Diagnostic;
import net.netshot.netshot.diagnostic.DiagnosticResult;
import net.netshot.netshot.diagnostic.DiagnosticTextResult;
import net.netshot.netshot.diagnostic.SimpleDiagnostic;

public class FakeDeviceFactory {

	static public Device getFakeCiscoIosDevice() {
		Domain domain = new Domain("Test domain", "Fake domain for tests", null, null);
		Diagnostic diagnostic = getReloadReasonIosSimpleDiagnostic();
		return FakeDeviceFactory.getFakeCiscoIosDevice(domain, diagnostic, 100);
	}

	static public Config getFakeCiscoIosConfig(Device device) {
		Config config = new Config(device);
		ConfigAttribute runningConfig = new ConfigLongTextAttribute(config, "runningConfig", "" +
			"version 16.1\n" +
			"no service pad\n" +
			"service timestamps debug datetime msec\n" +
			"no service password-encryption\n" +
			"!\n" +
			"hostname " + device.getName() + "\n" +
			"!\n"+
			"boot-start-marker\n" +
			"boot-end-marker\n" +
			"!\n" +
			"enable secret $6$aaa\n" +
			"!\n" +
			"ip cef\n" +
			"ipv6 unicast-routing\f\n" + // \f to test normalization
			"!\n" +
			"interface GigabitEthernet0/0\n" +
			" description Description of Gi0/0\n" +
			" ip address 10.0.0.1 255.255.255.0\n" +
			" no shutdown\n" +
			"!\n" +
			"interface GigabitEthernet0/1\n" +
			" description Description of Gi0/1\n" +
			" ip address 10.0.1.1 255.255.255.0\n" +
			" no shutdown\n" +
			"!\n" +
			"interface GigabitEthernet0/2\n" +
			" description Description of Gi0/2\n" +
			" ip address 10.0.2.1 255.255.255.0\n" +
			"!\n" +
			"line con 0\n" +
			" password something\n" +
			"line vty 0 4\n" +
			" password something\n" +
			" transport input all\n" +
			" transport output all\n" +
			"!\n");
		config.addAttribute(runningConfig);
		ConfigAttribute iosVersion = new ConfigTextAttribute(config, "iosVersion", "16.1.6");
		config.addAttribute(iosVersion);
		return config;
	}

	static public Device getFakeCiscoIosDevice(Domain domain, Diagnostic diagnostic, int shift) {
		Network4Address mgmtIp = null;
		try {
			mgmtIp = new Network4Address("172.16.0.0");
			mgmtIp.setAddress(mgmtIp.getAddress() + shift);
		}
		catch (UnknownHostException e) {
			// Ignore
		}
		Device device = new Device("CiscoIOS12", mgmtIp, domain, "test");
		device.setName(String.format("router%05d", shift));
		device.setFamily("Unknown IOS device");
		device.setNetworkClass(NetworkClass.ROUTER);
		device.setLocation("Test Location");
		device.setContact("Test Contact");
		device.setSoftwareVersion("16.1.6");
		device.setCreator("tester");
		DeviceCredentialSet cs = new DeviceSshAccount("user", "pass", "pass",
			DeviceCredentialSet.generateSpecificName());
		cs.setDeviceSpecific(true);
		device.setSpecificCredentialSet(cs);
		device.setSerialNumber("16161616TEST16");
		device.setComments("Comments for testing");
		device.setVrfInstances(new HashSet<String>(Set.of("VRF1", "VRF2")));
		device.getModules().add(new Module("chassis", "TESTCHASSIS", "16161616TEST16", device));
		device.getModules().add(new Module("slot", "TESTSLOT", "29038POSD203", device));
		{
			NetworkInterface ni = new NetworkInterface(device, "GigabitEthernet0/0", "", "VRF1", true, true, "Desc for interface 0/0");
			try {
				Network4Address na1 = new Network4Address("10.0.0.1", 25);
				na1.setAddress(na1.getAddress() + (shift << 8));
				ni.addIpAddress(na1);
				Network6Address na2 = new Network6Address(
					String.format("2016:1:%x:1::1", shift), 64);
				ni.addIpAddress(na2);
			}
			catch (UnknownHostException e) {
			}
			device.getNetworkInterfaces().add(ni);
		}
		{
			NetworkInterface ni = new NetworkInterface(device, "GigabitEthernet0/1", "", "VRF2", false, true, "Desc for interface 0/1");
			try {
				Network4Address na1 = new Network4Address("10.0.0.129", 25);
				na1.setAddress(na1.getAddress() + (shift << 8));
				ni.addIpAddress(na1);
				Network6Address na2 = new Network6Address(
					String.format("2016:1:%x:2::1", shift), 64);
				ni.addIpAddress(na2);
			}
			catch (UnknownHostException e) {
			}
			device.getNetworkInterfaces().add(ni);
		}
		Config config = getFakeCiscoIosConfig(device);
		device.getConfigs().add(config);
		device.setLastConfig(config);
		
		DeviceAttribute mainMemorySize = new DeviceNumericAttribute(device, "mainMemorySize", 2048);
		device.addAttribute(mainMemorySize);
		DeviceAttribute configRegister = new DeviceTextAttribute(device, "configRegister", "0x1202");
		device.addAttribute(configRegister);
		DeviceAttribute configurationSaved = new DeviceBinaryAttribute(device, "configurationSaved", false);
		device.addAttribute(configurationSaved);

		if (diagnostic != null) {
			DiagnosticResult reloadReasonDiagResult = new DiagnosticTextResult(device, diagnostic, "Reload Command");
			device.addDiagnosticResult(reloadReasonDiagResult);
		}

		return device;
	}

	static public DeviceGroup getAllDevicesGroup() {
		DeviceGroup group = new DynamicDeviceGroup("All", null, "");
		return group;
	}

	static public Diagnostic getReloadReasonIosSimpleDiagnostic() {
		Diagnostic diagnostic = new SimpleDiagnostic("Reload reason", true,
			getAllDevicesGroup(), AttributeType.TEXT, "CiscoIOS12",
			"enable",
			"show version | include reason",
			"(?s).*Last reload reason: (.+?)[\r\n]+",
			"$1");
		return diagnostic;
	}
}
