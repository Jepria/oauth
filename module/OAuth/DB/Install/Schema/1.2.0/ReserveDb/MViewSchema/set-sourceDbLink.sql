-- Определяет линк к исходной БД и сохраняет его в макропеременной sourceDbLink.

@oms-default sourceDbLink ""

declare

  sourceDbLink varchar2(200) := '&sourceDbLink';

  foundFlag integer;

begin
  if sourceDbLink is null then
    raise_application_error(
      pkg_Error.IllegalArgument
      , 'Database link to main DB not specified (SOURCE_DBLINK="").'
    );
  else
    select
      count(*)
    into foundFlag
    from
      all_db_links t
    where
      upper( db_link) = upper( sourceDbLink)
      and rownum <= 1
    ;
    if foundFlag = 0 then
      raise_application_error(
        pkg_Error.IllegalArgument
        , 'Database link not found ('
          || 'SOURCE_DBLINK="' || sourceDbLink || '"'
          || ').'
      );
    end if;
  end if;
end;
/
