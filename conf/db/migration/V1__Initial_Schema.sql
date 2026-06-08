-- ===========================================
-- INITIAL SCHEMA - Itera
-- ===========================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ===========================================
-- MAINTENANCE TABLES (CATALOGS)
-- ===========================================

CREATE TABLE IF NOT EXISTS roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(150),
    status VARCHAR(20) DEFAULT 'active'
);

COMMENT ON COLUMN roles.id IS 'Generated preferably as UUIDv4 or v7';
COMMENT ON COLUMN roles.name IS 'E.g., student, mentor, admin';

CREATE TABLE IF NOT EXISTS institutions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    acronym VARCHAR(20) UNIQUE NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    status VARCHAR(20) DEFAULT 'active'
);

COMMENT ON COLUMN institutions.acronym IS 'E.g., UPC';

CREATE TABLE IF NOT EXISTS progress_states (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) UNIQUE NOT NULL
);

COMMENT ON COLUMN progress_states.name IS 'pending, in_progress, passed';

-- =========================================
-- SECURITY CONTEXT (AUTH)
-- =========================================

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role_id UUID NOT NULL REFERENCES roles(id),
    plan VARCHAR(20) DEFAULT 'basic',
    status VARCHAR(20) DEFAULT 'active',
    refresh_token VARCHAR(255),
    last_access TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================================
-- ACADEMIC CONTEXT (CORE)
-- =========================================

CREATE TABLE IF NOT EXISTS students (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE NOT NULL REFERENCES users(id),
    institution_id UUID REFERENCES institutions(id),
    names VARCHAR(100) NOT NULL,
    surnames VARCHAR(100) NOT NULL,
    cycle INT DEFAULT 1
);

CREATE TABLE IF NOT EXISTS study_plans (
    student_id UUID PRIMARY KEY REFERENCES students(id),
    objective VARCHAR(150) NOT NULL,
    target_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    goal_date TIMESTAMP,
    hours_per_week INT
);

CREATE TABLE IF NOT EXISTS profiles (
    student_id UUID PRIMARY KEY REFERENCES students(id),
    photo VARCHAR(255),
    experience INT DEFAULT 0,
    preferences JSONB,
    skills JSONB,
    badges JSONB
);

CREATE TABLE IF NOT EXISTS progress (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL REFERENCES students(id),
    node_id VARCHAR(50) NOT NULL,
    status_id UUID NOT NULL REFERENCES progress_states(id),
    grade NUMERIC(4,2),
    evidence VARCHAR(255),
    attempts INT DEFAULT 1,
    origin VARCHAR(50) DEFAULT 'manual',
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    UNIQUE(student_id, node_id)
);
