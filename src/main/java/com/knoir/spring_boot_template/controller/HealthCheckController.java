package com.knoir.spring_boot_template.controller;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.time.Instant;

import com.knoir.spring_boot_template.dto.HealthCheckResponse;
import com.knoir.spring_boot_template.dto.HealthCheckResponse.AppInfo;
import com.knoir.spring_boot_template.dto.HealthCheckResponse.OsInfo;
import com.knoir.spring_boot_template.dto.HealthCheckResponse.ProcessMemoryInfo;
import com.knoir.spring_boot_template.dto.HealthCheckResponse.RuntimeInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

	@Value("${app.version}")
	private String appVersion;

	@Value("${app.environment}")
	private String environment;

	@GetMapping("/health")
	public ResponseEntity<HealthCheckResponse> healthCheck() {
		RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
		OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
		MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

		AppInfo app = new AppInfo(appVersion, environment);
		RuntimeInfo runtime = new RuntimeInfo(
			System.getProperty("java.version"),
			runtimeBean.getVmName()
		);

		long totalMemoryBytes = Runtime.getRuntime().totalMemory();
		long freeMemoryBytes = Runtime.getRuntime().freeMemory();
		if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {
			totalMemoryBytes = sunOsBean.getTotalMemorySize();
			freeMemoryBytes = sunOsBean.getFreeMemorySize();
		}

		OsInfo os = new OsInfo(
			osBean.getName(),
			osBean.getVersion(),
			osBean.getArch(),
			osBean.getAvailableProcessors(),
			totalMemoryBytes,
			freeMemoryBytes,
			osBean.getSystemLoadAverage()
		);

		MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
		MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
		ProcessMemoryInfo processMemory = new ProcessMemoryInfo(
			heapUsage.getUsed(),
			heapUsage.getCommitted(),
			heapUsage.getMax(),
			nonHeapUsage.getUsed(),
			nonHeapUsage.getCommitted()
		);

		HealthCheckResponse response = new HealthCheckResponse(
			"UP",
			Instant.now().toString(),
			app,
			runtime,
			runtimeBean.getUptime() / 1000.0,
			os,
			processMemory
		);

		return ResponseEntity.ok(response);
	}
}
