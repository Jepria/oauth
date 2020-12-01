-- script: Install/Schema/1.1.0/revert.sql
-- Отменяет изменения в объектах схемы, внесенные при установке версии 1.1.0.
--


update
  oa_client t
set
  t.client_secret = pkg_OptionCrypto.encrypt( pkg_OAuthCommon.decrypt( t.client_secret))
where
  t.client_secret is not null
/
commit
/


drop materialized view log on oa_client
/

drop materialized view log on oa_client_grant
/

drop materialized view log on oa_client_uri
/

drop materialized view log on oa_key
/

drop materialized view log on oa_session
/


alter sequence oa_session_seq increment by 1
/
