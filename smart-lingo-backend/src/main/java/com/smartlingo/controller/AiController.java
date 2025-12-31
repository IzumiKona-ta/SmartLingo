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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/chat")
    public SseEmitter chat(@RequestBody Map<String, String> request) {
        // 设置较长的超时时间 (5分钟)，保证长对话不断开
        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L);
        
        // 支持前端发送 message 或 text 字段
        String userMessage = request.get("message");
        if (userMessage == null) {
            userMessage = request.get("text");
        }
        final String finalUserMessage = userMessage;

        executor.execute(() -> {
            try {
                RestTemplate restTemplate = new RestTemplate();
                // 设置连接和读取超时
                ((SimpleClientHttpRequestFactory)restTemplate.getRequestFactory()).setConnectTimeout(5000);
                ((SimpleClientHttpRequestFactory)restTemplate.getRequestFactory()).setReadTimeout(60000 * 5); // 5分钟读取超时
                
                // 构建真实请求
                Map<String, Object> aiRequest = new HashMap<>();
                aiRequest.put("prompt", finalUserMessage);
                // aiRequest.put("stream", true); // 远程服务流式暂不可用，回退到普通模式
                
                // ... (rest of the code)

                // 用户指示端口已改为 8002
                // 使用 postForObject 以非流式方式获取完整响应
                Map aiResponse = restTemplate.postForObject("http://10.138.50.151:8002/agent/chat", aiRequest, Map.class);
                
                if (aiResponse != null) {
                    String content = null;
                    if (aiResponse.containsKey("reply")) content = (String) aiResponse.get("reply");
                    else if (aiResponse.containsKey("response")) content = (String) aiResponse.get("response");
                    else if (aiResponse.containsKey("content")) content = (String) aiResponse.get("content");
                    
                    if (content != null) {
                        // 模拟流式打字机效果，并解决换行符截断问题
                        // 将内容按小块发送，并封装为 JSON 避免 SSE 格式问题
                        int chunkSize = 5; // 每次发送 5 个字符
                        for (int i = 0; i < content.length(); i += chunkSize) {
                            int end = Math.min(content.length(), i + chunkSize);
                            String chunk = content.substring(i, end);
                            
                            Map<String, String> data = new HashMap<>();
                            data.put("chunk", chunk);
                            
                            // Spring 会自动将 Map 转为 JSON 字符串发送: data:{"chunk":"..."}
                            emitter.send(data, MediaType.APPLICATION_JSON);
                            
                            // 稍微延时，制造打字机效果
                            try {
                                Thread.sleep(20);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                        }
                    } else {
                        emitter.send(aiResponse.toString());
                    }
                }
                emitter.complete();

            } catch (Exception e) {
                System.err.println("AI Connection Error: " + e.getMessage());
                try {
                    emitter.send("【系统提示】连接 AI 服务失败: " + e.getMessage());
                    emitter.complete();
                } catch (Exception ex) {
                    // ignore
                }
            }
        });

        return emitter;
    }
}
