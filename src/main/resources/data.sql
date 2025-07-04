INSERT INTO users (email, password, role) -- BCrypt version of admin123
SELECT 'admin@example.com', '$2a$12$4wWL.lnRYIRwBCy8JVkMquqK1oE36AUoZYwApAKRPP1JnsQXoFHSC', 'ROLE_ADMIN'
    WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'admin@example.com'
);
