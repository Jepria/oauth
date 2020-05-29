import React, { useEffect } from 'react';
import { Page, Content } from '../../../components/Layout';
import { setCurrentRecord, searchSessions, postSearchSessionRequest } from '../state/redux/actions';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory } from 'react-router-dom';
import { AppState } from '../../store';
import { SessionState } from '../types';
import { TextCell } from '../../../components/cell/TextCell';
import { DateCell } from '../../../components/cell/DateCell';
import { NumberCell } from '../../../components/cell/NumberCell';
import { JepGrid, JepGridTable, JepGridHeader, JepGridHeaderCell, JepGridBody, JepGridRow, JepGridRowCell, JepGridPagingBar } from 'jfront-components';

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

  return (
    <Page>
      <Content>
        <JepGrid>
          <JepGridTable>
            <JepGridHeader>
              <JepGridHeaderCell>ID сессии</JepGridHeaderCell>
              <JepGridHeaderCell>Код авторизации</JepGridHeaderCell>
              <JepGridHeaderCell>Дата создания</JepGridHeaderCell>
              <JepGridHeaderCell>Логин оператора</JepGridHeaderCell>
              <JepGridHeaderCell>ID оператора</JepGridHeaderCell>
              <JepGridHeaderCell>URL переадресации</JepGridHeaderCell>
              <JepGridHeaderCell>Имя клиентского приложения</JepGridHeaderCell>
              <JepGridHeaderCell>ID клиентского приложения</JepGridHeaderCell>
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