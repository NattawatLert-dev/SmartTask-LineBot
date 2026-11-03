package com.smarttask.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

@Component
public class LineSignatureValidator {

    public boolean validate(String body, String signature, String channelSecret) {

        try {
            Mac mac = Mac.getInstance("HmacSHA256");

            SecretKeySpec secretKey = new SecretKeySpec(
                    channelSecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );

            mac.init(secretKey);

            byte[] hash = mac.doFinal(
                    body.getBytes(StandardCharsets.UTF_8)
            );

            String expectedSignature = Base64.getEncoder()
                    .encodeToString(hash);

            byte[] expected = expectedSignature.getBytes(StandardCharsets.UTF_8);
            byte[] actual = signature.getBytes(StandardCharsets.UTF_8);

            return MessageDigest.isEqual(expected, actual);

        } catch (Exception e) {
            return false;
        }
    }
}