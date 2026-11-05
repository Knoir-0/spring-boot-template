package com.knoir.spring_boot_template.dto;

public record HealthCheckResponse(
	String status,
	String timestamp,
	AppInfo app,
	RuntimeInfo runtime,
	double appUptimeSeconds,
	OsInfo os,
	ProcessMemoryInfo processMemory
) {
	public record AppInfo(
		String version,
		String environment
	) {}

	public record RuntimeInfo(
		String javaVersion,
		String jvmName
	) {}

	public record OsInfo(
		String platform,
		String release,
		String arch,
		int availableProcessors,
		long totalMemoryBytes,
		long freeMemoryBytes,
		double loadAverage
	) {}

	public record ProcessMemoryInfo(
		long heapUsedBytes,
		long heapCommittedBytes,
		long heapMaxBytes,
		long nonHeapUsedBytes,
		long nonHeapCommittedBytes
	) {}
}
