-- Заполнение date_finish в oa_session
declare

  nRecord integer;

begin
  nRecord := pkg_OAuthInternal.setSessionDateFinish();
  jobResultMessage :=
    'Заполнение date_finish в oa_session: обновлено записей: '
    || to_char( nRecord)
  ;
end;
