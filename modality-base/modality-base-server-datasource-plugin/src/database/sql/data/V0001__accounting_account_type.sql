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

INSERT INTO public.accounting_account_type VALUES (1, 1, 'Assets', NULL);
INSERT INTO public.accounting_account_type VALUES (2, 2, 'Liabilities', NULL);
INSERT INTO public.accounting_account_type VALUES (7, 7, 'Commitments for Inventories and Property, Plant and Equipment', NULL);
INSERT INTO public.accounting_account_type VALUES (6, 6, 'Commitments for Expenses', NULL);
INSERT INTO public.accounting_account_type VALUES (5, 5, 'Expenses', NULL);
INSERT INTO public.accounting_account_type VALUES (4, 4, 'Revenues', NULL);
INSERT INTO public.accounting_account_type VALUES (3, 3, 'Equity', NULL);
