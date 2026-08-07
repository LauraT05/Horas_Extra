-- Datos semilla para probar el flujo manualmente en desarrollo.
-- La BD H2 es en memoria y se recrea en cada arranque (ddl-auto=create-drop).

INSERT INTO employee (full_name, email, active) VALUES
    ('Laura Traslavina', 'laura.traslavina@horasextra.local', true),
    ('Carlos Perez', 'carlos.perez@horasextra.local', true),
    ('Maria Gomez', 'maria.gomez@horasextra.local', true);

INSERT INTO coordinator (full_name, email, active) VALUES
    ('Ana Rodriguez', 'ana.rodriguez@horasextra.local', true),
    ('Jorge Martinez', 'jorge.martinez@horasextra.local', true);
