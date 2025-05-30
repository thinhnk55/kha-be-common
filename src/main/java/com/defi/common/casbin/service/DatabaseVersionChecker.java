package com.defi.common.casbin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Database-based version checker implementation.
 * 
 * <p>
 * This implementation checks version numbers directly from the database
 * without going through API calls, providing the fastest possible version
 * checking for services that have direct database access.
 * </p>
 * 
 * @author Defi Team
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseVersionChecker implements VersionChecker {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<Long> getCurrentVersion(String code) {
        try {
            String sql = "SELECT version FROM auth_version WHERE code = ?";
            Long version = jdbcTemplate.queryForObject(sql, Long.class, code);
            return Optional.ofNullable(version);
        } catch (Exception e) {
            log.debug("Failed to get version for code: {} from database", code, e);
            return Optional.empty();
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            // Simple connectivity test
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return true;
        } catch (Exception e) {
            log.debug("Database version checker is not available", e);
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "Database-based version checker using direct SQL queries";
    }
}