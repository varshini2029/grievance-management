package com.grievance.model;

public class User {
    private final int id;
    private final String name;
    private final String email;
    private final String role;       // STUDENT, ADMIN or STAFF
    private final String department;

    public User(int id, String name, String email, String role, String department) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.department = department;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getDepartment() { return department; }

    @Override
    public String toString() { return name + " (" + department + ")"; }
}
