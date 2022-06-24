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

INSERT INTO public.authorization_rule VALUES (1, 'Monitor page access', 'grant route:/monitor');
INSERT INTO public.authorization_rule VALUES (2, 'Tester page access', 'grant route:/tester');
INSERT INTO public.authorization_rule VALUES (3, 'Bookings page access', 'grant route:/bookings/*');
INSERT INTO public.authorization_rule VALUES (4, 'Operations page access', 'grant route:/operations');
INSERT INTO public.authorization_rule VALUES (5, 'Authorizations page access', 'grant route:/authorizations');
