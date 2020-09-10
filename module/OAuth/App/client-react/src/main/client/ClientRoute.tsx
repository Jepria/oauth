import React, { useRef } from 'react';
import {
  Switch,
  Route,
  useRouteMatch,
  useHistory,
  useLocation
} from "react-router-dom";
import ClientCreatePage from './pages/ClientCreatePage';
import ClientEditPage from './pages/ClientEditPage';
import ClientViewPage from './pages/ClientViewPage';
import { AppState } from '../store';
import { useSelector, useDispatch } from 'react-redux';
import { LoadingPanel } from '../../components/mask';
import { ClientState } from './types';
import ClientSearchPage from './pages/ClientSearchPage';
import { ClientListPage } from './pages/ClientListPage';
import { TabPanel, SelectedTab, Tab } from '../../components/tabpanel/TabPanel';
import { ToolBar } from '../../components/toolbar';
import * as DefaultButtons from '../../components/toolbar/ToolBarButtons';
import { setCurrentRecord, deleteClient, searchClients } from './state/redux/actions';
import { HistoryState } from '../../components/HistoryState';
import { Page, Header, Content } from '@jfront/ui-core';
import { UserPanel } from '../../components/tabpanel/UserPanel';

const ClientRoute: React.FC = () => {

  const { path } = useRouteMatch();
  const { pathname } = useLocation();
  const history = useHistory<HistoryState>();
  const dispatch = useDispatch();
  const { isLoading, message, error, current, searchId, searchRequest } = useSelector<AppState, ClientState>(state => state.client)
  let formRef = useRef<HTMLFormElement>(null);
  
  return (
    <Page>
      {isLoading && <LoadingPanel text={message} />}
      <Header>
        <TabPanel>
          <SelectedTab>Клиент</SelectedTab>
          {current && <Tab onClick={() => history.push(`/ui/client/${current?.clientId}/client-uri/list`, {prevRoute: pathname})}>URL</Tab>}
          <UserPanel/>
        </TabPanel>
        <ToolBar>
          <DefaultButtons.CreateButton onCreate={() => {
            dispatch(setCurrentRecord(undefined, () => {
              history.push('/ui/client/create')
            }));
          }} disabled={pathname.endsWith('/create')} />
          <DefaultButtons.SaveButton onSave={() => { formRef.current?.dispatchEvent(new Event("submit")) }} disabled={!pathname.endsWith('/create') && !pathname.endsWith('/edit')} />
          <DefaultButtons.EditButton onEdit={() => history.push(`/ui/client/${current?.clientId}/edit`)} disabled={!current || pathname.endsWith('/edit')} />
          <DefaultButtons.ViewButton onView={() => { history.push(`/ui/client/${current?.clientId}/view`) }} disabled={!current || pathname.endsWith('view')} />
          <DefaultButtons.DeleteButton onDelete={() => {
            if (current?.clientId) {
              if (window.confirm('Вы точно хотите удалить запись?')) {
                dispatch(deleteClient(current.clientId, () => {
                  if (pathname.endsWith('/list') && searchId) {
                    dispatch(searchClients(searchId, 25, 1));
                  } else {
                    history.push('/ui/client/list');
                  }
                }));
              }
            }
          }} disabled={!current} />
          <DefaultButtons.Splitter />
          <DefaultButtons.ListButton onList={() => {
            dispatch(setCurrentRecord(undefined, () => {
              if (searchRequest) {
                history.push('/ui/client/list');
              } else {
                history.push('/ui/client/search');
              }
            }))
          }} disabled={pathname.endsWith('/search') || pathname.endsWith('/list')} />
          <DefaultButtons.SearchButton onSearch={() => {
            dispatch(setCurrentRecord(undefined, () => history.push('/ui/client/search')));
          }} />
          <DefaultButtons.DoSearchButton onDoSearch={() => { formRef.current?.handleSubmit() }} disabled={!pathname.endsWith('/search')} />
        </ToolBar>
      </Header>
      <Content>
        <Switch>
          <Route path={`${path}/create`}>
            <ClientCreatePage ref={formRef} />
          </Route>
          <Route path={`${path}/:clientId/edit`}>
            <ClientEditPage ref={formRef} />
          </Route>
          <Route path={`${path}/:clientId/view`}>
            <ClientViewPage />
          </Route>
          <Route path={`${path}/search`}>
            <ClientSearchPage ref={formRef} />
          </Route>
          <Route path={`${path}/list`}>
            <ClientListPage />
          </Route>
        </Switch>
      </Content>
    </Page>
  );
}

export default ClientRoute;