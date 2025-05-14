package com.novatech.dao;

import com.novatech.model.User;
import com.novatech.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAOImplementation implements UserDAO {

    @Override
    public User findById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        Connection connection = null;
        PreparedStatement sqlStatement = null;
        ResultSet rs = null;
        User user = null;

        try {
            connection = DBUtil.getConnection();
            sqlStatement = connection.prepareStatement(sql);
            sqlStatement.setInt(1, id);
            rs = sqlStatement.executeQuery();

            if (rs.next()) {
                user = extractUserFromResultSet(rs);
            }
        } finally {
            if (rs != null) rs.close();
            if (sqlStatement != null) sqlStatement.close();
            DBUtil.closeConnection(connection);
        }

        return user;
    }

    @Override
    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        Connection connection = null;
        PreparedStatement sqlStatement = null;
        ResultSet rs = null;
        User user = null;

        try {
            connection = DBUtil.getConnection();
            sqlStatement = connection.prepareStatement(sql);
            sqlStatement.setString(1, username);
            rs = sqlStatement.executeQuery();

            if (rs.next()) {
                user = extractUserFromResultSet(rs);
            }
        } finally {
            if (rs != null) rs.close();
            if (sqlStatement != null) sqlStatement.close();
            DBUtil.closeConnection(connection);
        }

        return user;
    }

    @Override
    public List<User> findAll() throws SQLException {
        String sql = "SELECT * FROM users";
        Connection connection = null;
        Statement sqlStatement = null;
        ResultSet rs = null;
        List<User> users = new ArrayList<>();

        try {
            connection = DBUtil.getConnection();
            sqlStatement = connection.createStatement();
            rs = sqlStatement.executeQuery(sql);

            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }
        } finally {
            if (rs != null) rs.close();
            if (sqlStatement != null) sqlStatement.close();
            DBUtil.closeConnection(connection);
        }

        return users;
    }

    @Override
    public int create(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password, email, full_name) VALUES (?, ?, ?, ?) RETURNING id";
        Connection connection = null;
        PreparedStatement sqlStatement = null;
        ResultSet rs = null;
        int id = 0;

        try {
            connection = DBUtil.getConnection();
            sqlStatement = connection.prepareStatement(sql);
            sqlStatement.setString(1, user.getUsername());
            sqlStatement.setString(2, user.getPassword());
            sqlStatement.setString(3, user.getEmail());
            sqlStatement.setString(4, user.getFullName());
            rs = sqlStatement.executeQuery();

            if (rs.next()) {
                id = rs.getInt("id");
                user.setId(id);
            }
        } finally {
            if (rs != null) rs.close();
            if (sqlStatement != null) sqlStatement.close();
            DBUtil.closeConnection(connection);
        }

        return id;
    }

    @Override
    public boolean update(User user) throws SQLException {
        String sql = "UPDATE users SET username = ?, password = ?, email = ?, full_name = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        Connection connection = null;
        PreparedStatement sqlStatement = null;
        boolean updated = false;

        try {
            connection = DBUtil.getConnection();
            sqlStatement = connection.prepareStatement(sql);
            sqlStatement.setString(1, user.getUsername());
            sqlStatement.setString(2, user.getPassword());
            sqlStatement.setString(3, user.getEmail());
            sqlStatement.setString(4, user.getFullName());
            sqlStatement.setInt(5, user.getId());

            int rowsAffected = sqlStatement.executeUpdate();
            updated = rowsAffected > 0;
        } finally {
            if (sqlStatement != null) sqlStatement.close();
            DBUtil.closeConnection(connection);
        }

        return updated;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        Connection connection = null;
        PreparedStatement sqlStatement = null;
        boolean deleted = false;

        try {
            connection = DBUtil.getConnection();
            sqlStatement = connection.prepareStatement(sql);
            sqlStatement.setInt(1, id);

            int rowsAffected = sqlStatement.executeUpdate();
            deleted = rowsAffected > 0;
        } finally {
            if (sqlStatement != null) sqlStatement.close();
            DBUtil.closeConnection(connection);
        }

        return deleted;
    }

    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setEmail(rs.getString("email"));
        user.setFullName(rs.getString("full_name"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        user.setUpdatedAt(rs.getTimestamp("updated_at"));
        return user;
    }
}