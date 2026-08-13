-- ============================================================
-- PRAGMATRIX 2026 — Migration v2
-- Adds: lead_email, team_login_otps, team_sessions
-- Run this on an existing pragmatrix2026 database.
-- ============================================================

USE pragmatrix2026;

-- 1. Add team lead email to existing teams table
ALTER TABLE teams
  ADD COLUMN lead_email VARCHAR(150) NOT NULL DEFAULT 'noemail@placeholder.com' AFTER college_name;

-- 2. OTP table for team dashboard login
CREATE TABLE IF NOT EXISTS team_login_otps (
    otp_id         INT AUTO_INCREMENT PRIMARY KEY,
    unique_id      VARCHAR(15)  NOT NULL,
    otp_code       VARCHAR(6)   NOT NULL,
    generated_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    expires_at     TIMESTAMP    NOT NULL,
    is_used        BOOLEAN      DEFAULT FALSE,
    attempt_count  INT          DEFAULT 0,
    FOREIGN KEY (unique_id) REFERENCES teams(unique_id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;

-- Index for fast OTP lookup during verification
CREATE INDEX idx_otp_lookup ON team_login_otps(unique_id, is_used);

-- 3. Team dashboard sessions (server-side tracking)
CREATE TABLE IF NOT EXISTS team_sessions (
    session_id  VARCHAR(64)  PRIMARY KEY,
    unique_id   VARCHAR(15)  NOT NULL,
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    expires_at  TIMESTAMP    NOT NULL,
    FOREIGN KEY (unique_id) REFERENCES teams(unique_id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;
