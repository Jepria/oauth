-- script: Install/Grant/Last/grant-external.sql
-- Выдача прав и создание синонимов на объекты модуля OAuth для внешних модулей

-- Параметры: имя пользователя для выдачи прав

define UserName = "&1"

grant execute on pkg_OAuthExternal to &UserName;

create or replace synonym &UserName..pkg_OAuthExternal for pkg_OAuthExternal;

undefine toUserName
