import React, { useContext, useEffect, useRef, useState } from 'react';
import {
  Switch,
  Route,
  useRouteMatch,
  useHistory,
  useLocation
} from "react-router-dom";
import SessionViewPage from './pages/SessionViewPage';
import { AppState } from '../../redux/store';
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
import { UserPanel } from '../../user/UserPanel';
import { UserContext } from '../../user/UserContext';
import { Loader } from '@jfront/oauth-ui';
import { Forbidden } from '../../user/Forbidden';
import { useTranslation } from 'react-i18next';

const SessionRoute: React.FC = () => {

  const { isRoleLoading, isUserInRole, currentUser } = useContext(UserContext);
  const [hasViewRole, setViewRole] = useState<boolean | null>(null);
  const [hasDeleteRole, setDeleteRole] = useState<boolean>(false);
  const { path } = useRouteMatch();
  const { pathname } = useLocation();
  const history = useHistory<HistoryState>();
  const dispatch = useDispatch();
  const { t } = useTranslation();
  const { isLoading, message, error, current, searchId, searchRequest } = useSelector<AppState, SessionState>(state => state.session)
  let formRef = useRef(null) as any;

  useEffect(() => {
    if (currentUser.username !== "Guest") {
      isUserInRole("OAViewSession")
        .then(setViewRole);
      isUserInRole("OADeleteSession")
        .then(setDeleteRole);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [currentUser])

  return (
    <>
      {isRoleLoading && <Loader title="OAuth" text="Проверка ролей" />}
      {hasViewRole === false && <Forbidden />}
      {hasViewRole === true && <Panel>
        {isLoading && <LoadingPanel text={message} />}
        <Panel.Header>
          <TabPanel>
            <Tab selected>{t('session.moduleName')}</Tab>
            <UserPanel />
          </TabPanel>
          <Toolbar style={{ margin: 0 }}>
            <ToolbarButtonView onClick={() => { history.push(`/ui/session/${current?.sessionId}/view`) }} disabled={!current || pathname.endsWith('view')} />
            <ToolbarButtonDelete onClick={() => {
              if (current?.sessionId) {
                if (window.confirm(t('delete'))) {
                  dispatch(deleteSession(`${current.sessionId}`, () => {
                    if (pathname.endsWith('/list') && searchId) {
                      dispatch(searchSessions(searchId, 25, 1));
                    } else {
                      history.push('/ui/session/list');
                    }
                  }));
                }
              }
            }} disabled={!current || !hasDeleteRole} />
            <ToolbarSplitter />
            <ToolbarButtonBase onClick={() => {
              dispatch(setCurrentRecord(undefined, () => {
                if (searchRequest) {
                  history.push('/ui/session/list');
                } else {
                  history.push('/ui/session/search');
                }
              }))
            }} disabled={pathname.endsWith('/search') || pathname.endsWith('/list')}>{'toolbar.list'}</ToolbarButtonBase>
            <ToolbarButtonFind onClick={() => {
              dispatch(setCurrentRecord(undefined, () => history.push('/ui/session/search')));
            }} />
            <ToolbarButtonBase onClick={() => { formRef.current?.dispatchEvent(new Event("submit")) }} disabled={!pathname.endsWith('/search')}>{t('toolbar.find')}</ToolbarButtonBase>
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
      </Panel>}
    </>
  );
}

export default SessionRoute;