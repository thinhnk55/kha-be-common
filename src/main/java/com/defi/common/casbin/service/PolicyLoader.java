package com.defi.common.casbin.service;

import com.defi.common.casbin.config.CasbinProperties;
import com.defi.common.casbin.entity.PolicyRule;
import com.defi.common.casbin.util.PolicySourceParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.casbin.jcasbin.main.Enforcer;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service responsible for loading policies into Casbin enforcer from multiple
 * sources.
 * 
 * <p>
 * This service orchestrates the complete policy loading process by:
 * </p>
 * <ul>
 * <li>Parsing and validating policy source configuration</li>
 * <li>Delegating to appropriate source-specific loaders</li>
 * <li>Applying resource filtering for microservice architectures</li>
 * <li>Batch loading into Casbin enforcer for optimal performance</li>
 * </ul>
 * 
 * <p>
 * Supported policy sources:
 * </p>
 * <ul>
 * <li><strong>Database</strong>: Custom SQL queries via
 * {@link DatabasePolicyLoader}</li>
 * <li><strong>Resource files</strong>: CSV files from classpath via
 * {@link ResourcePolicyLoader}</li>
 * <li><strong>API endpoints</strong>: REST APIs returning JSON via
 * {@link ApiPolicyLoader}</li>
 * </ul>
 * 
 * @author Defi Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyLoader {

    private final CasbinProperties casbinProperties;
    private final DatabasePolicyLoader databasePolicyLoader;
    private final ResourcePolicyLoader resourcePolicyLoader;
    private final ApiPolicyLoader apiPolicyLoader;

    /**
     * Loads policies into the Casbin enforcer from configured source.
     * 
     * <p>
     * This method performs a complete policy reload:
     * </p>
     * <ol>
     * <li>Parses and validates the policy source configuration</li>
     * <li>Delegates to appropriate source-specific loader</li>
     * <li>Applies resource filtering if configured</li>
     * <li>Clears existing policies from enforcer</li>
     * <li>Batch loads new policies into enforcer for optimal performance</li>
     * </ol>
     * 
     * @param enforcer the Casbin enforcer to load policies into
     * @throws RuntimeException if policy loading fails
     */
    public void loadPolicies(Enforcer enforcer) {
        String policySource = casbinProperties.getPolicySource();
        List<String> resources = casbinProperties.getResources();

        log.info("Starting policy loading with source: {} and resources: {}", policySource, resources);

        try {
            // Parse and validate policy source configuration
            PolicySourceParser.PolicySourceConfig config = PolicySourceParser.parse(policySource);

            // Load policies from the specified source
            List<PolicyRule> policies = loadPolicyRules(config.getType(), config.getQuery(), resources);

            // Clear existing policies first
            enforcer.clearPolicy();

            // Load new policies into enforcer
            loadPoliciesIntoEnforcer(enforcer, policies);

            log.info("Policy loading completed successfully - {} policies loaded from source: {}",
                    policies.size(), config.getType());

        } catch (Exception e) {
            log.error("Failed to load policies from source: {}", policySource, e);
            throw new RuntimeException("Policy loading failed: " + e.getMessage(), e);
        }
    }

    /**
     * Loads policies into Casbin enforcer using batch operations for optimal
     * performance.
     * 
     * <p>
     * This method converts all policy rules to Casbin format and performs a single
     * batch insert operation, which is significantly faster than individual inserts
     * when dealing with large numbers of policies.
     * </p>
     * 
     * @param enforcer the Casbin enforcer to load policies into
     * @param policies list of policy rules to load
     * @throws RuntimeException if batch loading fails
     */
    private void loadPoliciesIntoEnforcer(Enforcer enforcer, List<PolicyRule> policies) {
        log.debug("Loading {} policies into enforcer", policies.size());

        if (policies.isEmpty()) {
            log.info("No policies to load");
            return;
        }

        try {
            // Convert all policies to Casbin format
            String[][] casbinPolicies = policies.stream()
                    .map(PolicyRule::toCasbinPolicy)
                    .toArray(String[][]::new);

            // Add all policies in batch
            boolean success = enforcer.addPolicies(casbinPolicies);

            if (success) {
                log.info("Successfully loaded {} policies into enforcer", policies.size());
            } else {
                log.warn("Some policies may have failed to load or already existed");
            }

        } catch (Exception e) {
            log.error("Failed to load policies into enforcer", e);
            throw new RuntimeException("Policy loading into enforcer failed", e);
        }
    }

    /**
     * Routes policy loading to the appropriate source-specific loader.
     * 
     * @param policyType  the type of policy source (database, resource, api)
     * @param policyQuery the query/path/url for the policy source
     * @param resources   list of resource codes to filter by
     * @return list of loaded policy rules
     * @throws RuntimeException if policy type is unsupported
     */
    private List<PolicyRule> loadPolicyRules(String policyType, String policyQuery, List<String> resources) {
        log.debug("Loading policy rules - Type: {}, Query: {}, Resources: {}", policyType, policyQuery, resources);

        switch (policyType) {
            case "database":
                return databasePolicyLoader.loadPolicyRulesFromDatabase(policyQuery, resources);
            case "resource":
                return resourcePolicyLoader.loadPolicyRulesFromCsv(policyQuery, resources);
            case "api":
                return apiPolicyLoader.loadPolicyRulesFromApi(policyQuery, resources);
            default:
                throw new RuntimeException("Unsupported policy type: " + policyType);
        }
    }
}