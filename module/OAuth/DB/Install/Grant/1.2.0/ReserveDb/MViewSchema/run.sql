-- script: Install/Grant/1.2.0/ReserveDb/MViewSchema/run.sql
-- Выдает права на выборку и обновление материализованных представлений, а
-- также создает синонимы для основного пользователя в резервной БД.
--
-- Параметры:
-- toUserName                 - Имя пользователя, которому выдаются права
--

define toUserName = "&1"



grant select, alter on mv_oa_client to &toUserName
/
create or replace synonym &toUserName..oa_client for mv_oa_client
/



undefine toUserName
