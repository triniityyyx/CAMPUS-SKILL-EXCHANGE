# 🎓 CAMPUS SKILL EXCHANGE
> **A Peer-to-Peer College Skill Exchange Platform**  
> Built with **Java (Swing GUI)**, **MySQL**, and **JDBC** using **Object-Oriented Architecture**.

---

## 📌 1. Project Overview & Idea
**Campus Skill Exchange** is a peer-to-peer platform designed for college students to exchange academic and technical skills directly with peers without relying solely on formal classroom lectures.

### 🔄 End-to-End Workflow:
```
Registration
    ↓
Login
    ↓
Dashboard (Live Stats: Offered, Wanted, Requests, Sessions, Rating)
    ↓
My Skills (Add Skills I Can Teach / Skills I Want to Learn)
    ↓
Find Skills (Rule-Based Matching: 70% Skill, 15% Category, 15% Rating)
    ↓
Send Learning Request
    ↓
Accept / Reject Request (Received Requests)
    ↓
Schedule Session (Date, Time, Online/Offline, Meeting Link/Location)
    ↓
Complete Session
    ↓
Submit Review & Rating (1 to 5 Stars + Feedback Comment)
```

### 🏷️ Request Status Progression:
$$\text{PENDING} \longrightarrow \text{ACCEPTED / REJECTED} \longrightarrow \text{SCHEDULED} \longrightarrow \text{COMPLETED} \longrightarrow \text{REVIEWED}$$

---

## 🏗️ 2. Project Architecture & Directory Structure

```
campus-skill-exchange/
├── lib/
│   └── mysql-connector-j.jar     # MySQL JDBC Driver (Included)
├── sql/
│   └── schema.sql                # Complete DB schema, constraints & sample data
├── src/
│   ├── model/
│   │   ├── User.java             # Base class (Inheritance & Encapsulation)
│   │   ├── Student.java          # Student extends User (OOP Inheritance)
│   │   ├── Skill.java            # Skill entity
│   │   ├── StudentSkill.java     # OFFERED / WANTED mapping
│   │   ├── LearningRequest.java  # Request lifecycle model
│   │   ├── Session.java          # Learning session model
│   │   ├── Review.java           # Review & rating model
│   │   └── SkillMatchResult.java # Rule-based match scoring model
│   │
│   ├── dao/
│   │   ├── StudentDAO.java       # PreparedStatement CRUD for Students
│   │   ├── SkillDAO.java         # PreparedStatement CRUD for Skills & Mappings
│   │   ├── RequestDAO.java       # PreparedStatement CRUD for Requests
│   │   ├── SessionDAO.java       # PreparedStatement CRUD for Sessions
│   │   └── ReviewDAO.java        # PreparedStatement CRUD for Reviews
│   │
│   ├── service/
│   │   ├── AuthService.java      # Login & Registration with input validation
│   │   ├── SkillService.java     # Skill catalog & user skill management
│   │   ├── MatchingService.java  # Deterministic Rule-Based Skill Matching
│   │   ├── RequestService.java   # Request dispatching & lifecycle
│   │   └── SessionService.java   # Session scheduling & review management
│   │
│   ├── ui/
│   │   ├── Theme.java            # Modern Dark Theme design system (Colors, Badges, Cards, Fonts)
│   │   ├── LoginFrame.java       # Sign In screen with DB connection health check
│   │   ├── RegisterFrame.java    # Full registration with validation
│   │   ├── DashboardFrame.java   # Central modern application window with sidebar navigation
│   │   ├── MySkillsFrame.java    # Manage Teach/Learn skills (Add, Remove, View)
│   │   ├── SkillSearchFrame.java # Search mentors with rule-based match scores
│   │   ├── RequestsFrame.java    # Sent and Received requests with Accept/Reject
│   │   ├── SessionsFrame.java    # Session coordination (Schedule, Complete, Cancel)
│   │   └── ReviewsFrame.java     # 1-5 Star post-session peer review submission
│   │
│   ├── util/
│   │   ├── DBConnection.java     # Centralized JDBC Connection manager
│   │   └── DatabaseInitializer.java # 1-click database & sample data setup tool
│   │
│   └── Main.java                 # Main application launcher
├── db.properties                 # External DB credentials config (Easy edit)
├── build.bat                     # Windows compilation script
├── run.bat                       # Windows 1-click run script
├── init_db.bat                   # 1-click database initialization script
└── CampusSkillExchange.iml       # IntelliJ IDEA project module configuration
```

---

## ⚙️ 3. Object-Oriented Programming (OOP) Implementation

1. **Encapsulation**:
   - Every model class (`User`, `Student`, `Skill`, `LearningRequest`, `Session`, `Review`) has `private` variables with validated public getters and setters.
   - Database operations are completely encapsulated within DAO classes. SQL strings never leak into UI components.
2. **Inheritance**:
   - `User.java` defines common user identity (`id`, `name`, `email`, `password`).
   - `Student.java extends User` inherits user attributes and adds student-specific attributes (`department`, `year`, `averageRating`, `reviewCount`).
3. **Polymorphism**:
   - Method overriding for `toString()`, specialized rating calculations, and polymorphic rendering of student cards and badges.
4. **Abstraction & Layered Separation**:
   - Strict 3-tier enterprise architecture:
     $$\text{UI Layer} \longrightarrow \text{Service Layer} \longrightarrow \text{DAO Layer} \longrightarrow \text{MySQL Database}$$
   - Business validation (email format, self-request prevention, duplicate reviews check) is abstracted in the Service layer.

---

## 🎯 4. Rule-Based Skill Matching System

> **Note**: This system is a deterministic **Rule-Based Algorithmic Matcher**, not Machine Learning or AI.

When a student searches for a skill:
1. The system identifies students offering that skill (excluding the logged-in student).
2. It computes a **Total Match Score (0 - 100%)** based on:
   - **Skill Match (Up to 70%)**: Exact skill name matches receive 70%, substring/prefix matches receive 58–65%.
   - **Category & Mutual Barter Match (Up to 15%)**: 
     - **Full 15% Bonus**: If the candidate student wants a skill that the searcher can teach (*Mutual Barter*).
     - **12% Bonus**: If the candidate's skill category matches skills the searcher wants to learn.
     - **8% Baseline**: General category alignment.
   - **Rating Match (Up to 15%)**:
     - Calculated as $\left(\frac{\text{Average Rating}}{5.0}\right) \times 15\%$.
     - New students without reviews receive a standard neutral baseline of 4.0/5.0 (12.0%).
3. Results are sorted in descending order of highest match score.

---

## 🗄️ 5. Database Setup (MySQL)

### Step 5.1: Create Database & Tables Manually (or via Workbench)
Open **MySQL Workbench** or your MySQL command-line prompt and run the statements from `sql/schema.sql`:

```sql
CREATE DATABASE IF NOT EXISTS campus_skill_exchange;
USE campus_skill_exchange;

-- 1. STUDENT Table
CREATE TABLE STUDENT (
    student_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    department VARCHAR(100) NOT NULL,
    year INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. SKILL Table
CREATE TABLE SKILL (
    skill_id INT PRIMARY KEY AUTO_INCREMENT,
    skill_name VARCHAR(100) NOT NULL UNIQUE,
    category VARCHAR(100) NOT NULL
);

-- 3. STUDENT_SKILL Table
CREATE TABLE STUDENT_SKILL (
    student_id INT NOT NULL,
    skill_id INT NOT NULL,
    skill_type VARCHAR(20) NOT NULL, -- 'OFFERED' or 'WANTED'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (student_id, skill_id, skill_type),
    CONSTRAINT fk_student_skill_student FOREIGN KEY (student_id) REFERENCES STUDENT(student_id) ON DELETE CASCADE,
    CONSTRAINT fk_student_skill_skill FOREIGN KEY (skill_id) REFERENCES SKILL(skill_id) ON DELETE CASCADE
);

-- 4. LEARNING_REQUEST Table
CREATE TABLE LEARNING_REQUEST (
    request_id INT PRIMARY KEY AUTO_INCREMENT,
    sender_id INT NOT NULL,
    receiver_id INT NOT NULL,
    skill_id INT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_request_sender FOREIGN KEY (sender_id) REFERENCES STUDENT(student_id) ON DELETE CASCADE,
    CONSTRAINT fk_request_receiver FOREIGN KEY (receiver_id) REFERENCES STUDENT(student_id) ON DELETE CASCADE,
    CONSTRAINT fk_request_skill FOREIGN KEY (skill_id) REFERENCES SKILL(skill_id) ON DELETE CASCADE
);

-- 5. SESSION Table
CREATE TABLE SESSION (
    session_id INT PRIMARY KEY AUTO_INCREMENT,
    request_id INT NOT NULL,
    session_date DATE NOT NULL,
    session_time TIME NOT NULL,
    mode VARCHAR(30) NOT NULL, -- 'Online' or 'Offline'
    location_or_link VARCHAR(255) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'Scheduled', -- 'Scheduled', 'Completed', 'Cancelled'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_session_request FOREIGN KEY (request_id) REFERENCES LEARNING_REQUEST(request_id) ON DELETE CASCADE
);

-- 6. REVIEW Table
CREATE TABLE REVIEW (
    review_id INT PRIMARY KEY AUTO_INCREMENT,
    session_id INT NOT NULL,
    reviewer_id INT NOT NULL,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment VARCHAR(500) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_session_reviewer UNIQUE (session_id, reviewer_id),
    CONSTRAINT fk_review_session FOREIGN KEY (session_id) REFERENCES SESSION(session_id) ON DELETE CASCADE,
    CONSTRAINT fk_review_reviewer FOREIGN KEY (reviewer_id) REFERENCES STUDENT(student_id) ON DELETE CASCADE
);
```

### Step 5.2: 1-Click Database Setup Alternative
You can also run the built-in database setup script:
- Double-click **`init_db.bat`**, OR
- Run command:
  ```powershell
  java -cp "bin;lib/mysql-connector-j.jar" util.DatabaseInitializer
  ```

---

## 🔑 6. Sample Accounts & Credentials

The database is pre-populated with realistic students, skills, requests, sessions, and reviews.

| Student Name | Email | Password | Department | Year | Skills Offered | Skills Wanted |
| :--- | :--- | :--- | :--- | :---: | :--- | :--- |
| **Rahul Sharma** | `rahul@campus.edu` | `password123` | CSE | 2 | Java, DSA | Python, Web Dev |
| **Priya Patel** | `priya@campus.edu` | `password123` | IT | 3 | Python, Data Science, ML | UI/UX, Graphic Design |
| **Aarav Mehta** | `aarav@campus.edu` | `password123` | ECE | 2 | Web Dev, UI/UX | Java, Data Science |
| **Sneha Reddy** | `sneha@campus.edu` | `password123` | Mech | 4 | Graphic Design, Video Editing | Python, DSA |
| **Vikram Singh** | `vikram@campus.edu` | `password123` | CSE | 3 | C++, DSA | Video Editing, Public Speaking |

---

## ⚙️ 7. Database Credentials Configuration

You can change your database password in **two easy ways**:

### Option A: Edit `db.properties` (Recommended - No Recompilation Needed)
Open `db.properties` in the project root:
```properties
db.url=jdbc:mysql://localhost:3306/campus_skill_exchange?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=YOUR_MYSQL_PASSWORD_HERE
```

### Option B: Edit `src/util/DBConnection.java`
Open `src/util/DBConnection.java`:
```java
private static String URL = "jdbc:mysql://localhost:3306/campus_skill_exchange?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
private static String USER = "root";
private static String PASSWORD = "YOUR_MYSQL_PASSWORD_HERE";
```

---

## 🚀 8. How to Open and Run in IntelliJ IDEA

1. Open **IntelliJ IDEA**.
2. Click **File** > **Open...** and select the folder `campus-skill-exchange`.
3. IntelliJ will automatically detect the project configuration (`CampusSkillExchange.iml`).
4. Ensure JDK 17+ (or JDK 21 / JDK 27) is selected in **File > Project Structure > Project > SDK**.
5. The MySQL connector library is already linked in `lib/mysql-connector-j.jar`.
6. Open `src/Main.java`.
7. Click the green **Run ▶** icon beside `public static void main(String[] args)` (or press `Shift + F10`).
8. The modern dark-themed login window will appear!

---

## 💻 9. How to Run from Command Line / Terminal

### Compile the project:
```powershell
javac -cp "lib/mysql-connector-j.jar" src/*.java src/model/*.java src/dao/*.java src/service/*.java src/ui/*.java src/util/*.java -d bin
```

### Run the application:
```powershell
java -cp "bin;lib/mysql-connector-j.jar" Main
```

Or simply double-click **`run.bat`** on Windows!

---

## 🧪 10. End-to-End Testing Walkthrough

Follow these simple steps to test the entire application flow:

1. **Sign In**:
   - Enter `rahul@campus.edu` with password `password123`.
   - Notice the **Dashboard** displaying:
     - 2 Offered Skills (Java, DSA)
     - 2 Wanted Skills (Python, Web Development)
     - Live Rating and upcoming session count.
2. **Add a New Skill**:
   - Click **💡 My Skills** on the sidebar.
   - Click **+ Add Skill to Teach**, type `Spring Boot` or select `Programming`, and click **Add Skill**.
   - Notice the offered count updates instantly.
3. **Find Mentors**:
   - Click **🔍 Find Skills** on the sidebar.
   - Type `Python` and click **Search Mentors**.
   - You will see **Priya Patel** with match score breakdown (e.g. `98.0% Match`).
4. **Send Learning Request**:
   - Click **[ Send Learning Request ]** on Priya's card.
   - Confirm the dialog.
5. **Switch Account & Accept Request**:
   - Click **🚪 Sign Out** on the bottom left.
   - Log in as **`priya@campus.edu`** (`password123`).
   - Notice the sidebar has **📩 Requests (1 NEW)** badge!
   - Click **📩 Requests**, find the request from Rahul, and click **[ ✓ Accept ]**.
   - Click **Yes** when prompted to schedule a session.
6. **Schedule Session**:
   - Enter date `2026-10-20`, time `16:00:00`, mode `Online`, meeting link `https://meet.google.com/skill-exchange`.
   - Click **Confirm & Schedule**.
7. **Complete Session & Review**:
   - Navigate to **📅 Sessions**.
   - Find the scheduled session and click **[ ✓ Complete ]**.
   - Confirm completion and click **Yes** to write a review.
   - Select **5 Stars**, enter feedback (e.g. *"Excellent mentoring session, covered all concepts clearly!"*), and click **Submit Review**.
   - Check the **⭐ Reviews** tab and **Dashboard** to see the updated ratings and completed status!

---

## 🛠️ 11. Troubleshooting & Common Errors

| Error | Root Cause | Solution |
| :--- | :--- | :--- |
| `Access denied for user 'root'@'localhost'` | Incorrect MySQL root password. | Edit `db.properties` with your real MySQL password. |
| `Communications link failure` | MySQL Server service is stopped. | Start MySQL in Windows Services (`net start MySQL80`). |
| `ClassNotFoundException: com.mysql.cj.jdbc.Driver` | MySQL Connector jar missing from classpath. | Ensure `lib/mysql-connector-j.jar` is included with `-cp` or added to IntelliJ Project Libraries. |
| `Table 'campus_skill_exchange.student' doesn't exist` | Database schema not created yet. | Run `init_db.bat` or execute `sql/schema.sql` in MySQL. |
| `Duplicate entry for key 'email'` | An account already exists with that email. | Use another email or log into the existing account. |
