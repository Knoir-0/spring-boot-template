package com.knoir.spring_boot_template.enumeration;

public enum Environment {
	LOCAL("local"),
	DEV("dev"),
	PRD("prd");

	private final String value;

	Environment(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static Environment fromString(String value) {
		if (value == null) {
			return LOCAL;
		}
		for (Environment env : values()) {
			if (env.value.equalsIgnoreCase(value.trim())) {
				return env;
			}
		}
		return LOCAL;
	}

	public boolean isLocalOrDev() {
		return this == LOCAL || this == DEV;
	}
}
