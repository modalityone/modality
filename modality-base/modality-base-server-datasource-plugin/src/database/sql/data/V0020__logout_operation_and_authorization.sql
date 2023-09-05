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

INSERT INTO public.operation VALUES (65, 'Logout', 'Logout', 'Logout', NULL, true, true, false, false);
INSERT INTO public.authorization_assignment VALUES (8, NULL, NULL, NULL, NULL, true, NULL, 65);

