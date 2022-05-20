--
-- PostgreSQL database dump
--

-- Dumped from database version 12.10 (Ubuntu 12.10-1.pgdg21.10+1)
-- Dumped by pg_dump version 12.10 (Ubuntu 12.10-1.pgdg21.10+1)

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

--
-- Name: adminpack; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS adminpack WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION adminpack; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION adminpack IS 'administrative functions for PostgreSQL';


--
-- Name: hstore; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS hstore WITH SCHEMA public;


--
-- Name: EXTENSION hstore; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION hstore IS 'data type for storing sets of (key, value) pairs';


--
-- Name: pgcrypto; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA public;


--
-- Name: EXTENSION pgcrypto; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION pgcrypto IS 'cryptographic functions';


--
-- Name: unaccent; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS unaccent WITH SCHEMA public;


--
-- Name: EXTENSION unaccent; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION unaccent IS 'text search dictionary that removes accents';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: attendance; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.attendance (
    id integer NOT NULL,
    document_line_id integer NOT NULL,
    date date NOT NULL,
    "time" time without time zone,
    rate_id integer,
    present boolean DEFAULT true NOT NULL,
    charged boolean DEFAULT true NOT NULL,
    rate_group smallint
);


ALTER TABLE public.attendance OWNER TO kbs;

--
-- Name: document; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.document (
    id integer NOT NULL,
    creation_date timestamp without time zone DEFAULT now() NOT NULL,
    accounting_date date DEFAULT ('now'::text)::date NOT NULL,
    organization_id integer NOT NULL,
    expenditure boolean DEFAULT false NOT NULL,
    ref integer,
    activity_id integer NOT NULL,
    event_id integer,
    multiple_booking_id integer,
    cart_id integer,
    third_party_id integer,
    person_id integer,
    person_organization_id integer,
    person_branch_id integer,
    person_third_party_id integer,
    person_language_id integer,
    person_country_name character varying(64),
    person_country_geonameid integer,
    person_country_id integer,
    person_post_code character varying(16),
    person_city_name character varying(64),
    person_city_geonameid integer,
    person_city_latitude double precision,
    person_city_longitude double precision,
    person_city_timezone character varying(40),
    person_street character varying(128),
    person_latitude double precision,
    person_longitude double precision,
    person_name character varying(91),
    person_first_name character varying(45),
    person_last_name character varying(45),
    person_lay_name character varying(91),
    person_abc_names character varying(91),
    person_male boolean DEFAULT false NOT NULL,
    person_ordained boolean DEFAULT false NOT NULL,
    person_carer1_name character varying(91),
    person_carer1_id integer,
    person_carer1_document_id integer,
    person_carer2_name character varying(91),
    person_carer2_id integer,
    person_carer2_document_id integer,
    person_email character varying(127),
    person_phone character varying(45),
    inet boolean DEFAULT false NOT NULL,
    inet_ip inet,
    inet_geo_latitude double precision,
    inet_geo_longitude double precision,
    cancelled boolean DEFAULT false NOT NULL,
    abandoned boolean DEFAULT false NOT NULL,
    confirmed boolean DEFAULT false NOT NULL,
    changed boolean DEFAULT false NOT NULL,
    arrived boolean DEFAULT false NOT NULL,
    absent boolean DEFAULT false NOT NULL,
    read boolean DEFAULT false NOT NULL,
    dates character varying(127),
    request character varying(1024),
    comment character varying(1024),
    special_needs character varying(255),
    price_net integer DEFAULT 0 NOT NULL,
    price_min_deposit integer,
    price_non_refundable integer,
    price_deposit integer DEFAULT 0 NOT NULL,
    trigger_defer_compute_prices boolean DEFAULT false NOT NULL,
    trigger_defer_compute_deposit boolean DEFAULT false NOT NULL,
    person_age smallint,
    person_admin1_name character varying(255),
    person_admin1_code character varying(20),
    person_admin1_geonameid integer,
    person_admin2_name character varying(255),
    person_admin2_code character varying(80),
    person_admin2_geonameid integer,
    person_country_code character(2),
    person_passport character varying(64),
    person_lang character(2) DEFAULT 'en'::bpchar,
    person_nationality character varying(64),
    trigger_check_multiple_booking boolean DEFAULT false,
    trigger_send_letter_id integer,
    person_resident boolean DEFAULT false NOT NULL,
    paper boolean DEFAULT false NOT NULL,
    person_resident2 boolean DEFAULT false NOT NULL,
    person_verified boolean DEFAULT false NOT NULL,
    flagged boolean DEFAULT false NOT NULL,
    will_pay boolean DEFAULT false NOT NULL,
    person_photo_received boolean DEFAULT false NOT NULL,
    person_known boolean DEFAULT false NOT NULL,
    person_unknown boolean DEFAULT false NOT NULL,
    trigger_defer_compute_lines_deposit boolean DEFAULT false NOT NULL,
    pass_ready boolean DEFAULT false NOT NULL,
    cancellation_date timestamp without time zone,
    not_multiple_booking_id integer,
    trigger_cancel_other_multiple_bookings boolean DEFAULT false NOT NULL,
    trigger_merge_from_other_multiple_bookings boolean DEFAULT false NOT NULL,
    trigger_transfer_from_other_multiple_bookings boolean DEFAULT false NOT NULL,
    trigger_send_system_letter_id integer,
    person_organization_name character varying(255),
    trigger_defer_compute_dates boolean DEFAULT false NOT NULL,
    person_unemployed boolean DEFAULT false NOT NULL,
    person_facility_fee boolean DEFAULT false NOT NULL,
    person_discovery boolean DEFAULT false NOT NULL,
    person_discovery_reduced boolean DEFAULT false NOT NULL,
    person_working_visit boolean DEFAULT false NOT NULL,
    person_guest boolean DEFAULT false NOT NULL
);


ALTER TABLE public.document OWNER TO kbs;

--
-- Name: document_line; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.document_line (
    id integer NOT NULL,
    document_id integer NOT NULL,
    item_id integer,
    stock_transfer_id integer,
    site_id integer,
    budget_line_id integer,
    quantity integer DEFAULT 1 NOT NULL,
    purchase_rate_id integer,
    purchase_price_novat integer,
    purchase_vat_id integer,
    purchase_price integer,
    rate_id integer,
    price_novat integer,
    vat_id integer,
    price integer,
    price_discount smallint,
    price_is_custom boolean DEFAULT false NOT NULL,
    price_net integer,
    price_min_deposit integer,
    price_non_refundable integer,
    resource_id integer,
    share_owner boolean DEFAULT false NOT NULL,
    share_owner_quantity integer DEFAULT 1 NOT NULL,
    share_mate boolean DEFAULT false NOT NULL,
    share_mate_owner_name character varying(91),
    share_mate_owner_person_id integer,
    share_mate_owner_document_line_id integer,
    share_mate_charged boolean DEFAULT false NOT NULL,
    cancelled boolean DEFAULT false NOT NULL,
    dates character varying(127),
    trigger_defer_allocate boolean DEFAULT false NOT NULL,
    allocate boolean DEFAULT false NOT NULL,
    share_owner_mate1_name character varying(91),
    share_owner_mate2_name character varying(91),
    share_owner_mate3_name character varying(91),
    share_owner_mate4_name character varying(91),
    share_owner_mate5_name character varying(91),
    abandoned boolean DEFAULT false NOT NULL,
    share_owner_mate6_name character varying(91),
    share_owner_mate7_name character varying(91),
    backend_released boolean DEFAULT false NOT NULL,
    frontend_released boolean DEFAULT false NOT NULL,
    private boolean DEFAULT false NOT NULL,
    lock_allocation boolean DEFAULT false NOT NULL,
    read boolean DEFAULT false NOT NULL,
    cancellation_date date,
    creation_date date DEFAULT ('now'::text)::date NOT NULL,
    price_deposit integer DEFAULT 0 NOT NULL,
    resource_configuration_id integer,
    comment character varying(1024),
    system_allocated boolean DEFAULT false NOT NULL,
    price_custom integer,
    arrival_site_id integer,
    group_site_id integer,
    group_item_id integer,
    group_label_id integer,
    option_id integer,
    group_family_id integer,
    driver_id integer,
    confirmation_request_sent timestamp without time zone,
    confirmation_received timestamp without time zone
);


ALTER TABLE public.document_line OWNER TO kbs;

--
-- Name: COLUMN document_line.group_site_id; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.document_line.group_site_id IS 'Id of site in which to group the document line.';


--
-- Name: COLUMN document_line.group_item_id; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.document_line.group_item_id IS 'Id of item in which to group the document line.';


--
-- Name: COLUMN document_line.group_label_id; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.document_line.group_label_id IS 'Id of label in which to group the document line. Overrides group_item_id.';


--
-- Name: COLUMN document_line.option_id; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.document_line.option_id IS 'Id of option ticked to book this document line.';


--
-- Name: COLUMN document_line.group_family_id; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.document_line.group_family_id IS 'Id of item_family in which to group the document line. Overides item''s family.';


--
-- Name: rate; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.rate (
    id integer NOT NULL,
    item_id integer NOT NULL,
    sale boolean DEFAULT true NOT NULL,
    package_id integer,
    site_id integer,
    live boolean DEFAULT true NOT NULL,
    comment character varying(1024),
    start_date date,
    end_date date,
    currency_id integer,
    price_novat integer,
    vat_id integer,
    price integer,
    per_day boolean DEFAULT false NOT NULL,
    first_day date,
    last_day date,
    min_day integer,
    max_day integer,
    min_deposit integer,
    non_refundable integer,
    organization_id integer,
    max_age smallint,
    age1_max smallint,
    age1_price integer,
    age1_discount smallint,
    age2_max smallint,
    age2_price integer,
    age2_discount smallint,
    age3_max smallint,
    age3_price integer,
    age3_discount smallint,
    per_person boolean DEFAULT false NOT NULL,
    min_age smallint,
    resident_price integer,
    resident_discount smallint,
    arriving_or_leaving boolean DEFAULT false NOT NULL,
    resident2_price integer,
    resident2_discount smallint,
    on_date date,
    off_date date,
    cutoff_date date,
    min_deposit2 smallint,
    non_refundable2 smallint,
    cutoff_date2 date,
    min_deposit3 smallint,
    non_refundable3 smallint,
    cutoff_date3 date,
    min_deposit4 smallint,
    non_refundable4 smallint,
    cutoff_date4 date,
    min_deposit5 smallint,
    non_refundable5 smallint,
    cutoff_date5 date,
    min_deposit6 smallint,
    non_refundable6 smallint,
    unemployed_price integer,
    unemployed_discount smallint,
    facility_fee_price integer,
    facility_fee_discount smallint,
    discovery_discount smallint,
    discovery_price integer,
    discovery_reduced_discount smallint,
    discovery_reduced_price integer,
    working_visit_discount smallint,
    working_visit_price integer,
    guest_discount smallint,
    guest_price integer,
    arrival_site_id integer,
    min_day_ceiling boolean DEFAULT false NOT NULL,
    comment_label_id integer,
    bookings_updated timestamp without time zone
);


ALTER TABLE public.rate OWNER TO kbs;

--
-- Name: COLUMN rate.bookings_updated; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.rate.bookings_updated IS 'Indicates when was the last time related bookings minDeposit have been updated';


--
-- Name: compute_price_record; Type: TYPE; Schema: public; Owner: kbs
--

CREATE TYPE public.compute_price_record AS (
	d public.document,
	dl public.document_line,
	rate_item_id integer,
	rate_date date,
	rates public.rate[],
	document_line_index integer,
	document_lines_count integer,
	a public.attendance
);


ALTER TYPE public.compute_price_record OWNER TO kbs;

--
-- Name: resource_availibility; Type: TYPE; Schema: public; Owner: kbs
--

CREATE TYPE public.resource_availibility AS (
	row_number integer,
	site_id integer,
	item_id integer,
	date date,
	current integer,
	max integer
);


ALTER TYPE public.resource_availibility OWNER TO kbs;

--
-- Name: resource_capacity; Type: TYPE; Schema: public; Owner: kbs
--

CREATE TYPE public.resource_capacity AS (
	row_number integer,
	site_id integer,
	item_id integer,
	capacity integer,
	count integer,
	free integer
);


ALTER TYPE public.resource_capacity OWNER TO kbs;

--
-- Name: abc_names(text); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.abc_names(text) RETURNS text
    LANGUAGE plpgsql IMMUTABLE
    AS $_$
BEGIN
    return ' ' || array_to_string(array(select alpha from (select regexp_split_to_table(lower(unaccent(translate($1, '#,', '  '))), E'[\\s,-]+') as alpha order by alpha) a where not alpha ~ '^(-)?[0-9]+$'), ' ');
END; $_$;


ALTER FUNCTION public.abc_names(text) OWNER TO kbs;

--
-- Name: array_length(anyarray); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.array_length(anyarray) RETURNS integer
    LANGUAGE plpgsql
    AS $_$
BEGIN
	return coalesce(array_length($1, 1), 0);
END
$_$;


ALTER FUNCTION public.array_length(anyarray) OWNER TO kbs;

--
-- Name: money_transfer; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.money_transfer (
    id integer NOT NULL,
    parent_id integer,
    document_id integer,
    date timestamp without time zone DEFAULT now() NOT NULL,
    method_id integer NOT NULL,
    from_money_account_id integer,
    to_money_account_id integer,
    payment boolean DEFAULT true NOT NULL,
    refund boolean DEFAULT false NOT NULL,
    amount integer DEFAULT 0 NOT NULL,
    pending boolean DEFAULT false NOT NULL,
    successful boolean DEFAULT true NOT NULL,
    from_money_account_balance integer,
    to_money_account_balance integer,
    statement_id integer,
    spread boolean DEFAULT false NOT NULL,
    trigger_defer_update_spread_money_transfer boolean DEFAULT false NOT NULL,
    transaction_ref character varying(255),
    gateway_company_id integer,
    receipts_transfer boolean DEFAULT false NOT NULL,
    transfer_id integer,
    expected_amount integer,
    method_not_circled boolean DEFAULT false NOT NULL,
    comment character varying(1024),
    read boolean DEFAULT false NOT NULL,
    status character varying(64),
    verified boolean DEFAULT false NOT NULL,
    verifier character varying(45),
    gateway_accesscode character varying(512),
    gateway_response character varying(10240),
    gateway_generate_payment_url_response character varying(10240),
    CONSTRAINT check_document CHECK ((((spread OR ((payment = false) AND (refund = false))) AND (document_id IS NULL)) OR ((NOT spread) AND (document_id IS NOT NULL)))),
    CONSTRAINT check_parent CHECK (((spread AND (parent_id IS NULL)) OR (NOT spread)))
);


ALTER TABLE public.money_transfer OWNER TO kbs;

--
-- Name: COLUMN money_transfer.gateway_accesscode; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.money_transfer.gateway_accesscode IS 'Gateway pre-transaction unique identifier';


--
-- Name: COLUMN money_transfer.gateway_response; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.money_transfer.gateway_response IS 'Transaction result response from the gateway';


--
-- Name: COLUMN money_transfer.gateway_generate_payment_url_response; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.money_transfer.gateway_generate_payment_url_response IS 'Unique payment url generation response from the gateway';


--
-- Name: autoset_money_transfer_from_account(public.money_transfer, boolean); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.autoset_money_transfer_from_account(mt public.money_transfer, update_money_transfer boolean) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	ma_id int4 := null;
	eventid int4;
BEGIN

		IF (not mt.spread) THEN
			SELECT INTO eventid d.event_id from document d where d.id=mt.document_id;
		ELSE
			SELECT INTO eventid d.event_id from money_transfer mtc join document d on d.id=mtc.document_id where mtc.parent_id=mt.id limit 1;
		END IF;

      -- select MoneyAccount ma
			-- where type.internal=?thisMoneyTransfer.(payment=false or refund<>document.expenditure)
			-- and type.customer=?thisMoneyTransfer.(payment=true and document.expenditure=false and refund=false)
         -- and type.supplier=?thisMoneyTransfer.(payment=true and document.expenditure=true and refund=true)
         -- and exists(select MethodSupport ms where ms.moneyAccountType=ma.type and ms.method=?thisMoneyTransfer.method)			
		SELECT INTO ma_id ma.id FROM money_account as ma
			JOIN money_account_type as mat on ma.type_id=mat.id
			, document as d
			WHERE
				not ma.closed
				AND (ma.event_id is null or ma.event_id = eventid)
				AND ma.organization_id = d.organization_id				
				AND (not mt.spread and d.id=mt.document_id or mt.spread and (exists(select * from money_transfer c where c.parent_id=mt.id and c.document_id=d.id)))
				AND mat.internal = (mt.payment=false OR mt.refund<>d.expenditure)
				AND mat.customer = (mt.payment=true AND d.expenditure=false AND mt.refund=false)
				AND mat.supplier = (mt.payment=true AND d.expenditure=true AND mt.refund=true)
				AND EXISTS(SELECT * FROM method_support as ms WHERE ms.money_account_type_id=mat.id AND ms.method_id=mt.method_id)
			ORDER BY CASE WHEN ma.event_id=eventid THEN 0 ELSE ma.id END
			LIMIT 1;

		IF NOT FOUND and mt.method_id<>6 THEN -- 6 is Contra method used for transfers and don't need a money account associated
			RAISE EXCEPTION 'No open money account found for this payment';
		END IF;

		IF (update_money_transfer and (ma_id is null and mt.from_money_account_id is not null or ma_id is not null and (mt.from_money_account_id is null or mt.from_money_account_id<>ma_id))) THEN
			update money_transfer set from_money_account_id = ma_id where id=mt.id;
		END IF;
 
		RETURN ma_id;
END;
$$;


ALTER FUNCTION public.autoset_money_transfer_from_account(mt public.money_transfer, update_money_transfer boolean) OWNER TO kbs;

--
-- Name: autoset_money_transfer_to_account(public.money_transfer, boolean); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.autoset_money_transfer_to_account(mt public.money_transfer, update_money_transfer boolean) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	ma_id int4;eventid int4;BEGIN

		IF (not mt.spread) THEN
			SELECT INTO eventid d.event_id from document d where d.id=mt.document_id;ELSE
			SELECT INTO eventid d.event_id from money_transfer mtc join document d on d.id=mtc.document_id where mtc.parent_id=mt.id limit 1;END IF;-- select MoneyAccount ma
      	-- where type.internal=?thisMoneyTransfer.(payment=false or refund=document.expenditure)
			-- and type.customer=?thisMoneyTransfer.(payment=true and document.expenditure=false and refund=true)
			-- and type.supplier=?thisMoneyTransfer.(payment=true and document.expenditure=true and refund=false)
			-- and exists(select MethodSupport ms where ms.moneyAccountType=ma.type and ms.method=?thisMoneyTransfer.method)
			-- and exists(select MoneyFlow mf where mf.fromMoneyAccount=?thisMoneyTransfer.fromMoneyAccount and mf.toMoneyAccount=ma and (mf.method=null or mf.method=?thisMoneyTransfer.method))
		SELECT INTO ma_id ma.id FROM money_account as ma
			JOIN money_account_type as mat on ma.type_id=mat.id
			--LEFT JOIN money_account_priority as map on map.account_id=ma.id and username=(select username from sys_sync limit 1)
			LEFT JOIN money_flow as mf on mf.from_money_account_id=mt.from_money_account_id and mf.to_money_account_id=ma.id
			LEFT JOIN money_flow_priority as mfp on mfp.flow_id=mf.id and username=(select username from sys_sync limit 1)
			, document as d
			WHERE
				ma.closed=false
				AND ma.organization_id = d.organization_id
				AND (not mt.spread and d.id=mt.document_id or mt.spread and (exists(select * from money_transfer c where c.parent_id=mt.id and c.document_id=d.id)))
				AND mat.internal = (mt.payment=false OR mt.refund=d.expenditure)
				AND mat.customer = (mt.payment=true AND d.expenditure=false AND mt.refund=true)
				AND mat.supplier = (mt.payment=true AND d.expenditure=true AND mt.refund=false)
				AND EXISTS(SELECT * FROM method_support as ms WHERE ms.money_account_type_id=mat.id AND ms.method_id=mt.method_id)
				AND EXISTS(SELECT * FROM money_flow as mf WHERE mf.from_money_account_id=mt.from_money_account_id AND mf.to_money_account_id=ma.id AND (mf.method_id is null OR mf.method_id=mt.method_id) AND (mt.amount >=0 AND mf.positive_amounts OR mt.amount < 0 and mf.negative_amounts))
			ORDER BY mfp.ord NULLS LAST, CASE WHEN ma.event_id=eventid THEN 0 ELSE 1 END, ma.id
			LIMIT 1;IF NOT FOUND and mt.method_id<>6 THEN -- 6 is Contra method used for transfers and don't need a money account associated
			RAISE EXCEPTION 'No open money account found for this payment';END IF;IF (update_money_transfer and (ma_id is null and mt.to_money_account_id is not null or ma_id is not null and (mt.to_money_account_id is null or mt.to_money_account_id<>ma_id))) THEN
			update money_transfer set to_money_account_id = ma_id where id=mt.id;END IF;RETURN ma_id;END;$$;


ALTER FUNCTION public.autoset_money_transfer_to_account(mt public.money_transfer, update_money_transfer boolean) OWNER TO kbs;

--
-- Name: bookings_auto_confirm(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.bookings_auto_confirm() RETURNS integer
    LANGUAGE plpgsql
    AS $$
  DECLARE
    updated integer;
	BEGIN

with
-- Select the bookings to auto confirm
bookings as (
	select
		DISTINCT ON (d1.id)
		d1.id as document_id,
		e1.bookings_auto_confirm_letter_id as letter_id,
		e2.id as ref_event_id,
		e2.name as ref_event_name,
		d2.ref as ref_document_ref
	from
		"document" d1
	join "event" e1 on
		d1.event_id = e1.id
	join "country" c1 on
		c1.id = d1.person_country_id
	join "document" d2 on
		-- join to bookings of event of reference
	 	(d2.event_id = e1.bookings_auto_confirm_event_id1 OR d2.event_id = e1.bookings_auto_confirm_event_id2 OR d2.event_id = e1.bookings_auto_confirm_event_id3 OR d2.event_id = e1.bookings_auto_confirm_event_id4)
		-- and booking not cancelled
		and not d2.cancelled
		-- and booking confirmed
		and d2.confirmed
		and (
			-- same person id
		 	d1.person_id = d2.person_id
			-- or
			or (
				-- same email address
				lower(d1.person_email) = lower(d2.person_email)
				-- and same full name (no accent)
				and lower(d1.person_abc_names) = lower(d2.person_abc_names)
			)
		)
	join "event" e2 on
		e2.id = d2.event_id
	where
		-- only for events with bookings auto confirm...
	    e1.bookings_auto_confirm
		-- ... and at least one event of reference for auto confirm
		and (e1.bookings_auto_confirm_event_id1 is not null OR e1.bookings_auto_confirm_event_id2 is not null OR e1.bookings_auto_confirm_event_id3 is not null OR e1.bookings_auto_confirm_event_id4 is not null)
		-- ... and bookings still opened
		and e1.booking_closed = false
		-- ... and booking not already confirmed...
		and d1.confirmed = false
		-- ... and no (pending or sent) confirmation email
		and not exists(select * from mail m join letter l on l.id=m.letter_id join letter_type lt on lt.id=l.type_id where m.document_id=d1.id and lt.confirmation)
		-- ... and not cancelled
		and d1.cancelled = false
		-- ... and fully paid
		and d1.price_deposit >= d1.price_net
		-- ... and has no multiple bookings
		and (d1.multiple_booking_id is null or d1.not_multiple_booking_id=d1.multiple_booking_id or (select not_cancelled from multiple_booking where id=d1.multiple_booking_id limit 1) <= 1)
		-- ... and excluding China (2021 Festivals)
		and c1.name <> 'China'
		-- ... and excluding bookings with no centre (added for 2021 Summer Festival)
		and d1.person_organization_id is not null
		-- ... and excluding partial weeks bookings for 2021 Fall Festival
		and (e1.id <> 821 or
		    -- partial week for UK day hubs
		    (select count(*) from attendance a join document_line dl on dl.id=a.document_line_id where dl.document_id=d1.id and dl.item_id=37 and dl.site_id in (1358, 1352, 1353, 1354, 1355) and a.date between '2021-10-22' and '2021-10-28') in (0, 7) and
		    -- partial week for UK day+1 hubs
		    (select count(*) from attendance a join document_line dl on dl.id=a.document_line_id where dl.document_id=d1.id and dl.item_id=37 and dl.site_id in (1356, 1357) and a.date between '2021-10-23' and '2021-10-29') in (0, 7) and
		    -- partial week for UK day+2 hub
		    (select count(*) from attendance a join document_line dl on dl.id=a.document_line_id where dl.document_id=d1.id and dl.item_id=37 and dl.site_id in (1360) and a.date between '2021-10-24' and '2021-10-30') in (0, 7)
		    )
	ORDER BY d1.id, e2.start_date DESC
),
-- Auto confirm and send auto confirm letter to selected bookings
bookings_updated as (
	update
		"document" set
		--confirmed = true, -- commented as it's better to confirm the booking only once the confirmation letter has been successfully sent
		trigger_send_system_letter_id = b.letter_id
	from
		bookings b
	where
		"document".id = b.document_id
	returning
		document_id,
		letter_id,
		ref_event_id,
		ref_event_name,
		ref_document_ref
),
-- Add a record to the history of the auto confirmed bookings
history_confirm as (
	insert
		into
		history (document_id, "comment", "date", username)
	select
		document_id,
		'Auto confirmed based on ' || ref_event_id || ' - ' || ref_event_name || ' #' || ref_document_ref,
		now(),
		'system'
	from
		bookings_updated
)
select into updated count(*) from bookings_updated;

return updated;

	END;
$$;


ALTER FUNCTION public.bookings_auto_confirm() OWNER TO kbs;

--
-- Name: cart_messages(integer); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.cart_messages(from_cart_id integer) RETURNS TABLE(document_id integer, document_ref integer, cm_id integer, label_id integer, cm_grp smallint, cm_priority smallint, cm_ord smallint)
    LANGUAGE plpgsql
    AS $$
	BEGIN
		-- Select the lowest priority messages for each group
		return query 
		select * from (
			select distinct on (cm_grp) 
				* 
			from ( -- from valid messages for each booking of the cart
				-- select the highest priority message for each group for each document in the cart
				select distinct on (document_id, cm_grp)
					*
				from cart_messages_no_conflict(from_cart_id) as tmp
				order by document_id, cm_grp, cm_priority desc, cm_ord
			) as tmp2
			order by cm_grp, cm_priority
		) as tmp
		order by cm_ord
		;
	END;
$$;


ALTER FUNCTION public.cart_messages(from_cart_id integer) OWNER TO kbs;

--
-- Name: cart_messages(character varying); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.cart_messages(from_cart_uuid character varying) RETURNS TABLE(document_id integer, document_ref integer, cm_id integer, label_id integer, cm_grp smallint, cm_priority smallint, cm_ord smallint)
    LANGUAGE plpgsql
    AS $$
	DECLARE
		from_cart_id integer;
	BEGIN
		select id into from_cart_id from cart where uuid = from_cart_uuid;
		return query 
		select 
			* 
		from cart_messages(from_cart_id) as tmp
		;
	END;
$$;


ALTER FUNCTION public.cart_messages(from_cart_uuid character varying) OWNER TO kbs;

--
-- Name: cart_messages_all(integer); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.cart_messages_all(from_cart_id integer) RETURNS TABLE(document_id integer, document_ref integer, id integer, label_id integer, grp smallint, priority smallint, ord smallint)
    LANGUAGE plpgsql
    AS $$
	BEGIN
		-- select all valid messages for each booking in the cart
		return query select
			d.id as document_id,
			d.ref as document_ref,
			cm.id as id,
			cm.label_id as label_id,
			cm.grp as grp,
			cm.priority as priority,
			cm.ord as ord
		from
			cart_message cm
		join "document" d on
			d.event_id = cm.event_id
		where
			-- list of document ids in the cart, replacing cancelled bookings with any not-cancelled bookings for the same person or same email and fullname from any cart if available
			d.id in (select distinct on (d0.id)
					d1.id
				from 
					"document" d0
					-- join a second time to document to select an alternative not cancelled booking for cancelled bookings in the cart
					join "document" d1 on
						-- if booking cancelled, join to not cancelled booking of same event with same person_id or same email and fullname
						(d0.cancelled=true
							and d1.event_id=d0.event_id 
							and d1.cancelled=false 
							and (
								d1.person_id=d0.person_id 
								or (d1.person_email=d0.person_email 
									and d1.person_abc_names=d0.person_abc_names
								)
							)
						)
						-- fallback to same booking (for bookings not cancelled or cancelled but without any alternative)
						or d1.id=d0.id
				where 
					d0.cart_id=from_cart_id
				order by d0.id, d1.cancelled, d1.confirmed desc, d1.price_net-d1.price_deposit)
			and ( -- now must be null or equal or greater than from
				cm.show_from is null
				or now() >= show_from 
			)
			and ( -- now must be null or equal or lower than until
				cm.show_until is null
				or now() < show_until 
			)
			and ( -- document_cancelled message condition
				cm.document_cancelled is null -- no condition
				or cm.document_cancelled=d.cancelled -- condition
			)
			and ( -- document_confirmed message condition
				cm.document_confirmed is null -- no condition
				or cm.document_confirmed=d.confirmed -- condition
			)
			and ( -- document_fully_paid message condition
				cm.document_fully_paid is null -- no condition
				or (	-- if must be fully paid
					cm.document_fully_paid=true
					and d.price_deposit >= d.price_net
					)
				or (	-- if must not be fully paid
					cm.document_fully_paid=false
					and d.price_deposit < d.price_net
					)
			)
			-- all cmc should be true. Total of cmc of this message should equal the number of true cmc of this message for this document
			and (select count(*) from cart_message_condition cmc1 where cmc1.cart_message_id=cm.id) = (
					select 
						count(*) 
					from 
						cart_message_condition cmc2
					where 
						cmc2.cart_message_id = cm.id
						and (
							(
								cmc2.booked=true
								and (select count(*) from (select
										distinct dl1.id
									from document_line dl1
									join attendance a1 on a1.document_line_id = dl1.id
									where 
										dl1.document_id = d.id
										and (cmc2.site_id is null or dl1.site_id=cmc2.site_id)
										and (cmc2.item_id is null or dl1.item_id=cmc2.item_id)
										and (cmc2.date is null or a1.date=cmc2.date)
								) as temp) > 0
							)
							or (
								cmc2.booked=false
								and (select count(*) from (select
										distinct dl2.id
									from document_line dl2
									join attendance a2 on a2.document_line_id = dl2.id
									where 
										dl2.document_id = d.id
										and (cmc2.site_id is null or dl2.site_id=cmc2.site_id)
										and (cmc2.item_id is null or dl2.item_id=cmc2.item_id)
										and (cmc2.date is null or a2.date=cmc2.date)
								) as temp) = 0
							)
						)
				)
		order by d.id, cm.grp, cm.priority desc, cm.ord;
	END;
$$;


ALTER FUNCTION public.cart_messages_all(from_cart_id integer) OWNER TO kbs;

--
-- Name: cart_messages_no_conflict(integer); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.cart_messages_no_conflict(from_cart_id integer) RETURNS TABLE(document_id integer, document_ref integer, cm_id integer, label_id integer, cm_grp smallint, cm_priority smallint, cm_ord smallint)
    LANGUAGE plpgsql
    AS $$
	BEGIN
		-- select valid messages of the cart which are not in conflict
		return query 
		with 
		messages as (select * from cart_messages_all(from_cart_id)),
		messages_count as (
			-- count how many times each message is present
			select 
				m.id,
				count(*) as count
			from messages m
			group by id
		),
		grp_priority_count as (
			-- count how many messages of each priority in a same group
			select 
				grp,
				priority,
				count(*) as count
			from messages
			group by grp, priority
		)
		-- removes conflicts (different messages in the same group and with same priority)
		select 
			m.*
		from 
			messages m
		join messages_count mc on
			mc.id = m.id
		join grp_priority_count gpc on
			gpc.grp = m.grp and gpc.priority = m.priority
		where
			gpc.count<=mc.count;
	END;
$$;


ALTER FUNCTION public.cart_messages_no_conflict(from_cart_id integer) OWNER TO kbs;

--
-- Name: compute_dates(date[]); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.compute_dates(dates date[]) RETURNS text
    LANGUAGE plpgsql
    AS $$
DECLARE
	s text := '';
	date date;
	first_date_sequence date := null;
	last_date_sequence date;
BEGIN	
	foreach date in array dates loop
		-- RAISE NOTICE 'date=%', date_record.date;
		IF (first_date_sequence is null) THEN
			first_date_sequence := date;
			s := to_char(first_date_sequence, 'DD/MM');			 
		ELSIF (date - last_date_sequence > 1) THEN
			IF (last_date_sequence <> first_date_sequence) THEN
				s := s || '-' || to_char(last_date_sequence, 'DD/MM');
			END IF;
			first_date_sequence := date;
			s := s || ',' || to_char(first_date_sequence, 'DD/MM');
		END IF;
		last_date_sequence := date;
	END LOOP;
	IF (last_date_sequence <> first_date_sequence) THEN
		s := s || '-' || to_char(last_date_sequence, 'DD/MM');
	END IF;
	-- RAISE NOTICE 'dates=%', s;
	RETURN s;
END;
$$;


ALTER FUNCTION public.compute_dates(dates date[]) OWNER TO kbs;

--
-- Name: compute_document_line_pricing_quantity(public.document_line); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.compute_document_line_pricing_quantity(dl public.document_line) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	quantity document_line.quantity%TYPE;
	per_person bool := false;
BEGIN
	IF (dl.price_is_custom) THEN
		quantity := 1;
	ELSE
		quantity := dl.quantity;
		IF (dl.share_mate and not dl.share_mate_charged) THEN
			quantity := 0;
		ELSIF (dl.share_owner) THEN
			-- commented because on insert, the attendance rates are not yet set, so using item_id and site_id as criteria
			--select into per_person r.per_person from attendance a join rate r on a.rate_id=r.id where a.document_line_id=dl.id limit 1;
			select into per_person r.per_person from rate r join item i on i.id=dl.item_id where r.item_id=coalesce(i.rate_alias_item_id, i.id) and r.site_id=dl.site_id limit 1;
			IF (not per_person) THEN
				quantity := 1;
			ELSE
				quantity := dl.share_owner_quantity - COALESCE((select sum(dlm.quantity) from document_line dlm where dlm.share_mate_owner_document_line_id=dl.id and dlm.share_mate_charged=true), 0);
				--RAISE EXCEPTION 'quantity=%, FOUND=%, per_person=%', quantity, FOUND, per_person;
			END IF;			
		END IF;
	END IF;
	RETURN quantity;
END;
$$;


ALTER FUNCTION public.compute_document_line_pricing_quantity(dl public.document_line) OWNER TO kbs;

--
-- Name: compute_document_prices(integer, boolean); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.compute_document_prices(document_id integer, trace boolean) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	did int := document_id; -- second name to remove ambiguity in sql statement
	recs compute_price_record[];
	rec compute_price_record;
	rec_index int := 0;
	i int;
	document document;
	document_line document_line;
	attendance attendance;
	document_lines document_line[];
	document_line_rates rate[];
	document_line_rate_indexes int[];
    block_site_id int;
    block_rate_item_id int;
    block_price int;
    block_length int;
    single_line_block bool;
	block_rec_index int;
	rate_block_length int;
	consumed_days int[];
	consumed_prices int[];
	consumed_day int;
	consumed_price int;
	rate_unit_price int;
	attendance_price int;
	cheapest_attendance_price int;
    block_attendance_price int;
	rate rate;
	rates_count int;
	rate_index int;
	rate_applicable bool;
	rate_min_day int;
	rate_max_day int;
	rate_min_deposit int;
	rate_non_refundable int;
	r_date date;
	pricing_quantity int;
	dl_index int;
	write bool;
	starting_block bool;
	rounding_factor int := null;
    new_rounding_algo bool;
    rounding_net int := 0;
    rounding_min_deposit int := 0;
    rounding_non_refundable int := 0;
    family_code item_family.code%TYPE;
BEGIN
	raise notice '>>> compute_document_prices(%)', document_id;
	recs := array(
		with dls as (select d -- using a with statement to be able to use rate_item_id and rate_date in the final select for fetching rates
				, dl
				, coalesce((select rate_alias_item_id from item i where i.id=dl.item_id), dl.item_id) as rate_item_id
				, case when dl.cancelled and dl.cancellation_date is not null then dl.cancellation_date else cast(now() as date) end as rate_date
				, dl.site_id -- also used for fetching rates
				, dl.id as document_line_id -- used for attendance join in the final select
				, dl.cancelled or dl.abandoned as cancelled
				, cast(row_number() over (order by dl.id) as int) as document_line_index
				, cast((select count(*) from document_line dl where dl.document_id=d.id) as int) as document_lines_count
			from document_line dl join document d on d.id=dl.document_id
			where d.id=did and dl.item_id<>23)
		, dlrs as (select d, dl, rate_item_id, rate_date, site_id, document_line_id, cancelled, document_line_index, document_lines_count
				, array(select r from rate r where r.site_id=dls.site_id and r.item_id=rate_item_id and rate_matches_document(r,d) and (true or overlaps(rate_date, rate_date, r.on_date, r.off_date)) order by coalesce(compute_rate_unit_price(r, d), 0) / case when r.per_day then 1 else coalesce(r.max_day, 1) end desc) as rates
			from dls)
		select (d, dl, rate_item_id, rate_date, rates, document_line_index, document_lines_count
				, a)
			from dlrs left join attendance a on a.document_line_id=dlrs.document_line_id
			where a.charged or a is null -- skipping not charged attendance
			order by dlrs.site_id,rate_item_id,dlrs.cancelled,a.date
	);
	foreach rec in array recs loop
		rec_index := rec_index + 1;
		if (document is null) then -- first iteration, initializing document_lines as empty array
			document := rec.d;
			write := document.trigger_defer_compute_prices; -- detecting if called from trigger, if yes we need to update tables
			document_lines := array_fill(cast(null as document_line), ARRAY[rec.document_lines_count]);
			document_line_rates := array_fill(cast(null as rate), ARRAY[rec.document_lines_count]);
			document_line_rate_indexes := array_fill(0, ARRAY[rec.document_lines_count]);
		end if;
		document_line := document_lines[rec.document_line_index];
		if (document_line is null) then
			document_line := rec.dl;
			if (not document_line.price_is_custom) then
				document_line.price_net := 0;
				document_line.price_min_deposit := 0;
				document_line.price_non_refundable := 0;
			end if;
		end if;
		attendance := rec.a;
		-- new block detection (a block is identified by site_id and rate_item_id pair
		starting_block := block_site_id is null or block_site_id <> document_line.site_id or block_rate_item_id <> rec.rate_item_id;
		if (starting_block) then
			-- resetting the block as a new block
			block_site_id := document_line.site_id;
			block_rate_item_id := rec.rate_item_id;
			block_price := 0;
			block_rec_index = rec_index;
			-- computing the block length
			block_length := 1; -- starting with 1 (the minimum)
			single_line_block = true;
			-- then increasing the length until we are out of the block (another site_id or rate_item_id)
			while (rec_index + block_length <= array_length(recs) and recs[rec_index + block_length].dl.site_id = block_site_id and recs[rec_index + block_length].rate_item_id = block_rate_item_id) loop
				single_line_block := single_line_block and recs[rec_index + block_length].document_line_index = rec.document_line_index;
				block_length := block_length + 1;
			end loop;
			rates_count := array_length(rec.rates);
			consumed_days   := array_fill(0, ARRAY[rates_count]); -- consumed days for each rate
			consumed_prices := array_fill(0, ARRAY[rates_count]); -- consumed price for each rate
		end if;
		block_attendance_price := 0;
		if (not document.abandoned and not document_line.abandoned and not attendance is null) then
			-- searching the cheapest rate for this attendance
			cheapest_attendance_price := null;
			rate_index := 0;
			foreach rate in array rec.rates loop
				rate_index := rate_index + 1;
				rate_applicable := overlaps(attendance.date, attendance.date, rate.start_date, rate.end_date) and overlaps(document_line.creation_date, document_line.creation_date, rate.on_date, rate.off_date);
				if (rate_applicable and rate.arriving_or_leaving and rec_index > 1 and rec_index < array_length(recs)) then
					rate_applicable := recs[rec_index - 1].document_line_index <> rec.document_line_index
								    or recs[rec_index + 1].document_line_index <> rec.document_line_index
									or greatest(attendance.date - recs[rec_index - 1].a.date, recs[rec_index + 1].a.date - attendance.date) > 1 ;
				end if;
				rate_min_day = coalesce(rate.min_day, 1);
				rate_max_day = case when rate.per_day then 1 else coalesce(rate.max_day, 10000) end;
				rate_block_length = block_length;
				-- cropping the rate_block_length within the rate [start_date, end_date] for min day comparison (ex: 2018 Summer part 2 discount is within 3-11 August, minDay = 9 but free day 2 August should be ignored in rate_block_length)
				if (rate_applicable and block_length >= rate_min_day and (recs[block_rec_index].a.date < rate.start_date or recs[block_rec_index + block_length - 1].a.date > rate.end_date)) then
					for i in block_rec_index .. block_rec_index + block_length - 1 loop
						if (recs[i].a.date < rate.start_date or recs[i].a.date > rate.end_date) then
							rate_block_length := rate_block_length - 1;
						end if;
					end loop;
				end if;
				if (rate_applicable and rate_block_length < rate_min_day and not rate.min_day_ceiling) then
					rate_applicable := false;
				end if;
				if (not rate_applicable) then
					consumed_days[rate_index] := 0;
					consumed_prices[rate_index] := 0;
				else
					rate_unit_price := coalesce(compute_rate_unit_price(rate, rec.d), 0);
					-- When a rate defines a new lower daily price that applies after a minimum of days (ex: 30% discount when >= 14 days),
					-- we need to ensure that people approaching that number of days (ex: 12 or 13 days)
					-- don't pay more with the previous rate than people staying that minimum of days (ex: 14 days)
					-- In other words, we need to put an upper limit for such people, equals to the price that is applied at that minimum of days
					if (rate_block_length < rate_min_day and rate_max_day = 1) then -- So if the block is less than the rate min day,
						rate_unit_price = rate_unit_price * rate_min_day; -- we transform the daily rate into a fixed rate with the upper limit
						rate_max_day = rate_min_day; -- that applies over that period
					end if;
					consumed_day := consumed_days[rate_index];
					-- if (trace) THEN raise notice '148> consumed_price = %', consumed_price; end if;
					if (rates_count = 1 and not rate.per_day and rate.max_day is null) then
						consumed_price := block_price;
						-- if (trace) THEN raise notice '151> consumed_price = %', consumed_price; end if;
						if (document_line_rate_indexes[rec.document_line_index] = 0 and rate_unit_price > 0) then
							while (consumed_price >= rate_unit_price) LOOP
								consumed_price := consumed_price - rate_unit_price;
							end loop;
						end if;
					else
						consumed_price := consumed_prices[rate_index];
						-- if (trace) THEN raise notice '159> consumed_price = %', consumed_price; end if;
					end if;
					if (consumed_price > rate_unit_price) then
						attendance_price := LEAST(0, rate_unit_price);
					else
						attendance_price := rate_unit_price - consumed_price;
					end if;
					consumed_day := consumed_day + 1;
					if (consumed_day >= rate_max_day) then
						consumed_days[rate_index] := 0;
						consumed_prices[rate_index] := 0;
					else
						consumed_days[rate_index] := consumed_day;
					end if;
					if (cheapest_attendance_price is null or attendance_price < cheapest_attendance_price) then
						cheapest_attendance_price := attendance_price;
						-- memorizing applied rate to the document_line for min_deposit, non_refundable, and custom_price computation
    					document_line_rates[rec.document_line_index] := rate;
						document_line_rate_indexes[rec.document_line_index] := rate_index;
    					if (trace) then raise notice '>>> Cheapest rate rate_id = %, unit_price = %, consumed_day = %, attendance_price = %', rate.id, rate_unit_price, consumed_day, attendance_price; end if;
					end if;
				end if;
			end loop;
			block_attendance_price := coalesce(cheapest_attendance_price, 0);
			for rate_index in 1 .. rates_count loop
				if (consumed_days[rate_index] > 0) then
					consumed_prices[rate_index] := consumed_prices[rate_index] + block_attendance_price;
					-- if (trace) then	raise notice '>> rate_id = %, consumed_day = %, consumed_price = %', rec.rates[rate_index].id, consumed_days[rate_index], consumed_prices[rate_index]; end if;
				end if;
			end loop;
			if (trace) then raise notice '> attendance: date = %, price = %', attendance.date, block_attendance_price; end if;
		end if;
		-- appending it to the block
		block_price := block_price + block_attendance_price;
		-- if (trace) then raise notice 'block: rate_item_id = %, price = %', rec.rate_item_id, block_price; end if;
		if (not document_line.price_is_custom) then
			rate := coalesce(document_line_rates[rec.document_line_index], rate);
			-- if the rate applies on the whole block, we reset the whole document line computation because different
			-- amount may finally apply for min deposit and non refundable (ex: £5 admin fees)
    		rate_index = document_line_rate_indexes[rec.document_line_index];
			if (rate_index <> 0 and single_line_block and consumed_days[rate_index] = block_length) then
                document_line.price_net 			:= 0;
                document_line.price_min_deposit 	:= 0;
                document_line.price_non_refundable 	:= 0;
                block_attendance_price = block_price;
            end if;
            rate_min_deposit    = compute_rate_min_deposit   (rate, rec.rate_date);
            rate_non_refundable = compute_rate_non_refundable(rate, rec.rate_date);
            -- If rate min deposit or non refundable are negative, they express a fixed amount (and not a percentage).
            -- Note: only fixed non refundable are used so for (for admin fees), fixed min deposit should work as well
            -- here but be aware this will need an update of the front-end (to consider the case of negative values).
            document_line.price_net 			:= document_line.price_net				+ block_attendance_price;
            document_line.price_min_deposit 	:= document_line.price_min_deposit 		+ case when rate_min_deposit    >=0 then block_attendance_price else -100 end * rate_min_deposit;
            document_line.price_non_refundable 	:= document_line.price_non_refundable 	+ case when rate_non_refundable >=0 then block_attendance_price else -100 end * rate_non_refundable;
		end if;
		document_lines[rec.document_line_index] := document_line;
	end loop;

	-- final iteration: finalizing details (quantity, percentage, discount, rounding) and computing total prices
	document.price_net := 0;
	document.price_min_deposit := 0;
	document.price_non_refundable := 0;
	new_rounding_algo := document.event_id >= 45 and document.event_id <= 64 or document.event_id >= 90;
	if (document_lines is not null) then
		select into rounding_factor option_rounding_factor from event where id=document.event_id;
		dl_index := 0;
		foreach document_line in array document_lines loop
			dl_index := dl_index + 1;
			-- if custom price, computing the min deposit and non refundable over the whole line
			if (document_line.price_is_custom) then
				rate := document_line_rates[dl_index];
				r_date := case when document_line.cancelled and document_line.cancellation_date is not null then document_line.cancellation_date else cast(now() as date) end;
                rate_min_deposit    = case when rate is null then 100 else compute_rate_min_deposit(rate, r_date) end;
                rate_non_refundable = case when rate is null then 100 else compute_rate_non_refundable(rate, r_date) end;
				document_line.price_net            := document_line.price_custom;
				document_line.price_min_deposit    := case when rate_min_deposit    >=0 then document_line.price_net else -100 end * rate_min_deposit;
				document_line.price_non_refundable := case when rate_non_refundable >=0 then document_line.price_net else -100 end * rate_non_refundable;
				pricing_quantity := 1;
			else
				pricing_quantity := compute_document_line_pricing_quantity(document_line);
			end if;
			-- applying the quantity (for all) and percentage (for min deposit and non refundable)
			document_line.price_net            := coalesce(pricing_quantity * document_line.price_net, 0);
			document_line.price_min_deposit    := coalesce(pricing_quantity * document_line.price_min_deposit / 100, 0);
			document_line.price_non_refundable := coalesce(pricing_quantity * document_line.price_non_refundable / 100, 0);
			-- applying discount if any
			if (document_line.price_discount is not null and not document_line.price_is_custom) then -- no discount on custom price
				document_line.price_net            := document_line.price_net            - document_line.price_net            * document_line.price_discount / 100;
				document_line.price_min_deposit    := document_line.price_min_deposit    - document_line.price_min_deposit    * document_line.price_discount / 100;
				document_line.price_non_refundable := document_line.price_non_refundable - document_line.price_non_refundable * document_line.price_discount / 100;
			end if;
	      -- applying rounding factor if any
			if (rounding_factor is not null) then
				select into family_code f.code from item i join item_family f on f.id=i.family_id where i.id=document_line.item_id;
				if (family_code not in ('acco', 'tax')) then -- not on accommodation and tax (because they are not round anymore in KMCF courses)
					document_line.price_net := round(document_line.price_net * 1.0 / rounding_factor) * rounding_factor;
				end if;
			end if;
	      -- rounding balance for min deposit and non refundable
			if (not new_rounding_algo) then
				document_line.price_min_deposit    := document_line.price_net - (document_line.price_net - document_line.price_min_deposit)    / 100 * 100;
				document_line.price_non_refundable := document_line.price_net - (document_line.price_net - document_line.price_non_refundable) / 100 * 100;
			end if;
			-- price correction for cancelled lines => net and min deposit become non refundable
			if (document_line.cancelled) then
                rounding_net := rounding_net + document_line.price_net - document_line.price_non_refundable;
				document_line.price_net         := document_line.price_non_refundable;
				document_line.price_min_deposit := document_line.price_non_refundable;
			end if;
			rounding_min_deposit    := rounding_min_deposit    + document_line.price_net - document_line.price_min_deposit;
			rounding_non_refundable := rounding_non_refundable + document_line.price_net - document_line.price_non_refundable;
			document.price_net 			  := document.price_net            + document_line.price_net;
			document.price_min_deposit 	  := document.price_min_deposit    + document_line.price_min_deposit;
			document.price_non_refundable := document.price_non_refundable + document_line.price_non_refundable;
			if (write) then
			   	update document_line as dl set
	  					price_net            = document_line.price_net,
				        price_min_deposit    = document_line.price_min_deposit,
					    price_non_refundable = document_line.price_non_refundable
	  	   		where dl.id=document_line.id;
			end if;
			if (trace) then raise notice 'document_line = % - % - % - % - % - %', document_line.id, document_line.item_id, document_line.dates, document_line.price_net, document_line.price_min_deposit, document_line.price_non_refundable; end if;
		end loop;
	end if;

	if (write) then
      if (new_rounding_algo) then
				-- if (document.event_id = 115 and document.price_net % 100 <> 0) then
				--	rounding_net := rounding_net + 100 - document.price_net % 100;
				-- end if;
				rounding_net := rounding_net % 100;
				rounding_min_deposit := (rounding_min_deposit + rounding_net) % 100;
				rounding_non_refundable := (rounding_non_refundable + rounding_net) % 100;
				if (rounding_net = 0 and rounding_min_deposit = 0 and rounding_non_refundable = 0) then
					delete from document_line dl where dl.document_id=document.id and dl.item_id=23;
				else
					update document_line dl set price_net = rounding_net, price_min_deposit = rounding_min_deposit, price_non_refundable = rounding_non_refundable, read=true where dl.document_id=document.id and dl.item_id=23;
					if (not found) then
						insert into document_line (document_id, item_id, price_net, price_min_deposit, price_non_refundable, read) values (document.id, 23, rounding_net, rounding_min_deposit, rounding_non_refundable, true);
					end if;
					document.price_net := document.price_net + rounding_net;
					document.price_min_deposit := document.price_min_deposit + rounding_min_deposit;
				end if;
		end if;
   	update document as d set
  			price_net            = document.price_net,
	        price_min_deposit    = document.price_min_deposit,
		    price_non_refundable = document.price_non_refundable,
			trigger_defer_compute_prices = false
  	   where d.id=document.id;
	end if;

	RETURN document.price_net;
END;
$$;


ALTER FUNCTION public.compute_document_prices(document_id integer, trace boolean) OWNER TO kbs;

--
-- Name: compute_rate_min_deposit(public.rate, date); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.compute_rate_min_deposit(r public.rate, d date) RETURNS smallint
    LANGUAGE plpgsql
    AS $$
BEGIN
	RETURN coalesce(CASE
		WHEN r.cutoff_date  IS NULL or d < r.cutoff_date  THEN r.min_deposit
		WHEN r.cutoff_date2 IS NULL or d < r.cutoff_date2 THEN r.min_deposit2
		WHEN r.cutoff_date3 IS NULL or d < r.cutoff_date3 THEN r.min_deposit3
		WHEN r.cutoff_date4 IS NULL or d < r.cutoff_date4 THEN r.min_deposit4
		WHEN r.cutoff_date5 IS NULL or d < r.cutoff_date5 THEN r.min_deposit5
	   ELSE r.min_deposit6 END, 25);
END;
$$;


ALTER FUNCTION public.compute_rate_min_deposit(r public.rate, d date) OWNER TO kbs;

--
-- Name: compute_rate_non_refundable(public.rate, date); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.compute_rate_non_refundable(r public.rate, d date) RETURNS smallint
    LANGUAGE plpgsql
    AS $$
BEGIN
	RETURN coalesce(CASE
		WHEN r.cutoff_date  IS NULL or d < r.cutoff_date  THEN r.non_refundable
		WHEN r.cutoff_date2 IS NULL or d < r.cutoff_date2 THEN r.non_refundable2
		WHEN r.cutoff_date3 IS NULL or d < r.cutoff_date3 THEN r.non_refundable3
		WHEN r.cutoff_date4 IS NULL or d < r.cutoff_date4 THEN r.non_refundable4
		WHEN r.cutoff_date5 IS NULL or d < r.cutoff_date5 THEN r.non_refundable5
	   ELSE r.non_refundable6 END, 25);
END;
$$;


ALTER FUNCTION public.compute_rate_non_refundable(r public.rate, d date) OWNER TO kbs;

--
-- Name: compute_rate_unit_price(public.rate, public.document); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.compute_rate_unit_price(r public.rate, d public.document) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	price int4;BEGIN
	price := CASE WHEN r IS NULL THEN NULL ELSE r.price END;IF (price IS NOT NULL) THEN
		IF (d.person_age IS NOT NULL) THEN
			IF (r.age1_max IS NOT NULL AND d.person_age <= r.age1_max) THEN
				price := COALESCE(r.age1_price, price * (100 - r.age1_discount) / 100, price);ELSIF (r.age2_max IS NOT NULL AND d.person_age <= r.age2_max) THEN
				price := COALESCE(r.age2_price, price * (100 - r.age2_discount) / 100, price);ELSIF (r.age3_max IS NOT NULL AND d.person_age <= r.age3_max) THEN
				price := COALESCE(r.age3_price, price * (100 - r.age3_discount) / 100, price);END IF;ELSIF (d.person_resident AND (r.resident_price is not null or r.resident_discount is not null)) THEN
				price := COALESCE(r.resident_price, price * (100 - r.resident_discount) / 100, price);ELSIF (d.person_resident2 AND (r.resident2_price is not null or r.resident2_discount is not null)) THEN
				price := COALESCE(r.resident2_price, price * (100 - r.resident2_discount) / 100, price);ELSIF (d.person_unemployed AND (r.unemployed_price is not null or r.unemployed_discount is not null)) THEN
				price := COALESCE(r.unemployed_price, price * (100 - r.unemployed_discount) / 100, price);ELSIF (d.person_facility_fee AND (r.facility_fee_price is not null or r.facility_fee_discount is not null)) THEN
				price := COALESCE(r.facility_fee_price, price * (100 - r.facility_fee_discount) / 100, price);ELSIF (d.person_discovery AND (r.discovery_price is not null or r.discovery_discount is not null)) THEN
				price := COALESCE(r.discovery_price, price * (100 - r.discovery_discount) / 100, price);ELSIF (d.person_discovery_reduced AND (r.discovery_reduced_price is not null or r.discovery_reduced_discount is not null)) THEN
				price := COALESCE(r.discovery_reduced_price, price * (100 - r.discovery_reduced_discount) / 100, price);ELSIF (d.person_working_visit AND (r.working_visit_price is not null or r.working_visit_discount is not null)) THEN
				price := COALESCE(r.working_visit_price, price * (100 - r.working_visit_discount) / 100, price);ELSIF (d.person_guest AND (r.guest_price is not null or r.guest_discount is not null)) THEN
				price := COALESCE(r.guest_price, price * (100 - r.guest_discount) / 100, price);END IF;END IF;RETURN price;END;$$;


ALTER FUNCTION public.compute_rate_unit_price(r public.rate, d public.document) OWNER TO kbs;

--
-- Name: copy_date_info(integer, integer, integer); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.copy_date_info(src_event_id integer, src_date_info_id integer, dst_event_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	new_info record;new_id int := -1;BEGIN
	for new_info in

with src as (
	select * from date_info where (src_date_info_id is not null and id = src_date_info_id) or (src_date_info_id is null and event_id = src_event_id)
),
row_src as (select row_number() over (order by id),* from src order by id),
new as (
	insert into date_info (event_id, date, end_date, date_time_range, min_date_time_range, max_date_time_range, label_id, exclude, option_id, fees_bottom_label_id, fees_popup_label_id, force_soldout)
			 	select    dst_event_id, date, end_date, date_time_range, min_date_time_range, max_date_time_range, label_id, exclude, option_id, fees_bottom_label_id, fees_popup_label_id, force_soldout from row_src
   returning *
),
row_new as (select row_number() over (order by id),* from new)
select rn.id as new_id, rs.id as src_id from row_new rn join row_src rs on rs.row_number=rn.row_number

	loop
		if (new_id = -1) then
			new_id := new_info.new_id;end if;end loop;return new_id;END;$$;


ALTER FUNCTION public.copy_date_info(src_event_id integer, src_date_info_id integer, dst_event_id integer) OWNER TO kbs;

--
-- Name: copy_event(integer, integer, date); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.copy_event(src_event_id integer, dst_event_id integer, dst_event_start_date date) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
	update event eu set
		active=e.active,
		live=e.live,
		activity_id=e.activity_id,
		type_id=e.type_id,
	 	label_id=e.label_id,
		buddha_id=e.buddha_id,
		teacher_id=e.teacher_id,
		date_time_range=e.date_time_range,
		max_date_time_range=e.max_date_time_range,
		min_date_time_range=e.min_date_time_range,
		pre_date=e.pre_date,
		start_date=e.start_date,
		end_date=e.end_date,
		post_date=e.post_date,
		opening_date=e.opening_date,
		show_sites=e.show_sites,
		pass_control=e.pass_control,
		pass_control_date_time_range = e.pass_control_date_time_range,
		send_history_emails=e.send_history_emails,
		css_class=e.css_class,
		fees_bottom_label_id=e.fees_bottom_label_id,
		options_top_label_id=e.options_top_label_id,
		option_rounding_factor=e.option_rounding_factor,
		host=e.host,
		uri=e.uri,
		support_notice_label_id = e.support_notice_label_id,
		teachings_day_ticket = e.teachings_day_ticket,
		notification_cart_default_label_id = e.notification_cart_default_label_id, 
		notification_cart_payed_label_id = e.notification_cart_payed_label_id, 
		notification_cart_confirmed_label_id = e.notification_cart_confirmed_label_id, 
		notification_cart_payed_confirmed_label_id = e.notification_cart_payed_confirmed_label_id, 
		booking_closing_date = e.booking_closing_date,
		frontend = e.frontend,
		timezone = e.timezone, 
		no_account_booking = e.no_account_booking, 
		payment_closing_date = e.payment_closing_date,
		arrival_departure_from_dates_sections = e.arrival_departure_from_dates_sections
	 from event e
	 where eu.id=dst_event_id and e.id=src_event_id;

	perform copy_site(src_event_id, null, dst_event_id);
	perform copy_option(src_event_id, null, dst_event_id, null);
	perform copy_letter(src_event_id, null, dst_event_id);
	perform copy_date_info(src_event_id, null, dst_event_id);
	perform copy_money_entities(src_event_id, dst_event_id);
	perform shift_event_to(dst_event_id, dst_event_start_date);
	return dst_event_id;
END;
$$;


ALTER FUNCTION public.copy_event(src_event_id integer, dst_event_id integer, dst_event_start_date date) OWNER TO kbs;

--
-- Name: copy_event(integer, character varying, date); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.copy_event(src_event_id integer, dst_event_name character varying, dst_event_start_date date) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	dst_event_id int;
BEGIN
INSERT
	INTO
	"event" (
	live,
	active,
	corporation_id,
	organization_id,
	type_id,
	activity_id,
	name ,
	label_id,
	buddha_id,
	teacher_id,
	date_time_range,
	max_date_time_range,
	min_date_time_range,
	pre_date,
	start_date,
	end_date,
	post_date,
	opening_date,
	show_sites,
	pass_control,
	pass_control_date_time_range,
	send_history_emails,
	css_class,
	fees_bottom_label_id,
	options_top_label_id,
	option_rounding_factor,
	host,
	uri,
	support_notice_label_id,
	teachings_day_ticket,
	notification_cart_default_label_id,
	notification_cart_payed_label_id,
	notification_cart_confirmed_label_id,
	notification_cart_payed_confirmed_label_id,
	booking_closing_date,
	frontend,
	timezone,
	no_account_booking,
	payment_closing_date,
	arrival_departure_from_dates_sections
	)
SELECT
	FALSE,
	active,
	corporation_id,
	organization_id,
	type_id,
	activity_id,
	COALESCE(dst_event_name, 'Copy of ' || name),
	label_id,
	buddha_id,
	teacher_id,
	date_time_range,
	max_date_time_range,
	min_date_time_range,
	pre_date,
	start_date,
	end_date,
	post_date,
	opening_date,
	show_sites,
	pass_control,
	pass_control_date_time_range,
	send_history_emails,
	css_class,
	fees_bottom_label_id,
	options_top_label_id,
	option_rounding_factor,
	host,
	uri,
	support_notice_label_id,
	teachings_day_ticket,
	notification_cart_default_label_id,
	notification_cart_payed_label_id,
	notification_cart_confirmed_label_id,
	notification_cart_payed_confirmed_label_id,
	booking_closing_date,
	frontend,
	timezone,
	no_account_booking,
	payment_closing_date,
	arrival_departure_from_dates_sections
FROM
	"event"
WHERE
	id = src_event_id RETURNING id
INTO
	dst_event_id;
	perform copy_site(src_event_id, null, dst_event_id);
	perform copy_option(src_event_id, null, dst_event_id, null);
	perform copy_letter(src_event_id, null, dst_event_id);
	perform copy_date_info(src_event_id, null, dst_event_id);
	perform copy_money_entities(src_event_id, dst_event_id);
	perform shift_event_to(dst_event_id, dst_event_start_date);
	return dst_event_id;
END;
$$;


ALTER FUNCTION public.copy_event(src_event_id integer, dst_event_name character varying, dst_event_start_date date) OWNER TO kbs;

--
-- Name: copy_gateway_parameter(integer, integer, integer); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.copy_gateway_parameter(src_money_account_id integer, src_gateway_parameter_id integer, dst_money_account_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
	DECLARE
		new_info record;
		new_id int := -1;
	BEGIN
		-- copy gateway parameters
		for new_info in

			with src as (
				select * from gateway_parameter gp where (src_gateway_parameter_id is not null and id = src_gateway_parameter_id) or (src_gateway_parameter_id is null and account_id = src_money_account_id)
			),
			row_src as (select row_number() over (order by id),* from src order by id),
			new as (
				insert into gateway_parameter (account_id, company_id, name, value, test, live)
								select dst_money_account_id, company_id, name, value, test, live from row_src
			   returning *
			),
			row_new as (select row_number() over (order by id),* from new)
			select rn.id as new_id, rs.id as src_id from row_new rn join row_src rs on rs.row_number=rn.row_number

		loop
			if (new_id = -1) then
				new_id := new_info.new_id;
			end if;
		end loop;

		return new_id;
	END;
$$;


ALTER FUNCTION public.copy_gateway_parameter(src_money_account_id integer, src_gateway_parameter_id integer, dst_money_account_id integer) OWNER TO kbs;

--
-- Name: copy_letter(integer, integer, integer); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.copy_letter(src_event_id integer, src_letter_id integer, dst_event_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	new_info record;
	new_id int := -1;
BEGIN
	for new_info in

with src as (
	select * from letter where (src_letter_id is not null and id = src_letter_id) or (src_letter_id is null and event_id = src_event_id) 
),
row_src as (select row_number() over (order by id),* from src order by id),
new as (
	insert into letter (event_id, type_id,organization_id,account_id,active,name,subject,subject_en,subject_de,subject_fr,subject_es,subject_pt,content,en,de,fr,es,pt,document_condition)
			 	select dst_event_id, type_id,organization_id,account_id,active,name,subject,subject_en,subject_de,subject_fr,subject_es,subject_pt,content,en,de,fr,es,pt,document_condition from row_src
   returning *  
),
row_new as (select row_number() over (order by id),* from new)
select rn.id as new_id, rs.id as src_id from row_new rn join row_src rs on rs.row_number=rn.row_number

	loop
		if (new_id = -1) then
			new_id := new_info.new_id;
		end if;
	end loop;

	return new_id;
END;
$$;


ALTER FUNCTION public.copy_letter(src_event_id integer, src_letter_id integer, dst_event_id integer) OWNER TO kbs;

--
-- Name: copy_money_account(integer, integer, integer); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.copy_money_account(src_event_id integer, src_money_account_id integer, dst_event_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
	DECLARE
		new_info record;
		new_id int := -1;
	BEGIN
		-- copy money accounts with gateway parameters
		for new_info in

			-- copy money accounts
			with src as (
				select * from money_account ma where (src_money_account_id is not null and id = src_money_account_id) or (src_money_account_id is null and event_id = src_event_id)
			),
			row_src as (select row_number() over (order by id),* from src order by id),
			new as (
				insert into money_account (event_id, type_id, currency_id, organization_id, third_party_id, bank_system_account_id, name, trigger_defer_compute_balances, gateway_company_id, closed)
						 	select    dst_event_id, type_id, currency_id, organization_id, third_party_id, bank_system_account_id, name, trigger_defer_compute_balances, gateway_company_id, closed from row_src
			   returning *
			),
			row_new as (select row_number() over (order by id),* from new)
			select rn.id as new_id, rs.id as src_id from row_new rn join row_src rs on rs.row_number=rn.row_number

		loop	-- for each new money account

			-- copy the gateway parameters from src money account
			perform copy_gateway_parameter(new_info.src_id, null, new_info.new_id);

			if (new_id = -1) then
				new_id := new_info.new_id;
			end if;

		end loop;

		return new_id;
	END;
$$;


ALTER FUNCTION public.copy_money_account(src_event_id integer, src_money_account_id integer, dst_event_id integer) OWNER TO kbs;

--
-- Name: copy_money_entities(integer, integer); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.copy_money_entities(src_event_id integer, dst_event_id integer) RETURNS void
    LANGUAGE plpgsql
    AS $$
	BEGIN
		perform copy_money_account(src_event_id, null, dst_event_id);
		perform copy_money_flows(src_event_id, dst_event_id);

		-- renaming money accounts: replacing old event name with new event name
		update money_account ma
			set
				name = replace(ma.name, e_src.name, e.name)
			from
				"event" e,
				"event" e_src
			where
				e.id = ma.event_id and
				e_src.id = src_event_id and
				ma.event_id = dst_event_id;
	END;
$$;


ALTER FUNCTION public.copy_money_entities(src_event_id integer, dst_event_id integer) OWNER TO kbs;

--
-- Name: copy_money_flows(integer, integer); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.copy_money_flows(src_event_id integer, dst_event_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
	DECLARE
		new_info record;
		new_id int := -1;
	BEGIN
		for new_info in
			with
				-- select money accounts from src event mapped to dst event money accounts using the names
				ma_map as (select ma_src.id as src_id, ma_dst.id as dst_id from money_account ma_src join money_account ma_dst on ma_src.name=ma_dst.name where ma_src.event_id=src_event_id and ma_dst.event_id=dst_event_id order by ma_src.id),
				src as (select mf.* from money_flow mf join money_account ma on mf.from_money_account_id=ma.id where ma.event_id=src_event_id),
				row_src as (select row_number() over (order by id),* from src order by id),
				new as (
					insert into money_flow (organization_id, from_money_account_id, to_money_account_id, method_id, auto_transfer_time, trigger_transfer, positive_amounts, negative_amounts)
						-- select money flows from or to src event money accounts and mapping them to dst event money accounts
						select 				organization_id, ma_map_from.dst_id, 	ma_map_to.dst_id, 	 method_id, auto_transfer_time, trigger_transfer, positive_amounts, negative_amounts
							from row_src
								join ma_map as ma_map_from on row_src.from_money_account_id=ma_map_from.src_id
								join ma_map as ma_map_to on row_src.to_money_account_id=ma_map_to.src_id
					returning *
				),
				row_new as (select row_number() over (order by id),* from new)
			select rn.id as new_id, rs.id as src_id from row_new rn join row_src rs on rs.row_number=rn.row_number
		loop
			if (new_id = -1) then
				new_id := new_info.new_id;
			end if;
		end loop;

		return new_id;
	END;
$$;


ALTER FUNCTION public.copy_money_flows(src_event_id integer, dst_event_id integer) OWNER TO kbs;

--
-- Name: copy_option(integer, integer, integer, integer); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.copy_option(src_event_id integer, src_option_id integer, dst_event_id integer, dst_parent_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $_$
DECLARE
	new_info record;
	new_id int := -1;
	new_condition_id int;
	first_new_condition_id int := -1;
	src_event_name text;
	dst_event_name text;
BEGIN
	create temp table copy_option_map (old_id integer, new_id integer);
	select name into src_event_name from event where id=src_event_id;
	select name into dst_event_name from event where id=dst_event_id;

	for new_info in

with recursive src as (
	select o.* from option o where (src_option_id is not null and o.id = src_option_id) or (src_option_id is null and o.event_id = src_event_id and parent_id is null)
	union all
	select o.* from src join option o on o.parent_id=src.id
),
row_src as (select row_number() over (order by id),* from src order by id),
new as (
	insert into option (event_id, online, force_soldout, dev, frame, folder, obligatory, obligatory_agreement, children_radio, radio_traversal, layout, site_id, item_family_id, children_dynamic, sharing_item_id, item_id, rate_id, label_id, name, 											first_pass, second_pass, split_whole_partial, partial_enabled, time_range, date_time_range, floating, first_day, last_day, first_excluded_day, last_excluded_day, min_day, max_day, hide_days, allocate, attendance_option_id, attendance_dates_shift, attendance_document, male_allowed, female_allowed, lay_allowed, ordained_allowed, adult_allowed, child_allowed, min_age, max_age, top_label_id, prompt_label_id, bottom_label_id, children_prompt_label_id, footer_label_id, popup_label_id, ord, hide_per_person, per_day_availability, hide, price_custom, quantity_max, quantity_label_id, age_error_label_id, document_line_comment, visible_condition, cart_group_site_id, cart_group_item_id, cart_group_label_id, cart_group_family_id, selected, online_on, online_off, force_soldout_on, force_soldout_off, first_pass_on, first_pass_off, second_pass_on, second_pass_off)
				select dst_event_id, online, force_soldout, dev, frame, folder, obligatory, obligatory_agreement, children_radio, radio_traversal, layout, site_id, item_family_id, children_dynamic, sharing_item_id, item_id, rate_id, label_id, replace(name, src_event_name, dst_event_name), first_pass, second_pass, split_whole_partial, partial_enabled, time_range, date_time_range, floating, first_day, last_day, first_excluded_day, last_excluded_day, min_day, max_day, hide_days, allocate, attendance_option_id, attendance_dates_shift, attendance_document, male_allowed, female_allowed, lay_allowed, ordained_allowed, adult_allowed, child_allowed, min_age, max_age, top_label_id, prompt_label_id, bottom_label_id, children_prompt_label_id, footer_label_id, popup_label_id, ord, hide_per_person, per_day_availability, hide, price_custom, quantity_max, quantity_label_id, age_error_label_id, document_line_comment, visible_condition, cart_group_site_id, cart_group_item_id, cart_group_label_id, cart_group_family_id, selected, online_on, online_off, force_soldout_on, force_soldout_off, first_pass_on, first_pass_off, second_pass_on, second_pass_off from row_src
   returning *
),
row_new as (select row_number() over (order by id),* from new)
select rn.id as new_id, prn.id as new_parent_id, arn.id as new_attendance_option_id, rs.id as old_id from row_new rn join row_src rs on rs.row_number=rn.row_number left join row_src prs on prs.id=rs.parent_id left join row_new prn on prn.row_number=prs.row_number left join row_src ars on ars.id=rn.attendance_option_id left join row_new arn on arn.row_number=ars.row_number

	loop
		update option ou set
				parent_id = new_info.new_parent_id,
				attendance_option_id = new_info.new_attendance_option_id,
				site_id = case when ou.site_id is null then null else coalesce(
									-- keeping the site if not specific to an event (organization global site)
									(select s0.id from site s0 where s0.id=ou.site_id and s0.event_id is null),
									-- mapping to local event site with same name if any
									(select s1.id from site s1 join site s0 on s0.id=ou.site_id where s1.event_id=ou.event_id and s1.name=s0.name),
									-- mapping to local event site having a name with the same prefix but ending with the event id
									(select s1.id from site s1 join site s0 on s0.id=ou.site_id where s1.event_id=ou.event_id and s1.name=substring(s0.name from '(.*)' || s0.event_id || '$') || s1.event_id),
									-- copying the site to the local event
									copy_site(src_event_id, ou.site_id, ou.event_id)
								) end,
				cart_group_site_id = case when ou.cart_group_site_id is null then null else coalesce(
									-- keeping the site if not specific to an event (organization global site)
									(select s0.id from site s0 where s0.id=ou.cart_group_site_id and s0.event_id is null),
									-- mapping to local event site with same name if any
									(select s1.id from site s1 join site s0 on s0.id=ou.cart_group_site_id where s1.event_id=ou.event_id and s1.name=s0.name),
									-- mapping to local event site having a name with the same prefix but ending with the event id
									(select s1.id from site s1 join site s0 on s0.id=ou.cart_group_site_id where s1.event_id=ou.event_id and s1.name=substring(s0.name from '(.*)' || s0.event_id || '$') || s1.event_id),
									-- copying the site to the local event
									copy_site(src_event_id, ou.cart_group_site_id, ou.event_id)
								) end
		 	where id=new_info.new_id;

		-- copy option conditions
		select copy_option_condition(new_info.old_id, new_info.new_id) into new_condition_id;

		-- storing the id of the first new option conditions for future update of all new option conditions if_option_id
		if (first_new_condition_id = -1 and new_condition_id > 0) then
			first_new_condition_id := new_condition_id;
		end if;

		-- creating the map of the old options id to new options id
		insert into copy_option_map values (new_info.old_id, new_info.new_id);

		if (new_id = -1) then
			new_id := new_info.new_id;
			update option set parent_id = dst_parent_id where id=new_info.new_id;
		end if;
	end loop;

	-- if at least one option condition has been created
	if (first_new_condition_id > 0) then
		-- update option_condition.if_option_id for newly created option conditions
		update option_condition set
			if_option_id = comap.new_id
			from copy_option_map comap
			where
				comap.old_id = option_condition.if_option_id and
				option_condition.id >= first_new_condition_id;
	end if;

	drop table copy_option_map;

	return new_id;
END;
$_$;


ALTER FUNCTION public.copy_option(src_event_id integer, src_option_id integer, dst_event_id integer, dst_parent_id integer) OWNER TO kbs;

--
-- Name: copy_option_condition(integer, integer); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.copy_option_condition(src_option_id integer, dst_option_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $_$
DECLARE
	new_info record;
	new_id int := -1;
	dst_event_id int;
BEGIN
    select into dst_event_id event_id from option where id = dst_option_id;
	for new_info in

with recursive src as (
	select oc.* from option_condition oc where oc.option_id = src_option_id and oc.parent_id is null
	union all
	select oc.* from src join option_condition oc on oc.parent_id=src.id
),
row_src as (select row_number() over (order by id),* from src order by id),
new as (
	insert into option_condition (option_id, parent_id, is_group, is_group_or, is_not, if_option_id, first_day, last_day, any_day, if_main_site, if_not_main_site, if_item_family_id, if_site_id)
				          select dst_option_id, null,      is_group, is_group_or, is_not, if_option_id, first_day, last_day, any_day, if_main_site, if_not_main_site, if_item_family_id, if_site_id from row_src
   returning *  
),
row_new as (select row_number() over (order by id),* from new)
select rn.id as new_id, prn.id as new_parent_id from row_new rn join row_src rs on rs.row_number=rn.row_number left join row_src prs on prs.id=rs.parent_id left join row_new prn on prn.row_number=prs.row_number

	loop
		update option_condition ocu set
				parent_id = new_info.new_parent_id,
				if_site_id = case when ocu.if_site_id is null then null else coalesce(
									-- keeping the site if not specific to an event (organization global site)
									(select s0.id from site s0 where s0.id=ocu.if_site_id and s0.event_id is null),
									-- mapping to local event site with same name if any
									(select s1.id from site s1 join site s0 on s0.id=ocu.if_site_id where s1.event_id=dst_event_id and s1.name=s0.name),
									-- mapping to local event site having a name with the same prefix but ending with the event id
									(select s1.id from site s1 join site s0 on s0.id=ocu.if_site_id where s1.event_id=dst_event_id and s1.name=substring(s0.name from '(.*)' || s0.event_id || '$') || s1.event_id)
								) end
		 	where id=new_info.new_id;

		if (new_id = -1) then
			new_id := new_info.new_id;
		end if;
	end loop;

	return new_id;
END;

$_$;


ALTER FUNCTION public.copy_option_condition(src_option_id integer, dst_option_id integer) OWNER TO kbs;

--
-- Name: copy_rate(integer, integer, integer); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.copy_rate(src_site_item_family_id integer, src_rate_id integer, dst_site_item_family_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	new_info record;
	new_id int := -1;
	src_site_id int;
	dst_site_id int;
	src_family_id int;
BEGIN
 	 select into src_site_id site_id from site_item_family where id=src_site_item_family_id;
 	 select into dst_site_id site_id from site_item_family where id=dst_site_item_family_id;
 	 select into src_family_id item_family_id from site_item_family where id=src_site_item_family_id;

    for new_info in

with src as (
	select r.* from rate r join item i on i.id=r.item_id where (src_rate_id is not null and r.id = src_rate_id) or (src_rate_id is null and r.site_id=src_site_id and i.family_id = src_family_id)
),
row_src as (select row_number() over (order by id),* from src order by id),
new as (
	insert into rate (item_id, sale, package_id, site_id,     live, comment, start_date, end_date, on_date, off_date, currency_id, price_novat, vat_id, price, per_day, per_person, first_day, last_day, min_day, max_day, min_deposit, non_refundable, cutoff_date, min_deposit2, non_refundable2, cutoff_date2, min_deposit3, non_refundable3, cutoff_date3, min_deposit4, non_refundable4, cutoff_date4, min_deposit5, non_refundable5, cutoff_date5, min_deposit6, non_refundable6, organization_id, min_age, max_age, age1_max, age1_price, age1_discount, age2_max, age2_price, age2_discount, age3_max, age3_price, age3_discount, resident_price, resident_discount, resident2_price, resident2_discount, unemployed_price, unemployed_discount, facility_fee_price, facility_fee_discount, arriving_or_leaving, comment_label_id)
	           select item_id, sale, package_id, dst_site_id, live, comment, start_date, end_date, on_date, off_date, currency_id, price_novat, vat_id, price, per_day, per_person, first_day, last_day, min_day, max_day, min_deposit, non_refundable, cutoff_date, min_deposit2, non_refundable2, cutoff_date2, min_deposit3, non_refundable3, cutoff_date3, min_deposit4, non_refundable4, cutoff_date4, min_deposit5, non_refundable5, cutoff_date5, min_deposit6, non_refundable6, organization_id, min_age, max_age, age1_max, age1_price, age1_discount, age2_max, age2_price, age2_discount, age3_max, age3_price, age3_discount, resident_price, resident_discount, resident2_price, resident2_discount, unemployed_price, unemployed_discount, facility_fee_price, facility_fee_discount, arriving_or_leaving, comment_label_id from row_src
   returning *
),
row_new as (select row_number() over (order by id),* from new)
select rn.id as new_id, rs.id as src_id from row_new rn join row_src rs on rs.row_number=rn.row_number

	 loop
		if (new_id = -1) then
			new_id := new_info.new_id;
		end if;
    end loop;

	 return new_id;
END;
$$;


ALTER FUNCTION public.copy_rate(src_site_item_family_id integer, src_rate_id integer, dst_site_item_family_id integer) OWNER TO kbs;

--
-- Name: copy_resource(integer, integer, integer); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.copy_resource(src_site_item_family_id integer, src_resource_id integer, dst_site_item_family_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	new_info record;
	new_id int := -1;
BEGIN
    for new_info in

with src as (
	select * from resource where (src_resource_id is not null and id = src_resource_id) or (src_resource_id is null and site_item_family_id = src_site_item_family_id) 
),
row_src as (select row_number() over (order by id),* from src order by id),
new as (
	insert into resource (site_item_family_id, name)
              select dst_site_item_family_id, name from row_src
   returning *  
),
row_new as (select row_number() over (order by id),* from new)
select rn.id as new_id, rs.id as src_id from row_new rn join row_src rs on rs.row_number=rn.row_number

	 loop
		if (new_id = -1) then
			new_id := new_info.new_id;
		end if;
		perform copy_resource_configuration(new_info.src_id, null, new_info.new_id);
    end loop;

	 return new_id;
END;
$$;


ALTER FUNCTION public.copy_resource(src_site_item_family_id integer, src_resource_id integer, dst_site_item_family_id integer) OWNER TO kbs;

--
-- Name: copy_resource_configuration(integer, integer, integer); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.copy_resource_configuration(src_resource_id integer, src_resource_configuration_id integer, dst_resource_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	new_info record;new_id int := -1;BEGIN
    for new_info in

with src as (
	select * from resource_configuration where (src_resource_configuration_id is not null and id = src_resource_configuration_id) or (src_resource_configuration_id is null and resource_id = src_resource_id) 
),
row_src as (select row_number() over (order by id),* from src order by id),
new as (
	insert into resource_configuration (resource_id, name, item_id, start_date, end_date, online, max, max_paper, max_private, comment)
              					 select dst_resource_id, name, item_id, start_date, end_date, online, max, max_paper, max_private, comment from row_src
   returning *  
),
row_new as (select row_number() over (order by id),* from new)
select rn.id as new_id, rs.id as src_id from row_new rn join row_src rs on rs.row_number=rn.row_number

	 loop
		if (new_id = -1) then
			new_id := new_info.new_id;end if;end loop;return new_id;END;$$;


ALTER FUNCTION public.copy_resource_configuration(src_resource_id integer, src_resource_configuration_id integer, dst_resource_id integer) OWNER TO kbs;

--
-- Name: copy_site(integer, integer, integer); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.copy_site(src_event_id integer, src_site_id integer, dst_event_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $_$
DECLARE
	new_info record;
	new_id int := -1;
	name_prefix text;
BEGIN
	for new_info in

with src as (
	select * from site where (src_site_id is not null and id = src_site_id) or (src_site_id is null and event_id = src_event_id) 
),
row_src as (select row_number() over (order by id),* from src order by id),
new as (
	insert into site (event_id, organization_id, name, online, force_soldout, label_id, item_family_id, image_url, top_label_id, group_name, address, asks_for_passport, hide_dates, ord, main)
			 select dst_event_id, organization_id, name, online, force_soldout, label_id, item_family_id, image_url, top_label_id, group_name, address, asks_for_passport, hide_dates, ord, main from row_src
   returning *  
),
row_new as (select row_number() over (order by id),* from new)
select rn.id as new_id, rs.id as src_id, rn.name from row_new rn join row_src rs on rs.row_number=rn.row_number

	loop
		if (new_id = -1) then
			new_id := new_info.new_id;
		end if;
		perform copy_site_item_family(new_info.src_id, null, new_info.new_id);

		-- renaming site if its name ends with the event id number
		name_prefix := substring(new_info.name from '(.*)' || coalesce(src_event_id, (select event_id from site where id=src_site_id)) || '$');
		if (name_prefix is not null) then
			update site set name=name_prefix || dst_event_id where id=new_info.new_id; 
		end if;

	end loop;

	return new_id;
END;
$_$;


ALTER FUNCTION public.copy_site(src_event_id integer, src_site_id integer, dst_event_id integer) OWNER TO kbs;

--
-- Name: copy_site_item_family(integer, integer, integer); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.copy_site_item_family(src_site_id integer, src_site_item_family_id integer, dst_site_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	new_info record;
	new_id int := -1;
BEGIN
    for new_info in

with src as (
	select * from site_item_family where (src_site_item_family_id is not null and id = src_site_item_family_id) or (src_site_item_family_id is null and site_id = src_site_id) 
),
row_src as (select row_number() over (order by id),* from src order by id),
new as (
	insert into site_item_family (site_id, item_family_id, auto_release)
		                select dst_site_id, item_family_id, auto_release from row_src
   returning *  
),
row_new as (select row_number() over (order by id),* from new)
select rn.id as new_id, rs.id as src_id from row_new rn join row_src rs on rs.row_number=rn.row_number

	 loop
		if (new_id = -1) then
			new_id := new_info.new_id;
		end if;
		perform copy_rate(new_info.src_id, null, new_info.new_id);
		perform copy_resource(new_info.src_id, null, new_info.new_id);
    end loop;

	 return new_id;
END;
$$;


ALTER FUNCTION public.copy_site_item_family(src_site_id integer, src_site_item_family_id integer, dst_site_id integer) OWNER TO kbs;

--
-- Name: deferred_allocate_document_line(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.deferred_allocate_document_line() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
forced_soldout bool := false;
rcid document_line.resource_configuration_id%TYPE;
doc document%ROWTYPE;
fcode item_family.code%TYPE;
backend_request bool;
BEGIN

IF (OLD.trigger_defer_allocate = false and NEW.trigger_defer_allocate = true) THEN

   RAISE NOTICE 'Entering trigger %.%(%)', TG_RELNAME, TG_NAME, NEW.id;

-- first reason to raise soldout is when the associated option has been forced to be sold out
IF (NEW.allocate) THEN -- skipping this control if allocate is unticked (allocate = 'Check if soldout' in the backend)
select into forced_soldout force_soldout from option where force_soldout and site_id=NEW.site_id and item_id=NEW.item_id and (NEW.resource_configuration_id is null or (select item_id from resource_configuration where id=NEW.resource_configuration_id)<>NEW.item_id);
if forced_soldout then
RAISE EXCEPTION 'SOLDOUT site_id=%, item_id=% (option forced as soldout)', NEW.site_id, NEW.item_id;
end if;
END IF;

if (not NEW.lock_allocation) then

select into doc * from document d where d.id=NEW.document_id; -- used to allocation rules criteria

-- trying to allocate through automatic allocation rules (usually for meals dining areas)
select into rcid resource_configuration_id from allocation_rule ar
left join language l on l.id=ar.if_language_id
left join organization o on o.id=doc.person_organization_id
join item i on i.id=NEW.item_id
join resource_configuration rc on rc.id=ar.resource_configuration_id
join event e on e.id=doc.event_id
where ar.active and ar.event_id=doc.event_id and (NEW.resource_configuration_id is null or not doc.arrived) and ar.item_family_id=i.family_id and rc.item_id=i.id
 and (ar.if_language_id is null or l.iso_639_1 = doc.person_lang)
 and (ar.if_country_id is null or o.country_id = ar.if_country_id)
 and (ar.if_organization_id is null or doc.person_organization_id = ar.if_organization_id)
 and (not ar.if_child or doc.person_age is not null)
 and (not ar.if_carer or exists(select * from document where not cancelled and (person_carer1_document_id=doc.id or person_carer2_document_id=doc.id)))
          and (not ar.if_lay or not doc.person_ordained)
          and (not ar.if_ordained or doc.person_ordained)
 and (ar.if_ref_min is null or doc.ref >= ar.if_ref_min)
 and (ar.if_ref_max is null or doc.ref <= ar.if_ref_max)
 and (ar.if_site_id is null and ar.if_item_id is null or (ar.if_site_id is null or NEW.site_id = ar.if_site_id) and (ar.if_item_id is null or NEW.item_id = ar.if_item_id) or exists(select * from document_line dl2 where document_id=doc.id and not cancelled and (ar.if_site_id is null or dl2.site_id = ar.if_site_id) and (ar.if_item_id is null or dl2.item_id = ar.if_item_id)))
 and (not ar.if_whole_event or not exists(select * from event e, generate_dates(e.start_date, e.end_date) d where e.id=doc.event_id and not exists(select * from attendance a join document_line dl on dl.id=a.document_line_id where dl.document_id=NEW.document_id and date=d)))
 and (not ar.if_partial_event or   exists(select * from event e, generate_dates(e.start_date, e.end_date) d where e.id=doc.event_id and not exists(select * from attendance a join document_line dl on dl.id=a.document_line_id where dl.document_id=NEW.document_id and date=d)))
order by ar.ord limit 1;

IF NOT FOUND THEN
         backend_request := get_transaction_parameter();
--- selecting all dates related to this resource allocation
with dates as (select date from attendance a where a.document_line_id=NEW.id),
--- then all resources live on this period of time (but excluding offline resources for the frontend)
date_resource_info as (select d.date, rc.id as resource_configuration_id, r.site_id, rc.item_id, rc.max, rc.online from dates d, resource_configuration rc join resource r on rc.resource_id=r.id join site s on r.site_id=s.id join item i on i.id=NEW.item_id join item rci on rci.id=rc.item_id join event e on e.id=doc.event_id where overlaps(d.date, d.date, rc.start_date, rc.end_date) and s.id=NEW.site_id and rc.item_id=i.id and (backend_request or rc.online)),
--- then the current number of reservations (excluding itself) for each day and each resources
date_resource_info_with_current as (select dri.date, dri.resource_configuration_id, dri.site_id, dri.item_id, (select coalesce(sum(dl.quantity),0) from attendance a join document_line dl on a.document_line_id=dl.id join document d on d.id=dl.document_id where a.present and a.date=dri.date and dl.id<>NEW.id and dl.share_mate_owner_document_line_id is distinct from NEW.id and dl.resource_configuration_id=dri.resource_configuration_id and (backend_request and not backend_released or not backend_request and not frontend_released)) as current, dri.max, dri.online from date_resource_info dri)
select into rcid resource_configuration_id from date_resource_info_with_current group by resource_configuration_id having min(max-current)>=NEW.share_owner_quantity order by case when resource_configuration_id=NEW.resource_configuration_id then 0 else 1 end, first(online) desc, resource_configuration_id limit 1;
END IF;

IF NOT FOUND THEN
IF (NEW.allocate) THEN
select into fcode f.code from item i join item_family f on f.id=i.family_id where i.id=NEW.item_id;
with dates as (select date from attendance a where a.document_line_id=NEW.id),
date_resource_info as (select d.date, rc.id as resource_configuration_id, r.site_id, rc.item_id, rc.max from dates d, resource_configuration rc join resource r on rc.resource_id=r.id join site s on r.site_id=s.id where overlaps(d.date, d.date, rc.start_date, rc.end_date) and s.id=NEW.site_id and rc.item_id=NEW.item_id)
select into rcid resource_configuration_id from date_resource_info limit 1;
IF FOUND THEN -- not found means no resource at all => we skip soldout control in that case
RAISE EXCEPTION 'SOLDOUT site_id=%, item_id=% (no resource found)', NEW.site_id, NEW.item_id;
END IF;
END IF;
rcid := null;
END IF;

end if;

if (backend_request and NEW.resource_configuration_id <> rcid) then -- forcing notification to update the booking editor user interface with the new allocation name
INSERT into sys_log (table_name, update, oid, column_name) values ('resource_configuration', true, rcid, 'resource_id');
INSERT into sys_log (table_name, update, oid, column_name) values ('resource', true, (select resource_id from resource_configuration where id=rcid), 'name');
end if;

   -- update the table
   update document_line set
resource_configuration_id = rcid,
system_allocated = case when OLD.resource_configuration_id = rcid then system_allocated else NEW.resource_configuration_id is distinct from rcid end, -- this indicates that the system changed the allocation
trigger_defer_allocate = false
      where id=NEW.id;


END IF;

RETURN NEW;
END;
$$;


ALTER FUNCTION public.deferred_allocate_document_line() OWNER TO kbs;

--
-- Name: deferred_compute_document_dates(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.deferred_compute_document_dates() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN

	IF ((TG_OP='INSERT' OR OLD.trigger_defer_compute_dates = false) and NEW.trigger_defer_compute_dates = true) THEN

	   RAISE NOTICE 'Entering trigger %.%(%)', TG_RELNAME, TG_NAME, NEW.id;

	   -- compute document prices
   	update document as d set
			dates = (select compute_dates(array((select date from attendance a join document_line dl on dl.id=a.document_line_id where dl.document_id=d.id and (d.cancelled or not dl.cancelled) and a.present group by date order by date))) from Document limit 1),
			trigger_defer_compute_dates = false
	      where d.id=NEW.id;

   END IF;

	RETURN NEW;
END;
$$;


ALTER FUNCTION public.deferred_compute_document_dates() OWNER TO kbs;

--
-- Name: deferred_compute_document_deposit(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.deferred_compute_document_deposit() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN

	IF (OLD.trigger_defer_compute_deposit = false and NEW.trigger_defer_compute_deposit = true) THEN

	   RAISE NOTICE 'Entering trigger %.%(%)', TG_RELNAME, TG_NAME, NEW.id;

	   -- compute document deposit
   	update document as d set
      	price_deposit = COALESCE((select sum(amount) from money_transfer where document_id=d.id and pending=false and successful=true group by document_id), 0),
			trigger_defer_compute_deposit = false
	      where d.id=NEW.id;

   END IF;

	RETURN NEW;
END;
$$;


ALTER FUNCTION public.deferred_compute_document_deposit() OWNER TO kbs;

--
-- Name: deferred_compute_document_lines_deposit(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.deferred_compute_document_lines_deposit() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN

	IF ((TG_OP='INSERT' OR OLD.trigger_defer_compute_lines_deposit = false) and NEW.trigger_defer_compute_lines_deposit = true) THEN

	   RAISE NOTICE 'Entering trigger %.%(%)', TG_RELNAME, TG_NAME, NEW.id;if (NEW.price_deposit <= 0) then
			update document_line set price_deposit = 0 where document_id=NEW.id;elsif (NEW.price_deposit >= NEW.price_net) then
			update document_line set price_deposit = coalesce(price_net,0) where document_id=NEW.id;elsif (NEW.price_deposit = NEW.price_min_deposit) then
			update document_line set price_deposit = coalesce(price_min_deposit,0) where document_id=NEW.id;elsif (NEW.price_deposit < NEW.price_min_deposit) then
			update document_line dlu set price_deposit = GREATEST(0, LEAST(coalesce(dlu.price_min_deposit,0),                                                 NEW.price_deposit                         - coalesce((select sum(dlp.price_min_deposit)                           from document_line dl join item i on i.id=dl.item_id join item_family f on f.id=i.family_id, document_line dlp join item ip on ip.id=dlp.item_id join item_family fp on fp.id=ip.family_id where dl.id=dlu.id and dlp.document_id=NEW.id and (dlp.creation_date < dl.creation_date or dlp.creation_date=dl.creation_date and (fp.ord<f.ord or fp.ord=f.ord and dlp.id<dl.id))),0))) where dlu.document_id=NEW.id;else
			update document_line dlu set price_deposit = GREATEST(coalesce(dlu.price_min_deposit,0), LEAST(dlu.price_net, coalesce(dlu.price_min_deposit,0) + NEW.price_deposit - NEW.price_min_deposit - coalesce((select sum(dlp.price_net-coalesce(dlp.price_min_deposit,0)) from document_line dl join item i on i.id=dl.item_id join item_family f on f.id=i.family_id, document_line dlp join item ip on ip.id=dlp.item_id join item_family fp on fp.id=ip.family_id where dl.id=dlu.id and dlp.document_id=NEW.id and (dlp.creation_date < dl.creation_date or dlp.creation_date=dl.creation_date and (fp.ord<f.ord or fp.ord=f.ord and dlp.id<dl.id))),0))) where dlu.document_id=NEW.id;end if;update document as d set
			trigger_defer_compute_lines_deposit = false
	      where d.id=NEW.id;END IF;RETURN NEW;END;$$;


ALTER FUNCTION public.deferred_compute_document_lines_deposit() OWNER TO kbs;

--
-- Name: deferred_compute_document_prices(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.deferred_compute_document_prices() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
 DECLARE
 		net document.price_net%TYPE;
 		p_id package.id%TYPE;
 BEGIN

 	IF ((TG_OP='INSERT' OR OLD.trigger_defer_compute_prices = false) and NEW.trigger_defer_compute_prices = true) THEN

 	   RAISE NOTICE 'Entering trigger %.%(%)', TG_RELNAME, TG_NAME, NEW.id;

 		-- we take the opportunity to update the quantity for resource allocation (automatically set for owners, manually for other cases)
 		update document_line as dlo
 			set quantity = greatest(1, dlo.share_owner_quantity - COALESCE((select sum(dlm.quantity) from document_line dlm where dlm.share_mate_owner_document_line_id=dlo.id), 0))
 			where dlo.document_id=NEW.id and dlo.share_owner = true;

 /*
 		-- searching best package offers
 		LOOP
 			select into p_id id from package p where not exists(select * from package_item pi where package_id=p.id and not exists(select * from document_line where document_id=NEW.id and not cancelled and item_id=pi.item_id));
 			if not found then
 				exit;
 			end if;
 			EXIT;
 			--RAISE EXCEPTION 'Package found!!!';
 		END LOOP;
 */

 /* OLD PRICE ALGORITHM
 		-- compute price for each document line and return net price (sum for all lines)
 		select into net coalesce(sum(compute_document_line_price(d, dl, true)), 0) from document_line dl join document d on d.id=dl.document_id where d.id=NEW.id;

 	   -- compute document prices
    	update document as d set
       	price_net = net, -- COALESCE((select sum(price_net) from document_line where document_id=d.id group by document_id), 0),
 	      price_min_deposit = COALESCE((select sum(price_min_deposit) from document_line where document_id=d.id group by document_id), 0),
    	   price_non_refundable = COALESCE((select sum(price_non_refundable) from document_line where document_id=d.id group by document_id), 0),
 			trigger_defer_compute_prices = false
 	      where d.id=NEW.id;
 */

 		-- compute document prices (and also document_line prices)
 		perform compute_document_prices(NEW.id, false); -- NEW PRICE ALHGORITHM

 	   -- compute document dates
    	update document as d set
 			dates = (select compute_dates(array((select date from attendance a join document_line dl on dl.id=a.document_line_id where dl.document_id=d.id and (d.cancelled or not dl.cancelled) and a.present group by date order by date))) from Document limit 1)
 	      where d.id=NEW.id;

 	   -- also document_line dates
 		update document_line as dlu set
 			dates = (case when (select hide_dates from site s where s.id=dlu.site_id) then null else compute_dates(array(select date from attendance where document_line_id=dlu.id and present order by date)) end)
 			where dlu.document_id=NEW.id;

    END IF;

 	RETURN NEW;
 END;
 $$;


ALTER FUNCTION public.deferred_compute_document_prices() OWNER TO kbs;

--
-- Name: deferred_compute_money_account_balances(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.deferred_compute_money_account_balances() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
	mt money_transfer%ROWTYPE;
	pt money_transfer%ROWTYPE; -- previous money transfer
	from_date timestamp;
	balance int;
BEGIN

	IF (OLD.trigger_defer_compute_balances=false AND NEW.trigger_defer_compute_balances=true) THEN

	   RAISE NOTICE 'Entering %.%(%)', TG_RELNAME, TG_NAME, NEW.id;

		-- get the date from which we need to update the balances for this account
		select into from_date date from money_account_trigger_info where money_account_id=NEW.id;

		-- find the last money_transfer before the triggered date and start from that balance
		SELECT INTO mt * FROM money_transfer WHERE date < from_date AND not pending and successful AND (from_money_account_id=NEW.id OR to_money_account_id=NEW.id) ORDER BY date DESC,id DESC LIMIT 1;
		IF NOT FOUND THEN
			balance := 0;
		ELSIF mt.from_money_account_id=NEW.id THEN
			balance := COALESCE(mt.from_money_account_balance, 0);
		ELSE
			balance := COALESCE(mt.to_money_account_balance, 0);
		END IF;

		-- update all money_transfer balances after that date
		FOR mt IN SELECT * FROM money_transfer WHERE date >= from_date AND (from_money_account_id=NEW.id OR to_money_account_id=NEW.id) ORDER BY date, id 
		LOOP
			IF mt.from_money_account_id=NEW.id THEN
				IF not mt.pending and mt.successful THEN
					IF (mt.receipts_transfer and mt.amount<>balance) THEN
						update money_transfer set amount = balance WHERE id=mt.id;
						pt := null;
						select into pt * from money_transfer where receipts_transfer=true and from_money_account_id=mt.from_money_account_id and date < mt.date order by date desc limit 1;
						update money_transfer set transfer_id=mt.id where parent_id is null and receipts_transfer=false and to_money_account_id=mt.from_money_account_id and method_id=mt.method_id and date <= mt.date and (pt is null or date > pt.date);
						update money_transfer set transfer_id=null where transfer_id=mt.id and date > mt.date;
						mt.amount := balance;
					END IF;
					balance := balance - mt.amount;
				END IF;
				update money_transfer set from_money_account_balance = balance WHERE id=mt.id and (from_money_account_balance is null or from_money_account_balance <> balance);
				IF NEW.closed and FOUND THEN
					RAISE EXCEPTION '''%'' money account is closed', NEW.name;
				END IF;
			ELSE
				IF not mt.pending and mt.successful THEN
					balance := balance + mt.amount;
				END IF;
				update money_transfer set to_money_account_balance = balance WHERE id=mt.id and (to_money_account_balance is null or to_money_account_balance <> balance);
				IF NEW.closed and FOUND THEN
					RAISE EXCEPTION '''%'' money account is closed', NEW.name;
				END IF;
			END IF;		
		END LOOP;

	   -- reset trigger
   	UPDATE money_account SET trigger_defer_compute_balances = false WHERE id=NEW.id;

   END IF;

	RETURN NEW;
END $$;


ALTER FUNCTION public.deferred_compute_money_account_balances() OWNER TO kbs;

--
-- Name: deferred_send_history_email(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.deferred_send_history_email() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
	ma mail_account%ROWTYPE;subject mail.subject%TYPE;body mail.content%TYPE;BEGIN
   RAISE NOTICE 'Entering trigger %.%(%)', TG_RELNAME, TG_NAME, NEW.id;SELECT INTO ma * FROM mail_account m_a join document d on d.id=NEW.document_id where m_a.organization_id=d.organization_id order by case when m_a.event_id=d.event_id then 0 else m_a.id end LIMIT 1;IF FOUND THEN
		subject := substring(interpret_brackets('KBS2 [eventName] ([eventId]) - [fullName] #[ref] ' || NEW.comment, NEW.document_id, 'en') for 255);body := interpret_brackets('<html><body>' || NEW.comment || '<hr/>' || case when NEW.request is not null then NEW.request || '<hr/>' else '' end || '[personalDetails]<hr/>[options]<hr/>Invoiced: [invoiced]<br/>Deposit: [deposit]<br/>Balance: [balance]<hr/>[yourCart]</body></html>', NEW.document_id, 'en');INSERT INTO mail (account_id, letter_id, document_id, background, subject, content, read, out, auto_delete) values (ma.id, null, NEW.document_id, true, subject, body, true, false, true);END IF;RETURN NEW;END $$;


ALTER FUNCTION public.deferred_send_history_email() OWNER TO kbs;

--
-- Name: deferred_update_spread_money_transfer(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.deferred_update_spread_money_transfer() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
	new_amount money_transfer.amount%TYPE;
	new_from_money_account_id money_transfer.from_money_account_id%TYPE;
	new_to_money_account_id money_transfer.to_money_account_id%TYPE;
BEGIN

	IF (OLD.trigger_defer_update_spread_money_transfer = false and NEW.trigger_defer_update_spread_money_transfer = true) THEN

	   RAISE NOTICE 'Entering trigger %.%(%)', TG_RELNAME, TG_NAME, NEW.id;

		SELECT INTO new_amount COALESCE((select sum(amount) from money_transfer mt where mt.parent_id=NEW.id group by mt.parent_id), 0);
	   RAISE NOTICE 'amount = %', new_amount;
		new_from_money_account_id := NEW.from_money_account_id;
		IF (new_from_money_account_id is null) THEN
			SELECT INTO new_from_money_account_id autoset_money_transfer_from_account(NEW, false);
	   	RAISE NOTICE 'from_money_account_id = %', new_from_money_account_id;
		END IF;
		new_to_money_account_id := NEW.to_money_account_id;
		IF (new_to_money_account_id is null) THEN
			NEW.from_money_account_id := new_from_money_account_id;
			SELECT INTO new_to_money_account_id autoset_money_transfer_to_account(NEW, false);
	   	RAISE NOTICE 'to_money_account_id = %', new_to_money_account_id;
		END IF;
		if (new_amount<>NEW.amount or NEW.from_money_account_id is null or new_from_money_account_id<>NEW.from_money_account_id or NEW.to_money_account_id is null or new_to_money_account_id<>NEW.to_money_account_id) then
			update money_transfer set
   	   	amount = new_amount,
				from_money_account_id = new_from_money_account_id,
				to_money_account_id = new_to_money_account_id,
				trigger_defer_update_spread_money_transfer = false
				where id=NEW.id;
		else
			update money_transfer set
				trigger_defer_update_spread_money_transfer = false
				where id=NEW.id;
		end if;

   END IF;

	RETURN NEW;
END;
$$;


ALTER FUNCTION public.deferred_update_spread_money_transfer() OWNER TO kbs;

--
-- Name: first_agg(anyelement, anyelement); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.first_agg(anyelement, anyelement) RETURNS anyelement
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
        SELECT $1;
$_$;


ALTER FUNCTION public.first_agg(anyelement, anyelement) OWNER TO kbs;

--
-- Name: generate_dates(date, date); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.generate_dates(date, date) RETURNS SETOF date
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$select d.a::date from generate_series($1::timestamp, $2, '1 day') as d(a)$_$;


ALTER FUNCTION public.generate_dates(date, date) OWNER TO kbs;

--
-- Name: generate_event_dates(integer); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.generate_event_dates(integer) RETURNS SETOF date
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$select generate_dates(coalesce(e.pre_date,e.start_date), coalesce(e.post_date,e.end_date)) from event e where e.id=$1$_$;


ALTER FUNCTION public.generate_event_dates(integer) OWNER TO kbs;

--
-- Name: get_transaction_parameter(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.get_transaction_parameter() RETURNS boolean
    LANGUAGE plpgsql
    AS $$
DECLARE
    is_backend bool := false;
BEGIN
    select into is_backend backend from transaction_parameter;
    RETURN is_backend;
END;
$$;


ALTER FUNCTION public.get_transaction_parameter() OWNER TO kbs;

--
-- Name: install_table_notification(text, boolean); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.install_table_notification(tname text, also_insert boolean) RETURNS text
    LANGUAGE plpgsql
    AS $_$
DECLARE
	ri RECORD;s TEXT;BEGIN
	s := format(E'CREATE OR REPLACE FUNCTION record_changes_for_notification_%s() RETURNS TRIGGER AS \$\$\nBEGIN\n', tname);FOR ri IN SELECT column_name FROM information_schema.columns
					WHERE table_name = quote_ident(tname) AND (column_name<>'id' and column_name NOT LIKE 'trigger_%' OR column_name in ('trigger_allocate')) -- allocation rule trigger
					ORDER BY ordinal_position
	LOOP
		s := s || format(E'\nIF (TG_OP = ''INSERT'' OR TG_OP = ''UPDATE'' AND OLD.%2$I IS DISTINCT FROM NEW.%2$I) THEN INSERT into sys_log (table_name, update, oid, column_name) values (%1$L, true, NEW.id, %2$L); END IF;', tname, ri.column_name);END LOOP;s := s || E'\nRETURN NEW;\nEND;\n\$\$ LANGUAGE plpgsql;';s := s || E'\nCREATE TRIGGER record_changes_for_notification AFTER UPDATE' || case when also_insert then ' OR INSERT' else '' end;s := s || format(E' ON %1$s FOR EACH ROW EXECUTE PROCEDURE record_changes_for_notification_%1$s();', tname);EXECUTE s;RETURN s;END;$_$;


ALTER FUNCTION public.install_table_notification(tname text, also_insert boolean) OWNER TO kbs;

--
-- Name: interpret_brackets(text, integer, character); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.interpret_brackets(txt text, docid integer, language character) RETURNS text
    LANGUAGE plpgsql
    AS $_$
DECLARE
	br bracket_pattern%ROWTYPE;
	pattern text;
	replacement text;
BEGIN
	for br in select * from bracket_pattern bp where bp.lang is null or bp.lang=language order by ord
	loop
		pattern := '[' || br.pattern || ']';
		if (position(pattern in txt) > 0) then
			replacement := null;
			EXECUTE 'select (' || br.replacement || ') from document d where d.id=$1' || case when br.condition is null then '' else ' and (' || br.condition || ')' end into replacement using docId, language;
			if (replacement is not null) then
				txt := replace(txt, pattern, replacement);
			end if;
		end if;
	end loop;
	RETURN txt;
END;
$_$;


ALTER FUNCTION public.interpret_brackets(txt text, docid integer, language character) OWNER TO kbs;

--
-- Name: interpret_brakets(text, integer, character); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.interpret_brakets(txt text, docid integer, language character) RETURNS text
    LANGUAGE plpgsql
    AS $_$
DECLARE
	br bracket_pattern%ROWTYPE;
	pattern text;
	replacement text;
BEGIN
	for br in select * from bracket_pattern bp where bp.lang is null or bp.lang=language order by ord
	loop
		pattern := '[' || br.pattern || ']';
		if (position(pattern in txt) > 0) then
			replacement := null;
			EXECUTE 'select (' || br.replacement || ') from document d where d.id=$1' || case when br.condition is null then '' else ' and (' || br.condition || ')' end into replacement using docId, language;
			if (replacement is not null) then
				txt := replace(txt, pattern, replacement);
			end if;
		end if;
	end loop;
	RETURN txt;
END;
$_$;


ALTER FUNCTION public.interpret_brakets(txt text, docid integer, language character) OWNER TO kbs;

--
-- Name: is_on(boolean, timestamp without time zone, timestamp without time zone, timestamp without time zone); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.is_on(current_value boolean, on_ts timestamp without time zone DEFAULT NULL::timestamp without time zone, off_ts timestamp without time zone DEFAULT NULL::timestamp without time zone, ts timestamp without time zone DEFAULT now()) RETURNS boolean
    LANGUAGE sql
    AS $$
-- Return value
-- 1. TRUE IF on_ts is NOT NULL AND on_ts <= ts AND (off_ts is NULL OR off_ts < on_ts OR off_ts > ts)
-- 2. FALSE IF off_ts is NOT NULL AND off_ts <= ts AND (on_ts is NULL OR on_ts < off_ts OR on_ts > ts)
-- 3. current_value IF (current_value is NULL OR on_ts > ts) AND (off_ts is NULL OR off_ts > ts)
SELECT
	CASE
		WHEN on_ts IS NOT NULL
			AND on_ts <= ts
			AND (
				off_ts IS NULL
				OR off_ts < on_ts
				OR off_ts > ts
			) 
			THEN TRUE
		WHEN off_ts IS NOT NULL
			AND off_ts <= ts
			AND (
				on_ts IS NULL
				OR on_ts < off_ts
				OR on_ts > ts
			) 
			THEN FALSE
		ELSE current_value
	END
$$;


ALTER FUNCTION public.is_on(current_value boolean, on_ts timestamp without time zone, off_ts timestamp without time zone, ts timestamp without time zone) OWNER TO kbs;

--
-- Name: label(integer, text, character); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.label(label_id integer, name text, lang character) RETURNS text
    LANGUAGE plpgsql
    AS $_$
DECLARE
	label text;
BEGIN
	EXECUTE 'select coalesce(' || lang || ',en) from label where id=$1' into label using label_id;
	RETURN case when label is null then name else label end;
END;
$_$;


ALTER FUNCTION public.label(label_id integer, name text, lang character) OWNER TO kbs;

--
-- Name: last_agg(anyelement, anyelement); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.last_agg(anyelement, anyelement) RETURNS anyelement
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
        SELECT $2;
$_$;


ALTER FUNCTION public.last_agg(anyelement, anyelement) OWNER TO kbs;

--
-- Name: mincoalesce(integer, integer); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.mincoalesce(integer, integer) RETURNS integer
    LANGUAGE sql IMMUTABLE
    AS $_$SELECT CASE WHEN $1 is null THEN $2
			WHEN $2 is null THEN $1
			WHEN $1 <= $2 THEN $1
			ELSE $2
	 END;$_$;


ALTER FUNCTION public.mincoalesce(integer, integer) OWNER TO kbs;

--
-- Name: money_transfer_timeout(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.money_transfer_timeout() RETURNS integer
    LANGUAGE plpgsql
    AS $$
	declare
		updated integer;
	begin
		
with 
payments as (
	-- select the timed out money_transfers
	select
		case
			when mt2.id is not null then mt2.id
			else mt.id
		end as id,
		case
			when mt2.document_id is not null then mt2.document_id
			else mt.document_id
		end as document_id,
		case
			when mt.document_id is null then mt2.amount 
			else mt.amount 
		end as amount
	from
		money_transfer mt
	left join money_transfer mt2 on
		mt2.parent_id = mt.id or mt2.id = mt.id	-- join on money_transfer to get the children to update. Join the parent with itself to update it as well.
	join money_account ma on
		mt.to_money_account_id = ma.id
	join gateway_company gc on
		ma.gateway_company_id = gc.id
	where
		mt.pending
		and gc.notification_timeout is not null
		and mt.date + make_interval(0,0,0,0,0,0,gc.notification_timeout) < now()
	order by mt.id
),
updated as (
	-- update the money_transfers
	update
		money_transfer
	set
		pending = false,
		successful = false,
		"comment" = 'Timeout - No notification received from gateway'
	from
		payments
	where
		money_transfer.id = payments.id
)
-- insert record in history of all money_transfer with a document_id
insert into history (document_id, "comment", "date", username, money_transfer_id) select document_id, 'Payment ' || (amount/100) || ' timed out', now(), 'system', id from payments where document_id is not null
;

-- return the number of timed out money_transfers
GET DIAGNOSTICS updated = ROW_COUNT;
return updated;

	END;
$$;


ALTER FUNCTION public.money_transfer_timeout() OWNER TO kbs;

--
-- Name: FUNCTION money_transfer_timeout(); Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON FUNCTION public.money_transfer_timeout() IS 'Updates status of timed out money_transfers';


--
-- Name: multi_replace(text, text[]); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.multi_replace(text, text[]) RETURNS text
    LANGUAGE plpgsql IMMUTABLE
    AS $_$
DECLARE
    res text;
    pair text[];
BEGIN
    res := $1;
    
    FOREACH pair SLICE 1 IN ARRAY $2
    LOOP
        res := replace(res, pair[1], pair[2]);
    END LOOP;
    
    return res;
END; $_$;


ALTER FUNCTION public.multi_replace(text, text[]) OWNER TO kbs;

--
-- Name: numbers(text); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.numbers(text) RETURNS integer[]
    LANGUAGE plpgsql IMMUTABLE
    AS $_$
BEGIN
    return array(select num::int as i from (select regexp_split_to_table(translate($1, '#,', '  '), E'[\\s,-]+') as num) n where num ~ '^(-)?[0-9]+$' and char_length(num) < 10  order by i);
END; $_$;


ALTER FUNCTION public.numbers(text) OWNER TO kbs;

--
-- Name: option_update_scheduled_settings(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.option_update_scheduled_settings() RETURNS integer
    LANGUAGE plpgsql
    AS $$
	DECLARE
    	updated integer;
	BEGIN
		-- Update option records fields of records with scheduled updates and at least one of the updatable field has to be updated
		UPDATE
			"option"
		SET
			online = is_on(
				online, online_on, online_off
			),
			force_soldout = is_on(
				force_soldout, force_soldout_on, force_soldout_off
			),
			first_pass = is_on(
				first_pass, first_pass_on, first_pass_off
			),
			second_pass = is_on(
				second_pass, second_pass_on, second_pass_off
			)
		WHERE
			COALESCE(online_on, online_off, force_soldout_on, force_soldout_off, first_pass_on, first_pass_off, second_pass_on, second_pass_off) IS NOT NULL -- there is at least one scheduled update
			-- at least one field has to be updated 
			AND (
				online <> is_on(
					online, online_on, online_off
				)
				OR force_soldout <> is_on(
					force_soldout, force_soldout_on, force_soldout_off
				)
				OR first_pass <> is_on(
					first_pass, first_pass_on, first_pass_off
				)
				OR second_pass <> is_on(
					second_pass, second_pass_on, second_pass_off
				)
			) ;

		GET DIAGNOSTICS updated = ROW_COUNT;
		RETURN updated;
		
	END;
$$;


ALTER FUNCTION public.option_update_scheduled_settings() OWNER TO kbs;

--
-- Name: overlaps(date, date, date, date); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public."overlaps"(date, date, date, date) RETURNS boolean
    LANGUAGE sql IMMUTABLE
    AS $_$SELECT CASE WHEN $1 is null and $2 is null or $3 is null and $4 is null THEN true
			WHEN $1 is null THEN ($3 is null or $2 >= $3)
			WHEN $2 is null THEN ($4 is null or $4 >= $1)
			WHEN $3 is null THEN ($1 is null or $4 >= $1)
			WHEN $4 is null THEN ($2 is null or $2 >= $3)
			ELSE $1 <= $4 AND $2 >= $3
	 END;$_$;


ALTER FUNCTION public."overlaps"(date, date, date, date) OWNER TO kbs;

--
-- Name: person_merge(integer, integer); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.person_merge(person_dst_id integer, person_src_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
	DECLARE
		res int := 0;
		res2 int := 0;
	BEGIN

		update document set person_id = person_dst_id where person_id = person_src_id;
		get diagnostics res = row_count;
	
		update person set removed=true where id = person_src_id;
	
		get diagnostics res2 = row_count;
	
		if(res2 < 1)
		then
			return -1;
		end if;
	
		return res;
		
	END;
$$;


ALTER FUNCTION public.person_merge(person_dst_id integer, person_src_id integer) OWNER TO kbs;

--
-- Name: rate_matches_date(public.rate, date, public.attendance[], integer); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.rate_matches_date(r public.rate, d date, attendances public.attendance[], index integer) RETURNS boolean
    LANGUAGE plpgsql
    AS $$
DECLARE
	matches bool;
BEGIN
    if (r.arriving_or_leaving and index > 1 and index < array_length(attendances) and greatest(d - attendances[index-1].date, attendances[index+1].date - d) <= 1) then
	   matches := false;
	else
		matches := overlaps(d, d, r.start_date, r.end_date);
	end if;
	RETURN matches;
END;
$$;


ALTER FUNCTION public.rate_matches_date(r public.rate, d date, attendances public.attendance[], index integer) OWNER TO kbs;

--
-- Name: rate_matches_document(public.rate, public.document); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.rate_matches_document(r public.rate, d public.document) RETURNS boolean
    LANGUAGE plpgsql
    AS $$
BEGIN
	RETURN r.max_age is null or d.person_age <= r.max_age;
END;
$$;


ALTER FUNCTION public.rate_matches_document(r public.rate, d public.document) OWNER TO kbs;

--
-- Name: record_changes_for_notification(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.record_changes_for_notification() RETURNS trigger
    LANGUAGE plpgsql
    AS $_$
DECLARE
	ri RECORD;
	ov TEXT;
	nv TEXT;
BEGIN
	if (TG_OP = 'UPDATE') then
		FOR ri IN SELECT column_name FROM information_schema.columns
						WHERE table_schema = quote_ident(TG_TABLE_SCHEMA) AND table_name = quote_ident(TG_TABLE_NAME) AND column_name NOT LIKE 'trigger_%'
						ORDER BY ordinal_position
		LOOP
			EXECUTE 'SELECT case when ($1).' || ri.column_name || ' is null then ''null'' else ($1).' || ri.column_name || '::text end' INTO ov USING OLD;
			EXECUTE 'SELECT case when ($1).' || ri.column_name || ' is null then ''null'' else ($1).' || ri.column_name || '::text end' INTO nv USING NEW;
			if (nv <> ov) then
				RAISE NOTICE '%@%.% = %', TG_TABLE_NAME, NEW.id, ri.column_name, nv;
				INSERT into sys_log (table_name, update, oid, column_name) values (TG_TABLE_NAME, true, NEW.id, ri.column_name); 
			end if;
		END LOOP;
	end if;
	RETURN NEW;
END;
$_$;


ALTER FUNCTION public.record_changes_for_notification() OWNER TO kbs;

--
-- Name: record_changes_for_notification_allocation_rule(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.record_changes_for_notification_allocation_rule() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN

IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.active IS DISTINCT FROM NEW.active) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('allocation_rule', true, NEW.id, 'active'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.resource_id IS DISTINCT FROM NEW.resource_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('allocation_rule', true, NEW.id, 'resource_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.event_id IS DISTINCT FROM NEW.event_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('allocation_rule', true, NEW.id, 'event_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.item_family_id IS DISTINCT FROM NEW.item_family_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('allocation_rule', true, NEW.id, 'item_family_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.if_language_id IS DISTINCT FROM NEW.if_language_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('allocation_rule', true, NEW.id, 'if_language_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.if_country_id IS DISTINCT FROM NEW.if_country_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('allocation_rule', true, NEW.id, 'if_country_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.if_organization_id IS DISTINCT FROM NEW.if_organization_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('allocation_rule', true, NEW.id, 'if_organization_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.if_site_id IS DISTINCT FROM NEW.if_site_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('allocation_rule', true, NEW.id, 'if_site_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.if_item_id IS DISTINCT FROM NEW.if_item_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('allocation_rule', true, NEW.id, 'if_item_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.if_child IS DISTINCT FROM NEW.if_child) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('allocation_rule', true, NEW.id, 'if_child'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.ord IS DISTINCT FROM NEW.ord) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('allocation_rule', true, NEW.id, 'ord'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.trigger_allocate IS DISTINCT FROM NEW.trigger_allocate) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('allocation_rule', true, NEW.id, 'trigger_allocate'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.if_carer IS DISTINCT FROM NEW.if_carer) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('allocation_rule', true, NEW.id, 'if_carer'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.resource_configuration_id IS DISTINCT FROM NEW.resource_configuration_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('allocation_rule', true, NEW.id, 'resource_configuration_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.if_whole_event IS DISTINCT FROM NEW.if_whole_event) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('allocation_rule', true, NEW.id, 'if_whole_event'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.if_partial_event IS DISTINCT FROM NEW.if_partial_event) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('allocation_rule', true, NEW.id, 'if_partial_event'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.if_lay IS DISTINCT FROM NEW.if_lay) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('allocation_rule', true, NEW.id, 'if_lay'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.if_ordained IS DISTINCT FROM NEW.if_ordained) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('allocation_rule', true, NEW.id, 'if_ordained'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.if_ref_min IS DISTINCT FROM NEW.if_ref_min) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('allocation_rule', true, NEW.id, 'if_ref_min'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.if_ref_max IS DISTINCT FROM NEW.if_ref_max) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('allocation_rule', true, NEW.id, 'if_ref_max'); END IF;
RETURN NEW;
END;
$$;


ALTER FUNCTION public.record_changes_for_notification_allocation_rule() OWNER TO kbs;

--
-- Name: record_changes_for_notification_document(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.record_changes_for_notification_document() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN

IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.creation_date IS DISTINCT FROM NEW.creation_date) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'creation_date'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.accounting_date IS DISTINCT FROM NEW.accounting_date) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'accounting_date'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.organization_id IS DISTINCT FROM NEW.organization_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'organization_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.expenditure IS DISTINCT FROM NEW.expenditure) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'expenditure'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.ref IS DISTINCT FROM NEW.ref) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'ref'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.activity_id IS DISTINCT FROM NEW.activity_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'activity_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.event_id IS DISTINCT FROM NEW.event_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'event_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.multiple_booking_id IS DISTINCT FROM NEW.multiple_booking_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'multiple_booking_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.cart_id IS DISTINCT FROM NEW.cart_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'cart_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.third_party_id IS DISTINCT FROM NEW.third_party_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'third_party_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_id IS DISTINCT FROM NEW.person_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_organization_id IS DISTINCT FROM NEW.person_organization_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_organization_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_branch_id IS DISTINCT FROM NEW.person_branch_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_branch_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_third_party_id IS DISTINCT FROM NEW.person_third_party_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_third_party_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_language_id IS DISTINCT FROM NEW.person_language_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_language_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_country_name IS DISTINCT FROM NEW.person_country_name) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_country_name'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_country_geonameid IS DISTINCT FROM NEW.person_country_geonameid) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_country_geonameid'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_country_id IS DISTINCT FROM NEW.person_country_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_country_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_post_code IS DISTINCT FROM NEW.person_post_code) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_post_code'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_city_name IS DISTINCT FROM NEW.person_city_name) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_city_name'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_city_geonameid IS DISTINCT FROM NEW.person_city_geonameid) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_city_geonameid'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_city_latitude IS DISTINCT FROM NEW.person_city_latitude) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_city_latitude'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_city_longitude IS DISTINCT FROM NEW.person_city_longitude) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_city_longitude'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_city_timezone IS DISTINCT FROM NEW.person_city_timezone) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_city_timezone'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_street IS DISTINCT FROM NEW.person_street) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_street'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_latitude IS DISTINCT FROM NEW.person_latitude) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_latitude'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_longitude IS DISTINCT FROM NEW.person_longitude) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_longitude'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_name IS DISTINCT FROM NEW.person_name) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_name'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_first_name IS DISTINCT FROM NEW.person_first_name) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_first_name'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_last_name IS DISTINCT FROM NEW.person_last_name) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_last_name'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_lay_name IS DISTINCT FROM NEW.person_lay_name) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_lay_name'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_abc_names IS DISTINCT FROM NEW.person_abc_names) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_abc_names'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_male IS DISTINCT FROM NEW.person_male) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_male'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_ordained IS DISTINCT FROM NEW.person_ordained) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_ordained'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_carer1_name IS DISTINCT FROM NEW.person_carer1_name) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_carer1_name'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_carer1_id IS DISTINCT FROM NEW.person_carer1_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_carer1_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_carer1_document_id IS DISTINCT FROM NEW.person_carer1_document_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_carer1_document_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_carer2_name IS DISTINCT FROM NEW.person_carer2_name) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_carer2_name'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_carer2_id IS DISTINCT FROM NEW.person_carer2_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_carer2_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_carer2_document_id IS DISTINCT FROM NEW.person_carer2_document_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_carer2_document_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_email IS DISTINCT FROM NEW.person_email) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_email'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_phone IS DISTINCT FROM NEW.person_phone) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_phone'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.inet IS DISTINCT FROM NEW.inet) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'inet'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.inet_ip IS DISTINCT FROM NEW.inet_ip) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'inet_ip'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.inet_geo_latitude IS DISTINCT FROM NEW.inet_geo_latitude) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'inet_geo_latitude'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.inet_geo_longitude IS DISTINCT FROM NEW.inet_geo_longitude) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'inet_geo_longitude'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.cancelled IS DISTINCT FROM NEW.cancelled) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'cancelled'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.abandoned IS DISTINCT FROM NEW.abandoned) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'abandoned'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.confirmed IS DISTINCT FROM NEW.confirmed) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'confirmed'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.changed IS DISTINCT FROM NEW.changed) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'changed'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.arrived IS DISTINCT FROM NEW.arrived) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'arrived'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.absent IS DISTINCT FROM NEW.absent) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'absent'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.read IS DISTINCT FROM NEW.read) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'read'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.dates IS DISTINCT FROM NEW.dates) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'dates'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.request IS DISTINCT FROM NEW.request) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'request'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.comment IS DISTINCT FROM NEW.comment) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'comment'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.special_needs IS DISTINCT FROM NEW.special_needs) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'special_needs'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.price_net IS DISTINCT FROM NEW.price_net) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'price_net'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.price_min_deposit IS DISTINCT FROM NEW.price_min_deposit) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'price_min_deposit'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.price_non_refundable IS DISTINCT FROM NEW.price_non_refundable) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'price_non_refundable'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.price_deposit IS DISTINCT FROM NEW.price_deposit) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'price_deposit'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_age IS DISTINCT FROM NEW.person_age) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_age'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_admin1_name IS DISTINCT FROM NEW.person_admin1_name) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_admin1_name'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_admin1_code IS DISTINCT FROM NEW.person_admin1_code) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_admin1_code'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_admin1_geonameid IS DISTINCT FROM NEW.person_admin1_geonameid) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_admin1_geonameid'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_admin2_name IS DISTINCT FROM NEW.person_admin2_name) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_admin2_name'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_admin2_code IS DISTINCT FROM NEW.person_admin2_code) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_admin2_code'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_admin2_geonameid IS DISTINCT FROM NEW.person_admin2_geonameid) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_admin2_geonameid'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_country_code IS DISTINCT FROM NEW.person_country_code) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_country_code'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_passport IS DISTINCT FROM NEW.person_passport) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_passport'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_lang IS DISTINCT FROM NEW.person_lang) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_lang'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_nationality IS DISTINCT FROM NEW.person_nationality) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_nationality'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_resident IS DISTINCT FROM NEW.person_resident) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_resident'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.paper IS DISTINCT FROM NEW.paper) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'paper'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_resident2 IS DISTINCT FROM NEW.person_resident2) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_resident2'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_verified IS DISTINCT FROM NEW.person_verified) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_verified'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.flagged IS DISTINCT FROM NEW.flagged) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'flagged'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.will_pay IS DISTINCT FROM NEW.will_pay) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'will_pay'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_photo_received IS DISTINCT FROM NEW.person_photo_received) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_photo_received'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_known IS DISTINCT FROM NEW.person_known) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_known'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_unknown IS DISTINCT FROM NEW.person_unknown) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_unknown'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.pass_ready IS DISTINCT FROM NEW.pass_ready) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'pass_ready'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.cancellation_date IS DISTINCT FROM NEW.cancellation_date) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'cancellation_date'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.not_multiple_booking_id IS DISTINCT FROM NEW.not_multiple_booking_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'not_multiple_booking_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_organization_name IS DISTINCT FROM NEW.person_organization_name) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_organization_name'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_unemployed IS DISTINCT FROM NEW.person_unemployed) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_unemployed'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_facility_fee IS DISTINCT FROM NEW.person_facility_fee) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_facility_fee'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_discovery IS DISTINCT FROM NEW.person_discovery) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_discovery'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_discovery_reduced IS DISTINCT FROM NEW.person_discovery_reduced) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_discovery_reduced'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_working_visit IS DISTINCT FROM NEW.person_working_visit) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_working_visit'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.person_guest IS DISTINCT FROM NEW.person_guest) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document', true, NEW.id, 'person_guest'); END IF;
RETURN NEW;
END;
$$;


ALTER FUNCTION public.record_changes_for_notification_document() OWNER TO kbs;

--
-- Name: record_changes_for_notification_document_line(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.record_changes_for_notification_document_line() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN

IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.document_id IS DISTINCT FROM NEW.document_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'document_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.item_id IS DISTINCT FROM NEW.item_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'item_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.stock_transfer_id IS DISTINCT FROM NEW.stock_transfer_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'stock_transfer_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.site_id IS DISTINCT FROM NEW.site_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'site_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.budget_line_id IS DISTINCT FROM NEW.budget_line_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'budget_line_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.quantity IS DISTINCT FROM NEW.quantity) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'quantity'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.purchase_rate_id IS DISTINCT FROM NEW.purchase_rate_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'purchase_rate_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.purchase_price_novat IS DISTINCT FROM NEW.purchase_price_novat) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'purchase_price_novat'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.purchase_vat_id IS DISTINCT FROM NEW.purchase_vat_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'purchase_vat_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.purchase_price IS DISTINCT FROM NEW.purchase_price) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'purchase_price'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.rate_id IS DISTINCT FROM NEW.rate_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'rate_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.price_novat IS DISTINCT FROM NEW.price_novat) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'price_novat'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.vat_id IS DISTINCT FROM NEW.vat_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'vat_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.price IS DISTINCT FROM NEW.price) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'price'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.price_discount IS DISTINCT FROM NEW.price_discount) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'price_discount'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.price_is_custom IS DISTINCT FROM NEW.price_is_custom) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'price_is_custom'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.price_net IS DISTINCT FROM NEW.price_net) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'price_net'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.price_min_deposit IS DISTINCT FROM NEW.price_min_deposit) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'price_min_deposit'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.price_non_refundable IS DISTINCT FROM NEW.price_non_refundable) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'price_non_refundable'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.resource_id IS DISTINCT FROM NEW.resource_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'resource_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.share_owner IS DISTINCT FROM NEW.share_owner) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'share_owner'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.share_owner_quantity IS DISTINCT FROM NEW.share_owner_quantity) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'share_owner_quantity'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.share_mate IS DISTINCT FROM NEW.share_mate) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'share_mate'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.share_mate_owner_name IS DISTINCT FROM NEW.share_mate_owner_name) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'share_mate_owner_name'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.share_mate_owner_person_id IS DISTINCT FROM NEW.share_mate_owner_person_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'share_mate_owner_person_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.share_mate_owner_document_line_id IS DISTINCT FROM NEW.share_mate_owner_document_line_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'share_mate_owner_document_line_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.share_mate_charged IS DISTINCT FROM NEW.share_mate_charged) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'share_mate_charged'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.cancelled IS DISTINCT FROM NEW.cancelled) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'cancelled'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.dates IS DISTINCT FROM NEW.dates) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'dates'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.allocate IS DISTINCT FROM NEW.allocate) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'allocate'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.share_owner_mate1_name IS DISTINCT FROM NEW.share_owner_mate1_name) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'share_owner_mate1_name'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.share_owner_mate2_name IS DISTINCT FROM NEW.share_owner_mate2_name) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'share_owner_mate2_name'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.share_owner_mate3_name IS DISTINCT FROM NEW.share_owner_mate3_name) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'share_owner_mate3_name'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.share_owner_mate4_name IS DISTINCT FROM NEW.share_owner_mate4_name) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'share_owner_mate4_name'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.share_owner_mate5_name IS DISTINCT FROM NEW.share_owner_mate5_name) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'share_owner_mate5_name'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.abandoned IS DISTINCT FROM NEW.abandoned) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'abandoned'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.share_owner_mate6_name IS DISTINCT FROM NEW.share_owner_mate6_name) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'share_owner_mate6_name'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.share_owner_mate7_name IS DISTINCT FROM NEW.share_owner_mate7_name) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'share_owner_mate7_name'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.backend_released IS DISTINCT FROM NEW.backend_released) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'backend_released'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.frontend_released IS DISTINCT FROM NEW.frontend_released) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'frontend_released'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.private IS DISTINCT FROM NEW.private) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'private'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.lock_allocation IS DISTINCT FROM NEW.lock_allocation) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'lock_allocation'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.read IS DISTINCT FROM NEW.read) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'read'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.cancellation_date IS DISTINCT FROM NEW.cancellation_date) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'cancellation_date'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.creation_date IS DISTINCT FROM NEW.creation_date) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'creation_date'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.price_deposit IS DISTINCT FROM NEW.price_deposit) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'price_deposit'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.resource_configuration_id IS DISTINCT FROM NEW.resource_configuration_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'resource_configuration_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.comment IS DISTINCT FROM NEW.comment) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'comment'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.system_allocated IS DISTINCT FROM NEW.system_allocated) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'system_allocated'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.price_custom IS DISTINCT FROM NEW.price_custom) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'price_custom'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.arrival_site_id IS DISTINCT FROM NEW.arrival_site_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('document_line', true, NEW.id, 'arrival_site_id'); END IF;
RETURN NEW;
END;
$$;


ALTER FUNCTION public.record_changes_for_notification_document_line() OWNER TO kbs;

--
-- Name: record_changes_for_notification_label(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.record_changes_for_notification_label() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN

IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.ref IS DISTINCT FROM NEW.ref) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('label', true, NEW.id, 'ref'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.de IS DISTINCT FROM NEW.de) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('label', true, NEW.id, 'de'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.en IS DISTINCT FROM NEW.en) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('label', true, NEW.id, 'en'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.es IS DISTINCT FROM NEW.es) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('label', true, NEW.id, 'es'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.fr IS DISTINCT FROM NEW.fr) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('label', true, NEW.id, 'fr'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.pt IS DISTINCT FROM NEW.pt) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('label', true, NEW.id, 'pt'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.organization_id IS DISTINCT FROM NEW.organization_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('label', true, NEW.id, 'organization_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.alert IS DISTINCT FROM NEW.alert) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('label', true, NEW.id, 'alert'); END IF;
RETURN NEW;
END;
$$;


ALTER FUNCTION public.record_changes_for_notification_label() OWNER TO kbs;

--
-- Name: record_changes_for_notification_letter(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.record_changes_for_notification_letter() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN

IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.type_id IS DISTINCT FROM NEW.type_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('letter', true, NEW.id, 'type_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.event_id IS DISTINCT FROM NEW.event_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('letter', true, NEW.id, 'event_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.organization_id IS DISTINCT FROM NEW.organization_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('letter', true, NEW.id, 'organization_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.subject IS DISTINCT FROM NEW.subject) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('letter', true, NEW.id, 'subject'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.content IS DISTINCT FROM NEW.content) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('letter', true, NEW.id, 'content'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.en IS DISTINCT FROM NEW.en) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('letter', true, NEW.id, 'en'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.de IS DISTINCT FROM NEW.de) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('letter', true, NEW.id, 'de'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.fr IS DISTINCT FROM NEW.fr) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('letter', true, NEW.id, 'fr'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.es IS DISTINCT FROM NEW.es) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('letter', true, NEW.id, 'es'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.pt IS DISTINCT FROM NEW.pt) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('letter', true, NEW.id, 'pt'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.subject_en IS DISTINCT FROM NEW.subject_en) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('letter', true, NEW.id, 'subject_en'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.subject_de IS DISTINCT FROM NEW.subject_de) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('letter', true, NEW.id, 'subject_de'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.subject_fr IS DISTINCT FROM NEW.subject_fr) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('letter', true, NEW.id, 'subject_fr'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.subject_es IS DISTINCT FROM NEW.subject_es) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('letter', true, NEW.id, 'subject_es'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.subject_pt IS DISTINCT FROM NEW.subject_pt) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('letter', true, NEW.id, 'subject_pt'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.account_id IS DISTINCT FROM NEW.account_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('letter', true, NEW.id, 'account_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.active IS DISTINCT FROM NEW.active) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('letter', true, NEW.id, 'active'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.name IS DISTINCT FROM NEW.name) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('letter', true, NEW.id, 'name'); END IF;
RETURN NEW;
END;
$$;


ALTER FUNCTION public.record_changes_for_notification_letter() OWNER TO kbs;

--
-- Name: record_changes_for_notification_mail(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.record_changes_for_notification_mail() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN

IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.account_id IS DISTINCT FROM NEW.account_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('mail', true, NEW.id, 'account_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.letter_id IS DISTINCT FROM NEW.letter_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('mail', true, NEW.id, 'letter_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.document_id IS DISTINCT FROM NEW.document_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('mail', true, NEW.id, 'document_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD."out" IS DISTINCT FROM NEW."out") THEN INSERT into sys_log (table_name, update, oid, column_name) values ('mail', true, NEW.id, 'out'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.background IS DISTINCT FROM NEW.background) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('mail', true, NEW.id, 'background'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.transmitted IS DISTINCT FROM NEW.transmitted) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('mail', true, NEW.id, 'transmitted'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.date IS DISTINCT FROM NEW.date) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('mail', true, NEW.id, 'date'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.subject IS DISTINCT FROM NEW.subject) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('mail', true, NEW.id, 'subject'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.content IS DISTINCT FROM NEW.content) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('mail', true, NEW.id, 'content'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.error IS DISTINCT FROM NEW.error) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('mail', true, NEW.id, 'error'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.from_name IS DISTINCT FROM NEW.from_name) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('mail', true, NEW.id, 'from_name'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.from_email IS DISTINCT FROM NEW.from_email) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('mail', true, NEW.id, 'from_email'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.read IS DISTINCT FROM NEW.read) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('mail', true, NEW.id, 'read'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.transmission_date IS DISTINCT FROM NEW.transmission_date) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('mail', true, NEW.id, 'transmission_date'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.auto_delete IS DISTINCT FROM NEW.auto_delete) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('mail', true, NEW.id, 'auto_delete'); END IF;
RETURN NEW;
END;
$$;


ALTER FUNCTION public.record_changes_for_notification_mail() OWNER TO kbs;

--
-- Name: record_changes_for_notification_money_account(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.record_changes_for_notification_money_account() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN

IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.type_id IS DISTINCT FROM NEW.type_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('money_account', true, NEW.id, 'type_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.currency_id IS DISTINCT FROM NEW.currency_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('money_account', true, NEW.id, 'currency_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.organization_id IS DISTINCT FROM NEW.organization_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('money_account', true, NEW.id, 'organization_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.third_party_id IS DISTINCT FROM NEW.third_party_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('money_account', true, NEW.id, 'third_party_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.bank_system_account_id IS DISTINCT FROM NEW.bank_system_account_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('money_account', true, NEW.id, 'bank_system_account_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.name IS DISTINCT FROM NEW.name) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('money_account', true, NEW.id, 'name'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.gateway_company_id IS DISTINCT FROM NEW.gateway_company_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('money_account', true, NEW.id, 'gateway_company_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.event_id IS DISTINCT FROM NEW.event_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('money_account', true, NEW.id, 'event_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.closed IS DISTINCT FROM NEW.closed) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('money_account', true, NEW.id, 'closed'); END IF;
RETURN NEW;
END;
$$;


ALTER FUNCTION public.record_changes_for_notification_money_account() OWNER TO kbs;

--
-- Name: record_changes_for_notification_money_transfer(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.record_changes_for_notification_money_transfer() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN

IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.parent_id IS DISTINCT FROM NEW.parent_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('money_transfer', true, NEW.id, 'parent_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.document_id IS DISTINCT FROM NEW.document_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('money_transfer', true, NEW.id, 'document_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.date IS DISTINCT FROM NEW.date) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('money_transfer', true, NEW.id, 'date'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.method_id IS DISTINCT FROM NEW.method_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('money_transfer', true, NEW.id, 'method_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.from_money_account_id IS DISTINCT FROM NEW.from_money_account_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('money_transfer', true, NEW.id, 'from_money_account_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.to_money_account_id IS DISTINCT FROM NEW.to_money_account_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('money_transfer', true, NEW.id, 'to_money_account_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.payment IS DISTINCT FROM NEW.payment) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('money_transfer', true, NEW.id, 'payment'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.refund IS DISTINCT FROM NEW.refund) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('money_transfer', true, NEW.id, 'refund'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.amount IS DISTINCT FROM NEW.amount) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('money_transfer', true, NEW.id, 'amount'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.pending IS DISTINCT FROM NEW.pending) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('money_transfer', true, NEW.id, 'pending'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.successful IS DISTINCT FROM NEW.successful) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('money_transfer', true, NEW.id, 'successful'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.from_money_account_balance IS DISTINCT FROM NEW.from_money_account_balance) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('money_transfer', true, NEW.id, 'from_money_account_balance'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.to_money_account_balance IS DISTINCT FROM NEW.to_money_account_balance) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('money_transfer', true, NEW.id, 'to_money_account_balance'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.statement_id IS DISTINCT FROM NEW.statement_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('money_transfer', true, NEW.id, 'statement_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.spread IS DISTINCT FROM NEW.spread) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('money_transfer', true, NEW.id, 'spread'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.transaction_ref IS DISTINCT FROM NEW.transaction_ref) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('money_transfer', true, NEW.id, 'transaction_ref'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.gateway_company_id IS DISTINCT FROM NEW.gateway_company_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('money_transfer', true, NEW.id, 'gateway_company_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.receipts_transfer IS DISTINCT FROM NEW.receipts_transfer) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('money_transfer', true, NEW.id, 'receipts_transfer'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.transfer_id IS DISTINCT FROM NEW.transfer_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('money_transfer', true, NEW.id, 'transfer_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.expected_amount IS DISTINCT FROM NEW.expected_amount) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('money_transfer', true, NEW.id, 'expected_amount'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.method_not_circled IS DISTINCT FROM NEW.method_not_circled) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('money_transfer', true, NEW.id, 'method_not_circled'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.comment IS DISTINCT FROM NEW.comment) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('money_transfer', true, NEW.id, 'comment'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.read IS DISTINCT FROM NEW.read) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('money_transfer', true, NEW.id, 'read'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.status IS DISTINCT FROM NEW.status) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('money_transfer', true, NEW.id, 'status'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.verified IS DISTINCT FROM NEW.verified) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('money_transfer', true, NEW.id, 'verified'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.verifier IS DISTINCT FROM NEW.verifier) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('money_transfer', true, NEW.id, 'verifier'); END IF;
RETURN NEW;
END;
$$;


ALTER FUNCTION public.record_changes_for_notification_money_transfer() OWNER TO kbs;

--
-- Name: record_changes_for_notification_multiple_booking(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.record_changes_for_notification_multiple_booking() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN

IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.event_id IS DISTINCT FROM NEW.event_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('multiple_booking', true, NEW.id, 'event_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD."all" IS DISTINCT FROM NEW."all") THEN INSERT into sys_log (table_name, update, oid, column_name) values ('multiple_booking', true, NEW.id, 'all'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.not_cancelled IS DISTINCT FROM NEW.not_cancelled) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('multiple_booking', true, NEW.id, 'not_cancelled'); END IF;
RETURN NEW;
END;
$$;


ALTER FUNCTION public.record_changes_for_notification_multiple_booking() OWNER TO kbs;

--
-- Name: record_changes_for_notification_option(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.record_changes_for_notification_option() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN

IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.parent_id IS DISTINCT FROM NEW.parent_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'parent_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.folder IS DISTINCT FROM NEW.folder) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'folder'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.event_id IS DISTINCT FROM NEW.event_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'event_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.site_id IS DISTINCT FROM NEW.site_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'site_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.item_id IS DISTINCT FROM NEW.item_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'item_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.rate_id IS DISTINCT FROM NEW.rate_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'rate_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.label_id IS DISTINCT FROM NEW.label_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'label_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.name IS DISTINCT FROM NEW.name) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'name'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.first_pass IS DISTINCT FROM NEW.first_pass) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'first_pass'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.second_pass IS DISTINCT FROM NEW.second_pass) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'second_pass'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.ord IS DISTINCT FROM NEW.ord) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'ord'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.obligatory IS DISTINCT FROM NEW.obligatory) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'obligatory'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.children_radio IS DISTINCT FROM NEW.children_radio) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'children_radio'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.frame IS DISTINCT FROM NEW.frame) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'frame'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.item_family_id IS DISTINCT FROM NEW.item_family_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'item_family_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.split_whole_partial IS DISTINCT FROM NEW.split_whole_partial) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'split_whole_partial'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.layout IS DISTINCT FROM NEW.layout) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'layout'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.top_label_id IS DISTINCT FROM NEW.top_label_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'top_label_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.children_dynamic IS DISTINCT FROM NEW.children_dynamic) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'children_dynamic'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.allocate IS DISTINCT FROM NEW.allocate) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'allocate'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.attendance_option_id IS DISTINCT FROM NEW.attendance_option_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'attendance_option_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.sharing_item_id IS DISTINCT FROM NEW.sharing_item_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'sharing_item_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.prompt_label_id IS DISTINCT FROM NEW.prompt_label_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'prompt_label_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.children_prompt_label_id IS DISTINCT FROM NEW.children_prompt_label_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'children_prompt_label_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.first_day IS DISTINCT FROM NEW.first_day) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'first_day'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.last_day IS DISTINCT FROM NEW.last_day) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'last_day'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.first_excluded_day IS DISTINCT FROM NEW.first_excluded_day) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'first_excluded_day'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.last_excluded_day IS DISTINCT FROM NEW.last_excluded_day) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'last_excluded_day'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.hide_days IS DISTINCT FROM NEW.hide_days) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'hide_days'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.male_allowed IS DISTINCT FROM NEW.male_allowed) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'male_allowed'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.female_allowed IS DISTINCT FROM NEW.female_allowed) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'female_allowed'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.lay_allowed IS DISTINCT FROM NEW.lay_allowed) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'lay_allowed'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.ordained_allowed IS DISTINCT FROM NEW.ordained_allowed) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'ordained_allowed'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.adult_allowed IS DISTINCT FROM NEW.adult_allowed) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'adult_allowed'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.child_allowed IS DISTINCT FROM NEW.child_allowed) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'child_allowed'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.min_age IS DISTINCT FROM NEW.min_age) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'min_age'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.max_age IS DISTINCT FROM NEW.max_age) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'max_age'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.bottom_label_id IS DISTINCT FROM NEW.bottom_label_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'bottom_label_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.online IS DISTINCT FROM NEW.online) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'online'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.dev IS DISTINCT FROM NEW.dev) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'dev'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.hide_per_person IS DISTINCT FROM NEW.hide_per_person) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'hide_per_person'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.per_day_availability IS DISTINCT FROM NEW.per_day_availability) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'per_day_availability'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.attendance_document IS DISTINCT FROM NEW.attendance_document) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'attendance_document'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.force_soldout IS DISTINCT FROM NEW.force_soldout) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'force_soldout'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.obligatory_agreement IS DISTINCT FROM NEW.obligatory_agreement) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'obligatory_agreement'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.radio_traversal IS DISTINCT FROM NEW.radio_traversal) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'radio_traversal'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.min_day IS DISTINCT FROM NEW.min_day) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'min_day'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.max_day IS DISTINCT FROM NEW.max_day) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'max_day'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.partial_enabled IS DISTINCT FROM NEW.partial_enabled) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'partial_enabled'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.hide IS DISTINCT FROM NEW.hide) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'hide'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.price_custom IS DISTINCT FROM NEW.price_custom) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'price_custom'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.time_range IS DISTINCT FROM NEW.time_range) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'time_range'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.date_time_range IS DISTINCT FROM NEW.date_time_range) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'date_time_range'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.quantity_max IS DISTINCT FROM NEW.quantity_max) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'quantity_max'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.quantity_label_id IS DISTINCT FROM NEW.quantity_label_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'quantity_label_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.floating IS DISTINCT FROM NEW.floating) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'floating'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.popup_label_id IS DISTINCT FROM NEW.popup_label_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'popup_label_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.comment_label_id IS DISTINCT FROM NEW.comment_label_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'comment_label_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.template IS DISTINCT FROM NEW.template) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'template'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.footer_label_id IS DISTINCT FROM NEW.footer_label_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'footer_label_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.arrival_site_id IS DISTINCT FROM NEW.arrival_site_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('option', true, NEW.id, 'arrival_site_id'); END IF;
RETURN NEW;
END;
$$;


ALTER FUNCTION public.record_changes_for_notification_option() OWNER TO kbs;

--
-- Name: record_changes_for_notification_organization(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.record_changes_for_notification_organization() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN

IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.type_id IS DISTINCT FROM NEW.type_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('organization', true, NEW.id, 'type_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.country_id IS DISTINCT FROM NEW.country_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('organization', true, NEW.id, 'country_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.name IS DISTINCT FROM NEW.name) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('organization', true, NEW.id, 'name'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.closed IS DISTINCT FROM NEW.closed) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('organization', true, NEW.id, 'closed'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.domain_name IS DISTINCT FROM NEW.domain_name) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('organization', true, NEW.id, 'domain_name'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.ga_tracking_id IS DISTINCT FROM NEW.ga_tracking_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('organization', true, NEW.id, 'ga_tracking_id'); END IF;
RETURN NEW;
END;
$$;


ALTER FUNCTION public.record_changes_for_notification_organization() OWNER TO kbs;

--
-- Name: record_changes_for_notification_person(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.record_changes_for_notification_person() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN

IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.organization_id IS DISTINCT FROM NEW.organization_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('person', true, NEW.id, 'organization_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.branch_id IS DISTINCT FROM NEW.branch_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('person', true, NEW.id, 'branch_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.third_party_id IS DISTINCT FROM NEW.third_party_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('person', true, NEW.id, 'third_party_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.language_id IS DISTINCT FROM NEW.language_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('person', true, NEW.id, 'language_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.frontend_account_id IS DISTINCT FROM NEW.frontend_account_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('person', true, NEW.id, 'frontend_account_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.country_name IS DISTINCT FROM NEW.country_name) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('person', true, NEW.id, 'country_name'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.country_geonameid IS DISTINCT FROM NEW.country_geonameid) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('person', true, NEW.id, 'country_geonameid'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.country_id IS DISTINCT FROM NEW.country_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('person', true, NEW.id, 'country_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.post_code IS DISTINCT FROM NEW.post_code) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('person', true, NEW.id, 'post_code'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.city_name IS DISTINCT FROM NEW.city_name) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('person', true, NEW.id, 'city_name'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.city_geonameid IS DISTINCT FROM NEW.city_geonameid) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('person', true, NEW.id, 'city_geonameid'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.city_latitude IS DISTINCT FROM NEW.city_latitude) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('person', true, NEW.id, 'city_latitude'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.city_longitude IS DISTINCT FROM NEW.city_longitude) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('person', true, NEW.id, 'city_longitude'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.city_timezone IS DISTINCT FROM NEW.city_timezone) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('person', true, NEW.id, 'city_timezone'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.street IS DISTINCT FROM NEW.street) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('person', true, NEW.id, 'street'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.latitude IS DISTINCT FROM NEW.latitude) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('person', true, NEW.id, 'latitude'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.longitude IS DISTINCT FROM NEW.longitude) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('person', true, NEW.id, 'longitude'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.name IS DISTINCT FROM NEW.name) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('person', true, NEW.id, 'name'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.first_name IS DISTINCT FROM NEW.first_name) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('person', true, NEW.id, 'first_name'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.last_name IS DISTINCT FROM NEW.last_name) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('person', true, NEW.id, 'last_name'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.lay_name IS DISTINCT FROM NEW.lay_name) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('person', true, NEW.id, 'lay_name'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.abc_names IS DISTINCT FROM NEW.abc_names) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('person', true, NEW.id, 'abc_names'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.male IS DISTINCT FROM NEW.male) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('person', true, NEW.id, 'male'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.ordained IS DISTINCT FROM NEW.ordained) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('person', true, NEW.id, 'ordained'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.birthdate IS DISTINCT FROM NEW.birthdate) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('person', true, NEW.id, 'birthdate'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.carer1_name IS DISTINCT FROM NEW.carer1_name) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('person', true, NEW.id, 'carer1_name'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.carer1_id IS DISTINCT FROM NEW.carer1_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('person', true, NEW.id, 'carer1_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.carer2_name IS DISTINCT FROM NEW.carer2_name) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('person', true, NEW.id, 'carer2_name'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.carer2_id IS DISTINCT FROM NEW.carer2_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('person', true, NEW.id, 'carer2_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.email IS DISTINCT FROM NEW.email) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('person', true, NEW.id, 'email'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.phone IS DISTINCT FROM NEW.phone) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('person', true, NEW.id, 'phone'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.admin1_name IS DISTINCT FROM NEW.admin1_name) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('person', true, NEW.id, 'admin1_name'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.admin2_name IS DISTINCT FROM NEW.admin2_name) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('person', true, NEW.id, 'admin2_name'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.country_code IS DISTINCT FROM NEW.country_code) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('person', true, NEW.id, 'country_code'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.removed IS DISTINCT FROM NEW.removed) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('person', true, NEW.id, 'removed'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.never_booked IS DISTINCT FROM NEW.never_booked) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('person', true, NEW.id, 'never_booked'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.organization_name IS DISTINCT FROM NEW.organization_name) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('person', true, NEW.id, 'organization_name'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.nationality IS DISTINCT FROM NEW.nationality) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('person', true, NEW.id, 'nationality'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.passport IS DISTINCT FROM NEW.passport) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('person', true, NEW.id, 'passport'); END IF;
RETURN NEW;
END;
$$;


ALTER FUNCTION public.record_changes_for_notification_person() OWNER TO kbs;

--
-- Name: record_changes_for_notification_rate(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.record_changes_for_notification_rate() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN

IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.item_id IS DISTINCT FROM NEW.item_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'item_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.sale IS DISTINCT FROM NEW.sale) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'sale'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.package_id IS DISTINCT FROM NEW.package_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'package_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.site_id IS DISTINCT FROM NEW.site_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'site_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.live IS DISTINCT FROM NEW.live) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'live'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.comment IS DISTINCT FROM NEW.comment) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'comment'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.start_date IS DISTINCT FROM NEW.start_date) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'start_date'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.end_date IS DISTINCT FROM NEW.end_date) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'end_date'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.currency_id IS DISTINCT FROM NEW.currency_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'currency_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.price_novat IS DISTINCT FROM NEW.price_novat) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'price_novat'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.vat_id IS DISTINCT FROM NEW.vat_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'vat_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.price IS DISTINCT FROM NEW.price) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'price'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.per_day IS DISTINCT FROM NEW.per_day) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'per_day'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.first_day IS DISTINCT FROM NEW.first_day) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'first_day'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.last_day IS DISTINCT FROM NEW.last_day) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'last_day'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.min_day IS DISTINCT FROM NEW.min_day) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'min_day'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.max_day IS DISTINCT FROM NEW.max_day) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'max_day'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.min_deposit IS DISTINCT FROM NEW.min_deposit) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'min_deposit'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.non_refundable IS DISTINCT FROM NEW.non_refundable) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'non_refundable'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.organization_id IS DISTINCT FROM NEW.organization_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'organization_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.max_age IS DISTINCT FROM NEW.max_age) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'max_age'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.age1_max IS DISTINCT FROM NEW.age1_max) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'age1_max'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.age1_price IS DISTINCT FROM NEW.age1_price) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'age1_price'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.age1_discount IS DISTINCT FROM NEW.age1_discount) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'age1_discount'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.age2_max IS DISTINCT FROM NEW.age2_max) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'age2_max'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.age2_price IS DISTINCT FROM NEW.age2_price) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'age2_price'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.age2_discount IS DISTINCT FROM NEW.age2_discount) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'age2_discount'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.age3_max IS DISTINCT FROM NEW.age3_max) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'age3_max'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.age3_price IS DISTINCT FROM NEW.age3_price) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'age3_price'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.age3_discount IS DISTINCT FROM NEW.age3_discount) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'age3_discount'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.per_person IS DISTINCT FROM NEW.per_person) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'per_person'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.min_age IS DISTINCT FROM NEW.min_age) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'min_age'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.resident_price IS DISTINCT FROM NEW.resident_price) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'resident_price'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.resident_discount IS DISTINCT FROM NEW.resident_discount) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'resident_discount'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.arriving_or_leaving IS DISTINCT FROM NEW.arriving_or_leaving) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'arriving_or_leaving'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.resident2_price IS DISTINCT FROM NEW.resident2_price) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'resident2_price'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.resident2_discount IS DISTINCT FROM NEW.resident2_discount) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'resident2_discount'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.on_date IS DISTINCT FROM NEW.on_date) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'on_date'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.off_date IS DISTINCT FROM NEW.off_date) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'off_date'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.cutoff_date IS DISTINCT FROM NEW.cutoff_date) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'cutoff_date'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.min_deposit2 IS DISTINCT FROM NEW.min_deposit2) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'min_deposit2'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.non_refundable2 IS DISTINCT FROM NEW.non_refundable2) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'non_refundable2'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.cutoff_date2 IS DISTINCT FROM NEW.cutoff_date2) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'cutoff_date2'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.min_deposit3 IS DISTINCT FROM NEW.min_deposit3) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'min_deposit3'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.non_refundable3 IS DISTINCT FROM NEW.non_refundable3) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'non_refundable3'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.cutoff_date3 IS DISTINCT FROM NEW.cutoff_date3) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'cutoff_date3'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.min_deposit4 IS DISTINCT FROM NEW.min_deposit4) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'min_deposit4'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.non_refundable4 IS DISTINCT FROM NEW.non_refundable4) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'non_refundable4'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.cutoff_date4 IS DISTINCT FROM NEW.cutoff_date4) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'cutoff_date4'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.min_deposit5 IS DISTINCT FROM NEW.min_deposit5) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'min_deposit5'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.non_refundable5 IS DISTINCT FROM NEW.non_refundable5) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'non_refundable5'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.cutoff_date5 IS DISTINCT FROM NEW.cutoff_date5) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'cutoff_date5'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.min_deposit6 IS DISTINCT FROM NEW.min_deposit6) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'min_deposit6'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.non_refundable6 IS DISTINCT FROM NEW.non_refundable6) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'non_refundable6'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.unemployed_price IS DISTINCT FROM NEW.unemployed_price) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'unemployed_price'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.unemployed_discount IS DISTINCT FROM NEW.unemployed_discount) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'unemployed_discount'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.facility_fee_price IS DISTINCT FROM NEW.facility_fee_price) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'facility_fee_price'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.facility_fee_discount IS DISTINCT FROM NEW.facility_fee_discount) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'facility_fee_discount'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.discovery_discount IS DISTINCT FROM NEW.discovery_discount) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'discovery_discount'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.discovery_price IS DISTINCT FROM NEW.discovery_price) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'discovery_price'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.discovery_reduced_discount IS DISTINCT FROM NEW.discovery_reduced_discount) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'discovery_reduced_discount'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.discovery_reduced_price IS DISTINCT FROM NEW.discovery_reduced_price) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'discovery_reduced_price'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.working_visit_discount IS DISTINCT FROM NEW.working_visit_discount) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'working_visit_discount'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.working_visit_price IS DISTINCT FROM NEW.working_visit_price) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'working_visit_price'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.guest_discount IS DISTINCT FROM NEW.guest_discount) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'guest_discount'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.guest_price IS DISTINCT FROM NEW.guest_price) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'guest_price'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.arrival_site_id IS DISTINCT FROM NEW.arrival_site_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'arrival_site_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.min_day_ceiling IS DISTINCT FROM NEW.min_day_ceiling) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'min_day_ceiling'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.comment_label_id IS DISTINCT FROM NEW.comment_label_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('rate', true, NEW.id, 'comment_label_id'); END IF;
RETURN NEW;
END;
$$;


ALTER FUNCTION public.record_changes_for_notification_rate() OWNER TO kbs;

--
-- Name: record_changes_for_notification_resource(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.record_changes_for_notification_resource() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN

IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.site_id IS DISTINCT FROM NEW.site_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('resource', true, NEW.id, 'site_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.site_item_family_id IS DISTINCT FROM NEW.site_item_family_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('resource', true, NEW.id, 'site_item_family_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.name IS DISTINCT FROM NEW.name) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('resource', true, NEW.id, 'name'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.site_item_family_code IS DISTINCT FROM NEW.site_item_family_code) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('resource', true, NEW.id, 'site_item_family_code'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.building_id IS DISTINCT FROM NEW.building_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('resource', true, NEW.id, 'building_id'); END IF;
RETURN NEW;
END;
$$;


ALTER FUNCTION public.record_changes_for_notification_resource() OWNER TO kbs;

--
-- Name: record_changes_for_notification_resource_configuration(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.record_changes_for_notification_resource_configuration() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN

IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.resource_id IS DISTINCT FROM NEW.resource_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('resource_configuration', true, NEW.id, 'resource_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.item_id IS DISTINCT FROM NEW.item_id) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('resource_configuration', true, NEW.id, 'item_id'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.start_date IS DISTINCT FROM NEW.start_date) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('resource_configuration', true, NEW.id, 'start_date'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.end_date IS DISTINCT FROM NEW.end_date) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('resource_configuration', true, NEW.id, 'end_date'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.max IS DISTINCT FROM NEW.max) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('resource_configuration', true, NEW.id, 'max'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.max_paper IS DISTINCT FROM NEW.max_paper) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('resource_configuration', true, NEW.id, 'max_paper'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.max_private IS DISTINCT FROM NEW.max_private) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('resource_configuration', true, NEW.id, 'max_private'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.comment IS DISTINCT FROM NEW.comment) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('resource_configuration', true, NEW.id, 'comment'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.online IS DISTINCT FROM NEW.online) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('resource_configuration', true, NEW.id, 'online'); END IF;
IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE' AND OLD.name IS DISTINCT FROM NEW.name) THEN INSERT into sys_log (table_name, update, oid, column_name) values ('resource_configuration', true, NEW.id, 'name'); END IF;
RETURN NEW;
END;
$$;


ALTER FUNCTION public.record_changes_for_notification_resource_configuration() OWNER TO kbs;

--
-- Name: remove_table_notification(text); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.remove_table_notification(tname text) RETURNS void
    LANGUAGE plpgsql
    AS $_$
BEGIN
	EXECUTE format(E'\nDROP TRIGGER record_changes_for_notification ON %1$s;', tname);
END;
$_$;


ALTER FUNCTION public.remove_table_notification(tname text) OWNER TO kbs;

--
-- Name: resource_availability_by_event_items(integer); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.resource_availability_by_event_items(eventid integer) RETURNS SETOF public.resource_availibility
    LANGUAGE sql IMMUTABLE STRICT
    AS $$with dates as (select * from generate_event_dates(eventid) as d(date)),
  date_resource_info as (select d.date,r.site_id,rc.item_id,rc.max as max_online from dates d, event e, resource_configuration rc join resource r on rc.resource_id=r.id join site s on r.site_id=s.id where overlaps(d.date, d.date, rc.start_date, rc.end_date) and s.online and rc.online and (s.event_id=e.id or s.event_id is null and s.organization_id=e.organization_id or exists(select id from option o where o.event_id=e.id and o.site_id=s.id and o.item_id=rc.item_id)) and e.id=eventid),
  date_site_item_info as (select date,site_id,item_id,sum(max_online) as max_online from date_resource_info group by date,site_id,item_id),
  date_site_item_info_with_current as (select dsii.date, dsii.site_id, dsii.item_id, (select sum(dl.quantity) from attendance a join document_line dl on a.document_line_id=dl.id left join resource_configuration rc on rc.id=dl.resource_configuration_id join document d on d.id=dl.document_id where a.present and a.date=dsii.date and dl.site_id=dsii.site_id and dl.item_id=dsii.item_id and not dl.frontend_released and coalesce(rc.online, true)) as current, dsii.max_online from date_site_item_info dsii)
 select CAST(row_number() over(order by date) as int), site_id, item_id, date, CAST(COALESCE(current,0) as int) as current, CAST(max_online as int) as max from date_site_item_info_with_current$$;


ALTER FUNCTION public.resource_availability_by_event_items(eventid integer) OWNER TO kbs;

--
-- Name: resource_capacity_by_event_items(integer); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.resource_capacity_by_event_items(eventid integer) RETURNS SETOF public.resource_capacity
    LANGUAGE sql IMMUTABLE STRICT
    AS $$with dates as (select * from generate_event_dates(eventid) as d(date)),
		date_resource_info as (select d.date, rc.id as resource_configuration_id, r.site_id, rc.item_id, rc.max from dates d, event e, resource_configuration rc join resource r on rc.resource_id=r.id join site s on r.site_id=s.id where overlaps(d.date, d.date, rc.start_date, rc.end_date) and s.online and rc.online and (s.event_id=e.id or s.event_id is null and s.organization_id=e.organization_id) and e.id=eventid),
		date_resource_info_with_current as (select dri.date, dri.resource_configuration_id, dri.site_id, dri.item_id, (select coalesce(sum(dl.quantity),0) from attendance a join document_line dl on a.document_line_id=dl.id where a.present and a.date=dri.date and dl.resource_configuration_id=dri.resource_configuration_id and not dl.frontend_released) as current, dri.max from date_resource_info dri),
		resource_info as (select resource_configuration_id, min(site_id) as site_id, min(item_id) as item_id, min(max) as capacity,max(coalesce(current,0)) as current from date_resource_info_with_current group by resource_configuration_id having min(site_id)=max(site_id) and min(item_id)=max(item_id))
	select CAST(row_number() over(order by site_id,item_id,capacity) as int),site_id,item_id,capacity,CAST(count(*) as int) as count,CAST(sum(case current when 0 then 1 else 0 end) as int) as free from resource_info group by site_id,item_id,capacity;$$;


ALTER FUNCTION public.resource_capacity_by_event_items(eventid integer) OWNER TO kbs;

--
-- Name: set_transaction_parameters(boolean); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.set_transaction_parameters(backend boolean) RETURNS boolean
    LANGUAGE plpgsql
    AS $$
BEGIN
create temporary table if not exists transaction_parameter (backend bool) ON COMMIT DROP;
insert into transaction_parameter (backend) values (backend);
RETURN true;
END;
$$;


ALTER FUNCTION public.set_transaction_parameters(backend boolean) OWNER TO kbs;

--
-- Name: shift_date_time_range(character varying, integer); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.shift_date_time_range(date_time_range character varying, days_count integer) RETURNS character varying
    LANGUAGE plpgsql
    AS $_$
DECLARE
	statement text;new_date_time_range varchar := null;BEGIN
	if (date_time_range is not null) then
		select into statement 'select ''' || regexp_replace(date_time_range, '(\d\d/\d\d/\d\d\d\d)', ''' || to_char(to_date(''\1'', ''DD/MM/YYYY'') + $1, ''DD/MM/YYYY'') || ''', 'g') || '''';EXECUTE statement into new_date_time_range using days_count;end if;return new_date_time_range;END;$_$;


ALTER FUNCTION public.shift_date_time_range(date_time_range character varying, days_count integer) OWNER TO kbs;

--
-- Name: shift_event_for(integer, date); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.shift_event_for(event_id integer, start_date date) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
	return shift_event_for(event_id, (select start_date - e.start_date from event e where id=event_id));END;$$;


ALTER FUNCTION public.shift_event_for(event_id integer, start_date date) OWNER TO kbs;

--
-- Name: shift_event_for(integer, integer); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.shift_event_for(event_id integer, days_count integer) RETURNS integer
    LANGUAGE plpgsql
    AS $_$
BEGIN
   update event set
		  opening_date = opening_date + (days_count || ' days')::interval
		, booking_closing_date = booking_closing_date + (days_count || ' days')::interval
		, pre_date = pre_date + days_count
		, start_date = start_date + days_count
		, end_date = end_date + days_count
		, post_date = post_date + days_count
		, date_time_range = shift_date_time_range(date_time_range, days_count)
		, min_date_time_range = shift_date_time_range(min_date_time_range, days_count)
		, max_date_time_range = shift_date_time_range(max_date_time_range, days_count)
	where id=event_id;

	update option o set
		  date_time_range = shift_date_time_range(date_time_range, days_count)
		, first_day = first_day + days_count
		, last_day = last_day + days_count
		, first_excluded_day = first_excluded_day + days_count
		, last_excluded_day = last_excluded_day + days_count
	where o.event_id=$1;

	update option_condition oc set
		  first_day = oc.first_day + days_count
		, last_day = oc.last_day + days_count
	from option o
	where
		oc.option_id = o.id and
		o.event_id=$1;

	update date_info di set
		  date = date + days_count
		, end_date = end_date + days_count
		, date_time_range = shift_date_time_range(date_time_range, days_count)
		, min_date_time_range = shift_date_time_range(min_date_time_range, days_count)
		, max_date_time_range = shift_date_time_range(max_date_time_range, days_count)
	where di.event_id=$1;

	update rate ru set
		  start_date = ru.start_date + days_count
		, end_date = ru.end_date + days_count
		, on_date = ru.on_date + days_count
		, off_date = ru.off_date + days_count
		, first_day = ru.first_day + days_count
		, last_day = ru.last_day + days_count
		, cutoff_date = ru.cutoff_date + days_count
		, cutoff_date2 = ru.cutoff_date2 + days_count
		, cutoff_date3 = ru.cutoff_date3 + days_count
		, cutoff_date4 = ru.cutoff_date4 + days_count
		, cutoff_date5 = ru.cutoff_date5 + days_count
  from rate r join site s on s.id=r.site_id
	where r.id=ru.id and s.event_id=$1;

	return event_id;
END;
$_$;


ALTER FUNCTION public.shift_event_for(event_id integer, days_count integer) OWNER TO kbs;

--
-- Name: shift_event_to(integer, date); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.shift_event_to(event_id integer, new_start_date date) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
	return shift_event_for(event_id, (select new_start_date - e.start_date from event e where id=event_id));END;$$;


ALTER FUNCTION public.shift_event_to(event_id integer, new_start_date date) OWNER TO kbs;

--
-- Name: trigger_allocation_rule_apply_rule(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_allocation_rule_apply_rule() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
   RAISE NOTICE 'Entering trigger %.%(%)', TG_RELNAME, TG_NAME, NEW.id;
	update document_line dl set trigger_defer_allocate=true from document d, item i, organization o
		left join language l on l.id=NEW.if_language_id
		where not dl.cancelled and not dl.lock_allocation and not d.arrived and i.id=dl.item_id and i.family_id=NEW.item_family_id and d.id=dl.document_id and d.event_id=NEW.event_id and (o.id=d.person_organization_id or NEW.if_country_id is null)
		and (NEW.if_language_id is null or l.iso_639_1 = d.person_lang)
		and (NEW.if_country_id is null or o.country_id = NEW.if_country_id)
		and (NEW.if_organization_id is null or d.person_organization_id = NEW.if_organization_id)
		and (not NEW.if_child or d.person_age is not null)
		and (not NEW.if_carer or exists(select * from document where not cancelled and (person_carer1_document_id=d.id or person_carer2_document_id=d.id)))
      and (not NEW.if_lay or not d.person_ordained)
      and (not NEW.if_ordained or d.person_ordained)
		and (NEW.if_ref_min is null or d.ref >= NEW.if_ref_min)
		and (NEW.if_ref_max is null or d.ref <= NEW.if_ref_max)
		and (NEW.if_site_id is null and NEW.if_item_id is null or (NEW.if_site_id is null or dl.site_id = NEW.if_site_id) and (NEW.if_item_id is null or dl.item_id = NEW.if_item_id) or exists(select * from document_line dl2 where document_id=d.id and not cancelled and (NEW.if_site_id is null or dl2.site_id = NEW.if_site_id) and (NEW.if_item_id is null or dl2.item_id = NEW.if_item_id)))
		and exists(select * from resource_configuration rc join resource r on r.id=rc.resource_id join site s on s.id=r.site_id join event e on e.id=NEW.event_id join item rc_i on rc_i.id=rc.item_id where s.event_id=NEW.event_id and rc.item_id=i.id) -- (i.code not in('pdej', 'din')) -- for French Festivals: excluding Petit Déjeuner and Dîner (keeping only Déjeuner)
	;

   update allocation_rule set trigger_allocate=false where id=NEW.id;
	return new;
END $$;


ALTER FUNCTION public.trigger_allocation_rule_apply_rule() OWNER TO kbs;

--
-- Name: trigger_attendance_defer_compute_document_prices_dates(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_attendance_defer_compute_document_prices_dates() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
	THIS attendance%ROWTYPE;
BEGIN
   if (TG_OP = 'DELETE') then THIS := OLD; else THIS := NEW; end if;
   --RAISE NOTICE 'Entering trigger %.%(%)', TG_RELNAME, TG_NAME, THIS.id;
   update document as d set trigger_defer_compute_prices = true from document_line dl where dl.id=THIS.document_line_id and d.id=dl.document_id and trigger_defer_compute_prices = false;
   return THIS;
END $$;


ALTER FUNCTION public.trigger_attendance_defer_compute_document_prices_dates() OWNER TO kbs;

--
-- Name: trigger_document_auto_ref(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_document_auto_ref() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
   RAISE NOTICE 'Entering trigger %.%(%)', TG_RELNAME, TG_NAME, NEW.id;
    -- Setting the booking ref
select into new.ref count(*)+1 from document where event_id=new.event_id;
-- Setting the booking organization if not set (to be the same as event)
if (new.organization_id is null) then
select into new.organization_id e.organization_id from event e where e.id=new.event_id;
end if;
-- Setting the booking activity if not set (to be the same as event)
if (new.activity_id is null) then
select into new.activity_id e.activity_id from event e where e.id=new.event_id;
end if;
-- Setting the booking cart if not set (for frontend bookings only)
if (get_transaction_parameter() = false /* ie frontend request */) then
-- Now raising an exception on double bookings (unless it is a tester)
if (exists(select * from document d join person p on p.id=d.person_id join frontend_account fa on fa.id=p.frontend_account_id where d.event_id=new.event_id and d.person_id=new.person_id and not d.cancelled and not fa.tester)) then
raise exception 'DOUBLEBOOKING';
end if;
if (new.cart_id is null) then
    -- Checking if there is already a booking made for that event with the same frontend account and reusing the same booking cart in that case
select into new.cart_id cart_id from "document" d join person p on p.id=d.person_id join person np on np.id=new.person_id where event_id=new.event_id and p.frontend_account_id=np.frontend_account_id and cart_id is not null order by cart_id limit 1;
-- Otherwise creating a new booking cart
    if (new.cart_id is null) then
insert into cart
(uuid) values (uuid_in(overlay(overlay(md5(random()::text || ':' || clock_timestamp()::text) placing '4' from 13) placing to_hex(floor(random()*(11-8+1) + 8)::int)::text from 17)::cstring))
returning id into new.cart_id;
end if;
end if;
end if;
return new;
END $$;


ALTER FUNCTION public.trigger_document_auto_ref() OWNER TO kbs;

--
-- Name: trigger_document_cancel_other_multiple_bookings(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_document_cancel_other_multiple_bookings() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
	if (OLD.trigger_cancel_other_multiple_bookings = false and NEW.trigger_cancel_other_multiple_bookings = true) then
		insert into history (document_id,username,comment) select id,(select username from sys_sync limit 1),'Cancelled (multiple booking)' from document where not cancelled and multiple_booking_id=NEW.multiple_booking_id and id<>NEW.id;
		if (not found) then
			raise exception 'No more multiple bookings to cancel';
		end if;
		update document set cancelled=true where not cancelled and multiple_booking_id=NEW.multiple_booking_id and id<>NEW.id;
		update document set trigger_cancel_other_multiple_bookings = false where id=NEW.id;
	end if;
	return NEW;
END $$;


ALTER FUNCTION public.trigger_document_cancel_other_multiple_bookings() OWNER TO kbs;

--
-- Name: trigger_document_cascadecancelled(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_document_cascadecancelled() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
	NEW.abandoned := NEW.cancelled and NEW.price_deposit = 0;
	if (OLD.cancelled<>NEW.cancelled or OLD.abandoned<>NEW.abandoned) then
		update document set abandoned = NEW.abandoned where id=NEW.id;
		update document_line set cancelled=NEW.cancelled, abandoned=NEW.abandoned where document_id=NEW.id;
		update multiple_booking set trigger_update_counts=true where id=NEW.multiple_booking_id;
	end if;
	return NEW;
END $$;


ALTER FUNCTION public.trigger_document_cascadecancelled() OWNER TO kbs;

--
-- Name: trigger_document_cascaderead(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_document_cascaderead() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
	UPDATE document_line set read=true where document_id=NEW.id and not read;
	UPDATE money_transfer set read=true where document_id=NEW.id and not read;
	UPDATE mail set read=true where document_id=NEW.id and not read;
	RETURN NEW;
END $$;


ALTER FUNCTION public.trigger_document_cascaderead() OWNER TO kbs;

--
-- Name: trigger_document_check_multiple_booking(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_document_check_multiple_booking() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
	doc document%ROWTYPE;
	mbid document.multiple_booking_id%TYPE;
	names text; 
BEGIN
	if (TG_OP = 'INSERT' or OLD.trigger_check_multiple_booking = false and NEW.trigger_check_multiple_booking = true) then
		names := abc_names(NEW.person_first_name || ' ' || NEW.person_last_name);
		select into doc * from document
				 where id<>NEW.id and event_id=NEW.event_id and (
					 person_id=NEW.person_id
					 or (
						abc_names(person_first_name || ' ' || person_last_name) = names
						and (
							not person_ordained
							or person_email = NEW.person_email
							or person_organization_id = NEW.person_organization_id
							or NEW.person_organization_id is null and person_organization_id is null
						)
					)
				 ) limit 1;
		if found then
			mbid := doc.multiple_booking_id;
			if (mbid is null) then
				INSERT INTO multiple_booking (event_id) VALUES (NEW.event_id);
				mbid = currval('multiple_booking_id_seq');
			end if;
			update document set multiple_booking_id = mbid
				 where event_id=NEW.event_id and (
					 person_id=NEW.person_id
					 or (
						abc_names(person_first_name || ' ' || person_last_name) = names
						and (
							not person_ordained
							or person_email = NEW.person_email
							or person_organization_id = NEW.person_organization_id
							or NEW.person_organization_id is null and person_organization_id is null
						)
					)
				 );
			update multiple_booking set trigger_update_counts=true where id=mbid;
		end if;
		if (NEW.trigger_check_multiple_booking) then
			update document set trigger_check_multiple_booking = false where id=NEW.id;
		end if;
	end if;
	-- second possibility = change of not_multiple_booking_change_id;
	if (TG_OP = 'UPDATE' and (OLD.not_multiple_booking_id is null and NEW.not_multiple_booking_id is not null or OLD.not_multiple_booking_id is not null and NEW.not_multiple_booking_id is null or OLD.not_multiple_booking_id<>NEW.not_multiple_booking_id)) then
			update multiple_booking set trigger_update_counts=true where id in (OLD.not_multiple_booking_id, NEW.not_multiple_booking_id, NEW.multiple_booking_id);
	end if;
	return NEW;
END $$;


ALTER FUNCTION public.trigger_document_check_multiple_booking() OWNER TO kbs;

--
-- Name: trigger_document_defer_compute_lines_deposit(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_document_defer_compute_lines_deposit() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
   RAISE NOTICE 'Entering trigger %.%(%)', TG_RELNAME, TG_NAME, NEW.id;
   update document as d set trigger_defer_compute_lines_deposit=true where trigger_defer_compute_lines_deposit=false and d.id=NEW.id;
	return new;
END $$;


ALTER FUNCTION public.trigger_document_defer_compute_lines_deposit() OWNER TO kbs;

--
-- Name: trigger_document_defer_compute_prices(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_document_defer_compute_prices() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
BEGIN
   RAISE NOTICE 'Entering trigger %.%(%)', TG_RELNAME, TG_NAME, NEW.id;
   update document as d set trigger_defer_compute_prices=true where trigger_defer_compute_prices=false and d.id=NEW.id;
	return new;
END $$;


ALTER FUNCTION public.trigger_document_defer_compute_prices() OWNER TO kbs;

--
-- Name: trigger_document_generate_mails_on_booking(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_document_generate_mails_on_booking() RETURNS trigger
    LANGUAGE plpgsql
    AS $_$
DECLARE
	lt letter%ROWTYPE;
	ml mail%ROWTYPE;
BEGIN
   RAISE NOTICE 'Entering trigger %.%(%)', TG_RELNAME, TG_NAME, NEW.id;
	FOR lt IN EXECUTE 'SELECT * FROM letter l JOIN letter_type t ON l.type_id=t.id, document d WHERE d.id=$1 and t.cart=true AND (l.event_id=d.event_id OR l.event_id is null AND l.organization_id=d.organization_id) ORDER BY CASE WHEN l.event_id=d.event_id THEN 0 ELSE l.id END' USING NEW.id
	LOOP
		SELECT INTO ml * FROM mail m JOIN document d ON m.document_id=d.id WHERE d.cart_id=NEW.cart_id AND letter_id=lt.id AND EXISTS(SELECT * FROM recipient r WHERE r.mail_id=m.id AND r.email=NEW.person_email);
		IF NOT FOUND THEN
			update document set trigger_send_letter_id=lt.id where id=NEW.id;
		END IF;
	END LOOP;
	RETURN NEW;
END $_$;


ALTER FUNCTION public.trigger_document_generate_mails_on_booking() OWNER TO kbs;

--
-- Name: trigger_document_line_auto_allocate(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_document_line_auto_allocate() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN	
	return NEW;
END $$;


ALTER FUNCTION public.trigger_document_line_auto_allocate() OWNER TO kbs;

--
-- Name: trigger_document_line_cascadeunread(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_document_line_cascadeunread() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
	UPDATE document set read=false where id=NEW.document_id and read;
	RETURN NEW;
END $$;


ALTER FUNCTION public.trigger_document_line_cascadeunread() OWNER TO kbs;

--
-- Name: trigger_document_line_defer_allocate(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_document_line_defer_allocate() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
	share bool;
BEGIN
   -- RAISE NOTICE 'Entering trigger %.%(%)', TG_RELNAME, TG_NAME, NEW.id;
	-- first, checking that there was really a change (an update trigger is triggered whenever new values are identical or not to old ones)
	IF (TG_OP = 'INSERT' or NEW.site_id is distinct from OLD.site_id or NEW.item_id is distinct from OLD.item_id or NEW.dates is distinct from OLD.dates) THEN
		-- then, checking that it's not a share_mate item (because mates are managed by on_share_linked_copy_info trigger that automatically allocates to the same resource as the room booker when the link is made)
		select into share share_mate from item where id=NEW.item_id;
		IF share = false THEN
 -- Now all OK to trigger a normal allocation
	   	update document_line dlu set trigger_defer_allocate=true from document_line dl join item i on i.id=dl.item_id where dl.id=dlu.id and dl.trigger_defer_allocate=false and dl.id=NEW.id and (i.code is null or i.code not in('pdej', 'din')); -- for French Festivals: excluding Petit Déjeuner and Dîner (keeping only Déjeuner)
		END IF;
	END IF;
	return NEW;
END $$;


ALTER FUNCTION public.trigger_document_line_defer_allocate() OWNER TO kbs;

--
-- Name: trigger_document_line_defer_compute_document_prices(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_document_line_defer_compute_document_prices() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
   THIS document_line%ROWTYPE;
BEGIN
   if (TG_OP = 'DELETE') then THIS := OLD; else THIS := NEW; end if;
   RAISE NOTICE 'Entering trigger %.%(%)', TG_RELNAME, TG_NAME, THIS.id;
   update document as d set trigger_defer_compute_prices=true where trigger_defer_compute_prices=false and d.id=THIS.document_id;
	IF (THIS.share_mate_owner_document_line_id IS NOT NULL) THEN
		-- infinite deferred loop between the two documents !!! update document set trigger_defer_compute_prices=true from document d join document_line dl on d.id=dl.document_id where d.trigger_defer_compute_prices=false and dl.id=THIS.share_mate_owner_document_line_id;
		-- so we just update now what is needed: the quantity for resource allocation (automatically set for owners, manually for other cases)
		update document_line as dlo
			set quantity = greatest(1, dlo.share_owner_quantity - COALESCE((select sum(dlm.quantity) from document_line dlm where dlm.share_mate_owner_document_line_id=dlo.id and not dlm.cancelled), 0))
			where dlo.id = THIS.share_mate_owner_document_line_id and dlo.share_owner = true;
	END IF;
	return new;
END $$;


ALTER FUNCTION public.trigger_document_line_defer_compute_document_prices() OWNER TO kbs;

--
-- Name: trigger_document_line_on_cancelled_auto_release(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_document_line_on_cancelled_auto_release() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
	if (NEW.cancelled) then -- cancelled
		update document_line dlu set backend_released=true, frontend_released=true from document_line dl join resource_configuration rc on rc.id=dl.resource_configuration_id join resource r on r.id=rc.resource_id join site_item_family sif on sif.id=r.site_item_family_id where dlu.id=NEW.id and dl.id=dlu.id and sif.auto_release;else -- uncancelled
		update document_line set backend_released=false, frontend_released=false where id=NEW.id;end if;return NEW;END $$;


ALTER FUNCTION public.trigger_document_line_on_cancelled_auto_release() OWNER TO kbs;

--
-- Name: trigger_document_line_on_not_system_allocated(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_document_line_on_not_system_allocated() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
	NEW.system_allocated = false;
	return NEW;
END $$;


ALTER FUNCTION public.trigger_document_line_on_not_system_allocated() OWNER TO kbs;

--
-- Name: trigger_document_line_on_owner_changes_cascade_mates(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_document_line_on_owner_changes_cascade_mates() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
	update document_line set
		site_id=NEW.site_id,
		item_id=NEW.item_id,
		resource_configuration_id=NEW.resource_configuration_id,
		backend_released=NEW.backend_released,
		frontend_released=NEW.frontend_released
	where share_mate_owner_document_line_id=NEW.id;
	if (NEW.dates <> OLD.dates) then
		DELETE FROM attendance am using document_line dlm WHERE dlm.id=am.document_line_id and dlm.share_mate_owner_document_line_id = NEW.id;
		INSERT INTO attendance (document_line_id,date,time) SELECT dlm.id, ao.date, ao.time FROM attendance ao, document_line dlm WHERE ao.document_line_id=NEW.id and dlm.share_mate_owner_document_line_id = NEW.id;
	end if;
	RETURN NEW;
END $$;


ALTER FUNCTION public.trigger_document_line_on_owner_changes_cascade_mates() OWNER TO kbs;

--
-- Name: trigger_document_line_on_share_linked_copy_info(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_document_line_on_share_linked_copy_info() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
   RAISE NOTICE 'Entering trigger %.%(%)', TG_RELNAME, TG_NAME, NEW.id;
	IF (NEW.share_mate_owner_document_line_id IS NOT NULL) THEN
		UPDATE document_line dl SET site_id=dlo.site_id, item_id=dlo.item_id, resource_configuration_id=dlo.resource_configuration_id FROM document_line dlo WHERE dl.id=NEW.id AND dlo.id=NEW.share_mate_owner_document_line_id;
		DELETE FROM attendance WHERE document_line_id = NEW.id;
		INSERT INTO attendance (document_line_id,date,time) SELECT NEW.id, date, time FROM attendance WHERE document_line_id=NEW.share_mate_owner_document_line_id;
	END IF;
	RETURN NEW;
END $$;


ALTER FUNCTION public.trigger_document_line_on_share_linked_copy_info() OWNER TO kbs;

--
-- Name: trigger_document_line_set_cancellation_date(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_document_line_set_cancellation_date() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
	NEW.cancellation_date := now();
	return NEW;
END $$;


ALTER FUNCTION public.trigger_document_line_set_cancellation_date() OWNER TO kbs;

--
-- Name: trigger_document_line_set_donation_site(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_document_line_set_donation_site() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
	select into NEW.site_id coalesce(site_id, NEW.site_id) from option where item_id=NEW.item_id and event_id=(select event_id from document where id=NEW.document_id);return NEW;END $$;


ALTER FUNCTION public.trigger_document_line_set_donation_site() OWNER TO kbs;

--
-- Name: trigger_document_line_set_lang(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_document_line_set_lang() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
	lang item.name%TYPE;
BEGIN
	select into lang i.name from item i join item_family f on f.id=i.family_id where i.id=NEW.item_id and f.code='transl';
	if (FOUND) then
		lang = case when lang='Hard of Hearing' then 'en' when lang='German' then 'de' when lang='French' then 'fr' when lang='Spanish' then 'es' when lang='Portuguese' then 'pt' else null end;
		if (lang is not null) then
			update document set person_lang = lang where id=NEW.document_id;
		end if;
	end if; 
	return NEW;
END $$;


ALTER FUNCTION public.trigger_document_line_set_lang() OWNER TO kbs;

--
-- Name: trigger_document_merge_from_other_multiple_bookings(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_document_merge_from_other_multiple_bookings() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
	if (OLD.trigger_merge_from_other_multiple_bookings = false and NEW.trigger_merge_from_other_multiple_bookings = true) then
		insert into document_line 	(document_id,site_id,item_id,private,share_owner,share_owner_quantity,share_owner_mate1_name,share_owner_mate2_name,share_owner_mate3_name,share_owner_mate4_name,share_owner_mate5_name,share_owner_mate6_name,share_owner_mate7_name,share_mate,share_mate_owner_name,share_mate_owner_person_id,share_mate_owner_document_line_id,share_mate_charged,allocate,lock_allocation,resource_id,read)
						   		select NEW.id     ,site_id,item_id,private,share_owner,share_owner_quantity,share_owner_mate1_name,share_owner_mate2_name,share_owner_mate3_name,share_owner_mate4_name,share_owner_mate5_name,share_owner_mate6_name,share_owner_mate7_name,share_mate,share_mate_owner_name,share_mate_owner_person_id,share_mate_owner_document_line_id,share_mate_charged,false,lock_allocation,resource_id,dl.read
									from document_line dl join document d on d.id=dl.document_id
									where d.id<>NEW.id and not d.cancelled and not dl.cancelled and d.multiple_booking_id=NEW.multiple_booking_id and (d.not_multiple_booking_id is null or d.not_multiple_booking_id<>NEW.multiple_booking_id)
									and not exists(select * from document_line where document_id=NEW.id and not cancelled and site_id=dl.site_id and item_id=dl.item_id);		
		insert into attendance (document_line_id,    date,    present,   charged)
								      select dl.id     , a.date,  a.present, a.charged
										from attendance a join document_line adl on adl.id=a.document_line_id join document ad on ad.id=adl.document_id,
											  document_line dl  join document d on d.id=dl.document_id
									 where ad.id<>NEW.id and not ad.cancelled and not adl.cancelled and ad.multiple_booking_id=NEW.multiple_booking_id and (ad.not_multiple_booking_id is null or ad.not_multiple_booking_id<>NEW.multiple_booking_id) and
											 d.id=NEW.id and not dl.cancelled and
											 dl.site_id=adl.site_id and dl.item_id=adl.item_id
									 and not exists(select * from attendance where date=a.date and document_line_id=dl.id);
		if (not found) then
			raise exception 'Nothing more to merge';
		end if;
		update document set trigger_merge_from_other_multiple_bookings = false where id=NEW.id;
	end if;
	return NEW;
END $$;


ALTER FUNCTION public.trigger_document_merge_from_other_multiple_bookings() OWNER TO kbs;

--
-- Name: trigger_document_read_on_confirmed_or_arrived(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_document_read_on_confirmed_or_arrived() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    update document set read = true where id=NEW.id; -- this will also trigger cascadeRead
    return NEW;
END $$;


ALTER FUNCTION public.trigger_document_read_on_confirmed_or_arrived() OWNER TO kbs;

--
-- Name: trigger_document_send_letter(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_document_send_letter() RETURNS trigger
    LANGUAGE plpgsql
    AS $_$
DECLARE
	lt letter%ROWTYPE;
	ma mail_account%ROWTYPE;
	lang document.person_lang%TYPE;
	body letter.en%TYPE;
	subject letter.subject_en%TYPE;
BEGIN
   RAISE NOTICE 'Entering trigger %.%(%)', TG_RELNAME, TG_NAME, NEW.id;
	SELECT INTO lt * FROM letter l WHERE l.id=NEW.trigger_send_letter_id;
	IF FOUND THEN
		SELECT INTO ma * FROM mail_account where organization_id=NEW.organization_id order by case when id=lt.account_id then 0 else 1 end, case when event_id=NEW.event_id then 0 else id end LIMIT 1;
		IF FOUND THEN
			lang := NEW.person_lang;
			EXECUTE 'SELECT ($1).subject_' || lang INTO subject USING lt;
			IF (subject is null) THEN
				lang := 'en';
				subject := lt.subject_en;
			END IF;
			EXECUTE 'SELECT ($1).' || lang INTO body USING lt;
			body := interpret_brackets(body, NEW.id, lang);
			subject := interpret_brackets(subject, NEW.id, lang);
			INSERT INTO mail (account_id,letter_id, document_id, background, subject, content, read) values (ma.id, lt.id, NEW.id, true, subject, body, true);
		END IF;
	END IF;
	update document set trigger_send_letter_id=null where id=NEW.id;
	RETURN NEW;
END $_$;


ALTER FUNCTION public.trigger_document_send_letter() OWNER TO kbs;

--
-- Name: trigger_document_send_system_letter(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_document_send_system_letter() RETURNS trigger
    LANGUAGE plpgsql
    AS $_$
DECLARE
	lt letter%ROWTYPE;
	ma mail_account%ROWTYPE;
	lang document.person_lang%TYPE;
	body letter.en%TYPE;
	subject letter.subject_en%TYPE;
	mail_id mail.id%TYPE;
BEGIN
   RAISE NOTICE 'Entering trigger %.%(%)', TG_RELNAME, TG_NAME, NEW.id;
	SELECT INTO lt * FROM letter l WHERE l.id=NEW.trigger_send_system_letter_id;
	IF FOUND THEN
		SELECT INTO ma * FROM mail_account where organization_id=NEW.organization_id order by case when id=lt.account_id then 0 else 1 end, case when event_id=NEW.event_id then 0 else id end LIMIT 1;
		IF FOUND THEN
			lang := NEW.person_lang;
			EXECUTE 'SELECT ($1).subject_' || lang INTO subject USING lt;
			IF (subject is null) THEN
				lang := 'en';
				subject := lt.subject_en;
			END IF;
			EXECUTE 'SELECT ($1).' || lang INTO body USING lt;
			body := interpret_brackets(body, NEW.id, lang);
			subject := interpret_brackets(subject, NEW.id, lang);
			INSERT INTO mail (account_id,letter_id, document_id, background, subject, content, read) values (ma.id, lt.id, NEW.id, true, subject, body, true) returning id into mail_id;
         INSERT INTO history (document_id, mail_id, username, comment) values (NEW.id, mail_id, 'system', 'Sent ''' || subject || '''');
		END IF;
	END IF;
	update document set trigger_send_system_letter_id=null where id=NEW.id;
	RETURN NEW;
END $_$;


ALTER FUNCTION public.trigger_document_send_system_letter() OWNER TO kbs;

--
-- Name: trigger_document_set_cancellation_date(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_document_set_cancellation_date() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
	NEW.cancellation_date := now();
	return NEW;
END $$;


ALTER FUNCTION public.trigger_document_set_cancellation_date() OWNER TO kbs;

--
-- Name: trigger_document_set_person_abc_names(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_document_set_person_abc_names() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
	NEW.person_abc_names := abc_names(NEW.person_first_name || ' ' || NEW.person_last_name);return NEW;END $$;


ALTER FUNCTION public.trigger_document_set_person_abc_names() OWNER TO kbs;

--
-- Name: trigger_document_transfer_from_other_multiple_bookings(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_document_transfer_from_other_multiple_bookings() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
	doc document%ROWTYPE;
	sid int;
BEGIN
	if (OLD.trigger_transfer_from_other_multiple_bookings = false and NEW.trigger_transfer_from_other_multiple_bookings = true) then
		for doc in select * from document where id<>NEW.id and cancelled and price_deposit>0 and multiple_booking_id=NEW.multiple_booking_id and (not_multiple_booking_id is null or not_multiple_booking_id<>NEW.multiple_booking_id)
		loop
			insert into money_transfer (spread,method_id,comment,read) values (true,6,'Transfer #' || doc.ref || ' -> #' || NEW.ref || ' (multiple bookings)', true); -- spread transfer, method = contra
			sid := currval('money_transfer_id_seq');
			insert into money_transfer (parent_id,document_id,amount,comment,read) values (sid, NEW.id,  doc.price_deposit, 'Transfered from #' || doc.ref, true); -- crediting transfer
			insert into money_transfer (parent_id,document_id,amount,comment,read) values (sid, doc.id, -doc.price_deposit, 'Transfered to #' || NEW.ref, true); -- debiting transfer
			insert into history (document_id,username,comment) values (doc.id,(select username from sys_sync limit 1),'Transferred deposit=' || (doc.price_deposit / 100) || ' to #' || NEW.ref);
		end loop;
		if (not found) then
			raise exception 'No more deposit to transfer';
		end if;
		update document set trigger_transfer_from_other_multiple_bookings = false where id=NEW.id;
	end if;
	return NEW;
END $$;


ALTER FUNCTION public.trigger_document_transfer_from_other_multiple_bookings() OWNER TO kbs;

--
-- Name: trigger_frontend_account_generate_password_email(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_frontend_account_generate_password_email() RETURNS trigger
    LANGUAGE plpgsql
    AS $_$
DECLARE
	lt letter%ROWTYPE;
	ml mail%ROWTYPE;
	ma mail_account%ROWTYPE;
	subject letter.subject_en%TYPE;
	body letter.en%TYPE;
	eventid event.id%TYPE := NEW.trigger_send_password_event_id;
	organizationid organization.id%TYPE := 1;
	pwdrst_token frontend_account.pwdreset_token%TYPE;
BEGIN
  	RAISE NOTICE 'Entering trigger %.%(%)', TG_RELNAME, TG_NAME, NEW.id;
  	select into pwdrst_token md5(random()::text || clock_timestamp()::text)::uuid;
   	if (eventid is not null) then
		select into organizationid organization_id from event where id=eventid limit 1;
	end if;
	-- Takes "send password" letter from NKT if none active in the event
	SELECT INTO lt * FROM letter l JOIN letter_type t ON l.type_id=t.id WHERE t.send_password and l.active and (eventid is not null and (l.event_id=eventid or l.event_id is null and l.organization_id=organizationid) or eventid is null and l.event_id is null and l.organization_id=organizationid) order by l.event_id asc LIMIT 1;
	IF FOUND THEN
			SELECT INTO ma * FROM mail_account a where eventid is not null and a.organization_id=organizationid or eventid is null and exists(select * from event e where e.corporation_id=NEW.corporation_id and a.organization_id=e.organization_id) order by case when event_id=eventid then 0 else 1 end,case when a.event_id is null and a.organization_id=organizationid then 0 else 1 end,id LIMIT 1;
			IF FOUND THEN
				EXECUTE 'SELECT ($1).' || NEW.lang INTO body USING lt;
				EXECUTE 'SELECT ($1).subject_' || NEW.lang INTO subject USING lt;
				IF (body is null) THEN
					body := lt.en;
					subject := lt.subject_en;
				END IF;
				body := replace(body, '[username]', NEW.username);
				body := replace(body, '[pwdResetToken]', pwdrst_token);
				INSERT INTO mail (account_id,letter_id,background,subject,content) values (ma.id, lt.id, true, subject, body);
				INSERT INTO recipient (mail_id, email) values (currval('mail_id_seq'), NEW.username);
			END IF;
	END IF;
	UPDATE frontend_account SET trigger_send_password=false, trigger_send_password_event_id=null, pwdreset_token=pwdrst_token, pwdreset_expires = now() + interval '1 day' WHERE id=NEW.id;
	RETURN NEW;
END $_$;


ALTER FUNCTION public.trigger_frontend_account_generate_password_email() OWNER TO kbs;

--
-- Name: trigger_history_append_request_to_document(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_history_append_request_to_document() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
	update document d set request=case when d.request is null or d.request='' then NEW.request else d.request || E'\n\n---\n\n' || NEW.request end where d.id=NEW.document_id;
	return NEW;
END $$;


ALTER FUNCTION public.trigger_history_append_request_to_document() OWNER TO kbs;

--
-- Name: trigger_history_online_defer_send_email(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_history_online_defer_send_email() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
   RAISE NOTICE 'Entering trigger %.%(%)', TG_RELNAME, TG_NAME, NEW.id;
   update history as hu set trigger_defer_send_email=true from history h join document d on d.id=h.document_id join event e on e.id=d.event_id where hu.id=NEW.id and h.id=hu.id and h.trigger_defer_send_email=false and e.send_history_emails;
	return new;
END $$;


ALTER FUNCTION public.trigger_history_online_defer_send_email() OWNER TO kbs;

--
-- Name: trigger_history_send_online_notification_email(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_history_send_online_notification_email() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
	ma mail_account%ROWTYPE;
	subject mail.subject%TYPE;
	body mail.content%TYPE;
BEGIN
   RAISE NOTICE 'Entering trigger %.%(%)', TG_RELNAME, TG_NAME, NEW.id;
	SELECT INTO ma * FROM mail_account m_a join document d on d.id=NEW.document_id where m_a.organization_id=d.organization_id order by case when m_a.event_id=d.event_id then 0 else m_a.id end LIMIT 1;
	IF FOUND THEN
		subject := interpret_brackets('KBS#[ref] [fullName] ' || NEW.comment || ' - [event]', NEW.document_id, 'fr');
		body := interpret_brackets('<html><body>[personalDetails]<hr/>[options]<hr/>Invoiced: [invoiced]<br/>Deposit: [deposit]<br/>Balance: [balance]<hr/>[yourCart]</body></html>', NEW.document_id, 'fr');
		INSERT INTO mail (account_id, letter_id, document_id, background, subject, content, read, out, auto_delete) values (ma.id, null, NEW.document_id, true, subject, body, true, false, true);
	END IF;
	RETURN NEW;
END $$;


ALTER FUNCTION public.trigger_history_send_online_notification_email() OWNER TO kbs;

--
-- Name: trigger_label_copy_translation(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_label_copy_translation() RETURNS trigger
    LANGUAGE plpgsql
    AS $_$
DECLARE
langs char(2)[] := array['de', 'en', 'es', 'fr', 'pt'];
    lang char(2);
    changed_lang char(2);
    new_hstore hstore := hstore(NEW);
    old_hstore hstore := case when TG_OP='INSERT' then null else hstore(OLD) end;
    translation label.en%TYPE;
BEGIN
    RAISE NOTICE 'Entering trigger %.%(%)', TG_RELNAME, TG_NAME, NEW.id;
    foreach lang in array langs loop
    if ((new_hstore -> lang) is not null and (old_hstore is null or (old_hstore -> lang) <> (new_hstore -> lang))) then
    changed_lang := lang;
    exit;
    end if;
end loop;
if (changed_lang is not null) then
foreach lang in array langs loop
if (lang <> changed_lang) then
EXECUTE 'select ' || lang || ' from label where ' || lang || ' is not null and ' || changed_lang || ' = $1 limit 1' into translation using new_hstore -> changed_lang;
if translation is not null then
new_hstore := new_hstore || hstore(lang, translation);
end if;
end if;
end loop;
new := new #= new_hstore;
end if;
return new;
END $_$;


ALTER FUNCTION public.trigger_label_copy_translation() OWNER TO kbs;

--
-- Name: trigger_mail_auto_account(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_mail_auto_account() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
   RAISE NOTICE 'Entering trigger %.%(%)', TG_RELNAME, TG_NAME, NEW.id;
	SELECT INTO NEW.account_id ma.id FROM mail_account ma, document d where d.id=NEW.document_id and ma.organization_id=d.organization_id order by case when NEW.letter_id is not null and ma.id=(select account_id from letter l where l.id=NEW.letter_id) then 0 else 1 end, case when ma.event_id=d.event_id then 0 else ma.id end LIMIT 1;
	RETURN NEW;
END $$;


ALTER FUNCTION public.trigger_mail_auto_account() OWNER TO kbs;

--
-- Name: trigger_mail_auto_delete(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_mail_auto_delete() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
	delete from mail where id=NEW.id;
	return NEW;
END $$;


ALTER FUNCTION public.trigger_mail_auto_delete() OWNER TO kbs;

--
-- Name: trigger_mail_auto_recipient(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_mail_auto_recipient() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
	ma mail_account%ROWTYPE;	
	doc document%ROWTYPE;
BEGIN
   RAISE NOTICE 'Entering trigger %.%(%)', TG_RELNAME, TG_NAME, NEW.id;
	IF (NOT EXISTS(select * from recipient where mail_id=NEW.id)) THEN
		IF (NEW.out) THEN -- ougoing email => sent to the person
				select into doc * from document d where d.id=NEW.document_id;
				IF FOUND THEN
					INSERT INTO recipient (mail_id,person_id, email) values (NEW.id, doc.person_id, doc.person_email);
				END IF;		
		ELSE -- incoming email => sent to the mail_acoount
			SELECT INTO ma * FROM mail_account where id=NEW.account_id LIMIT 1;
			IF FOUND THEN
				INSERT INTO recipient (mail_id, name, email) values (NEW.id, ma.name, ma.email);
			END IF;
		END IF;
	END IF;
	RETURN NEW;
END $$;


ALTER FUNCTION public.trigger_mail_auto_recipient() OWNER TO kbs;

--
-- Name: trigger_mail_cascadeunread(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_mail_cascadeunread() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
	UPDATE document set read=false where id=NEW.document_id and read;
	RETURN NEW;
END $$;


ALTER FUNCTION public.trigger_mail_cascadeunread() OWNER TO kbs;

--
-- Name: trigger_money_flow_transfer_ready_daily_batch(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_money_flow_transfer_ready_daily_batch() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
	next_transfer_timestamp timestamp;BEGIN
   RAISE NOTICE 'Entering trigger %.%(%)', TG_RELNAME, TG_NAME, NEW.id;-- selecting the next expected batch transfer time: the one after the first not yet batch transaction
	select into next_transfer_timestamp date_trunc('day', date) + NEW.auto_transfer_time + case when date < date_trunc('day', date) + NEW.auto_transfer_time then interval '0 day' else interval '1 day' end from money_transfer where transfer_id is null and to_money_account_id=NEW.from_money_account_id and method_id=NEW.method_id and not receipts_transfer and parent_id is null and not pending and successful order by date limit 1;if (now() > next_transfer_timestamp) then -- if we have now reached the next batch transfer time
		-- we create a batch transfer at that time (which will include at least one transaction)
		insert into money_transfer (date, from_money_account_id, to_money_account_id, method_id, receipts_transfer, payment) values (next_transfer_timestamp, NEW.from_money_account_id, NEW.to_money_account_id, NEW.method_id, true, false);end if;NEW.trigger_transfer := false;-- rearming the trigger
	RETURN NEW;END $$;


ALTER FUNCTION public.trigger_money_flow_transfer_ready_daily_batch() OWNER TO kbs;

--
-- Name: trigger_money_transfer_autoset_accounts(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_money_transfer_autoset_accounts() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
   RAISE NOTICE 'Entering trigger %.%(%)', TG_RELNAME, TG_NAME, NEW.id;
	IF (not NEW.spread) THEN
		IF (NEW.from_money_account_id is null OR TG_OP = 'UPDATE' AND OLD.method_id<>NEW.method_id) THEN
			NEW.from_money_account_id := autoset_money_transfer_from_account(NEW, true);
		END IF;
		IF (NEW.to_money_account_id is null OR TG_OP = 'UPDATE' AND OLD.method_id<>NEW.method_id) THEN
			NEW.to_money_account_id := autoset_money_transfer_to_account(NEW, true);
		END IF;
	ELSE
		update money_transfer set trigger_defer_update_spread_money_transfer=true where id=NEW.parent_id and trigger_defer_update_spread_money_transfer=false;
	END IF;

	RETURN NEW;
END $$;


ALTER FUNCTION public.trigger_money_transfer_autoset_accounts() OWNER TO kbs;

--
-- Name: trigger_money_transfer_autoset_children(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_money_transfer_autoset_children() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
	select into NEW.method_id method_id from money_transfer mt where mt.id=NEW.parent_id;
	select into NEW.payment payment from money_transfer mt where mt.id=NEW.parent_id;
	select into NEW.refund refund from money_transfer mt where mt.id=NEW.parent_id;
	select into NEW.pending pending from money_transfer mt where mt.id=NEW.parent_id;
	select into NEW.successful successful from money_transfer mt where mt.id=NEW.parent_id;
	return NEW;
END $$;


ALTER FUNCTION public.trigger_money_transfer_autoset_children() OWNER TO kbs;

--
-- Name: trigger_money_transfer_cascade_children(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_money_transfer_cascade_children() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
	update money_transfer set
		date = NEW.date,
		method_id = NEW.method_id,
		payment = NEW.payment,
		refund = NEW.refund,
		pending = NEW.pending,
		successful = NEW.successful,
		read = NEW.read,
		verified = NEW.verified
		where parent_id=NEW.id;RETURN NEW;END $$;


ALTER FUNCTION public.trigger_money_transfer_cascade_children() OWNER TO kbs;

--
-- Name: trigger_money_transfer_cascadedeletetransfer(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_money_transfer_cascadedeletetransfer() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
	delete from money_transfer where id=OLD.parent_id and amount=0;
	return OLD;
END $$;


ALTER FUNCTION public.trigger_money_transfer_cascadedeletetransfer() OWNER TO kbs;

--
-- Name: trigger_money_transfer_cascadeunread(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_money_transfer_cascadeunread() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
	UPDATE document set read=false where id=NEW.document_id and read;
	RETURN NEW;
END $$;


ALTER FUNCTION public.trigger_money_transfer_cascadeunread() OWNER TO kbs;

--
-- Name: trigger_money_transfer_defer_compute_document_deposit(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_money_transfer_defer_compute_document_deposit() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
   THIS money_transfer%ROWTYPE; 
BEGIN
   if (TG_OP = 'DELETE') then THIS := OLD; else THIS := NEW; end if;
   -- RAISE NOTICE 'Entering trigger %.%(%)', TG_RELNAME, TG_NAME, THIS.id;
   update document as d set trigger_defer_compute_deposit = true where d.id=THIS.document_id and trigger_defer_compute_deposit = false;
	IF (TG_OP = 'UPDATE' AND NEW.document_id<>OLD.document_id) THEN
	   update document as d set trigger_defer_compute_deposit = true where d.id=OLD.document_id and trigger_defer_compute_deposit = false;
	END IF;
	IF (THIS.parent_id is not null) THEN
	   update money_transfer set trigger_defer_update_spread_money_transfer = true where id=THIS.parent_id and trigger_defer_update_spread_money_transfer = false;
	ELSIF (TG_OP = 'UPDATE' AND OLD.successful=false AND NEW.successful=true) THEN
		update money_transfer set pending=false where id=NEW.id;
	END IF;
	RETURN NEW;
END $$;


ALTER FUNCTION public.trigger_money_transfer_defer_compute_document_deposit() OWNER TO kbs;

--
-- Name: trigger_money_transfer_defer_compute_money_account_balances(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_money_transfer_defer_compute_money_account_balances() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
   THIS money_transfer%ROWTYPE;
	PREV money_transfer%ROWTYPE;
BEGIN
	IF (TG_OP = 'DELETE') THEN THIS := OLD; ELSE THIS := NEW; END IF;
	IF (TG_OP = 'INSERT') THEN PREV := NEW; ELSE PREV := OLD; END IF;
   -- RAISE NOTICE 'Entering trigger %.%(%)', TG_RELNAME, TG_NAME, THIS.id;
	IF (THIS.parent_id is null) THEN
	   update money_account set trigger_defer_compute_balances = true where (id=THIS.from_money_account_id or id=THIS.to_money_account_id or id=PREV.from_money_account_id or id=PREV.to_money_account_id) and trigger_defer_compute_balances = false;
		create temporary table if not exists money_account_trigger_info (money_account_id int, date timestamp) ON COMMIT DROP;
		insert into money_account_trigger_info (money_account_id) select id from money_account where (id=THIS.from_money_account_id or id=THIS.to_money_account_id or id=PREV.from_money_account_id or id=PREV.to_money_account_id) and not exists(select * from money_account_trigger_info where money_account_id = id);
		update money_account_trigger_info set date = THIS.date from money_account as ma where money_account_id=id and (date is null or date > THIS.date) and (id=THIS.from_money_account_id or id=THIS.to_money_account_id or id=PREV.from_money_account_id or id=PREV.to_money_account_id);
   END IF;
	RETURN NEW;
END $$;


ALTER FUNCTION public.trigger_money_transfer_defer_compute_money_account_balances() OWNER TO kbs;

--
-- Name: trigger_money_transfer_on_success_send_history_email(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_money_transfer_on_success_send_history_email() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
   update history as hu set trigger_defer_send_email=true from history h join document d on d.id=h.document_id join event e on e.id=d.event_id where hu.money_transfer_id=NEW.id and h.id=hu.id and h.trigger_defer_send_email=false and e.send_history_emails;
	return NEW;
END $$;


ALTER FUNCTION public.trigger_money_transfer_on_success_send_history_email() OWNER TO kbs;

--
-- Name: trigger_money_transfer_on_verified(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_money_transfer_on_verified() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
	if (NEW.verified) then
		if (NEW.expected_amount is not null and NEW.expected_amount<>NEW.amount) then
			RAISE EXCEPTION 'Can''t mark a transfer with error as verified';else
			update money_transfer set
				expected_amount = amount,
				read = true,
				verifier = (select username from sys_sync limit 1)
				where id=NEW.id;end if;update money_transfer set
			read = true,
			pending = false,
			verified = true,
			verifier = (select username from sys_sync limit 1)
			where transfer_id=NEW.id;else
		update money_transfer set
			verified = false
			where transfer_id=NEW.id;end if;RETURN NEW;END $$;


ALTER FUNCTION public.trigger_money_transfer_on_verified() OWNER TO kbs;

--
-- Name: trigger_multiple_booking_update_counts(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_multiple_booking_update_counts() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
	count_all multiple_booking."all"%TYPE;
	count_not_cancelled multiple_booking.not_cancelled%TYPE;
BEGIN
	if (TG_OP = 'INSERT' or OLD.trigger_update_counts = false and NEW.trigger_update_counts = true) then
		select count(*), coalesce(sum(case when cancelled then 0 else 1 end),0) into count_all, count_not_cancelled from document where multiple_booking_id=NEW.id and (not_multiple_booking_id is null or not_multiple_booking_id<>NEW.id);
		update multiple_booking set
			"all" = count_all,
			not_cancelled = count_not_cancelled,
			trigger_update_counts = false
		where id=NEW.id;
	end if;
	return NEW;
END $$;


ALTER FUNCTION public.trigger_multiple_booking_update_counts() OWNER TO kbs;

--
-- Name: trigger_option_autoset_event_from_parent(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_option_autoset_event_from_parent() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
	SELECT INTO NEW.event_id o.event_id FROM option o where o.id=NEW.parent_id;
	return NEW;
END $$;


ALTER FUNCTION public.trigger_option_autoset_event_from_parent() OWNER TO kbs;

--
-- Name: trigger_person_on_email_change_update_frontend_username(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_person_on_email_change_update_frontend_username() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
	update frontend_account set username=NEW.email where id=NEW.frontend_account_id and not exists(select * from person where frontend_account_id=NEW.frontend_account_id and id<NEW.id);
	return NEW;
END $$;


ALTER FUNCTION public.trigger_person_on_email_change_update_frontend_username() OWNER TO kbs;

--
-- Name: trigger_resource_auto_site(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_resource_auto_site() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
   RAISE NOTICE 'Entering trigger %.%(%)', TG_RELNAME, TG_NAME, NEW.id;
	select into new.site_id site_id from site_item_family where id=NEW.site_item_family_id;
	return new;
END $$;


ALTER FUNCTION public.trigger_resource_auto_site() OWNER TO kbs;

--
-- Name: trigger_resource_auto_site_item_family(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_resource_auto_site_item_family() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
   RAISE NOTICE 'Entering trigger %.%(%)', TG_RELNAME, TG_NAME, NEW.id;
	select into new.site_item_family_id sif.id from site_item_family sif join item_family f on f.id=sif.item_family_id where sif.site_id=NEW.site_id and f.code=NEW.site_item_family_code limit 1;
	return new;
END $$;


ALTER FUNCTION public.trigger_resource_auto_site_item_family() OWNER TO kbs;

--
-- Name: trigger_resource_configuration_check_overlap(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_resource_configuration_check_overlap() RETURNS trigger
    LANGUAGE plpgsql
    AS $$

BEGIN

	IF NEW.start_date is not null and NEW.end_date is not null and NEW.start_date > NEW.end_date THEN

		RAISE EXCEPTION 'end_date cannot be before start_date in resource_configuration';

	END IF;

	IF exists(select * from resource_configuration where id<>NEW.id and resource_id=NEW.resource_id and overlaps(start_date, end_date, NEW.start_date, NEW.end_date)) THEN

		RAISE EXCEPTION 'Resource configurations cannot overlap';

	END IF;

	RETURN NEW;

END $$;


ALTER FUNCTION public.trigger_resource_configuration_check_overlap() OWNER TO kbs;

--
-- Name: trigger_resource_configuration_on_item_changed_cascade_document(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_resource_configuration_on_item_changed_cascade_document() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
	update document_line set item_id=NEW.item_id where not cancelled and resource_configuration_id=NEW.id;insert into history (document_id, username, comment) select document_id, (select username from sys_sync limit 1), 'Changed ''' || (select name from resource where id=NEW.resource_id limit 1) || ''' room type from ''' || (select name from item where id=OLD.item_id limit 1) || ''' to ''' || (select name from item where id=NEW.item_id limit 1) || '''' from document_line where not cancelled and resource_configuration_id=NEW.id;RETURN NEW;END $$;


ALTER FUNCTION public.trigger_resource_configuration_on_item_changed_cascade_document() OWNER TO kbs;

--
-- Name: trigger_resource_duplicate(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.trigger_resource_duplicate() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
i int2 := 0;
rid resource.id%TYPE;
prefix resource.name%TYPE;
first int2;
BEGIN
prefix := substring(NEW.name from 1 for char_length(NEW.name) - 1);
SELECT INTO first COUNT(*) FROM resource WHERE site_id=NEW.site_id AND name LIKE prefix || '%';
LOOP
IF i >= NEW.trigger_duplicate THEN EXIT; END IF;
i := i + 1;
INSERT INTO resource (site_id,site_item_family_id,name) VALUES (NEW.site_id,NEW.site_item_family_id,prefix || (first + i));
rid = currval('resource_id_seq');
INSERT INTO resource_configuration (resource_id, item_id, start_date, end_date, max) SELECT rid,rc.item_id,rc.start_date,rc.end_date,rc.max FROM resource_configuration rc WHERE rc.resource_id=NEW.id;
END LOOP;
RETURN NEW;
END $$;


ALTER FUNCTION public.trigger_resource_duplicate() OWNER TO kbs;

--
-- Name: update_documents_min_deposit(); Type: FUNCTION; Schema: public; Owner: kbs
--

CREATE FUNCTION public.update_documents_min_deposit() RETURNS integer
    LANGUAGE plpgsql
    AS $$
  DECLARE
    updated integer;
  begin

perform set_transaction_parameters(true);
with rates as (
	select id from rate r where 
	   (r.cutoff_date is not null and r.cutoff_date <= now()   and (bookings_updated < r.cutoff_date or bookings_updated is null))
	or (r.cutoff_date2 is not null and r.cutoff_date2 <= now() and (bookings_updated < r.cutoff_date2 or bookings_updated is null))
	or (r.cutoff_date3 is not null and r.cutoff_date3 <= now() and (bookings_updated < r.cutoff_date3 or bookings_updated is null))
	or (r.cutoff_date4 is not null and r.cutoff_date4 <= now() and (bookings_updated < r.cutoff_date4 or bookings_updated is null))
	or (r.cutoff_date5 is not null and r.cutoff_date5 <= now() and (bookings_updated < r.cutoff_date5 or bookings_updated is null))
),
updates as (
update
	"document"
set
	trigger_defer_compute_prices = true
from
	rate r,
	document_line dl,
	rates
where
	dl.document_id = "document".id
	and not dl.cancelled
	and not "document".cancelled
	and r.item_id = dl.item_id
	and r.site_id = dl.site_id
	and r.id = rates.id
)
update rate set bookings_updated = now() from rates where rate.id=rates.id;
GET DIAGNOSTICS updated = ROW_COUNT;
return updated;
	END;
$$;


ALTER FUNCTION public.update_documents_min_deposit() OWNER TO kbs;

--
-- Name: first(anyelement); Type: AGGREGATE; Schema: public; Owner: kbs
--

CREATE AGGREGATE public.first(anyelement) (
    SFUNC = public.first_agg,
    STYPE = anyelement
);


ALTER AGGREGATE public.first(anyelement) OWNER TO kbs;

--
-- Name: last(anyelement); Type: AGGREGATE; Schema: public; Owner: kbs
--

CREATE AGGREGATE public.last(anyelement) (
    SFUNC = public.last_agg,
    STYPE = anyelement
);


ALTER AGGREGATE public.last(anyelement) OWNER TO kbs;

--
-- Name: accounting_account; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.accounting_account (
    id integer NOT NULL,
    type_id integer NOT NULL,
    model_id integer,
    number smallint,
    name character varying(64) NOT NULL,
    label_id integer
);


ALTER TABLE public.accounting_account OWNER TO kbs;

--
-- Name: accounting_account_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.accounting_account_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.accounting_account_id_seq OWNER TO kbs;

--
-- Name: accounting_account_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.accounting_account_id_seq OWNED BY public.accounting_account.id;


--
-- Name: accounting_account_type; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.accounting_account_type (
    id integer NOT NULL,
    number smallint NOT NULL,
    name character varying(64) NOT NULL,
    label_id integer
);


ALTER TABLE public.accounting_account_type OWNER TO kbs;

--
-- Name: accounting_account_type_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.accounting_account_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.accounting_account_type_id_seq OWNER TO kbs;

--
-- Name: accounting_account_type_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.accounting_account_type_id_seq OWNED BY public.accounting_account_type.id;


--
-- Name: accounting_model; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.accounting_model (
    id integer NOT NULL,
    name character varying(64) NOT NULL
);


ALTER TABLE public.accounting_model OWNER TO kbs;

--
-- Name: accounting_model_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.accounting_model_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.accounting_model_id_seq OWNER TO kbs;

--
-- Name: accounting_model_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.accounting_model_id_seq OWNED BY public.accounting_model.id;


--
-- Name: activity; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.activity (
    id integer NOT NULL,
    parent_id integer,
    organization_id integer,
    name character varying(64) NOT NULL,
    label_id integer
);


ALTER TABLE public.activity OWNER TO kbs;

--
-- Name: activity_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.activity_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.activity_id_seq OWNER TO kbs;

--
-- Name: activity_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.activity_id_seq OWNED BY public.activity.id;


--
-- Name: activity_state; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.activity_state (
    id integer NOT NULL,
    name character varying(64),
    owner_id integer NOT NULL,
    route character varying(255) NOT NULL,
    parameters character varying(1024)
);


ALTER TABLE public.activity_state OWNER TO kbs;

--
-- Name: activity_state_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.activity_state_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.activity_state_id_seq OWNER TO kbs;

--
-- Name: activity_state_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.activity_state_id_seq OWNED BY public.activity_state.id;


--
-- Name: allocation_rule; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.allocation_rule (
    id integer NOT NULL,
    active boolean DEFAULT false NOT NULL,
    resource_id integer,
    event_id integer NOT NULL,
    item_family_id integer NOT NULL,
    if_language_id integer,
    if_country_id integer,
    if_organization_id integer,
    if_site_id integer,
    if_item_id integer,
    if_child boolean DEFAULT false NOT NULL,
    ord integer,
    trigger_allocate boolean DEFAULT false NOT NULL,
    if_carer boolean DEFAULT false NOT NULL,
    resource_configuration_id integer NOT NULL,
    if_whole_event boolean DEFAULT false NOT NULL,
    if_partial_event boolean DEFAULT false NOT NULL,
    if_lay boolean DEFAULT false NOT NULL,
    if_ordained boolean DEFAULT false NOT NULL,
    if_ref_min integer,
    if_ref_max integer
);


ALTER TABLE public.allocation_rule OWNER TO kbs;

--
-- Name: allocation_rule_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.allocation_rule_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.allocation_rule_id_seq OWNER TO kbs;

--
-- Name: allocation_rule_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.allocation_rule_id_seq OWNED BY public.allocation_rule.id;


--
-- Name: attendance_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.attendance_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.attendance_id_seq OWNER TO kbs;

--
-- Name: attendance_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.attendance_id_seq OWNED BY public.attendance.id;


--
-- Name: authorization_assignment; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.authorization_assignment (
    id integer NOT NULL,
    management_id integer NOT NULL,
    scope_id integer,
    role_id integer,
    rule_id integer,
    active boolean DEFAULT true NOT NULL,
    activity_state_id integer,
    operation_id integer
);


ALTER TABLE public.authorization_assignment OWNER TO kbs;

--
-- Name: authorization_assignment_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.authorization_assignment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.authorization_assignment_id_seq OWNER TO kbs;

--
-- Name: authorization_assignment_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.authorization_assignment_id_seq OWNED BY public.authorization_assignment.id;


--
-- Name: authorization_management; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.authorization_management (
    id integer NOT NULL,
    manager_id integer NOT NULL,
    user_id integer NOT NULL,
    active boolean DEFAULT true NOT NULL
);


ALTER TABLE public.authorization_management OWNER TO kbs;

--
-- Name: authorization_management_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.authorization_management_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.authorization_management_id_seq OWNER TO kbs;

--
-- Name: authorization_management_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.authorization_management_id_seq OWNED BY public.authorization_management.id;


--
-- Name: authorization_role; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.authorization_role (
    id integer NOT NULL,
    name character varying(64) NOT NULL
);


ALTER TABLE public.authorization_role OWNER TO kbs;

--
-- Name: authorization_role_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.authorization_role_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.authorization_role_id_seq OWNER TO kbs;

--
-- Name: authorization_role_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.authorization_role_id_seq OWNED BY public.authorization_role.id;


--
-- Name: authorization_rule; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.authorization_rule (
    id integer NOT NULL,
    name character varying(64) NOT NULL,
    rule character varying(255) NOT NULL
);


ALTER TABLE public.authorization_rule OWNER TO kbs;

--
-- Name: authorization_rule_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.authorization_rule_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.authorization_rule_id_seq OWNER TO kbs;

--
-- Name: authorization_rule_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.authorization_rule_id_seq OWNED BY public.authorization_rule.id;


--
-- Name: authorization_scope; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.authorization_scope (
    id integer NOT NULL,
    name character varying(64) NOT NULL
);


ALTER TABLE public.authorization_scope OWNER TO kbs;

--
-- Name: authorization_scope_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.authorization_scope_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.authorization_scope_id_seq OWNER TO kbs;

--
-- Name: authorization_scope_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.authorization_scope_id_seq OWNED BY public.authorization_scope.id;


--
-- Name: bank; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.bank (
    id integer NOT NULL,
    statement_system integer
);


ALTER TABLE public.bank OWNER TO kbs;

--
-- Name: bank_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.bank_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.bank_id_seq OWNER TO kbs;

--
-- Name: bank_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.bank_id_seq OWNED BY public.bank.id;


--
-- Name: bank_system; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.bank_system (
    id integer NOT NULL,
    gateway_company_id integer
);


ALTER TABLE public.bank_system OWNER TO kbs;

--
-- Name: bank_system_account; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.bank_system_account (
    id integer NOT NULL,
    statement_system_id integer
);


ALTER TABLE public.bank_system_account OWNER TO kbs;

--
-- Name: bank_system_account_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.bank_system_account_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.bank_system_account_id_seq OWNER TO kbs;

--
-- Name: bank_system_account_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.bank_system_account_id_seq OWNED BY public.bank_system_account.id;


--
-- Name: bank_system_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.bank_system_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.bank_system_id_seq OWNER TO kbs;

--
-- Name: bank_system_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.bank_system_id_seq OWNED BY public.bank_system.id;


--
-- Name: booking_form_layout; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.booking_form_layout (
    id integer NOT NULL,
    name character varying(64) NOT NULL,
    practitioner boolean DEFAULT true NOT NULL,
    age boolean DEFAULT true NOT NULL,
    street boolean DEFAULT true NOT NULL,
    admin1 boolean DEFAULT true NOT NULL,
    post_code boolean DEFAULT true NOT NULL,
    organization_prompt_label_id integer
);


ALTER TABLE public.booking_form_layout OWNER TO kbs;

--
-- Name: booking_form_layout_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.booking_form_layout_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.booking_form_layout_id_seq OWNER TO kbs;

--
-- Name: booking_form_layout_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.booking_form_layout_id_seq OWNED BY public.booking_form_layout.id;


--
-- Name: event; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.event (
    id integer NOT NULL,
    organization_id integer NOT NULL,
    activity_id integer,
    name character varying(64) NOT NULL,
    label_id integer,
    start_date date NOT NULL,
    end_date date NOT NULL,
    opening_date timestamp without time zone,
    pre_date date,
    post_date date,
    live boolean DEFAULT false NOT NULL,
    active boolean DEFAULT false NOT NULL,
    css_class character varying(64),
    show_sites boolean DEFAULT false NOT NULL,
    use_frontend_accounts boolean DEFAULT true NOT NULL,
    host character varying(45),
    uri character varying(255),
    booking_form_layout_id integer,
    corporation_id integer NOT NULL,
    booking_success_message_id integer,
    cart_message_id integer,
    date_time_range character varying(255),
    max_date_time_range character varying(255),
    pass_control boolean DEFAULT false NOT NULL,
    image_id integer,
    buddha_id integer,
    teacher_id integer,
    short_description_label_id integer,
    long_description_label_id integer,
    send_history_emails boolean DEFAULT false NOT NULL,
    min_date_time_range character varying(255),
    fees_bottom_label_id integer,
    options_top_label_id integer,
    facebook_conversion_pixel_id character varying(25),
    option_rounding_factor smallint,
    type_id integer,
    support_notice_label_id integer,
    teachings_day_ticket boolean DEFAULT false NOT NULL,
    pass_control_date_time_range character varying(255),
    notification_cart_default_label_id integer,
    notification_cart_payed_label_id integer,
    notification_cart_confirmed_label_id integer,
    notification_cart_payed_confirmed_label_id integer,
    options_hide_days_outside_event boolean DEFAULT false NOT NULL,
    booking_closed boolean DEFAULT false NOT NULL,
    booking_closing_date timestamp without time zone,
    frontend character(3) DEFAULT 'KMC'::bpchar NOT NULL,
    timezone character varying(40),
    bookings_auto_confirm boolean DEFAULT false NOT NULL,
    bookings_auto_confirm_event_id1 integer,
    bookings_auto_confirm_letter_id integer,
    no_account_booking boolean DEFAULT false NOT NULL,
    booking_process_start timestamp without time zone,
    terms_url_en character varying(255),
    terms_url_es character varying(255),
    terms_url_fr character varying(255),
    terms_url_de character varying(255),
    terms_url_pt character varying(255),
    bookings_auto_confirm_event_id3 integer,
    bookings_auto_confirm_event_id2 integer,
    payment_closing_date timestamp(0) without time zone,
    arrival_departure_from_dates_sections boolean DEFAULT false NOT NULL,
    bookings_auto_confirm_event_id4 integer
);


ALTER TABLE public.event OWNER TO kbs;

--
-- Name: COLUMN event.notification_cart_default_label_id; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.event.notification_cart_default_label_id IS 'Default notification to show in the cart if other notifications do not apply or there is at least one cancelled booking in the cart';


--
-- Name: COLUMN event.notification_cart_payed_label_id; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.event.notification_cart_payed_label_id IS 'Notification to show in the cart when booking is fully payed';


--
-- Name: COLUMN event.notification_cart_confirmed_label_id; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.event.notification_cart_confirmed_label_id IS 'Notification to show in the cart when booking is confirmed';


--
-- Name: COLUMN event.notification_cart_payed_confirmed_label_id; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.event.notification_cart_payed_confirmed_label_id IS 'Notification to show in cart when booking is fully payed and confirmed';


--
-- Name: COLUMN event.options_hide_days_outside_event; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.event.options_hide_days_outside_event IS 'KMCFE Hide days outside event.dateTimeRange from Options screen';


--
-- Name: COLUMN event.booking_closed; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.event.booking_closed IS 'Prevents new bookings from front-ends';


--
-- Name: COLUMN event.booking_closing_date; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.event.booking_closing_date IS 'Date and time at which the booking_closed field will be set to true';


--
-- Name: COLUMN event.frontend; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.event.frontend IS 'Indicates which front-end to use for the booking form of this event';


--
-- Name: COLUMN event.timezone; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.event.timezone IS 'IANA time zone id that applies to the event (all dates and times are recorded in UTC, this field only helps to show the dates and times in the correct time zone in the different UI)';


--
-- Name: COLUMN event.bookings_auto_confirm; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.event.bookings_auto_confirm IS 'If TRUE, the system will confirm all fully paid bookings for people who have a confirmed booking in the event of reference';


--
-- Name: COLUMN event.bookings_auto_confirm_event_id1; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.event.bookings_auto_confirm_event_id1 IS 'Event of reference for auto confirm fully paid bookings';


--
-- Name: COLUMN event.bookings_auto_confirm_letter_id; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.event.bookings_auto_confirm_letter_id IS 'Id of the email to send to the booker on auto confirm. Null if no email to send on auto confirm.';


--
-- Name: COLUMN event.no_account_booking; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.event.no_account_booking IS 'Allows bookings without a user account';


--
-- Name: COLUMN event.booking_process_start; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.event.booking_process_start IS 'Date and time when to start to process the queue of bookings. Null to process bookings without any delay.';


--
-- Name: COLUMN event.terms_url_en; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.event.terms_url_en IS 'English T&Cs url and jquery selector';


--
-- Name: COLUMN event.terms_url_es; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.event.terms_url_es IS 'Spanish T&Cs url and jquery selector';


--
-- Name: COLUMN event.terms_url_fr; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.event.terms_url_fr IS 'French T&Cs url and jquery selector';


--
-- Name: COLUMN event.terms_url_de; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.event.terms_url_de IS 'German T&Cs url and jquery selector';


--
-- Name: COLUMN event.terms_url_pt; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.event.terms_url_pt IS 'Portuguese T&Cs url and jquery selector';


--
-- Name: COLUMN event.bookings_auto_confirm_event_id3; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.event.bookings_auto_confirm_event_id3 IS 'Event of reference for auto confirm fully paid bookings';


--
-- Name: COLUMN event.bookings_auto_confirm_event_id2; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.event.bookings_auto_confirm_event_id2 IS 'Event of reference for auto confirm fully paid bookings';


--
-- Name: COLUMN event.payment_closing_date; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.event.payment_closing_date IS 'Date and time after which online payments are not possible anymore';


--
-- Name: COLUMN event.arrival_departure_from_dates_sections; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.event.arrival_departure_from_dates_sections IS 'Populate list of possible arrival and departure dates from Dates Sections if any in KMC front-end';


--
-- Name: COLUMN event.bookings_auto_confirm_event_id4; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.event.bookings_auto_confirm_event_id4 IS 'Event of reference for auto confirm fully paid bookings';


--
-- Name: bookings; Type: VIEW; Schema: public; Owner: kbs
--

CREATE VIEW public.bookings AS
 SELECT DISTINCT ON (d1.id) d1.id AS document_id,
    e1.bookings_auto_confirm_letter_id AS letter_id,
    e2.id AS ref_event_id,
    e2.name AS ref_event_name,
    d2.ref AS ref_document_ref
   FROM (((public.document d1
     JOIN public.event e1 ON ((d1.event_id = e1.id)))
     JOIN public.document d2 ON ((((d2.event_id = e1.bookings_auto_confirm_event_id1) OR (d2.event_id = e1.bookings_auto_confirm_event_id2) OR (d2.event_id = e1.bookings_auto_confirm_event_id3)) AND d2.confirmed AND ((d1.person_id = d2.person_id) OR (((d1.person_email)::text = (d2.person_email)::text) AND ((d1.person_abc_names)::text = (d2.person_abc_names)::text))))))
     JOIN public.event e2 ON ((e2.id = d2.event_id)))
  WHERE (e1.bookings_auto_confirm AND ((e1.bookings_auto_confirm_event_id1 IS NOT NULL) OR (e1.bookings_auto_confirm_event_id2 IS NOT NULL) OR (e1.bookings_auto_confirm_event_id3 IS NOT NULL)) AND (e1.booking_closed = false) AND (d1.confirmed = false) AND (d1.cancelled = false) AND (d1.price_deposit >= d1.price_net))
  ORDER BY d1.id, e2.start_date DESC;


ALTER TABLE public.bookings OWNER TO kbs;

--
-- Name: bracket_pattern; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.bracket_pattern (
    id integer NOT NULL,
    pattern character varying(32) NOT NULL,
    replacement character varying(2048) NOT NULL,
    lang character(2),
    condition character varying(255),
    ord integer,
    description character varying(1024)
);


ALTER TABLE public.bracket_pattern OWNER TO kbs;

--
-- Name: bracket_pattern_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.bracket_pattern_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.bracket_pattern_id_seq OWNER TO kbs;

--
-- Name: bracket_pattern_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.bracket_pattern_id_seq OWNED BY public.bracket_pattern.id;


--
-- Name: buddha; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.buddha (
    id integer NOT NULL,
    name name NOT NULL,
    image_id integer,
    label_id integer,
    who_label_id integer
);


ALTER TABLE public.buddha OWNER TO kbs;

--
-- Name: buddha_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.buddha_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.buddha_id_seq OWNER TO kbs;

--
-- Name: buddha_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.buddha_id_seq OWNED BY public.buddha.id;


--
-- Name: budget; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.budget (
    id integer NOT NULL,
    organization_id integer NOT NULL
);


ALTER TABLE public.budget OWNER TO kbs;

--
-- Name: budget_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.budget_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.budget_id_seq OWNER TO kbs;

--
-- Name: budget_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.budget_id_seq OWNED BY public.budget.id;


--
-- Name: budget_line; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.budget_line (
    id integer NOT NULL,
    budget_id integer NOT NULL
);


ALTER TABLE public.budget_line OWNER TO kbs;

--
-- Name: budget_line_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.budget_line_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.budget_line_id_seq OWNER TO kbs;

--
-- Name: budget_line_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.budget_line_id_seq OWNED BY public.budget_line.id;


--
-- Name: budget_transfer; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.budget_transfer (
    id integer NOT NULL,
    from_id integer NOT NULL,
    to_id integer NOT NULL,
    amount integer NOT NULL,
    comment character varying(1024)
);


ALTER TABLE public.budget_transfer OWNER TO kbs;

--
-- Name: budget_transfer_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.budget_transfer_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.budget_transfer_id_seq OWNER TO kbs;

--
-- Name: budget_transfer_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.budget_transfer_id_seq OWNED BY public.budget_transfer.id;


--
-- Name: building; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.building (
    id integer NOT NULL,
    name character varying(64)
);


ALTER TABLE public.building OWNER TO kbs;

--
-- Name: building_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.building_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.building_id_seq OWNER TO kbs;

--
-- Name: building_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.building_id_seq OWNED BY public.building.id;


--
-- Name: cart; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.cart (
    id integer NOT NULL,
    uuid character varying(45) NOT NULL,
    forward_to_cart_id integer
);


ALTER TABLE public.cart OWNER TO kbs;

--
-- Name: cart_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.cart_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.cart_id_seq OWNER TO kbs;

--
-- Name: cart_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.cart_id_seq OWNED BY public.cart.id;


--
-- Name: cart_message; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.cart_message (
    id integer NOT NULL,
    event_id integer NOT NULL,
    label_id integer NOT NULL,
    show_from timestamp without time zone,
    show_until timestamp without time zone,
    document_cancelled boolean,
    document_confirmed boolean,
    document_fully_paid boolean,
    grp smallint,
    priority smallint,
    ord smallint
);


ALTER TABLE public.cart_message OWNER TO kbs;

--
-- Name: COLUMN cart_message.show_from; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.cart_message.show_from IS 'If not NULL, message will show only from this date time';


--
-- Name: COLUMN cart_message.show_until; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.cart_message.show_until IS 'If not NULL, message will show only until this date time';


--
-- Name: COLUMN cart_message.document_cancelled; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.cart_message.document_cancelled IS 'Show this message for cancelled documents (yes=only cancelled documents, no=only not-cancelled documents, null=all documents)';


--
-- Name: COLUMN cart_message.document_confirmed; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.cart_message.document_confirmed IS 'Show this message for confirmed documents (yes=only confirmed documents, no=only not-confirmed documents, null=all documents)';


--
-- Name: COLUMN cart_message.document_fully_paid; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.cart_message.document_fully_paid IS 'Show this message for fully paid documents (yes=only fully paid documents, no=only not fully paid documents, null=all documents)';


--
-- Name: COLUMN cart_message.grp; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.cart_message.grp IS 'Only one message of a group will be shown, the one with the highest priority with all conditions true';


--
-- Name: COLUMN cart_message.priority; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.cart_message.priority IS 'Priority level of the message in its group (see group field)';


--
-- Name: COLUMN cart_message.ord; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.cart_message.ord IS 'Order in which to show the messages: order by ord, id';


--
-- Name: cart_message_condition; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.cart_message_condition (
    id integer NOT NULL,
    cart_message_id integer NOT NULL,
    site_id integer,
    item_id integer,
    date date,
    booked boolean DEFAULT true NOT NULL
);


ALTER TABLE public.cart_message_condition OWNER TO kbs;

--
-- Name: COLUMN cart_message_condition.site_id; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.cart_message_condition.site_id IS 'Null = any site';


--
-- Name: COLUMN cart_message_condition.item_id; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.cart_message_condition.item_id IS 'Null = any item';


--
-- Name: COLUMN cart_message_condition.date; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.cart_message_condition.date IS 'Null = any date';


--
-- Name: COLUMN cart_message_condition.booked; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.cart_message_condition.booked IS 'True = site/item/date must be booked; False = site/item/date must NOT be booked';


--
-- Name: cart_message_condition_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.cart_message_condition_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.cart_message_condition_id_seq OWNER TO kbs;

--
-- Name: cart_message_condition_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.cart_message_condition_id_seq OWNED BY public.cart_message_condition.id;


--
-- Name: cart_message_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.cart_message_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.cart_message_id_seq OWNER TO kbs;

--
-- Name: cart_message_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.cart_message_id_seq OWNED BY public.cart_message.id;


--
-- Name: centre; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.centre (
    id integer,
    code character varying(15),
    name character varying(255),
    country_id integer,
    open boolean,
    mailing boolean,
    booking_notification_letter_id integer,
    payment_notification_letter_id integer,
    time_zone character varying(255),
    ga_account character varying(255),
    domain_name character varying(255),
    country_code character varying(2)
);


ALTER TABLE public.centre OWNER TO kbs;

--
-- Name: centre_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.centre_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.centre_id_seq OWNER TO kbs;

--
-- Name: continent; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.continent (
    id integer NOT NULL,
    code character(2) NOT NULL,
    name character varying(20) NOT NULL,
    geonameid integer
);


ALTER TABLE public.continent OWNER TO kbs;

--
-- Name: continent_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.continent_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.continent_id_seq OWNER TO kbs;

--
-- Name: continent_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.continent_id_seq OWNED BY public.continent.id;


--
-- Name: country; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.country (
    id integer NOT NULL,
    iso_alpha2 character(2),
    iso_alpha3 character(3),
    iso_numeric integer,
    fips_code character varying(3),
    name character varying(64) NOT NULL,
    areainsqkm integer,
    population integer,
    continent_code character(2),
    continent_id integer,
    currency_code character(3),
    currency_id integer,
    tld character varying(3),
    phone character varying(20),
    postal character varying(60),
    postalregex character varying(200),
    languages character varying(200),
    main_language_code character varying(200),
    main_language_id integer,
    geonameid integer,
    latitude double precision,
    longitude double precision,
    account_model_id integer,
    alternate_name character varying(64),
    alternate_name2 character varying(64),
    label_id integer,
    require_state boolean DEFAULT false NOT NULL,
    require_postcode boolean DEFAULT false NOT NULL
);


ALTER TABLE public.country OWNER TO kbs;

--
-- Name: COLUMN country.require_state; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.country.require_state IS 'Front-end should require a state only for countries with requireState = true and only for these countries.';


--
-- Name: COLUMN country.require_postcode; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.country.require_postcode IS 'Front-end should require a postcode for countries with requirePostcode = true and only for these countries.';


--
-- Name: country_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.country_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.country_id_seq OWNER TO kbs;

--
-- Name: country_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.country_id_seq OWNED BY public.country.id;


--
-- Name: css; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.css (
    id integer NOT NULL,
    event_id integer NOT NULL
);


ALTER TABLE public.css OWNER TO kbs;

--
-- Name: css_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.css_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.css_id_seq OWNER TO kbs;

--
-- Name: css_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.css_id_seq OWNED BY public.css.id;


--
-- Name: currency; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.currency (
    id integer NOT NULL,
    code character(3) NOT NULL,
    name character varying(255) NOT NULL,
    symbol character varying(10)
);


ALTER TABLE public.currency OWNER TO kbs;

--
-- Name: currency_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.currency_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.currency_id_seq OWNER TO kbs;

--
-- Name: currency_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.currency_id_seq OWNED BY public.currency.id;


--
-- Name: currency_support; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.currency_support (
    id integer NOT NULL,
    company_id integer NOT NULL,
    currency_id integer NOT NULL
);


ALTER TABLE public.currency_support OWNER TO kbs;

--
-- Name: currency_support_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.currency_support_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.currency_support_id_seq OWNER TO kbs;

--
-- Name: currency_support_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.currency_support_id_seq OWNED BY public.currency_support.id;


--
-- Name: date_info; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.date_info (
    id integer NOT NULL,
    event_id integer NOT NULL,
    date date NOT NULL,
    label_id integer,
    end_date date,
    exclude boolean DEFAULT false NOT NULL,
    option_id integer,
    date_time_range character varying(255),
    min_date_time_range character varying(255),
    max_date_time_range character varying(255),
    fees_bottom_label_id integer,
    fees_popup_label_id integer,
    force_soldout boolean DEFAULT false NOT NULL,
    dev boolean DEFAULT false NOT NULL
);


ALTER TABLE public.date_info OWNER TO kbs;

--
-- Name: date_info_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.date_info_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.date_info_id_seq OWNER TO kbs;

--
-- Name: date_info_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.date_info_id_seq OWNED BY public.date_info.id;


--
-- Name: document_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.document_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.document_id_seq OWNER TO kbs;

--
-- Name: document_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.document_id_seq OWNED BY public.document.id;


--
-- Name: document_line_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.document_line_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.document_line_id_seq OWNER TO kbs;

--
-- Name: document_line_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.document_line_id_seq OWNED BY public.document_line.id;


--
-- Name: domain; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.domain (
    id integer NOT NULL,
    host character varying(45) NOT NULL,
    organization_id integer NOT NULL,
    ftp_port integer,
    ftp_username character varying(45),
    ftp_password character varying(45),
    db_port integer,
    db_username character varying(45),
    db_password character varying(45),
    ga_account character varying(64)
);


ALTER TABLE public.domain OWNER TO kbs;

--
-- Name: domain_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.domain_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.domain_id_seq OWNER TO kbs;

--
-- Name: domain_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.domain_id_seq OWNED BY public.domain.id;


--
-- Name: driver; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.driver (
    id integer NOT NULL,
    person_id integer,
    is_manager boolean DEFAULT false NOT NULL,
    is_active boolean DEFAULT false NOT NULL,
    wants_shuttle_notice boolean DEFAULT false NOT NULL
);


ALTER TABLE public.driver OWNER TO kbs;

--
-- Name: driver_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.driver_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.driver_id_seq OWNER TO kbs;

--
-- Name: driver_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.driver_id_seq OWNED BY public.driver.id;


--
-- Name: enqueued_request; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.enqueued_request (
    id integer NOT NULL,
    request_string character varying(32768) NOT NULL,
    client_push_address character varying(512),
    creation_date timestamp without time zone DEFAULT now() NOT NULL,
    waiting_date timestamp without time zone,
    paused boolean,
    execution_date timestamp without time zone,
    execution_duration bigint,
    successful boolean,
    error character varying(1024),
    document_id integer,
    acknowledge_date timestamp without time zone,
    reply_string character varying(100000)
);


ALTER TABLE public.enqueued_request OWNER TO kbs;

--
-- Name: enqueued_request_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.enqueued_request_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.enqueued_request_id_seq OWNER TO kbs;

--
-- Name: enqueued_request_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.enqueued_request_id_seq OWNED BY public.enqueued_request.id;


--
-- Name: event_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.event_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.event_id_seq OWNER TO kbs;

--
-- Name: event_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.event_id_seq OWNED BY public.event.id;


--
-- Name: event_type; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.event_type (
    id integer NOT NULL,
    organization_id integer NOT NULL,
    name character varying(64),
    background character varying(255)
);


ALTER TABLE public.event_type OWNER TO kbs;

--
-- Name: event_type_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.event_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.event_type_id_seq OWNER TO kbs;

--
-- Name: event_type_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.event_type_id_seq OWNED BY public.event_type.id;


--
-- Name: filter; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.filter (
    id integer NOT NULL,
    name character varying(64),
    description character varying(1024),
    is_columns boolean DEFAULT false NOT NULL,
    is_condition boolean DEFAULT false NOT NULL,
    is_group boolean DEFAULT false NOT NULL,
    active boolean DEFAULT true NOT NULL,
    activity_name character varying(64),
    class name NOT NULL,
    alias character varying(64),
    columns character varying(2048),
    fields character varying(2048),
    where_clause character varying(2048),
    group_by_clause character varying(2048),
    having_clause character varying(2048),
    order_by_clause character varying(2048),
    limit_clause character varying(2048),
    ord integer
);


ALTER TABLE public.filter OWNER TO kbs;

--
-- Name: filter_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.filter_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.filter_id_seq OWNER TO kbs;

--
-- Name: filter_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.filter_id_seq OWNED BY public.filter.id;


--
-- Name: frontend_account; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.frontend_account (
    id integer NOT NULL,
    username character varying(127) NOT NULL,
    password character(32) NOT NULL,
    lang character(2),
    tester boolean DEFAULT false NOT NULL,
    admin boolean DEFAULT false NOT NULL,
    trigger_send_password boolean DEFAULT false NOT NULL,
    translator boolean DEFAULT false NOT NULL,
    developer boolean DEFAULT false NOT NULL,
    corporation_id integer NOT NULL,
    trigger_send_password_event_id integer,
    pwdreset_token character(36),
    pwdreset_expires timestamp without time zone
);


ALTER TABLE public.frontend_account OWNER TO kbs;

--
-- Name: COLUMN frontend_account.pwdreset_token; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.frontend_account.pwdreset_token IS 'Uuid token for password reset';


--
-- Name: COLUMN frontend_account.pwdreset_expires; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.frontend_account.pwdreset_expires IS 'Password reset token expiration';


--
-- Name: frontend_account_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.frontend_account_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.frontend_account_id_seq OWNER TO kbs;

--
-- Name: frontend_account_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.frontend_account_id_seq OWNED BY public.frontend_account.id;


--
-- Name: gateway_company; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.gateway_company (
    id integer NOT NULL,
    name character varying(64) NOT NULL,
    callback_transaction_ref character varying(255) NOT NULL,
    callback_transaction_status character varying(255) NOT NULL,
    successful_status character varying(255) NOT NULL,
    failed_status character varying(255) NOT NULL,
    information_label_id integer,
    post boolean DEFAULT true NOT NULL,
    json boolean DEFAULT false NOT NULL,
    generate_payment_url boolean DEFAULT false NOT NULL,
    generate_payment_url_accesscode character varying(64),
    request_result boolean DEFAULT false NOT NULL,
    notification_cancel_response text,
    notification_success_response text,
    notification_cancel_cart_redirection boolean DEFAULT false NOT NULL,
    notification_success_cart_redirection boolean DEFAULT false NOT NULL,
    notification_timeout integer,
    alert_before_payment_label_id integer
);


ALTER TABLE public.gateway_company OWNER TO kbs;

--
-- Name: COLUMN gateway_company.json; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.gateway_company.json IS 'Indicates that the data should be a json string';


--
-- Name: COLUMN gateway_company.generate_payment_url; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.gateway_company.generate_payment_url IS 'Indicates that the payment url is unique for each payment and is generated by the gateway server';


--
-- Name: COLUMN gateway_company.generate_payment_url_accesscode; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.gateway_company.generate_payment_url_accesscode IS 'Name of the parameter holding the pre-transaction access code in the gateway response';


--
-- Name: COLUMN gateway_company.request_result; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.gateway_company.request_result IS 'Indicates that the system should request transactions result from the gateway';


--
-- Name: COLUMN gateway_company.notification_cancel_response; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.gateway_company.notification_cancel_response IS 'KBS server response to payment cancellation and failure notification';


--
-- Name: COLUMN gateway_company.notification_success_response; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.gateway_company.notification_success_response IS 'KBS server response to payment success notification';


--
-- Name: COLUMN gateway_company.notification_cancel_cart_redirection; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.gateway_company.notification_cancel_cart_redirection IS 'Indicates that KBS server should redirect to the user cart after payment cancellation notification';


--
-- Name: COLUMN gateway_company.notification_success_cart_redirection; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.gateway_company.notification_success_cart_redirection IS 'Indicates that KBS server should redirect to the user cart after payment success notification';


--
-- Name: COLUMN gateway_company.notification_timeout; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.gateway_company.notification_timeout IS 'Number of seconds after which a payment status is automatically set to ''failed''';


--
-- Name: COLUMN gateway_company.alert_before_payment_label_id; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.gateway_company.alert_before_payment_label_id IS 'Text to show as an alert popup in front-end before redirecting to payment gateway page';


--
-- Name: gateway_company_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.gateway_company_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.gateway_company_id_seq OWNER TO kbs;

--
-- Name: gateway_company_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.gateway_company_id_seq OWNED BY public.gateway_company.id;


--
-- Name: gateway_parameter; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.gateway_parameter (
    id integer NOT NULL,
    company_id integer NOT NULL,
    account_id integer,
    name character varying(64) NOT NULL,
    value character varying(255) NOT NULL,
    test boolean DEFAULT true NOT NULL,
    live boolean DEFAULT true NOT NULL
);


ALTER TABLE public.gateway_parameter OWNER TO kbs;

--
-- Name: gateway_parameter_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.gateway_parameter_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.gateway_parameter_id_seq OWNER TO kbs;

--
-- Name: gateway_parameter_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.gateway_parameter_id_seq OWNED BY public.gateway_parameter.id;


--
-- Name: history; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.history (
    id integer NOT NULL,
    document_id integer NOT NULL,
    user_id integer,
    comment character varying(1024),
    date timestamp without time zone DEFAULT now() NOT NULL,
    request character varying(1024),
    username character varying(64),
    mail_id integer,
    money_transfer_id integer,
    trigger_defer_send_email boolean DEFAULT false NOT NULL
);


ALTER TABLE public.history OWNER TO kbs;

--
-- Name: history_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.history_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.history_id_seq OWNER TO kbs;

--
-- Name: history_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.history_id_seq OWNED BY public.history.id;


--
-- Name: image; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.image (
    id integer NOT NULL,
    name character varying(64),
    url character varying(255),
    frontend_style character varying(128),
    small_url character varying(255),
    small_frontend_style character varying(128),
    medium_url character varying(255),
    medium_frontend_style character varying(128)
);


ALTER TABLE public.image OWNER TO kbs;

--
-- Name: image_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.image_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.image_id_seq OWNER TO kbs;

--
-- Name: image_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.image_id_seq OWNED BY public.image.id;


--
-- Name: item; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.item (
    id integer NOT NULL,
    family_id integer,
    name character varying(64) NOT NULL,
    label_id integer,
    rate_alias_item_id integer,
    share_mate boolean DEFAULT false NOT NULL,
    organization_id integer,
    ord integer NOT NULL,
    code character varying(10),
    temporal boolean DEFAULT false NOT NULL,
    per_resource_label_id integer,
    indoor boolean DEFAULT false NOT NULL
);


ALTER TABLE public.item OWNER TO kbs;

--
-- Name: item_family; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.item_family (
    id integer NOT NULL,
    account_id integer NOT NULL,
    sector_id integer,
    code character varying(10),
    name character varying(64) NOT NULL,
    label_id integer,
    ord integer NOT NULL,
    per_resource_label_id integer
);


ALTER TABLE public.item_family OWNER TO kbs;

--
-- Name: item_family_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.item_family_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.item_family_id_seq OWNER TO kbs;

--
-- Name: item_family_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.item_family_id_seq OWNED BY public.item_family.id;


--
-- Name: item_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.item_id_seq OWNER TO kbs;

--
-- Name: item_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.item_id_seq OWNED BY public.item.id;


--
-- Name: kbs1_country; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.kbs1_country (
    id integer,
    language_id integer,
    code character varying(15),
    name character varying(45)
);


ALTER TABLE public.kbs1_country OWNER TO kbs;

--
-- Name: label; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.label (
    id integer NOT NULL,
    ref character(2),
    de character varying(4096),
    en character varying(4096),
    es character varying(4096),
    fr character varying(4096),
    pt character varying(4096),
    organization_id integer,
    alert boolean DEFAULT false NOT NULL
);


ALTER TABLE public.label OWNER TO kbs;

--
-- Name: label_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.label_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.label_id_seq OWNER TO kbs;

--
-- Name: label_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.label_id_seq OWNED BY public.label.id;


--
-- Name: language; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.language (
    id integer NOT NULL,
    iso_639_1 character(2),
    iso_639_2 character varying(20),
    iso_639_3 character(3),
    main_country_id integer,
    use_country_icon boolean DEFAULT true NOT NULL,
    name character varying(45) NOT NULL,
    label character varying(45),
    supported boolean DEFAULT false NOT NULL
);


ALTER TABLE public.language OWNER TO kbs;

--
-- Name: language_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.language_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.language_id_seq OWNER TO kbs;

--
-- Name: language_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.language_id_seq OWNED BY public.language.id;


--
-- Name: letter; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.letter (
    id integer NOT NULL,
    type_id integer NOT NULL,
    event_id integer,
    organization_id integer NOT NULL,
    subject character varying(255),
    content text,
    en text,
    de text,
    fr text,
    es text,
    pt text,
    subject_en character varying(255),
    subject_de character varying(255),
    subject_fr character varying(255),
    subject_es character varying(255),
    subject_pt character varying(255),
    account_id integer,
    active boolean DEFAULT true NOT NULL,
    name character varying(255) NOT NULL,
    document_condition character varying(2048),
    automation_enabled boolean DEFAULT false NOT NULL
);


ALTER TABLE public.letter OWNER TO kbs;

--
-- Name: COLUMN letter.document_condition; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.letter.document_condition IS 'Required booking condition (expressed in OQL) to send the letter';


--
-- Name: COLUMN letter.automation_enabled; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.letter.automation_enabled IS 'Enable this letter to be automatically sent according to the document condition.';


--
-- Name: letter_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.letter_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.letter_id_seq OWNER TO kbs;

--
-- Name: letter_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.letter_id_seq OWNED BY public.letter.id;


--
-- Name: letter_type; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.letter_type (
    id integer NOT NULL,
    name character varying(64) NOT NULL,
    label_id integer,
    news boolean DEFAULT false NOT NULL,
    event boolean DEFAULT false NOT NULL,
    terms boolean DEFAULT false NOT NULL,
    cart boolean DEFAULT false NOT NULL,
    no_deposit1 boolean DEFAULT false NOT NULL,
    no_deposit2 boolean DEFAULT false NOT NULL,
    cancellation boolean DEFAULT false NOT NULL,
    confirmation boolean DEFAULT false NOT NULL,
    send_password boolean DEFAULT false NOT NULL,
    document_condition character varying(255),
    no_deposit3 boolean DEFAULT false NOT NULL,
    ord integer NOT NULL,
    delay_minutes integer,
    no_shuttle_time boolean DEFAULT false NOT NULL
);


ALTER TABLE public.letter_type OWNER TO kbs;

--
-- Name: letter_type_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.letter_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.letter_type_id_seq OWNER TO kbs;

--
-- Name: letter_type_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.letter_type_id_seq OWNED BY public.letter_type.id;


--
-- Name: lt_test_event; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.lt_test_event (
    id integer NOT NULL,
    event_time timestamp without time zone DEFAULT now() NOT NULL,
    type smallint NOT NULL,
    value integer,
    lt_test_set_id integer NOT NULL
);


ALTER TABLE public.lt_test_event OWNER TO kbs;

--
-- Name: lt_test_event_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.lt_test_event_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.lt_test_event_id_seq OWNER TO kbs;

--
-- Name: lt_test_event_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.lt_test_event_id_seq OWNED BY public.lt_test_event.id;


--
-- Name: lt_test_set; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.lt_test_set (
    id integer NOT NULL,
    name character varying(64) NOT NULL,
    date timestamp without time zone DEFAULT now() NOT NULL,
    comment character varying(1024)
);


ALTER TABLE public.lt_test_set OWNER TO kbs;

--
-- Name: lt_test_set_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.lt_test_set_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.lt_test_set_id_seq OWNER TO kbs;

--
-- Name: lt_test_set_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.lt_test_set_id_seq OWNED BY public.lt_test_set.id;


--
-- Name: mail; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.mail (
    id integer NOT NULL,
    account_id integer NOT NULL,
    letter_id integer,
    document_id integer,
    "out" boolean DEFAULT true NOT NULL,
    background boolean DEFAULT false NOT NULL,
    transmitted boolean DEFAULT false NOT NULL,
    date timestamp without time zone DEFAULT now() NOT NULL,
    subject character varying(255) NOT NULL,
    content text NOT NULL,
    error character varying(255),
    from_name character varying(64),
    from_email character varying(127),
    read boolean DEFAULT false NOT NULL,
    transmission_date timestamp without time zone,
    auto_delete boolean DEFAULT false NOT NULL
);


ALTER TABLE public.mail OWNER TO kbs;

--
-- Name: mail_account; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.mail_account (
    id integer NOT NULL,
    name character varying(64) NOT NULL,
    email character varying(127) NOT NULL,
    organization_id integer NOT NULL,
    smtp_account_id integer NOT NULL,
    event_id integer,
    signature_label_id integer
);


ALTER TABLE public.mail_account OWNER TO kbs;

--
-- Name: mail_account_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.mail_account_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.mail_account_id_seq OWNER TO kbs;

--
-- Name: mail_account_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.mail_account_id_seq OWNED BY public.mail_account.id;


--
-- Name: mail_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.mail_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.mail_id_seq OWNER TO kbs;

--
-- Name: mail_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.mail_id_seq OWNED BY public.mail.id;


--
-- Name: method; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.method (
    id integer NOT NULL,
    code character varying(10) NOT NULL,
    name character varying(64) NOT NULL,
    label_id integer
);


ALTER TABLE public.method OWNER TO kbs;

--
-- Name: method_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.method_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.method_id_seq OWNER TO kbs;

--
-- Name: method_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.method_id_seq OWNED BY public.method.id;


--
-- Name: method_support; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.method_support (
    id integer NOT NULL,
    method_id integer NOT NULL,
    money_account_type_id integer,
    "in" boolean DEFAULT true NOT NULL,
    "out" boolean DEFAULT true NOT NULL
);


ALTER TABLE public.method_support OWNER TO kbs;

--
-- Name: method_support_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.method_support_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.method_support_id_seq OWNER TO kbs;

--
-- Name: method_support_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.method_support_id_seq OWNED BY public.method_support.id;


--
-- Name: metrics; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.metrics (
    id integer NOT NULL,
    date timestamp without time zone DEFAULT now() NOT NULL,
    memory_total bigint,
    memory_free bigint,
    memory_max bigint,
    system_load_average double precision,
    process_cpu_load double precision,
    lt_test_set_id integer
);


ALTER TABLE public.metrics OWNER TO kbs;

--
-- Name: metrics_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.metrics_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.metrics_id_seq OWNER TO kbs;

--
-- Name: metrics_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.metrics_id_seq OWNED BY public.metrics.id;


--
-- Name: money_account; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.money_account (
    id integer NOT NULL,
    type_id integer NOT NULL,
    currency_id integer NOT NULL,
    organization_id integer,
    third_party_id integer,
    bank_system_account_id integer,
    name character varying(64) NOT NULL,
    trigger_defer_compute_balances boolean DEFAULT false NOT NULL,
    gateway_company_id integer,
    event_id integer,
    closed boolean DEFAULT false NOT NULL
);


ALTER TABLE public.money_account OWNER TO kbs;

--
-- Name: money_account_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.money_account_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.money_account_id_seq OWNER TO kbs;

--
-- Name: money_account_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.money_account_id_seq OWNED BY public.money_account.id;


--
-- Name: money_account_type; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.money_account_type (
    id integer NOT NULL,
    code character varying(10) NOT NULL,
    name character varying(64) NOT NULL,
    label_id integer,
    customer boolean DEFAULT false NOT NULL,
    supplier boolean DEFAULT false NOT NULL,
    internal boolean DEFAULT true NOT NULL
);


ALTER TABLE public.money_account_type OWNER TO kbs;

--
-- Name: money_account_type_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.money_account_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.money_account_type_id_seq OWNER TO kbs;

--
-- Name: money_account_type_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.money_account_type_id_seq OWNED BY public.money_account_type.id;


--
-- Name: money_flow; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.money_flow (
    id integer NOT NULL,
    organization_id integer NOT NULL,
    from_money_account_id integer NOT NULL,
    to_money_account_id integer NOT NULL,
    method_id integer,
    auto_transfer_time time without time zone,
    trigger_transfer boolean DEFAULT false NOT NULL,
    positive_amounts boolean DEFAULT true NOT NULL,
    negative_amounts boolean DEFAULT true NOT NULL
);


ALTER TABLE public.money_flow OWNER TO kbs;

--
-- Name: money_flow_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.money_flow_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.money_flow_id_seq OWNER TO kbs;

--
-- Name: money_flow_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.money_flow_id_seq OWNED BY public.money_flow.id;


--
-- Name: money_flow_priority; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.money_flow_priority (
    id integer NOT NULL,
    flow_id integer NOT NULL,
    username character varying(45) NOT NULL,
    ord integer
);


ALTER TABLE public.money_flow_priority OWNER TO kbs;

--
-- Name: money_flow_priority_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.money_flow_priority_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.money_flow_priority_id_seq OWNER TO kbs;

--
-- Name: money_flow_priority_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.money_flow_priority_id_seq OWNED BY public.money_flow_priority.id;


--
-- Name: money_flow_type; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.money_flow_type (
    id integer NOT NULL,
    from_money_account_type_id integer,
    to_money_account_type_id integer
);


ALTER TABLE public.money_flow_type OWNER TO kbs;

--
-- Name: money_flow_type_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.money_flow_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.money_flow_type_id_seq OWNER TO kbs;

--
-- Name: money_flow_type_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.money_flow_type_id_seq OWNED BY public.money_flow_type.id;


--
-- Name: money_statement; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.money_statement (
    id integer NOT NULL,
    system_acount_id integer,
    account_id integer NOT NULL,
    transfer_id integer,
    confirmed boolean DEFAULT false NOT NULL
);


ALTER TABLE public.money_statement OWNER TO kbs;

--
-- Name: money_statement_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.money_statement_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.money_statement_id_seq OWNER TO kbs;

--
-- Name: money_statement_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.money_statement_id_seq OWNED BY public.money_statement.id;


--
-- Name: money_transfer_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.money_transfer_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.money_transfer_id_seq OWNER TO kbs;

--
-- Name: money_transfer_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.money_transfer_id_seq OWNED BY public.money_transfer.id;


--
-- Name: multiple_booking; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.multiple_booking (
    id integer NOT NULL,
    event_id integer NOT NULL,
    "all" integer DEFAULT 0 NOT NULL,
    not_cancelled integer DEFAULT 0 NOT NULL,
    trigger_update_counts boolean DEFAULT false NOT NULL
);


ALTER TABLE public.multiple_booking OWNER TO kbs;

--
-- Name: multiple_booking_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.multiple_booking_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.multiple_booking_id_seq OWNER TO kbs;

--
-- Name: multiple_booking_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.multiple_booking_id_seq OWNED BY public.multiple_booking.id;


--
-- Name: operation; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.operation (
    id integer NOT NULL,
    operation_code character varying(1024) NOT NULL,
    i18n_code character varying(64),
    name character varying(64),
    label_id integer,
    backend boolean DEFAULT true NOT NULL,
    frontend boolean DEFAULT false NOT NULL,
    public boolean DEFAULT false NOT NULL,
    read_only boolean DEFAULT false NOT NULL
);


ALTER TABLE public.operation OWNER TO kbs;

--
-- Name: operation_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.operation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.operation_id_seq OWNER TO kbs;

--
-- Name: operation_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.operation_id_seq OWNED BY public.operation.id;


--
-- Name: option; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.option (
    id integer NOT NULL,
    parent_id integer,
    folder boolean DEFAULT false NOT NULL,
    event_id integer,
    site_id integer,
    item_id integer,
    rate_id integer,
    label_id integer,
    name character varying(255),
    first_pass boolean DEFAULT true NOT NULL,
    second_pass boolean DEFAULT false NOT NULL,
    ord integer NOT NULL,
    obligatory boolean DEFAULT false NOT NULL,
    children_radio boolean DEFAULT true NOT NULL,
    frame boolean DEFAULT false NOT NULL,
    item_family_id integer,
    split_whole_partial boolean DEFAULT false NOT NULL,
    layout character varying(64),
    top_label_id integer,
    children_dynamic boolean DEFAULT false NOT NULL,
    allocate boolean DEFAULT false NOT NULL,
    attendance_option_id integer,
    sharing_item_id integer,
    prompt_label_id integer,
    children_prompt_label_id integer,
    first_day date,
    last_day date,
    first_excluded_day date,
    last_excluded_day date,
    hide_days boolean DEFAULT false NOT NULL,
    male_allowed boolean DEFAULT true NOT NULL,
    female_allowed boolean DEFAULT true NOT NULL,
    lay_allowed boolean DEFAULT true NOT NULL,
    ordained_allowed boolean DEFAULT true NOT NULL,
    adult_allowed boolean DEFAULT true NOT NULL,
    child_allowed boolean DEFAULT true NOT NULL,
    min_age smallint,
    max_age smallint,
    bottom_label_id integer,
    online boolean DEFAULT true NOT NULL,
    dev boolean DEFAULT false NOT NULL,
    hide_per_person boolean DEFAULT false NOT NULL,
    per_day_availability boolean DEFAULT false NOT NULL,
    attendance_document boolean DEFAULT false NOT NULL,
    force_soldout boolean DEFAULT false NOT NULL,
    obligatory_agreement boolean DEFAULT false NOT NULL,
    radio_traversal boolean DEFAULT false NOT NULL,
    min_day integer,
    max_day integer,
    partial_enabled boolean DEFAULT false NOT NULL,
    hide boolean DEFAULT false NOT NULL,
    price_custom integer,
    time_range character varying(128),
    date_time_range character varying(1024),
    quantity_max integer,
    quantity_label_id integer,
    floating boolean DEFAULT false NOT NULL,
    popup_label_id integer,
    comment_label_id integer,
    template boolean DEFAULT false NOT NULL,
    footer_label_id integer,
    arrival_site_id integer,
    age_error_label_id integer,
    document_line_comment character varying(1024),
    visible_condition character varying(1024),
    attendance_dates_shift integer,
    cart_group_site_id integer,
    cart_group_item_id integer,
    cart_group_label_id integer,
    cart_group_family_id integer,
    selected boolean DEFAULT false NOT NULL,
    online_on timestamp(0) without time zone,
    online_off timestamp(0) without time zone,
    force_soldout_on timestamp(0) without time zone,
    force_soldout_off timestamp(0) without time zone,
    first_pass_on timestamp(0) without time zone,
    first_pass_off timestamp(0) without time zone,
    second_pass_on timestamp(0) without time zone,
    second_pass_off timestamp(0) without time zone
);


ALTER TABLE public.option OWNER TO kbs;

--
-- Name: COLUMN option.cart_group_site_id; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.option.cart_group_site_id IS 'Id of site in which to show the option in the cart and summary';


--
-- Name: COLUMN option.cart_group_item_id; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.option.cart_group_item_id IS 'Id of item in which to show the option in the cart and summary';


--
-- Name: COLUMN option.cart_group_label_id; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.option.cart_group_label_id IS 'Id of label in which to show the option in the cart and summary. Overrides cart_group_item_id. Used when no item correspond.';


--
-- Name: COLUMN option.cart_group_family_id; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.option.cart_group_family_id IS 'Id of item_family in which to show the option in the cart and summary. Overrides item''s family.';


--
-- Name: COLUMN option.selected; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.option.selected IS '(KMC front-end) Default state (selected or not) for root options';


--
-- Name: COLUMN option.online_on; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.option.online_on IS 'When to set automatically online to true';


--
-- Name: COLUMN option.online_off; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.option.online_off IS 'When to set automatically online to false';


--
-- Name: COLUMN option.force_soldout_on; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.option.force_soldout_on IS 'When to set automatically force_soldout to true';


--
-- Name: COLUMN option.force_soldout_off; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.option.force_soldout_off IS 'When to set automatically force_soldout to false';


--
-- Name: COLUMN option.first_pass_on; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.option.first_pass_on IS 'When to set automatically first_pass to true';


--
-- Name: COLUMN option.first_pass_off; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.option.first_pass_off IS 'When to set automatically first_pass to false';


--
-- Name: COLUMN option.second_pass_on; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.option.second_pass_on IS 'When to set automatically second_pass to true';


--
-- Name: COLUMN option.second_pass_off; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.option.second_pass_off IS 'When to set automatically second_pass to false';


--
-- Name: option_condition; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.option_condition (
    id integer NOT NULL,
    option_id integer NOT NULL,
    if_option_id integer,
    parent_id integer,
    is_group boolean DEFAULT false NOT NULL,
    is_group_or boolean DEFAULT false NOT NULL,
    is_not boolean DEFAULT false NOT NULL,
    first_day date,
    last_day date,
    any_day boolean DEFAULT false NOT NULL,
    if_site_id integer,
    if_main_site boolean,
    if_not_main_site boolean,
    if_item_family_id integer
);


ALTER TABLE public.option_condition OWNER TO kbs;

--
-- Name: option_condition_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.option_condition_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.option_condition_id_seq OWNER TO kbs;

--
-- Name: option_condition_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.option_condition_id_seq OWNED BY public.option_condition.id;


--
-- Name: option_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.option_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.option_id_seq OWNER TO kbs;

--
-- Name: option_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.option_id_seq OWNED BY public.option.id;


--
-- Name: organization; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.organization (
    id integer NOT NULL,
    type_id integer NOT NULL,
    country_id integer,
    name character varying(255) NOT NULL,
    closed boolean DEFAULT false NOT NULL,
    domain_name character varying(45),
    ga_tracking_id character varying(20),
    teachings_day_ticket_item_id integer,
    timezone character varying(40),
    terms_url_en character varying(255),
    terms_url_es character varying(255),
    terms_url_fr character varying(255),
    terms_url_de character varying(255),
    terms_url_pt character varying(255)
);


ALTER TABLE public.organization OWNER TO kbs;

--
-- Name: COLUMN organization.timezone; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.organization.timezone IS 'IANA time zone id to use as a default for new events of this organization';


--
-- Name: COLUMN organization.terms_url_en; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.organization.terms_url_en IS 'Default english T&Cs url and jquery selector for events of this organization';


--
-- Name: COLUMN organization.terms_url_es; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.organization.terms_url_es IS 'Default spanish T&Cs url and jquery selector for events of this organization';


--
-- Name: COLUMN organization.terms_url_fr; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.organization.terms_url_fr IS 'Default french T&Cs url and jquery selector for events of this organization';


--
-- Name: COLUMN organization.terms_url_de; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.organization.terms_url_de IS 'Default german T&Cs url and jquery selector for events of this organization';


--
-- Name: COLUMN organization.terms_url_pt; Type: COMMENT; Schema: public; Owner: kbs
--

COMMENT ON COLUMN public.organization.terms_url_pt IS 'Default portuguese T&Cs url and jquery selector for events of this organization';


--
-- Name: organization_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.organization_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.organization_id_seq OWNER TO kbs;

--
-- Name: organization_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.organization_id_seq OWNED BY public.organization.id;


--
-- Name: organization_type; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.organization_type (
    id integer NOT NULL,
    name character varying(64) NOT NULL,
    label_id integer,
    code character varying(10)
);


ALTER TABLE public.organization_type OWNER TO kbs;

--
-- Name: organization_type_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.organization_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.organization_type_id_seq OWNER TO kbs;

--
-- Name: organization_type_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.organization_type_id_seq OWNED BY public.organization_type.id;


--
-- Name: package; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.package (
    id integer NOT NULL,
    name character varying(64) NOT NULL
);


ALTER TABLE public.package OWNER TO kbs;

--
-- Name: package_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.package_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.package_id_seq OWNER TO kbs;

--
-- Name: package_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.package_id_seq OWNED BY public.package.id;


--
-- Name: package_item; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.package_item (
    id integer NOT NULL,
    package_id integer NOT NULL,
    item_id integer NOT NULL
);


ALTER TABLE public.package_item OWNER TO kbs;

--
-- Name: package_item_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.package_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.package_item_id_seq OWNER TO kbs;

--
-- Name: package_item_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.package_item_id_seq OWNED BY public.package_item.id;


--
-- Name: person; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.person (
    id integer NOT NULL,
    organization_id integer,
    branch_id integer,
    third_party_id integer,
    language_id integer,
    frontend_account_id integer,
    country_name character varying(64),
    country_geonameid integer,
    country_id integer,
    post_code character varying(16),
    city_name character varying(64),
    city_geonameid integer,
    city_latitude double precision,
    city_longitude double precision,
    city_timezone character varying(40),
    street character varying(128),
    latitude double precision,
    longitude double precision,
    name character varying(91),
    first_name character varying(45),
    last_name character varying(45),
    lay_name character varying(91),
    abc_names character varying(91),
    male boolean,
    ordained boolean,
    birthdate date,
    carer1_name character varying(91),
    carer1_id integer,
    carer2_name character varying(91),
    carer2_id integer,
    email character varying(127),
    phone character varying(45),
    admin1_name character varying(255),
    admin2_name character varying(255),
    country_code character(2),
    removed boolean DEFAULT false NOT NULL,
    never_booked boolean DEFAULT true NOT NULL,
    organization_name character varying(255),
    nationality character varying(64),
    passport character varying(64),
    emailing_list boolean DEFAULT true NOT NULL
);


ALTER TABLE public.person OWNER TO kbs;

--
-- Name: person_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.person_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.person_id_seq OWNER TO kbs;

--
-- Name: person_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.person_id_seq OWNED BY public.person.id;


--
-- Name: rate_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.rate_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.rate_id_seq OWNER TO kbs;

--
-- Name: rate_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.rate_id_seq OWNED BY public.rate.id;


--
-- Name: recipient; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.recipient (
    id integer NOT NULL,
    mail_id integer NOT NULL,
    name character varying(91),
    email character varying(127) NOT NULL,
    person_id integer,
    "to" boolean DEFAULT true NOT NULL,
    cc boolean DEFAULT false NOT NULL,
    bcc boolean DEFAULT false NOT NULL,
    ok boolean DEFAULT false NOT NULL,
    permanent_error boolean DEFAULT false NOT NULL,
    temporary_error boolean DEFAULT false NOT NULL
);


ALTER TABLE public.recipient OWNER TO kbs;

--
-- Name: recipient_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.recipient_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.recipient_id_seq OWNER TO kbs;

--
-- Name: recipient_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.recipient_id_seq OWNED BY public.recipient.id;


--
-- Name: resource; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.resource (
    id integer NOT NULL,
    site_id integer NOT NULL,
    site_item_family_id integer NOT NULL,
    name character varying(64) NOT NULL,
    trigger_duplicate smallint,
    site_item_family_code character varying(10),
    building_id integer
);


ALTER TABLE public.resource OWNER TO kbs;

--
-- Name: resource_configuration; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.resource_configuration (
    id integer NOT NULL,
    resource_id integer NOT NULL,
    item_id integer NOT NULL,
    start_date date,
    end_date date,
    max integer,
    max_paper integer,
    max_private integer,
    comment character varying(1024),
    online boolean DEFAULT true NOT NULL,
    name character varying(64)
);


ALTER TABLE public.resource_configuration OWNER TO kbs;

--
-- Name: resource_configuration_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.resource_configuration_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.resource_configuration_id_seq OWNER TO kbs;

--
-- Name: resource_configuration_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.resource_configuration_id_seq OWNED BY public.resource_configuration.id;


--
-- Name: resource_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.resource_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.resource_id_seq OWNER TO kbs;

--
-- Name: resource_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.resource_id_seq OWNED BY public.resource.id;


--
-- Name: role; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.role (
    id integer NOT NULL,
    name character varying(64) NOT NULL,
    label_id integer,
    system boolean DEFAULT false NOT NULL,
    translator boolean DEFAULT false NOT NULL
);


ALTER TABLE public.role OWNER TO kbs;

--
-- Name: role_attribution; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.role_attribution (
    id integer NOT NULL,
    person_id integer NOT NULL,
    role_id integer NOT NULL,
    read_only boolean DEFAULT false NOT NULL,
    from_language_id integer,
    to_language_id integer
);


ALTER TABLE public.role_attribution OWNER TO kbs;

--
-- Name: role_attribution_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.role_attribution_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.role_attribution_id_seq OWNER TO kbs;

--
-- Name: role_attribution_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.role_attribution_id_seq OWNED BY public.role_attribution.id;


--
-- Name: role_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.role_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.role_id_seq OWNER TO kbs;

--
-- Name: role_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.role_id_seq OWNED BY public.role.id;


--
-- Name: sector; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.sector (
    id integer NOT NULL,
    name character varying(64) NOT NULL
);


ALTER TABLE public.sector OWNER TO kbs;

--
-- Name: sector_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.sector_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.sector_id_seq OWNER TO kbs;

--
-- Name: sector_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.sector_id_seq OWNED BY public.sector.id;


--
-- Name: session_agent; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.session_agent (
    id integer NOT NULL,
    previous_id integer,
    next_id integer,
    agent_string character varying(1024) NOT NULL,
    start timestamp without time zone NOT NULL,
    "end" timestamp without time zone
);


ALTER TABLE public.session_agent OWNER TO kbs;

--
-- Name: session_agent_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.session_agent_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.session_agent_id_seq OWNER TO kbs;

--
-- Name: session_agent_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.session_agent_id_seq OWNED BY public.session_agent.id;


--
-- Name: session_application; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.session_application (
    id integer NOT NULL,
    agent_id integer NOT NULL,
    previous_id integer,
    next_id integer,
    start timestamp without time zone DEFAULT now() NOT NULL,
    "end" timestamp without time zone,
    name character varying(64) NOT NULL,
    version character varying(64) NOT NULL,
    build_tool character varying(64) NOT NULL,
    build_number_string character varying(64) NOT NULL,
    build_number integer,
    build_timestamp_string character varying(64) NOT NULL,
    build_timestamp timestamp without time zone
);


ALTER TABLE public.session_application OWNER TO kbs;

--
-- Name: session_application_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.session_application_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.session_application_id_seq OWNER TO kbs;

--
-- Name: session_application_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.session_application_id_seq OWNED BY public.session_application.id;


--
-- Name: session_connection; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.session_connection (
    id integer NOT NULL,
    process_id integer NOT NULL,
    previous_id integer,
    next_id integer,
    start timestamp without time zone DEFAULT now() NOT NULL,
    "end" timestamp without time zone
);


ALTER TABLE public.session_connection OWNER TO kbs;

--
-- Name: session_connection_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.session_connection_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.session_connection_id_seq OWNER TO kbs;

--
-- Name: session_connection_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.session_connection_id_seq OWNED BY public.session_connection.id;


--
-- Name: session_process; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.session_process (
    id integer NOT NULL,
    application_id integer NOT NULL,
    previous_id integer,
    next_id integer,
    start timestamp without time zone DEFAULT now() NOT NULL,
    "end" timestamp without time zone
);


ALTER TABLE public.session_process OWNER TO kbs;

--
-- Name: session_process_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.session_process_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.session_process_id_seq OWNER TO kbs;

--
-- Name: session_process_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.session_process_id_seq OWNED BY public.session_process.id;


--
-- Name: session_user; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public."session_user" (
    id integer NOT NULL,
    process_id integer NOT NULL,
    user_id integer,
    previous_id integer,
    next_id integer,
    start timestamp without time zone NOT NULL,
    "end" timestamp without time zone
);


ALTER TABLE public."session_user" OWNER TO kbs;

--
-- Name: session_user_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.session_user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.session_user_id_seq OWNER TO kbs;

--
-- Name: session_user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.session_user_id_seq OWNED BY public."session_user".id;


--
-- Name: site; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.site (
    id integer NOT NULL,
    organization_id integer,
    event_id integer,
    name character varying(64) NOT NULL,
    label_id integer,
    item_family_id integer NOT NULL,
    image_url character varying(255),
    top_label_id integer,
    ord integer,
    group_name character varying(64),
    address character varying(1024),
    asks_for_passport boolean DEFAULT false NOT NULL,
    online boolean DEFAULT true NOT NULL,
    hide_dates boolean DEFAULT false NOT NULL,
    force_soldout boolean DEFAULT false NOT NULL,
    main boolean DEFAULT false NOT NULL
);


ALTER TABLE public.site OWNER TO kbs;

--
-- Name: site_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.site_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.site_id_seq OWNER TO kbs;

--
-- Name: site_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.site_id_seq OWNED BY public.site.id;


--
-- Name: site_item_family; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.site_item_family (
    id integer NOT NULL,
    site_id integer NOT NULL,
    item_family_id integer NOT NULL,
    auto_release boolean DEFAULT true NOT NULL
);


ALTER TABLE public.site_item_family OWNER TO kbs;

--
-- Name: site_item_family_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.site_item_family_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.site_item_family_id_seq OWNER TO kbs;

--
-- Name: site_item_family_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.site_item_family_id_seq OWNED BY public.site_item_family.id;


--
-- Name: smtp_account; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.smtp_account (
    id integer NOT NULL,
    organization_id integer NOT NULL,
    host character varying(45) NOT NULL,
    port integer DEFAULT 465 NOT NULL,
    username character varying(45),
    password character varying(45),
    ssl boolean DEFAULT true NOT NULL,
    quota_id integer
);


ALTER TABLE public.smtp_account OWNER TO kbs;

--
-- Name: smtp_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.smtp_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.smtp_id_seq OWNER TO kbs;

--
-- Name: smtp_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.smtp_id_seq OWNED BY public.smtp_account.id;


--
-- Name: smtp_quota; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.smtp_quota (
    id integer NOT NULL,
    name character varying(64) NOT NULL
);


ALTER TABLE public.smtp_quota OWNER TO kbs;

--
-- Name: smtp_quota_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.smtp_quota_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.smtp_quota_id_seq OWNER TO kbs;

--
-- Name: smtp_quota_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.smtp_quota_id_seq OWNED BY public.smtp_quota.id;


--
-- Name: stock; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.stock (
    id integer NOT NULL,
    item_id integer NOT NULL,
    warehouse_id integer,
    third_party_id integer,
    start_date date,
    end_date date,
    initial_quantity integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.stock OWNER TO kbs;

--
-- Name: stock_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.stock_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.stock_id_seq OWNER TO kbs;

--
-- Name: stock_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.stock_id_seq OWNED BY public.stock.id;


--
-- Name: stock_transfer; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.stock_transfer (
    id integer NOT NULL,
    from_id integer NOT NULL,
    to_id integer NOT NULL,
    datetime timestamp without time zone NOT NULL,
    quantity integer NOT NULL,
    new_from_inventory integer,
    new_to_inventory integer,
    document_line_id integer,
    purchase boolean DEFAULT false NOT NULL,
    remaining_quantity integer,
    provision_transfer_id integer
);


ALTER TABLE public.stock_transfer OWNER TO kbs;

--
-- Name: stock_transfer_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.stock_transfer_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.stock_transfer_id_seq OWNER TO kbs;

--
-- Name: stock_transfer_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.stock_transfer_id_seq OWNED BY public.stock_transfer.id;


--
-- Name: subscription; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.subscription (
    id integer NOT NULL,
    person_id integer NOT NULL,
    organization_id integer NOT NULL,
    sector_id integer,
    activity_id integer,
    newsletter boolean DEFAULT false NOT NULL,
    start_date date NOT NULL,
    end_date date
);


ALTER TABLE public.subscription OWNER TO kbs;

--
-- Name: subscription_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.subscription_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.subscription_id_seq OWNER TO kbs;

--
-- Name: subscription_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.subscription_id_seq OWNED BY public.subscription.id;


--
-- Name: sys_log; Type: TABLE; Schema: public; Owner: kbs
--

CREATE UNLOGGED TABLE public.sys_log (
    id integer NOT NULL,
    table_name character varying(50) NOT NULL,
    insert boolean DEFAULT false NOT NULL,
    update boolean DEFAULT false NOT NULL,
    delete boolean DEFAULT false NOT NULL,
    oid integer,
    column_name character varying(50)
);


ALTER TABLE public.sys_log OWNER TO kbs;

--
-- Name: sys_log_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.sys_log_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.sys_log_id_seq OWNER TO kbs;

--
-- Name: sys_log_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.sys_log_id_seq OWNED BY public.sys_log.id;


--
-- Name: sys_sync; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.sys_sync (
    username character varying(45) NOT NULL
);


ALTER TABLE public.sys_sync OWNER TO kbs;

--
-- Name: teacher; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.teacher (
    id integer NOT NULL,
    name character varying(64) NOT NULL,
    organization_id integer NOT NULL,
    person_id integer,
    image_id integer,
    label_id integer,
    who_label_id integer
);


ALTER TABLE public.teacher OWNER TO kbs;

--
-- Name: teacher_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.teacher_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.teacher_id_seq OWNER TO kbs;

--
-- Name: teacher_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.teacher_id_seq OWNED BY public.teacher.id;


--
-- Name: third_party; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.third_party (
    id integer NOT NULL,
    name character varying(64) NOT NULL,
    customer boolean DEFAULT false NOT NULL,
    supplier boolean DEFAULT true NOT NULL
);


ALTER TABLE public.third_party OWNER TO kbs;

--
-- Name: third_party_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.third_party_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.third_party_id_seq OWNER TO kbs;

--
-- Name: third_party_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.third_party_id_seq OWNED BY public.third_party.id;


--
-- Name: timezone; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.timezone (
    id integer NOT NULL,
    name character varying(64) NOT NULL,
    gmt_offset numeric NOT NULL,
    dst_offset numeric NOT NULL,
    raw_offset numeric NOT NULL,
    country_code character(2)
);


ALTER TABLE public.timezone OWNER TO kbs;

--
-- Name: timezone_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.timezone_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.timezone_id_seq OWNER TO kbs;

--
-- Name: timezone_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.timezone_id_seq OWNED BY public.timezone.id;


--
-- Name: vat; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.vat (
    id integer NOT NULL,
    account_model_id integer NOT NULL,
    rate smallint NOT NULL
);


ALTER TABLE public.vat OWNER TO kbs;

--
-- Name: vat_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.vat_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.vat_id_seq OWNER TO kbs;

--
-- Name: vat_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.vat_id_seq OWNED BY public.vat.id;


--
-- Name: visibility; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.visibility (
    id integer NOT NULL
);


ALTER TABLE public.visibility OWNER TO kbs;

--
-- Name: visibility_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.visibility_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.visibility_id_seq OWNER TO kbs;

--
-- Name: visibility_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.visibility_id_seq OWNED BY public.visibility.id;


--
-- Name: warehouse; Type: TABLE; Schema: public; Owner: kbs
--

CREATE TABLE public.warehouse (
    id integer NOT NULL,
    organization_id integer,
    name character varying(64) NOT NULL
);


ALTER TABLE public.warehouse OWNER TO kbs;

--
-- Name: warehouse_id_seq; Type: SEQUENCE; Schema: public; Owner: kbs
--

CREATE SEQUENCE public.warehouse_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.warehouse_id_seq OWNER TO kbs;

--
-- Name: warehouse_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: kbs
--

ALTER SEQUENCE public.warehouse_id_seq OWNED BY public.warehouse.id;


--
-- Name: accounting_account id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.accounting_account ALTER COLUMN id SET DEFAULT nextval('public.accounting_account_id_seq'::regclass);


--
-- Name: accounting_account_type id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.accounting_account_type ALTER COLUMN id SET DEFAULT nextval('public.accounting_account_type_id_seq'::regclass);


--
-- Name: accounting_model id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.accounting_model ALTER COLUMN id SET DEFAULT nextval('public.accounting_model_id_seq'::regclass);


--
-- Name: activity id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.activity ALTER COLUMN id SET DEFAULT nextval('public.activity_id_seq'::regclass);


--
-- Name: activity_state id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.activity_state ALTER COLUMN id SET DEFAULT nextval('public.activity_state_id_seq'::regclass);


--
-- Name: allocation_rule id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.allocation_rule ALTER COLUMN id SET DEFAULT nextval('public.allocation_rule_id_seq'::regclass);


--
-- Name: allocation_rule ord; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.allocation_rule ALTER COLUMN ord SET DEFAULT currval('public.allocation_rule_id_seq'::regclass);


--
-- Name: attendance id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.attendance ALTER COLUMN id SET DEFAULT nextval('public.attendance_id_seq'::regclass);


--
-- Name: authorization_assignment id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.authorization_assignment ALTER COLUMN id SET DEFAULT nextval('public.authorization_assignment_id_seq'::regclass);


--
-- Name: authorization_management id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.authorization_management ALTER COLUMN id SET DEFAULT nextval('public.authorization_management_id_seq'::regclass);


--
-- Name: authorization_role id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.authorization_role ALTER COLUMN id SET DEFAULT nextval('public.authorization_role_id_seq'::regclass);


--
-- Name: authorization_rule id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.authorization_rule ALTER COLUMN id SET DEFAULT nextval('public.authorization_rule_id_seq'::regclass);


--
-- Name: authorization_scope id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.authorization_scope ALTER COLUMN id SET DEFAULT nextval('public.authorization_scope_id_seq'::regclass);


--
-- Name: bank id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.bank ALTER COLUMN id SET DEFAULT nextval('public.bank_id_seq'::regclass);


--
-- Name: bank_system id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.bank_system ALTER COLUMN id SET DEFAULT nextval('public.bank_system_id_seq'::regclass);


--
-- Name: bank_system_account id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.bank_system_account ALTER COLUMN id SET DEFAULT nextval('public.bank_system_account_id_seq'::regclass);


--
-- Name: booking_form_layout id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.booking_form_layout ALTER COLUMN id SET DEFAULT nextval('public.booking_form_layout_id_seq'::regclass);


--
-- Name: bracket_pattern id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.bracket_pattern ALTER COLUMN id SET DEFAULT nextval('public.bracket_pattern_id_seq'::regclass);


--
-- Name: bracket_pattern ord; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.bracket_pattern ALTER COLUMN ord SET DEFAULT currval('public.bracket_pattern_id_seq'::regclass);


--
-- Name: buddha id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.buddha ALTER COLUMN id SET DEFAULT nextval('public.buddha_id_seq'::regclass);


--
-- Name: budget id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.budget ALTER COLUMN id SET DEFAULT nextval('public.budget_id_seq'::regclass);


--
-- Name: budget_line id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.budget_line ALTER COLUMN id SET DEFAULT nextval('public.budget_line_id_seq'::regclass);


--
-- Name: budget_transfer id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.budget_transfer ALTER COLUMN id SET DEFAULT nextval('public.budget_transfer_id_seq'::regclass);


--
-- Name: building id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.building ALTER COLUMN id SET DEFAULT nextval('public.building_id_seq'::regclass);


--
-- Name: cart id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.cart ALTER COLUMN id SET DEFAULT nextval('public.cart_id_seq'::regclass);


--
-- Name: cart_message id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.cart_message ALTER COLUMN id SET DEFAULT nextval('public.cart_message_id_seq'::regclass);


--
-- Name: cart_message_condition id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.cart_message_condition ALTER COLUMN id SET DEFAULT nextval('public.cart_message_condition_id_seq'::regclass);


--
-- Name: continent id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.continent ALTER COLUMN id SET DEFAULT nextval('public.continent_id_seq'::regclass);


--
-- Name: country id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.country ALTER COLUMN id SET DEFAULT nextval('public.country_id_seq'::regclass);


--
-- Name: css id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.css ALTER COLUMN id SET DEFAULT nextval('public.css_id_seq'::regclass);


--
-- Name: currency id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.currency ALTER COLUMN id SET DEFAULT nextval('public.currency_id_seq'::regclass);


--
-- Name: currency_support id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.currency_support ALTER COLUMN id SET DEFAULT nextval('public.currency_support_id_seq'::regclass);


--
-- Name: date_info id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.date_info ALTER COLUMN id SET DEFAULT nextval('public.date_info_id_seq'::regclass);


--
-- Name: document id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.document ALTER COLUMN id SET DEFAULT nextval('public.document_id_seq'::regclass);


--
-- Name: document_line id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.document_line ALTER COLUMN id SET DEFAULT nextval('public.document_line_id_seq'::regclass);


--
-- Name: domain id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.domain ALTER COLUMN id SET DEFAULT nextval('public.domain_id_seq'::regclass);


--
-- Name: driver id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.driver ALTER COLUMN id SET DEFAULT nextval('public.driver_id_seq'::regclass);


--
-- Name: enqueued_request id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.enqueued_request ALTER COLUMN id SET DEFAULT nextval('public.enqueued_request_id_seq'::regclass);


--
-- Name: event id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.event ALTER COLUMN id SET DEFAULT nextval('public.event_id_seq'::regclass);


--
-- Name: event_type id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.event_type ALTER COLUMN id SET DEFAULT nextval('public.event_type_id_seq'::regclass);


--
-- Name: filter id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.filter ALTER COLUMN id SET DEFAULT nextval('public.filter_id_seq'::regclass);


--
-- Name: frontend_account id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.frontend_account ALTER COLUMN id SET DEFAULT nextval('public.frontend_account_id_seq'::regclass);


--
-- Name: gateway_company id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.gateway_company ALTER COLUMN id SET DEFAULT nextval('public.gateway_company_id_seq'::regclass);


--
-- Name: gateway_parameter id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.gateway_parameter ALTER COLUMN id SET DEFAULT nextval('public.gateway_parameter_id_seq'::regclass);


--
-- Name: history id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.history ALTER COLUMN id SET DEFAULT nextval('public.history_id_seq'::regclass);


--
-- Name: image id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.image ALTER COLUMN id SET DEFAULT nextval('public.image_id_seq'::regclass);


--
-- Name: item id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.item ALTER COLUMN id SET DEFAULT nextval('public.item_id_seq'::regclass);


--
-- Name: item ord; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.item ALTER COLUMN ord SET DEFAULT currval('public.item_id_seq'::regclass);


--
-- Name: item_family id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.item_family ALTER COLUMN id SET DEFAULT nextval('public.item_family_id_seq'::regclass);


--
-- Name: item_family ord; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.item_family ALTER COLUMN ord SET DEFAULT currval('public.item_family_id_seq'::regclass);


--
-- Name: label id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.label ALTER COLUMN id SET DEFAULT nextval('public.label_id_seq'::regclass);


--
-- Name: language id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.language ALTER COLUMN id SET DEFAULT nextval('public.language_id_seq'::regclass);


--
-- Name: letter id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.letter ALTER COLUMN id SET DEFAULT nextval('public.letter_id_seq'::regclass);


--
-- Name: letter_type id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.letter_type ALTER COLUMN id SET DEFAULT nextval('public.letter_type_id_seq'::regclass);


--
-- Name: letter_type ord; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.letter_type ALTER COLUMN ord SET DEFAULT currval('public.letter_type_id_seq'::regclass);


--
-- Name: lt_test_event id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.lt_test_event ALTER COLUMN id SET DEFAULT nextval('public.lt_test_event_id_seq'::regclass);


--
-- Name: lt_test_set id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.lt_test_set ALTER COLUMN id SET DEFAULT nextval('public.lt_test_set_id_seq'::regclass);


--
-- Name: mail id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.mail ALTER COLUMN id SET DEFAULT nextval('public.mail_id_seq'::regclass);


--
-- Name: mail_account id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.mail_account ALTER COLUMN id SET DEFAULT nextval('public.mail_account_id_seq'::regclass);


--
-- Name: method id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.method ALTER COLUMN id SET DEFAULT nextval('public.method_id_seq'::regclass);


--
-- Name: method_support id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.method_support ALTER COLUMN id SET DEFAULT nextval('public.method_support_id_seq'::regclass);


--
-- Name: metrics id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.metrics ALTER COLUMN id SET DEFAULT nextval('public.metrics_id_seq'::regclass);


--
-- Name: money_account id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.money_account ALTER COLUMN id SET DEFAULT nextval('public.money_account_id_seq'::regclass);


--
-- Name: money_account_type id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.money_account_type ALTER COLUMN id SET DEFAULT nextval('public.money_account_type_id_seq'::regclass);


--
-- Name: money_flow id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.money_flow ALTER COLUMN id SET DEFAULT nextval('public.money_flow_id_seq'::regclass);


--
-- Name: money_flow_priority id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.money_flow_priority ALTER COLUMN id SET DEFAULT nextval('public.money_flow_priority_id_seq'::regclass);


--
-- Name: money_flow_type id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.money_flow_type ALTER COLUMN id SET DEFAULT nextval('public.money_flow_type_id_seq'::regclass);


--
-- Name: money_statement id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.money_statement ALTER COLUMN id SET DEFAULT nextval('public.money_statement_id_seq'::regclass);


--
-- Name: money_transfer id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.money_transfer ALTER COLUMN id SET DEFAULT nextval('public.money_transfer_id_seq'::regclass);


--
-- Name: multiple_booking id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.multiple_booking ALTER COLUMN id SET DEFAULT nextval('public.multiple_booking_id_seq'::regclass);


--
-- Name: operation id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.operation ALTER COLUMN id SET DEFAULT nextval('public.operation_id_seq'::regclass);


--
-- Name: option id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option ALTER COLUMN id SET DEFAULT nextval('public.option_id_seq'::regclass);


--
-- Name: option ord; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option ALTER COLUMN ord SET DEFAULT currval('public.option_id_seq'::regclass);


--
-- Name: option_condition id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition ALTER COLUMN id SET DEFAULT nextval('public.option_condition_id_seq'::regclass);


--
-- Name: organization id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.organization ALTER COLUMN id SET DEFAULT nextval('public.organization_id_seq'::regclass);


--
-- Name: organization_type id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.organization_type ALTER COLUMN id SET DEFAULT nextval('public.organization_type_id_seq'::regclass);


--
-- Name: package id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.package ALTER COLUMN id SET DEFAULT nextval('public.package_id_seq'::regclass);


--
-- Name: package_item id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.package_item ALTER COLUMN id SET DEFAULT nextval('public.package_item_id_seq'::regclass);


--
-- Name: person id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.person ALTER COLUMN id SET DEFAULT nextval('public.person_id_seq'::regclass);


--
-- Name: rate id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate ALTER COLUMN id SET DEFAULT nextval('public.rate_id_seq'::regclass);


--
-- Name: recipient id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.recipient ALTER COLUMN id SET DEFAULT nextval('public.recipient_id_seq'::regclass);


--
-- Name: resource id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.resource ALTER COLUMN id SET DEFAULT nextval('public.resource_id_seq'::regclass);


--
-- Name: resource_configuration id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.resource_configuration ALTER COLUMN id SET DEFAULT nextval('public.resource_configuration_id_seq'::regclass);


--
-- Name: role id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.role ALTER COLUMN id SET DEFAULT nextval('public.role_id_seq'::regclass);


--
-- Name: role_attribution id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.role_attribution ALTER COLUMN id SET DEFAULT nextval('public.role_attribution_id_seq'::regclass);


--
-- Name: sector id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.sector ALTER COLUMN id SET DEFAULT nextval('public.sector_id_seq'::regclass);


--
-- Name: session_agent id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.session_agent ALTER COLUMN id SET DEFAULT nextval('public.session_agent_id_seq'::regclass);


--
-- Name: session_application id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.session_application ALTER COLUMN id SET DEFAULT nextval('public.session_application_id_seq'::regclass);


--
-- Name: session_connection id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.session_connection ALTER COLUMN id SET DEFAULT nextval('public.session_connection_id_seq'::regclass);


--
-- Name: session_process id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.session_process ALTER COLUMN id SET DEFAULT nextval('public.session_process_id_seq'::regclass);


--
-- Name: session_user id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public."session_user" ALTER COLUMN id SET DEFAULT nextval('public.session_user_id_seq'::regclass);


--
-- Name: site id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.site ALTER COLUMN id SET DEFAULT nextval('public.site_id_seq'::regclass);


--
-- Name: site_item_family id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.site_item_family ALTER COLUMN id SET DEFAULT nextval('public.site_item_family_id_seq'::regclass);


--
-- Name: smtp_account id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.smtp_account ALTER COLUMN id SET DEFAULT nextval('public.smtp_id_seq'::regclass);


--
-- Name: smtp_quota id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.smtp_quota ALTER COLUMN id SET DEFAULT nextval('public.smtp_quota_id_seq'::regclass);


--
-- Name: stock id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.stock ALTER COLUMN id SET DEFAULT nextval('public.stock_id_seq'::regclass);


--
-- Name: stock_transfer id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.stock_transfer ALTER COLUMN id SET DEFAULT nextval('public.stock_transfer_id_seq'::regclass);


--
-- Name: subscription id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.subscription ALTER COLUMN id SET DEFAULT nextval('public.subscription_id_seq'::regclass);


--
-- Name: sys_log id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.sys_log ALTER COLUMN id SET DEFAULT nextval('public.sys_log_id_seq'::regclass);


--
-- Name: teacher id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.teacher ALTER COLUMN id SET DEFAULT nextval('public.teacher_id_seq'::regclass);


--
-- Name: third_party id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.third_party ALTER COLUMN id SET DEFAULT nextval('public.third_party_id_seq'::regclass);


--
-- Name: timezone id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.timezone ALTER COLUMN id SET DEFAULT nextval('public.timezone_id_seq'::regclass);


--
-- Name: vat id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.vat ALTER COLUMN id SET DEFAULT nextval('public.vat_id_seq'::regclass);


--
-- Name: visibility id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.visibility ALTER COLUMN id SET DEFAULT nextval('public.visibility_id_seq'::regclass);


--
-- Name: warehouse id; Type: DEFAULT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.warehouse ALTER COLUMN id SET DEFAULT nextval('public.warehouse_id_seq'::regclass);


--
-- Name: accounting_account accounting_account_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.accounting_account
    ADD CONSTRAINT accounting_account_pkey PRIMARY KEY (id);


--
-- Name: accounting_account_type accounting_account_type_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.accounting_account_type
    ADD CONSTRAINT accounting_account_type_pkey PRIMARY KEY (id);


--
-- Name: accounting_model accounting_model_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.accounting_model
    ADD CONSTRAINT accounting_model_pkey PRIMARY KEY (id);


--
-- Name: activity activity_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.activity
    ADD CONSTRAINT activity_pkey PRIMARY KEY (id);


--
-- Name: activity_state activity_state_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.activity_state
    ADD CONSTRAINT activity_state_pkey PRIMARY KEY (id);


--
-- Name: allocation_rule allocation_rule_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.allocation_rule
    ADD CONSTRAINT allocation_rule_pkey PRIMARY KEY (id);


--
-- Name: attendance attendance_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.attendance
    ADD CONSTRAINT attendance_pkey PRIMARY KEY (id);


--
-- Name: authorization_assignment authorization_assignment_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.authorization_assignment
    ADD CONSTRAINT authorization_assignment_pkey PRIMARY KEY (id);


--
-- Name: authorization_management authorization_management_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.authorization_management
    ADD CONSTRAINT authorization_management_pkey PRIMARY KEY (id);


--
-- Name: authorization_role authorization_role_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.authorization_role
    ADD CONSTRAINT authorization_role_pkey PRIMARY KEY (id);


--
-- Name: authorization_rule authorization_rule_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.authorization_rule
    ADD CONSTRAINT authorization_rule_pkey PRIMARY KEY (id);


--
-- Name: authorization_scope authorization_scope_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.authorization_scope
    ADD CONSTRAINT authorization_scope_pkey PRIMARY KEY (id);


--
-- Name: bank bank_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.bank
    ADD CONSTRAINT bank_pkey PRIMARY KEY (id);


--
-- Name: bank_system_account bank_system_account_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.bank_system_account
    ADD CONSTRAINT bank_system_account_pkey PRIMARY KEY (id);


--
-- Name: bank_system bank_system_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.bank_system
    ADD CONSTRAINT bank_system_pkey PRIMARY KEY (id);


--
-- Name: booking_form_layout booking_form_layout_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.booking_form_layout
    ADD CONSTRAINT booking_form_layout_pkey PRIMARY KEY (id);


--
-- Name: bracket_pattern bracket_pattern_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.bracket_pattern
    ADD CONSTRAINT bracket_pattern_pkey PRIMARY KEY (id);


--
-- Name: buddha buddha_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.buddha
    ADD CONSTRAINT buddha_pkey PRIMARY KEY (id);


--
-- Name: budget_line budget_line_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.budget_line
    ADD CONSTRAINT budget_line_pkey PRIMARY KEY (id);


--
-- Name: budget budget_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.budget
    ADD CONSTRAINT budget_pkey PRIMARY KEY (id);


--
-- Name: budget_transfer budget_transfer_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.budget_transfer
    ADD CONSTRAINT budget_transfer_pkey PRIMARY KEY (id);


--
-- Name: building building_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.building
    ADD CONSTRAINT building_pkey PRIMARY KEY (id);


--
-- Name: cart_message_condition cart_message_condition_pk; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.cart_message_condition
    ADD CONSTRAINT cart_message_condition_pk PRIMARY KEY (id);


--
-- Name: cart_message cart_message_pk; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.cart_message
    ADD CONSTRAINT cart_message_pk PRIMARY KEY (id);


--
-- Name: cart cart_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.cart
    ADD CONSTRAINT cart_pkey PRIMARY KEY (id);


--
-- Name: cart cart_uuid_key; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.cart
    ADD CONSTRAINT cart_uuid_key UNIQUE (uuid);


--
-- Name: continent continent_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.continent
    ADD CONSTRAINT continent_pkey PRIMARY KEY (id);


--
-- Name: country country_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.country
    ADD CONSTRAINT country_pkey PRIMARY KEY (id);


--
-- Name: css css_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.css
    ADD CONSTRAINT css_pkey PRIMARY KEY (id);


--
-- Name: currency currency_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.currency
    ADD CONSTRAINT currency_pkey PRIMARY KEY (id);


--
-- Name: currency_support currency_support_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.currency_support
    ADD CONSTRAINT currency_support_pkey PRIMARY KEY (id);


--
-- Name: date_info date_info_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.date_info
    ADD CONSTRAINT date_info_pkey PRIMARY KEY (id);


--
-- Name: document_line document_line_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.document_line
    ADD CONSTRAINT document_line_pkey PRIMARY KEY (id);


--
-- Name: document document_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.document
    ADD CONSTRAINT document_pkey PRIMARY KEY (id);


--
-- Name: domain domain_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.domain
    ADD CONSTRAINT domain_pkey PRIMARY KEY (id);


--
-- Name: driver driver_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.driver
    ADD CONSTRAINT driver_pkey PRIMARY KEY (id);


--
-- Name: enqueued_request enqueued_request_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.enqueued_request
    ADD CONSTRAINT enqueued_request_pkey PRIMARY KEY (id);


--
-- Name: event event_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.event
    ADD CONSTRAINT event_pkey PRIMARY KEY (id);


--
-- Name: event_type event_type_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.event_type
    ADD CONSTRAINT event_type_pkey PRIMARY KEY (id);


--
-- Name: filter filter_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.filter
    ADD CONSTRAINT filter_pkey PRIMARY KEY (id);


--
-- Name: frontend_account frontend_account_corporation_id_username; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.frontend_account
    ADD CONSTRAINT frontend_account_corporation_id_username UNIQUE (corporation_id, username);


--
-- Name: frontend_account frontend_account_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.frontend_account
    ADD CONSTRAINT frontend_account_pkey PRIMARY KEY (id);


--
-- Name: gateway_company gateway_company_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.gateway_company
    ADD CONSTRAINT gateway_company_pkey PRIMARY KEY (id);


--
-- Name: gateway_parameter gateway_parameter_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.gateway_parameter
    ADD CONSTRAINT gateway_parameter_pkey PRIMARY KEY (id);


--
-- Name: history history_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.history
    ADD CONSTRAINT history_pkey PRIMARY KEY (id);


--
-- Name: image image_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.image
    ADD CONSTRAINT image_pkey PRIMARY KEY (id);


--
-- Name: item_family item_family_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.item_family
    ADD CONSTRAINT item_family_pkey PRIMARY KEY (id);


--
-- Name: item item_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.item
    ADD CONSTRAINT item_pkey PRIMARY KEY (id);


--
-- Name: label label_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.label
    ADD CONSTRAINT label_pkey PRIMARY KEY (id);


--
-- Name: language language_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.language
    ADD CONSTRAINT language_pkey PRIMARY KEY (id);


--
-- Name: letter letter_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.letter
    ADD CONSTRAINT letter_pkey PRIMARY KEY (id);


--
-- Name: letter_type letter_type_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.letter_type
    ADD CONSTRAINT letter_type_pkey PRIMARY KEY (id);


--
-- Name: lt_test_event lt_test_event_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.lt_test_event
    ADD CONSTRAINT lt_test_event_pkey PRIMARY KEY (id);


--
-- Name: lt_test_set lt_test_set_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.lt_test_set
    ADD CONSTRAINT lt_test_set_pkey PRIMARY KEY (id);


--
-- Name: mail_account mail_account_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.mail_account
    ADD CONSTRAINT mail_account_pkey PRIMARY KEY (id);


--
-- Name: mail mail_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.mail
    ADD CONSTRAINT mail_pkey PRIMARY KEY (id);


--
-- Name: method method_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.method
    ADD CONSTRAINT method_pkey PRIMARY KEY (id);


--
-- Name: method_support method_support_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.method_support
    ADD CONSTRAINT method_support_pkey PRIMARY KEY (id);


--
-- Name: metrics metrics_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.metrics
    ADD CONSTRAINT metrics_pkey PRIMARY KEY (id);


--
-- Name: money_account money_account_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.money_account
    ADD CONSTRAINT money_account_pkey PRIMARY KEY (id);


--
-- Name: money_account_type money_account_type_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.money_account_type
    ADD CONSTRAINT money_account_type_pkey PRIMARY KEY (id);


--
-- Name: money_flow money_flow_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.money_flow
    ADD CONSTRAINT money_flow_pkey PRIMARY KEY (id);


--
-- Name: money_flow_priority money_flow_priority_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.money_flow_priority
    ADD CONSTRAINT money_flow_priority_pkey PRIMARY KEY (id);


--
-- Name: money_flow_type money_flow_type_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.money_flow_type
    ADD CONSTRAINT money_flow_type_pkey PRIMARY KEY (id);


--
-- Name: money_statement money_statement_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.money_statement
    ADD CONSTRAINT money_statement_pkey PRIMARY KEY (id);


--
-- Name: money_transfer money_transfer_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.money_transfer
    ADD CONSTRAINT money_transfer_pkey PRIMARY KEY (id);


--
-- Name: multiple_booking multiple_booking_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.multiple_booking
    ADD CONSTRAINT multiple_booking_pkey PRIMARY KEY (id);


--
-- Name: operation operation_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.operation
    ADD CONSTRAINT operation_pkey PRIMARY KEY (id);


--
-- Name: option_condition option_condition_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_pkey PRIMARY KEY (id);


--
-- Name: option option_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option
    ADD CONSTRAINT option_pkey PRIMARY KEY (id);


--
-- Name: organization organization_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.organization
    ADD CONSTRAINT organization_pkey PRIMARY KEY (id);


--
-- Name: organization_type organization_type_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.organization_type
    ADD CONSTRAINT organization_type_pkey PRIMARY KEY (id);


--
-- Name: package_item package_item_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.package_item
    ADD CONSTRAINT package_item_pkey PRIMARY KEY (id);


--
-- Name: package package_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.package
    ADD CONSTRAINT package_pkey PRIMARY KEY (id);


--
-- Name: person person_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.person
    ADD CONSTRAINT person_pkey PRIMARY KEY (id);


--
-- Name: rate rate_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_pkey PRIMARY KEY (id);


--
-- Name: recipient recipient_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.recipient
    ADD CONSTRAINT recipient_pkey PRIMARY KEY (id);


--
-- Name: resource_configuration resource_configuration_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.resource_configuration
    ADD CONSTRAINT resource_configuration_pkey PRIMARY KEY (id);


--
-- Name: resource resource_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.resource
    ADD CONSTRAINT resource_pkey PRIMARY KEY (id);


--
-- Name: role_attribution role_attribution_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.role_attribution
    ADD CONSTRAINT role_attribution_pkey PRIMARY KEY (id);


--
-- Name: role role_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.role
    ADD CONSTRAINT role_pkey PRIMARY KEY (id);


--
-- Name: sector sector_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.sector
    ADD CONSTRAINT sector_pkey PRIMARY KEY (id);


--
-- Name: session_agent session_agent_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.session_agent
    ADD CONSTRAINT session_agent_pkey PRIMARY KEY (id);


--
-- Name: session_application session_application_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.session_application
    ADD CONSTRAINT session_application_pkey PRIMARY KEY (id);


--
-- Name: session_connection session_connection_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.session_connection
    ADD CONSTRAINT session_connection_pkey PRIMARY KEY (id);


--
-- Name: session_process session_process_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.session_process
    ADD CONSTRAINT session_process_pkey PRIMARY KEY (id);


--
-- Name: session_user session_user_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public."session_user"
    ADD CONSTRAINT session_user_pkey PRIMARY KEY (id);


--
-- Name: site_item_family site_item_family_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.site_item_family
    ADD CONSTRAINT site_item_family_pkey PRIMARY KEY (id);


--
-- Name: site site_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.site
    ADD CONSTRAINT site_pkey PRIMARY KEY (id);


--
-- Name: smtp_account smtp_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.smtp_account
    ADD CONSTRAINT smtp_pkey PRIMARY KEY (id);


--
-- Name: smtp_quota smtp_quota_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.smtp_quota
    ADD CONSTRAINT smtp_quota_pkey PRIMARY KEY (id);


--
-- Name: stock stock_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.stock
    ADD CONSTRAINT stock_pkey PRIMARY KEY (id);


--
-- Name: stock_transfer stock_transfer_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.stock_transfer
    ADD CONSTRAINT stock_transfer_pkey PRIMARY KEY (id);


--
-- Name: subscription subscription_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.subscription
    ADD CONSTRAINT subscription_pkey PRIMARY KEY (id);


--
-- Name: sys_log sys_log_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.sys_log
    ADD CONSTRAINT sys_log_pkey PRIMARY KEY (id);


--
-- Name: teacher teacher_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.teacher
    ADD CONSTRAINT teacher_pkey PRIMARY KEY (id);


--
-- Name: third_party third_party_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.third_party
    ADD CONSTRAINT third_party_pkey PRIMARY KEY (id);


--
-- Name: timezone timezone_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.timezone
    ADD CONSTRAINT timezone_pkey PRIMARY KEY (id);


--
-- Name: vat vat_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.vat
    ADD CONSTRAINT vat_pkey PRIMARY KEY (id);


--
-- Name: visibility visibility_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.visibility
    ADD CONSTRAINT visibility_pkey PRIMARY KEY (id);


--
-- Name: warehouse warehouse_pkey; Type: CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.warehouse
    ADD CONSTRAINT warehouse_pkey PRIMARY KEY (id);


--
-- Name: attendance_document_line_id_idx; Type: INDEX; Schema: public; Owner: kbs
--

CREATE INDEX attendance_document_line_id_idx ON public.attendance USING btree (document_line_id);


--
-- Name: cart_message_condition_cart_message_id_idx; Type: INDEX; Schema: public; Owner: kbs
--

CREATE INDEX cart_message_condition_cart_message_id_idx ON public.cart_message_condition USING btree (cart_message_id);


--
-- Name: cart_message_event_id_idx; Type: INDEX; Schema: public; Owner: kbs
--

CREATE INDEX cart_message_event_id_idx ON public.cart_message USING btree (event_id);


--
-- Name: document_cart_id_idx; Type: INDEX; Schema: public; Owner: kbs
--

CREATE INDEX document_cart_id_idx ON public.document USING btree (cart_id);


--
-- Name: document_event_id_idx; Type: INDEX; Schema: public; Owner: kbs
--

CREATE INDEX document_event_id_idx ON public.document USING btree (event_id);


--
-- Name: document_line_document_id_idx; Type: INDEX; Schema: public; Owner: kbs
--

CREATE INDEX document_line_document_id_idx ON public.document_line USING btree (document_id);


--
-- Name: document_line_item_id_idx; Type: INDEX; Schema: public; Owner: kbs
--

CREATE INDEX document_line_item_id_idx ON public.document_line USING btree (item_id);


--
-- Name: document_line_resource_configuration_id_idx; Type: INDEX; Schema: public; Owner: kbs
--

CREATE INDEX document_line_resource_configuration_id_idx ON public.document_line USING btree (resource_configuration_id);


--
-- Name: document_line_resource_id_idx; Type: INDEX; Schema: public; Owner: kbs
--

CREATE INDEX document_line_resource_id_idx ON public.document_line USING btree (resource_id);


--
-- Name: document_line_share_mate_owner_document_line_id_idx; Type: INDEX; Schema: public; Owner: kbs
--

CREATE INDEX document_line_share_mate_owner_document_line_id_idx ON public.document_line USING btree (share_mate_owner_document_line_id);


--
-- Name: document_line_site_id_idx; Type: INDEX; Schema: public; Owner: kbs
--

CREATE INDEX document_line_site_id_idx ON public.document_line USING btree (site_id);


--
-- Name: document_person_carer1_document_id_idx; Type: INDEX; Schema: public; Owner: kbs
--

CREATE INDEX document_person_carer1_document_id_idx ON public.document USING btree (person_carer1_document_id);


--
-- Name: document_person_carer2_document_id_idx; Type: INDEX; Schema: public; Owner: kbs
--

CREATE INDEX document_person_carer2_document_id_idx ON public.document USING btree (person_carer2_document_id);


--
-- Name: document_person_email_idx; Type: INDEX; Schema: public; Owner: kbs
--

CREATE INDEX document_person_email_idx ON public.document USING btree (person_email, person_abc_names);


--
-- Name: document_person_email_lower_idx; Type: INDEX; Schema: public; Owner: kbs
--

CREATE INDEX document_person_email_lower_idx ON public.document USING btree (lower((person_email)::text) varchar_pattern_ops, lower((person_abc_names)::text) varchar_pattern_ops);


--
-- Name: document_person_id_idx; Type: INDEX; Schema: public; Owner: kbs
--

CREATE INDEX document_person_id_idx ON public.document USING btree (person_id);


--
-- Name: document_person_organization_id_idx; Type: INDEX; Schema: public; Owner: kbs
--

CREATE INDEX document_person_organization_id_idx ON public.document USING btree (person_organization_id);


--
-- Name: event_booking_closing_date_idx; Type: INDEX; Schema: public; Owner: kbs
--

CREATE INDEX event_booking_closing_date_idx ON public.event USING btree (booking_closing_date, booking_closed);


--
-- Name: event_corporation_id_idx; Type: INDEX; Schema: public; Owner: kbs
--

CREATE INDEX event_corporation_id_idx ON public.event USING btree (corporation_id);


--
-- Name: event_organization_id_idx; Type: INDEX; Schema: public; Owner: kbs
--

CREATE INDEX event_organization_id_idx ON public.event USING btree (organization_id);


--
-- Name: frontend_account_corporation_id_idx; Type: INDEX; Schema: public; Owner: kbs
--

CREATE INDEX frontend_account_corporation_id_idx ON public.frontend_account USING btree (corporation_id);


--
-- Name: frontend_account_pwdreset_token_idx; Type: INDEX; Schema: public; Owner: kbs
--

CREATE INDEX frontend_account_pwdreset_token_idx ON public.frontend_account USING btree (pwdreset_token, username);


--
-- Name: history_document_id_idx; Type: INDEX; Schema: public; Owner: kbs
--

CREATE INDEX history_document_id_idx ON public.history USING btree (document_id);


--
-- Name: mail_document_id_idx; Type: INDEX; Schema: public; Owner: kbs
--

CREATE INDEX mail_document_id_idx ON public.mail USING btree (document_id);


--
-- Name: money_flow_priority_flow_id_idx; Type: INDEX; Schema: public; Owner: kbs
--

CREATE INDEX money_flow_priority_flow_id_idx ON public.money_flow_priority USING btree (flow_id);


--
-- Name: money_transfer_document_id_idx; Type: INDEX; Schema: public; Owner: kbs
--

CREATE INDEX money_transfer_document_id_idx ON public.money_transfer USING btree (document_id);


--
-- Name: money_transfer_from_money_account_id_idx; Type: INDEX; Schema: public; Owner: kbs
--

CREATE INDEX money_transfer_from_money_account_id_idx ON public.money_transfer USING btree (from_money_account_id);


--
-- Name: money_transfer_gateway_accesscode_idx; Type: INDEX; Schema: public; Owner: kbs
--

CREATE INDEX money_transfer_gateway_accesscode_idx ON public.money_transfer USING btree (gateway_accesscode);


--
-- Name: money_transfer_parent_id_idx; Type: INDEX; Schema: public; Owner: kbs
--

CREATE INDEX money_transfer_parent_id_idx ON public.money_transfer USING btree (parent_id);


--
-- Name: money_transfer_to_money_account_id_idx; Type: INDEX; Schema: public; Owner: kbs
--

CREATE INDEX money_transfer_to_money_account_id_idx ON public.money_transfer USING btree (to_money_account_id);


--
-- Name: money_transfer_transfer_id_idx; Type: INDEX; Schema: public; Owner: kbs
--

CREATE INDEX money_transfer_transfer_id_idx ON public.money_transfer USING btree (transfer_id);


--
-- Name: option_condition_if_option_id_idx; Type: INDEX; Schema: public; Owner: kbs
--

CREATE INDEX option_condition_if_option_id_idx ON public.option_condition USING btree (if_option_id);


--
-- Name: option_condition_option_id_idx; Type: INDEX; Schema: public; Owner: kbs
--

CREATE INDEX option_condition_option_id_idx ON public.option_condition USING btree (option_id);


--
-- Name: option_condition_parent_id_idx; Type: INDEX; Schema: public; Owner: kbs
--

CREATE INDEX option_condition_parent_id_idx ON public.option_condition USING btree (parent_id);


--
-- Name: person_frontend_account_id_idx; Type: INDEX; Schema: public; Owner: kbs
--

CREATE INDEX person_frontend_account_id_idx ON public.person USING btree (frontend_account_id);


--
-- Name: resource_configuration_item_id_idx; Type: INDEX; Schema: public; Owner: kbs
--

CREATE INDEX resource_configuration_item_id_idx ON public.resource_configuration USING btree (item_id);


--
-- Name: resource_configuration_resource_id_idx; Type: INDEX; Schema: public; Owner: kbs
--

CREATE INDEX resource_configuration_resource_id_idx ON public.resource_configuration USING btree (resource_id);


--
-- Name: resource_site_id_idx; Type: INDEX; Schema: public; Owner: kbs
--

CREATE INDEX resource_site_id_idx ON public.resource USING btree (site_id);


--
-- Name: site_event_id_idx; Type: INDEX; Schema: public; Owner: kbs
--

CREATE INDEX site_event_id_idx ON public.site USING btree (event_id);


--
-- Name: site_organization_id_idx; Type: INDEX; Schema: public; Owner: kbs
--

CREATE INDEX site_organization_id_idx ON public.site USING btree (organization_id);


--
-- Name: history append_request_to_document; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER append_request_to_document AFTER INSERT ON public.history FOR EACH ROW WHEN (((new.request IS NOT NULL) AND ((new.request)::text <> ''::text) AND ((new.username)::text = 'online'::text))) EXECUTE FUNCTION public.trigger_history_append_request_to_document();


--
-- Name: allocation_rule apply_rule; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER apply_rule AFTER UPDATE OF trigger_allocate ON public.allocation_rule FOR EACH ROW WHEN (((old.trigger_allocate = false) AND (new.trigger_allocate = true))) EXECUTE FUNCTION public.trigger_allocation_rule_apply_rule();


--
-- Name: mail auto_account; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER auto_account BEFORE INSERT ON public.mail FOR EACH ROW WHEN ((new.account_id IS NULL)) EXECUTE FUNCTION public.trigger_mail_auto_account();


--
-- Name: mail auto_delete; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER auto_delete AFTER UPDATE OF transmitted ON public.mail FOR EACH ROW WHEN ((new.auto_delete AND new.transmitted AND (new.error IS NULL))) EXECUTE FUNCTION public.trigger_mail_auto_delete();


--
-- Name: mail auto_recipient; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER auto_recipient AFTER INSERT ON public.mail FOR EACH ROW EXECUTE FUNCTION public.trigger_mail_auto_recipient();


--
-- Name: document auto_ref; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER auto_ref BEFORE INSERT ON public.document FOR EACH ROW EXECUTE FUNCTION public.trigger_document_auto_ref();


--
-- Name: resource auto_site; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER auto_site BEFORE INSERT ON public.resource FOR EACH ROW WHEN ((new.site_id IS NULL)) EXECUTE FUNCTION public.trigger_resource_auto_site();


--
-- Name: resource auto_site_item_family; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER auto_site_item_family BEFORE INSERT ON public.resource FOR EACH ROW WHEN ((new.site_item_family_code IS NOT NULL)) EXECUTE FUNCTION public.trigger_resource_auto_site_item_family();


--
-- Name: money_transfer autoset_accounts; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER autoset_accounts AFTER INSERT OR UPDATE OF method_id ON public.money_transfer FOR EACH ROW WHEN ((new.parent_id IS NULL)) EXECUTE FUNCTION public.trigger_money_transfer_autoset_accounts();


--
-- Name: money_transfer autoset_children; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER autoset_children BEFORE INSERT ON public.money_transfer FOR EACH ROW WHEN ((new.parent_id IS NOT NULL)) EXECUTE FUNCTION public.trigger_money_transfer_autoset_children();


--
-- Name: option autoset_event_from_parent; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER autoset_event_from_parent BEFORE INSERT ON public.option FOR EACH ROW WHEN ((new.event_id IS NULL)) EXECUTE FUNCTION public.trigger_option_autoset_event_from_parent();


--
-- Name: document cancel_other_multiple_bookings; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER cancel_other_multiple_bookings AFTER UPDATE OF trigger_cancel_other_multiple_bookings ON public.document FOR EACH ROW EXECUTE FUNCTION public.trigger_document_cancel_other_multiple_bookings();


--
-- Name: money_transfer cascade_children; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER cascade_children AFTER UPDATE OF date, method_id, payment, refund, pending, successful, read, verified ON public.money_transfer FOR EACH ROW WHEN (new.spread) EXECUTE FUNCTION public.trigger_money_transfer_cascade_children();


--
-- Name: document cascadecancelled; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER cascadecancelled AFTER UPDATE OF cancelled, price_deposit ON public.document FOR EACH ROW EXECUTE FUNCTION public.trigger_document_cascadecancelled();


--
-- Name: money_transfer cascadedeletetransfer; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER cascadedeletetransfer BEFORE DELETE ON public.money_transfer FOR EACH ROW WHEN ((old.parent_id IS NOT NULL)) EXECUTE FUNCTION public.trigger_money_transfer_cascadedeletetransfer();


--
-- Name: document cascaderead; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER cascaderead AFTER UPDATE OF read ON public.document FOR EACH ROW WHEN ((new.read = true)) EXECUTE FUNCTION public.trigger_document_cascaderead();


--
-- Name: document_line cascadeunread; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER cascadeunread AFTER INSERT OR UPDATE OF read ON public.document_line FOR EACH ROW WHEN ((NOT new.read)) EXECUTE FUNCTION public.trigger_document_line_cascadeunread();


--
-- Name: mail cascadeunread; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER cascadeunread AFTER INSERT OR UPDATE OF read ON public.mail FOR EACH ROW WHEN ((new.read = false)) EXECUTE FUNCTION public.trigger_mail_cascadeunread();


--
-- Name: money_transfer cascadeunread; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER cascadeunread AFTER INSERT OR UPDATE OF read ON public.money_transfer FOR EACH ROW WHEN ((new.read = false)) EXECUTE FUNCTION public.trigger_money_transfer_cascadeunread();


--
-- Name: document check_multiple_booking; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER check_multiple_booking AFTER INSERT OR UPDATE OF trigger_check_multiple_booking, not_multiple_booking_id ON public.document FOR EACH ROW EXECUTE FUNCTION public.trigger_document_check_multiple_booking();


--
-- Name: resource_configuration check_overlap; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER check_overlap BEFORE INSERT OR UPDATE OF resource_id, start_date, end_date ON public.resource_configuration FOR EACH ROW EXECUTE FUNCTION public.trigger_resource_configuration_check_overlap();


--
-- Name: document_line defer_allocate; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER defer_allocate AFTER INSERT OR UPDATE OF site_id, item_id, private, dates ON public.document_line FOR EACH ROW WHEN ((new.share_mate_owner_document_line_id IS NULL)) EXECUTE FUNCTION public.trigger_document_line_defer_allocate();


--
-- Name: money_transfer defer_compute_document_deposit; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER defer_compute_document_deposit AFTER INSERT OR DELETE OR UPDATE OF amount, successful, document_id ON public.money_transfer FOR EACH ROW EXECUTE FUNCTION public.trigger_money_transfer_defer_compute_document_deposit();


--
-- Name: document_line defer_compute_document_prices; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER defer_compute_document_prices AFTER INSERT OR DELETE OR UPDATE OF price, price_net, price_is_custom, price_custom, price_discount, price_min_deposit, price_non_refundable, site_id, item_id, rate_id, share_owner_quantity, share_mate, share_mate_charged, cancelled, abandoned, cancellation_date ON public.document_line FOR EACH ROW EXECUTE FUNCTION public.trigger_document_line_defer_compute_document_prices();


--
-- Name: attendance defer_compute_document_prices_dates; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER defer_compute_document_prices_dates AFTER INSERT OR DELETE OR UPDATE OF date, charged, present ON public.attendance FOR EACH ROW EXECUTE FUNCTION public.trigger_attendance_defer_compute_document_prices_dates();


--
-- Name: document defer_compute_lines_deposit; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER defer_compute_lines_deposit AFTER UPDATE OF price_net, price_min_deposit, price_deposit ON public.document FOR EACH ROW EXECUTE FUNCTION public.trigger_document_defer_compute_lines_deposit();


--
-- Name: money_transfer defer_compute_money_account_balances; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER defer_compute_money_account_balances AFTER INSERT OR DELETE OR UPDATE OF date, from_money_account_id, to_money_account_id, amount, pending, successful ON public.money_transfer FOR EACH ROW EXECUTE FUNCTION public.trigger_money_transfer_defer_compute_money_account_balances();


--
-- Name: document defer_compute_prices; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER defer_compute_prices AFTER UPDATE OF person_age, person_resident, person_resident2, person_unemployed, person_facility_fee, person_discovery, person_discovery_reduced, person_working_visit, person_guest ON public.document FOR EACH ROW EXECUTE FUNCTION public.trigger_document_defer_compute_prices();


--
-- Name: document_line deferred_allocate_document_line; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE CONSTRAINT TRIGGER deferred_allocate_document_line AFTER UPDATE OF trigger_defer_allocate ON public.document_line DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION public.deferred_allocate_document_line();


--
-- Name: document deferred_compute_document_dates; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE CONSTRAINT TRIGGER deferred_compute_document_dates AFTER INSERT OR UPDATE OF trigger_defer_compute_dates ON public.document DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION public.deferred_compute_document_dates();


--
-- Name: document deferred_compute_document_deposit; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE CONSTRAINT TRIGGER deferred_compute_document_deposit AFTER UPDATE OF trigger_defer_compute_deposit ON public.document DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION public.deferred_compute_document_deposit();


--
-- Name: document deferred_compute_document_lines_deposit; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE CONSTRAINT TRIGGER deferred_compute_document_lines_deposit AFTER UPDATE OF trigger_defer_compute_lines_deposit ON public.document DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION public.deferred_compute_document_lines_deposit();


--
-- Name: document deferred_compute_document_price; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE CONSTRAINT TRIGGER deferred_compute_document_price AFTER INSERT OR UPDATE OF trigger_defer_compute_prices ON public.document DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION public.deferred_compute_document_prices();


--
-- Name: money_account deferred_compute_money_account_balances; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE CONSTRAINT TRIGGER deferred_compute_money_account_balances AFTER UPDATE OF trigger_defer_compute_balances ON public.money_account DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION public.deferred_compute_money_account_balances();


--
-- Name: history deferred_send_history_email; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE CONSTRAINT TRIGGER deferred_send_history_email AFTER UPDATE OF trigger_defer_send_email ON public.history DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION public.deferred_send_history_email();


--
-- Name: money_transfer deferred_update_spread_money_transfer; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE CONSTRAINT TRIGGER deferred_update_spread_money_transfer AFTER UPDATE OF trigger_defer_update_spread_money_transfer ON public.money_transfer DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION public.deferred_update_spread_money_transfer();


--
-- Name: resource duplicate; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER duplicate AFTER UPDATE OF trigger_duplicate ON public.resource FOR EACH ROW EXECUTE FUNCTION public.trigger_resource_duplicate();


--
-- Name: document generate_mails_on_booking; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER generate_mails_on_booking AFTER INSERT ON public.document FOR EACH ROW WHEN (((new.event_id IS NOT NULL) AND (new.person_email IS NOT NULL))) EXECUTE FUNCTION public.trigger_document_generate_mails_on_booking();


--
-- Name: frontend_account generate_password_email; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER generate_password_email AFTER UPDATE OF trigger_send_password, trigger_send_password_event_id ON public.frontend_account FOR EACH ROW WHEN (((new.trigger_send_password = true) OR (new.trigger_send_password_event_id IS NOT NULL))) EXECUTE FUNCTION public.trigger_frontend_account_generate_password_email();


--
-- Name: document merge_from_other_multiple_bookings; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER merge_from_other_multiple_bookings AFTER UPDATE OF trigger_merge_from_other_multiple_bookings ON public.document FOR EACH ROW EXECUTE FUNCTION public.trigger_document_merge_from_other_multiple_bookings();


--
-- Name: document_line on_cancelled_auto_release; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER on_cancelled_auto_release AFTER UPDATE OF cancelled ON public.document_line FOR EACH ROW EXECUTE FUNCTION public.trigger_document_line_on_cancelled_auto_release();


--
-- Name: resource_configuration on_item_changed_cascade_document_lines; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER on_item_changed_cascade_document_lines AFTER UPDATE OF item_id ON public.resource_configuration FOR EACH ROW EXECUTE FUNCTION public.trigger_resource_configuration_on_item_changed_cascade_document();


--
-- Name: document_line on_not_system_allocated; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER on_not_system_allocated BEFORE UPDATE OF resource_configuration_id ON public.document_line FOR EACH ROW WHEN ((NOT old.trigger_defer_allocate)) EXECUTE FUNCTION public.trigger_document_line_on_not_system_allocated();


--
-- Name: document_line on_owner_changes_cascade_mates; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER on_owner_changes_cascade_mates AFTER UPDATE OF site_id, item_id, dates, resource_configuration_id, backend_released, frontend_released ON public.document_line FOR EACH ROW WHEN ((new.share_owner = true)) EXECUTE FUNCTION public.trigger_document_line_on_owner_changes_cascade_mates();


--
-- Name: document_line on_share_linked_copy_info; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER on_share_linked_copy_info AFTER INSERT OR UPDATE OF share_mate_owner_document_line_id ON public.document_line FOR EACH ROW EXECUTE FUNCTION public.trigger_document_line_on_share_linked_copy_info();


--
-- Name: money_transfer on_success_send_history_email; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER on_success_send_history_email AFTER UPDATE OF successful ON public.money_transfer FOR EACH ROW WHEN (((old.successful = false) AND (new.successful = true))) EXECUTE FUNCTION public.trigger_money_transfer_on_success_send_history_email();


--
-- Name: money_transfer on_verified; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER on_verified AFTER UPDATE OF verified ON public.money_transfer FOR EACH ROW WHEN (new.receipts_transfer) EXECUTE FUNCTION public.trigger_money_transfer_on_verified();


--
-- Name: history online_defer_send_email; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER online_defer_send_email AFTER INSERT ON public.history FOR EACH ROW WHEN ((((new.username)::text = 'online'::text) AND (new.money_transfer_id IS NULL))) EXECUTE FUNCTION public.trigger_history_online_defer_send_email();


--
-- Name: document read_on_confirmed_or_arrived; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER read_on_confirmed_or_arrived AFTER UPDATE OF confirmed, arrived ON public.document FOR EACH ROW WHEN ((new.confirmed OR new.arrived)) EXECUTE FUNCTION public.trigger_document_read_on_confirmed_or_arrived();


--
-- Name: allocation_rule record_changes_for_notification; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER record_changes_for_notification AFTER UPDATE ON public.allocation_rule FOR EACH ROW EXECUTE FUNCTION public.record_changes_for_notification_allocation_rule();


--
-- Name: document record_changes_for_notification; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER record_changes_for_notification AFTER UPDATE ON public.document FOR EACH ROW EXECUTE FUNCTION public.record_changes_for_notification_document();


--
-- Name: document_line record_changes_for_notification; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER record_changes_for_notification AFTER INSERT OR UPDATE ON public.document_line FOR EACH ROW EXECUTE FUNCTION public.record_changes_for_notification_document_line();


--
-- Name: label record_changes_for_notification; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER record_changes_for_notification AFTER INSERT OR UPDATE ON public.label FOR EACH ROW EXECUTE FUNCTION public.record_changes_for_notification_label();


--
-- Name: letter record_changes_for_notification; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER record_changes_for_notification AFTER UPDATE ON public.letter FOR EACH ROW EXECUTE FUNCTION public.record_changes_for_notification_letter();


--
-- Name: mail record_changes_for_notification; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER record_changes_for_notification AFTER UPDATE ON public.mail FOR EACH ROW EXECUTE FUNCTION public.record_changes_for_notification_mail();


--
-- Name: money_account record_changes_for_notification; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER record_changes_for_notification AFTER UPDATE ON public.money_account FOR EACH ROW EXECUTE FUNCTION public.record_changes_for_notification_money_account();


--
-- Name: money_transfer record_changes_for_notification; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER record_changes_for_notification AFTER UPDATE ON public.money_transfer FOR EACH ROW EXECUTE FUNCTION public.record_changes_for_notification_money_transfer();


--
-- Name: multiple_booking record_changes_for_notification; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER record_changes_for_notification AFTER UPDATE ON public.multiple_booking FOR EACH ROW EXECUTE FUNCTION public.record_changes_for_notification_multiple_booking();


--
-- Name: option record_changes_for_notification; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER record_changes_for_notification AFTER INSERT OR UPDATE ON public.option FOR EACH ROW EXECUTE FUNCTION public.record_changes_for_notification_option();


--
-- Name: organization record_changes_for_notification; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER record_changes_for_notification AFTER UPDATE ON public.organization FOR EACH ROW EXECUTE FUNCTION public.record_changes_for_notification_organization();


--
-- Name: rate record_changes_for_notification; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER record_changes_for_notification AFTER UPDATE ON public.rate FOR EACH ROW EXECUTE FUNCTION public.record_changes_for_notification_rate();


--
-- Name: resource record_changes_for_notification; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER record_changes_for_notification AFTER UPDATE ON public.resource FOR EACH ROW EXECUTE FUNCTION public.record_changes_for_notification_resource();


--
-- Name: resource_configuration record_changes_for_notification; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER record_changes_for_notification AFTER UPDATE ON public.resource_configuration FOR EACH ROW EXECUTE FUNCTION public.record_changes_for_notification_resource_configuration();


--
-- Name: document send_letter; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER send_letter AFTER UPDATE OF trigger_send_letter_id ON public.document FOR EACH ROW WHEN ((new.trigger_send_letter_id IS NOT NULL)) EXECUTE FUNCTION public.trigger_document_send_letter();


--
-- Name: document send_system_letter; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER send_system_letter AFTER UPDATE OF trigger_send_system_letter_id ON public.document FOR EACH ROW WHEN ((new.trigger_send_system_letter_id IS NOT NULL)) EXECUTE FUNCTION public.trigger_document_send_system_letter();


--
-- Name: document set_cancellation_date; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER set_cancellation_date BEFORE UPDATE OF cancelled ON public.document FOR EACH ROW WHEN (((NOT old.cancelled) AND new.cancelled)) EXECUTE FUNCTION public.trigger_document_set_cancellation_date();


--
-- Name: document_line set_cancellation_date; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER set_cancellation_date BEFORE UPDATE OF cancelled ON public.document_line FOR EACH ROW WHEN (((NOT old.cancelled) AND new.cancelled)) EXECUTE FUNCTION public.trigger_document_line_set_cancellation_date();


--
-- Name: document_line set_donation_site; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER set_donation_site BEFORE INSERT ON public.document_line FOR EACH ROW WHEN ((new.item_id = 104)) EXECUTE FUNCTION public.trigger_document_line_set_donation_site();


--
-- Name: document_line set_lang; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER set_lang AFTER INSERT OR UPDATE OF item_id ON public.document_line FOR EACH ROW EXECUTE FUNCTION public.trigger_document_line_set_lang();


--
-- Name: document set_person_abc_names; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER set_person_abc_names BEFORE INSERT OR UPDATE OF person_first_name, person_last_name ON public.document FOR EACH ROW EXECUTE FUNCTION public.trigger_document_set_person_abc_names();


--
-- Name: document transfer_from_other_multiple_bookings; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER transfer_from_other_multiple_bookings AFTER UPDATE OF trigger_transfer_from_other_multiple_bookings ON public.document FOR EACH ROW EXECUTE FUNCTION public.trigger_document_transfer_from_other_multiple_bookings();


--
-- Name: money_flow transfer_ready_daily_batch; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER transfer_ready_daily_batch BEFORE UPDATE OF trigger_transfer ON public.money_flow FOR EACH ROW WHEN (new.trigger_transfer) EXECUTE FUNCTION public.trigger_money_flow_transfer_ready_daily_batch();


--
-- Name: multiple_booking update_counts; Type: TRIGGER; Schema: public; Owner: kbs
--

CREATE TRIGGER update_counts AFTER UPDATE OF trigger_update_counts ON public.multiple_booking FOR EACH ROW EXECUTE FUNCTION public.trigger_multiple_booking_update_counts();


--
-- Name: accounting_account accounting_account_label_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.accounting_account
    ADD CONSTRAINT accounting_account_label_id_fkey FOREIGN KEY (label_id) REFERENCES public.label(id);


--
-- Name: accounting_account accounting_account_model_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.accounting_account
    ADD CONSTRAINT accounting_account_model_id_fkey FOREIGN KEY (model_id) REFERENCES public.accounting_model(id);


--
-- Name: accounting_account accounting_account_type_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.accounting_account
    ADD CONSTRAINT accounting_account_type_id_fkey FOREIGN KEY (type_id) REFERENCES public.accounting_account_type(id);


--
-- Name: accounting_account_type accounting_account_type_label_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.accounting_account_type
    ADD CONSTRAINT accounting_account_type_label_id_fkey FOREIGN KEY (label_id) REFERENCES public.label(id);


--
-- Name: activity activity_label_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.activity
    ADD CONSTRAINT activity_label_id_fkey FOREIGN KEY (label_id) REFERENCES public.label(id);


--
-- Name: activity activity_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.activity
    ADD CONSTRAINT activity_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES public.organization(id);


--
-- Name: activity activity_parent_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.activity
    ADD CONSTRAINT activity_parent_id_fkey FOREIGN KEY (parent_id) REFERENCES public.activity(id);


--
-- Name: activity_state activity_state_owner_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.activity_state
    ADD CONSTRAINT activity_state_owner_id_fkey FOREIGN KEY (owner_id) REFERENCES public.person(id);


--
-- Name: allocation_rule allocation_rule_event_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.allocation_rule
    ADD CONSTRAINT allocation_rule_event_id_fkey FOREIGN KEY (event_id) REFERENCES public.event(id) ON DELETE CASCADE;


--
-- Name: allocation_rule allocation_rule_if_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.allocation_rule
    ADD CONSTRAINT allocation_rule_if_country_id_fkey FOREIGN KEY (if_country_id) REFERENCES public.country(id);


--
-- Name: allocation_rule allocation_rule_if_item_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.allocation_rule
    ADD CONSTRAINT allocation_rule_if_item_id_fkey FOREIGN KEY (if_item_id) REFERENCES public.item(id);


--
-- Name: allocation_rule allocation_rule_if_language_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.allocation_rule
    ADD CONSTRAINT allocation_rule_if_language_id_fkey FOREIGN KEY (if_language_id) REFERENCES public.language(id);


--
-- Name: allocation_rule allocation_rule_if_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.allocation_rule
    ADD CONSTRAINT allocation_rule_if_organization_id_fkey FOREIGN KEY (if_organization_id) REFERENCES public.organization(id);


--
-- Name: allocation_rule allocation_rule_if_site_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.allocation_rule
    ADD CONSTRAINT allocation_rule_if_site_id_fkey FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: allocation_rule allocation_rule_item_family_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.allocation_rule
    ADD CONSTRAINT allocation_rule_item_family_id_fkey FOREIGN KEY (item_family_id) REFERENCES public.item_family(id) ON DELETE CASCADE;


--
-- Name: allocation_rule allocation_rule_resource_configuration_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.allocation_rule
    ADD CONSTRAINT allocation_rule_resource_configuration_id_fkey FOREIGN KEY (resource_configuration_id) REFERENCES public.resource_configuration(id);


--
-- Name: allocation_rule allocation_rule_resource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.allocation_rule
    ADD CONSTRAINT allocation_rule_resource_id_fkey FOREIGN KEY (resource_id) REFERENCES public.resource(id) ON DELETE CASCADE;


--
-- Name: attendance attendance_document_line_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.attendance
    ADD CONSTRAINT attendance_document_line_id_fkey FOREIGN KEY (document_line_id) REFERENCES public.document_line(id) ON DELETE CASCADE;


--
-- Name: attendance attendance_rate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.attendance
    ADD CONSTRAINT attendance_rate_id_fkey FOREIGN KEY (rate_id) REFERENCES public.rate(id);


--
-- Name: authorization_assignment authorization_assignment_activity_state_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.authorization_assignment
    ADD CONSTRAINT authorization_assignment_activity_state_id_fkey FOREIGN KEY (activity_state_id) REFERENCES public.activity_state(id);


--
-- Name: authorization_assignment authorization_assignment_management_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.authorization_assignment
    ADD CONSTRAINT authorization_assignment_management_id_fkey FOREIGN KEY (management_id) REFERENCES public.authorization_management(id);


--
-- Name: authorization_assignment authorization_assignment_operation_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.authorization_assignment
    ADD CONSTRAINT authorization_assignment_operation_id_fkey FOREIGN KEY (operation_id) REFERENCES public.operation(id);


--
-- Name: authorization_assignment authorization_assignment_role_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.authorization_assignment
    ADD CONSTRAINT authorization_assignment_role_id_fkey FOREIGN KEY (role_id) REFERENCES public.authorization_role(id);


--
-- Name: authorization_assignment authorization_assignment_rule_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.authorization_assignment
    ADD CONSTRAINT authorization_assignment_rule_id_fkey FOREIGN KEY (rule_id) REFERENCES public.authorization_rule(id);


--
-- Name: authorization_assignment authorization_assignment_scope_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.authorization_assignment
    ADD CONSTRAINT authorization_assignment_scope_id_fkey FOREIGN KEY (scope_id) REFERENCES public.authorization_scope(id);


--
-- Name: authorization_management authorization_management_manager_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.authorization_management
    ADD CONSTRAINT authorization_management_manager_id_fkey FOREIGN KEY (manager_id) REFERENCES public.person(id);


--
-- Name: authorization_management authorization_management_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.authorization_management
    ADD CONSTRAINT authorization_management_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.person(id);


--
-- Name: bank bank_statement_system_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.bank
    ADD CONSTRAINT bank_statement_system_fkey FOREIGN KEY (statement_system) REFERENCES public.bank_system(id);


--
-- Name: bank_system_account bank_system_account_statement_system_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.bank_system_account
    ADD CONSTRAINT bank_system_account_statement_system_id_fkey FOREIGN KEY (statement_system_id) REFERENCES public.bank_system(id);


--
-- Name: bank_system bank_system_gateway_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.bank_system
    ADD CONSTRAINT bank_system_gateway_company_id_fkey FOREIGN KEY (gateway_company_id) REFERENCES public.gateway_company(id);


--
-- Name: booking_form_layout booking_form_layout_organization_prompt_label_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.booking_form_layout
    ADD CONSTRAINT booking_form_layout_organization_prompt_label_id_fkey FOREIGN KEY (organization_prompt_label_id) REFERENCES public.label(id);


--
-- Name: buddha buddha_image_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.buddha
    ADD CONSTRAINT buddha_image_id_fkey FOREIGN KEY (image_id) REFERENCES public.image(id);


--
-- Name: buddha buddha_label_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.buddha
    ADD CONSTRAINT buddha_label_id_fkey FOREIGN KEY (label_id) REFERENCES public.label(id);


--
-- Name: buddha buddha_who_label_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.buddha
    ADD CONSTRAINT buddha_who_label_id_fkey FOREIGN KEY (who_label_id) REFERENCES public.label(id);


--
-- Name: budget_line budget_line_budget_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.budget_line
    ADD CONSTRAINT budget_line_budget_id_fkey FOREIGN KEY (budget_id) REFERENCES public.budget(id);


--
-- Name: budget budget_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.budget
    ADD CONSTRAINT budget_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES public.organization(id);


--
-- Name: budget_transfer budget_transfer_from_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.budget_transfer
    ADD CONSTRAINT budget_transfer_from_id_fkey FOREIGN KEY (from_id) REFERENCES public.budget_line(id);


--
-- Name: budget_transfer budget_transfer_to_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.budget_transfer
    ADD CONSTRAINT budget_transfer_to_id_fkey FOREIGN KEY (to_id) REFERENCES public.budget_line(id);


--
-- Name: cart cart_forward_to_cart_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.cart
    ADD CONSTRAINT cart_forward_to_cart_id_fkey FOREIGN KEY (forward_to_cart_id) REFERENCES public.cart(id);


--
-- Name: cart_message_condition cart_message_condition_cart_message_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.cart_message_condition
    ADD CONSTRAINT cart_message_condition_cart_message_id_fk FOREIGN KEY (cart_message_id) REFERENCES public.cart_message(id) ON DELETE CASCADE;


--
-- Name: cart_message_condition cart_message_condition_item_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.cart_message_condition
    ADD CONSTRAINT cart_message_condition_item_id_fk FOREIGN KEY (item_id) REFERENCES public.item(id);


--
-- Name: cart_message_condition cart_message_condition_site_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.cart_message_condition
    ADD CONSTRAINT cart_message_condition_site_id_fk FOREIGN KEY (site_id) REFERENCES public.site(id);


--
-- Name: cart_message cart_message_event_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.cart_message
    ADD CONSTRAINT cart_message_event_id_fk FOREIGN KEY (event_id) REFERENCES public.event(id) ON DELETE CASCADE;


--
-- Name: cart_message cart_message_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.cart_message
    ADD CONSTRAINT cart_message_fk FOREIGN KEY (label_id) REFERENCES public.label(id) ON DELETE RESTRICT;


--
-- Name: country country_account_model_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.country
    ADD CONSTRAINT country_account_model_id_fkey FOREIGN KEY (account_model_id) REFERENCES public.accounting_model(id);


--
-- Name: country country_continent_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.country
    ADD CONSTRAINT country_continent_id_fkey FOREIGN KEY (continent_id) REFERENCES public.continent(id);


--
-- Name: country country_currency_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.country
    ADD CONSTRAINT country_currency_id_fkey FOREIGN KEY (currency_id) REFERENCES public.currency(id);


--
-- Name: country country_label_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.country
    ADD CONSTRAINT country_label_id_fkey FOREIGN KEY (label_id) REFERENCES public.label(id);


--
-- Name: country country_main_language_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.country
    ADD CONSTRAINT country_main_language_id_fkey FOREIGN KEY (main_language_id) REFERENCES public.language(id);


--
-- Name: css css_event_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.css
    ADD CONSTRAINT css_event_id_fkey FOREIGN KEY (event_id) REFERENCES public.event(id);


--
-- Name: currency_support currency_support_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.currency_support
    ADD CONSTRAINT currency_support_company_id_fkey FOREIGN KEY (company_id) REFERENCES public.gateway_company(id);


--
-- Name: currency_support currency_support_currency_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.currency_support
    ADD CONSTRAINT currency_support_currency_id_fkey FOREIGN KEY (currency_id) REFERENCES public.currency(id);


--
-- Name: date_info date_info_event_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.date_info
    ADD CONSTRAINT date_info_event_id_fkey FOREIGN KEY (event_id) REFERENCES public.event(id);


--
-- Name: date_info date_info_fees_bottom_label_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.date_info
    ADD CONSTRAINT date_info_fees_bottom_label_id_fkey FOREIGN KEY (fees_bottom_label_id) REFERENCES public.label(id);


--
-- Name: date_info date_info_fees_popup_label_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.date_info
    ADD CONSTRAINT date_info_fees_popup_label_id_fkey FOREIGN KEY (fees_popup_label_id) REFERENCES public.label(id);


--
-- Name: date_info date_info_label_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.date_info
    ADD CONSTRAINT date_info_label_id_fkey FOREIGN KEY (label_id) REFERENCES public.label(id);


--
-- Name: date_info date_info_option_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.date_info
    ADD CONSTRAINT date_info_option_id_fkey FOREIGN KEY (option_id) REFERENCES public.option(id) ON DELETE SET NULL;


--
-- Name: document document_activity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.document
    ADD CONSTRAINT document_activity_id_fkey FOREIGN KEY (activity_id) REFERENCES public.activity(id);


--
-- Name: document document_cart_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.document
    ADD CONSTRAINT document_cart_id_fkey FOREIGN KEY (cart_id) REFERENCES public.cart(id);


--
-- Name: document document_event_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.document
    ADD CONSTRAINT document_event_id_fkey FOREIGN KEY (event_id) REFERENCES public.event(id) ON DELETE SET NULL;


--
-- Name: document_line document_line_arrival_site_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.document_line
    ADD CONSTRAINT document_line_arrival_site_id_fkey FOREIGN KEY (arrival_site_id) REFERENCES public.site(id);


--
-- Name: document_line document_line_budget_line_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.document_line
    ADD CONSTRAINT document_line_budget_line_id_fkey FOREIGN KEY (budget_line_id) REFERENCES public.budget_line(id);


--
-- Name: document_line document_line_document_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.document_line
    ADD CONSTRAINT document_line_document_id_fkey FOREIGN KEY (document_id) REFERENCES public.document(id) ON DELETE CASCADE;


--
-- Name: document_line document_line_driver_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.document_line
    ADD CONSTRAINT document_line_driver_id_fkey FOREIGN KEY (driver_id) REFERENCES public.driver(id);


--
-- Name: document_line document_line_item_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.document_line
    ADD CONSTRAINT document_line_item_id_fkey FOREIGN KEY (item_id) REFERENCES public.item(id);


--
-- Name: document_line document_line_purchase_rate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.document_line
    ADD CONSTRAINT document_line_purchase_rate_id_fkey FOREIGN KEY (purchase_rate_id) REFERENCES public.rate(id);


--
-- Name: document_line document_line_purchase_vat_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.document_line
    ADD CONSTRAINT document_line_purchase_vat_id_fkey FOREIGN KEY (purchase_vat_id) REFERENCES public.vat(id);


--
-- Name: document_line document_line_rate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.document_line
    ADD CONSTRAINT document_line_rate_id_fkey FOREIGN KEY (rate_id) REFERENCES public.rate(id);


--
-- Name: document_line document_line_resource_configuration_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.document_line
    ADD CONSTRAINT document_line_resource_configuration_id_fkey FOREIGN KEY (resource_configuration_id) REFERENCES public.resource_configuration(id) ON DELETE SET NULL;


--
-- Name: document_line document_line_resource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.document_line
    ADD CONSTRAINT document_line_resource_id_fkey FOREIGN KEY (resource_id) REFERENCES public.resource(id) ON DELETE SET NULL;


--
-- Name: document_line document_line_share_mate_owner_document_line_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.document_line
    ADD CONSTRAINT document_line_share_mate_owner_document_line_id_fkey FOREIGN KEY (share_mate_owner_document_line_id) REFERENCES public.document_line(id);


--
-- Name: document_line document_line_site_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.document_line
    ADD CONSTRAINT document_line_site_id_fkey FOREIGN KEY (site_id) REFERENCES public.site(id);


--
-- Name: document_line document_line_stock_transfer_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.document_line
    ADD CONSTRAINT document_line_stock_transfer_id_fkey FOREIGN KEY (stock_transfer_id) REFERENCES public.stock_transfer(id);


--
-- Name: document_line document_line_vat_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.document_line
    ADD CONSTRAINT document_line_vat_id_fkey FOREIGN KEY (vat_id) REFERENCES public.vat(id);


--
-- Name: document document_multiple_booking_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.document
    ADD CONSTRAINT document_multiple_booking_id_fkey FOREIGN KEY (multiple_booking_id) REFERENCES public.multiple_booking(id);


--
-- Name: document document_not_multiple_booking_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.document
    ADD CONSTRAINT document_not_multiple_booking_id_fkey FOREIGN KEY (not_multiple_booking_id) REFERENCES public.multiple_booking(id) ON DELETE SET NULL;


--
-- Name: document document_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.document
    ADD CONSTRAINT document_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES public.organization(id);


--
-- Name: document document_person_branch_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.document
    ADD CONSTRAINT document_person_branch_id_fkey FOREIGN KEY (person_branch_id) REFERENCES public.organization(id);


--
-- Name: document document_person_carer1_document_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.document
    ADD CONSTRAINT document_person_carer1_document_id_fkey FOREIGN KEY (person_carer1_document_id) REFERENCES public.document(id);


--
-- Name: document document_person_carer1_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.document
    ADD CONSTRAINT document_person_carer1_id_fkey FOREIGN KEY (person_carer1_id) REFERENCES public.person(id);


--
-- Name: document document_person_carer2_document_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.document
    ADD CONSTRAINT document_person_carer2_document_id_fkey FOREIGN KEY (person_carer2_document_id) REFERENCES public.document(id);


--
-- Name: document document_person_carer2_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.document
    ADD CONSTRAINT document_person_carer2_id_fkey FOREIGN KEY (person_carer2_id) REFERENCES public.person(id);


--
-- Name: document document_person_geo_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.document
    ADD CONSTRAINT document_person_geo_country_id_fkey FOREIGN KEY (person_country_id) REFERENCES public.country(id);


--
-- Name: document document_person_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.document
    ADD CONSTRAINT document_person_id_fkey FOREIGN KEY (person_id) REFERENCES public.person(id) ON DELETE SET NULL;


--
-- Name: document document_person_language_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.document
    ADD CONSTRAINT document_person_language_id_fkey FOREIGN KEY (person_language_id) REFERENCES public.language(id);


--
-- Name: document document_person_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.document
    ADD CONSTRAINT document_person_organization_id_fkey FOREIGN KEY (person_organization_id) REFERENCES public.organization(id);


--
-- Name: document document_person_third_party_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.document
    ADD CONSTRAINT document_person_third_party_id_fkey FOREIGN KEY (person_third_party_id) REFERENCES public.third_party(id);


--
-- Name: document document_third_party_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.document
    ADD CONSTRAINT document_third_party_id_fkey FOREIGN KEY (third_party_id) REFERENCES public.third_party(id);


--
-- Name: document document_trigger_send_letter_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.document
    ADD CONSTRAINT document_trigger_send_letter_id_fkey FOREIGN KEY (trigger_send_letter_id) REFERENCES public.letter(id);


--
-- Name: document document_trigger_send_system_letter_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.document
    ADD CONSTRAINT document_trigger_send_system_letter_id_fkey FOREIGN KEY (trigger_send_system_letter_id) REFERENCES public.letter(id);


--
-- Name: domain domain_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.domain
    ADD CONSTRAINT domain_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES public.organization(id);


--
-- Name: driver driver_person_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.driver
    ADD CONSTRAINT driver_person_id_fkey FOREIGN KEY (person_id) REFERENCES public.person(id);


--
-- Name: enqueued_request enqueued_request_document_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.enqueued_request
    ADD CONSTRAINT enqueued_request_document_id_fkey FOREIGN KEY (document_id) REFERENCES public.document(id);


--
-- Name: event event_activity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.event
    ADD CONSTRAINT event_activity_id_fkey FOREIGN KEY (activity_id) REFERENCES public.activity(id);


--
-- Name: event event_booking_form_layout_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.event
    ADD CONSTRAINT event_booking_form_layout_id_fkey FOREIGN KEY (booking_form_layout_id) REFERENCES public.booking_form_layout(id);


--
-- Name: event event_booking_success_message_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.event
    ADD CONSTRAINT event_booking_success_message_id_fkey FOREIGN KEY (booking_success_message_id) REFERENCES public.label(id);


--
-- Name: event event_bookings_auto_confirm_event_id2_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.event
    ADD CONSTRAINT event_bookings_auto_confirm_event_id2_fk FOREIGN KEY (bookings_auto_confirm_event_id2) REFERENCES public.event(id);


--
-- Name: event event_bookings_auto_confirm_event_id3_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.event
    ADD CONSTRAINT event_bookings_auto_confirm_event_id3_fk FOREIGN KEY (bookings_auto_confirm_event_id3) REFERENCES public.event(id);


--
-- Name: event event_bookings_auto_confirm_event_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.event
    ADD CONSTRAINT event_bookings_auto_confirm_event_id_fk FOREIGN KEY (bookings_auto_confirm_event_id1) REFERENCES public.event(id);


--
-- Name: event event_bookings_auto_confirm_letter_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.event
    ADD CONSTRAINT event_bookings_auto_confirm_letter_id_fkey FOREIGN KEY (bookings_auto_confirm_letter_id) REFERENCES public.letter(id);


--
-- Name: event event_buddha_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.event
    ADD CONSTRAINT event_buddha_id_fkey FOREIGN KEY (buddha_id) REFERENCES public.buddha(id);


--
-- Name: event event_cart_message_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.event
    ADD CONSTRAINT event_cart_message_id_fkey FOREIGN KEY (cart_message_id) REFERENCES public.label(id);


--
-- Name: event event_corporation_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.event
    ADD CONSTRAINT event_corporation_id_fkey FOREIGN KEY (corporation_id) REFERENCES public.organization(id);


--
-- Name: event event_fees_bottom_label_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.event
    ADD CONSTRAINT event_fees_bottom_label_id_fkey FOREIGN KEY (fees_bottom_label_id) REFERENCES public.label(id);


--
-- Name: event event_image_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.event
    ADD CONSTRAINT event_image_id_fkey FOREIGN KEY (image_id) REFERENCES public.image(id);


--
-- Name: event event_label_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.event
    ADD CONSTRAINT event_label_id_fkey FOREIGN KEY (label_id) REFERENCES public.label(id);


--
-- Name: event event_long_description_label_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.event
    ADD CONSTRAINT event_long_description_label_id_fkey FOREIGN KEY (long_description_label_id) REFERENCES public.label(id);


--
-- Name: event event_notification_cart_confirmed_label_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.event
    ADD CONSTRAINT event_notification_cart_confirmed_label_id_fk FOREIGN KEY (notification_cart_confirmed_label_id) REFERENCES public.label(id);


--
-- Name: event event_notification_cart_default_label_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.event
    ADD CONSTRAINT event_notification_cart_default_label_id_fk FOREIGN KEY (notification_cart_default_label_id) REFERENCES public.label(id);


--
-- Name: event event_notification_cart_payed_confirmed_label_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.event
    ADD CONSTRAINT event_notification_cart_payed_confirmed_label_id_fk FOREIGN KEY (notification_cart_payed_confirmed_label_id) REFERENCES public.label(id);


--
-- Name: event event_notification_cart_payed_label_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.event
    ADD CONSTRAINT event_notification_cart_payed_label_id_fk FOREIGN KEY (notification_cart_payed_label_id) REFERENCES public.label(id);


--
-- Name: event event_options_top_label_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.event
    ADD CONSTRAINT event_options_top_label_id_fkey FOREIGN KEY (options_top_label_id) REFERENCES public.label(id);


--
-- Name: event event_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.event
    ADD CONSTRAINT event_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES public.organization(id);


--
-- Name: event event_short_description_label_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.event
    ADD CONSTRAINT event_short_description_label_id_fkey FOREIGN KEY (short_description_label_id) REFERENCES public.label(id);


--
-- Name: event event_support_notice_label_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.event
    ADD CONSTRAINT event_support_notice_label_id_fkey FOREIGN KEY (support_notice_label_id) REFERENCES public.label(id);


--
-- Name: event event_teacher_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.event
    ADD CONSTRAINT event_teacher_id_fkey FOREIGN KEY (teacher_id) REFERENCES public.teacher(id);


--
-- Name: event event_type_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.event
    ADD CONSTRAINT event_type_id_fkey FOREIGN KEY (type_id) REFERENCES public.event_type(id);


--
-- Name: event_type event_type_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.event_type
    ADD CONSTRAINT event_type_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES public.organization(id);


--
-- Name: frontend_account frontend_account_corporation_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.frontend_account
    ADD CONSTRAINT frontend_account_corporation_id_fkey FOREIGN KEY (corporation_id) REFERENCES public.organization(id);


--
-- Name: frontend_account frontend_account_trigger_send_password_event_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.frontend_account
    ADD CONSTRAINT frontend_account_trigger_send_password_event_id_fkey FOREIGN KEY (trigger_send_password_event_id) REFERENCES public.event(id);


--
-- Name: gateway_company gateway_company_alert_before_payment_label_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.gateway_company
    ADD CONSTRAINT gateway_company_alert_before_payment_label_id_fkey FOREIGN KEY (alert_before_payment_label_id) REFERENCES public.label(id);


--
-- Name: gateway_company gateway_company_information_label_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.gateway_company
    ADD CONSTRAINT gateway_company_information_label_id_fkey FOREIGN KEY (information_label_id) REFERENCES public.label(id);


--
-- Name: gateway_parameter gateway_parameter_account_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.gateway_parameter
    ADD CONSTRAINT gateway_parameter_account_id_fkey FOREIGN KEY (account_id) REFERENCES public.money_account(id);


--
-- Name: gateway_parameter gateway_parameter_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.gateway_parameter
    ADD CONSTRAINT gateway_parameter_company_id_fkey FOREIGN KEY (company_id) REFERENCES public.gateway_company(id);


--
-- Name: history history_document_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.history
    ADD CONSTRAINT history_document_id_fkey FOREIGN KEY (document_id) REFERENCES public.document(id) ON DELETE CASCADE;


--
-- Name: history history_mail_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.history
    ADD CONSTRAINT history_mail_id_fkey FOREIGN KEY (mail_id) REFERENCES public.mail(id) ON DELETE SET NULL;


--
-- Name: history history_money_transfer_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.history
    ADD CONSTRAINT history_money_transfer_id_fkey FOREIGN KEY (money_transfer_id) REFERENCES public.money_transfer(id) ON DELETE SET NULL;


--
-- Name: history history_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.history
    ADD CONSTRAINT history_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.person(id);


--
-- Name: item_family item_family_account_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.item_family
    ADD CONSTRAINT item_family_account_id_fkey FOREIGN KEY (account_id) REFERENCES public.accounting_account(id);


--
-- Name: item item_family_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.item
    ADD CONSTRAINT item_family_id_fkey FOREIGN KEY (family_id) REFERENCES public.item_family(id);


--
-- Name: item_family item_family_label_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.item_family
    ADD CONSTRAINT item_family_label_id_fkey FOREIGN KEY (label_id) REFERENCES public.label(id);


--
-- Name: item_family item_family_per_resource_label_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.item_family
    ADD CONSTRAINT item_family_per_resource_label_id_fkey FOREIGN KEY (per_resource_label_id) REFERENCES public.label(id);


--
-- Name: item_family item_family_sector_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.item_family
    ADD CONSTRAINT item_family_sector_id_fkey FOREIGN KEY (sector_id) REFERENCES public.sector(id);


--
-- Name: item item_label_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.item
    ADD CONSTRAINT item_label_id_fkey FOREIGN KEY (label_id) REFERENCES public.label(id);


--
-- Name: item item_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.item
    ADD CONSTRAINT item_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES public.organization(id) ON DELETE CASCADE;


--
-- Name: item item_per_resource_label_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.item
    ADD CONSTRAINT item_per_resource_label_id_fkey FOREIGN KEY (per_resource_label_id) REFERENCES public.label(id);


--
-- Name: item item_rate_alias_item_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.item
    ADD CONSTRAINT item_rate_alias_item_id_fkey FOREIGN KEY (rate_alias_item_id) REFERENCES public.item(id);


--
-- Name: label label_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.label
    ADD CONSTRAINT label_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES public.organization(id);


--
-- Name: language language_main_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.language
    ADD CONSTRAINT language_main_country_id_fkey FOREIGN KEY (main_country_id) REFERENCES public.country(id);


--
-- Name: letter letter_account_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.letter
    ADD CONSTRAINT letter_account_id_fkey FOREIGN KEY (account_id) REFERENCES public.mail_account(id);


--
-- Name: letter letter_event_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.letter
    ADD CONSTRAINT letter_event_id_fkey FOREIGN KEY (event_id) REFERENCES public.event(id);


--
-- Name: letter letter_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.letter
    ADD CONSTRAINT letter_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES public.organization(id);


--
-- Name: letter letter_type_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.letter
    ADD CONSTRAINT letter_type_id_fkey FOREIGN KEY (type_id) REFERENCES public.letter_type(id);


--
-- Name: letter_type letter_type_label_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.letter_type
    ADD CONSTRAINT letter_type_label_id_fkey FOREIGN KEY (label_id) REFERENCES public.label(id);


--
-- Name: lt_test_event lt_test_event_lt_test_set_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.lt_test_event
    ADD CONSTRAINT lt_test_event_lt_test_set_id_fkey FOREIGN KEY (lt_test_set_id) REFERENCES public.lt_test_set(id);


--
-- Name: mail_account mail_account_event_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.mail_account
    ADD CONSTRAINT mail_account_event_id_fkey FOREIGN KEY (event_id) REFERENCES public.event(id);


--
-- Name: mail mail_account_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.mail
    ADD CONSTRAINT mail_account_id_fkey FOREIGN KEY (account_id) REFERENCES public.mail_account(id);


--
-- Name: mail_account mail_account_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.mail_account
    ADD CONSTRAINT mail_account_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES public.organization(id);


--
-- Name: mail_account mail_account_signature_label_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.mail_account
    ADD CONSTRAINT mail_account_signature_label_id_fkey FOREIGN KEY (signature_label_id) REFERENCES public.label(id) ON DELETE SET NULL;


--
-- Name: mail_account mail_account_smtp_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.mail_account
    ADD CONSTRAINT mail_account_smtp_id_fkey FOREIGN KEY (smtp_account_id) REFERENCES public.smtp_account(id);


--
-- Name: mail mail_document_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.mail
    ADD CONSTRAINT mail_document_id_fkey FOREIGN KEY (document_id) REFERENCES public.document(id) ON DELETE CASCADE;


--
-- Name: mail mail_letter_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.mail
    ADD CONSTRAINT mail_letter_id_fkey FOREIGN KEY (letter_id) REFERENCES public.letter(id);


--
-- Name: method method_label_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.method
    ADD CONSTRAINT method_label_id_fkey FOREIGN KEY (label_id) REFERENCES public.label(id);


--
-- Name: method_support method_support_method_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.method_support
    ADD CONSTRAINT method_support_method_id_fkey FOREIGN KEY (method_id) REFERENCES public.method(id);


--
-- Name: method_support method_support_money_account_type_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.method_support
    ADD CONSTRAINT method_support_money_account_type_id_fkey FOREIGN KEY (money_account_type_id) REFERENCES public.money_account_type(id);


--
-- Name: metrics metrics_lt_test_set_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.metrics
    ADD CONSTRAINT metrics_lt_test_set_id_fkey FOREIGN KEY (lt_test_set_id) REFERENCES public.lt_test_set(id);


--
-- Name: money_account money_account_bank_system_account_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.money_account
    ADD CONSTRAINT money_account_bank_system_account_id_fkey FOREIGN KEY (bank_system_account_id) REFERENCES public.bank_system_account(id);


--
-- Name: money_account money_account_currency_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.money_account
    ADD CONSTRAINT money_account_currency_id_fkey FOREIGN KEY (currency_id) REFERENCES public.currency(id);


--
-- Name: money_account money_account_event_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.money_account
    ADD CONSTRAINT money_account_event_id_fkey FOREIGN KEY (event_id) REFERENCES public.event(id);


--
-- Name: money_account money_account_gateway_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.money_account
    ADD CONSTRAINT money_account_gateway_company_id_fkey FOREIGN KEY (gateway_company_id) REFERENCES public.gateway_company(id);


--
-- Name: money_account money_account_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.money_account
    ADD CONSTRAINT money_account_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES public.organization(id);


--
-- Name: money_account money_account_third_party_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.money_account
    ADD CONSTRAINT money_account_third_party_id_fkey FOREIGN KEY (third_party_id) REFERENCES public.third_party(id);


--
-- Name: money_account money_account_type_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.money_account
    ADD CONSTRAINT money_account_type_id_fkey FOREIGN KEY (type_id) REFERENCES public.money_account_type(id);


--
-- Name: money_account_type money_account_type_label_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.money_account_type
    ADD CONSTRAINT money_account_type_label_id_fkey FOREIGN KEY (label_id) REFERENCES public.label(id);


--
-- Name: money_flow money_flow_from_money_account_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.money_flow
    ADD CONSTRAINT money_flow_from_money_account_id_fkey FOREIGN KEY (from_money_account_id) REFERENCES public.money_account(id);


--
-- Name: money_flow money_flow_method_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.money_flow
    ADD CONSTRAINT money_flow_method_id_fkey FOREIGN KEY (method_id) REFERENCES public.method(id);


--
-- Name: money_flow money_flow_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.money_flow
    ADD CONSTRAINT money_flow_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES public.organization(id);


--
-- Name: money_flow_priority money_flow_priority_flow_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.money_flow_priority
    ADD CONSTRAINT money_flow_priority_flow_id_fkey FOREIGN KEY (flow_id) REFERENCES public.money_flow(id) ON DELETE CASCADE;


--
-- Name: money_flow money_flow_to_money_account_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.money_flow
    ADD CONSTRAINT money_flow_to_money_account_id_fkey FOREIGN KEY (to_money_account_id) REFERENCES public.money_account(id);


--
-- Name: money_flow_type money_flow_type_from_money_account_type_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.money_flow_type
    ADD CONSTRAINT money_flow_type_from_money_account_type_id_fkey FOREIGN KEY (from_money_account_type_id) REFERENCES public.money_account_type(id);


--
-- Name: money_flow_type money_flow_type_to_money_account_type_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.money_flow_type
    ADD CONSTRAINT money_flow_type_to_money_account_type_id_fkey FOREIGN KEY (to_money_account_type_id) REFERENCES public.money_account_type(id);


--
-- Name: money_statement money_statement_account_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.money_statement
    ADD CONSTRAINT money_statement_account_id_fkey FOREIGN KEY (account_id) REFERENCES public.money_account(id);


--
-- Name: money_statement money_statement_system_acount_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.money_statement
    ADD CONSTRAINT money_statement_system_acount_id_fkey FOREIGN KEY (system_acount_id) REFERENCES public.bank_system_account(id);


--
-- Name: money_statement money_statement_transfer_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.money_statement
    ADD CONSTRAINT money_statement_transfer_id_fkey FOREIGN KEY (transfer_id) REFERENCES public.money_transfer(id);


--
-- Name: money_transfer money_transfer_document_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.money_transfer
    ADD CONSTRAINT money_transfer_document_id_fkey FOREIGN KEY (document_id) REFERENCES public.document(id) ON DELETE CASCADE;


--
-- Name: money_transfer money_transfer_from_money_account_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.money_transfer
    ADD CONSTRAINT money_transfer_from_money_account_id_fkey FOREIGN KEY (from_money_account_id) REFERENCES public.money_account(id);


--
-- Name: money_transfer money_transfer_gateway_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.money_transfer
    ADD CONSTRAINT money_transfer_gateway_company_id_fkey FOREIGN KEY (gateway_company_id) REFERENCES public.gateway_company(id);


--
-- Name: money_transfer money_transfer_method_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.money_transfer
    ADD CONSTRAINT money_transfer_method_id_fkey FOREIGN KEY (method_id) REFERENCES public.method(id);


--
-- Name: money_transfer money_transfer_parent_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.money_transfer
    ADD CONSTRAINT money_transfer_parent_id_fkey FOREIGN KEY (parent_id) REFERENCES public.money_transfer(id) ON DELETE CASCADE;


--
-- Name: money_transfer money_transfer_statement_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.money_transfer
    ADD CONSTRAINT money_transfer_statement_id_fkey FOREIGN KEY (statement_id) REFERENCES public.money_statement(id);


--
-- Name: money_transfer money_transfer_to_money_account_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.money_transfer
    ADD CONSTRAINT money_transfer_to_money_account_id_fkey FOREIGN KEY (to_money_account_id) REFERENCES public.money_account(id);


--
-- Name: money_transfer money_transfer_transfer_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.money_transfer
    ADD CONSTRAINT money_transfer_transfer_id_fkey FOREIGN KEY (transfer_id) REFERENCES public.money_transfer(id) ON DELETE SET NULL;


--
-- Name: multiple_booking multiple_booking_event_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.multiple_booking
    ADD CONSTRAINT multiple_booking_event_id_fkey FOREIGN KEY (event_id) REFERENCES public.event(id);


--
-- Name: operation operation_label_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.operation
    ADD CONSTRAINT operation_label_id_fkey FOREIGN KEY (label_id) REFERENCES public.label(id);


--
-- Name: option option_age_error_label_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option
    ADD CONSTRAINT option_age_error_label_id_fkey FOREIGN KEY (age_error_label_id) REFERENCES public.label(id);


--
-- Name: option option_age_error_label_id_fkey1; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option
    ADD CONSTRAINT option_age_error_label_id_fkey1 FOREIGN KEY (age_error_label_id) REFERENCES public.label(id);


--
-- Name: option option_age_error_label_id_fkey2; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option
    ADD CONSTRAINT option_age_error_label_id_fkey2 FOREIGN KEY (age_error_label_id) REFERENCES public.label(id);


--
-- Name: option option_arrival_site_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option
    ADD CONSTRAINT option_arrival_site_id_fkey FOREIGN KEY (arrival_site_id) REFERENCES public.site(id);


--
-- Name: option option_attendance_option_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option
    ADD CONSTRAINT option_attendance_option_id_fkey FOREIGN KEY (attendance_option_id) REFERENCES public.option(id);


--
-- Name: option option_bottom_label_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option
    ADD CONSTRAINT option_bottom_label_id_fkey FOREIGN KEY (bottom_label_id) REFERENCES public.label(id);


--
-- Name: option option_children_prompt_label_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option
    ADD CONSTRAINT option_children_prompt_label_id_fkey FOREIGN KEY (children_prompt_label_id) REFERENCES public.label(id);


--
-- Name: option option_comment_label_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option
    ADD CONSTRAINT option_comment_label_id_fkey FOREIGN KEY (comment_label_id) REFERENCES public.label(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey1; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey1 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey10; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey10 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey11; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey11 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey12; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey12 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey13; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey13 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey14; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey14 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey15; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey15 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey16; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey16 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey17; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey17 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey18; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey18 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey19; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey19 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey2; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey2 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey20; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey20 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey21; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey21 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey22; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey22 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey23; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey23 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey24; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey24 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey25; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey25 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey26; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey26 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey27; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey27 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey28; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey28 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey29; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey29 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey3; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey3 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey30; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey30 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey31; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey31 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey32; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey32 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey33; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey33 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey34; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey34 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey35; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey35 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey36; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey36 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey37; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey37 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey38; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey38 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey39; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey39 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey4; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey4 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey40; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey40 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey41; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey41 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey42; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey42 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey43; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey43 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey44; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey44 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey45; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey45 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey46; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey46 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey47; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey47 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey48; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey48 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey5; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey5 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey6; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey6 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey7; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey7 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey8; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey8 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_item_family_id_fkey9; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_item_family_id_fkey9 FOREIGN KEY (if_item_family_id) REFERENCES public.item_family(id);


--
-- Name: option_condition option_condition_if_option_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_option_id_fkey FOREIGN KEY (if_option_id) REFERENCES public.option(id) ON DELETE CASCADE;


--
-- Name: option_condition option_condition_if_site_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey1; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey1 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey10; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey10 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey11; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey11 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey12; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey12 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey13; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey13 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey14; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey14 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey15; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey15 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey16; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey16 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey17; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey17 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey18; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey18 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey19; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey19 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey2; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey2 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey20; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey20 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey21; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey21 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey22; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey22 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey23; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey23 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey24; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey24 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey25; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey25 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey26; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey26 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey27; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey27 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey28; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey28 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey29; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey29 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey3; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey3 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey30; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey30 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey31; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey31 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey32; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey32 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey33; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey33 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey34; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey34 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey35; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey35 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey36; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey36 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey37; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey37 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey38; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey38 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey39; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey39 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey4; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey4 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey40; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey40 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey41; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey41 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey42; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey42 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey43; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey43 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey44; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey44 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey45; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey45 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey46; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey46 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey47; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey47 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey48; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey48 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey5; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey5 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey6; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey6 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey7; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey7 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey8; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey8 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_if_site_id_fkey9; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_if_site_id_fkey9 FOREIGN KEY (if_site_id) REFERENCES public.site(id);


--
-- Name: option_condition option_condition_option_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_option_id_fkey FOREIGN KEY (option_id) REFERENCES public.option(id) ON DELETE CASCADE;


--
-- Name: option_condition option_condition_parent_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option_condition
    ADD CONSTRAINT option_condition_parent_id_fkey FOREIGN KEY (parent_id) REFERENCES public.option_condition(id);


--
-- Name: option option_event_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option
    ADD CONSTRAINT option_event_id_fkey FOREIGN KEY (event_id) REFERENCES public.event(id);


--
-- Name: option option_footer_label_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option
    ADD CONSTRAINT option_footer_label_id_fkey FOREIGN KEY (footer_label_id) REFERENCES public.label(id);


--
-- Name: option option_item_family_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option
    ADD CONSTRAINT option_item_family_id_fkey FOREIGN KEY (item_family_id) REFERENCES public.item_family(id);


--
-- Name: option option_item_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option
    ADD CONSTRAINT option_item_id_fkey FOREIGN KEY (item_id) REFERENCES public.item(id);


--
-- Name: option option_label_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option
    ADD CONSTRAINT option_label_id_fkey FOREIGN KEY (label_id) REFERENCES public.label(id);


--
-- Name: option option_parent_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option
    ADD CONSTRAINT option_parent_id_fkey FOREIGN KEY (parent_id) REFERENCES public.option(id) ON DELETE CASCADE;


--
-- Name: option option_popup_label_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option
    ADD CONSTRAINT option_popup_label_id_fkey FOREIGN KEY (popup_label_id) REFERENCES public.label(id);


--
-- Name: option option_prompt_label_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option
    ADD CONSTRAINT option_prompt_label_id_fkey FOREIGN KEY (prompt_label_id) REFERENCES public.label(id);


--
-- Name: option option_quantity_label_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option
    ADD CONSTRAINT option_quantity_label_id_fkey FOREIGN KEY (quantity_label_id) REFERENCES public.label(id);


--
-- Name: option option_rate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option
    ADD CONSTRAINT option_rate_id_fkey FOREIGN KEY (rate_id) REFERENCES public.rate(id);


--
-- Name: option option_sharing_item_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option
    ADD CONSTRAINT option_sharing_item_id_fkey FOREIGN KEY (sharing_item_id) REFERENCES public.item(id);


--
-- Name: option option_site_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option
    ADD CONSTRAINT option_site_id_fkey FOREIGN KEY (site_id) REFERENCES public.site(id);


--
-- Name: option option_top_label_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.option
    ADD CONSTRAINT option_top_label_id_fkey FOREIGN KEY (top_label_id) REFERENCES public.label(id);


--
-- Name: organization organization_geo_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.organization
    ADD CONSTRAINT organization_geo_country_id_fkey FOREIGN KEY (country_id) REFERENCES public.country(id);


--
-- Name: organization organization_teachings_day_ticket_item_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.organization
    ADD CONSTRAINT organization_teachings_day_ticket_item_id_fkey FOREIGN KEY (teachings_day_ticket_item_id) REFERENCES public.item(id);


--
-- Name: organization organization_type_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.organization
    ADD CONSTRAINT organization_type_id_fkey FOREIGN KEY (type_id) REFERENCES public.organization_type(id);


--
-- Name: organization_type organization_type_label_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.organization_type
    ADD CONSTRAINT organization_type_label_id_fkey FOREIGN KEY (label_id) REFERENCES public.label(id);


--
-- Name: package_item package_item_item_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.package_item
    ADD CONSTRAINT package_item_item_id_fkey FOREIGN KEY (item_id) REFERENCES public.item(id);


--
-- Name: package_item package_item_package_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.package_item
    ADD CONSTRAINT package_item_package_id_fkey FOREIGN KEY (package_id) REFERENCES public.package(id);


--
-- Name: person person_branch_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.person
    ADD CONSTRAINT person_branch_id_fkey FOREIGN KEY (branch_id) REFERENCES public.organization(id);


--
-- Name: person person_carer1_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.person
    ADD CONSTRAINT person_carer1_id_fkey FOREIGN KEY (carer1_id) REFERENCES public.person(id);


--
-- Name: person person_carer2_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.person
    ADD CONSTRAINT person_carer2_id_fkey FOREIGN KEY (carer2_id) REFERENCES public.person(id);


--
-- Name: person person_frontend_account_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.person
    ADD CONSTRAINT person_frontend_account_id_fkey FOREIGN KEY (frontend_account_id) REFERENCES public.frontend_account(id) ON DELETE SET NULL;


--
-- Name: person person_geo_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.person
    ADD CONSTRAINT person_geo_country_id_fkey FOREIGN KEY (country_id) REFERENCES public.country(id);


--
-- Name: person person_language_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.person
    ADD CONSTRAINT person_language_id_fkey FOREIGN KEY (language_id) REFERENCES public.language(id);


--
-- Name: person person_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.person
    ADD CONSTRAINT person_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES public.organization(id);


--
-- Name: person person_third_party_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.person
    ADD CONSTRAINT person_third_party_id_fkey FOREIGN KEY (third_party_id) REFERENCES public.third_party(id);


--
-- Name: rate rate_arrival_site_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_arrival_site_id_fkey FOREIGN KEY (arrival_site_id) REFERENCES public.site(id);


--
-- Name: rate rate_comment_label_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_comment_label_id_fkey FOREIGN KEY (comment_label_id) REFERENCES public.label(id);


--
-- Name: rate rate_comment_label_id_fkey1; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_comment_label_id_fkey1 FOREIGN KEY (comment_label_id) REFERENCES public.label(id);


--
-- Name: rate rate_comment_label_id_fkey10; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_comment_label_id_fkey10 FOREIGN KEY (comment_label_id) REFERENCES public.label(id);


--
-- Name: rate rate_comment_label_id_fkey11; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_comment_label_id_fkey11 FOREIGN KEY (comment_label_id) REFERENCES public.label(id);


--
-- Name: rate rate_comment_label_id_fkey12; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_comment_label_id_fkey12 FOREIGN KEY (comment_label_id) REFERENCES public.label(id);


--
-- Name: rate rate_comment_label_id_fkey13; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_comment_label_id_fkey13 FOREIGN KEY (comment_label_id) REFERENCES public.label(id);


--
-- Name: rate rate_comment_label_id_fkey14; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_comment_label_id_fkey14 FOREIGN KEY (comment_label_id) REFERENCES public.label(id);


--
-- Name: rate rate_comment_label_id_fkey15; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_comment_label_id_fkey15 FOREIGN KEY (comment_label_id) REFERENCES public.label(id);


--
-- Name: rate rate_comment_label_id_fkey16; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_comment_label_id_fkey16 FOREIGN KEY (comment_label_id) REFERENCES public.label(id);


--
-- Name: rate rate_comment_label_id_fkey17; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_comment_label_id_fkey17 FOREIGN KEY (comment_label_id) REFERENCES public.label(id);


--
-- Name: rate rate_comment_label_id_fkey18; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_comment_label_id_fkey18 FOREIGN KEY (comment_label_id) REFERENCES public.label(id);


--
-- Name: rate rate_comment_label_id_fkey19; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_comment_label_id_fkey19 FOREIGN KEY (comment_label_id) REFERENCES public.label(id);


--
-- Name: rate rate_comment_label_id_fkey2; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_comment_label_id_fkey2 FOREIGN KEY (comment_label_id) REFERENCES public.label(id);


--
-- Name: rate rate_comment_label_id_fkey20; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_comment_label_id_fkey20 FOREIGN KEY (comment_label_id) REFERENCES public.label(id);


--
-- Name: rate rate_comment_label_id_fkey21; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_comment_label_id_fkey21 FOREIGN KEY (comment_label_id) REFERENCES public.label(id);


--
-- Name: rate rate_comment_label_id_fkey22; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_comment_label_id_fkey22 FOREIGN KEY (comment_label_id) REFERENCES public.label(id);


--
-- Name: rate rate_comment_label_id_fkey23; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_comment_label_id_fkey23 FOREIGN KEY (comment_label_id) REFERENCES public.label(id);


--
-- Name: rate rate_comment_label_id_fkey24; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_comment_label_id_fkey24 FOREIGN KEY (comment_label_id) REFERENCES public.label(id);


--
-- Name: rate rate_comment_label_id_fkey25; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_comment_label_id_fkey25 FOREIGN KEY (comment_label_id) REFERENCES public.label(id);


--
-- Name: rate rate_comment_label_id_fkey26; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_comment_label_id_fkey26 FOREIGN KEY (comment_label_id) REFERENCES public.label(id);


--
-- Name: rate rate_comment_label_id_fkey27; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_comment_label_id_fkey27 FOREIGN KEY (comment_label_id) REFERENCES public.label(id);


--
-- Name: rate rate_comment_label_id_fkey28; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_comment_label_id_fkey28 FOREIGN KEY (comment_label_id) REFERENCES public.label(id);


--
-- Name: rate rate_comment_label_id_fkey29; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_comment_label_id_fkey29 FOREIGN KEY (comment_label_id) REFERENCES public.label(id);


--
-- Name: rate rate_comment_label_id_fkey3; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_comment_label_id_fkey3 FOREIGN KEY (comment_label_id) REFERENCES public.label(id);


--
-- Name: rate rate_comment_label_id_fkey30; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_comment_label_id_fkey30 FOREIGN KEY (comment_label_id) REFERENCES public.label(id);


--
-- Name: rate rate_comment_label_id_fkey31; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_comment_label_id_fkey31 FOREIGN KEY (comment_label_id) REFERENCES public.label(id);


--
-- Name: rate rate_comment_label_id_fkey32; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_comment_label_id_fkey32 FOREIGN KEY (comment_label_id) REFERENCES public.label(id);


--
-- Name: rate rate_comment_label_id_fkey33; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_comment_label_id_fkey33 FOREIGN KEY (comment_label_id) REFERENCES public.label(id);


--
-- Name: rate rate_comment_label_id_fkey34; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_comment_label_id_fkey34 FOREIGN KEY (comment_label_id) REFERENCES public.label(id);


--
-- Name: rate rate_comment_label_id_fkey35; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_comment_label_id_fkey35 FOREIGN KEY (comment_label_id) REFERENCES public.label(id);


--
-- Name: rate rate_comment_label_id_fkey36; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_comment_label_id_fkey36 FOREIGN KEY (comment_label_id) REFERENCES public.label(id);


--
-- Name: rate rate_comment_label_id_fkey37; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_comment_label_id_fkey37 FOREIGN KEY (comment_label_id) REFERENCES public.label(id);


--
-- Name: rate rate_comment_label_id_fkey38; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_comment_label_id_fkey38 FOREIGN KEY (comment_label_id) REFERENCES public.label(id);


--
-- Name: rate rate_comment_label_id_fkey39; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_comment_label_id_fkey39 FOREIGN KEY (comment_label_id) REFERENCES public.label(id);


--
-- Name: rate rate_comment_label_id_fkey4; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_comment_label_id_fkey4 FOREIGN KEY (comment_label_id) REFERENCES public.label(id);


--
-- Name: rate rate_comment_label_id_fkey40; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_comment_label_id_fkey40 FOREIGN KEY (comment_label_id) REFERENCES public.label(id);


--
-- Name: rate rate_comment_label_id_fkey41; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_comment_label_id_fkey41 FOREIGN KEY (comment_label_id) REFERENCES public.label(id);


--
-- Name: rate rate_comment_label_id_fkey5; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_comment_label_id_fkey5 FOREIGN KEY (comment_label_id) REFERENCES public.label(id);


--
-- Name: rate rate_comment_label_id_fkey6; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_comment_label_id_fkey6 FOREIGN KEY (comment_label_id) REFERENCES public.label(id);


--
-- Name: rate rate_comment_label_id_fkey7; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_comment_label_id_fkey7 FOREIGN KEY (comment_label_id) REFERENCES public.label(id);


--
-- Name: rate rate_comment_label_id_fkey8; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_comment_label_id_fkey8 FOREIGN KEY (comment_label_id) REFERENCES public.label(id);


--
-- Name: rate rate_comment_label_id_fkey9; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_comment_label_id_fkey9 FOREIGN KEY (comment_label_id) REFERENCES public.label(id);


--
-- Name: rate rate_currency_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_currency_id_fkey FOREIGN KEY (currency_id) REFERENCES public.currency(id);


--
-- Name: rate rate_item_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_item_id_fkey FOREIGN KEY (item_id) REFERENCES public.item(id);


--
-- Name: rate rate_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES public.organization(id) ON DELETE CASCADE;


--
-- Name: rate rate_package_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_package_id_fkey FOREIGN KEY (package_id) REFERENCES public.package(id);


--
-- Name: rate rate_site_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_site_id_fkey FOREIGN KEY (site_id) REFERENCES public.site(id) ON DELETE CASCADE;


--
-- Name: rate rate_vat_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.rate
    ADD CONSTRAINT rate_vat_id_fkey FOREIGN KEY (vat_id) REFERENCES public.vat(id);


--
-- Name: recipient recipient_person_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.recipient
    ADD CONSTRAINT recipient_person_id_fkey FOREIGN KEY (person_id) REFERENCES public.person(id) ON DELETE SET NULL;


--
-- Name: resource resource_building_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.resource
    ADD CONSTRAINT resource_building_id_fkey FOREIGN KEY (building_id) REFERENCES public.building(id);


--
-- Name: resource_configuration resource_configuration_item_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.resource_configuration
    ADD CONSTRAINT resource_configuration_item_id_fkey FOREIGN KEY (item_id) REFERENCES public.item(id);


--
-- Name: resource_configuration resource_configuration_resource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.resource_configuration
    ADD CONSTRAINT resource_configuration_resource_id_fkey FOREIGN KEY (resource_id) REFERENCES public.resource(id) ON DELETE CASCADE;


--
-- Name: resource resource_site_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.resource
    ADD CONSTRAINT resource_site_id_fkey FOREIGN KEY (site_id) REFERENCES public.site(id) ON DELETE CASCADE;


--
-- Name: resource resource_site_item_family_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.resource
    ADD CONSTRAINT resource_site_item_family_id_fkey FOREIGN KEY (site_item_family_id) REFERENCES public.site_item_family(id) ON DELETE CASCADE;


--
-- Name: role_attribution role_attribution_from_language_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.role_attribution
    ADD CONSTRAINT role_attribution_from_language_id_fkey FOREIGN KEY (from_language_id) REFERENCES public.language(id);


--
-- Name: role_attribution role_attribution_person_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.role_attribution
    ADD CONSTRAINT role_attribution_person_id_fkey FOREIGN KEY (person_id) REFERENCES public.person(id);


--
-- Name: role_attribution role_attribution_role_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.role_attribution
    ADD CONSTRAINT role_attribution_role_id_fkey FOREIGN KEY (role_id) REFERENCES public.role(id);


--
-- Name: role_attribution role_attribution_to_language_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.role_attribution
    ADD CONSTRAINT role_attribution_to_language_id_fkey FOREIGN KEY (to_language_id) REFERENCES public.language(id);


--
-- Name: role role_label_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.role
    ADD CONSTRAINT role_label_id_fkey FOREIGN KEY (label_id) REFERENCES public.label(id);


--
-- Name: session_agent session_agent_next_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.session_agent
    ADD CONSTRAINT session_agent_next_id_fkey FOREIGN KEY (next_id) REFERENCES public.session_agent(id);


--
-- Name: session_agent session_agent_previous_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.session_agent
    ADD CONSTRAINT session_agent_previous_id_fkey FOREIGN KEY (previous_id) REFERENCES public.session_agent(id);


--
-- Name: session_application session_application_agent_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.session_application
    ADD CONSTRAINT session_application_agent_id_fkey FOREIGN KEY (agent_id) REFERENCES public.session_agent(id);


--
-- Name: session_application session_application_next_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.session_application
    ADD CONSTRAINT session_application_next_id_fkey FOREIGN KEY (next_id) REFERENCES public.session_application(id);


--
-- Name: session_application session_application_previous_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.session_application
    ADD CONSTRAINT session_application_previous_id_fkey FOREIGN KEY (previous_id) REFERENCES public.session_application(id);


--
-- Name: session_connection session_connection_next_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.session_connection
    ADD CONSTRAINT session_connection_next_id_fkey FOREIGN KEY (next_id) REFERENCES public.session_connection(id);


--
-- Name: session_connection session_connection_previous_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.session_connection
    ADD CONSTRAINT session_connection_previous_id_fkey FOREIGN KEY (previous_id) REFERENCES public.session_connection(id);


--
-- Name: session_connection session_connection_process_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.session_connection
    ADD CONSTRAINT session_connection_process_id_fkey FOREIGN KEY (process_id) REFERENCES public.session_process(id);


--
-- Name: session_process session_process_application_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.session_process
    ADD CONSTRAINT session_process_application_id_fkey FOREIGN KEY (application_id) REFERENCES public.session_application(id);


--
-- Name: session_process session_process_next_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.session_process
    ADD CONSTRAINT session_process_next_id_fkey FOREIGN KEY (next_id) REFERENCES public.session_process(id);


--
-- Name: session_process session_process_previous_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.session_process
    ADD CONSTRAINT session_process_previous_id_fkey FOREIGN KEY (previous_id) REFERENCES public.session_process(id);


--
-- Name: session_user session_user_next_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public."session_user"
    ADD CONSTRAINT session_user_next_id_fkey FOREIGN KEY (next_id) REFERENCES public."session_user"(id);


--
-- Name: session_user session_user_previous_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public."session_user"
    ADD CONSTRAINT session_user_previous_id_fkey FOREIGN KEY (previous_id) REFERENCES public."session_user"(id);


--
-- Name: session_user session_user_process_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public."session_user"
    ADD CONSTRAINT session_user_process_id_fkey FOREIGN KEY (process_id) REFERENCES public.session_process(id);


--
-- Name: session_user session_user_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public."session_user"
    ADD CONSTRAINT session_user_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.person(id);


--
-- Name: site site_event_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.site
    ADD CONSTRAINT site_event_id_fkey FOREIGN KEY (event_id) REFERENCES public.event(id);


--
-- Name: site site_item_family_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.site
    ADD CONSTRAINT site_item_family_id_fkey FOREIGN KEY (item_family_id) REFERENCES public.item_family(id);


--
-- Name: site_item_family site_item_family_item_family_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.site_item_family
    ADD CONSTRAINT site_item_family_item_family_id_fkey FOREIGN KEY (item_family_id) REFERENCES public.item_family(id);


--
-- Name: site_item_family site_item_family_site_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.site_item_family
    ADD CONSTRAINT site_item_family_site_id_fkey FOREIGN KEY (site_id) REFERENCES public.site(id) ON DELETE CASCADE;


--
-- Name: site site_label_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.site
    ADD CONSTRAINT site_label_id_fkey FOREIGN KEY (label_id) REFERENCES public.label(id);


--
-- Name: site site_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.site
    ADD CONSTRAINT site_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES public.organization(id);


--
-- Name: site site_top_label_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.site
    ADD CONSTRAINT site_top_label_id_fkey FOREIGN KEY (top_label_id) REFERENCES public.label(id);


--
-- Name: smtp_account smtp_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.smtp_account
    ADD CONSTRAINT smtp_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES public.organization(id);


--
-- Name: smtp_account smtp_quota_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.smtp_account
    ADD CONSTRAINT smtp_quota_id_fkey FOREIGN KEY (quota_id) REFERENCES public.smtp_quota(id);


--
-- Name: stock stock_item_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.stock
    ADD CONSTRAINT stock_item_id_fkey FOREIGN KEY (item_id) REFERENCES public.item(id);


--
-- Name: stock stock_third_party_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.stock
    ADD CONSTRAINT stock_third_party_id_fkey FOREIGN KEY (third_party_id) REFERENCES public.third_party(id);


--
-- Name: stock_transfer stock_transfer_document_line_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.stock_transfer
    ADD CONSTRAINT stock_transfer_document_line_id_fkey FOREIGN KEY (document_line_id) REFERENCES public.document_line(id);


--
-- Name: stock_transfer stock_transfer_from_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.stock_transfer
    ADD CONSTRAINT stock_transfer_from_id_fkey FOREIGN KEY (from_id) REFERENCES public.stock(id);


--
-- Name: stock_transfer stock_transfer_provision_transfer_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.stock_transfer
    ADD CONSTRAINT stock_transfer_provision_transfer_id_fkey FOREIGN KEY (provision_transfer_id) REFERENCES public.stock_transfer(id);


--
-- Name: stock_transfer stock_transfer_to_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.stock_transfer
    ADD CONSTRAINT stock_transfer_to_id_fkey FOREIGN KEY (to_id) REFERENCES public.stock(id);


--
-- Name: stock stock_warehouse_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.stock
    ADD CONSTRAINT stock_warehouse_id_fkey FOREIGN KEY (warehouse_id) REFERENCES public.warehouse(id);


--
-- Name: subscription subscription_activity_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.subscription
    ADD CONSTRAINT subscription_activity_id_fkey FOREIGN KEY (activity_id) REFERENCES public.activity(id);


--
-- Name: subscription subscription_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.subscription
    ADD CONSTRAINT subscription_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES public.organization(id);


--
-- Name: subscription subscription_person_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.subscription
    ADD CONSTRAINT subscription_person_id_fkey FOREIGN KEY (person_id) REFERENCES public.person(id);


--
-- Name: subscription subscription_sector_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.subscription
    ADD CONSTRAINT subscription_sector_id_fkey FOREIGN KEY (sector_id) REFERENCES public.sector(id);


--
-- Name: teacher teacher_image_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.teacher
    ADD CONSTRAINT teacher_image_id_fkey FOREIGN KEY (image_id) REFERENCES public.image(id);


--
-- Name: teacher teacher_label_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.teacher
    ADD CONSTRAINT teacher_label_id_fkey FOREIGN KEY (label_id) REFERENCES public.label(id);


--
-- Name: teacher teacher_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.teacher
    ADD CONSTRAINT teacher_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES public.organization(id);


--
-- Name: teacher teacher_person_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.teacher
    ADD CONSTRAINT teacher_person_id_fkey FOREIGN KEY (person_id) REFERENCES public.person(id);


--
-- Name: teacher teacher_who_label_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.teacher
    ADD CONSTRAINT teacher_who_label_id_fkey FOREIGN KEY (who_label_id) REFERENCES public.label(id);


--
-- Name: vat vat_account_model_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.vat
    ADD CONSTRAINT vat_account_model_id_fkey FOREIGN KEY (account_model_id) REFERENCES public.accounting_model(id);


--
-- Name: warehouse warehouse_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: kbs
--

ALTER TABLE ONLY public.warehouse
    ADD CONSTRAINT warehouse_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES public.organization(id);


--
-- PostgreSQL database dump complete
--

