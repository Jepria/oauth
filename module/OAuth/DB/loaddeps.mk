#
# Зависимости при загрузке файлов в БД.
#
# Файлы в зависимостях должны указываться с дополнительным суффиксом:
# .$(lu)      - загрузка под первым пользователем
# .$(lu2)     - загрузка под вторым пользователем
# .$(lu3)     - загрузка под третьим пользователем
# ...         - ...
#
# Пример ( зависимость тела пакета pkg_TestModule от собственной спецификации
# и спецификации пакета pkg_TestModule2 при загрузке под первым пользователем):
#
# pkg_TestModule.pkb.$(lu): \
#   pkg_TestModule.pks.$(lu) \
#   pkg_TestModule2.pks.$(lu)
#
#
# Замечания:
# - в данном файле не должен использоваться символ табуляции ( вместо него для
#   форматирования нужно использовать пробелы), т.к. символ табуляции имеет
#   специальное значение для make и его случайное появление может привести к
#   труднообнаруживаемым ошибкам;
# - в случае, если последняя строка зависимости также завершается символом
#   экранирования ( обратной косой чертой), то после зависимости
#   должна идти как минимум одна пустая строка, иначе при загрузке будет
#   возникать ошибка "*** No rule to make target ` ', needed by ...";
# - файлы в зависимости должны указываться с путем относительно каталога DB
#   с учетом регистра, например "Install/Schema/Last/test_view.vw.$(lu): ...";
#

Common/pkg_OAuthCommon.pkb.$(lu): \
  Common/pkg_OAuthCommon.pks.$(lu) \


pkg_OAuth.pkb.$(lu): \
  pkg_OAuth.pks.$(lu) \
  Common/pkg_OAuthCommon.pks.$(lu) \


pkg_OAuthInternal.pkb.$(lu): \
  pkg_OAuthInternal.pks.$(lu) \
  Common/pkg_OAuthCommon.pks.$(lu) \
  Install/Schema/Last/v_oa_session.vw.$(lu) \


pkg_OAuthExternal.pkb.$(lu): \
  pkg_OAuthExternal.pks.$(lu) \
  pkg_OAuth.pks.$(lu) \


Install/Schema/Last/v_oa_session.vw.$(lu): \
  Install/Schema/Last/set-session-comment.sql \


Install/Data/Last/op_group.sql.$(lu): \
  Install/Data/Last/op_role.sql.$(lu) \


Common/pkg_OAuthCommon.pkb.$(lu2): \
  Common/pkg_OAuthCommon.pks.$(lu2) \


ReserveDb/pkg_OAuth.pkb.$(lu2): \
  ReserveDb/pkg_OAuth.pks.$(lu2) \
  Common/pkg_OAuthCommon.pks.$(lu2) \


ReserveDb/pkg_OAuthInternal.pkb.$(lu2): \
  ReserveDb/pkg_OAuthInternal.pks.$(lu2) \
  Common/pkg_OAuthCommon.pks.$(lu2) \
  Install/Schema/Last/ReserveDb/v_oa_session.vw.$(lu2) \


Install/Schema/Last/ReserveDb/v_oa_session.vw.$(lu2): \
  Install/Schema/Last/set-session-comment.sql \


