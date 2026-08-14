-- ============================================================
-- PRAGMATRIX 2026 — Migration v3
-- Dropping student name columns, adding team_lead_name,
-- and recreating the leaderboard view.
-- Run this on an existing pragmatrix2026 database.
-- ============================================================

USE pragmatrix2026;

-- Drop old student columns and add team_lead_name
ALTER TABLE teams
  DROP COLUMN IF EXISTS student1_name,
  DROP COLUMN IF EXISTS student2_name,
  DROP COLUMN IF EXISTS student3_name,
  ADD COLUMN team_lead_name VARCHAR(100) NOT NULL AFTER college_name;

-- Recreate the leaderboard view to use team_lead_name
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
