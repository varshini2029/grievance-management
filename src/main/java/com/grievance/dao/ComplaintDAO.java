package com.grievance.dao;

import com.grievance.config.DBConfig;
import com.grievance.model.Complaint;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ComplaintDAO {

    private static final String SELECT_BASE =
        "SELECT c.complaint_id, c.student_id, s.full_name AS student_name, c.title, c.description, "
      + "c.category, c.status, c.assigned_staff_id, f.full_name AS staff_name, c.remarks, "
      + "c.created_at, c.updated_at "
      + "FROM complaints c "
      + "JOIN users s ON c.student_id = s.user_id "
      + "LEFT JOIN users f ON c.assigned_staff_id = f.user_id ";

    private Complaint map(ResultSet rs) throws SQLException {
        Complaint c = new Complaint();
        c.setId(rs.getInt("complaint_id"));
        c.setStudentId(rs.getInt("student_id"));
        c.setStudentName(rs.getString("student_name"));
        c.setTitle(rs.getString("title"));
        c.setDescription(rs.getString("description"));
        c.setCategory(rs.getString("category"));
        c.setStatus(rs.getString("status"));
        int staff = rs.getInt("assigned_staff_id");
        c.setStaffId(rs.wasNull() ? null : staff);
        c.setStaffName(rs.getString("staff_name"));
        c.setRemarks(rs.getString("remarks"));
        c.setCreatedAt(String.valueOf(rs.getTimestamp("created_at")));
        c.setUpdatedAt(String.valueOf(rs.getTimestamp("updated_at")));
        return c;
    }

    private List<Complaint> query(String where, Object... params) throws SQLException {
        List<Complaint> list = new ArrayList<>();
        String sql = SELECT_BASE + where + " ORDER BY c.created_at DESC, c.complaint_id DESC";
        try (Connection con = DBConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public List<Complaint> findAll() throws SQLException { return query(""); }

    public List<Complaint> findByStudent(int studentId) throws SQLException {
        return query("WHERE c.student_id = ?", studentId);
    }

    public List<Complaint> findByStaff(int staffId) throws SQLException {
        return query("WHERE c.assigned_staff_id = ?", staffId);
    }

    /** Inserts the complaint and its first history row in one transaction. */
    public void add(int studentId, String title, String description, String category) throws SQLException {
        String insert = "INSERT INTO complaints (student_id, title, description, category) VALUES (?, ?, ?, ?)";
        String hist = "INSERT INTO complaint_history (complaint_id, status, remarks, changed_by) "
                    + "VALUES (?, 'Pending', 'Complaint submitted', ?)";
        try (Connection con = DBConfig.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(insert, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, studentId);
                ps.setString(2, title);
                ps.setString(3, description);
                ps.setString(4, category);
                ps.executeUpdate();
                int newId;
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    newId = keys.getInt(1);
                }
                try (PreparedStatement ph = con.prepareStatement(hist)) {
                    ph.setInt(1, newId);
                    ph.setInt(2, studentId);
                    ph.executeUpdate();
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /** Admin assigns a complaint to a staff member; status becomes In Progress if it was Pending. */
    public void assign(int complaintId, int staffId, int adminId) throws SQLException {
        String upd = "UPDATE complaints SET assigned_staff_id = ?, "
                   + "status = IF(status = 'Pending', 'In Progress', status) WHERE complaint_id = ?";
        String hist = "INSERT INTO complaint_history (complaint_id, status, remarks, changed_by) "
                    + "SELECT complaint_id, status, 'Assigned to staff', ? FROM complaints WHERE complaint_id = ?";
        try (Connection con = DBConfig.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement p1 = con.prepareStatement(upd);
                 PreparedStatement p2 = con.prepareStatement(hist)) {
                p1.setInt(1, staffId);
                p1.setInt(2, complaintId);
                p1.executeUpdate();
                p2.setInt(1, adminId);
                p2.setInt(2, complaintId);
                p2.executeUpdate();
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    public void updateStatus(int complaintId, String status, String remarks, int changedBy) throws SQLException {
        String upd = "UPDATE complaints SET status = ?, remarks = ? WHERE complaint_id = ?";
        String hist = "INSERT INTO complaint_history (complaint_id, status, remarks, changed_by) VALUES (?, ?, ?, ?)";
        try (Connection con = DBConfig.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement p1 = con.prepareStatement(upd);
                 PreparedStatement p2 = con.prepareStatement(hist)) {
                p1.setString(1, status);
                if (remarks == null || remarks.isBlank()) p1.setNull(2, Types.VARCHAR); else p1.setString(2, remarks);
                p1.setInt(3, complaintId);
                p1.executeUpdate();
                p2.setInt(1, complaintId);
                p2.setString(2, status);
                p2.setString(3, remarks);
                p2.setInt(4, changedBy);
                p2.executeUpdate();
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    public List<String> history(int complaintId) throws SQLException {
        List<String> lines = new ArrayList<>();
        String sql = "SELECT h.changed_at, h.status, h.remarks, u.full_name FROM complaint_history h "
                   + "JOIN users u ON h.changed_by = u.user_id WHERE h.complaint_id = ? ORDER BY h.history_id";
        try (Connection con = DBConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, complaintId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String r = rs.getString("remarks");
                    lines.add(rs.getTimestamp("changed_at") + "  |  " + rs.getString("status")
                            + "  |  " + (r == null ? "-" : r) + "  |  by " + rs.getString("full_name"));
                }
            }
        }
        return lines;
    }

    public Map<String, Integer> countByStatus() throws SQLException {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (String s : new String[]{"Pending", "In Progress", "Resolved", "Rejected"}) map.put(s, 0);
        String sql = "SELECT status, COUNT(*) AS n FROM complaints GROUP BY status";
        try (Connection con = DBConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) map.put(rs.getString("status"), rs.getInt("n"));
        }
        return map;
    }
}
