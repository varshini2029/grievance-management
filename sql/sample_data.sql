USE grievance_db;

-- Demo passwords (stored as SHA-256):  admin123 / staff123 / student123
INSERT INTO users (full_name, email, password_hash, role, department) VALUES
('System Admin', 'admin@college.edu',     '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMIN',   'Administration'),
('Ravi Kumar',   'ravi@college.edu',      '10176e7b7b24d317acfcf8d2064cfd2f24e154f7b5a96603077d5ef813d6a6b6', 'STAFF',   'Hostel'),
('Meena Iyer',   'meena@college.edu',     '10176e7b7b24d317acfcf8d2064cfd2f24e154f7b5a96603077d5ef813d6a6b6', 'STAFF',   'Academics'),
('Varshini',     'varshini@college.edu',  '703b0a3d6ad75b649a28adde7d83c6251da457549263bc7ff45ec709b0a8448b', 'STUDENT', 'AIML'),
('Arun S',       'arun@college.edu',      '703b0a3d6ad75b649a28adde7d83c6251da457549263bc7ff45ec709b0a8448b', 'STUDENT', 'CSE');

INSERT INTO complaints (student_id, title, description, category, status, assigned_staff_id, remarks) VALUES
(4, 'Wi-Fi not working in hostel', 'Wi-Fi in block B, floor 2 has been down for three days.', 'Hostel',         'In Progress', 2,    'Technician informed'),
(4, 'Wrong marks in internal exam', 'My internal mark for DBMS is entered incorrectly.',       'Academics',      'Pending',     NULL, NULL),
(5, 'Broken fan in classroom',      'Fan in room 305 is not working.',                         'Infrastructure', 'Pending',     NULL, NULL);

INSERT INTO complaint_history (complaint_id, status, remarks, changed_by) VALUES
(1, 'Pending',     'Complaint submitted', 4),
(1, 'In Progress', 'Technician informed', 2),
(2, 'Pending',     'Complaint submitted', 4),
(3, 'Pending',     'Complaint submitted', 5);
