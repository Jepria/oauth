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
import { Grid, GridTable, GridHeader, GridHeaderCell, GridBody, GridRow, GridRowCell, GridPagingBar } from '../../../components/grid/StyledGrid';

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
        <Grid>
          <GridTable>
            <GridHeader>
              <GridHeaderCell>ID сессии</GridHeaderCell>
              <GridHeaderCell>Код авторизации</GridHeaderCell>
              <GridHeaderCell>Дата создания</GridHeaderCell>
              <GridHeaderCell>Логин оператора</GridHeaderCell>
              <GridHeaderCell>ID оператора</GridHeaderCell>
              <GridHeaderCell>URL переадресации</GridHeaderCell>
              <GridHeaderCell>Имя клиентского приложения</GridHeaderCell>
              <GridHeaderCell>ID клиентского приложения</GridHeaderCell>
            </GridHeader>
            <GridBody>
              {records ? records.map(record => {
                return (
                  <GridRow key={record.sessionId}
                    onClick={() => dispatch(setCurrentRecord(record))}
                    onDoubleClick={() => current !== record ? dispatch(setCurrentRecord(record,
                      () => history.push(`/ui/session/${record.sessionId}/view`))) : history.push(`/ui/session/${record.sessionId}/view`)}
                    selected={record === current}>
                    <GridRowCell label="ID сессии">
                      <TextCell>{record.sessionId}</TextCell>
                    </GridRowCell>
                    <GridRowCell label="Код авторизации">
                      <TextCell>{record.authorizationCode}</TextCell>
                    </GridRowCell>
                    <GridRowCell label="Дата создания">
                      <DateCell>{record.dateIns}</DateCell>
                    </GridRowCell>
                    <GridRowCell label="Логин оператора">
                      <TextCell>{record.operatorLogin}</TextCell>
                    </GridRowCell>
                    <GridRowCell label="ID оператора">
                      <NumberCell>{record.operator?.value}</NumberCell>
                    </GridRowCell>
                    <GridRowCell label="URL переадресации">
                      <TextCell>{record.redirectUri}</TextCell>
                    </GridRowCell>
                    <GridRowCell label="Имя клиентского приложения">
                      <TextCell>{record.client?.name}</TextCell>
                    </GridRowCell>
                    <GridRowCell label="ID клиентского приложения">
                      <TextCell>{record.client?.value}</TextCell>
                    </GridRowCell>
                  </GridRow>);
              }): null}
            </GridBody>
          </GridTable>
          <GridPagingBar rowCount={records?.length} totalRowCount={resultSetSize} onRefresh={(page, pageSize) => {
            if (searchId) {
              dispatch(searchSessions(searchId, pageSize, page))
            }
          }} />
        </Grid>
      </Content>
    </Page>
  );
}

export default SessionListPage;