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

@RestController
@RequestMapping("/api/line")
public class LineWebhookController {

    private final LineSignatureValidator signatureValidator;

    @Value("${line.channel-secret}")
    private String channelSecret;

    public LineWebhookController(LineSignatureValidator signatureValidator) {
        this.signatureValidator = signatureValidator;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> receiveWebhook(
            @RequestBody String body,
            @RequestHeader(value = "X-Line-Signature", required = false)
            String signature) {

        if (signature == null ||
                !signatureValidator.validate(body, signature, channelSecret)) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid signature");
        }

        System.out.println("LINE Webhook received:");
        System.out.println(body);

        return ResponseEntity.ok("OK");
    }
}