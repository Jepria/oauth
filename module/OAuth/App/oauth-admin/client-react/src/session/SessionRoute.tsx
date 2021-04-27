import React, { useContext, useEffect, useRef, useState } from 'react';
import {
  Switch,
  Route,
  useRouteMatch
} from "react-router-dom";
import SessionViewPage from './pages/SessionViewPage';
import { AppState } from '../app/store/reducer';
import { useSelector } from 'react-redux';
import { Session } from './types';
import SessionSearchPage from './pages/SessionSearchPage';
import SessionListPage from './pages/SessionListPage';
import {
  Panel,
  TabPanel,
  Tab, 
  Loader
} from '@jfront/ui-core';
import { UserPanel, Loader as OAuthLoader, Forbidden } from '@jfront/oauth-ui';
import { UserContext } from '@jfront/oauth-user'
import { useTranslation } from 'react-i18next';
import { DeleteAllDialog } from './components/DeleteAllDialog';
import { EntityState } from '@jfront/core-redux-saga';
import { SessionToolbar } from './components/SessionToolbar';
import SessionErrorBoundary from './components/SessionErrorBoundary';

const SessionRoute: React.FC = () => {

  const { isRoleLoading, isUserInRole, currentUser } = useContext(UserContext);
  const [hasViewRole, setViewRole] = useState<boolean | null>(null);
  const [showDeleteAll, setDeleteAll] = useState<boolean>(false);
  const { path } = useRouteMatch();
  const { t } = useTranslation();
  const { isLoading } = useSelector<AppState, EntityState<Session>>(state => state.session.crudSlice);
  let formRef = useRef(null) as any;

  useEffect(() => {
    if (currentUser.username !== "Guest") {
      isUserInRole("OAViewSession")
        .then(setViewRole);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [currentUser])

  return (
    <SessionErrorBoundary>
      {isRoleLoading && <OAuthLoader title="OAuth" text="Проверка ролей" />}
      {hasViewRole === false && <Forbidden />}
      {hasViewRole === true && <Panel>
        {isLoading && <Loader text={t("dataLoadingMessage")} />}
        <Panel.Header>
          <TabPanel>
            <Tab selected>{t('session.moduleName')}</Tab>
            <UserPanel />
          </TabPanel>
          <SessionToolbar formRef={formRef} openDeleteAllDialog={() => setDeleteAll(true)}/>
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
    </SessionErrorBoundary>
  );
}

export default SessionRoute;