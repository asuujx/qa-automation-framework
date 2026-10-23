package io.github.asuujx.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class DashboardPage {

    private final Locator userMenu;

    public DashboardPage(Page page) {
        this.userMenu = page.getByRole(AriaRole.MENU,
                new Page.GetByRoleOptions().setName("Profile and Settings…"));
    }

    public Locator userAvatar(String username) {
        return userMenu.getByRole(AriaRole.IMG,
                new Locator.GetByRoleOptions().setName(username).setExact(true));
    }
}
