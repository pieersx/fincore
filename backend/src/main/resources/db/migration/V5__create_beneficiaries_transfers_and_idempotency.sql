-- Beneficiarios, transferencias confirmadas e idempotencia persistida.

create table beneficiary (
    id uuid primary key,
    owner_customer_id uuid not null,
    destination_account_id uuid not null,
    alias varchar(80) not null,
    created_at timestamptz not null,
    deleted_at timestamptz,
    version bigint not null default 0,
    constraint fk_beneficiary_owner
        foreign key (owner_customer_id) references customer (id) on delete restrict,
    constraint fk_beneficiary_destination
        foreign key (destination_account_id) references financial_account (id) on delete restrict,
    constraint ck_beneficiary_alias check (length(trim(alias)) between 2 and 80)
);

create unique index uk_beneficiary_active_destination
    on beneficiary (owner_customer_id, destination_account_id)
    where deleted_at is null;
create index ix_beneficiary_owner on beneficiary (owner_customer_id, created_at desc);

create table financial_transfer (
    id uuid primary key,
    reference varchar(30) not null,
    created_by_user_id uuid not null,
    source_account_id uuid not null,
    destination_account_id uuid not null,
    beneficiary_id uuid not null,
    currency varchar(3) not null,
    amount numeric(19, 2) not null,
    status varchar(20) not null,
    description varchar(140),
    created_at timestamptz not null,
    completed_at timestamptz not null,
    constraint uk_financial_transfer_reference unique (reference),
    constraint fk_financial_transfer_user
        foreign key (created_by_user_id) references app_user (id) on delete restrict,
    constraint fk_financial_transfer_source
        foreign key (source_account_id) references financial_account (id) on delete restrict,
    constraint fk_financial_transfer_destination
        foreign key (destination_account_id) references financial_account (id) on delete restrict,
    constraint fk_financial_transfer_beneficiary
        foreign key (beneficiary_id) references beneficiary (id) on delete restrict,
    constraint ck_financial_transfer_accounts
        check (source_account_id <> destination_account_id),
    constraint ck_financial_transfer_currency check (currency in ('PEN', 'USD')),
    constraint ck_financial_transfer_amount check (amount > 0),
    constraint ck_financial_transfer_scale check (amount = round(amount, 2)),
    constraint ck_financial_transfer_status check (status = 'CONFIRMED')
);

create index ix_financial_transfer_user
    on financial_transfer (created_by_user_id, created_at desc);
create index ix_financial_transfer_source
    on financial_transfer (source_account_id, created_at desc);
create index ix_financial_transfer_destination
    on financial_transfer (destination_account_id, created_at desc);
create index ix_financial_transfer_completed
    on financial_transfer (completed_at desc);

create function reject_confirmed_transfer_mutation()
returns trigger
language plpgsql
as $$
begin
    raise exception 'Una transferencia confirmada no se puede actualizar ni eliminar';
end;
$$;

create trigger tr_financial_transfer_immutable
before update or delete on financial_transfer
for each row execute function reject_confirmed_transfer_mutation();

create table transfer_idempotency (
    id uuid primary key,
    actor_user_id uuid not null,
    idempotency_key varchar(100) not null,
    request_hash char(64) not null,
    transfer_id uuid,
    created_at timestamptz not null,
    completed_at timestamptz,
    constraint fk_transfer_idempotency_user
        foreign key (actor_user_id) references app_user (id) on delete restrict,
    constraint fk_transfer_idempotency_transfer
        foreign key (transfer_id) references financial_transfer (id) on delete restrict,
    constraint uk_transfer_idempotency_actor_key unique (actor_user_id, idempotency_key),
    constraint ck_transfer_idempotency_key
        check (idempotency_key ~ '^[A-Za-z0-9._:-]{8,100}$'),
    constraint ck_transfer_idempotency_hash
        check (request_hash ~ '^[0-9a-f]{64}$'),
    constraint ck_transfer_idempotency_completion
        check (
            (transfer_id is null and completed_at is null)
            or (transfer_id is not null and completed_at is not null)
        )
);
