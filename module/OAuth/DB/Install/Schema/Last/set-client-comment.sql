-- Устанавливает комментарии к полям таблицы oa_client и
-- м-представлению на ее основе.
--
-- Параметры:
-- tableName                  - Имя таблицы или представления для
--                              комментирования полей
--

define tableName = "&1"



comment on column &tableName..client_id is
  'Id клиентского приложения'
/
comment on column &tableName..client_short_name is
  'Краткое наименование приложения/случайная строка'
/
comment on column &tableName..client_secret is
  'Случайная криптографически устойчивая строка'
/
comment on column &tableName..client_name is
  'Имя клиентского приложения'
/
comment on column &tableName..client_name_en is
  'Имя клиентского приложения на английском'
/
comment on column &tableName..application_type is
  'Тип клиентского приложения'
/
comment on column &tableName..login_module_uri is
  'URI логин модуля'
/
comment on column &tableName..date_ins is
  'Дата создания записи'
/
comment on column &tableName..operator_id_ins is
  'Id оператора, создавшего запись'
/
comment on column &tableName..change_date is
  'Дата последнего изменения записи'
/
comment on column &tableName..change_operator_id is
  'Id оператора, изменившего запись'
/
comment on column &tableName..operator_id is
  'Id привязанного оператора'
/
comment on column &tableName..is_deleted is
  'Признак удаления записи'
/



undefine tableName
