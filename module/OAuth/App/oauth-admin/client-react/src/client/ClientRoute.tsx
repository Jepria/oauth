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
import { AppState } from '../app/store';
import { useSelector, useDispatch } from 'react-redux';
import { LoadingPanel } from '../app/common/components/mask';
import { ClientState } from './types';
import ClientSearchPage from './pages/ClientSearchPage';
import { ClientListPage } from './pages/ClientListPage';
import { setCurrentRecord, deleteClient, searchClients } from './state/actions';
import { HistoryState } from '../app/common/components/HistoryState';
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
import { UserPanel } from '@jfront/oauth-ui';
import { UserContext } from '@jfront/oauth-user'
import { useTranslation } from 'react-i18next';

const ClientRoute: React.FC = () => {

  const { path } = useRouteMatch();
  const { pathname } = useLocation();
  const history = useHistory<HistoryState>();
  const dispatch = useDispatch();
  const { isRoleLoading, isUserInRole } = useContext(UserContext);
  const [hasCreateRole, setHasCreateRole] = useState(false);
  const [hasEditRole, setHasEditRole] = useState(false);
  const [hasDeleteRole, setHasDeleteRole] = useState(false);
  const { t } = useTranslation();
  const { isLoading, message, current, selectedRecords, searchId, searchRequest } = useSelector<AppState, ClientState>(state => state.client)
  let formRef = useRef<HTMLFormElement>(null);

  useEffect(() => {
    isUserInRole("OACreateClient")
      .then(setHasCreateRole);
    isUserInRole("OAEditClient")
      .then(setHasEditRole);
    isUserInRole("OADeleteClient")
      .then(setHasDeleteRole);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  return (
    <>
      <Panel>
        {(isLoading || isRoleLoading) && <LoadingPanel text={message || "Загрузка данных"} />}
        <Panel.Header>
          <TabPanel>
            <Tab selected>{t('client.moduleName')}</Tab>
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
              if (window.confirm(t('delete'))) {
                dispatch(deleteClient(selectedRecords.map(selectRecord => selectRecord.clientId), t('deleteMessage'), () => {
                  if (pathname.endsWith('/list') && searchId) {
                    dispatch(searchClients(searchId, 25, 1, t('dataLoadingMessage')));
                  } else {
                    history.push('/ui/client/list');
                  }
                }));
              }
            }} disabled={selectedRecords.length === 0 || !hasDeleteRole} />
            <ToolbarSplitter />
            <ToolbarButtonBase onClick={() => {
              dispatch(setCurrentRecord(undefined, () => {
                if (searchRequest) {
                  history.push('/ui/client/list');
                } else {
                  history.push('/ui/client/search');
                }
              }))
            }} disabled={pathname.endsWith('/search') || pathname.endsWith('/list')}>{t('toolbar.list')}</ToolbarButtonBase>
            <ToolbarButtonFind onClick={() => {
              dispatch(setCurrentRecord(undefined, () => history.push('/ui/client/search')));
            }} />
            <ToolbarButtonBase onClick={() => { formRef.current?.dispatchEvent(new Event("submit")) }} disabled={!pathname.endsWith('/search')}>{t('toolbar.find')}</ToolbarButtonBase>
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
    </>
  );
}

export default ClientRoute;