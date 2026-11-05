package com.knoir.spring_boot_template.config;

import org.springframework.boot.web.server.Compression;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

@Configuration
public class CompressionConfig {

	private static final String[] COMPRESSIBLE_MIME_TYPES = {
		"text/html",
		"text/xml",
		"text/plain",
		"text/css",
		"text/javascript",
		"application/javascript",
		"application/json",
		"application/xml"
	};

	@Bean
	public WebServerFactoryCustomizer<ConfigurableWebServerFactory> compressionCustomizer() {
		return factory -> {
			Compression compression = new Compression();
			compression.setEnabled(true);
			compression.setMinResponseSize(DataSize.ofKilobytes(1));
			compression.setMimeTypes(COMPRESSIBLE_MIME_TYPES);
			factory.setCompression(compression);
		};
	}
}
