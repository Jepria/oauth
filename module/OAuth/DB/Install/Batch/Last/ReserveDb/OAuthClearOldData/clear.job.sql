-- Очистка устаревших данных
declare

  -- Параметры процедуры
  saveDayCount integer := pkg_Scheduler.getContextNumber(
    'SaveDayCount', riseException => 1
  );

  maxExecTimeHour number := pkg_Scheduler.getContextNumber(
    'MaxExecTimeHour'
  );

  -- Дата, до которой очищаются данные
  toTime timestamp with time zone;

  nRecord integer;

begin
  toTime :=
    to_timestamp_tz(
        to_char( systimestamp, 'dd.mm.yyyy tzh:tzm')
        , 'dd.mm.yyyy tzh:tzm'
      )
    - numtodsinterval( saveDayCount, 'DAY')
  ;
  nRecord := pkg_OAuthInternal.clearOldData(
    toTime          => toTime
    , maxExecTime   => numtodsinterval( maxExecTimeHour, 'HOUR')
  );
  jobResultMessage :=
    'Очистка выполнена ('
    || ' до даты: ' || to_char( toTime, 'dd.mm.yyyy hh24:mi:ss tzh:tzm')
    || ', удалено записей: ' || to_char( nRecord)
    || ').'
  ;
end;
