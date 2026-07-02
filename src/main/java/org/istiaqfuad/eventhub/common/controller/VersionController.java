package org.istiaqfuad.eventhub.common.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Demo endpoint proving media-type version routing without any DTO.
 * {@code Accept: application/vnd.eventhub+json;version=1} → v1 handler,
 * {@code ;version=2} → v2 handler, absent → v1 (default). Remove once real
 * versioned endpoints exist.
 */
@RestController
@RequestMapping("/version")
public class VersionController {

    @GetMapping(version = "1")
    public Map<String, String> v1() {
        return Map.of("version", "1");
    }

    @GetMapping(version = "2")
    public Map<String, String> v2() {
        return Map.of("version", "2");
    }
}
