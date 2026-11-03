package com.smarttask.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smarttask.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // ค้นหางานทั้งหมดของผู้ใช้แต่ละคน
    List<Task> findByLineUserId(String lineUserId);
}