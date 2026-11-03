package com.smarttask.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_name", nullable = false, length = 255)
    private String taskName;

    @Column(nullable = false)
    private boolean completed = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "line_user_id", nullable = false, length = 64)
    private String lineUserId;

    // กำหนดวันที่สร้างงานก่อนบันทึกลง Database
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // Constructor สำหรับ JPA
    protected Task() {
    }

    // Constructor สำหรับสร้าง Task ใหม่
    public Task(String taskName, String lineUserId) {
        this.taskName = taskName;
        this.lineUserId = lineUserId;
    }

    public Long getId() {
        return id;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getLineUserId() {
        return lineUserId;
    }

    public void setLineUserId(String lineUserId) {
        this.lineUserId = lineUserId;
    }
}