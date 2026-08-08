-- Datos semilla para probar el flujo manualmente en desarrollo.
-- La BD H2 es en memoria y se recrea en cada arranque (ddl-auto=create-drop).

INSERT INTO employee (full_name, email, document_type, document_number, active) VALUES
    ('Laura Traslavina', 'laura.traslavina@horasextra.local', 'CC', '1000000001', true),
    ('Carlos Perez', 'carlos.perez@horasextra.local', 'CC', '1000000002', true),
    ('Maria Gomez', 'maria.gomez@horasextra.local', 'CC', '1000000003', true);

INSERT INTO coordinator (full_name, email, active) VALUES
    ('Ana Rodriguez', 'ana.rodriguez@horasextra.local', true),
    ('Jorge Martinez', 'jorge.martinez@horasextra.local', true);
