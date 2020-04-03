-- script: Install/Schema/Last/set-session-comment.sql
-- Устанавливает комментарии к общим колонкам таблицы <oa_session> и
-- представления <v_oa_session>.
--
-- Параметры:
-- tableName                  - имя таблицы или представления для
--                              комментирования полей
--

define tableName = "&1"



comment on column &tableName..session_id is
  'Id пользовательской сессии'
/
comment on column &tableName..auth_code is
  'Авторизационный код (One-Time-Password)'
/
comment on column &tableName..client_id is
  'Id клиентского приложения'
/
comment on column &tableName..redirect_uri is
  'URI для перенаправления'
/
comment on column &tableName..operator_id is
  'Пользователь, владелец сессии'
/
comment on column &tableName..code_challenge is
  'Криптографически случайная строка, используется при авторизации по PKCE'
/
comment on column &tableName..access_token is
  'Уникальный UUID токена доступа'
/
comment on column &tableName..access_token_date_ins is
  'Дата создания токена доступа'
/
comment on column &tableName..access_token_date_finish is
  'Дата окончания действия токена доступа'
/
comment on column &tableName..refresh_token is
  'Уникальный UUID токена обновления'
/
comment on column &tableName..refresh_token_date_ins is
  'Дата создания токена обновления'
/
comment on column &tableName..refresh_token_date_finish is
  'Дата окончания действия токена обновления'
/
comment on column &tableName..session_token is
  'UUID токена сессии'
/
comment on column &tableName..session_token_date_ins is
  'Дата создания токена сессии'
/
comment on column &tableName..session_token_date_finish is
  'Дата окончания действия токена сессии'
/
comment on column &tableName..is_manual_blocked is
  'Признак ручной блокировки сессии (1 если да, иначе NULL)'
/
comment on column &tableName..date_ins is
  'Дата создания записи'
/
comment on column &tableName..operator_id_ins is
  'Id оператора, создавшего запись'
/



undefine tableName
