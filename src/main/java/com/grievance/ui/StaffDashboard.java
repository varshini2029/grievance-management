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
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StaffDashboard extends JFrame {
    private final User user;
    private final ComplaintDAO dao = new ComplaintDAO();
    private final DefaultTableModel model = UiUtil.newModel();
    private final JTable table = UiUtil.newTable(model);
    private List<Complaint> current = new ArrayList<>();

    public StaffDashboard(User user) {
        super("Staff Dashboard - " + user.getName());
        this.user = user;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 480);
        setLocationRelativeTo(null);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton update = new JButton("Update Status");
        JButton view = new JButton("View Details");
        JButton refresh = new JButton("Refresh");
        JButton logout = new JButton("Logout");
        top.add(new JLabel("Welcome, " + user.getName() + "   (complaints assigned to you)   "));
        top.add(update); top.add(view); top.add(refresh); top.add(logout);

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
        setContentPane(root);
        load();
    }

    private void load() {
        try {
            current = dao.findByStaff(user.getId());
            UiUtil.fill(model, current);
        } catch (SQLException e) {
            UiUtil.error(this, e);
        }
    }

    private void updateStatus() {
        int id = UiUtil.selectedId(this, table);
        if (id < 0) return;
        JComboBox<String> status = new JComboBox<>(new String[]{"In Progress", "Resolved", "Rejected"});
        JTextField remarks = new JTextField(25);
        Object[] form = {"New status:", status, "Remarks:", remarks};
        int ok = JOptionPane.showConfirmDialog(this, form, "Update complaint #" + id,
                JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION) return;
        try {
            dao.updateStatus(id, (String) status.getSelectedItem(), remarks.getText().trim(), user.getId());
            load();
        } catch (SQLException e) {
            UiUtil.error(this, e);
        }
    }
}
