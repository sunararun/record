package com.record.java.controller;

import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api/version")
public class VersionController {

    private final BuildProperties buildProperties;

    public VersionController(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @GetMapping("/getVersion")
    public String getVersion() {
        return buildProperties.getVersion(); // Returns "0.0.1-SNAPSHOT"
    }
}
