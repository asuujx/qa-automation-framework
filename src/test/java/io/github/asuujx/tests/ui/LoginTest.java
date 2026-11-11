package io.github.asuujx.tests.ui;

import com.microsoft.playwright.Page;
import io.github.asuujx.config.Config;
import io.github.asuujx.ui.junit.UiTest;
import io.github.asuujx.ui.pages.DashboardPage;
import io.github.asuujx.ui.pages.LoginPage;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static io.qameta.allure.Allure.step;

@UiTest
@Feature("Login")
@DisplayName("Login")
class LoginTest {

    @Test
    @DisplayName("Successful login with valid credentials")
    @Severity(SeverityLevel.BLOCKER)
    void shouldShowUserAvatarAfterValidLogin(Page page) {
        DashboardPage dashboard = new LoginPage(page)
                .open()
                .loginAs(Config.adminUsername(), Config.adminPassword());

        step("Verify the user's avatar is shown in the navbar", () ->
                assertThat(dashboard.userAvatar(Config.adminUsername())).isVisible());
    }

    @Test
    @DisplayName("Login fails with a wrong password")
    @Severity(SeverityLevel.CRITICAL)
    void shouldShowErrorMessageForWrongPassword(Page page) {
        LoginPage loginPage = new LoginPage(page)
                .open()
                .loginAsExpectingFailure(Config.adminUsername(), "wrong-password");

        step("Verify the \"incorrect credentials\" error is shown", () ->
                assertThat(loginPage.errorMessage()).hasText("Username or password is incorrect."));
    }
}
