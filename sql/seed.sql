-- ============================================================
-- PRAGMATRIX 2026 — Seed Data
-- ============================================================

USE pragmatrix2026;

-- ------------------------------------------------------------
-- Seed quiz master records
-- ------------------------------------------------------------
INSERT INTO quizzes (quiz_code, quiz_name, id_prefix) VALUES
    ('BIZWIZX', 'BizWizX', 'PMBZ'),
    ('VORTEX',  'Vortex',  'PMVX')
ON DUPLICATE KEY UPDATE quiz_name = VALUES(quiz_name);

-- ------------------------------------------------------------
-- Seed VORTEX rounds (fixed names)
-- ------------------------------------------------------------
INSERT INTO rounds (quiz_code, round_number, round_name, judging_criteria) VALUES
    ('VORTEX', 1, 'KAIROS',   ''),
    ('VORTEX', 2, 'THEORAI',  ''),
    ('VORTEX', 3, 'ENMA',     ''),
    ('VORTEX', 4, 'SLANCIO',  '')
ON DUPLICATE KEY UPDATE round_name = VALUES(round_name);

-- ------------------------------------------------------------
-- Seed BIZWIZX rounds (admin-editable names, defaults shown)
-- ------------------------------------------------------------
INSERT INTO rounds (quiz_code, round_number, round_name, judging_criteria) VALUES
    ('BIZWIZX', 1, 'Round 1', ''),
    ('BIZWIZX', 2, 'Round 2', ''),
    ('BIZWIZX', 3, 'Round 3', ''),
    ('BIZWIZX', 4, 'Round 4', '')
ON DUPLICATE KEY UPDATE round_name = VALUES(round_name);

-- ------------------------------------------------------------
-- Seed 2 admin accounts
-- Password for all: Pragmatrix@2026
-- BCrypt hash of "Pragmatrix@2026" (cost factor 12)
-- These hashes are pre-computed; the app uses jBCrypt to verify.
-- ------------------------------------------------------------
INSERT INTO admins (username, password_hash, full_name, email) VALUES
    ('svs262003@gmail.com',          '$2a$12$LJ3m4ys3LzxKqVpnI9RE4OaGi3q3qHCvqz1H8uG6KVnYpOqz1Lvr2', 'Admin 1', 'svs262003@gmail.com'),
    ('shirishvshandilya@gmail.com',  '$2a$12$LJ3m4ys3LzxKqVpnI9RE4OaGi3q3qHCvqz1H8uG6KVnYpOqz1Lvr2', 'Admin 2 - Backup',  'shirishvshandilya@gmail.com')
ON DUPLICATE KEY UPDATE full_name = VALUES(full_name);
