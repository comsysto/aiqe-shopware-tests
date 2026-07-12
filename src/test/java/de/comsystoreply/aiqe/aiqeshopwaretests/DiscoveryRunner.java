package de.comsystoreply.aiqe.aiqeshopwaretests;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import com.google.gson.GsonBuilder;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;

public class DiscoveryRunner {

    private static final Path OUTPUT_DIR = Path.of("build/discovery");
    private static final Set<String> USED_SLUGS = new HashSet<>();

    @SuppressWarnings("unchecked")
    enum ActionType {
        OPEN("open", value -> open((String) value)),
        CLICK("click", value -> $((String) value).click()),
        FILL("fill", value -> {
            final var params = (Map<String, String>) value;
            $(params.get("selector")).setValue(params.get("value"));
        }),
        SNAPSHOT("snapshot", value -> {
            final var params = (Map<String, Object>) value;
            final var name = (String) params.get("name");
            final var authRequired = Boolean.TRUE.equals(params.getOrDefault("auth_required", false));
            DiscoveryRunner.takeSnapshot(name, authRequired);
        }),
        WAIT("wait", value -> {
            try {
                Thread.sleep(((Number) value).longValue());
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        final String key;
        private final Consumer<Object> action;

        ActionType(final String key, final Consumer<Object> action) {
            this.key = key;
            this.action = action;
        }

        void execute(final Object value) {
            action.accept(value);
        }
    }

    record Step(ActionType type, Object value) {

        static Step from(final Map<String, Object> raw) {
            for (final var actionType : ActionType.values()) {
                if (raw.containsKey(actionType.key)) {
                    return new Step(actionType, raw.get(actionType.key));
                }
            }
            final var unknownKey = raw.keySet().iterator().next();
            System.err.println("Unknown step type: '" + unknownKey + "'. Supported: open, click, fill, snapshot, wait");
            System.exit(1);
            throw new AssertionError("unreachable");
        }

        void execute() {
            type.execute(value);
        }
    }

    static void main(final String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: DiscoveryRunner <script-path>");
            System.err.println("Example: ./gradlew discover -Pargs=discovery-scripts/bootstrap.yml");
            System.exit(1);
        }

        final var scriptPath = Path.of(args[0]);
        if (!Files.exists(scriptPath)) {
            System.err.println("Script file not found: " + scriptPath);
            System.exit(1);
        }

        Configuration.baseUrl = DockwareContainer.start();
        Configuration.browserSize = "1280x800";
        Configuration.headless = true;

        try {
            executeScript(scriptPath);
        } finally {
            DockwareContainer.stop();
        }
    }

    @SuppressWarnings("unchecked")
    private static void executeScript(final Path scriptPath) {
        final Map<String, Object> script;
        try (final InputStream in = Files.newInputStream(scriptPath)) {
            script = new Yaml().load(in);
        } catch (final IOException e) {
            throw new RuntimeException("Failed to read script: " + scriptPath, e);
        }

        final var steps = (List<Map<String, Object>>) script.get("steps");
        if (steps == null || steps.isEmpty()) {
            System.out.println("No steps found in script, nothing to do.");
            return;
        }

        steps.stream()
                .map(Step::from)
                .forEach(Step::execute);
    }

    private static void takeSnapshot(final String name, final boolean authRequired) {
        final var url = WebDriverRunner.getWebDriver().getCurrentUrl();
        final var slug = toSlug(name);
        final var elements = extractElements();
        writeJson(url, authRequired, elements, slug, name);
        writeScreenshot(slug);
    }

    private static void writeJson(final String url, final boolean authRequired,
                                  final Map<String, List<String>> elements, final String slug,
                                  final String journeyHint) {
        final var snapshot = Map.of(
                "url", url,
                "title", Objects.requireNonNull(Selenide.title()),
                "journey_hint", journeyHint,
                "auth_required", authRequired,
                "elements", elements
        );

        final var json = new GsonBuilder().setPrettyPrinting().create().toJson(snapshot);

        try {
            Files.createDirectories(OUTPUT_DIR);
            Files.writeString(OUTPUT_DIR.resolve(slug + ".json"), json);
        } catch (final IOException e) {
            throw new RuntimeException("Failed to write snapshot JSON for " + url, e);
        }
    }

    private static void writeScreenshot(final String slug) {
        final var src = ((TakesScreenshot) WebDriverRunner.getWebDriver()).getScreenshotAs(OutputType.FILE);
        try {
            Files.createDirectories(OUTPUT_DIR);
            Files.copy(src.toPath(), OUTPUT_DIR.resolve(slug + ".png"), StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException e) {
            throw new RuntimeException("Failed to write screenshot for " + slug, e);
        }
    }

    static String toSlug(final String name) {
        var base = name
                .toLowerCase()
                .replaceAll("[^a-z0-9-]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        if (base.isBlank()) {
            base = "snapshot";
        }

        var slug = base;
        var counter = 2;
        while (USED_SLUGS.contains(slug)) {
            slug = base + "-" + counter++;
        }
        USED_SLUGS.add(slug);
        return slug;
    }

    private static Map<String, List<String>> extractElements() {
        return Map.of(
                "navLinks", collectTexts("nav a"),
                "buttons", collectTexts("button, input[type='submit']"),
                "formFields", collectAttributes("input:not([type='hidden']), select, textarea", "name"),
                "headings", collectTexts("h1, h2, h3")
        );
    }

    private static List<String> collectTexts(final String selector) {
        return $$(selector).asFixedIterable().stream()
                .map(el -> el.getText().strip())
                .filter(text -> !text.isBlank())
                .distinct()
                .toList();
    }

    private static List<String> collectAttributes(final String selector, final String attribute) {
        return $$(selector).asFixedIterable().stream()
                .map(el -> el.getAttribute(attribute))
                .filter(val -> val != null && !val.isBlank())
                .distinct()
                .toList();
    }
}
