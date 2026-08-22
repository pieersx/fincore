-- Crea las estructuras de identidad, clientes y auditoría del incremento v0.3.0.
-- Las demás funcionalidades referenciarán estos identificadores sin compartir entidades JPA.

create table app_user (
    id uuid primary key,
    username varchar(50) not null,
    password_hash varchar(100) not null,
    status varchar(20) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    version bigint not null default 0,
    constraint uk_app_user_username unique (username),
    constraint ck_app_user_username_lowercase check (username = lower(username)),
    constraint ck_app_user_username_format check (username ~ '^[a-z0-9._-]{4,50}$'),
    constraint ck_app_user_status check (status in ('ACTIVE', 'SUSPENDED'))
);

create table app_user_role (
    user_id uuid not null,
    role varchar(20) not null,
    primary key (user_id, role),
    constraint fk_app_user_role_user
        foreign key (user_id) references app_user (id) on delete cascade,
    constraint ck_app_user_role check (role in ('CUSTOMER', 'ANALYST', 'ADMIN'))
);

create table customer (
    id uuid primary key,
    user_id uuid not null,
    display_name varchar(120) not null,
    status varchar(20) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    version bigint not null default 0,
    constraint uk_customer_user unique (user_id),
    constraint fk_customer_user
        foreign key (user_id) references app_user (id) on delete restrict,
    constraint ck_customer_display_name check (length(trim(display_name)) >= 2),
    constraint ck_customer_status check (status in ('ACTIVE', 'SUSPENDED'))
);

create table audit_event (
    id uuid primary key,
    actor_username varchar(50),
    action varchar(60) not null,
    outcome varchar(20) not null,
    resource_type varchar(60),
    resource_id varchar(100),
    correlation_id varchar(100),
    detail varchar(500),
    occurred_at timestamptz not null,
    constraint ck_audit_event_outcome check (outcome in ('SUCCESS', 'FAILURE', 'DENIED'))
);

create index ix_app_user_status on app_user (status);
create index ix_customer_status on customer (status);
create index ix_audit_event_occurred_at on audit_event (occurred_at desc);
create index ix_audit_event_actor on audit_event (actor_username, occurred_at desc);
create index ix_audit_event_correlation on audit_event (correlation_id);
