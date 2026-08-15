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
-- ------------------------------------------------------------
-- Seed 10 admin accounts (admin1 to admin10)
-- Password for all 10 accounts: Pragmatrix@2026
-- BCrypt hash of "Pragmatrix@2026" (cost factor 12)
-- ------------------------------------------------------------
INSERT INTO admins (username, password_hash, full_name, email) VALUES
    ('admin1',  '$2a$12$QrRaOdah7KqbssSOYnxXkuxv3oLVCNngNMgei9WJ/7YneOYu92CR6', 'Admin One',   'admin1@pragmatrix.com'),
    ('admin2',  '$2a$12$QrRaOdah7KqbssSOYnxXkuxv3oLVCNngNMgei9WJ/7YneOYu92CR6', 'Admin Two',   'admin2@pragmatrix.com'),
    ('admin3',  '$2a$12$QrRaOdah7KqbssSOYnxXkuxv3oLVCNngNMgei9WJ/7YneOYu92CR6', 'Admin Three', 'admin3@pragmatrix.com'),
    ('admin4',  '$2a$12$QrRaOdah7KqbssSOYnxXkuxv3oLVCNngNMgei9WJ/7YneOYu92CR6', 'Admin Four',  'admin4@pragmatrix.com'),
    ('admin5',  '$2a$12$QrRaOdah7KqbssSOYnxXkuxv3oLVCNngNMgei9WJ/7YneOYu92CR6', 'Admin Five',  'admin5@pragmatrix.com'),
    ('admin6',  '$2a$12$QrRaOdah7KqbssSOYnxXkuxv3oLVCNngNMgei9WJ/7YneOYu92CR6', 'Admin Six',   'admin6@pragmatrix.com'),
    ('admin7',  '$2a$12$QrRaOdah7KqbssSOYnxXkuxv3oLVCNngNMgei9WJ/7YneOYu92CR6', 'Admin Seven', 'admin7@pragmatrix.com'),
    ('admin8',  '$2a$12$QrRaOdah7KqbssSOYnxXkuxv3oLVCNngNMgei9WJ/7YneOYu92CR6', 'Admin Eight', 'admin8@pragmatrix.com'),
    ('admin9',  '$2a$12$QrRaOdah7KqbssSOYnxXkuxv3oLVCNngNMgei9WJ/7YneOYu92CR6', 'Admin Nine',  'admin9@pragmatrix.com'),
    ('admin10', '$2a$12$QrRaOdah7KqbssSOYnxXkuxv3oLVCNngNMgei9WJ/7YneOYu92CR6', 'Admin Ten',   'admin10@pragmatrix.com')
ON DUPLICATE KEY UPDATE password_hash = VALUES(password_hash), full_name = VALUES(full_name);
