package org.istiaqfuad.eventhub.payment;

import com.stripe.StripeClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Binds {@link StripeProperties} and exposes a single {@link StripeClient} built from the
 * configured secret key (a restricted key in practice). Using a client instance rather than
 * the global {@code Stripe.apiKey} keeps the key out of static state.
 */
@Configuration
@EnableConfigurationProperties(StripeProperties.class)
public class StripeConfig {

    @Bean
    public StripeClient stripeClient(StripeProperties properties) {
        return new StripeClient(properties.secretKey());
    }
}
