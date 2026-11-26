package com.record.java.controller;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api/version")
public class VersionController {

    private final BuildProperties buildProperties;

    public VersionController(ObjectProvider<BuildProperties> buildProperties) {
        this.buildProperties = buildProperties.getIfAvailable();
    }

    @GetMapping("/getVersion")
    public String getVersion() {
        return buildProperties.getVersion(); // Returns "0.0.1-SNAPSHOT"
    }
}
