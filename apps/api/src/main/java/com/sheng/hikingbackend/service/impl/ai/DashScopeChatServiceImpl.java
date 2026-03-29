package com.sheng.hikingbackend.service.impl.ai;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sheng.hikingbackend.common.exception.BusinessException;
import com.sheng.hikingbackend.config.AiProperties;
import com.sheng.hikingbackend.service.DashScopeChatService;
import com.sheng.hikingbackend.service.impl.ai.model.DashScopeMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashScopeChatServiceImpl implements DashScopeChatService {

    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;

    @Override
    public String completeJson(List<DashScopeMessage> messages) {
        ensureConfigured();
        try {
            HttpClient client = buildClient();
            JsonNode requestBody = objectMapper.createObjectNode()
                    .put("model", aiProperties.getModel())
                    .set("messages", objectMapper.valueToTree(messages));
            ((com.fasterxml.jackson.databind.node.ObjectNode) requestBody)
                    .set("response_format", objectMapper.createObjectNode().put("type", "json_object"));
            ((com.fasterxml.jackson.databind.node.ObjectNode) requestBody).put("stream", false);

            HttpRequest request = baseRequest()
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                    .build();

            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            String body = readResponseBody(response);
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw BusinessException.badRequest("AI_PROVIDER_ERROR", "千问请求失败：" + summarizeProviderError(body));
            }
            JsonNode root = objectMapper.readTree(body);
            return root.path("choices").path(0).path("message").path("content").asText("");
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("DashScope completeJson failed", ex);
            throw BusinessException.badRequest("AI_PROVIDER_ERROR", "千问服务暂时不可用，请稍后重试");
        }
    }

    @Override
    public String streamCompletion(List<DashScopeMessage> messages, Consumer<String> onDelta) {
        ensureConfigured();
        StringBuilder answer = new StringBuilder();
        try {
            HttpClient client = buildClient();
            JsonNode requestBody = objectMapper.createObjectNode()
                    .put("model", aiProperties.getModel())
                    .put("stream", true)
                    .set("messages", objectMapper.valueToTree(messages));

            HttpRequest request = baseRequest()
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                    .build();

            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                String body = readResponseBody(response);
                throw BusinessException.badRequest("AI_PROVIDER_ERROR", "千问流式响应失败：" + summarizeProviderError(body));
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(openBodyStream(response), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("data:")) {
                        continue;
                    }
                    String payload = line.substring(5).trim();
                    if (!StringUtils.hasText(payload)) {
                        continue;
                    }
                    if ("[DONE]".equals(payload)) {
                        break;
                    }
                    JsonNode node = objectMapper.readTree(payload);
                    JsonNode deltaNode = node.path("choices").path(0).path("delta").path("content");
                    if (deltaNode.isMissingNode() || deltaNode.isNull()) {
                        continue;
                    }
                    String delta = deltaNode.isTextual() ? deltaNode.asText() : extractContentText(deltaNode);
                    if (!StringUtils.hasText(delta)) {
                        continue;
                    }
                    answer.append(delta);
                    onDelta.accept(delta);
                }
            }
            return answer.toString();
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("DashScope streamCompletion failed", ex);
            throw BusinessException.badRequest("AI_PROVIDER_ERROR", "千问流式响应失败，请稍后重试");
        }
    }

    private HttpClient buildClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(aiProperties.getConnectTimeoutMs()))
                .build();
    }

    private HttpRequest.Builder baseRequest() {
        return HttpRequest.newBuilder()
                .uri(URI.create(aiProperties.getBaseUrl().replaceAll("/$", "") + "/chat/completions"))
                .timeout(Duration.ofMillis(aiProperties.getReadTimeoutMs()))
                .header("Authorization", "Bearer " + aiProperties.getApiKey())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Accept-Encoding", "gzip");
    }

    private void ensureConfigured() {
        if (!StringUtils.hasText(aiProperties.getApiKey())) {
            throw BusinessException.badRequest("AI_CONFIG_MISSING", "AI 配置缺失，请检查后端配置");
        }
    }

    private String readResponseBody(HttpResponse<InputStream> response) throws Exception {
        try (InputStream stream = openBodyStream(response);
                ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            stream.transferTo(output);
            return output.toString(StandardCharsets.UTF_8);
        }
    }

    private InputStream openBodyStream(HttpResponse<InputStream> response) throws Exception {
        InputStream stream = response.body();
        String encoding = response.headers().firstValue("Content-Encoding").orElse("");
        if ("gzip".equalsIgnoreCase(encoding)) {
            return new GZIPInputStream(stream);
        }
        return stream;
    }

    private String summarizeProviderError(String body) {
        if (!StringUtils.hasText(body)) {
            return "空响应";
        }
        try {
            JsonNode root = objectMapper.readTree(body);
            String message = root.path("error").path("message").asText("");
            String code = root.path("error").path("code").asText("");
            if (StringUtils.hasText(message)) {
                return StringUtils.hasText(code) ? code + " - " + message : message;
            }
        } catch (Exception ex) {
            log.debug("Failed to parse provider error body", ex);
        }
        return body.length() <= 160 ? body : body.substring(0, 160) + "...";
    }

    private String extractContentText(JsonNode deltaNode) {
        if (deltaNode.isArray()) {
            StringBuilder builder = new StringBuilder();
            for (JsonNode item : deltaNode) {
                if (item.path("type").asText("").equals("text")) {
                    builder.append(item.path("text").asText(""));
                }
            }
            return builder.toString();
        }
        return deltaNode.toString();
    }
}
