-- ============================================================
-- CAMPUS SKILL EXCHANGE DATABASE SCHEMA & SAMPLE DATA
-- Database: campus_skill_exchange
-- ============================================================

CREATE DATABASE IF NOT EXISTS campus_skill_exchange;
USE campus_skill_exchange;

-- 1. DROP TABLES IN PROPER ORDER TO AVOID FOREIGN KEY ISSUES
DROP TABLE IF EXISTS REVIEW;
DROP TABLE IF EXISTS SESSION;
DROP TABLE IF EXISTS LEARNING_REQUEST;
DROP TABLE IF EXISTS STUDENT_SKILL;
DROP TABLE IF EXISTS SKILL;
DROP TABLE IF EXISTS STUDENT;

-- 2. CREATE TABLE: STUDENT
CREATE TABLE STUDENT (
    student_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    department VARCHAR(100) NOT NULL,
    year INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. CREATE TABLE: SKILL
CREATE TABLE SKILL (
    skill_id INT PRIMARY KEY AUTO_INCREMENT,
    skill_name VARCHAR(100) NOT NULL UNIQUE,
    category VARCHAR(100) NOT NULL
);

-- 4. CREATE TABLE: STUDENT_SKILL
CREATE TABLE STUDENT_SKILL (
    student_id INT NOT NULL,
    skill_id INT NOT NULL,
    skill_type VARCHAR(20) NOT NULL, -- 'OFFERED' or 'WANTED'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (student_id, skill_id, skill_type),
    CONSTRAINT fk_student_skill_student FOREIGN KEY (student_id) REFERENCES STUDENT(student_id) ON DELETE CASCADE,
    CONSTRAINT fk_student_skill_skill FOREIGN KEY (skill_id) REFERENCES SKILL(skill_id) ON DELETE CASCADE
);

-- 5. CREATE TABLE: LEARNING_REQUEST
CREATE TABLE LEARNING_REQUEST (
    request_id INT PRIMARY KEY AUTO_INCREMENT,
    sender_id INT NOT NULL,
    receiver_id INT NOT NULL,
    skill_id INT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING', 
    -- Status progression: PENDING -> ACCEPTED / REJECTED -> SCHEDULED -> COMPLETED -> REVIEWED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_request_sender FOREIGN KEY (sender_id) REFERENCES STUDENT(student_id) ON DELETE CASCADE,
    CONSTRAINT fk_request_receiver FOREIGN KEY (receiver_id) REFERENCES STUDENT(student_id) ON DELETE CASCADE,
    CONSTRAINT fk_request_skill FOREIGN KEY (skill_id) REFERENCES SKILL(skill_id) ON DELETE CASCADE
);

-- 6. CREATE TABLE: SESSION
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

-- 7. CREATE TABLE: REVIEW
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

-- ============================================================
-- INDEXES FOR PERFORMANCE
-- ============================================================
CREATE INDEX idx_student_email ON STUDENT(email);
CREATE INDEX idx_skill_name ON SKILL(skill_name);
CREATE INDEX idx_student_skill_student ON STUDENT_SKILL(student_id);
CREATE INDEX idx_student_skill_type ON STUDENT_SKILL(skill_type);
CREATE INDEX idx_request_sender ON LEARNING_REQUEST(sender_id);
CREATE INDEX idx_request_receiver ON LEARNING_REQUEST(receiver_id);
CREATE INDEX idx_request_status ON LEARNING_REQUEST(status);
CREATE INDEX idx_session_request ON SESSION(request_id);
CREATE INDEX idx_session_status ON SESSION(status);
CREATE INDEX idx_review_session ON REVIEW(session_id);

-- ============================================================
-- SAMPLE DATA INSERTION
-- ============================================================

-- A. SAMPLE STUDENTS (Default password for all sample accounts is: password123)
INSERT INTO STUDENT (student_id, name, email, password, department, year) VALUES
(1, 'Rahul Sharma', 'rahul@campus.edu', 'password123', 'Computer Science & Engineering', 2),
(2, 'Priya Patel', 'priya@campus.edu', 'password123', 'Information Technology', 3),
(3, 'Aarav Mehta', 'aarav@campus.edu', 'password123', 'Electronics & Comm. Engineering', 2),
(4, 'Sneha Reddy', 'sneha@campus.edu', 'password123', 'Mechanical Engineering', 4),
(5, 'Vikram Singh', 'vikram@campus.edu', 'password123', 'Computer Science & Engineering', 3);

-- B. SAMPLE SKILLS
INSERT INTO SKILL (skill_id, skill_name, category) VALUES
(1, 'Java', 'Programming'),
(2, 'Python', 'Programming'),
(3, 'C++', 'Programming'),
(4, 'DSA', 'Computer Science'),
(5, 'Web Development', 'Web & App'),
(6, 'UI/UX', 'Design'),
(7, 'Graphic Design', 'Design'),
(8, 'Video Editing', 'Media & Arts'),
(9, 'Public Speaking', 'Soft Skills'),
(10, 'Data Science', 'Data & AI'),
(11, 'Machine Learning', 'Data & AI'),
(12, 'Mobile App Dev', 'Web & App');

-- C. SAMPLE STUDENT SKILLS (OFFERED & WANTED)
-- Rahul Sharma (Student 1)
INSERT INTO STUDENT_SKILL (student_id, skill_id, skill_type) VALUES
(1, 1, 'OFFERED'), -- Offers Java
(1, 4, 'OFFERED'), -- Offers DSA
(1, 2, 'WANTED'),  -- Wants Python
(1, 5, 'WANTED');  -- Wants Web Development

-- Priya Patel (Student 2)
INSERT INTO STUDENT_SKILL (student_id, skill_id, skill_type) VALUES
(2, 2, 'OFFERED'),  -- Offers Python
(2, 10, 'OFFERED'), -- Offers Data Science
(2, 11, 'OFFERED'), -- Offers Machine Learning
(2, 6, 'WANTED'),   -- Wants UI/UX
(2, 7, 'WANTED');   -- Wants Graphic Design

-- Aarav Mehta (Student 3)
INSERT INTO STUDENT_SKILL (student_id, skill_id, skill_type) VALUES
(3, 5, 'OFFERED'), -- Offers Web Development
(3, 6, 'OFFERED'), -- Offers UI/UX
(3, 1, 'WANTED'),  -- Wants Java
(3, 10, 'WANTED'); -- Wants Data Science

-- Sneha Reddy (Student 4)
INSERT INTO STUDENT_SKILL (student_id, skill_id, skill_type) VALUES
(4, 7, 'OFFERED'), -- Offers Graphic Design
(4, 8, 'OFFERED'), -- Offers Video Editing
(4, 9, 'OFFERED'), -- Offers Public Speaking
(4, 2, 'WANTED'),  -- Wants Python
(4, 4, 'WANTED');  -- Wants DSA

-- Vikram Singh (Student 5)
INSERT INTO STUDENT_SKILL (student_id, skill_id, skill_type) VALUES
(5, 3, 'OFFERED'), -- Offers C++
(5, 4, 'OFFERED'), -- Offers DSA
(5, 8, 'WANTED'),  -- Wants Video Editing
(5, 9, 'WANTED');  -- Wants Public Speaking

-- D. SAMPLE REQUESTS
-- 1) Rahul (1) requested Priya (2) for Python (Skill 2) -> ACCEPTED -> SCHEDULED -> COMPLETED -> REVIEWED
INSERT INTO LEARNING_REQUEST (request_id, sender_id, receiver_id, skill_id, status, created_at) VALUES
(1, 1, 2, 2, 'REVIEWED', '2026-09-20 10:30:00');

-- 2) Aarav (3) requested Rahul (1) for Java (Skill 1) -> SCHEDULED
INSERT INTO LEARNING_REQUEST (request_id, sender_id, receiver_id, skill_id, status, created_at) VALUES
(2, 3, 1, 1, 'SCHEDULED', '2026-10-01 14:15:00');

-- 3) Sneha (4) requested Priya (2) for Python (Skill 2) -> PENDING
INSERT INTO LEARNING_REQUEST (request_id, sender_id, receiver_id, skill_id, status, created_at) VALUES
(3, 4, 2, 2, 'PENDING', '2026-10-05 16:00:00');

-- 4) Vikram (5) requested Rahul (1) for DSA (Skill 4) -> ACCEPTED
INSERT INTO LEARNING_REQUEST (request_id, sender_id, receiver_id, skill_id, status, created_at) VALUES
(4, 5, 1, 4, 'ACCEPTED', '2026-10-06 11:00:00');

-- E. SAMPLE SESSIONS
-- Completed Session for Request 1 (Priya teaching Rahul Python)
INSERT INTO SESSION (session_id, request_id, session_date, session_time, mode, location_or_link, status, created_at) VALUES
(1, 1, '2026-09-25', '16:00:00', 'Online', 'https://meet.google.com/cse-pyth-ses', 'Completed', '2026-09-21 09:00:00');

-- Scheduled Session for Request 2 (Rahul teaching Aarav Java)
INSERT INTO SESSION (session_id, request_id, session_date, session_time, mode, location_or_link, status, created_at) VALUES
(2, 2, '2026-10-15', '15:30:00', 'Offline', 'Library Discussion Room B', 'Scheduled', '2026-10-02 11:20:00');

-- F. SAMPLE REVIEWS
-- Rahul reviewing Priya after Session 1
INSERT INTO REVIEW (review_id, session_id, reviewer_id, rating, comment, created_at) VALUES
(1, 1, 1, 5, 'Priya explained Python basics and list comprehensions extremely well with live coding examples!', '2026-09-26 18:00:00');
