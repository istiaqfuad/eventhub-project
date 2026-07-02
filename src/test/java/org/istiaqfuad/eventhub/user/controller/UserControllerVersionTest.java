package org.istiaqfuad.eventhub.user.controller;

import org.istiaqfuad.eventhub.config.WebMvcConfig;
import org.istiaqfuad.eventhub.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Confirms the type-level {@code @RequestMapping(version = "1")} on a real
 * feature controller is honored by the api-versioning machinery (no datasource
 * — UserController is a dependency-free stub).
 */
@WebMvcTest(UserController.class)
@Import(WebMvcConfig.class)
class UserControllerVersionTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    UserService userService;

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
