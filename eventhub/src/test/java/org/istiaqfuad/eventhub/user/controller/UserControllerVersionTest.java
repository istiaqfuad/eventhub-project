package org.istiaqfuad.eventhub.user.controller;

import org.istiaqfuad.eventhub.config.WebMvcConfig;
import org.istiaqfuad.eventhub.security.jwt.JwtAuthenticationToken;
import org.istiaqfuad.eventhub.user.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Confirms the type-level {@code @RequestMapping(version = "1")} on a real
 * feature controller is honored by the api-versioning machinery (no datasource
 * — UserService is mocked). {@code GET /users/{id}} now takes a {@code @CurrentUser}
 * argument, so an authenticated caller (id 1, reading their own profile) is placed
 * in the security context for each request.
 */
@WebMvcTest(UserController.class)
@Import(WebMvcConfig.class)
class UserControllerVersionTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    UserService userService;

    @BeforeEach
    void authenticateAsUser1() {
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(JwtAuthenticationToken.authenticated(1L, AuthorityUtils.NO_AUTHORITIES));
        SecurityContextHolder.setContext(ctx);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void v1MediaTypeIsAccepted() throws Exception {
        mvc.perform(get("/api/users/1").accept("application/vnd.eventhub+json;version=1"))
                .andExpect(status().isOk());
    }

    @Test
    void missingVersionFallsBackToDefaultV1() throws Exception {
        mvc.perform(get("/api/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    void unsupportedVersionIsRejected() throws Exception {
        mvc.perform(get("/api/users/1").accept("application/vnd.eventhub+json;version=3"))
                .andExpect(status().isBadRequest());
    }
}
