import React, { useRef } from 'react';
import {
  Switch,
  Route,
  useRouteMatch,
  useHistory,
  useLocation,
  useParams
} from "react-router-dom";
import { ClientUriCreatePage} from './pages/ClientUriCreatePage';
import { ClientUriViewPage } from './pages/ClientUriViewPage';
import { AppState } from '../../../redux/store';
import { useSelector, useDispatch } from 'react-redux';
import { LoadingPanel } from '../../../components/mask';
import { ClientUriState } from './types';
import { ClientUriListPage } from './pages/ClientUriListPage';
import { setCurrentRecord, deleteClientUri, searchClientUri } from './state/redux/actions';
import { HistoryState } from '../../../components/HistoryState';
import { Panel, 
  TabPanel, Tab, Toolbar,
  ToolbarButtonCreate,
  ToolbarButtonDelete,
  ToolbarButtonSave, 
  ToolbarButtonView, 
  ToolbarSplitter, 
  ToolbarButtonBase } from '@jfront/ui-core';
import { UserPanel } from '../../../user/UserPanel';

const ClientUriRoute: React.FC = () => {

  const { path } = useRouteMatch();
  const { pathname, state } = useLocation<HistoryState>();
  const { clientId } = useParams<any>();
  const history = useHistory();
  const dispatch = useDispatch();
  const { isLoading, message, error, current } = useSelector<AppState, ClientUriState>(state => state.clientUri)
  let formRef = useRef(null) as any;
  
  return (
    <Panel>
      {isLoading && <LoadingPanel text={message} />}
      <Panel.Header>
        <TabPanel>
          <Tab onClick={() => history.push(state?.prevRoute? state.prevRoute : `/ui/client/${clientId}/view`)}>Клиент</Tab>
          <Tab selected>URL</Tab>
          <UserPanel/>
        </TabPanel>
        <Toolbar style={{margin: 0}}>
          <ToolbarButtonCreate onClick={() => {
            dispatch(setCurrentRecord(undefined, () => {
              history.push(`/ui/client/${clientId}/client-uri/create`, state)
            }));
          }} disabled={pathname.endsWith('/create')} />
          <ToolbarButtonSave onClick={() => { formRef.current?.dispatchEvent(new Event("submit")) }} disabled={!pathname.endsWith('/create')} />
          <ToolbarButtonView onClick={() => { history.push(`/ui/client/${clientId}/client-uri/${current?.clientUriId}/view`, state) }} disabled={!current || pathname.endsWith('view')} />
          <ToolbarButtonDelete onClick={() => {
            if (clientId && current?.clientUriId) {
              if (window.confirm('Вы точно хотите удалить запись?')) {
                dispatch(deleteClientUri(clientId, `${current.clientUriId}`, () => {
                  if (pathname.endsWith('/list')) {
                    dispatch(searchClientUri(clientId));
                  } else {
                    history.push(`/ui/client/${clientId}/client-uri/list`, state);
                  }
                }));
              }
            }
          }} disabled={!current} />
          <ToolbarSplitter />
          <ToolbarButtonBase onClick={() => {
            dispatch(setCurrentRecord(undefined, () => {
              history.push(`/ui/client/${clientId}/client-uri/list`, state);
            }))
          }} disabled={pathname.endsWith('/list')}>Список</ToolbarButtonBase>
        </Toolbar>
      </Panel.Header>
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
  );
}

export default ClientUriRoute;