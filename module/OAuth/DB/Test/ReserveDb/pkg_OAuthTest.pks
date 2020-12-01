create or replace package pkg_OAuthTest is
/* package: pkg_OAuthTest( ReserveDb)
  Функции для тестирования модуля в резервной БД.

  SVN root: JEP/Module/OAuth
*/



/* group: Функции */

/* pproc: testUserApi
  Тестирует API функции.

  Параметры:
  testCaseNumber              - Номер проверяемого тестового случая
                                (по умолчанию без ограничений)
  saveDataFlag                - Флаг сохранения тестовых данных
                                (1 да, 0 нет ( по умолчанию))

  ( <body::testUserApi>)
*/
procedure testUserApi(
  testCaseNumber integer := null
  , saveDataFlag integer := null
);

/* pproc: testInternal
  Тестирует служебные функции.

  Параметры:
  testCaseNumber              - Номер проверяемого тестового случая
                                (по умолчанию без ограничений)
  saveDataFlag                - Флаг сохранения тестовых данных
                                (1 да, 0 нет ( по умолчанию))

  ( <body::testInternal>)
*/
procedure testInternal(
  testCaseNumber integer := null
  , saveDataFlag integer := null
);

end pkg_OAuthTest;
/
