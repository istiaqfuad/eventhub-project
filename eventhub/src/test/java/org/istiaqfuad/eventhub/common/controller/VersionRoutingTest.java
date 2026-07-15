package org.istiaqfuad.eventhub.common.controller;

import org.istiaqfuad.eventhub.config.WebMvcConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies vendor media-type version routing in isolation (no datasource).
 * Proves the {@link WebMvcConfig} api-versioning wiring + custom resolver.
 */
@WebMvcTest(VersionController.class)
@Import(WebMvcConfig.class)
class VersionRoutingTest {

    @Autowired
    MockMvc mvc;

    @Test
    void v1MediaTypeRoutesToV1Handler() throws Exception {
        mvc.perform(get("/api/version").accept("application/vnd.eventhub+json;version=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value("1"));
    }

    @Test
    void v2MediaTypeRoutesToV2Handler() throws Exception {
        mvc.perform(get("/api/version").accept("application/vnd.eventhub+json;version=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value("2"));
    }

    @Test
    void missingVersionFallsBackToDefaultV1() throws Exception {
        mvc.perform(get("/api/version"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value("1"));
    }

    @Test
    void unsupportedVersionIsRejected() throws Exception {
        mvc.perform(get("/api/version").accept("application/vnd.eventhub+json;version=3"))
                .andExpect(status().isBadRequest());
    }
}
