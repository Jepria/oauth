import React, { useEffect } from 'react';
import { Page, Content } from '../../../components/Layout';
import { setCurrentRecord, searchSessions, postSearchSessionRequest } from '../state/redux/actions';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory } from 'react-router-dom';
import { AppState } from '../../store';
import { SessionState } from '../types';
import { TextCell, Grid, DateCell, NumberCell } from '../../../components/grid';

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
          <Grid.Table>
            <Grid.Header>
              <Grid.HeaderCell>ID сессии</Grid.HeaderCell>
              <Grid.HeaderCell>Код авторизации</Grid.HeaderCell>
              <Grid.HeaderCell>Дата создания</Grid.HeaderCell>
              <Grid.HeaderCell>Логин оператора</Grid.HeaderCell>
              <Grid.HeaderCell>ID оператора</Grid.HeaderCell>
              <Grid.HeaderCell>URL переадресации</Grid.HeaderCell>
              <Grid.HeaderCell>Имя клиентского приложения</Grid.HeaderCell>
              <Grid.HeaderCell>ID клиентского приложения</Grid.HeaderCell>
            </Grid.Header>
            <Grid.Body>
              {records ? records.map(record => {
                return (
                  <Grid.Row key={record.sessionId}
                    onClick={() => dispatch(setCurrentRecord(record))}
                    onDoubleClick={() => current !== record ? dispatch(setCurrentRecord(record,
                      () => history.push(`/ui/session/${record.sessionId}/view`))) : history.push(`/ui/session/${record.sessionId}/view`)}
                    selected={record === current}>
                    <Grid.Column label="ID сессии">
                      <TextCell>{record.sessionId}</TextCell>
                    </Grid.Column>
                    <Grid.Column label="Код авторизации">
                      <TextCell>{record.authorizationCode}</TextCell>
                    </Grid.Column>
                    <Grid.Column label="Дата создания">
                      <DateCell>{record.dateIns}</DateCell>
                    </Grid.Column>
                    <Grid.Column label="Логин оператора">
                      <TextCell>{record.operatorLogin}</TextCell>
                    </Grid.Column>
                    <Grid.Column label="ID оператора">
                      <NumberCell>{record.operator?.value}</NumberCell>
                    </Grid.Column>
                    <Grid.Column label="URL переадресации">
                      <TextCell>{record.redirectUri}</TextCell>
                    </Grid.Column>
                    <Grid.Column label="Имя клиентского приложения">
                      <TextCell>{record.client?.name}</TextCell>
                    </Grid.Column>
                    <Grid.Column label="ID клиентского приложения">
                      <TextCell>{record.client?.value}</TextCell>
                    </Grid.Column>
                  </Grid.Row>);
              }): null}
            </Grid.Body>
          </Grid.Table>
          <Grid.PagingBar maxRowCount={resultSetSize} onChange={(page, pageSize) => {
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