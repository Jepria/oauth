-- script: Install/Grant/Last/link-user.sql
-- Выдает права для пользователя, под которым работает линк из
-- резервной БД.
--
-- Параметры:
-- toUserName                 - пользователь для выдачи прав
--

define toUserName = &1



@oms-run master-table.sql oa_client
@oms-run master-table.sql oa_client_grant
@oms-run master-table.sql oa_client_uri
@oms-run master-table.sql oa_key
@oms-run master-table.sql oa_session



undefine toUserName
