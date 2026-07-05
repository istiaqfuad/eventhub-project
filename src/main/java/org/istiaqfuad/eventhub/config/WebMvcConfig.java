package org.istiaqfuad.eventhub.config;

import org.istiaqfuad.eventhub.security.web.CurrentUserArgumentResolver;
import org.istiaqfuad.eventhub.security.web.CurrentUserIdArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Web MVC configuration:
 * <ul>
 *   <li>Prefixes every {@code @RestController} with {@code /api}, so controllers
 *       declare paths without it (e.g. {@code @RequestMapping("/users")}).</li>
 *   <li>Enables media-type API versioning via a parameter on the vendor media
 *       type ({@code Accept: application/vnd.eventhub+json;version=1}) using
 *       Spring's built-in resolver — no custom code.</li>
 * </ul>
 *
 * <p>Deliberately does NOT use {@code @EnableWebMvc}, which would switch off
 * Spring Boot's MVC autoconfiguration.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final MediaType VENDOR_JSON = MediaType.parseMediaType("application/vnd.eventhub+json");

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix("/api", HandlerTypePredicate.forAnnotation(RestController.class));
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new CurrentUserIdArgumentResolver());
        resolvers.add(new CurrentUserArgumentResolver());
    }

    @Override
    public void configureApiVersioning(ApiVersionConfigurer configurer) {
        configurer.useMediaTypeParameter(VENDOR_JSON, "version")
                .addSupportedVersions("1", "2")   // "2" backs the demo endpoint
                .setDefaultVersion("1")
                .setVersionRequired(false);        // lenient: missing/unknown → v1
    }
}
