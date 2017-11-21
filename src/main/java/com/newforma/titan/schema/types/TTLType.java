package com.newforma.titan.schema.types;

import java.time.Duration;

public class TTLType {
	private final Duration duration;

	public TTLType(Duration duration) {
		this.duration = duration;
	}

	public Duration getDuration() {
		return duration;
	}
}
