package com.mobile.smartcalling.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.util.StreamUtils;

@Slf4j
public class RestTemplateLoggingInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        // 打印请求信息
        logRequest(request, body);

        // 执行请求并获取响应
        ClientHttpResponse response = execution.execute(request, body);

        // 可选择打印响应信息
        // logResponse(response);

        return response;
    }

    private void logRequest(HttpRequest request, byte[] body) {
        StringBuilder requestLog = new StringBuilder();
        requestLog.append("\n======================== REST REQUEST ========================\n")
                .append("URI         : ").append(request.getURI()).append("\n")
                .append("Method      : ").append(request.getMethod()).append("\n")
                .append("Headers     : ").append(request.getHeaders()).append("\n");

        if (body.length > 0) {
            requestLog.append("Request Body: ").append(new String(body, StandardCharsets.UTF_8)).append("\n");
        }

        requestLog.append("==============================================================\n");
        log.info(String.valueOf(requestLog));

    }
}