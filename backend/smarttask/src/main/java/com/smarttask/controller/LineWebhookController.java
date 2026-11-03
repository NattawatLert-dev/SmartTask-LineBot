package com.smarttask.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smarttask.entity.Task;
import com.smarttask.parser.MessageParser;
import com.smarttask.parser.ParsedCommand;
import com.smarttask.security.LineSignatureValidator;
import com.smarttask.service.LineReplyService;
import com.smarttask.service.TaskService;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/line")
public class LineWebhookController {

    private final LineSignatureValidator signatureValidator;
    private final LineReplyService lineReplyService;
    private final ObjectMapper objectMapper;
    private final MessageParser messageParser;
    private final TaskService taskService;

    @Value("${line.channel-secret}")
    private String channelSecret;

    public LineWebhookController(
            LineSignatureValidator signatureValidator,
            LineReplyService lineReplyService,
            ObjectMapper objectMapper,
            MessageParser messageParser,
            TaskService taskService) {

        this.signatureValidator = signatureValidator;
        this.lineReplyService = lineReplyService;
        this.objectMapper = objectMapper;
        this.messageParser = messageParser;
        this.taskService = taskService;
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

                // 4. สนใจเฉพาะข้อความ Text
                if ("message".equals(eventType)
                        && "text".equals(event.path("message")
                                .path("type").asText())) {

                    String replyToken =
                            event.path("replyToken").asText();

                    String userMessage =
                            event.path("message").path("text").asText();

                    String lineUserId =
                            event.path("source").path("userId").asText("");

                    if (replyToken.isBlank()) {
                        continue;
                    }

                    // 5. ตรวจสอบ LINE User ID
                    if (lineUserId.isBlank()) {
                        lineReplyService.replyText(
                                replyToken,
                                "ไม่สามารถระบุผู้ใช้งานได้ กรุณาใช้แชตส่วนตัวกับ Bot"
                        );
                        continue;
                    }

                    // 6. แปลคำสั่ง
                    ParsedCommand command = messageParser.parse(userMessage);

                    String replyMessage;

                    // 7. ประมวลผลคำสั่ง
                    switch (command.getCommandType()) {

                        case ADD_TASK:
                            Task newTask = taskService.addTask(
                                    command.getTaskName(),
                                    lineUserId
                            );

                            replyMessage = "เพิ่มงานสำเร็จ!\n"
                                    + "ID: " + newTask.getId() + "\n"
                                    + "งาน: " + newTask.getTaskName();
                            break;

                        case LIST_TASKS:
                            List<Task> tasks =
                                    taskService.getTasks(lineUserId);

                            if (tasks.isEmpty()) {
                                replyMessage = "คุณยังไม่มีรายการงาน";
                            } else {
                                StringBuilder taskList =
                                        new StringBuilder("รายการงานของคุณ:\n\n");

                                for (Task task : tasks) {
                                    String status = task.isCompleted()
                                            ? "เสร็จแล้ว"
                                            : "ยังไม่เสร็จ";

                                    taskList.append("ID: ")
                                            .append(task.getId())
                                            .append("\n")
                                            .append("งาน: ")
                                            .append(task.getTaskName())
                                            .append("\n")
                                            .append("สถานะ: ")
                                            .append(status)
                                            .append("\n\n");
                                }

                                replyMessage = taskList.toString();
                            }
                            break;

                        case COMPLETE_TASK:
                            try {
                                Long completeId = Long.parseLong(
                                        command.getTaskName()
                                );

                                Optional<Task> completedTask =
                                        taskService.completeTask(
                                                completeId,
                                                lineUserId
                                        );

                                if (completedTask.isPresent()) {
                                    replyMessage = "ทำงานเสร็จแล้ว!\n"
                                            + completedTask.get().getTaskName();
                                } else {
                                    replyMessage = "ไม่พบงานนี้ หรือคุณไม่มีสิทธิ์จัดการงานนี้";
                                }

                            } catch (NumberFormatException e) {
                                replyMessage = "กรุณาระบุ ID งานให้ถูกต้อง";
                            }
                            break;

                        case DELETE_TASK:
                            try {
                                Long deleteId = Long.parseLong(
                                        command.getTaskName()
                                );

                                boolean deleted = taskService.deleteTask(
                                        deleteId,
                                        lineUserId
                                );

                                replyMessage = deleted
                                        ? "ลบงาน ID " + deleteId + " สำเร็จ"
                                        : "ไม่พบงานนี้ หรือคุณไม่มีสิทธิ์ลบงานนี้";

                            } catch (NumberFormatException e) {
                                replyMessage = "กรุณาระบุ ID งานให้ถูกต้อง";
                            }
                            break;

                        default:
                            replyMessage = "ไม่เข้าใจคำสั่ง กรุณาลองใหม่";
                    }

                    // 8. ตอบกลับ LINE
                    lineReplyService.replyText(replyToken, replyMessage);
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