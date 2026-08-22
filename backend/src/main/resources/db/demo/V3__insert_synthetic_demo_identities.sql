-- Datos exclusivamente sintéticos habilitados solo por los perfiles local y test.
-- La contraseña compartida de estas cuentas de DEMO es: FincoreDemo!2026

insert into app_user (
    id,
    username,
    password_hash,
    status,
    created_at,
    updated_at
)
values
    (
        '10000000-0000-0000-0000-000000000001',
        'customer.one',
        '$2b$12$o8mlAQcAHkOzywQvsn/2IO/0mFRtfokQiPz4KYZWlOyjInsKTvuuG',
        'ACTIVE',
        '2026-01-01T00:00:00Z',
        '2026-01-01T00:00:00Z'
    ),
    (
        '10000000-0000-0000-0000-000000000002',
        'customer.two',
        '$2b$12$o8mlAQcAHkOzywQvsn/2IO/0mFRtfokQiPz4KYZWlOyjInsKTvuuG',
        'ACTIVE',
        '2026-01-01T00:00:00Z',
        '2026-01-01T00:00:00Z'
    ),
    (
        '10000000-0000-0000-0000-000000000003',
        'analyst.demo',
        '$2b$12$o8mlAQcAHkOzywQvsn/2IO/0mFRtfokQiPz4KYZWlOyjInsKTvuuG',
        'ACTIVE',
        '2026-01-01T00:00:00Z',
        '2026-01-01T00:00:00Z'
    ),
    (
        '10000000-0000-0000-0000-000000000004',
        'admin.demo',
        '$2b$12$o8mlAQcAHkOzywQvsn/2IO/0mFRtfokQiPz4KYZWlOyjInsKTvuuG',
        'ACTIVE',
        '2026-01-01T00:00:00Z',
        '2026-01-01T00:00:00Z'
    );

insert into app_user_role (user_id, role)
values
    ('10000000-0000-0000-0000-000000000001', 'CUSTOMER'),
    ('10000000-0000-0000-0000-000000000002', 'CUSTOMER'),
    ('10000000-0000-0000-0000-000000000003', 'ANALYST'),
    ('10000000-0000-0000-0000-000000000004', 'ADMIN');

insert into customer (
    id,
    user_id,
    display_name,
    status,
    created_at,
    updated_at
)
values
    (
        '20000000-0000-0000-0000-000000000001',
        '10000000-0000-0000-0000-000000000001',
        'Cliente Demo Uno',
        'ACTIVE',
        '2026-01-01T00:00:00Z',
        '2026-01-01T00:00:00Z'
    ),
    (
        '20000000-0000-0000-0000-000000000002',
        '10000000-0000-0000-0000-000000000002',
        'Cliente Demo Dos',
        'ACTIVE',
        '2026-01-01T00:00:00Z',
        '2026-01-01T00:00:00Z'
    );
