package com.example.demo.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Test security configuration to ensure method-level security works in tests.
 * This explicitly enables @PreAuthorize annotations for test environments.
 */
@TestConfiguration
@EnableMethodSecurity(prePostEnabled = true)
public class TestSecurityConfig {

    // No additional configuration needed - just ensuring method security is enabled
}