-- Определяет имя схемы в исходной БД и сохраняет его в макропеременной
-- sourceSchema.

@oms-default sourceSchema ""

declare

  sourceSchema varchar2(200) := '&sourceSchema';

begin
  if sourceSchema is null then
    raise_application_error(
      pkg_Error.IllegalArgument
      , 'Source schema in main DB not specified (SOURCE_SCHEMA="").'
    );
  end if;
end;
/
