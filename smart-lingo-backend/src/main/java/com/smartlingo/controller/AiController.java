package com.smartlingo.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @PostMapping("/chat")
    public Map<String, String> chat(@RequestBody Map<String, String> request) {
        Map<String, String> response = new HashMap<>();
        // 支持前端发送 message 或 text 字段
        String userMessage = request.get("message");
        if (userMessage == null) {
            userMessage = request.get("text");
        }
        
        try {
            RestTemplate restTemplate = new RestTemplate();
            // 设置较短的连接超时，避免前端长时间等待
            ((SimpleClientHttpRequestFactory)restTemplate.getRequestFactory()).setConnectTimeout(2000);
            ((SimpleClientHttpRequestFactory)restTemplate.getRequestFactory()).setReadTimeout(5000);
            
            // 构建真实请求
            Map<String, String> aiRequest = new HashMap<>();
            aiRequest.put("prompt", userMessage); 
            // 如果对方API需要 standard OpenAI format (messages array), 需要调整这里
            // 目前假设对方接口接受 {"prompt": "..."}
            
            // 用户指示端口已改为 8002
            ResponseEntity<Map> aiResponse = restTemplate.postForEntity("http://10.138.50.151:8002/agent/chat", aiRequest, Map.class);
            
            if (aiResponse.getBody() != null) {
                // 尝试适配多种可能的返回字段
                if (aiResponse.getBody().containsKey("reply")) {
                    response.put("reply", aiResponse.getBody().get("reply").toString());
                } else if (aiResponse.getBody().containsKey("response")) {
                    response.put("reply", aiResponse.getBody().get("response").toString());
                } else {
                    response.put("reply", aiResponse.getBody().toString());
                }
                return response;
            }
            
            throw new RuntimeException("Empty response from AI server");
            
        } catch (Exception e) {
            // 用户明确要求不要"假页面"，因此这里返回真实的错误信息
            System.err.println("AI Connection Error: " + e.getMessage());
            response.put("reply", "【系统提示】连接 AI 服务失败: " + e.getMessage() + "\n请检查目标服务器 (10.138.50.151:8002) 是否开启，或网络是否通畅。");
            return response;
        }
    }
}
