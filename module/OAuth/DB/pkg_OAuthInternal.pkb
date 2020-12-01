create or replace package body pkg_OAuthInternal is
/* package body: pkg_OAuthInternal::body */



/* group: Переменные */

/* ivar: logger
  Логер пакета.
*/
logger lg_logger_t := lg_logger_t.getLogger(
  moduleName    => pkg_OAuthCommon.Module_Name
  , objectName  => 'pkg_OAuthInternal'
);



/* group: Функции */

/* func: setSessionDateFinish
  Заполняет поле date_finish для записей в <oa_session>, имеющих токены
  с истекшим сроком действия.

  Возврат:
  число обновленных записей.
*/
function setSessionDateFinish
return integer
is

  cursor dataCur is
    select
      ss.session_id
    from
      v_oa_session ss
    where
      ss.is_blocked = 1
    for update of session_id nowait
  ;

  -- Id записей
  type IdListT is table of integer;
  idList IdListT;

  -- Число обрабатанных записей
  nProcessed integer := 0;

begin
  open dataCur;
  loop
    fetch dataCur bulk collect into idList limit 100;

    -- Выход, если нет загруженных данных
    exit when idList.first is null;

    forall i in idList.first .. idList.last
      update
        oa_session t
      set
        t.date_finish = systimestamp
      where
        t.session_id = idList( i)
        and t.date_finish is null
    ;
    nProcessed := nProcessed + idList.count;
    exit when dataCur%notfound;
  end loop;
  close dataCur;
  return nProcessed;
exception when others then
  raise_application_error(
    pkg_Error.ErrorStackInfo
    , logger.errorStack(
        'Ошибка при заполнении date_finish в oa_session.'
      )
    , true
  );
end setSessionDateFinish;

/* func: clearOldData
  Очищает устаревшие неактуальные данные.

  Параметры:
  toTime                      - Момент времени, до которого удаляется
                                неактуальные данные
                                (не включая)
                                (по умолчанию 31 сутки назад)
  saveKeyCount                - Число неактуальных ключей, сохраняемых в таблице
                                <oa_key>
                                (по умолчанию 9)
  maxExecTime                 - Максимальное время выполнения процедуры (в
                                случае, если время превышено и остались данные
                                для удаления, процедура завершает работу
                                с выводом предупреждения в лог
                                (по умолчанию без ограничений)

  Возврат:
  число удаленных записей.
*/
function clearOldData(
  toTime timestamp with time zone := null
  , saveKeyCount integer := null
  , maxExecTime interval day to second := null
)
return integer
is

  -- Число удаленных записей
  nDeleted integer := 0;

  -- Id удаляемых записей
  type IdListT is table of integer;
  idList IdListT;

  -- Время прекращения обработки
  stopProcessDate date := sysdate + maxExecTime;

  -- Флаг прерывания обработки
  isStopProcess boolean := false;



  /*
    Очистка данных по сессиям.
  */
  procedure clearSession
  is

    -- Удаляемые записи
    cursor clearDataCur is
      select
        t.session_id
      from
        oa_session t
      where
        t.date_finish < coalesce(
            toTime
            , systimestamp - INTERVAL '31' DAY
          )
      for update of session_id nowait
    ;

    nRows integer := 0;

  begin
    open clearDataCur;
    loop
      fetch clearDataCur bulk collect into idList limit 100;
      exit when idList.first is null;
      forall i in idList.first .. idList.last
        delete from
          oa_session t
        where
          t.session_id = idList( i)
      ;
      nRows := nRows + idList.count;
      exit when clearDataCur%notfound;
      -- выполняем очистку как минимум один раз
      isStopProcess := sysdate >= stopProcessDate;
      exit when isStopProcess;
    end loop;
    close clearDataCur;
    logger.trace( 'clearSession: ' || nRows || ' deleted');
    nDeleted := nDeleted + nRows;
  exception when others then
    raise_application_error(
      pkg_Error.ErrorStackInfo
      , logger.errorStack(
          'Ошибка при очистке данных по сессиям.'
        )
      , true
    );
  end clearSession;



  /*
    Очистка данных по ключам.
  */
  procedure clearKey
  is

    -- Удаляемые записи
    cursor clearDataCur is
      select
        d.key_id
      from
        oa_key d
      where
        d.is_actual = 0
        and d.key_id not in
          (
          select
            *
          from
            (
            select
              t.key_id
            from
              oa_key t
            where
              t.is_actual = 0
            order by
              t.date_ins desc
              , t.key_id desc
            )
          where
            rownum <= coalesce( saveKeyCount, 9)
          )
      for update of key_id nowait
    ;

    nRows integer := 0;

  begin
    open clearDataCur;
    loop
      fetch clearDataCur bulk collect into idList limit 100;
      exit when idList.first is null;
      forall i in idList.first .. idList.last
        delete from
          oa_key t
        where
          t.key_id = idList( i)
      ;
      nRows := nRows + idList.count;
      exit when clearDataCur%notfound;
      -- выполняем очистку как минимум один раз
      isStopProcess := sysdate >= stopProcessDate;
      exit when isStopProcess;
    end loop;
    close clearDataCur;
    logger.trace( 'clearKey: ' || nRows || ' deleted');
    nDeleted := nDeleted + nRows;
  exception when others then
    raise_application_error(
      pkg_Error.ErrorStackInfo
      , logger.errorStack(
          'Ошибка при очистке данных по ключам.'
        )
      , true
    );
  end clearKey;



-- clearOldData
begin
  clearSession();
  clearKey();
  if isStopProcess then
    logger.info(
      'Очистка прекращена в связи с достижением лимита времени.'
    );
  end if;
  return nDeleted;
exception when others then
  raise_application_error(
    pkg_Error.ErrorStackInfo
    , logger.errorStack(
        'Ошибка при очистке устаревших данных ('
        || ' toTime=' || to_char( toTime, 'dd.mm.yyyy hh24:mi:ss tzh:tzm')
        || ', saveKeyCount=' || to_char( saveKeyCount)
        || ', maxExecTime=' || to_char( maxExecTime)
        || ').'
      )
    , true
  );
end clearOldData;

end pkg_OAuthInternal;
/
