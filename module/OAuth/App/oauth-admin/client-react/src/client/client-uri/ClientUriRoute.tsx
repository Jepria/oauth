import React, { useEffect, useRef } from 'react';
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
import { LoadingPanel } from '../../app/common/components/mask';
import { ClientUriState } from './types';
import { ClientUriListPage } from './pages/ClientUriListPage';
import { actions } from './state/clientUriSlice';
import { HistoryState } from '../../app/common/components/HistoryState';
import {
  Panel,
  TabPanel, Tab, Toolbar,
  ToolbarButtonCreate,
  ToolbarButtonDelete,
  ToolbarButtonSave,
  ToolbarButtonView,
  ToolbarSplitter,
  ToolbarButtonBase
} from '@jfront/ui-core';
import { UserPanel } from '@jfront/oauth-ui';
import { useTranslation } from 'react-i18next';
import { ClientState } from '../types';
import { actions as clientActions } from '../state/clientSlice'

const ClientUriRoute: React.FC = () => {

  const { path } = useRouteMatch();
  const { pathname, state } = useLocation<HistoryState>();
  const { clientId } = useParams<any>();
  const history = useHistory();
  const dispatch = useDispatch();
  const { t } = useTranslation();
  const { isLoading, message, current, selectedRecords } = useSelector<AppState, ClientUriState>(state => state.clientUri)
  const client = useSelector<AppState, ClientState>(state => state.client)
  let formRef = useRef(null) as any;

  useEffect(() => {
    if (!client.current) {
      dispatch(clientActions.getRecordById({clientId, loadingMessage: t('dataLoadingMessage')}))
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [client, clientId, dispatch])

  return (
    <Panel>
      {isLoading && <LoadingPanel text={message} />}
      <Panel.Header>
        <TabPanel>
          <Tab onClick={() => history.push(state?.prevRoute ? state.prevRoute : `/ui/client/${clientId}/view`)}>{t('client.moduleName')}</Tab>
          <Tab selected>{t('clientUri.moduleName')}</Tab>
          <UserPanel />
        </TabPanel>
        <Toolbar style={{ margin: 0 }}>
          <ToolbarButtonCreate onClick={() => {
            dispatch(actions.setCurrentRecord({
              currentRecord: undefined,
              callback: () => {
                history.push(`/ui/client/${clientId}/client-uri/create`, state)
              }
            }));
          }} disabled={pathname.endsWith('/create')} />
          <ToolbarButtonSave
            onClick={() => { formRef.current?.dispatchEvent(new Event("submit")) }}
            disabled={!pathname.endsWith('/create')} />
          <ToolbarButtonView
            onClick={() => { history.push(`/ui/client/${clientId}/client-uri/${current?.clientUriId}/view`, state) }}
            disabled={!current || pathname.endsWith('view')} />
          <ToolbarButtonDelete onClick={() => {
            if (window.confirm(t('delete'))) {
              dispatch(actions.remove({
                clientId, clientUriIds: selectedRecords.map(selectedRecord => String(selectedRecord.clientUriId)),
                loadingMessage: t('deleteMessage'), callback: () => {
                  if (pathname.endsWith('/list')) {
                    dispatch(actions.search({ clientId, loadingMessage: t('dataLoadingMessage') }));
                  } else {
                    history.push(`/ui/client/${clientId}/client-uri/list`, state);
                  }
                }
              }));
            }
          }} disabled={selectedRecords.length === 0} />
          <ToolbarSplitter />
          <ToolbarButtonBase onClick={() => {
            dispatch(actions.setCurrentRecord({
              currentRecord: undefined,
              callback: () => {
                history.push(`/ui/client/${clientId}/client-uri/list`, state)
              }
            }));
          }} disabled={pathname.endsWith('/list')}>{t('toolbar.list')}</ToolbarButtonBase>
        </Toolbar>
      </Panel.Header>
      <Panel.Content>
        <Panel>
          {client.current && <Panel.Header
            style={{ backgroundImage: "linear-gradient(rgb(255, 255, 255), rgb(208, 222, 240))" }}>
            <div
              style={{ margin: "5px", fontSize: "11px", fontWeight: "bold", color: "rgb(21, 66, 139)" }}>
              {client.current?.clientName}
            </div>
          </Panel.Header>}
          <Panel.Content>
            <Switch>
              <Route path={`${path}/create`}>
                <ClientUriCreatePage ref={formRef} />
              </Route>
              <Route path={`${path}/:clientUriId/view`}>
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
  );
}

export default ClientUriRoute;