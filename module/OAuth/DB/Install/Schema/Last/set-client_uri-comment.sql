-- Устанавливает комментарии к полям таблицы oa_client_uri и
-- м-представлению на ее основе.
--
-- Параметры:
-- tableName                  - Имя таблицы или представления для
--                              комментирования полей
--

define tableName = "&1"



comment on column &tableName..client_uri_id is
  'Id записи с URI клиентского приложения'
/
comment on column &tableName..client_id is
  'Id клиентского приложения'
/
comment on column &tableName..client_uri is
  'URI клиентского приложения'
/
comment on column &tableName..date_ins is
  'Дата создания записи'
/
comment on column &tableName..operator_id is
  'Id оператора, создавшего запись'
/



undefine tableName
