-- ============================================================
-- PRAGMATRIX 2026 — Migration v4: Optional Team Member Names
-- Adds member2_name and member3_name columns to teams table
-- ============================================================

USE pragmatrix2026;

-- Add nullable columns for optional team members
ALTER TABLE teams
    ADD COLUMN member2_name VARCHAR(150) NULL AFTER lead_email,
    ADD COLUMN member3_name VARCHAR(150) NULL AFTER member2_name;
