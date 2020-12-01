ifneq ($(SOURCE_DBLINK),)
  override SQL_DEFINE += ,sourceDbLink=$(SOURCE_DBLINK)
endif

ifneq ($(SOURCE_SCHEMA),)
  override SQL_DEFINE += ,sourceSchema=$(SOURCE_SCHEMA)
endif
