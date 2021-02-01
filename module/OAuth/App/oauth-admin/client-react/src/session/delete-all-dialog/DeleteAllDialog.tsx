import React, { useEffect } from 'react';
import { useFormik } from 'formik';
import { FormDialog } from '@jfront/ui-core';
import { ComboBox, ComboBoxItem, Form } from '@jfront/ui-core';
import { useDispatch, useSelector } from 'react-redux';
import { AppState } from '../../app/store/reducer';
import { actions as searchActions } from '../state/sessionSearchSlice';
import { actions as crudActions } from '../state/sessionCrudSlice';
import { actions as operatorActions } from '../state/sessionOperatorSlice';
import { actions as clientActions } from '../state/sessionClientSlice';
import { OperatorOptionState, Session, SessionSearchTemplate } from '../types';
import { useTranslation } from 'react-i18next';
import { useHistory, useLocation } from 'react-router-dom';
import { HistoryState } from '../../app/common/components/HistoryState';
import { EntityState, SearchState } from '@jfront/core-redux-saga';

interface DeleteAllForm {
  operatorId?: number
}

export interface DeleteAllDialogProps {
  visible: boolean
  onCancel: () => void
}

export const DeleteAllDialog = ({ visible, onCancel }: DeleteAllDialogProps) => {

  const dispatch = useDispatch();
  const { currentRecord } = useSelector<AppState, EntityState<Session>>(state => state.session.crudSlice);
  const { searchId } = useSelector<AppState, SearchState<SessionSearchTemplate, Session>>(state => state.session.searchSlice);
  const operators = useSelector<AppState, OperatorOptionState>(state => state.session.operatorSlice);
  const { t } = useTranslation();
  const history = useHistory<HistoryState>();
  const { pathname } = useLocation();

  const formik = useFormik<DeleteAllForm>({
    initialValues: {},
    onSubmit: (values: DeleteAllForm) => {
      if (values.operatorId) {
        if (window.confirm(t('session.deleteAllMessage'))) {
          dispatch(crudActions.removeAll({
            operatorId: values.operatorId,
            loadingMessage: t("deleteMessage"),
            callback: () => {
              onCancel();
              if (pathname.endsWith('/list') && searchId) {
                dispatch(searchActions.search({
                  searchId,
                  pageSize: 25,
                  pageNumber: 1
                }));
              } else if (pathname.endsWith('/view') && values.operatorId === currentRecord?.operator?.value) {
                history.push('/ui/session/list');
              } else if (pathname.endsWith('/search')) {
                dispatch(operatorActions.getOptionsStart({ params: "" }));
                dispatch(clientActions.getOptionsStart({ params: "" }));
              }
            }
          }));
        }
      }
    },
    validate: (values) => {
      const errors: { operatorId?: string } = {};
      if (!values['operatorId']) {
        errors.operatorId = t('validation.notEmpty')
      }
      return errors;
    }
  })

  useEffect(() => {
    dispatch(operatorActions.getOptionsStart({ params: "" }));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <FormDialog
      onClose={onCancel}
      visible={visible}
      header={t('session.deleteAllDialogHeader')}
      handleSubmit={formik.handleSubmit}
      cancelButtonLabel={t('session.deleteAllDialogCancel')}
      withCloseButton>
      <Form.Field>
        <Form.Label style={{ minWidth: 'unset', width: 'unset' }}>{t('session.operator.legend')}</Form.Label>
        <Form.Control>
          <ComboBox
            name="operatorId"
            isLoading={operators.isLoading}
            value={formik.values.operatorId}
            error={formik.errors.operatorId}
            onInputChange={(e: { target: { value: string | undefined; }; }) => dispatch(operatorActions.getOptionsStart({ params: e.target.value }))}
            onSelectionChange={formik.setFieldValue} style={{ maxWidth: '250px' }}>
            {operators.options.map(operator => <ComboBoxItem key={operator.value} label={operator.name} value={operator.value} />)}
          </ComboBox>
        </Form.Control>
      </Form.Field>
    </FormDialog>
  )
}