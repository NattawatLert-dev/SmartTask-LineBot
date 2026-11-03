package com.smarttask.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.smarttask.entity.Task;
import com.smarttask.repository.TaskRepository;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    // เพิ่มงานใหม่
    public Task addTask(String taskName, String lineUserId) {
        Task task = new Task(taskName, lineUserId);
        return taskRepository.save(task);
    }

    // ดูงานทั้งหมดของผู้ใช้
    public List<Task> getTasks(String lineUserId) {
        return taskRepository.findByLineUserId(lineUserId);
    }

    // ทำเครื่องหมายว่างานเสร็จแล้ว
    public Optional<Task> completeTask(Long taskId, String lineUserId) {
        Optional<Task> optionalTask =
                taskRepository.findById(taskId);

        if (optionalTask.isEmpty()) {
            return Optional.empty();
        }

        Task task = optionalTask.get();

        // ป้องกันการแก้ไขงานของผู้ใช้อื่น
        if (!task.getLineUserId().equals(lineUserId)) {
            return Optional.empty();
        }

        task.setCompleted(true);

        return Optional.of(taskRepository.save(task));
    }

    // ลบงาน
    public boolean deleteTask(Long taskId, String lineUserId) {
        Optional<Task> optionalTask =
                taskRepository.findById(taskId);

        if (optionalTask.isEmpty()) {
            return false;
        }

        Task task = optionalTask.get();

        // ป้องกันการลบงานของผู้ใช้อื่น
        if (!task.getLineUserId().equals(lineUserId)) {
            return false;
        }

        taskRepository.delete(task);

        return true;
    }
}