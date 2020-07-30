import React, { useEffect } from 'react';
import { setCurrentRecord, searchClients, postSearchClientRequest } from '../state/redux/actions';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory } from 'react-router-dom';
import { AppState } from '../../store';
import { ClientState, ColumnSortConfiguration } from '../types';
import { GrantType, ApplicationType } from '../../../security/OAuth';
import { TextCell } from '../../../components/cell/TextCell';
import { JepGrid, JepGridTable, JepGridHeader, JepGridHeaderCell, JepGridBody, JepGridRow, JepGridRowCell, JepGridPagingBar, Page, Content } from 'jfront-components';

export const ClientListPage: React.FC = () => {

  const dispatch = useDispatch();
  const history = useHistory();
  const { records, current, searchId, searchRequest, resultSetSize } = useSelector<AppState, ClientState>(state => state.client);

  useEffect(() => {
    if (searchId && searchRequest) {
      dispatch(searchClients(searchId, 25, 1));
    } else if (!searchId && searchRequest) {
      dispatch(postSearchClientRequest(searchRequest));
    } else {
      dispatch(postSearchClientRequest({ template: { maxRowCount: 25 } }));
    }
  }, [searchId, searchRequest, dispatch]);

  let columnConfig: Map<string, string> = new Map();

  const mapColumnConfig = (): Array<ColumnSortConfiguration> => {
    return (Array.from(columnConfig) as Array<Array<string>>).map(entry => ({ columnName: entry[0], sortOrder: entry[1] }));
  }

  const onColumnConfigChange = () => {
    if (searchRequest) {
      searchRequest.listSortConfiguration = mapColumnConfig();
      dispatch(postSearchClientRequest(searchRequest));
    } else {
      dispatch(postSearchClientRequest({ template: { maxRowCount: 25 }, listSortConfiguration: mapColumnConfig() }));
    }
  }

  const onSingleColumnSort = (colName: string) => {
    if (columnConfig.get(colName)) {
      if (columnConfig.get(colName) === "asc") {
        columnConfig = new Map([[colName, "desc"]]);
      } else {
        columnConfig = new Map([[colName, "asc"]]);
      }
    } else {
      columnConfig = new Map([[colName, "desc"]]);
    }
    onColumnConfigChange();
  }

  const onMultiColumnSort = (colName: string) => {
    if (columnConfig.get(colName)) {
      if (columnConfig.get(colName) === "asc") {
        columnConfig.set(colName, "desc");
      } else {
        columnConfig.set(colName, "asc");
      }
    } else {
      columnConfig.set(colName, "desc");
    }
    onColumnConfigChange();
  }

  const onColumnHeaderClick = (e: React.MouseEvent, colName: string) => {
    if (e.ctrlKey) {
      onMultiColumnSort(colName);
    } else {
      onSingleColumnSort(colName);
    }
  }

  return (
    <Page>
      <Content>
        <JepGrid>
          <JepGridTable>
            <JepGridHeader>
              <JepGridHeaderCell onClick={e => onColumnHeaderClick(e, "clientId")}>ID клиентского приложения</JepGridHeaderCell>
              <JepGridHeaderCell onClick={e => onColumnHeaderClick(e, "clientSecret")}>Секретное слово</JepGridHeaderCell>
              <JepGridHeaderCell onClick={e => onColumnHeaderClick(e, "clientName")}>Наименование</JepGridHeaderCell>
              <JepGridHeaderCell onClick={e => onColumnHeaderClick(e, "clientNameEn")}>Наименование (англ)</JepGridHeaderCell>
              <JepGridHeaderCell onClick={e => onColumnHeaderClick(e, "applicationType")}>Тип приложения</JepGridHeaderCell>
              <JepGridHeaderCell>Разрешенные типы авторизации</JepGridHeaderCell>
            </JepGridHeader>
            <JepGridBody>
              {records ? records.map(record => {
                return (
                  <JepGridRow key={record.clientId}
                    onClick={() => dispatch(setCurrentRecord(record))}
                    onDoubleClick={() => current !== record ? dispatch(setCurrentRecord(record,
                      () => history.push(`/ui/client/${record.clientId}/view`))) : history.push(`/ui/client/${record.clientId}/view`)}
                    selected={record === current}>
                    <JepGridRowCell label="ID приложения">
                      <TextCell>{record.clientId}</TextCell>
                    </JepGridRowCell>
                    <JepGridRowCell label="Секретное слово">
                      <TextCell>{record.clientSecret}</TextCell>
                    </JepGridRowCell>
                    <JepGridRowCell label="Наименование">
                      <TextCell>{record.clientName}</TextCell>
                    </JepGridRowCell>
                    <JepGridRowCell label="Наименование (англ.)">
                      <TextCell>{record.clientNameEn}</TextCell>
                    </JepGridRowCell>
                    <JepGridRowCell label="Тип приложения">
                      <TextCell>{ApplicationType[record.applicationType]}</TextCell>
                    </JepGridRowCell>
                    <JepGridRowCell label="Разрешения на авторизацию">
                      <TextCell wrapText>{record.grantTypes.map((grantType) => GrantType[grantType]).join(', ')}</TextCell>
                    </JepGridRowCell>
                  </JepGridRow>);
              }) : null}
            </JepGridBody>
          </JepGridTable>
          <JepGridPagingBar rowCount={records?.length} totalRowCount={resultSetSize} onRefresh={(page, pageSize) => {
            if (searchId) {
              dispatch(searchClients(searchId, pageSize, page))
            }
          }} />
        </JepGrid>
      </Content>
    </Page>
  );
}