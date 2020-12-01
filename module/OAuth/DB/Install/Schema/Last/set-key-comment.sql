-- Устанавливает комментарии к полям таблицы oa_key и
-- м-представлению на ее основе.
--
-- Параметры:
-- tableName                  - Имя таблицы или представления для
--                              комментирования полей
--

define tableName = "&1"



comment on column &tableName..key_id is
  'Id записи с ключами'
/
comment on column &tableName..public_key is
  'Публичный ключ'
/
comment on column &tableName..private_key is
  'Приватный ключ'
/
comment on column &tableName..is_actual is
  'Признак актуальной записи (1 - актуальная; 0 - неактуальная)'
/
comment on column &tableName..date_ins is
  'Дата создания записи'
/
comment on column &tableName..operator_id_ins is
  'Id оператора, создавшего запись'
/



undefine tableName
