import React, { useEffect } from 'react';
import { Page, Content, Header as PageHeader, Footer, Header } from '../../../components/Layout';
import { setCurrentRecord, searchClients, postSearchClientRequest, deleteClient } from '../state/redux/actions';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory, useParams } from 'react-router-dom';
import { AppState } from '../../store';
import { ClientState } from '../types';
import { GrantType, ApplicationType } from '../../../security/OAuth';
import { TextCell } from '../../../components/cell/TextCell';
import { Grid, GridTable, GridHeader, GridHeaderCell, GridBody, GridRow, GridRowCell, GridPagingBar } from '../../../components/grid/StyledGrid';

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
          <GridTable>
            <GridHeader>
              <GridHeaderCell>ID клиентского приложения</GridHeaderCell>
              <GridHeaderCell>Секретное слово</GridHeaderCell>
              <GridHeaderCell>Наименование</GridHeaderCell>
              <GridHeaderCell>Наименование (англ)</GridHeaderCell>
              <GridHeaderCell>Тип приложения</GridHeaderCell>
              <GridHeaderCell>Разрешенные типы авторизации</GridHeaderCell>
            </GridHeader>
            <GridBody>
              {records ? records.map(record => {
                return (
                  <GridRow key={record.clientId}
                    onClick={() => dispatch(setCurrentRecord(record))}
                    onDoubleClick={() => current !== record ? dispatch(setCurrentRecord(record,
                      () => history.push(`/ui/client/${record.clientId}/view`))) : history.push(`/ui/client/${record.clientId}/view`)}
                    selected={record === current}>
                    <GridRowCell label="ID клиентского приложения">
                      <TextCell>{record.clientId}</TextCell>
                    </GridRowCell>
                    <GridRowCell label="Секретное слово">
                      <TextCell>{record.clientSecret}</TextCell>
                    </GridRowCell>
                    <GridRowCell label="Наименование">
                      <TextCell>{record.clientName}</TextCell>
                    </GridRowCell>
                    <GridRowCell label="Наименование (англ)">
                      <TextCell>{record.clientNameEn}</TextCell>
                    </GridRowCell>
                    <GridRowCell label="Тип приложения">
                      <TextCell>{ApplicationType[record.applicationType]}</TextCell>
                    </GridRowCell>
                    <GridRowCell label="Разрешенные типы авторизации">
                      <TextCell wrapText>{record.grantTypes.map((grantType) => GrantType[grantType]).join(', ')}</TextCell>
                    </GridRowCell>
                  </GridRow>);
              }) : null}
            </GridBody>
          </GridTable>
          <GridPagingBar rowCount={records?.length} totalRowCount={resultSetSize} onRefresh={(page, pageSize) => {
            if (searchId) {
              dispatch(searchClients(searchId, pageSize, page))
            }
          }} />
        </Grid>
      </Content>
    </Page>
  );
}