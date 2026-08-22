-- Mantiene cada prueba independiente de usuarios registrados por pruebas anteriores.
delete from beneficiary
where owner_customer_id not in (
    '20000000-0000-0000-0000-000000000001',
    '20000000-0000-0000-0000-000000000002'
);

-- Las cuentas creadas por el onboarding empiezan en cero y todavía no tienen ledger.
delete from financial_account
where customer_id is not null
  and customer_id not in (
    '20000000-0000-0000-0000-000000000001',
    '20000000-0000-0000-0000-000000000002'
);

delete from customer
where user_id not in (
    '10000000-0000-0000-0000-000000000001',
    '10000000-0000-0000-0000-000000000002'
);

delete from app_user_role
where user_id not in (
    '10000000-0000-0000-0000-000000000001',
    '10000000-0000-0000-0000-000000000002',
    '10000000-0000-0000-0000-000000000003',
    '10000000-0000-0000-0000-000000000004'
);

delete from app_user
where id not in (
    '10000000-0000-0000-0000-000000000001',
    '10000000-0000-0000-0000-000000000002',
    '10000000-0000-0000-0000-000000000003',
    '10000000-0000-0000-0000-000000000004'
);

delete from audit_event;

update app_user
set status = 'ACTIVE', updated_at = '2026-01-01T00:00:00Z', version = 0;

update customer
set status = 'ACTIVE', updated_at = '2026-01-01T00:00:00Z', version = 0;

update financial_account
set status = 'ACTIVE'
where account_kind = 'CUSTOMER';
