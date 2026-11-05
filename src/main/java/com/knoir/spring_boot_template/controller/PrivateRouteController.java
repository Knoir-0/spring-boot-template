package com.knoir.spring_boot_template.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PrivateRouteController {

	@GetMapping("/private-route-example/{id}")
	public ResponseEntity<String> getPrivateRouteExampleById(@PathVariable String id) {
		return ResponseEntity.ok(id);
	}

	@GetMapping("/private-route-example")
	public ResponseEntity<Void> getPrivateRouteExample() {
		return ResponseEntity.ok().build();
	}
}
