-- script: Test/run.sql
-- Выполняет все тесты.
--
-- Используемые макромеременные:
-- testCaseNumber             - Номер проверяемого тестового случая
--                              (по умолчанию без ограничений)
-- loggingLevelCode           - Уровень логирования для модуля
--                              (по-умолчанию из rootLoggingLevelCode либо
--                                WARN)
-- rootLoggingLevelCode       - Уровень логирования корневого логера
--                              (по-умолчанию WARN)
-- saveDataFlag               - Флаг сохранения тестовых данных
--                              (1 да, 0 нет (по умолчанию))
--

@oms-default testCaseNumber ""
@oms-default loggingLevelCode ""
@oms-default rootLoggingLevelCode ""
@oms-default saveDataFlag ""

set feedback off

declare
  loggingLevelCode varchar2(10) := '&loggingLevelCode';
  rootLoggingLevelCode varchar2(10) := '&rootLoggingLevelCode';
begin
  lg_logger_t.getRootLogger().setLevel(
    coalesce( rootLoggingLevelCode, pkg_Logging.Warning_LevelCode)
  );
  lg_logger_t.getLogger( 'OAuth').setLevel(
    coalesce(
      loggingLevelCode
      , rootLoggingLevelCode
      , pkg_Logging.Warning_LevelCode
    )
  );
end;
/

set feedback on

@oms-run Test/AutoTest/user-api.sql
@oms-run Test/AutoTest/internal.sql
