package com.knoir.spring_boot_template.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PublicRouteController {

	@GetMapping("/public-route-example/{id}")
	public ResponseEntity<String> getPublicRouteExampleById(@PathVariable String id) {
		return ResponseEntity.ok(id);
	}

	@GetMapping("/public-route-example")
	public ResponseEntity<Void> getPublicRouteExample() {
		return ResponseEntity.ok().build();
	}
}
