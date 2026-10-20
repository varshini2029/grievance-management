package com.grievance.ui;

import com.grievance.dao.ComplaintDAO;
import com.grievance.dao.UserDAO;
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
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminDashboard extends JFrame {
    private final User user;
    private final ComplaintDAO dao = new ComplaintDAO();
    private final UserDAO userDAO = new UserDAO();
    private final DefaultTableModel model = UiUtil.newModel();
    private final JTable table = UiUtil.newTable(model);
    private final JLabel statsLabel = new JLabel(" ");
    private final JComboBox<String> filter;
    private List<Complaint> current = new ArrayList<>();

    public AdminDashboard(User user) {
        super("Admin Dashboard - " + user.getName());
        this.user = user;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 520);
        setLocationRelativeTo(null);

        String[] opts = {"All", "Pending", "In Progress", "Resolved", "Rejected"};
        filter = new JComboBox<>(opts);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton assign = new JButton("Assign Staff");
        JButton update = new JButton("Update Status");
        JButton view = new JButton("View Details");
        JButton refresh = new JButton("Refresh");
        JButton logout = new JButton("Logout");
        top.add(new JLabel("Filter:"));
        top.add(filter);
        top.add(assign); top.add(update); top.add(view); top.add(refresh); top.add(logout);

        filter.addActionListener(e -> load());
        assign.addActionListener(e -> assignStaff());
        update.addActionListener(e -> updateStatus());
        view.addActionListener(e -> {
            int id = UiUtil.selectedId(this, table);
            Complaint c = id < 0 ? null : UiUtil.find(current, id);
            if (c != null) UiUtil.showDetails(this, c, dao);
        });
        refresh.addActionListener(e -> load());
        logout.addActionListener(e -> { dispose(); new LoginFrame().setVisible(true); });

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        root.add(top, BorderLayout.NORTH);
        root.add(new JScrollPane(table), BorderLayout.CENTER);
        root.add(statsLabel, BorderLayout.SOUTH);
        setContentPane(root);
        load();
    }

    private void load() {
        try {
            String f = (String) filter.getSelectedItem();
            current = new ArrayList<>();
            for (Complaint c : dao.findAll()) {
                if ("All".equals(f) || c.getStatus().equals(f)) current.add(c);
            }
            UiUtil.fill(model, current);

            Map<String, Integer> counts = dao.countByStatus();
            StringBuilder sb = new StringBuilder("Totals:  ");
            counts.forEach((k, v) -> sb.append(k).append(": ").append(v).append("    "));
            statsLabel.setText(sb.toString());
        } catch (SQLException e) {
            UiUtil.error(this, e);
        }
    }

    private void assignStaff() {
        int id = UiUtil.selectedId(this, table);
        if (id < 0) return;
        try {
            List<User> staff = userDAO.listStaff();
            if (staff.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No staff accounts exist yet.");
                return;
            }
            JComboBox<User> box = new JComboBox<>(staff.toArray(new User[0]));
            int ok = JOptionPane.showConfirmDialog(this, new Object[]{"Assign complaint #" + id + " to:", box},
                    "Assign staff", JOptionPane.OK_CANCEL_OPTION);
            if (ok != JOptionPane.OK_OPTION) return;
            User chosen = (User) box.getSelectedItem();
            dao.assign(id, chosen.getId(), user.getId());
            load();
        } catch (SQLException e) {
            UiUtil.error(this, e);
        }
    }

    private void updateStatus() {
        int id = UiUtil.selectedId(this, table);
        if (id < 0) return;
        JComboBox<String> status = new JComboBox<>(UiUtil.STATUSES);
        JTextField remarks = new JTextField(25);
        int ok = JOptionPane.showConfirmDialog(this, new Object[]{"New status:", status, "Remarks:", remarks},
                "Update complaint #" + id, JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION) return;
        try {
            dao.updateStatus(id, (String) status.getSelectedItem(), remarks.getText().trim(), user.getId());
            load();
        } catch (SQLException e) {
            UiUtil.error(this, e);
        }
    }
}
