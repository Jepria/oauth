-- script: Install/Grant/1.2.0/link-user.sql
-- Выдает права для пользователя, под которым работает линк из
-- резервной БД.
--
-- Параметры:
-- toUserName                 - пользователь для выдачи прав
--

define toUserName = &1



@oms-run Install/Grant/Last/master-table.sql oa_client



undefine toUserName
