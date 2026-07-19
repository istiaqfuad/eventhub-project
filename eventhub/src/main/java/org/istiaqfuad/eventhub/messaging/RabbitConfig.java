package org.istiaqfuad.eventhub.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declares the EventHub topic exchange and all durable consumer queues.
 *
 * <p>Topology:
 * <pre>
 *   eventhub.exchange (topic)
 *       BookingConfirmed  ──► analytics.update
 *       BookingConfirmed  ──► (future: ticket.generate, email.send, …)
 * </pre>
 *
 * <p>All queues are durable so they survive broker restarts.
 * Add new queues + bindings here as new consumers are built.
 */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "eventhub.exchange";
    public static final String ROUTING_BOOKING_CONFIRMED = "BookingConfirmed";

    // --- Queue names ---
    public static final String QUEUE_ANALYTICS = "analytics.update";

    @Bean
    public TopicExchange eventHubExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    // ── Analytics queue ────────────────────────────────────────────────────────

    @Bean
    public Queue analyticsQueue() {
        return QueueBuilder.durable(QUEUE_ANALYTICS).build();
    }

    @Bean
    public Binding analyticsBinding() {
        return BindingBuilder.bind(analyticsQueue())
                .to(eventHubExchange())
                .with(ROUTING_BOOKING_CONFIRMED);
    }

    /**
     * Jackson-based converter so consumers receive typed Maps without manual parsing.
     * Passing the application ObjectMapper ensures consistent date/enum handling.
     */
    @Bean
    public MessageConverter messageConverter() {
        // Jackson2JsonMessageConverter is the stable Jackson-2-based converter.
        // The deprecation note signals a future migration to Jackson 3 (JacksonJsonMessageConverter),
        // but it remains fully functional in Spring AMQP 3.x with the bundled Jackson 2.
        @SuppressWarnings("deprecation")
        var converter = new org.springframework.amqp.support.converter.Jackson2JsonMessageConverter();
        return converter;
    }
}
