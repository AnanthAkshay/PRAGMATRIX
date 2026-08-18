-- ============================================================
-- PRAGMATRIX 2026 — VORTEX Judging Criteria Schema & Seed
-- ============================================================

USE pragmatrix2026;

-- 1. Vortex Rounds
CREATE TABLE IF NOT EXISTS vortex_rounds (
    round_id      INT AUTO_INCREMENT PRIMARY KEY,
    round_name    VARCHAR(100) NOT NULL,
    display_order INT NOT NULL DEFAULT 1
) ENGINE=InnoDB;

-- 2. Judging Components
CREATE TABLE IF NOT EXISTS judging_components (
    component_id    INT AUTO_INCREMENT PRIMARY KEY,
    round_id        INT NOT NULL,
    component_label VARCHAR(150) DEFAULT NULL,
    max_marks       INT NOT NULL DEFAULT 0,
    display_order   INT NOT NULL DEFAULT 1,
    FOREIGN KEY (round_id) REFERENCES vortex_rounds(round_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 3. Judging Criteria
CREATE TABLE IF NOT EXISTS judging_criteria (
    criterion_id     INT AUTO_INCREMENT PRIMARY KEY,
    component_id     INT NOT NULL,
    criterion_name   VARCHAR(255) NOT NULL,
    judges_look_for  TEXT DEFAULT NULL,
    max_marks        INT NOT NULL DEFAULT 0,
    display_order    INT NOT NULL DEFAULT 1,
    FOREIGN KEY (component_id) REFERENCES judging_components(component_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 4. Team Round Detailed Scores
CREATE TABLE IF NOT EXISTS team_round_scores (
    score_id        INT AUTO_INCREMENT PRIMARY KEY,
    unique_id       VARCHAR(15) NOT NULL,
    criterion_id    INT NOT NULL,
    score_awarded   DECIMAL(5,2) NOT NULL DEFAULT 0,
    evaluator_name  VARCHAR(100) DEFAULT 'Admin',
    date_evaluated  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_team_criterion (unique_id, criterion_id),
    FOREIGN KEY (unique_id) REFERENCES teams(unique_id) ON DELETE CASCADE,
    FOREIGN KEY (criterion_id) REFERENCES judging_criteria(criterion_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- SEED DATA FOR VORTEX 4 ROUNDS
-- ------------------------------------------------------------

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE team_round_scores;
TRUNCATE TABLE judging_criteria;
TRUNCATE TABLE judging_components;
TRUNCATE TABLE vortex_rounds;
SET FOREIGN_KEY_CHECKS = 1;

-- Insert 4 VORTEX Rounds with KAIROS (1), TREORAI (2), ENMA (3), GRAND FINALE (4)
INSERT INTO vortex_rounds (round_id, round_name, display_order) VALUES
(1, 'TREORAI', 2),
(2, 'KAIROS', 1),
(3, 'ENMA', 3),
(4, 'GRAND FINALE', 4);

-- ------------------------------------------------------------
-- ROUND 1: TREORAI (Flat list, 1 component, 100 total)
-- ------------------------------------------------------------
INSERT INTO judging_components (component_id, round_id, component_label, max_marks, display_order) VALUES
(1, 1, 'General Evaluation', 100, 1);

INSERT INTO judging_criteria (criterion_id, component_id, criterion_name, judges_look_for, max_marks, display_order) VALUES
(1, 1, 'Quality of candidate selection', NULL, 20, 1),
(2, 1, 'Role-job fit', NULL, 20, 2),
(3, 1, 'Team strategy & decision-making', NULL, 15, 3),
(4, 1, 'Justification for hiring (including budget allocation)', NULL, 15, 4),
(5, 1, 'Understanding of the disruption and answering it', NULL, 20, 5),
(6, 1, 'Ability to answer judge\'s question', NULL, 10, 6);

-- ------------------------------------------------------------
-- ROUND 2: KAIROS (2 components, 50 marks each, 100 total)
-- ------------------------------------------------------------
INSERT INTO judging_components (component_id, round_id, component_label, max_marks, display_order) VALUES
(2, 2, 'Component A - Investor Pitch', 50, 1),
(3, 2, 'Component B - Budget Allocation & Market Response', 50, 2);

-- Component A Criteria (50)
INSERT INTO judging_criteria (criterion_id, component_id, criterion_name, judges_look_for, max_marks, display_order) VALUES
(7, 2, 'Crisis Diagnosis & Prioritisation', NULL, 12, 1),
(8, 2, 'Turnaround Strategy', NULL, 14, 2),
(9, 2, 'Investment Justification & Financial Logic', NULL, 12, 3),
(10, 2, 'Clarity, Confidence & Persuasiveness', NULL, 12, 4);

-- Component B Criteria (50)
INSERT INTO judging_criteria (criterion_id, component_id, criterion_name, judges_look_for, max_marks, display_order) VALUES
(11, 3, 'Budget Allocation Strategy', NULL, 16, 1),
(12, 3, 'Financial Reasoning & Trade-offs', NULL, 12, 2),
(13, 3, 'Risk Management & Financial Sustainability', NULL, 10, 3),
(14, 3, 'Adaptability to Market Shock', NULL, 12, 4);

-- ------------------------------------------------------------
-- ROUND 3: ENMA (Marketing Round, 2 components with Judges Look For)
-- ------------------------------------------------------------
INSERT INTO judging_components (component_id, round_id, component_label, max_marks, display_order) VALUES
(4, 3, 'Component 1 - 4P Pitch', 55, 1),
(5, 3, 'Component 2 - Defend the Ad', 45, 2);

-- Component 1 Criteria (55)
INSERT INTO judging_criteria (criterion_id, component_id, criterion_name, judges_look_for, max_marks, display_order) VALUES
(15, 4, '4Ps', 'All 4 covered, consistent, on-brief.', 15, 1),
(16, 4, 'Target Audience', 'Specific, well-reasoned, not generic.', 10, 2),
(17, 4, 'USP', 'Genuinely differentiating, credible.', 10, 3),
(18, 4, 'Tagline', 'Original, memorable, on-brand.', 10, 4),
(19, 4, 'Presentation & Teamwork', 'Confident delivery, clear roles.', 10, 5);

-- Component 2 Criteria (45)
INSERT INTO judging_criteria (criterion_id, component_id, criterion_name, judges_look_for, max_marks, display_order) VALUES
(20, 5, 'Acknowledging Controversy', 'Addresses it directly, no deflection.', 10, 1),
(21, 5, 'Quality of Defense', 'Coherent rationale, logically sound.', 15, 2),
(22, 5, 'Composure Under Questioning', 'Calm, measured, not rattled.', 10, 3),
(23, 5, 'Crisis-Communication Thinking', 'Real PR instincts, not improvisation.', 10, 4);

-- ------------------------------------------------------------
-- ROUND 4: Round 4 (Empty Shell for Admin to define later)
-- ------------------------------------------------------------
-- No components or criteria inserted for Round 4.
