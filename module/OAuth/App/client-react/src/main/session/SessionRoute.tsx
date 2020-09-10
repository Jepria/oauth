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
import { TabPanel, SelectedTab } from '../../components/tabpanel/TabPanel';
import { ToolBar } from '../../components/toolbar';
import * as DefaultButtons from '../../components/toolbar/ToolBarButtons';
import { setCurrentRecord, deleteSession, searchSessions } from './state/redux/actions';
import { HistoryState } from '../../components/HistoryState';
import { Page, Header, Content } from '@jfront/ui-core';
import { UserPanel } from '../../components/tabpanel/UserPanel';

const SessionRoute: React.FC = () => {

  const { path } = useRouteMatch();
  const { pathname } = useLocation();
  const history = useHistory<HistoryState>();
  const dispatch = useDispatch();
  const { isLoading, message, error, current, searchId, searchRequest } = useSelector<AppState, SessionState>(state => state.session)
  let formRef = useRef(null) as any;
  
  return (
    <Page>
      {isLoading && <LoadingPanel text={message} />}
      <Header>
        <TabPanel>
          <SelectedTab>Сессия</SelectedTab>
          <UserPanel/>
        </TabPanel>
        <ToolBar>
          <DefaultButtons.ViewButton onView={() => { history.push(`/ui/session/${current?.sessionId}/view`) }} disabled={!current || pathname.endsWith('view')} />
          <DefaultButtons.DeleteButton onDelete={() => {
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
          <DefaultButtons.Splitter />
          <DefaultButtons.ListButton onList={() => {
            dispatch(setCurrentRecord(undefined, () => {
              if (searchRequest) {
                history.push('/ui/session/list');
              } else {
                history.push('/ui/session/search');
              }
            }))
          }} disabled={pathname.endsWith('/search') || pathname.endsWith('/list')} />
          <DefaultButtons.SearchButton onSearch={() => {
            dispatch(setCurrentRecord(undefined, () => history.push('/ui/session/search')));
          }} />
          <DefaultButtons.DoSearchButton onDoSearch={() => { formRef.current?.handleSubmit() }} disabled={!pathname.endsWith('/search')} />
        </ToolBar>
      </Header>
      <Content>
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
      </Content>
    </Page>
  );
}

export default SessionRoute;