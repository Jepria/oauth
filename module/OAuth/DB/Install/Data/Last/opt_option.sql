declare

  opt opt_option_list_t := opt_option_list_t(
    moduleName => pkg_OAuthCommon.Module_Name
  );

begin
  opt.addString(
    optionShortName       => pkg_OAuthCommon.CryptoKey_OptSName
    , optionName          =>
        'Ключ шифрования, используемый в модуле'
    , encryptionFlag      => 1
    , stringValue         => rawtohex( dbms_crypto.randomBytes( 32))
  );
end;
/
