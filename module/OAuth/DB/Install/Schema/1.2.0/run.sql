-- script: Install/Schema/1.2.0/run.sql
-- Обновление объектов схемы до версии 1.2.0.
--
-- Основные изменения:
--  - в таблицу <oa_client> добавлется поле login_module_uri, содержащее URL логин-модуля приложения
--  - обновлен мат-логи на таблицу oa_client;
--

-- Определяет табличное пространство для индексов
@oms-set-indexTablespace.sql

@oms-run remove-mlog.sql
@oms-run oa_client.sql
@oms-run create-mlog.sql

-- обновляем пакеты для последующего использования
@oms-run Common/pkg_OAuthCommon.pks
show errors
@oms-run ./pkg_OAuth.pks
show errors

@oms-run Common/pkg_OAuthCommon.pkb
show errors
@oms-run ./pkg_OAuth.pkb
show errors
