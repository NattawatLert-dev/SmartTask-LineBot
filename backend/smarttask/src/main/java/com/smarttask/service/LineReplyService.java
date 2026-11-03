package com.smarttask.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class LineReplyService {

    private final RestClient restClient;

    public LineReplyService(
            @Value("${line.channel-access-token}") String channelAccessToken) {

        this.restClient = RestClient.builder()
                .baseUrl("https://api.line.me")
                .defaultHeader(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + channelAccessToken
                )
                .defaultHeader(
                        HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_JSON_VALUE
                )
                .build();
    }

    public void replyText(String replyToken, String message) {

        Map<String, Object> requestBody = Map.of(
                "replyToken", replyToken,
                "messages", List.of(
                        Map.of(
                                "type", "text",
                                "text", message
                        )
                )
        );

        restClient.post()
                .uri("/v2/bot/message/reply")
                .body(requestBody)
                .retrieve()
                .toBodilessEntity();
    }
}