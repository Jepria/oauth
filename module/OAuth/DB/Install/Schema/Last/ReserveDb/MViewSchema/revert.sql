-- script: Install/Schema/Last/ReserveDb/MViewSchema/revert.sql
-- Отменяет установку модуля в схему для материализованных представлений
-- резервной БД, удаляя созданные объекты.

drop materialized view mv_oa_client
/
drop materialized view mv_oa_client_grant
/
drop materialized view mv_oa_client_uri
/
drop materialized view mv_oa_key
/
drop materialized view mv_oa_session
/
