import { Workstates, createEvent, useWorkstate } from "@jfront/core-common";
import { EntityState } from "@jfront/core-redux-saga";
import { UserContext } from "@jfront/oauth-user";
import { Toolbar, ToolbarButtonCreate, ToolbarButtonSave, ToolbarButtonView, ToolbarButtonDelete, ToolbarSplitter, ToolbarButtonBase } from "@jfront/ui-core";
import React, { useContext, useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useLocation, useParams, useHistory } from "react-router-dom";
import { HistoryState } from "../../../app/common/components/HistoryState";
import { actions as searchActions } from '../state/clientUriSearchSlice';
import { actions as crudActions } from '../state/clientUriCrudSlice';
import { AppState } from "../../../app/store/reducer";
import { ClientUri } from "../types";
import { useTranslation } from "react-i18next";

export interface ClientUriToolbarProps {
  formRef: React.RefObject<HTMLFormElement>
}

export const ClientUriToolbar = ({ formRef }: ClientUriToolbarProps) => {
  const { pathname, state } = useLocation<HistoryState>();
  const { clientId } = useParams<any>();
  const history = useHistory();
  const workstate = useWorkstate(pathname);
  const dispatch = useDispatch();
  const { t } = useTranslation();
  const { isUserInRole } = useContext(UserContext);
  const [hasCreateRole, setHasCreateRole] = useState(false);
  const [hasEditRole, setHasEditRole] = useState(false);
  const { currentRecord, selectedRecords } = useSelector<AppState, EntityState<ClientUri>>(state => state.clientUri.crudSlice);

  useEffect(() => {
    isUserInRole("OACreateClient")
      .then(setHasCreateRole);
    isUserInRole("OAEditClient")
      .then(setHasEditRole);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  return (
    <Toolbar style={{ margin: 0 }}>
      {(hasCreateRole || hasEditRole) && (
        <>
          <ToolbarButtonCreate onClick={() => {
            dispatch(crudActions.setCurrentRecord({
              currentRecord: undefined,
              callback: () => {
                history.push(`/client/${clientId}/client-uri/create`, state)
              }
            }));
          }} disabled={workstate === Workstates.Create} />
          <ToolbarButtonSave
            onClick={() => { formRef.current?.dispatchEvent(createEvent("submit")) }}
            disabled={workstate !== Workstates.Create} />
        </>)}
      <ToolbarButtonView
        onClick={() => { history.push(`/client/${clientId}/client-uri/${currentRecord?.clientUriId}/detail`, state) }}
        disabled={!currentRecord || workstate === Workstates.Detail} />
      {(hasCreateRole || hasEditRole) && <ToolbarButtonDelete onClick={() => {
        if (window.confirm(t('delete'))) {
          dispatch(crudActions.delete({
            primaryKeys: selectedRecords.map(selectedRecord => ({ clientId, clientUriId: selectedRecord.clientUriId })),
            onSuccess: () => {
              if (workstate === Workstates.List) {
                dispatch(searchActions.search({ clientId }));
              } else {
                history.push(`/client/${clientId}/client-uri/list`, state);
              }
            }
          }));
        }
      }} disabled={currentRecord === undefined} />}
      <ToolbarSplitter />
      <ToolbarButtonBase onClick={() => {
        dispatch(crudActions.setCurrentRecord({
          currentRecord: undefined,
          callback: () => {
            history.push(`/client/${clientId}/client-uri/list`, state)
          }
        }));
      }} disabled={workstate === Workstates.List}>{t('toolbar.list')}</ToolbarButtonBase>
    </Toolbar>
  )
}