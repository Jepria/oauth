-- table: OA_CLIENT
-- Клиентские приложения.

-- Добавление поля LOGIN_MODULE_URI
declare
  -- Наименование таблицы
  tableName     varchar2(30) := 'OA_CLIENT';

  -- Наименование поля
  columnName    varchar2(30) := 'LOGIN_MODULE_URI';

  -- Признак наличия
  cnt           number;
begin
  select
    count(*)
  into
    cnt
  from
    user_tab_columns c
  where
    c.table_name = tableName
    and c.column_name = columnName
  ;

  if cnt = 0 then
    execute immediate
      'alter table ' || tableName || '
        add (' || columnName || ' varchar2(4000))';

    execute immediate
      'comment on column ' || tableName || '.' || columnName || ' is
        ''URI логин модуля''';

    dbms_output.put_line(
      'Add column ' || tableName || '.' || columnName
    );
  else
    dbms_output.put_line(
      'Column ' || tableName || '.' || columnName || ' already exists!'
    );
  end if;
end;
/
