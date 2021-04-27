import { Workstates, createEvent, useWorkstate } from "@jfront/core-common";
import { EntityState, SearchState } from "@jfront/core-redux-saga";
import { UserContext } from "@jfront/oauth-user";
import {
  Toolbar,
  ToolbarButtonCreate,
  ToolbarButtonSave,
  ToolbarButtonEdit,
  ToolbarButtonView,
  ToolbarButtonDelete,
  ToolbarSplitter,
  ToolbarButtonBase,
  ToolbarButtonFind
} from "@jfront/ui-core";
import React, { useContext, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useDispatch, useSelector } from "react-redux";
import { useLocation, useHistory } from "react-router-dom";
import { HistoryState } from "../../app/common/components/HistoryState";
import { AppState } from "../../app/store/reducer";
import { Client, ClientSearchTemplate } from "../types";
import { actions as searchActions } from '../state/clientSearchSlice';
import { actions as crudActions } from '../state/clientCrudSlice';

export interface ClientToolbarProps {
  formRef: React.RefObject<HTMLFormElement>
}

export const ClientToolbar = ({ formRef }: ClientToolbarProps) => {
  const { pathname } = useLocation();
  const workstate = useWorkstate(pathname);
  const history = useHistory<HistoryState>();
  const dispatch = useDispatch();
  const [hasCreateRole, setHasCreateRole] = useState(false);
  const [hasEditRole, setHasEditRole] = useState(false);
  const [hasDeleteRole, setHasDeleteRole] = useState(false);
  const { isUserInRole } = useContext(UserContext);
  const { t } = useTranslation();
  const { currentRecord, selectedRecords } = useSelector<AppState, EntityState<Client>>(state => state.client.crudSlice)
  const { searchRequest, pageSize, pageNumber } = useSelector<AppState, SearchState<ClientSearchTemplate, Client>>(state => state.client.searchSlice)

  useEffect(() => {
    isUserInRole("OACreateClient")
      .then(setHasCreateRole);
    isUserInRole("OAEditClient")
      .then(setHasEditRole);
    isUserInRole("OADeleteClient")
      .then(setHasDeleteRole);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  return (
    <Toolbar style={{ margin: 0 }}>
      {hasCreateRole && <ToolbarButtonCreate onClick={() => {
        dispatch(crudActions.setCurrentRecord({
          currentRecord: undefined as any, callback: () => {
            history.push('/client/create')
          }
        }));
      }} disabled={workstate === Workstates.Create} />}
      {(hasCreateRole || hasEditRole) && <ToolbarButtonSave
        onClick={() => { formRef.current?.dispatchEvent(createEvent("submit")) }}
        disabled={workstate !== Workstates.Create && workstate !== Workstates.Edit} />}
      {hasEditRole && <ToolbarButtonEdit
        onClick={() => history.push(`/client/${currentRecord?.clientId}/edit`)}
        disabled={!currentRecord || workstate === Workstates.Edit} />}
      <ToolbarButtonView
        onClick={() => { history.push(`/client/${currentRecord?.clientId}/detail`) }}
        disabled={!currentRecord || workstate === Workstates.Detail} />
      {hasDeleteRole && <ToolbarButtonDelete onClick={() => {
        if (window.confirm(t('delete'))) {
          dispatch(crudActions.delete({
            primaryKeys: selectedRecords.map(selectedRecord => selectedRecord.clientId),
            onSuccess: () => {
              if (workstate === Workstates.List) {
                  dispatch(searchActions.search({
                    searchTemplate: searchRequest,
                    pageSize,
                    pageNumber
                  }));
              } else {
                history.push('/client/list');
              }
            }
          }));
        }
      }} disabled={selectedRecords.length === 0} />}
      <ToolbarSplitter />
      <ToolbarButtonBase onClick={() => {
        dispatch(crudActions.setCurrentRecord({
          callback: () => {
            if (searchRequest) {
              history.push('/client/list');
            } else {
              history.push('/client');
            }
          }
        }))
      }} disabled={workstate === Workstates.Search || workstate === Workstates.List}>{t('toolbar.list')}</ToolbarButtonBase>
      <ToolbarButtonFind onClick={() => {
        dispatch(crudActions.setCurrentRecord({
          callback: () => {
            history.push('/client')
          }
        }));
      }} />
      <ToolbarButtonBase
        onClick={() => { formRef.current?.dispatchEvent(createEvent("submit")) }}
        disabled={workstate !== Workstates.Search}>
        {t('toolbar.find')}
      </ToolbarButtonBase>
    </Toolbar>
  )
}