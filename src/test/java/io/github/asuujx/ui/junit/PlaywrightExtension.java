package io.github.asuujx.ui.junit;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import io.github.asuujx.config.Config;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * Manages the Playwright lifecycle for UI tests and injects a {@link Page} into test methods.
 *
 * <ul>
 *   <li>Playwright and Browser: one per test class, started before the first test, closed after the last.</li>
 *   <li>BrowserContext and Page: one per test, so every test starts with clean cookies and storage.</li>
 * </ul>
 *
 * Every test is traced. When a test fails, the page URL, a screenshot and the trace are attached
 * to the Allure report (see {@link FailureArtifacts}); when it passes, the trace is discarded.
 *
 * Everything is kept in the JUnit {@link ExtensionContext.Store}, which closes {@link AutoCloseable}
 * values when their scope ends. Use it through {@link UiTest}.
 */
public class PlaywrightExtension implements BeforeAllCallback, ParameterResolver, AfterTestExecutionCallback {

    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(PlaywrightExtension.class);

    @Override
    public void beforeAll(ExtensionContext classContext) {
        // A @Nested class finds the parent's session in the store and reuses it
        classContext.getStore(NAMESPACE).computeIfAbsent(BrowserSession.class, key -> BrowserSession.start(),
                BrowserSession.class);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType() == Page.class;
    }

    @Override
    public Page resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        ExtensionContext.Store store = extensionContext.getStore(NAMESPACE);
        BrowserSession session = store.get(BrowserSession.class, BrowserSession.class);
        // Created on first use per test; stored at test level, so it is closed when the test ends
        TestPage testPage = store.computeIfAbsent(TestPage.class, key -> TestPage.open(session.browser()),
                TestPage.class);
        return testPage.page();
    }

    /**
     * Runs right after the test method, before @AfterEach methods and before the page is closed,
     * so the page is still in the state the test left it in.
     */
    @Override
    public void afterTestExecution(ExtensionContext testContext) {
        TestPage testPage = testContext.getStore(NAMESPACE).get(TestPage.class, TestPage.class);
        if (testPage == null) {
            return; // the test didn't use a browser
        }
        if (testContext.getExecutionException().isPresent()) {
            FailureArtifacts.attach(testPage.context(), testPage.page(), testName(testContext));
        } else {
            testPage.context().tracing().stop();
        }
    }

    private static String testName(ExtensionContext testContext) {
        return testContext.getRequiredTestClass().getSimpleName() + "." + testContext.getRequiredTestMethod().getName();
    }

    private record BrowserSession(Playwright playwright, Browser browser) implements AutoCloseable {

        static BrowserSession start() {
            Playwright playwright = Playwright.create();
            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(Config.headless()));
            return new BrowserSession(playwright, browser);
        }

        @Override
        public void close() {
            // Close the browser explicitly: playwright.close() alone stops the driver
            // without a graceful browser shutdown (and browser.isConnected() stays true)
            browser.close();
            playwright.close();
        }
    }

    private record TestPage(BrowserContext context, Page page) implements AutoCloseable {

        static TestPage open(Browser browser) {
            BrowserContext context = browser.newContext(
                    new Browser.NewContextOptions().setBaseURL(Config.baseUrl()));
            // Screenshots + DOM snapshots let the trace viewer replay every step of the test
            context.tracing().start(new Tracing.StartOptions().setScreenshots(true).setSnapshots(true));
            return new TestPage(context, context.newPage());
        }

        @Override
        public void close() {
            // Closing the context also closes its pages
            context.close();
        }
    }
}
