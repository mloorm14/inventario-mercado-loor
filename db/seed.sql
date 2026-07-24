-- Datos semilla de usuarios para pruebas de autenticación JWT (paso 3).
--
-- Contraseñas en texto plano (SOLO para pruebas locales, NO usar en producción):
--   admin.mercado   -> Admin#Quevedo2026
--   usuario.mercado -> User#Quevedo2026
--
-- Los hashes fueron generados con BCrypt (factor de costo 10) y verificados
-- con un round-trip real antes de insertarlos aquí.

INSERT INTO usuarios (username, password_hash, role) VALUES
    ('admin.mercado', '$2b$10$/dNQP6JVPUOeV6GLTzmx..W/tDPiNUfHiM54YHEkhY8FkHEJUF7ve', 'ROLE_ADMIN'),
    ('usuario.mercado', '$2b$10$3s8wEGOPyH1YozRjGoDIXO3k6ZGqEIBh4Tv90PJkAWXOL/Mm./aOO', 'ROLE_USER');
