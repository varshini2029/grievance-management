package com.grievance.ui;

import com.grievance.dao.ComplaintDAO;
import com.grievance.model.Complaint;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import java.awt.Component;
import java.awt.Dimension;
import java.sql.SQLException;
import java.util.List;

/** Small helpers shared by all screens. */
final class UiUtil {
    static final String[] STATUSES = {"Pending", "In Progress", "Resolved", "Rejected"};
    static final String[] CATEGORIES = {"Academics", "Hostel", "Infrastructure", "Library", "Transport", "Other"};
    static final String[] COLUMNS = {"ID", "Title", "Category", "Student", "Assigned To", "Status", "Last Updated"};

    private UiUtil() { }

    static DefaultTableModel newModel() {
        return new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    static JTable newTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setRowHeight(24);
        t.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        t.getColumnModel().getColumn(0).setMaxWidth(50);
        t.getColumnModel().getColumn(1).setPreferredWidth(240);
        return t;
    }

    static void fill(DefaultTableModel model, List<Complaint> list) {
        model.setRowCount(0);
        for (Complaint c : list) {
            model.addRow(new Object[]{c.getId(), c.getTitle(), c.getCategory(), c.getStudentName(),
                    c.getStaffName() == null ? "Not assigned" : c.getStaffName(), c.getStatus(), c.getUpdatedAt()});
        }
    }

    /** Returns the complaint ID of the selected row, or -1 (after warning the user). */
    static int selectedId(Component parent, JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(parent, "Please select a complaint first.", "No selection",
                    JOptionPane.WARNING_MESSAGE);
            return -1;
        }
        return (int) table.getValueAt(row, 0);
    }

    static void error(Component parent, Exception e) {
        JOptionPane.showMessageDialog(parent,
                "Database error: " + e.getMessage()
                + "\n\nCheck that MySQL is running and config.properties is correct.",
                "Error", JOptionPane.ERROR_MESSAGE);
    }

    static void showDetails(Component parent, Complaint c, ComplaintDAO dao) {
        StringBuilder sb = new StringBuilder();
        sb.append("Complaint #").append(c.getId()).append("\n")
          .append("Title: ").append(c.getTitle()).append("\n")
          .append("Category: ").append(c.getCategory()).append("\n")
          .append("Student: ").append(c.getStudentName()).append("\n")
          .append("Assigned to: ").append(c.getStaffName() == null ? "Not assigned" : c.getStaffName()).append("\n")
          .append("Status: ").append(c.getStatus()).append("\n")
          .append("Submitted: ").append(c.getCreatedAt()).append("\n\n")
          .append("Description:\n").append(c.getDescription()).append("\n\n")
          .append("Latest remarks: ").append(c.getRemarks() == null ? "-" : c.getRemarks()).append("\n\n")
          .append("History (time | status | remarks | by):\n");
        try {
            for (String line : dao.history(c.getId())) sb.append("  ").append(line).append("\n");
        } catch (SQLException e) {
            sb.append("  (could not load history)\n");
        }
        JTextArea area = new JTextArea(sb.toString(), 18, 60);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(620, 380));
        JOptionPane.showMessageDialog(parent, sp, "Complaint details", JOptionPane.PLAIN_MESSAGE);
    }

    /** Finds a complaint in the list by id. */
    static Complaint find(List<Complaint> list, int id) {
        for (Complaint c : list) if (c.getId() == id) return c;
        return null;
    }
}
