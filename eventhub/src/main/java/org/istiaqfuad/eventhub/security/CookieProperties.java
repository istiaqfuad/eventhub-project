package org.istiaqfuad.eventhub.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Refresh-cookie attributes. {@code secure} is true in real environments; set false
 * for plain-HTTP local dev. {@code refreshName} is the cookie name.
 */
@ConfigurationProperties(prefix = "app.security.cookie")
public record CookieProperties(boolean secure, String refreshName) {
}
