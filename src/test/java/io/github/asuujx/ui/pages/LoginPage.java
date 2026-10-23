package io.github.asuujx.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Param;
import io.qameta.allure.Step;

import static io.qameta.allure.model.Parameter.Mode.MASKED;

public class LoginPage {

    private static final String PATH = "/user/login";

    private final Page page;
    private final Locator usernameInput;
    private final Locator passwordInput;
    private final Locator signInButton;
    private final Locator errorMessage;

    public LoginPage(Page page) {
        this.page = page;
        this.usernameInput = page.getByLabel("Username or Email Address");
        this.passwordInput = page.getByLabel("Password", new Page.GetByLabelOptions().setExact(true));
        this.signInButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign In"));
        this.errorMessage = page.locator(".flash-error");
    }

    @Step("Open the login page")
    public LoginPage open() {
        page.navigate(PATH);
        return this;
    }

    @Step("Log in as \"{username}\"")
    public DashboardPage loginAs(String username, @Param(mode = MASKED) String password) {
        submitCredentials(username, password);
        return new DashboardPage(page);
    }

    @Step("Attempt to log in as \"{username}\"")
    public LoginPage loginAsExpectingFailure(String username, @Param(mode = MASKED) String password) {
        submitCredentials(username, password);
        return this;
    }

    public Locator errorMessage() {
        return errorMessage;
    }

    private void submitCredentials(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        clickSignIn();
    }

    @Step("Enter username \"{username}\"")
    private void enterUsername(String username) {
        usernameInput.fill(username);
    }

    @Step("Enter password")
    private void enterPassword(@Param(mode = MASKED) String password) {
        passwordInput.fill(password);
    }

    @Step("Click \"Sign In\"")
    private void clickSignIn() {
        signInButton.click();
    }
}
