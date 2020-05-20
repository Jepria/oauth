import React, { useEffect } from 'react';
import { Page, Content, Header as PageHeader, Footer, Header } from '../../../components/Layout';
import { setCurrentRecord, searchClients, postSearchClientRequest, deleteClient } from '../state/redux/actions';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory, useParams } from 'react-router-dom';
import { AppState } from '../../store';
import { ClientState } from '../types';
import { GrantType, ApplicationType } from '../../../security/OAuth';
import { Grid, TextCell } from '../../../components/grid';

export const ClientListPage: React.FC = () => {

  const dispatch = useDispatch();
  const history = useHistory();
  const { clientId } = useParams();
  const { records, current, searchId, searchRequest, resultSetSize } = useSelector<AppState, ClientState>(state => state.client);

  useEffect(() => {
    if (searchId && searchRequest) {
      dispatch(searchClients(searchId, 25, 1));
    } else if (!searchId && searchRequest) {
      dispatch(postSearchClientRequest(searchRequest));
    } else {
      dispatch(postSearchClientRequest({template: {}}));
    }
  }, [searchId, searchRequest, dispatch]);

  return (
    <Page>
      <Content>
        <Grid>
          <Grid.Table>
            <Grid.Header>
              <Grid.HeaderCell>ID клиентского приложения</Grid.HeaderCell>
              <Grid.HeaderCell>Секретное слово</Grid.HeaderCell>
              <Grid.HeaderCell>Наименование</Grid.HeaderCell>
              <Grid.HeaderCell>Наименование (англ)</Grid.HeaderCell>
              <Grid.HeaderCell>Тип приложения</Grid.HeaderCell>
              <Grid.HeaderCell>Разрешенные типы авторизации</Grid.HeaderCell>
            </Grid.Header>
            <Grid.Body>
              {records ? records.map(record => {
                return (
                  <Grid.Row key={record.clientId}
                    onClick={() => dispatch(setCurrentRecord(record))}
                    onDoubleClick={() => current !== record ? dispatch(setCurrentRecord(record,
                      () => history.push(`/ui/client/${record.clientId}/view`))) : history.push(`/ui/client/${record.clientId}/view`)}
                    selected={record === current}>
                    <Grid.Column label="ID клиентского приложения">
                      <TextCell>{record.clientId}</TextCell>
                    </Grid.Column>
                    <Grid.Column label="Секретное слово">
                      <TextCell>{record.clientSecret}</TextCell>
                    </Grid.Column>
                    <Grid.Column label="Наименование">
                      <TextCell>{record.clientName}</TextCell>
                    </Grid.Column>
                    <Grid.Column label="Наименование (англ)">
                      <TextCell>{record.clientNameEn}</TextCell>
                    </Grid.Column>
                    <Grid.Column label="Тип приложения">
                      <TextCell>{ApplicationType[record.applicationType]}</TextCell>
                    </Grid.Column>
                    <Grid.Column label="Разрешенные типы авторизации">
                      <TextCell wrapText>{record.grantTypes.map((grantType) => GrantType[grantType]).join(', ')}</TextCell>
                    </Grid.Column>
                  </Grid.Row>);
              }) : null}
            </Grid.Body>
          </Grid.Table>
          <Grid.PagingBar maxRowCount={resultSetSize} onChange={(page, pageSize) => {
            if (searchId) {
              dispatch(searchClients(searchId, pageSize, page))
            }
          }} />
        </Grid>
      </Content>
    </Page>
  );
}