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
    private String sqlQuery;

    public void setSqlQuery(String sqlQuery) {
        this.sqlQuery = sqlQuery;
    }


    @Override
    public Optional<Long> getCurrentVersion() {
        try {
            Long version = jdbcTemplate.queryForObject(sqlQuery, Long.class);
            return Optional.of(version);
        } catch (Exception e) {
            log.debug("Failed to get version from database using default query", e);
            return Optional.empty();
        }
    }

    @Override
    public boolean isAvailable() {
        jdbcTemplate.queryForObject(sqlQuery, Long.class);
        return true;
    }

    @Override
    public String getDescription() {
        return "Database-based version checker using: " + sqlQuery;
    }
}