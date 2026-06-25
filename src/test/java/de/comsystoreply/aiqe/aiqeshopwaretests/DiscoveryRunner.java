package de.comsystoreply.aiqe.aiqeshopwaretests;

import com.codeborne.selenide.Configuration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.time.Duration;

public class DiscoveryRunner {

    @SuppressWarnings("resource")
    private static final GenericContainer<?> SHOPWARE =
            new GenericContainer<>("dockware/dev:latest")
                    .withExposedPorts(80)
                    .waitingFor(Wait.forLogMessage(".*IS READY.*", 1).withStartupTimeout(Duration.ofMinutes(3)));

    public static void main(final String[] args) {
        SHOPWARE.start();

        final var shopUrl = "http://" + SHOPWARE.getHost() + ":" + SHOPWARE.getMappedPort(80);
        configureSalesChannelDomain(shopUrl);

        Configuration.baseUrl = shopUrl;
        Configuration.browserSize = "1280x800";
        Configuration.headless = true;

        try {
            crawl();
        } finally {
            SHOPWARE.stop();
        }
    }

    private static void configureSalesChannelDomain(final String shopUrl) {
        // TODO: implement in task 2.2
    }

    private static void crawl() {
        // TODO: implement in tasks 2.3–4.2
    }
}
