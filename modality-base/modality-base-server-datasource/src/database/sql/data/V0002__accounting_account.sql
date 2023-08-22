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

INSERT INTO public.accounting_account VALUES (1, 4, NULL, 1, 'Accommodation', NULL);
INSERT INTO public.accounting_account VALUES (2, 4, NULL, 2, 'Food', NULL);
INSERT INTO public.accounting_account VALUES (8, 4, NULL, 0, 'Charge free', NULL);
INSERT INTO public.accounting_account VALUES (9, 4, NULL, 3, 'Transport', NULL);
INSERT INTO public.accounting_account VALUES (10, 4, NULL, 4, 'Donation', NULL);
INSERT INTO public.accounting_account VALUES (11, 4, NULL, 5, 'Unreconciled', NULL);
