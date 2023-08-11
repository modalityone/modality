SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

create table kdm_center (
    id                  serial,
	kdm_id			    int not null,
    name                varchar(255) not null,
    lat                 real,
    lng                 real,
    type                varchar(64) not null,
    mothercenter        bool not null default false,
    address             varchar(255),
    address2            varchar(255),
    address3            varchar(255),
    city                varchar(255),
    state               varchar(255),
    postal              varchar(255),
    email               varchar(255),
    phone               varchar(255),
    photo               varchar(1024),
    web                 varchar(1024),

    primary key(id)
 );