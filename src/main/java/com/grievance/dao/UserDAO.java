package com.grievance.dao;

import com.grievance.config.DBConfig;
import com.grievance.model.User;
import com.grievance.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    /** Returns the user if email and password match, otherwise null. */
    public User login(String email, String password) throws SQLException {
        String sql = "SELECT user_id, full_name, email, role, department FROM users "
                   + "WHERE email = ? AND password_hash = ?";
        try (Connection con = DBConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, PasswordUtil.hash(password));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(rs.getInt("user_id"), rs.getString("full_name"),
                            rs.getString("email"), rs.getString("role"), rs.getString("department"));
                }
            }
        }
        return null;
    }

    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection con = DBConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Self-registration is only for students. */
    public void registerStudent(String name, String email, String password, String department) throws SQLException {
        String sql = "INSERT INTO users (full_name, email, password_hash, role, department) "
                   + "VALUES (?, ?, ?, 'STUDENT', ?)";
        try (Connection con = DBConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, PasswordUtil.hash(password));
            ps.setString(4, department);
            ps.executeUpdate();
        }
    }

    public List<User> listStaff() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT user_id, full_name, email, role, department FROM users "
                   + "WHERE role = 'STAFF' ORDER BY full_name";
        try (Connection con = DBConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new User(rs.getInt("user_id"), rs.getString("full_name"),
                        rs.getString("email"), rs.getString("role"), rs.getString("department")));
            }
        }
        return list;
    }
}
