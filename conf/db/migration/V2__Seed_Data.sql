-- ===========================================
-- SEED DATA - Itera Catalogs
-- ===========================================

-- 1. Initial Roles (Only if they don't exist)
INSERT INTO roles (name, description) VALUES 
('student', 'Standard student user'),
('mentor', 'Academic mentor'),
('admin', 'System administrator')
ON CONFLICT (name) DO NOTHING;

-- 2. Progress States
INSERT INTO progress_states (name) VALUES 
('pending'),
('in_progress'),
('passed'),
('failed')
ON CONFLICT (name) DO NOTHING;

-- 3. Peruvian Institutions
INSERT INTO institutions (acronym, full_name) VALUES 
('UPC', 'Universidad Peruana de Ciencias Aplicadas'),
('PUCP', 'Pontificia Universidad Católica del Perú'),
('UNMSM', 'Universidad Nacional Mayor de San Marcos'),
('UNI', 'Universidad Nacional de Ingeniería'),
('ULIMA', 'Universidad de Lima'),
('USIL', 'Universidad San Ignacio de Loyola'),
('UP', 'Universidad del Pacífico'),
('UDEP', 'Universidad de Piura'),
('URP', 'Universidad Ricardo Palma'),
('UARM', 'Universidad Antonio Ruiz de Montoya'),
('UTP', 'Universidad Tecnológica del Perú'),
('UCSM', 'Universidad Católica de Santa María'),
('UNFV', 'Universidad Nacional Federico Villarreal'),
('UNALM', 'Universidad Nacional Agraria La Molina'),
('UPCH', 'Universidad Peruana Cayetano Heredia')
ON CONFLICT (acronym) DO NOTHING;
