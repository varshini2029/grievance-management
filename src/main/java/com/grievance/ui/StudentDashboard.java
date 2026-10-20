package com.grievance.ui;

import com.grievance.dao.ComplaintDAO;
import com.grievance.model.Complaint;
import com.grievance.model.User;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StudentDashboard extends JFrame {
    private final User user;
    private final ComplaintDAO dao = new ComplaintDAO();
    private final DefaultTableModel model = UiUtil.newModel();
    private final JTable table = UiUtil.newTable(model);
    private List<Complaint> current = new ArrayList<>();

    public StudentDashboard(User user) {
        super("Student Dashboard - " + user.getName());
        this.user = user;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 480);
        setLocationRelativeTo(null);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton add = new JButton("New Complaint");
        JButton view = new JButton("View Details");
        JButton refresh = new JButton("Refresh");
        JButton logout = new JButton("Logout");
        top.add(new JLabel("Welcome, " + user.getName() + "   "));
        top.add(add); top.add(view); top.add(refresh); top.add(logout);

        add.addActionListener(e -> newComplaint());
        view.addActionListener(e -> viewDetails());
        refresh.addActionListener(e -> load());
        logout.addActionListener(e -> { dispose(); new LoginFrame().setVisible(true); });

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        root.add(top, BorderLayout.NORTH);
        root.add(new JScrollPane(table), BorderLayout.CENTER);
        setContentPane(root);
        load();
    }

    private void load() {
        try {
            current = dao.findByStudent(user.getId());
            UiUtil.fill(model, current);
        } catch (SQLException e) {
            UiUtil.error(this, e);
        }
    }

    private void viewDetails() {
        int id = UiUtil.selectedId(this, table);
        if (id < 0) return;
        Complaint c = UiUtil.find(current, id);
        if (c != null) UiUtil.showDetails(this, c, dao);
    }

    private void newComplaint() {
        JTextField title = new JTextField(30);
        JComboBox<String> category = new JComboBox<>(UiUtil.CATEGORIES);
        JTextArea desc = new JTextArea(6, 30);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        Object[] form = {"Title:", title, "Category:", category, "Description:", new JScrollPane(desc)};
        int ok = JOptionPane.showConfirmDialog(this, form, "New complaint", JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION) return;

        String t = title.getText().trim();
        String d = desc.getText().trim();
        if (t.isEmpty() || d.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title and description are required.");
            return;
        }
        try {
            dao.add(user.getId(), t, d, (String) category.getSelectedItem());
            load();
            JOptionPane.showMessageDialog(this, "Complaint submitted.");
        } catch (SQLException e) {
            UiUtil.error(this, e);
        }
    }
}
