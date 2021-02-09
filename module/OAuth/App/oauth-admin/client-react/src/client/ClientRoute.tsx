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
import { AppState } from '../app/store/reducer';
import { useSelector, useDispatch } from 'react-redux';
import { Client, ClientSearchTemplate } from './types';
import ClientSearchPage from './pages/ClientSearchPage';
import { ClientListPage } from './pages/ClientListPage';
import { actions as searchActions } from './state/clientSearchSlice';
import { actions as crudActions } from './state/clientCrudSlice';
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
  ToolbarButtonBase,
  Loader
} from '@jfront/ui-core';
import { UserPanel } from '@jfront/oauth-ui';
import { UserContext } from '@jfront/oauth-user'
import { useTranslation } from 'react-i18next';
import { EntityState, SearchState } from '@jfront/core-redux-saga';
import { createEvent, useWorkstate, Workstates } from '@jfront/core-common';

const ClientRoute: React.FC = () => {

  const { path } = useRouteMatch();
  const { pathname } = useLocation();
  const workstate = useWorkstate(pathname);
  const history = useHistory<HistoryState>();
  const dispatch = useDispatch();
  const { isRoleLoading, isUserInRole } = useContext(UserContext);
  const [hasCreateRole, setHasCreateRole] = useState(false);
  const [hasEditRole, setHasEditRole] = useState(false);
  const [hasDeleteRole, setHasDeleteRole] = useState(false);
  const { t } = useTranslation();
  const { isLoading, currentRecord, selectedRecords } = useSelector<AppState, EntityState<Client>>(state => state.client.crudSlice)
  const { searchId, searchRequest, pageSize, pageNumber } = useSelector<AppState, SearchState<ClientSearchTemplate, Client>>(state => state.client.searchSlice)
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
        {(isLoading || isRoleLoading) && <Loader text={t("dataLoadingMessage")} />}
        <Panel.Header>
          <TabPanel>
            <Tab selected>{t('client.moduleName')}</Tab>
            {currentRecord && <Tab onClick={() => history.push(`/ui/client/${currentRecord?.clientId}/client-uri/list`, { prevRoute: pathname })}>URL</Tab>}
            <UserPanel />
          </TabPanel>
          <Toolbar style={{ margin: 0 }}>
            {hasCreateRole && <ToolbarButtonCreate onClick={() => {
              dispatch(crudActions.setCurrentRecord({
                currentRecord: undefined as any, callback: () => {
                  history.push('/ui/client/create')
                }
              }));
            }} disabled={workstate === Workstates.Create} />}
            {(hasCreateRole || hasEditRole) && <ToolbarButtonSave
              onClick={() => { formRef.current?.dispatchEvent(createEvent("submit")) }}
              disabled={workstate !== Workstates.Create && workstate !== Workstates.Edit} />}
            {hasEditRole && <ToolbarButtonEdit
              onClick={() => history.push(`/ui/client/${currentRecord?.clientId}/edit`)}
              disabled={!currentRecord || workstate === Workstates.Edit} />}
            <ToolbarButtonView
              onClick={() => { history.push(`/ui/client/${currentRecord?.clientId}/detail`) }}
              disabled={!currentRecord || workstate === Workstates.Detail} />
            {hasDeleteRole && <ToolbarButtonDelete onClick={() => {
              if (window.confirm(t('delete'))) {
                dispatch(crudActions.delete({
                  primaryKeys: selectedRecords.map(selectedRecord => selectedRecord.clientId),
                  onSuccess: () => {
                    if (workstate === Workstates.List) {
                      if (searchId) {
                        dispatch(searchActions.search({
                          searchId,
                          pageSize,
                          pageNumber
                        }));
                      } else if (searchRequest) {
                        dispatch(searchActions.postSearchRequest({ searchTemplate: searchRequest }))
                      }
                    } else {
                      history.push('/ui/client/list');
                    }
                  }
                }));
              }
            }} disabled={selectedRecords.length === 0} />}
            <ToolbarSplitter />
            <ToolbarButtonBase onClick={() => {
              dispatch(crudActions.setCurrentRecord({
                callback: () => {
                  if (searchRequest) {
                    history.push('/ui/client/list');
                  } else {
                    history.push('/ui/client');
                  }
                }
              }))
            }} disabled={workstate === Workstates.Search || workstate === Workstates.List}>{t('toolbar.list')}</ToolbarButtonBase>
            <ToolbarButtonFind onClick={() => {
              dispatch(crudActions.setCurrentRecord({
                callback: () => {
                  history.push('/ui/client')
                }
              }));
            }} />
            <ToolbarButtonBase
              onClick={() => { formRef.current?.dispatchEvent(createEvent("submit")) }}
              disabled={workstate !== Workstates.Search}>
              {t('toolbar.find')}
            </ToolbarButtonBase>
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
            <Route path={`${path}/:clientId/detail`}>
              <ClientViewPage />
            </Route>
            <Route path={`${path}`} exact>
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