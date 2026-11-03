package com.smarttask.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smarttask.parser.MessageParser;
import com.smarttask.parser.ParsedCommand;
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
    private final MessageParser messageParser;

    @Value("${line.channel-secret}")
    private String channelSecret;

    public LineWebhookController(
        LineSignatureValidator signatureValidator,
        LineReplyService lineReplyService,
        ObjectMapper objectMapper,
        MessageParser messageParser) {

    this.signatureValidator = signatureValidator;
    this.lineReplyService = lineReplyService;
    this.objectMapper = objectMapper;
    this.messageParser = messageParser;
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
                        ParsedCommand command = messageParser.parse(userMessage);

                        String replyMessage;

                        switch (command.getCommandType()) {

                            case ADD_TASK:
                                replyMessage = "รับคำสั่งเพิ่มงาน: " + command.getTaskName();
                                break;

                            case LIST_TASKS:
                                replyMessage = "กำลังเรียกดูรายการงาน";
                                break;

                            case COMPLETE_TASK:
                                replyMessage = "รับคำสั่งทำงานหมายเลข " + command.getTaskName() + " เสร็จแล้ว";
                                break;

                            case DELETE_TASK:
                                replyMessage = "รับคำสั่งลบงานหมายเลข " + command.getTaskName();
                                break;

                            default:
                                replyMessage = "ไม่เข้าใจคำสั่ง กรุณาลองใหม่";
                        }

                        lineReplyService.replyText(replyToken, replyMessage);
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