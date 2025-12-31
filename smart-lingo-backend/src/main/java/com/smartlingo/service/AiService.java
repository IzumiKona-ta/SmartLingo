package com.smartlingo.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AiService {

    private static final String AI_API_URL = "http://10.138.50.151:8001/agent/chat";

    public String chat(String text, String imageBase64) {
        String prompt = text;
        if (imageBase64 != null && !imageBase64.isEmpty()) {
            String ocrText = mockOcr(imageBase64);
            prompt = "Context from image: " + ocrText + "\n\nUser Question: " + text;
        }

        // Call Python API
        // Assuming the API expects JSON like { "query": "..." } or similar. 
        // The user said "standard JSON QA", let's assume { "query": "..." } for now or adjust if I knew the schema.
        // User prompt: "support standard JSON QA".
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("query", prompt);
        payload.put("stream", false); 

        try {
            String result = HttpRequest.post(AI_API_URL)
                    .body(JSONUtil.toJsonStr(payload))
                    .timeout(20000)
                    .execute()
                    .body();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"reply\": \"Error connecting to AI service.\"}";
        }
    }

    private String mockOcr(String imageBase64) {
        // Placeholder for Baidu OCR
        return " [OCR Result: This is a sample extracted text from the image] ";
    }
}
