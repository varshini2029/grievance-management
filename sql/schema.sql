-- Student Complaint Grievance Redressal System - schema
DROP DATABASE IF EXISTS grievance_db;
CREATE DATABASE grievance_db;
USE grievance_db;

CREATE TABLE users (
    user_id       INT AUTO_INCREMENT PRIMARY KEY,
    full_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(100) NOT NULL UNIQUE,
    password_hash CHAR(64)     NOT NULL,
    role          ENUM('STUDENT','ADMIN','STAFF') NOT NULL,
    department    VARCHAR(80),
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE complaints (
    complaint_id      INT AUTO_INCREMENT PRIMARY KEY,
    student_id        INT NOT NULL,
    title             VARCHAR(150) NOT NULL,
    description       TEXT NOT NULL,
    category          VARCHAR(50)  NOT NULL,
    status            ENUM('Pending','In Progress','Resolved','Rejected') NOT NULL DEFAULT 'Pending',
    assigned_staff_id INT NULL,
    remarks           VARCHAR(500),
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id)        REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_staff_id) REFERENCES users(user_id) ON DELETE SET NULL
);

CREATE TABLE complaint_history (
    history_id   INT AUTO_INCREMENT PRIMARY KEY,
    complaint_id INT NOT NULL,
    status       ENUM('Pending','In Progress','Resolved','Rejected') NOT NULL,
    remarks      VARCHAR(500),
    changed_by   INT NOT NULL,
    changed_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (complaint_id) REFERENCES complaints(complaint_id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by)   REFERENCES users(user_id)
);

CREATE INDEX idx_complaint_status  ON complaints(status);
CREATE INDEX idx_complaint_student ON complaints(student_id);
