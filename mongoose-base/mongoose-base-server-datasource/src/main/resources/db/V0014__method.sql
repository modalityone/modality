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

ALTER TABLE public.method DISABLE TRIGGER ALL;
INSERT INTO public.method VALUES (1, 'cash', 'Cash', 1034);
INSERT INTO public.method VALUES (2, 'chq', 'Cheque', 1036);
INSERT INTO public.method VALUES (3, 'cc', 'Credit card', 1035);
INSERT INTO public.method VALUES (4, 'bt', 'Bank transfer', 1038);
INSERT INTO public.method VALUES (6, 'contra', 'Contra', 1039);
INSERT INTO public.method VALUES (5, 'ol', 'Online', 1037);
ALTER TABLE public.method ENABLE TRIGGER ALL;
