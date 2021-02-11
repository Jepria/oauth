import { UserContext } from "@jfront/oauth-user";
import { Toolbar, ToolbarButtonBase } from "@jfront/ui-core"
import React, { useContext, useEffect, useState } from "react"
import { useTranslation } from "react-i18next";
import { useDispatch } from "react-redux";
import { actions } from '../state/keySlice';

export const KeyToolbar = () => {
  const dispatch = useDispatch();
  const { t } = useTranslation();
  const { isUserInRole } = useContext(UserContext);
  const [hasUpdateRole, setHasUpdateRole] = useState(false);

  useEffect(() => {
    isUserInRole("OAUpdateKey")
      .then(setHasUpdateRole);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  return (
    <Toolbar>
      <ToolbarButtonBase onClick={() => {
        if (window.confirm(t('key.toolbar.updateMessage'))) {
          dispatch(actions.update({
            loadingMessage: t('dataLoadingMessage'),
            callback: () => dispatch(actions.getRecordById({ loadingMessage: t('dataLoadingMessage') }))
          }))
        }
      }} title={t('key.toolbar.update')} disabled={!hasUpdateRole}>
        <img src={process.env.PUBLIC_URL + '/images/change_password.png'} alt={t('key.toolbar.update')} title={t('key.toolbar.update')} />
      </ToolbarButtonBase>
    </Toolbar>
  )
}