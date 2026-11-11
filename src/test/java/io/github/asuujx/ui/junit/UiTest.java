package io.github.asuujx.ui.junit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a UI test class: manages the browser through {@link PlaywrightExtension}
 * and tags the tests with "ui", so they can be run on their own ({@code mvn test -Dgroups=ui}).
 * Test methods receive a fresh {@link com.microsoft.playwright.Page} as a parameter.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(PlaywrightExtension.class)
@Tag("ui")
public @interface UiTest {
}
