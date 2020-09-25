import React, { useContext, useEffect, useRef, useState } from 'react';
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
import { AppState } from '../../redux/store';
import { useSelector, useDispatch } from 'react-redux';
import { LoadingPanel } from '../../components/mask';
import { ClientState } from './types';
import ClientSearchPage from './pages/ClientSearchPage';
import { ClientListPage } from './pages/ClientListPage';
import { setCurrentRecord, deleteClient, searchClients } from './state/redux/actions';
import { HistoryState } from '../../components/HistoryState';
import {
  Panel,
  TabPanel, Tab, Toolbar,
  ToolbarButtonCreate,
  ToolbarButtonEdit,
  ToolbarButtonDelete,
  ToolbarButtonFind,
  ToolbarButtonSave,
  ToolbarButtonView,
  ToolbarSplitter,
  ToolbarButtonBase
} from '@jfront/ui-core';
import { UserPanel } from '../../user/UserPanel';
import { UserContext } from '../../user/UserContext';

const ClientRoute: React.FC = () => {

  const { path } = useRouteMatch();
  const { pathname } = useLocation();
  const history = useHistory<HistoryState>();
  const dispatch = useDispatch();
  const { isRoleLoading, isUserInRole } = useContext(UserContext);
  const [hasCreateRole, setHasCreateRole] = useState(false);
  const [hasEditRole, setHasEditRole] = useState(false);
  const [hasDeleteRole, setHasDeleteRole] = useState(false);
  const { isLoading, message, error, current, searchId, searchRequest } = useSelector<AppState, ClientState>(state => state.client)
  let formRef = useRef<HTMLFormElement>(null);

  useEffect(() => {
    isUserInRole("OACreateClient")
      .then(setHasCreateRole);
    isUserInRole("OAEditClient")
      .then(setHasEditRole);
    isUserInRole("OADeleteClient")
      .then(setHasDeleteRole);
  }, [])

  return (
    <Panel>
      {(isLoading || isRoleLoading) && <LoadingPanel text={message || "Загрузка данных"} />}
      <Panel.Header>
        <TabPanel>
          <Tab selected>Клиент</Tab>
          {current && <Tab onClick={() => history.push(`/ui/client/${current?.clientId}/client-uri/list`, { prevRoute: pathname })}>URL</Tab>}
          <UserPanel />
        </TabPanel>
        <Toolbar style={{ margin: 0 }}>
          <ToolbarButtonCreate onClick={() => {
            dispatch(setCurrentRecord(undefined, () => {
              history.push('/ui/client/create')
            }));
          }} disabled={pathname.endsWith('/create') || !hasCreateRole} />
          <ToolbarButtonSave
            onClick={() => { formRef.current?.dispatchEvent(new Event("submit")) }}
            disabled={(!pathname.endsWith('/create') && !pathname.endsWith('/edit')) || (!hasCreateRole && !hasEditRole)} />
          <ToolbarButtonEdit
            onClick={() => history.push(`/ui/client/${current?.clientId}/edit`)}
            disabled={!current || pathname.endsWith('/edit') || pathname.endsWith('/edit/') || !hasEditRole} />
          <ToolbarButtonView
            onClick={() => { history.push(`/ui/client/${current?.clientId}/view`) }}
            disabled={!current || pathname.endsWith('/view') || pathname.endsWith('/view/')} />
          <ToolbarButtonDelete onClick={() => {
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
          }} disabled={!current || !hasDeleteRole} />
          <ToolbarSplitter />
          <ToolbarButtonBase onClick={() => {
            dispatch(setCurrentRecord(undefined, () => {
              if (searchRequest) {
                history.push('/ui/client/list');
              } else {
                history.push('/ui/client/search');
              }
            }))
          }} disabled={pathname.endsWith('/search') || pathname.endsWith('/list')}>Список</ToolbarButtonBase>
          <ToolbarButtonFind onClick={() => {
            dispatch(setCurrentRecord(undefined, () => history.push('/ui/client/search')));
          }} />
          <ToolbarButtonBase onClick={() => { formRef.current?.dispatchEvent(new Event("submit")) }} disabled={!pathname.endsWith('/search')}>Найти</ToolbarButtonBase>
        </Toolbar>
      </Panel.Header>
      <Panel.Content>
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
      </Panel.Content>
    </Panel>
  );
}

export default ClientRoute;