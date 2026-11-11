package io.github.asuujx.ui.junit;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Tracing;
import io.qameta.allure.Allure;
import io.qameta.allure.AttachmentOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Collects debugging artifacts for a failed UI test and attaches them to the Allure report:
 * the page URL, a full-page screenshot and the Playwright trace.
 *
 * <p>Each artifact is collected independently, and errors are logged rather than thrown:
 * a broken page (e.g. a crashed tab) must never hide the original test failure.
 */
final class FailureArtifacts {

    static final Path TRACES_DIR = Path.of("target", "playwright-traces");

    private static final Logger LOG = LoggerFactory.getLogger(FailureArtifacts.class);

    private FailureArtifacts() {
    }

    static void attach(BrowserContext context, Page page, String testName) {
        attachUrl(page);
        attachScreenshot(page);
        attachTrace(context, testName);
    }

    private static void attachUrl(Page page) {
        try {
            byte[] url = page.url().getBytes(StandardCharsets.UTF_8);
            addAttachment("Page URL", "text/uri-list", new ByteArrayInputStream(url), "uri");
        } catch (RuntimeException e) {
            LOG.warn("Could not attach the page URL", e);
        }
    }

    private static void attachScreenshot(Page page) {
        try {
            byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
            addAttachment("Screenshot", "image/png", new ByteArrayInputStream(screenshot), "png");
        } catch (RuntimeException e) {
            LOG.warn("Could not take a screenshot", e);
        }
    }

    private static void attachTrace(BrowserContext context, String testName) {
        Path tracePath = TRACES_DIR.resolve(testName + ".zip");
        try {
            context.tracing().stop(new Tracing.StopOptions().setPath(tracePath));
            try (InputStream trace = Files.newInputStream(tracePath)) {
                addAttachment("Playwright trace", "application/zip", trace, "zip");
            }
        } catch (RuntimeException | IOException e) {
            LOG.warn("Could not save the Playwright trace", e);
        }
    }

    // Allure.attachment(...) wraps each attachment in its own step; the lifecycle API
    // attaches it to the test itself, so the report doesn't show fake "steps"
    private static void addAttachment(String name, String type, InputStream content, String extension) {
        Allure.getLifecycle().addAttachment(name, type, content, AttachmentOptions.withFileExtension(extension));
    }
}
