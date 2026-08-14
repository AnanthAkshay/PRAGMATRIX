-- ============================================================
-- PRAGMATRIX 2026 — Full Database Schema
-- Quiz Scoring & Leaderboard System
-- ============================================================

CREATE DATABASE IF NOT EXISTS pragmatrix2026
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE pragmatrix2026;

-- ------------------------------------------------------------
-- Admin accounts
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS admins (
    admin_id    INT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(150) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name   VARCHAR(100),
    email       VARCHAR(150) UNIQUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Quiz master table (BIZWIZX / VORTEX)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS quizzes (
    quiz_code   VARCHAR(10)  PRIMARY KEY,
    quiz_name   VARCHAR(50)  NOT NULL,
    id_prefix   VARCHAR(10)  NOT NULL
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Teams / participants
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS teams (
    unique_id       VARCHAR(15)  PRIMARY KEY,
    quiz_code       VARCHAR(10)  NOT NULL,
    college_name    VARCHAR(150) NOT NULL,
    team_lead_name  VARCHAR(100) NOT NULL,
    lead_email      VARCHAR(150) NOT NULL,
    registered_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_teams_quiz (quiz_code),
    INDEX idx_teams_college (college_name),
    FOREIGN KEY (quiz_code) REFERENCES quizzes(quiz_code)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Rounds (4 per quiz)
-- BIZWIZX: names are admin-editable
-- VORTEX:  names fixed to KAIROS / THEORAI / ENMA / SLANCIO
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS rounds (
    round_id        INT AUTO_INCREMENT PRIMARY KEY,
    quiz_code       VARCHAR(10)  NOT NULL,
    round_number    INT          NOT NULL,
    round_name      VARCHAR(100) NOT NULL,
    judging_criteria VARCHAR(255) DEFAULT '',
    is_finished     BOOLEAN DEFAULT FALSE,
    finished_at     TIMESTAMP NULL,
    INDEX idx_rounds_quiz (quiz_code),
    UNIQUE KEY uq_quiz_round (quiz_code, round_number),
    FOREIGN KEY (quiz_code) REFERENCES quizzes(quiz_code)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Scores: one row per team per round
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS scores (
    score_id   INT AUTO_INCREMENT PRIMARY KEY,
    unique_id  VARCHAR(15)   NOT NULL,
    round_id   INT           NOT NULL,
    points     DECIMAL(6,2)  NOT NULL DEFAULT 0,
    entered_by INT,
    entered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_scores_team (unique_id),
    INDEX idx_scores_round (round_id),
    UNIQUE KEY uq_team_round (unique_id, round_id),
    FOREIGN KEY (unique_id) REFERENCES teams(unique_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (round_id)  REFERENCES rounds(round_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (entered_by) REFERENCES admins(admin_id)
        ON UPDATE SET NULL ON DELETE SET NULL
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- OTPs issued for team dashboard login
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS team_login_otps (
    otp_id         INT AUTO_INCREMENT PRIMARY KEY,
    unique_id      VARCHAR(15)  NOT NULL,
    otp_code       VARCHAR(6)   NOT NULL,
    generated_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    expires_at     TIMESTAMP    NOT NULL,
    is_used        BOOLEAN      DEFAULT FALSE,
    attempt_count  INT          DEFAULT 0,
    INDEX idx_otp_lookup (unique_id, is_used),
    FOREIGN KEY (unique_id) REFERENCES teams(unique_id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Team dashboard sessions (server-side tracking)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS team_sessions (
    session_id  VARCHAR(64)  PRIMARY KEY,
    unique_id   VARCHAR(15)  NOT NULL,
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    expires_at  TIMESTAMP    NOT NULL,
    FOREIGN KEY (unique_id) REFERENCES teams(unique_id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Leaderboard VIEW (computed, not stored)
-- Sums points across all finished rounds per team
-- ------------------------------------------------------------
CREATE OR REPLACE VIEW leaderboard AS
SELECT
    t.unique_id,
    t.college_name,
    t.team_lead_name,
    t.quiz_code,
    COALESCE(SUM(s.points), 0) AS total_points
FROM teams t
LEFT JOIN scores s ON t.unique_id = s.unique_id
LEFT JOIN rounds r ON s.round_id = r.round_id AND r.is_finished = TRUE
GROUP BY t.unique_id, t.college_name, t.team_lead_name, t.quiz_code
ORDER BY total_points DESC;
