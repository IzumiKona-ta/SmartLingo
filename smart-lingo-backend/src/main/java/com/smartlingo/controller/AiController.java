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
                aiRequest.put("stream", true); // 尝试开启流式模式

                RequestCallback requestCallback = requestEntity -> {
                    new ObjectMapper().writeValue(requestEntity.getBody(), aiRequest);
                    requestEntity.getHeaders().add("Content-Type", "application/json");
                };

                ResponseExtractor<Void> responseExtractor = response -> {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getBody()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            // 尝试解析各种可能的流式格式
                            // 1. 如果是 data: 开头的 SSE 格式
                            if (line.startsWith("data:")) {
                                String data = line.substring(5).trim();
                                if ("[DONE]".equals(data)) break;
                                try {
                                    // 尝试作为 JSON 解析
                                    Map map = objectMapper.readValue(data, Map.class);
                                    // 提取内容字段，常见字段名: content, reply, response, delta
                                    String content = null;
                                    if (map.containsKey("reply")) content = (String) map.get("reply");
                                    else if (map.containsKey("response")) content = (String) map.get("response");
                                    else if (map.containsKey("content")) content = (String) map.get("content");
                                    
                                    // OpenAI 格式: choices[0].delta.content
                                    if (content == null && map.containsKey("choices")) {
                                        // 简化处理，略过复杂结构解析，视具体 API 而定
                                    }

                                    if (content != null) {
                                        emitter.send(content);
                                    } else {
                                        // 如果无法提取，直接发送原始数据作为 fallback (或者为了调试)
                                        // emitter.send(data); 
                                    }
                                } catch (Exception e) {
                                    // 不是 JSON，可能是纯文本，直接发
                                    emitter.send(data);
                                }
                            } else if (!line.trim().isEmpty()) {
                                // 2. 可能是纯文本流或非标准格式
                                // 尝试解析 JSON
                                try {
                                     Map map = objectMapper.readValue(line, Map.class);
                                     String content = null;
                                     if (map.containsKey("reply")) content = (String) map.get("reply");
                                     else if (map.containsKey("response")) content = (String) map.get("response");
                                     
                                     if (content != null) emitter.send(content);
                                } catch (Exception e) {
                                    // 纯文本
                                    emitter.send(line);
                                }
                            }
                        }
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }
                    return null;
                };

                // 用户指示端口已改为 8002
                restTemplate.execute("http://10.138.50.151:8002/agent/chat", HttpMethod.POST, requestCallback, responseExtractor);
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
