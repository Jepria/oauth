-- script: Install/Schema/Last/UserDB/run.sql
-- Выполняет установку последней версии объектов схемы в схему для
-- материализованных представлений резервной БД.
-- Обеспечивается репликация данных для их локального использования.

-- Определяет табличное пространство для индексов
@oms-set-indexTablespace.sql

-- Определяем линк и схему в исходной БД
@oms-run set-sourceDbLink.sql
@oms-run set-sourceSchema.sql

-- Создание мат. представлений
-- (отдельные скрипты для пересоздания каждого м-представления добавлены
-- для возможности исключения пересоздания с помощью SKIP_FILE_MASK)
@oms-run mv_oa_client.sql