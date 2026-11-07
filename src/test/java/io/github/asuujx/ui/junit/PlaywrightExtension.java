package io.github.asuujx.ui.junit;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import io.github.asuujx.config.Config;
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
 * Everything is kept in the JUnit {@link ExtensionContext.Store}, which closes {@link AutoCloseable}
 * values when their scope ends. Use it through {@link UiTest}.
 */
public class PlaywrightExtension implements BeforeAllCallback, ParameterResolver {

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
            return new TestPage(context, context.newPage());
        }

        @Override
        public void close() {
            // Closing the context also closes its pages
            context.close();
        }
    }
}
