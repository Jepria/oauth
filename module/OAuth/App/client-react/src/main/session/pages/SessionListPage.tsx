import React, { useEffect } from 'react';
import { setCurrentRecord, searchSessions, postSearchSessionRequest } from '../state/redux/actions';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory } from 'react-router-dom';
import { AppState } from '../../store';
import { SessionState, ColumnSortConfiguration } from '../types';
import { TextCell } from '../../../components/cell/TextCell';
import { DateCell } from '../../../components/cell/DateCell';
import { NumberCell } from '../../../components/cell/NumberCell';
import { JepGrid, JepGridTable, JepGridHeader, JepGridHeaderCell, JepGridBody, JepGridRow, JepGridRowCell, JepGridPagingBar, Page, Content } from 'jfront-components';

const SessionListPage: React.FC = () => {

  const dispatch = useDispatch();
  const history = useHistory();
  const { records, current, searchId, searchRequest, resultSetSize } = useSelector<AppState, SessionState>(state => state.session);

  useEffect(() => {
    if (searchId && searchRequest) {
      dispatch(searchSessions(searchId, 25, 1));
    } else if (!searchId && searchRequest) {
      dispatch(postSearchSessionRequest(searchRequest));
    } else {
      dispatch(postSearchSessionRequest({ template: { maxRowCount: 25 } }));
    }
  }, [searchId, searchRequest, dispatch]);

  let columnConfig: Map<string, string> = new Map();

  const mapColumnConfig = (): Array<ColumnSortConfiguration> => {
    return (Array.from(columnConfig) as Array<Array<string>>).map(entry => ({ columnName: entry[0], sortOrder: entry[1] }));
  }

  const onColumnConfigChange = () => {
    if (searchRequest) {
      searchRequest.listSortConfiguration = mapColumnConfig();
      dispatch(postSearchSessionRequest(searchRequest));
    } else {
      dispatch(postSearchSessionRequest({ template: { maxRowCount: 25 }, listSortConfiguration: mapColumnConfig() }));
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
              <JepGridHeaderCell onClick={e => onColumnHeaderClick(e, "sessionId")}>ID сессии</JepGridHeaderCell>
              <JepGridHeaderCell onClick={e => onColumnHeaderClick(e, "dateIns")}>Дата создания</JepGridHeaderCell>
              <JepGridHeaderCell onClick={e => onColumnHeaderClick(e, "operatorLogin")}>Логин оператора</JepGridHeaderCell>
              <JepGridHeaderCell onClick={e => onColumnHeaderClick(e, "operatorName")}>Имя оператора</JepGridHeaderCell>
              <JepGridHeaderCell onClick={e => onColumnHeaderClick(e, "operatorId")}>ID оператора</JepGridHeaderCell>
              <JepGridHeaderCell onClick={e => onColumnHeaderClick(e, "redirectUri")}>URL переадресации</JepGridHeaderCell>
              <JepGridHeaderCell onClick={e => onColumnHeaderClick(e, "clientName")}>Имя клиентского приложения</JepGridHeaderCell>
              <JepGridHeaderCell onClick={e => onColumnHeaderClick(e, "clientId")}>ID клиентского приложения</JepGridHeaderCell>
            </JepGridHeader>
            <JepGridBody>
              {records ? records.map(record => {
                return (
                  <JepGridRow key={record.sessionId}
                    onClick={() => dispatch(setCurrentRecord(record))}
                    onDoubleClick={() => current !== record ? dispatch(setCurrentRecord(record,
                      () => history.push(`/ui/session/${record.sessionId}/view`))) : history.push(`/ui/session/${record.sessionId}/view`)}
                    selected={record === current}>
                    <JepGridRowCell label="ID сессии">
                      <TextCell>{record.sessionId}</TextCell>
                    </JepGridRowCell>
                    <JepGridRowCell label="Код авторизации">
                      <TextCell>{record.authorizationCode}</TextCell>
                    </JepGridRowCell>
                    <JepGridRowCell label="Дата создания">
                      <DateCell>{record.dateIns}</DateCell>
                    </JepGridRowCell>
                    <JepGridRowCell label="Логин оператора">
                      <TextCell>{record.operatorLogin}</TextCell>
                    </JepGridRowCell>
                    <JepGridRowCell label="ID оператора">
                      <NumberCell>{record.operator?.value}</NumberCell>
                    </JepGridRowCell>
                    <JepGridRowCell label="URL переадресации">
                      <TextCell>{record.redirectUri}</TextCell>
                    </JepGridRowCell>
                    <JepGridRowCell label="Имя клиентского приложения">
                      <TextCell>{record.client?.name}</TextCell>
                    </JepGridRowCell>
                    <JepGridRowCell label="ID клиентского приложения">
                      <TextCell>{record.client?.value}</TextCell>
                    </JepGridRowCell>
                  </JepGridRow>);
              }): null}
            </JepGridBody>
          </JepGridTable>
          <JepGridPagingBar rowCount={records?.length} totalRowCount={resultSetSize} onRefresh={(page, pageSize) => {
            if (searchId) {
              dispatch(searchSessions(searchId, pageSize, page))
            }
          }} />
        </JepGrid>
      </Content>
    </Page>
  );
}

export default SessionListPage;