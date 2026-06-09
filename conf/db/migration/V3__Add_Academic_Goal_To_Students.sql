-- V3: Add academic_goal to students table to support profile themes
ALTER TABLE students ADD COLUMN academic_goal VARCHAR(50) DEFAULT 'General';
