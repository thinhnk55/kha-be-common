package com.defi.common.casbin.service;

import com.defi.common.casbin.entity.PolicyRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for loading policies from resource files (CSV format).
 * 
 * <p>
 * This service handles loading policy rules from CSV files located in the
 * classpath.
 * The CSV file should follow Casbin's standard format:
 * {@code p,roleId,resourceCode,actionCode}
 * </p>
 * 
 * @author Defi Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ResourcePolicyLoader {

    private final ResourceLoader resourceLoader;

    /**
     * Loads policy rules from a CSV resource file.
     * 
     * @param resourcePath the path to the CSV resource file (e.g.,
     *                     "casbin/policy.csv")
     * @param resources    list of resource codes to filter by (empty list loads
     *                     all)
     * @return list of policy rules loaded from the CSV file
     * @throws RuntimeException if the file cannot be read or parsed
     */
    public List<PolicyRule> loadPolicyRulesFromCsv(String resourcePath, List<String> resources) {
        log.info("Loading policy rules from resource: {}", resourcePath);

        try {
            // Read CSV content from classpath resource (similar to model.conf loading)
            Resource resource = resourceLoader.getResource("classpath:" + resourcePath);
            String csvContent = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            List<PolicyRule> policies = new ArrayList<>();
            String[] lines = csvContent.split("\n");

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();

                // Skip empty lines and comments
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                // Parse Casbin CSV format: p,roleId,resourceCode,actionCode
                String[] parts = line.split(",");
                if (parts.length >= 4 && "p".equals(parts[0].trim())) {
                    try {
                        Long roleId = Long.parseLong(parts[1].trim());
                        String resourceCode = parts[2].trim();
                        String actionCode = parts[3].trim();

                        // Apply resource filtering if specified
                        if (resources.isEmpty() || resources.contains(resourceCode)) {
                            PolicyRule policyRule = PolicyRule.builder()
                                    .id((long) (i + 1)) // Use line number as ID
                                    .roleId(roleId)
                                    .resourceCode(resourceCode)
                                    .actionCode(actionCode)
                                    .build();
                            policies.add(policyRule);
                        }
                    } catch (NumberFormatException e) {
                        log.warn("Invalid role ID format at line {}: {}", i + 1, line);
                    }
                }
            }

            log.info("Successfully loaded {} policy rules from resource: {}", policies.size(), resourcePath);
            return policies;

        } catch (Exception e) {
            log.error("Failed to load policy rules from resource: {}", resourcePath, e);
            throw new RuntimeException("Resource policy loading failed: " + resourcePath, e);
        }
    }
}