-- Cuentas financieras y libro mayor append-only de doble entrada.

create table financial_account (
    id uuid primary key,
    customer_id uuid,
    account_number varchar(20) not null,
    account_kind varchar(20) not null,
    currency varchar(3) not null,
    status varchar(20) not null,
    balance numeric(19, 2) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    version bigint not null default 0,
    constraint uk_financial_account_number unique (account_number),
    constraint fk_financial_account_customer
        foreign key (customer_id) references customer (id) on delete restrict,
    constraint ck_financial_account_kind
        check (account_kind in ('CUSTOMER', 'SYSTEM')),
    constraint ck_financial_account_owner
        check (
            (account_kind = 'CUSTOMER' and customer_id is not null)
            or (account_kind = 'SYSTEM' and customer_id is null)
        ),
    constraint ck_financial_account_currency check (currency in ('PEN', 'USD')),
    constraint ck_financial_account_status check (status in ('ACTIVE', 'SUSPENDED')),
    constraint ck_financial_account_balance
        check (account_kind = 'SYSTEM' or balance >= 0),
    constraint ck_financial_account_scale
        check (balance = round(balance, 2))
);

create table ledger_journal (
    id uuid primary key,
    reference_type varchar(30) not null,
    reference_id uuid not null,
    currency varchar(3) not null,
    description varchar(140) not null,
    created_by varchar(50) not null,
    occurred_at timestamptz not null,
    constraint uk_ledger_journal_reference unique (reference_type, reference_id),
    constraint ck_ledger_journal_reference_type
        check (reference_type in ('OPENING_BALANCE', 'TRANSFER')),
    constraint ck_ledger_journal_currency check (currency in ('PEN', 'USD'))
);

create table ledger_entry (
    id uuid primary key,
    journal_id uuid not null,
    account_id uuid not null,
    entry_type varchar(10) not null,
    amount numeric(19, 2) not null,
    created_at timestamptz not null,
    constraint fk_ledger_entry_journal
        foreign key (journal_id) references ledger_journal (id) on delete restrict,
    constraint fk_ledger_entry_account
        foreign key (account_id) references financial_account (id) on delete restrict,
    constraint ck_ledger_entry_type check (entry_type in ('DEBIT', 'CREDIT')),
    constraint ck_ledger_entry_amount check (amount > 0),
    constraint ck_ledger_entry_scale check (amount = round(amount, 2)),
    constraint uk_ledger_entry_side unique (journal_id, account_id, entry_type)
);

create index ix_financial_account_customer on financial_account (customer_id, currency);
create index ix_financial_account_status on financial_account (status);
create index ix_ledger_journal_occurred_at on ledger_journal (occurred_at desc);
create index ix_ledger_entry_account on ledger_entry (account_id, created_at desc);
create index ix_ledger_entry_journal on ledger_entry (journal_id);

create function reject_ledger_mutation()
returns trigger
language plpgsql
as $$
begin
    raise exception 'El ledger es append-only: no se permiten actualizaciones ni eliminaciones';
end;
$$;

create trigger tr_ledger_journal_immutable
before update or delete on ledger_journal
for each row execute function reject_ledger_mutation();

create trigger tr_ledger_entry_immutable
before update or delete on ledger_entry
for each row execute function reject_ledger_mutation();

create function verify_ledger_journal_balance()
returns trigger
language plpgsql
as $$
declare
    checked_journal_id uuid;
    debit_total numeric(19, 2);
    credit_total numeric(19, 2);
    entry_count integer;
begin
    -- NEW tiene una forma distinta en cada tabla; IF evita intentar leer un campo inexistente.
    if tg_table_name = 'ledger_journal' then
        checked_journal_id := new.id;
    else
        checked_journal_id := new.journal_id;
    end if;

    select
        coalesce(sum(amount) filter (where entry_type = 'DEBIT'), 0),
        coalesce(sum(amount) filter (where entry_type = 'CREDIT'), 0),
        count(*)
    into debit_total, credit_total, entry_count
    from ledger_entry
    where journal_id = checked_journal_id;

    if entry_count < 2 or debit_total <> credit_total then
        raise exception 'El journal % no está balanceado', checked_journal_id;
    end if;

    return null;
end;
$$;

create constraint trigger tr_ledger_journal_balance_from_journal
after insert on ledger_journal
deferrable initially deferred
for each row execute function verify_ledger_journal_balance();

create constraint trigger tr_ledger_journal_balance_from_entry
after insert on ledger_entry
deferrable initially deferred
for each row execute function verify_ledger_journal_balance();
