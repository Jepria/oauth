import { Workstates, createEvent, useWorkstate } from "@jfront/core-common";
import { EntityState, SessionSearchState } from "@jfront/core-redux-saga";
import { UserContext } from "@jfront/oauth-user";
import { Toolbar, ToolbarButtonView, ToolbarButtonDelete, ToolbarButtonBase, ToolbarSplitter, ToolbarButtonFind } from "@jfront/ui-core";
import React, { useContext, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useDispatch, useSelector } from "react-redux";
import { useLocation, useHistory } from "react-router-dom";
import { HistoryState } from "../../app/common/components/HistoryState";
import { AppState } from "../../app/store/reducer";
import { Session, SessionSearchTemplate } from "../types";
import { actions as crudActions } from '../state/sessionCrudSlice';
import { actions as searchActions } from '../state/sessionSearchSlice';

export interface SessionToolbarProps {
 formRef: React.RefObject<HTMLFormElement>
 openDeleteAllDialog: () => void
}

export const SessionToolbar = ({ formRef, openDeleteAllDialog }: SessionToolbarProps) => {
  const { isUserInRole, currentUser } = useContext(UserContext);
  const [hasDeleteRole, setDeleteRole] = useState<boolean>(false);
  const { pathname } = useLocation();
  const history = useHistory<HistoryState>();
  const dispatch = useDispatch();
  const workstate = useWorkstate(pathname);
  const { t } = useTranslation();
  const { currentRecord, selectedRecords } = useSelector<AppState, EntityState<Session>>(state => state.session.crudSlice);
  const { pageNumber, pageSize, searchRequest, searchId } = useSelector<AppState, SessionSearchState<SessionSearchTemplate, Session>>(state => state.session.searchSlice);

  useEffect(() => {
    if (currentUser.username !== "Guest") {
      isUserInRole("OADeleteSession")
        .then(setDeleteRole);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [currentUser])

  return (
    <Toolbar style={{ margin: 0 }}>
      <ToolbarButtonView
        onClick={() => { history.push(`/session/${currentRecord?.sessionId}/detail`) }}
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
                      dispatch(searchActions.getResultSet({
                        searchId,
                        pageSize,
                        pageNumber
                      }));
                    } else if (searchRequest) {
                      dispatch(searchActions.postSearchRequest({ searchTemplate: searchRequest }))
                    }
                  } else {
                    history.push('/session/list');
                  }
                }
              }))
            }
          }} disabled={selectedRecords.length === 0 || !hasDeleteRole} />
          <ToolbarButtonBase title="Удалить все сессии пользователя" onClick={() => openDeleteAllDialog()} disabled={!hasDeleteRole}>
            <img src={process.env.PUBLIC_URL + '/images/deleteAll.png'} alt="" />
          </ToolbarButtonBase>
        </>)}
      <ToolbarSplitter />
      <ToolbarButtonBase onClick={() => {
        dispatch(crudActions.setCurrentRecord({
          currentRecord: undefined as any,
          callback: () => {
            if (searchRequest) {
              history.push('/session/list');
            } else {
              history.push('/session');
            }
          }
        }))
      }} disabled={workstate === Workstates.Search || workstate === Workstates.List}>{t('toolbar.list')}</ToolbarButtonBase>
      <ToolbarButtonFind onClick={() => {
        dispatch(crudActions.setCurrentRecord({
          currentRecord: undefined as any,
          callback: () => history.push('/session')
        }))
      }} />
      <ToolbarButtonBase
        onClick={() => { 
          formRef.current?.dispatchEvent(createEvent("submit")) 
        }}
        disabled={workstate !== Workstates.Search}>{t('toolbar.find')}</ToolbarButtonBase>
    </Toolbar>
  )
}