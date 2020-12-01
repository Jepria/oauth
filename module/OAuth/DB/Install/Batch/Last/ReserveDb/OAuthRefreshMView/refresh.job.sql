-- Обновление материализованных представлений с данными OAuth из основной БД
declare

  mviewSchema varchar2(200) := pkg_Scheduler.getContextString(
    'MViewSchema', riseException => 1
  );

  refreshAfterErrorsFlag integer := pkg_Scheduler.getContextNumber(
    'RefreshAfterErrorsFlag'
  );

  atomicRefreshFlag integer := pkg_Scheduler.getContextNumber(
    'AtomicRefreshFlag'
  );

begin
  if mviewSchema is null then
    select
      max( t.table_owner)
    into mviewSchema
    from
      user_synonyms t
    where
      t.synonym_name = 'OA_CLIENT'
    ;
    if mviewSchema is not null then
      lg_logger_t.getLogger(
          moduleName    => pkg_OAuthCommon.Module_Name
          , objectName  => 'refresh.job.sql'
        )
      .info(
          'Определена схема материализованных представлений:'
          || ' "' || mviewSchema || '"'
        )
      ;
    else
      raise_application_error(
        pkg_Error.IllegalArgument
        , 'Необходимо задать схему, в которую установлены материализованные'
          || ' представления (параметр MViewSchema).'
      );
    end if;
  end if;
  dbms_mview.refresh(
    list                    =>
      replace(
        '$(schema).mv_oa_client'
          || ',$(schema).mv_oa_client_grant'
          || ',$(schema).mv_oa_client_uri'
          || ',$(schema).mv_oa_key'
          || ',$(schema).mv_oa_session'
        , '$(schema)', mviewSchema
      )
    , method                => lpad( '?', 5, '?')
    , atomic_refresh        => coalesce( atomicRefreshFlag = 1, true)
    , refresh_after_errors  => coalesce( refreshAfterErrorsFlag = 1, false)
  );
end;
