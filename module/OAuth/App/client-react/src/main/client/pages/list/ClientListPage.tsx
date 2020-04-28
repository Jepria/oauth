import React, { useEffect } from 'react';
import { Page, Content, Header as PageHeader, Footer, Header } from '../../../../components/page/Layout';
import { TabPanel, SelectedTab, Tab } from '../../../../components/tabpanel/TabPanel';
import { ToolBar } from '../../../../components/toolbar';
import * as DefaultButtons from '../../../../components/toolbar/ToolBarButtons';
import { setCurrentRecord, searchClients, postSearchClientRequest, deleteClient } from '../../state/redux/actions';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory, useParams } from 'react-router-dom';
import { AppState } from '../../../store';
import { ClientState } from '../../types';
import { GrantType, ApplicationType } from '../../../../security/OAuth';
import { PagingToolBar } from '../../../../components/PagingToolBar';
import { TextCell, Grid } from '../../../../components/grid';

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
    }
  }, [searchId, searchRequest, dispatch]);

  return (
    <Page>
      <PageHeader>
        <TabPanel>
          <SelectedTab>Клиент</SelectedTab>
        </TabPanel>
        <ToolBar>
          <DefaultButtons.CreateButton onCreate={() => {
            dispatch(setCurrentRecord(undefined));
            history.push('/ui/client/create')
          }} disabled={false} />
          <DefaultButtons.SaveButton onSave={() => { }} disabled />
          <DefaultButtons.EditButton onEdit={() => { history.push(`/ui/client/${clientId}/edit`) }} disabled={!current} />
          <DefaultButtons.ViewButton onView={() => { history.push(`/ui/client/${clientId}/view`) }} disabled={!current} />
          <DefaultButtons.DeleteButton onDelete={() => {
            if (current && current.clientId) {
              dispatch(deleteClient(current.clientId));
            }
          }} disabled={!current} />
          <DefaultButtons.Splitter />
          <DefaultButtons.ListButton onList={() => { }} disabled />
          <DefaultButtons.SearchButton onSearch={() => {
            history.push('/ui/client/search');
          }} />
          <DefaultButtons.DoSearchButton onDoSearch={() => { }} disabled />
        </ToolBar>
      </PageHeader>
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
              {records && records.map(record => {
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
              })}
            </Grid.Body>
          </Grid.Table>
          <Grid.PagingBar maxRowCount={resultSetSize} onChange={(page, pageSize) => {
            if (searchId) {
              dispatch(searchClients(searchId, pageSize, page))
            }
          }} />
        </Grid>
      </Content>
      {/* <Content>
        <TableContainer>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHeaderCell>ID клиентского приложения</TableHeaderCell>
                <TableHeaderCell>Секретное слово</TableHeaderCell>
                <TableHeaderCell>Наименование</TableHeaderCell>
                <TableHeaderCell>Наименование (англ)</TableHeaderCell>
                <TableHeaderCell>Тип приложения</TableHeaderCell>
                <TableHeaderCell>Разрешенные типы авторизации</TableHeaderCell>
              </TableRow>
            </TableHeader>
            <TableBody>
              {records && records.map(record => {
                return (
                  <TableRow key={record.clientId}
                    onClick={() => dispatch(setCurrentRecord(record))}
                    onDoubleClick={() => current !== record ? dispatch(setCurrentRecord(record,
                      () => history.push(`/ui/client/${record.clientId}/view`))) : history.push(`/ui/client/${record.clientId}/view`)}
                    selected={record === current}>
                    <TableColumn label="ID клиентского приложения">
                      <TextCell>{record.clientId}</TextCell>
                    </TableColumn>
                    <TableColumn label="Секретное слово">
                      <TextCell>{record.clientSecret}</TextCell>
                    </TableColumn>
                    <TableColumn label="Наименование">
                      <TextCell>{record.clientName}</TextCell>
                    </TableColumn>
                    <TableColumn label="Наименование (англ)">
                      <TextCell>{record.clientNameEn}</TextCell>
                    </TableColumn>
                    <TableColumn label="Тип приложения">
                      <TextCell>{ApplicationType[record.applicationType]}</TextCell>
                    </TableColumn>
                    <TableColumn label="Разрешенные типы авторизации">
                      <TextCell wrapText>{record.grantTypes.map((grantType) => GrantType[grantType]).join(', ')}</TextCell>
                    </TableColumn>
                  </TableRow>);
              })}
            </TableBody>
          </Table>
        </TableContainer>
      </Content>
      <Footer>
        <GridPagingBar maxRowCount={resultSetSize} onChange={(page, pageSize) => {
          if (searchId) {
            dispatch(searchClients(searchId, pageSize, page))
          }
        }}/>
      </Footer> */}
    </Page>
  );
}