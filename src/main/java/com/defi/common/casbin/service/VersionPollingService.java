package com.defi.common.casbin.service;

import com.defi.common.casbin.config.CasbinProperties;
import com.defi.common.casbin.util.PolicySourceParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.casbin.jcasbin.main.Enforcer;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service for polling version changes and automatically reloading policies.
 * 
 * <p>
 * This service implements efficient polling strategy to detect policy changes
 * without constantly reloading policies. It caches the current version in
 * memory
 * and only triggers policy reload when version changes are detected.
 * </p>
 * 
 * <p>
 * Key features:
 * </p>
 * <ul>
 * <li>Memory-cached version for fast comparison</li>
 * <li>Configurable polling intervals (minimum 1 minute)</li>
 * <li>Automatic source detection (database vs API)</li>
 * <li>Fail-safe operation (errors don't crash the service)</li>
 * <li>Only applicable to database and API sources</li>
 * </ul>
 * 
 * @author Defi Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VersionPollingService {

    private final CasbinProperties casbinProperties;
    private final PolicyLoader policyLoader;
    private final Enforcer enforcer;
    private final DatabaseVersionChecker databaseVersionChecker;
    private final ApiVersionChecker apiVersionChecker;

    /**
     * Cached version in memory for fast comparison.
     */
    private final AtomicLong cachedVersion = new AtomicLong(0L);

    /**
     * Selected version checker based on policy source type.
     */
    private VersionChecker selectedVersionChecker;

    /**
     * Whether polling is enabled and configured properly.
     */
    private boolean pollingEnabled = false;

    @PostConstruct
    public void initialize() {
        try {
            log.info("Initializing Version Polling Service...");

            // Validate and setup polling configuration
            if (!validatePollingConfiguration()) {
                log.info("Version polling is disabled or not configured");
                return;
            }

            // Setup version checker based on policy source type
            setupVersionChecker();

            if (selectedVersionChecker == null || !selectedVersionChecker.isAvailable()) {
                log.warn("Version checker is not available, polling disabled");
                return;
            }

            pollingEnabled = true;
            log.info("Version polling enabled with {} using checker: {}",
                    casbinProperties.getPolling().getDuration(),
                    selectedVersionChecker.getDescription());

        } catch (Exception e) {
            log.error("Failed to initialize version polling service", e);
        }
    }

    /**
     * Loads initial version after application is fully started.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void loadInitialVersion() {
        if (!pollingEnabled) {
            return;
        }

        try {
            String versionCode = casbinProperties.getPolling().getVersionCode();
            Optional<Long> currentVersion = selectedVersionChecker.getCurrentVersion(versionCode);

            long version = currentVersion.orElse(0L);
            cachedVersion.set(version);

            log.info("Loaded initial version: {} for code: {}", version, versionCode);

        } catch (Exception e) {
            log.error("Failed to load initial version", e);
        }
    }

    /**
     * Scheduled method to check for version changes and reload policies if needed.
     * 
     * <p>
     * This method runs at the configured polling interval and performs
     * the following steps:
     * </p>
     * <ol>
     * <li>Check current version from source</li>
     * <li>Compare with cached version</li>
     * <li>If different, trigger policy reload</li>
     * <li>Update cached version</li>
     * </ol>
     */
    @Scheduled(fixedDelayString = "#{T(java.time.Duration).parse('${app.casbin.polling.duration:PT1H}').toMillis()}")
    public void checkVersionAndReload() {
        if (!pollingEnabled) {
            return;
        }

        try {
            String versionCode = casbinProperties.getPolling().getVersionCode();
            Optional<Long> currentVersionOpt = selectedVersionChecker.getCurrentVersion(versionCode);

            if (currentVersionOpt.isEmpty()) {
                log.debug("Unable to retrieve current version for code: {}", versionCode);
                return;
            }

            long currentVersion = currentVersionOpt.get();
            long cachedVer = cachedVersion.get();

            if (currentVersion != cachedVer) {
                log.info("Version change detected: {} -> {} for code: {}",
                        cachedVer, currentVersion, versionCode);

                // Trigger policy reload
                reloadPolicies();

                // Update cached version
                cachedVersion.set(currentVersion);

                log.info("Policy reload completed, version updated to: {}", currentVersion);
            } else {
                log.debug("No version change detected, current version: {}", currentVersion);
            }

        } catch (Exception e) {
            // Fail-safe: log error but don't crash the polling
            log.error("Error during version checking and policy reload", e);
        }
    }

    /**
     * Gets the current cached version.
     * 
     * @return current cached version number
     */
    public long getCachedVersion() {
        return cachedVersion.get();
    }

    /**
     * Checks if polling is enabled and working.
     * 
     * @return true if polling is operational
     */
    public boolean isPollingEnabled() {
        return pollingEnabled;
    }

    private boolean validatePollingConfiguration() {
        CasbinProperties.PollingConfig polling = casbinProperties.getPolling();

        log.debug("Validating polling configuration - Duration: {}, VersionCode: {}, IsValid: {}",
                polling.getDuration(), polling.getVersionCode(), polling.isValidForPolling());

        if (!polling.isValidForPolling()) {
            log.info("Polling configuration is invalid or disabled");
            return false;
        }

        // Check minimum duration
        if (polling.getDuration().compareTo(CasbinProperties.PollingConfig.getMinimumDuration()) < 0) {
            log.error("Polling duration {} is below minimum required duration of {}",
                    polling.getDuration(), CasbinProperties.PollingConfig.getMinimumDuration());
            return false;
        }

        // Check if policy source supports polling
        String policySource = casbinProperties.getPolicySource();
        if (policySource == null || policySource.trim().isEmpty()) {
            log.warn("Policy source not configured, polling not applicable");
            return false;
        }

        PolicySourceParser.PolicySourceConfig sourceInfo = PolicySourceParser.parse(policySource);
        if ("resource".equals(sourceInfo.getType())) {
            log.info("Policy source is 'resource', polling not applicable for static files");
            return false;
        }

        return true;
    }

    private void setupVersionChecker() {
        String policySource = casbinProperties.getPolicySource();
        PolicySourceParser.PolicySourceConfig sourceInfo = PolicySourceParser.parse(policySource);

        switch (sourceInfo.getType()) {
            case "database":
                selectedVersionChecker = databaseVersionChecker;
                log.info("Using database version checker for policy source: database");
                break;

            case "api":
                setupApiVersionChecker();
                selectedVersionChecker = apiVersionChecker;
                log.info("Using API version checker for policy source: api");
                break;

            default:
                log.warn("Unsupported policy source type for version checking: {}", sourceInfo.getType());
                break;
        }
    }

    private void setupApiVersionChecker() {
        String versionApiEndpoint = casbinProperties.getPolling().getVersionApiEndpoint();
        if (versionApiEndpoint == null || versionApiEndpoint.trim().isEmpty()) {
            log.error("Version API endpoint not configured for API policy source");
            return;
        }

        apiVersionChecker.setVersionApiEndpoint(versionApiEndpoint);
        log.info("Configured API version checker with endpoint: {}", versionApiEndpoint);
    }

    private void reloadPolicies() {
        try {
            log.info("Reloading policies due to version change...");
            policyLoader.loadPolicies(enforcer);
            log.info("Policies reloaded successfully");
        } catch (Exception e) {
            log.error("Failed to reload policies", e);
            throw e; // Re-throw to be caught by the caller
        }
    }
    public void setCachedVersion(long version) {
        cachedVersion.set(version);
        log.info("Cached version updated to: {}", version);
    }
}