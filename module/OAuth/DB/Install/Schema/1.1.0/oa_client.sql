update
  oa_client t
set
  t.client_secret = pkg_OAuthCommon.encrypt( pkg_OptionCrypto.decrypt( t.client_secret))
where
  t.client_secret is not null
/

commit
/
