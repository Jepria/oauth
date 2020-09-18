import React, { useRef } from 'react';
import {
  Switch,
  Route,
  useRouteMatch,
  useHistory,
  useLocation
} from "react-router-dom";
import SessionViewPage from './pages/SessionViewPage';
import { AppState } from '../store';
import { useSelector, useDispatch } from 'react-redux';
import { LoadingPanel } from '../../components/mask';
import { SessionState } from './types';
import SessionSearchPage from './pages/SessionSearchPage';
import SessionListPage from './pages/SessionListPage';
import { setCurrentRecord, deleteSession, searchSessions } from './state/redux/actions';
import { HistoryState } from '../../components/HistoryState';
import {
  Panel,
  TabPanel, Tab, Toolbar,
  ToolbarButtonDelete,
  ToolbarButtonFind,
  ToolbarButtonView,
  ToolbarSplitter,
  ToolbarButtonBase
} from '@jfront/ui-core';
import { UserPanel } from '../../components/tabpanel/UserPanel';

const SessionRoute: React.FC = () => {

  const { path } = useRouteMatch();
  const { pathname } = useLocation();
  const history = useHistory<HistoryState>();
  const dispatch = useDispatch();
  const { isLoading, message, error, current, searchId, searchRequest } = useSelector<AppState, SessionState>(state => state.session)
  let formRef = useRef(null) as any;
  
  return (
    <Panel>
      {isLoading && <LoadingPanel text={message} />}
      <Panel.Header>
        <TabPanel>
          <Tab selected>Сессия</Tab>
          <UserPanel/>
        </TabPanel>
        <Toolbar style={{margin: 0}}>
          <ToolbarButtonView onClick={() => { history.push(`/ui/session/${current?.sessionId}/view`) }} disabled={!current || pathname.endsWith('view')} />
          <ToolbarButtonDelete onClick={() => {
            if (current?.sessionId) {
              if (window.confirm('Вы точно хотите удалить запись?')) {
                dispatch(deleteSession(`${current.sessionId}`, () => {
                  if (pathname.endsWith('/list') && searchId) {
                    dispatch(searchSessions(searchId, 25, 1));
                  } else {
                    history.push('/ui/session/list');
                  }
                }));
              }
            }
          }} disabled={!current} />
          <ToolbarSplitter/>
          <ToolbarButtonBase onClick={() => {
            dispatch(setCurrentRecord(undefined, () => {
              if (searchRequest) {
                history.push('/ui/session/list');
              } else {
                history.push('/ui/session/search');
              }
            }))
          }} disabled={pathname.endsWith('/search') || pathname.endsWith('/list')}>Список</ToolbarButtonBase>
          <ToolbarButtonFind onClick={() => {
            dispatch(setCurrentRecord(undefined, () => history.push('/ui/session/search')));
          }} />
          <ToolbarButtonBase onClick={() => { formRef.current?.dispatchEvent(new Event("submit")) }} disabled={!pathname.endsWith('/search')}>Найти</ToolbarButtonBase>
        </Toolbar>
      </Panel.Header>
      <Panel.Content>
        <Switch>
          <Route path={`${path}/:sessionId/view`}>
            <SessionViewPage />
          </Route>
          <Route path={`${path}/search`}>
            <SessionSearchPage ref={formRef} />
          </Route>
          <Route path={`${path}/list`}>
            <SessionListPage />
          </Route>
        </Switch>
      </Panel.Content>
    </Panel>
  );
}

export default SessionRoute;