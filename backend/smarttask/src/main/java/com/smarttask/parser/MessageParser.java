package com.smarttask.parser;

import org.springframework.stereotype.Component;

@Component
public class MessageParser {

    public ParsedCommand parse(String message) {

        // ป้องกันข้อความว่าง
        if (message == null || message.isBlank()) {
            return new ParsedCommand(CommandType.UNKNOWN, null);
        }

        // ตัดช่องว่างหัวท้าย
        message = message.trim();

        // คำสั่งเพิ่มงาน
        if (message.startsWith("เพิ่มงาน ")) {

            String taskName = message.substring("เพิ่มงาน ".length()).trim();

            if (taskName.isBlank()) {
                return new ParsedCommand(CommandType.UNKNOWN, null);
            }

            return new ParsedCommand(CommandType.ADD_TASK, taskName);
        }

        // คำสั่งดูงาน
        if (message.equals("ดูงาน")) {
            return new ParsedCommand(CommandType.LIST_TASKS, null);
        }

        // คำสั่งทำงานเสร็จ
        if (message.startsWith("เสร็จแล้ว ")) {

            String taskId = message.substring("เสร็จแล้ว ".length()).trim();

            return new ParsedCommand(CommandType.COMPLETE_TASK, taskId);
        }

        // คำสั่งลบงาน
        if (message.startsWith("ลบงาน ")) {

            String taskId = message.substring("ลบงาน ".length()).trim();

            return new ParsedCommand(CommandType.DELETE_TASK, taskId);
        }

        // ไม่ตรงกับคำสั่งใดเลย
        return new ParsedCommand(CommandType.UNKNOWN, null);
    }
}