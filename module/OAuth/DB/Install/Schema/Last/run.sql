-- script: Install/Schema/Last/run.sql
-- Выполняет установку последней версии объектов схемы.


-- Определяем табличное пространство для индексов
@oms-set-indexTablespace.sql


-- Последовательности

@oms-run oa_client_seq.sqs
@oms-run oa_client_uri_seq.sqs
@oms-run oa_key_seq.sqs
@oms-run oa_session_seq.sqs


-- Таблицы

@oms-run oa_client.tab
@oms-run oa_client_grant.tab
@oms-run oa_client_uri.tab
@oms-run oa_key.tab
@oms-run oa_session.tab


-- Outline-ограничения целостности

@oms-run oa_client.con
@oms-run oa_client_grant.con
@oms-run oa_client_uri.con
@oms-run oa_key.con
@oms-run oa_session.con
