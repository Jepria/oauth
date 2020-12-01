-- script: Install/Schema/Last/ReserveDb/revert.sql
-- Отменяет установку модуля в основную схему резервной БД, удаляя созданные
-- объекты.

-- Удаление пакетных заданий
declare

  cursor batchCur is
    select
      t.batch_short_name
    from
      sch_batch t
    where
      t.module_id =
        (
        select
          md.module_id
        from
          v_mod_module md
        where
          md.module_name = 'OAuth'
        )
    order by
      t.batch_short_name
  ;

begin
  for rec in batchCur loop
    dbms_output.put_line(
      'delete batch: ' || rec.batch_short_name
    );
    pkg_SchedulerLoad.deleteBatch(
      batchShortName => rec.batch_short_name
    );
  end loop;
  commit;
end;
/

-- Удаление параметров модуля
begin
  opt_option_list_t( moduleName => 'OAuth').deleteAll();
  commit;
end;
/

-- Удаление тестовых объектов (при наличии)
begin
  for rec in (
        select
          ob.object_name
          , ob.object_type
        from
          user_objects ob
        where
          ob.object_type = 'PACKAGE'
          and ob.object_name = upper( 'pkg_OAuthTest')
      )
      loop
    dbms_output.put_line(
      'drop: ' || rec.object_type || ': ' || rec.object_name
    );
    execute immediate
      'drop ' || rec.object_type || ' ' || rec.object_name
    ;
  end loop;
end;
/


-- Пакеты

drop package pkg_OAuth
/
drop package pkg_OAuthCommon
/
drop package pkg_OAuthInternal
/


-- Представления

drop view v_oa_session
/


-- Внешние ключи

@oms-drop-foreign-key oa_session


-- Таблицы

drop table oa_session
/


-- Последовательности

drop sequence oa_session_seq
/
