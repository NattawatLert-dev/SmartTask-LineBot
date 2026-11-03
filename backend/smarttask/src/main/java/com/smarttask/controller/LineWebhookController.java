package com.smarttask.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smarttask.security.LineSignatureValidator;
import com.smarttask.service.LineReplyService;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/line")
public class LineWebhookController {

    private final LineSignatureValidator signatureValidator;
    private final LineReplyService lineReplyService;
    private final ObjectMapper objectMapper;

    @Value("${line.channel-secret}")
    private String channelSecret;

    public LineWebhookController(
            LineSignatureValidator signatureValidator,
            LineReplyService lineReplyService,
            ObjectMapper objectMapper) {

        this.signatureValidator = signatureValidator;
        this.lineReplyService = lineReplyService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> receiveWebhook(
            @RequestBody String body,
            @RequestHeader(value = "X-Line-Signature", required = false)
            String signature) {

        // 1. ตรวจสอบ Signature
        if (signature == null ||
                !signatureValidator.validate(body, signature, channelSecret)) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid signature");
        }

        try {
            // 2. อ่าน JSON
            JsonNode root = objectMapper.readTree(body);
            JsonNode events = root.path("events");

            // 3. วนอ่าน Event
            for (JsonNode event : events) {

                String eventType = event.path("type").asText();

                // 4. สนใจเฉพาะข้อความที่ผู้ใช้ส่งมา
                if ("message".equals(eventType)
                        && "text".equals(event.path("message")
                                .path("type").asText())) {

                    String replyToken =
                            event.path("replyToken").asText();

                    String userMessage =
                            event.path("message").path("text").asText();

                    // 5. ตอบกลับข้อความ
                    if (!replyToken.isBlank()) {
                        lineReplyService.replyText(
                                replyToken,
                                "SmartTask ได้รับข้อความแล้ว: " + userMessage
                        );
                    }
                }
            }

            return ResponseEntity.ok("OK");

        } catch (Exception e) {
            System.err.println("Webhook processing failed: "
                    + e.getMessage());

            return ResponseEntity.internalServerError()
                    .body("Webhook processing failed");
        }
    }
}