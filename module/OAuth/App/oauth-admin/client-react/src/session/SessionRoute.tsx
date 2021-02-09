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
  ToolbarButtonBase,
  Loader
} from '@jfront/ui-core';
import { UserPanel, Loader as OAuthLoader, Forbidden } from '@jfront/oauth-ui';
import { UserContext } from '@jfront/oauth-user'
import { useTranslation } from 'react-i18next';
import { DeleteAllDialog } from './delete-all-dialog/DeleteAllDialog';
import { EntityState, SearchState } from '@jfront/core-redux-saga';
import { createEvent, useWorkstate, Workstates } from '@jfront/core-common';

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
  const workstate = useWorkstate(pathname);
  const { currentRecord, selectedRecords, isLoading } = useSelector<AppState, EntityState<Session>>(state => state.session.crudSlice);
  const { pageNumber, pageSize, searchRequest, searchId } = useSelector<AppState, SearchState<SessionSearchTemplate, Session>>(state => state.session.searchSlice);
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
      {isRoleLoading && <OAuthLoader title="OAuth" text="Проверка ролей" />}
      {hasViewRole === false && <Forbidden />}
      {hasViewRole === true && <Panel>
        {isLoading && <Loader text={t("dataLoadingMessage")} />}
        <Panel.Header>
          <TabPanel>
            <Tab selected>{t('session.moduleName')}</Tab>
            <UserPanel />
          </TabPanel>
          <Toolbar style={{ margin: 0 }}>
            <ToolbarButtonView
              onClick={() => { history.push(`/ui/session/${currentRecord?.sessionId}/detail`) }}
              disabled={!currentRecord || workstate === Workstates.Detail} />
            {hasDeleteRole && (
              <>
                <ToolbarButtonDelete onClick={() => {
                  if (window.confirm(t('delete'))) {
                    dispatch(crudActions.delete({
                      primaryKeys: selectedRecords.map(selectedRecord => selectedRecord.sessionId),
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
                          history.push('/ui/session/list');
                        }
                      }
                    }))
                  }
                }} disabled={selectedRecords.length === 0 || !hasDeleteRole} />
                <ToolbarButtonBase onClick={() => setDeleteAll(true)} disabled={!hasDeleteRole}>
                  <img src={process.env.PUBLIC_URL + '/images/deleteAll.png'} alt="" />
                </ToolbarButtonBase>
              </>)}
            <ToolbarSplitter />
            <ToolbarButtonBase onClick={() => {
              dispatch(crudActions.setCurrentRecord({
                currentRecord: undefined as any,
                callback: () => {
                  if (searchRequest) {
                    history.push('/ui/session/list');
                  } else {
                    history.push('/ui/session');
                  }
                }
              }))
            }} disabled={workstate === Workstates.Search || workstate === Workstates.List}>{t('toolbar.list')}</ToolbarButtonBase>
            <ToolbarButtonFind onClick={() => {
              dispatch(crudActions.setCurrentRecord({
                currentRecord: undefined as any,
                callback: () => history.push('/ui/session')
              }))
            }} />
            <ToolbarButtonBase
              onClick={() => { formRef.current?.dispatchEvent(createEvent("submit")) }}
              disabled={workstate !== Workstates.Search}>{t('toolbar.find')}</ToolbarButtonBase>
          </Toolbar>
        </Panel.Header>
        <Panel.Content>
          <Switch>
            <Route path={`${path}/:sessionId/detail`}>
              <SessionViewPage />
            </Route>
            <Route path={`${path}`} exact>
              <SessionSearchPage ref={formRef} />
            </Route>
            <Route path={`${path}/list`}>
              <SessionListPage />
            </Route>
          </Switch>
        </Panel.Content>
        <DeleteAllDialog onCancel={() => setDeleteAll(false)} visible={showDeleteAll} />
      </Panel>}
    </>
  );
}

export default SessionRoute;