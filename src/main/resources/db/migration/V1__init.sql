create table adapter
(
    dn                bytea   not null,
    client_id         varchar(255),
    client_secret     varchar(512),
    is_managed        boolean not null,
    name              varchar(255),
    note              varchar(4096),
    password          varchar(512),
    public_key        varchar(512),
    short_description varchar(255),
    primary key (dn)
);
create table adapter_access_packages
(
    adapter_dn      bytea not null,
    access_packages varchar(255)
);
create table adapter_asset_ids
(
    adapter_dn bytea not null,
    asset_ids  varchar(255)
);
create table adapter_assets
(
    adapter_dn bytea not null,
    assets     varchar(255)
);
create table adapter_components
(
    adapter_dn bytea not null,
    components varchar(255)
);
create table client
(
    dn                bytea   not null,
    asset             varchar(255),
    asset_id          varchar(255),
    client_id         varchar(255),
    client_secret     varchar(512),
    is_managed        boolean not null,
    name              varchar(255),
    note              varchar(4096),
    password          varchar(512),
    public_key        varchar(512),
    short_description varchar(255),
    primary key (dn)
);
create table client_access_packages
(
    client_dn       bytea not null,
    access_packages varchar(255)
);
create table client_components
(
    client_dn  bytea not null,
    components varchar(255)
);
alter table adapter_access_packages
    add constraint FKim2s0gwv2lf1wwj62frbaqilk foreign key (adapter_dn) references adapter;
alter table adapter_asset_ids
    add constraint FK85rj21lrsm05ffsn9qlkxlcuh foreign key (adapter_dn) references adapter;
alter table adapter_assets
    add constraint FK70t6l9lu5eagpm1rkwc8aisc0 foreign key (adapter_dn) references adapter;
alter table adapter_components
    add constraint FKoreqavdmvencyhby3qu5nf2nc foreign key (adapter_dn) references adapter;
alter table client_access_packages
    add constraint FKb08q8u0se8rcfpqtrblxvb5b0 foreign key (client_dn) references client;
alter table client_components
    add constraint FK9gv28o6l5waprhykl59ixl11v foreign key (client_dn) references client;
