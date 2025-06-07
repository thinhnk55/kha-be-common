package com.defi.common.casbin.service;

import com.defi.common.casbin.entity.PolicyRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service for loading policies from database using custom SQL queries.
 * 
 * <p>
 * This service handles loading policy rules from database tables or views.
 * It supports custom SQL queries and resource-based filtering for optimal
 * performance.
 * </p>
 * 
 * @author Defi Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DatabasePolicyLoader {

    private final JdbcTemplate jdbcTemplate;

    /**
     * RowMapper for converting SQL query results to PolicyRule objects.
     */
    private static final RowMapper<PolicyRule> POLICY_RULE_ROW_MAPPER = new RowMapper<PolicyRule>() {
        @Override
        public PolicyRule mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
            return PolicyRule.builder()
                    .id(rs.getLong("id"))
                    .roleId(rs.getLong("role_id"))
                    .resourceCode(rs.getString("resource_code"))
                    .actionCode(rs.getString("action_code"))
                    .build();
        }
    };

    /**
     * Loads policy rules from database using a custom SQL query.
     * 
     * @param resources list of resource codes to filter by (empty list loads all)
     * @return list of policy rules loaded from database
     * @throws RuntimeException if database query fails
     */
    public List<PolicyRule> loadPolicyRulesFromDatabase(List<String> resources) {
        String sqlQuery = "SELECT * FROM auth.policy_rules";
        log.info("Loading policy rules from database with query: {}", sqlQuery);

        try {
            List<PolicyRule> policies;

            if (resources.isEmpty()) {
                // Load all policies without filtering
                policies = jdbcTemplate.query(sqlQuery, POLICY_RULE_ROW_MAPPER);
            } else {
                // Apply resource filtering - modify SQL to include WHERE clause
                String filteredQuery = addResourceFilterToQuery(sqlQuery, resources);
                policies = jdbcTemplate.query(filteredQuery, POLICY_RULE_ROW_MAPPER, resources.toArray());
            }

            log.info("Successfully loaded {} policy rules from database", policies.size());
            return policies;

        } catch (Exception e) {
            log.error("Failed to load policy rules from database", e);
            throw new RuntimeException("Database policy loading failed: " + e.getMessage(), e);
        }
    }

    /**
     * Adds resource filtering to a SQL query by appending WHERE clause.
     * 
     * <p>
     * This method intelligently adds resource filtering to existing SQL queries
     * by detecting if a WHERE clause already exists and adding the appropriate
     * condition.
     * </p>
     * 
     * @param originalQuery the original SQL query
     * @param resources     list of resource codes to filter by
     * @return modified SQL query with resource filtering
     */
    private String addResourceFilterToQuery(String originalQuery, List<String> resources) {
        if (resources.isEmpty()) {
            return originalQuery;
        }

        String upperQuery = originalQuery.toUpperCase().trim();
        String placeholders = String.join(",", resources.stream().map(r -> "?").toList());
        String resourceFilter = "resource_code IN (" + placeholders + ")";

        if (upperQuery.contains("WHERE")) {
            // Query already has WHERE clause, add AND condition
            return originalQuery + " AND " + resourceFilter;
        } else {
            // Query doesn't have WHERE clause, add it
            return originalQuery + " WHERE " + resourceFilter;
        }
    }

    /**
     * Tests database connectivity and query validity.
     * 
     * @param sqlQuery the SQL query to test
     * @return true if query is valid and database is accessible
     */
    public boolean isQueryValid(String sqlQuery) {
        try {
            jdbcTemplate.queryForRowSet(sqlQuery + " LIMIT 1");
            return true;
        } catch (Exception e) {
            log.warn("Database query validation failed for: {}", sqlQuery, e);
            return false;
        }
    }

    /**
     * Gets the current version for a specific component code from database.
     * 
     * <p>
     * This method provides direct database access to version information,
     * which is faster than API calls for services with database connectivity.
     * </p>
     * 
     * @param code the component code (e.g., "policy_version")
     * @return current version number, or empty if not found
     */
    public Optional<Long> getCurrentVersion(String code) {
        try {
            String sql = "SELECT version FROM auth_version WHERE code = ?";
            Long version = jdbcTemplate.queryForObject(sql, Long.class, code);
            log.debug("Retrieved version {} for code: {} from database", version, code);
            return Optional.ofNullable(version);
        } catch (Exception e) {
            log.debug("Failed to get version for code: {} from database", code, e);
            return Optional.empty();
        }
    }

    /**
     * Gets the current policy version specifically.
     * 
     * @return current policy version, or 0 if not found
     */
    public long getCurrentPolicyVersion() {
        return getCurrentVersion("policy_version").orElse(0L);
    }
}