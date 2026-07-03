package org.istiaqfuad.eventhub.common.exception;

import org.istiaqfuad.eventhub.config.WebMvcConfig;
import org.istiaqfuad.eventhub.user.controller.UserController;
import org.istiaqfuad.eventhub.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies the exception pipeline in isolation (no datasource). Drives
 * {@link UserController} with a mocked service so each exception type is
 * mapped to the intended RFC 9457 {@code application/problem+json} response.
 */
@WebMvcTest(UserController.class)
@Import({WebMvcConfig.class, GlobalExceptionHandler.class})
class GlobalExceptionHandlerTest {

    private static final String V1 = "application/vnd.eventhub+json;version=1";

    @Autowired
    MockMvc mvc;

    @MockitoBean
    UserService userService;

    @Test
    void resourceNotFoundMapsTo404ProblemDetail() throws Exception {
        given(userService.get(eq(999L))).willThrow(new ResourceNotFoundException("User", 999L));

        mvc.perform(get("/api/users/999").accept(V1))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.detail").value("User not found: 999"))
                .andExpect(jsonPath("$.instance").value("/api/users/999"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void duplicateResourceMapsTo409() throws Exception {
        given(userService.register(any()))
                .willThrow(new DuplicateResourceException("email already registered: a@b.com"));

        mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON).accept(V1)
                        .content("{\"email\":\"a@b.com\",\"password\":\"password123\"}"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code").value("DUPLICATE_RESOURCE"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void invalidBodyMapsTo400WithFieldErrors() throws Exception {
        mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON).accept(V1)
                        .content("{\"email\":\"not-an-email\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[*].field").value(org.hamcrest.Matchers.hasItem("email")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void typeMismatchMapsTo400ProblemDetailWithTimestamp() throws Exception {
        mvc.perform(get("/api/users/not-a-number").accept(V1))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
