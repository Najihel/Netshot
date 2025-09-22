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
package net.netshot.netshot.rest;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.jakarta.rs.json.JacksonXmlBindJsonProvider;
import com.fasterxml.jackson.jakarta.rs.xml.JacksonXmlBindXMLProvider;
import com.fasterxml.jackson.jakarta.rs.yaml.JacksonXmlBindYAMLProvider;

import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.jaxrs2.integration.resources.AcceptHeaderOpenApiResource;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import io.swagger.v3.oas.models.servers.Server;
import lombok.extern.slf4j.Slf4j;
import net.netshot.netshot.rest.RestViews.RestApiView;

/**
 * Netshot Web application definition.
 */
@Slf4j
public class NetshotWebApplication extends ResourceConfig {
	public NetshotWebApplication() {
		registerClasses(RestService.class, SecurityFilter.class, ApiTokenAuthFilter.class);
		register(NetshotExceptionMapper.class);
		register(RolesAllowedDynamicFeature.class);
		property(ServerProperties.RESPONSE_SET_STATUS_OVER_SEND_ERROR, "true");
		property(ServerProperties.APPLICATION_NAME, "Netshot");
		register(LoggerFilter.class);
		register(ResponseCodeFilter.class);
		// property(ServerProperties.TRACING, "ALL");

		// JSON
		JacksonXmlBindJsonProvider jsonProvider = new JacksonXmlBindJsonProvider();
		jsonProvider.setDefaultView(RestApiView.class);
		jsonProvider.setMapper(JsonMapper.builder()
			.disable(MapperFeature.DEFAULT_VIEW_INCLUSION)
			.build());
		register(jsonProvider);

		// XML
		JacksonXmlBindXMLProvider xmlProvider = new JacksonXmlBindXMLProvider();
		xmlProvider.setDefaultView(RestApiView.class);
		xmlProvider.setMapper(XmlMapper.builder()
			.disable(MapperFeature.DEFAULT_VIEW_INCLUSION)
			.build());
		register(xmlProvider);

		// YAML
		JacksonXmlBindYAMLProvider yamlProvider = new JacksonXmlBindYAMLProvider();
		yamlProvider.setDefaultView(RestApiView.class);
		yamlProvider.setMapper(YAMLMapper.builder()
			.disable(MapperFeature.DEFAULT_VIEW_INCLUSION)
			.build());
		register(yamlProvider);

		// Swagger
		registerClasses(OpenApiResource.class, AcceptHeaderOpenApiResource.class);
		OpenAPI oas = new OpenAPI();
		oas.info(new Info()
			.title("Netshot API")
			.version("1")
			.description("Network Infrastructure Configuration and Compliance Management Software")
			.contact(new Contact().email("contact@netshot.net"))
			.license(new License().name("GPLv3").url("https://www.gnu.org/licenses/gpl-3.0.txt")));
		oas.servers(Arrays.asList(new Server().url(RestService.HTTP_API_PATH)));
		oas.components(new Components()
			.addSecuritySchemes("ApiTokenAuth", new SecurityScheme()
				.name(ApiTokenAuthFilter.HTTP_API_TOKEN_HEADER)
				.type(Type.APIKEY).in(In.HEADER))
			.addSecuritySchemes("CookieAuth", new SecurityScheme()
				.name(RestService.SESSION_COOKIE_NAME)
				.type(Type.APIKEY).in(In.COOKIE)));
		oas.addSecurityItem(new SecurityRequirement().addList("ApiTokenAuth"));
		oas.addSecurityItem(new SecurityRequirement().addList("CookieAuth"));
		SwaggerConfiguration oasConfig = new SwaggerConfiguration().openAPI(oas).prettyPrint(true)
			.resourcePackages(Stream.of(RestService.class.getPackageName()).collect(Collectors.toSet()));

		try {
			new JaxrsOpenApiContextBuilder<>().application(this).openApiConfiguration(oasConfig).buildContext(true);
		}
		catch (OpenApiConfigurationException e) {
			log.error("Can't initialize OpenAPI for JAX-RS", e);
		}
	}
}
