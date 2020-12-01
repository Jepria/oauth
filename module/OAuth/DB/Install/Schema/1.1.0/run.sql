-- script: Install/Schema/1.1.0/run.sql
-- Обновление объектов схемы до версии 1.1.0.
--
-- Основные изменения:
--  - в таблице <oa_client> выполняется перешифрование значения client_secret
--    с использованием собственного ключа;
--  - пересоздается <oa_session_seq> и корректируется
--    session_id в <oa_session> чтобы обеспечить использование только
--    нечетных значений;
--  - созданы мат-логи на таблицы;
--

-- Определяет табличное пространство для индексов
@oms-set-indexTablespace.sql

-- обновляем пакеты для последующего использования
@oms-run Common/pkg_OAuthCommon.pks
show errors
@oms-run ./pkg_OAuth.pks
show errors
@oms-run ./pkg_OAuthInternal.pks
show errors

@oms-run Common/pkg_OAuthCommon.pkb
show errors
@oms-run ./pkg_OAuth.pkb
show errors
@oms-run ./pkg_OAuthInternal.pkb
show errors

@oms-run Install/Data/Last/opt_option.sql

@oms-run oa_client.sql

@oms-run set-odd-session_id.sql
@oms-run create-mlog.sql
