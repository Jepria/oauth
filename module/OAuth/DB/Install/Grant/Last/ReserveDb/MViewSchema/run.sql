-- script: Install/Grant/Last/ReserveDb/MViewSchema/run.sql
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

grant select, alter on mv_oa_client_grant to &toUserName
/
create or replace synonym &toUserName..oa_client_grant for mv_oa_client_grant
/

grant select, alter on mv_oa_client_uri to &toUserName
/
create or replace synonym &toUserName..oa_client_uri for mv_oa_client_uri
/

grant select, alter on mv_oa_key to &toUserName
/
create or replace synonym &toUserName..oa_key for mv_oa_key
/

grant select, alter on mv_oa_session to &toUserName
/
-- синоним по имени м-представления (т.к. будет одноименная локальная таблица)
create or replace synonym &toUserName..mv_oa_session for mv_oa_session
/



undefine toUserName
