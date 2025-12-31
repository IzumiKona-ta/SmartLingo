package com.smartlingo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private static final String RESOURCE_PATH = "C:/Users/35742/Desktop/workfile/TrainingWeek/resource/";

    @GetMapping
    public Map<String, Object> listResources() {
        Map<String, Object> response = new HashMap<>();
        try {
            File folder = new File(RESOURCE_PATH);
            List<Map<String, String>> files = new ArrayList<>();
            
            if (folder.exists() && folder.isDirectory()) {
                for (File file : folder.listFiles()) {
                    if (file.isFile() && file.getName().toLowerCase().endsWith(".pdf")) {
                        Map<String, String> fileInfo = new HashMap<>();
                        fileInfo.put("name", file.getName());
                        fileInfo.put("url", "/files/" + file.getName());
                        files.add(fileInfo);
                    }
                }
            }
            
            response.put("success", true);
            response.put("files", files);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }
}
