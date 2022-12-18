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

INSERT INTO public.operation VALUES (66, 'AddNewMoneyAccount', 'Add...', 'Add new money account', NULL, true, false, true, false);
INSERT INTO public.operation VALUES (67, 'EditMoneyAccount', 'Edit...', 'Edit money account', NULL, true, false, true, false);
INSERT INTO public.operation VALUES (68, 'DeleteMoneyAccount', 'Delete...', 'Delete money account', NULL, true, false, true, false);
INSERT INTO public.operation VALUES (69, 'AddNewMoneyFlow', 'Add...', 'Add new money flow', NULL, true, false, true, false);
INSERT INTO public.operation VALUES (70, 'EditMoneyFlow', 'Edit...', 'Edit money flow', NULL, true, false, true, false);
INSERT INTO public.operation VALUES (71, 'DeleteMoneyFlow', 'Delete...', 'Delete money flow', NULL, true, false, true, false);
