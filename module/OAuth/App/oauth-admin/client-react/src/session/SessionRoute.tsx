import React, { useContext, useEffect, useRef, useState } from 'react';
import {
  Switch,
  Route,
  useRouteMatch,
  useHistory,
  useLocation
} from "react-router-dom";
import SessionViewPage from './pages/SessionViewPage';
import { AppState } from '../app/store/reducer';
import { useSelector, useDispatch } from 'react-redux';
import { LoadingPanel } from '../app/common/components/mask';
import { Session, SessionSearchTemplate } from './types';
import SessionSearchPage from './pages/SessionSearchPage';
import SessionListPage from './pages/SessionListPage';
import { actions as crudActions } from './state/sessionCrudSlice';
import { actions as searchActions } from './state/sessionSearchSlice';
import { HistoryState } from '../app/common/components/HistoryState';
import {
  Panel,
  TabPanel, Tab, Toolbar,
  ToolbarButtonDelete,
  ToolbarButtonFind,
  ToolbarButtonView,
  ToolbarSplitter,
  ToolbarButtonBase
} from '@jfront/ui-core';
import { UserPanel } from '@jfront/oauth-ui';
import { UserContext } from '@jfront/oauth-user'
import { Forbidden } from '@jfront/oauth-ui'
import { Loader } from '@jfront/oauth-ui';
import { useTranslation } from 'react-i18next';
import { DeleteAllDialog } from './delete-all-dialog/DeleteAllDialog';
import { EntityState, SearchState } from '@jfront/core-redux-saga';

const SessionRoute: React.FC = () => {

  const { isRoleLoading, isUserInRole, currentUser } = useContext(UserContext);
  const [hasViewRole, setViewRole] = useState<boolean | null>(null);
  const [hasDeleteRole, setDeleteRole] = useState<boolean>(false);
  const [showDeleteAll, setDeleteAll] = useState<boolean>(false);
  const { path } = useRouteMatch();
  const { pathname } = useLocation();
  const history = useHistory<HistoryState>();
  const dispatch = useDispatch();
  const { t } = useTranslation();
  const { currentRecord, selectedRecords, isLoading } = useSelector<AppState, EntityState<Session>>(state => state.session.crudSlice);
  const {
    searchTemplate,
    searchId
  } = useSelector<AppState, SearchState<SessionSearchTemplate, Session>>(state => state.session.searchSlice);
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
        {isLoading && <LoadingPanel text={t("dataLoadingMessage")} />}
        <Panel.Header>
          <TabPanel>
            <Tab selected>{t('session.moduleName')}</Tab>
            <UserPanel />
          </TabPanel>
          <Toolbar style={{ margin: 0 }}>
            <ToolbarButtonView
              onClick={() => { history.push(`/ui/session/${currentRecord?.sessionId}/view`) }}
              disabled={!currentRecord || pathname.endsWith('view')} />
            <ToolbarButtonDelete onClick={() => {
              if (window.confirm(t('delete'))) {
                dispatch(crudActions.remove({
                  sessionIds: selectedRecords.map(selectedRecord => String(selectedRecord.sessionId)),
                  loadingMessage: t('deleteMessage'),
                  callback: () => {
                    if (pathname.endsWith('/list') && searchId) {
                      dispatch(searchActions.search({
                        searchId,
                        pageSize: 25,
                        page: 1
                      }));
                    } else {
                      history.push('/ui/session/list');
                    }
                  }
                }))
              }
            }} disabled={selectedRecords.length === 0 || !hasDeleteRole} />
            <ToolbarButtonBase onClick={() => setDeleteAll(true)} disabled={!hasDeleteRole}>
              <img src={process.env.PUBLIC_URL + '/images/deleteAll.png'} alt="" />
            </ToolbarButtonBase>
            <ToolbarSplitter />
            <ToolbarButtonBase onClick={() => {
              dispatch(crudActions.setCurrentRecord({
                currentRecord: undefined as any,
                callback: () => {
                  if (searchTemplate) {
                    history.push('/ui/session/list');
                  } else {
                    history.push('/ui/session/search');
                  }
                }
              }))
            }} disabled={pathname.endsWith('/search') || pathname.endsWith('/list')}>{t('toolbar.list')}</ToolbarButtonBase>
            <ToolbarButtonFind onClick={() => {
              dispatch(crudActions.setCurrentRecord({
                currentRecord: undefined as any,
                callback: () => history.push('/ui/session/search')
              }))
            }} />
            <ToolbarButtonBase
              onClick={() => { formRef.current?.dispatchEvent(new Event("submit")) }}
              disabled={!pathname.endsWith('/search')}>{t('toolbar.find')}</ToolbarButtonBase>
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
        {showDeleteAll && <DeleteAllDialog onCancel={() => setDeleteAll(false)} />}
      </Panel>}
    </>
  );
}

export default SessionRoute;