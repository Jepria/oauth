import React, { useContext, useRef } from 'react';
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
import { useSelector } from 'react-redux';
import { Client } from './types';
import ClientSearchPage from './pages/ClientSearchPage';
import { ClientListPage } from './pages/ClientListPage';
import { HistoryState } from '../app/common/components/HistoryState';
import {
  Panel,
  TabPanel, 
  Tab,
  Loader
} from '@jfront/ui-core';
import { UserPanel } from '@jfront/oauth-ui';
import { UserContext } from '@jfront/oauth-user'
import { useTranslation } from 'react-i18next';
import { EntityState } from '@jfront/core-redux-saga';
import { ClientToolbar } from './components/ClientToolbar';

const ClientRoute: React.FC = () => {

  const { path } = useRouteMatch();
  const { pathname } = useLocation();
  const history = useHistory<HistoryState>();
  const { isRoleLoading } = useContext(UserContext);
  const { t } = useTranslation();
  const { isLoading, currentRecord } = useSelector<AppState, EntityState<Client>>(state => state.client.crudSlice)
  let formRef = useRef<HTMLFormElement>(null);

  return (
    <>
      <Panel>
        {(isLoading || isRoleLoading) && <Loader text={t("dataLoadingMessage")} />}
        <Panel.Header>
          <TabPanel>
            <Tab selected>{t('client.moduleName')}</Tab>
            {currentRecord && <Tab onClick={() => history.push(`/client/${currentRecord?.clientId}/client-uri/list`, { prevRoute: pathname })}>URL</Tab>}
            <UserPanel />
          </TabPanel>
          <ClientToolbar formRef={formRef}/>
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