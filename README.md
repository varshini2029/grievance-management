# Student Complaint Grievance Redressal System

Java Swing + JDBC + MySQL desktop application (Maven project).

Students submit complaints. The admin assigns them to staff. Staff update the status.
Every change is recorded in a history table.

## Features
- Login for three roles: **Student**, **Admin**, **Staff** (students can self-register)
- Student: submit complaint, see own complaints, view status history
- Admin: see all complaints, filter by status, assign staff, change status, see totals
- Staff: see assigned complaints, update status with remarks
- Statuses: Pending, In Progress, Resolved, Rejected
- Passwords stored as SHA-256 hashes. DB password is NOT in the code.

## Folder structure
```
grievance-system/
├── pom.xml
├── config.properties.example     <- copy to config.properties
├── .gitignore
├── sql/
│   ├── schema.sql                <- run first
│   └── sample_data.sql           <- run second (demo users + complaints)
└── src/main/java/com/grievance/
    ├── Main.java
    ├── config/DBConfig.java      (reads DB settings)
    ├── util/PasswordUtil.java    (SHA-256)
    ├── model/    User.java, Complaint.java
    ├── dao/      UserDAO.java, ComplaintDAO.java   (all SQL lives here)
    └── ui/       LoginFrame, StudentDashboard, AdminDashboard, StaffDashboard, UiUtil
```

## Setup (5 steps)
1. **Install:** JDK 17+, Maven, MySQL Server + Workbench, and the VS Code *Extension Pack for Java*.
2. **Database:** in MySQL Workbench, open `sql/schema.sql` (File > Open SQL Script), click the lightning bolt.
   Then open `sql/sample_data.sql` and run it. Refresh the Schemas panel to see `grievance_db`.
3. **Config:** copy `config.properties.example` to `config.properties` in the project root
   (next to `pom.xml`) and put your MySQL password in it.
   (Or set environment variables `DB_URL`, `DB_USER`, `DB_PASSWORD`.)
4. **Open the folder** `grievance-system` in VS Code. Wait for Maven to download the MySQL driver
   (bottom-right progress).
5. **Run:** open `Main.java` and click **Run** above `main`, or in the VS Code terminal:
   ```
   mvn compile exec:java
   ```
   (run it from the project folder so `config.properties` is found)

## Demo logins
| Role    | Email                  | Password   |
|---------|------------------------|------------|
| Admin   | admin@college.edu      | admin123   |
| Staff   | ravi@college.edu       | staff123   |
| Staff   | meena@college.edu      | staff123   |
| Student | varshini@college.edu   | student123 |
| Student | arun@college.edu       | student123 |

## Testing guide
1. **Wrong login:** enter a wrong password. Expect "Invalid email or password".
2. **Register:** click "New student? Register", create a student, log in with it.
3. **Submit:** as a student, click New Complaint, fill the form. It appears with status Pending.
4. **Assign:** log in as admin, select that complaint, Assign Staff > Ravi Kumar.
   Status becomes In Progress.
5. **Staff update:** log in as ravi@college.edu, select the complaint, Update Status > Resolved with remarks.
6. **Student check:** log in as the student again, View Details. Status is Resolved and the history shows every step.
7. **Admin filter:** log in as admin, use the Filter box. Totals at the bottom update.
8. **Validation:** try an empty title, a duplicate email, or a short password. Expect warnings.

## Troubleshooting
| Problem | Fix |
|---|---|
| "Communications link failure" | MySQL server is not running. Start it from Services (Windows) or MySQL Workbench. |
| "Access denied for user" | Wrong user or password in `config.properties`. |
| "Unknown database grievance_db" | You have not run `sql/schema.sql` yet. |
| "No suitable driver" | Maven has not finished downloading. Right-click `pom.xml` > *Reload project*. |
| Config not picked up | Run from the project root folder (where `config.properties` is). |

## Team split suggestion (3 members)
- Member 1: SQL, `DBConfig`, `UserDAO`, `LoginFrame`
- Member 2: `ComplaintDAO`, `StudentDashboard`
- Member 3: `AdminDashboard`, `StaffDashboard`, testing and README
