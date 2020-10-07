import React, { useEffect } from 'react';
import { useFormik } from 'formik'
import { Dialog } from '../../../components/dialog/Dialog';
import { ComboBox, ComboBoxItem, Form } from '@jfront/ui-core';
import { useDispatch, useSelector } from 'react-redux';
import { AppState } from '../../../redux/store';
import { deleteAll, getOperators, searchSessions } from '../state/redux/actions';
import { SessionState } from '../types';
import { useTranslation } from 'react-i18next';
import styled from 'styled-components';
import { useHistory, useLocation } from 'react-router-dom';
import { HistoryState } from '../../../components/HistoryState';


const Button = styled.button`
  width: 30%;
  background: linear-gradient(rgb(255, 255, 255), rgb(208, 222, 240));
  margin: 5px;
  padding: 3px 5px;
  border: 1px outset #ccc;
`;

const Controls = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
`;

interface DeleteAllForm {
  operatorId?: number
}

export interface DeleteAllDialogProps {
  onCancel: () => void
}

export const DeleteAllDialog = ({ onCancel }: DeleteAllDialogProps) => {

  const dispatch = useDispatch();
  const { searchId, current, operators, operatorsLoading } = useSelector<AppState, SessionState>(state => state.session);
  const { t } = useTranslation();
  const history = useHistory<HistoryState>();
  const { pathname } = useLocation();

  const formik = useFormik<DeleteAllForm>({
    initialValues: {  },
    onSubmit: (values: DeleteAllForm) => {      
      if (values.operatorId) {
        if (window.confirm(t('session.deleteAllMessage'))) {
          dispatch(deleteAll(values.operatorId, t("deleteMessage"), () => {
            onCancel();
            if (pathname.endsWith('/list') && searchId) {
              dispatch(searchSessions(searchId, 25, 1, t('dataLoadingMessage')));
            } else if (pathname.endsWith('/view') && values.operatorId === current?.operator?.value) {
              history.push('/ui/session/list');
            } else if (pathname.endsWith('/search')) {
              dispatch(getOperators(""));
            }
          }));
        }
      }
    },
    validate: (values) => {
      const errors: { operatorId?: string} = {};
      if (!values['operatorId']) {
        errors.operatorId = t('validation.notEmpty')
      }
      return errors;
    }
  })

  useEffect(() => {
    dispatch(getOperators(""));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <Dialog header={t('session.deleteAllDialogHeader')}>
      <Form onSubmit={formik.handleSubmit}>
        <Form.Field>
          <Form.Label style={{minWidth: 'unset', width: 'unset'}}>{t('session.operator.legend')}</Form.Label>
          <Form.Control>
            <ComboBox
              name="operatorId"
              isLoading={operatorsLoading}
              value={formik.values.operatorId}
              error={formik.errors.operatorId}
              onInputChange={(e: { target: { value: string | undefined; }; }) => dispatch(getOperators(e.target.value))}
              onSelectionChange={formik.setFieldValue} style={{ maxWidth: '250px' }}>
              {operators?.map(operator => <ComboBoxItem key={operator.value} label={operator.name} value={operator.value} />)}
            </ComboBox>
          </Form.Control>
        </Form.Field>
        <Controls>
          <Button type="submit">OK</Button>
          <Button type="button" onClick={() => onCancel()}>{t('session.deleteAllDialogCancel')}</Button>
        </Controls>
      </Form>
    </Dialog>
  )
}