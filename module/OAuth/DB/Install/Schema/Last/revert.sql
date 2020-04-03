-- script: Install/Schema/Last/revert.sql
-- Отменяет установку модуля, удаляя созданные объекты схемы.


-- Пакеты

drop package pkg_OAuth
/


-- Представления

drop view v_oa_session
/


-- Внешние ключи

@oms-drop-foreign-key oa_client
@oms-drop-foreign-key oa_client_grant
@oms-drop-foreign-key oa_client_uri
@oms-drop-foreign-key oa_key
@oms-drop-foreign-key oa_session


-- Таблицы

drop table oa_client
/
drop table oa_client_grant
/
drop table oa_client_uri
/
drop table oa_key
/
drop table oa_session
/


-- Последовательности

drop sequence oa_client_seq
/
drop sequence oa_client_uri_seq
/
drop sequence oa_key_seq
/
drop sequence oa_session_seq
/
