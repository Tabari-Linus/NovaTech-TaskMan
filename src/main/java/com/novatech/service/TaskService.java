package com.novatech.service;

import com.novatech.dao.TaskDAO;
import com.novatech.dao.TaskDAOImplementation;
import com.novatech.model.Task;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public class TaskService {
    private TaskDAO taskDAO;

    public TaskService() {
        this.taskDAO = new TaskDAOImplementation();
    }

    public Task findById(int id) throws SQLException {
        return taskDAO.findById(id);
    }

    public List<Task> findAll() throws SQLException {
        return taskDAO.findAll();
    }

    public List<Task> findByUserId(int userId) throws SQLException {
        return taskDAO.findByUserId(userId);
    }

    public List<Task> findByStatus(int userId, String status) throws SQLException {
        return taskDAO.findByStatus(userId, status);
    }

    public List<Task> findByDateRange(int userId, Date startDate, Date endDate, String status, boolean sortDescending) throws SQLException {
        return taskDAO.findByDateRange(userId, startDate, endDate, status, sortDescending);
    }

    public int create(Task task) throws SQLException {
        return taskDAO.create(task);
    }

    public boolean update(Task task) throws SQLException {
        return taskDAO.update(task);
    }

    public boolean delete(int id) throws SQLException {
        return taskDAO.delete(id);
    }
}