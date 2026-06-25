package de.comsystoreply.aiqe.aiqeshopwaretests;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.time.Duration;

public class DockwareContainer {

    @SuppressWarnings("resource")
    private static final GenericContainer<?> CONTAINER =
            new GenericContainer<>("dockware/dev:latest")
                    .withExposedPorts(80)
                    .waitingFor(Wait.forLogMessage(".*IS READY.*", 1).withStartupTimeout(Duration.ofMinutes(3)));

    /**
     * Starts the Shopware container and patches the sales channel domain to match the
     * dynamic port assigned by Testcontainers.
     *
     * {@return the base URL at which the storefront is reachable}
     */
    public static String start() {
        CONTAINER.start();
        final var shopUrl = "http://" + CONTAINER.getHost() + ":" + CONTAINER.getMappedPort(80);
        configureSalesChannelDomain(shopUrl);
        return shopUrl;
    }

    public static void stop() {
        CONTAINER.stop();
    }

    private static void configureSalesChannelDomain(final String shopUrl) {
        try {
            final var sqlResult = CONTAINER.execInContainer(
                    "mysql", "-h", "127.0.0.1", "-u", "root", "-proot", "shopware", "-e",
                    "UPDATE sales_channel_domain SET url = '" + shopUrl + "' WHERE url = 'http://localhost';"
            );
            if (sqlResult.getExitCode() != 0) {
                throw new RuntimeException("SQL update failed: " + sqlResult.getStderr());
            }
            final var cacheResult = CONTAINER.execInContainer(
                    "php", "/var/www/html/bin/console", "cache:clear"
            );
            if (cacheResult.getExitCode() != 0) {
                throw new RuntimeException("Cache clear failed: " + cacheResult.getStderr());
            }
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new RuntimeException("Failed to configure Shopware sales channel domain", e);
        }
    }
}
