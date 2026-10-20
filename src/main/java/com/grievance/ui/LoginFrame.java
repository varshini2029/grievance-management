package com.grievance.ui;

import com.grievance.dao.UserDAO;
import com.grievance.model.User;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;

public class LoginFrame extends JFrame {
    private final JTextField emailField = new JTextField(20);
    private final JPasswordField passField = new JPasswordField(20);
    private final UserDAO userDAO = new UserDAO();

    public LoginFrame() {
        super("Student Complaint Grievance Redressal System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Grievance Redressal Login", JLabel.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
        p.add(title, g);

        g.gridwidth = 1;
        g.gridy = 1; g.gridx = 0; p.add(new JLabel("Email:"), g);
        g.gridx = 1; p.add(emailField, g);
        g.gridy = 2; g.gridx = 0; p.add(new JLabel("Password:"), g);
        g.gridx = 1; p.add(passField, g);

        JButton login = new JButton("Login");
        JButton register = new JButton("New student? Register");
        g.gridy = 3; g.gridx = 0; g.gridwidth = 2; p.add(login, g);
        g.gridy = 4; p.add(register, g);

        login.addActionListener(e -> doLogin());
        passField.addActionListener(e -> doLogin());
        register.addActionListener(e -> doRegister());

        getRootPane().setDefaultButton(login);
        setContentPane(p);
        pack();
        setLocationRelativeTo(null);
    }

    private void doLogin() {
        String email = emailField.getText().trim();
        String pass = new String(passField.getPassword());
        if (email.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter email and password.");
            return;
        }
        try {
            User u = userDAO.login(email, pass);
            if (u == null) {
                JOptionPane.showMessageDialog(this, "Invalid email or password.", "Login failed",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            switch (u.getRole()) {
                case "ADMIN" -> new AdminDashboard(u).setVisible(true);
                case "STAFF" -> new StaffDashboard(u).setVisible(true);
                default -> new StudentDashboard(u).setVisible(true);
            }
            dispose();
        } catch (SQLException ex) {
            UiUtil.error(this, ex);
        }
    }

    private void doRegister() {
        JTextField name = new JTextField(18);
        JTextField email = new JTextField(18);
        JTextField dept = new JTextField(18);
        JPasswordField pass = new JPasswordField(18);
        Object[] form = {"Full name:", name, "Email:", email, "Department:", dept, "Password (min 6 chars):", pass};
        int ok = JOptionPane.showConfirmDialog(this, form, "Student registration", JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION) return;

        String n = name.getText().trim(), em = email.getText().trim(), d = dept.getText().trim();
        String pw = new String(pass.getPassword());
        if (n.isEmpty() || em.isEmpty() || d.isEmpty() || pw.length() < 6) {
            JOptionPane.showMessageDialog(this, "Fill all fields. Password must be at least 6 characters.");
            return;
        }
        if (!em.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) {
            JOptionPane.showMessageDialog(this, "Enter a valid email address.");
            return;
        }
        try {
            if (userDAO.emailExists(em)) {
                JOptionPane.showMessageDialog(this, "This email is already registered.");
                return;
            }
            userDAO.registerStudent(n, em, pw, d);
            JOptionPane.showMessageDialog(this, "Registered successfully. You can log in now.");
        } catch (SQLException ex) {
            UiUtil.error(this, ex);
        }
    }
}
