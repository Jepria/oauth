create or replace package pkg_OAuthInternal is
/* package: pkg_OAuthInternal( ReserveDb)
  Внутренние функции модуля OAuth для резервной БД.

  SVN root: JEP/Module/OAuth
*/



/* group: Функции */

/* pfunc: setSessionDateFinish
  Заполняет поле date_finish для записей в <oa_session>, имеющих токены
  с истекшим сроком действия.

  Возврат:
  число обновленных записей.

  ( <body::setSessionDateFinish>)
*/
function setSessionDateFinish
return integer;

/* pfunc: clearOldData
  Очищает устаревшие неактуальные данные.

  Параметры:
  toTime                      - Момент времени, до которого удаляется
                                неактуальные данные
                                (не включая)
                                (по умолчанию 31 сутки назад)
  maxExecTime                 - Максимальное время выполнения процедуры (в
                                случае, если время превышено и остались данные
                                для удаления, процедура завершает работу
                                с выводом предупреждения в лог
                                (по умолчанию без ограничений)

  Возврат:
  число удаленных записей.

  ( <body::clearOldData>)
*/
function clearOldData(
  toTime timestamp with time zone := null
  , maxExecTime interval day to second := null
)
return integer;

end pkg_OAuthInternal;
/
