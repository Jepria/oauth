-- script: Install/Schema/Last/ReserveDb/run.sql
-- Выполняет установку последней версии объектов основной схемы резервной БД.


-- Определяем табличное пространство для индексов
@oms-set-indexTablespace.sql


-- Последовательности

@oms-run oa_session_seq.sqs


-- Таблицы

@oms-run oa_session.tab


-- Outline-ограничения целостности

@oms-run oa_session.con
