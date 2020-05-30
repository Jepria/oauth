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
import { AppState } from '../../store';
import { useSelector, useDispatch } from 'react-redux';
import { LoadingPanel } from '../../../components/mask';
import { ClientUriState } from './types';
import { ClientUriListPage } from './pages/ClientUriListPage';
import { TabPanel, SelectedTab, Tab } from '../../../components/tabpanel/TabPanel';
import { ToolBar } from '../../../components/toolbar';
import * as DefaultButtons from '../../../components/toolbar/ToolBarButtons';
import { setCurrentRecord, deleteClientUri, searchClientUri } from './state/redux/actions';
import { HistoryState } from '../../../components/HistoryState';
import { Page, Header, Content } from 'jfront-components';

const ClientUriRoute: React.FC = () => {

  const { path } = useRouteMatch();
  const { pathname, state } = useLocation<HistoryState>();
  const { clientId } = useParams();
  const history = useHistory();
  const dispatch = useDispatch();
  const { isLoading, message, error, current } = useSelector<AppState, ClientUriState>(state => state.clientUri)
  let formRef = useRef(null) as any;
  
  return (
    <Page>
      {isLoading && <LoadingPanel text={message} />}
      <Header>
        <TabPanel>
          {!pathname.endsWith('/view') && <Tab onClick={() => history.push(state?.prevRoute? state.prevRoute : `/ui/client/${clientId}/view`)}>Клиент</Tab>}
          <SelectedTab>URL</SelectedTab>
        </TabPanel>
        <ToolBar>
          <DefaultButtons.CreateButton onCreate={() => {
            dispatch(setCurrentRecord(undefined, () => {
              history.push(`/ui/client/${clientId}/client-uri/create`, state)
            }));
          }} disabled={pathname.endsWith('/create')} />
          <DefaultButtons.SaveButton onSave={() => { formRef.current?.handleSubmit() }} disabled={!pathname.endsWith('/create')} />
          <DefaultButtons.ViewButton onView={() => { history.push(`/ui/client/${clientId}/client-uri/${current?.clientUriId}/view`, state) }} disabled={!current || pathname.endsWith('view')} />
          <DefaultButtons.DeleteButton onDelete={() => {
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
          <DefaultButtons.Splitter />
          <DefaultButtons.ListButton onList={() => {
            dispatch(setCurrentRecord(undefined, () => {
              history.push(`/ui/client/${clientId}/client-uri/list`, state);
            }))
          }} disabled={pathname.endsWith('/list')} />
        </ToolBar>
      </Header>
      <Content>
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
      </Content>
    </Page>
  );
}

export default ClientUriRoute;