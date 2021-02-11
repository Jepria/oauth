import React, { useContext, useEffect, useRef } from 'react';
import {
  Switch,
  Route,
  useRouteMatch,
  useHistory,
  useLocation,
  useParams
} from "react-router-dom";
import { ClientUriCreatePage } from './pages/ClientUriCreatePage';
import { ClientUriViewPage } from './pages/ClientUriViewPage';
import { AppState } from '../../app/store/reducer';
import { useSelector, useDispatch } from 'react-redux';
import { ClientUri } from './types';
import { ClientUriListPage } from './pages/ClientUriListPage';
import { HistoryState } from '../../app/common/components/HistoryState';
import {
  Panel,
  TabPanel,
  Tab,
  Loader
} from '@jfront/ui-core';
import { UserPanel } from '@jfront/oauth-ui';
import { useTranslation } from 'react-i18next';
import { Client } from '../types';
import { actions as clientActions } from '../state/clientCrudSlice'
import { EntityState } from '@jfront/core-redux-saga';
import { UserContext } from '@jfront/oauth-user';
import { ClientUriToolbar } from './components/ClientUriToolbar';

const ClientUriRoute: React.FC = () => {

  const { path } = useRouteMatch();
  const { state } = useLocation<HistoryState>();
  const { clientId } = useParams<any>();
  const history = useHistory();
  const dispatch = useDispatch();
  const { t } = useTranslation();
  const { isRoleLoading } = useContext(UserContext);
  const { isLoading } = useSelector<AppState, EntityState<ClientUri>>(state => state.clientUri.crudSlice);
  const client = useSelector<AppState, EntityState<Client>>(state => state.client.crudSlice)
  let formRef = useRef(null) as any;

  useEffect(() => {
    if (!client.currentRecord) {
      dispatch(clientActions.getRecordById({ primaryKey: clientId }))
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [client, clientId, dispatch])

  return (
    <>
      {(isLoading || isRoleLoading) && <Loader text={t("dataLoadingMessage")} />}
      <Panel>
        <Panel.Header>
          <TabPanel>
            <Tab onClick={() => history.push(state?.prevRoute ? state.prevRoute : `/ui/client/${clientId}/detail`)}>{t('client.moduleName')}</Tab>
            <Tab selected>{t('clientUri.moduleName')}</Tab>
            <UserPanel />
          </TabPanel>
          <ClientUriToolbar formRef={formRef}/>
        </Panel.Header>
        <Panel.Content>
          <Panel>
            {client.currentRecord && <Panel.Header
              style={{ backgroundImage: "linear-gradient(rgb(255, 255, 255), rgb(208, 222, 240))" }}>
              <div
                style={{ margin: "5px", fontSize: "11px", fontWeight: "bold", color: "rgb(21, 66, 139)" }}>
                {client.currentRecord?.clientName}
              </div>
            </Panel.Header>}
            <Panel.Content>
              <Switch>
                <Route path={`${path}/create`}>
                  <ClientUriCreatePage ref={formRef} />
                </Route>
                <Route path={`${path}/:clientUriId/detail`}>
                  <ClientUriViewPage />
                </Route>
                <Route path={`${path}/list`}>
                  <ClientUriListPage />
                </Route>
              </Switch>
            </Panel.Content>
          </Panel>
        </Panel.Content>
      </Panel>
    </>
  );
}

export default ClientUriRoute;