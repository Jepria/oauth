declare

  opt opt_option_list_t := opt_option_list_t(
    moduleName => pkg_OAuthCommon.Module_Name
  );

begin
  opt.addString(
    optionShortName       => pkg_OAuthCommon.CryptoKey_OptSName
    , optionName          =>
        'Ключ шифрования, используемый в модуле'
    , optionDescription   =>
        'Ключ в резервной БД должен быть таким же, как и в основной БД'
    , encryptionFlag      => 1
  );
end;
/
