import React, { HTMLAttributes, useEffect } from 'react';
import { useHistory } from 'react-router-dom';
import { useFormik } from 'formik';
import { useSelector, useDispatch } from 'react-redux';
import { Operator, Session, SessionSearchTemplate } from '../types';
import { actions as searchActions } from '../state/sessionSearchSlice';
import { actions as operatorActions } from '../state/sessionOperatorSlice';
import { actions as clientActions } from '../state/sessionClientSlice';
import { AppState } from '../../app/store/reducer';
import { Form, ComboBox, NumberInput, ComboBoxItem } from '@jfront/ui-core';
import { useTranslation } from 'react-i18next';
import queryString from 'query-string';
import { OptionState, SearchState } from '@jfront/core-redux-saga';
import { Client } from '../../client/types';

const SessionSearchPage = React.forwardRef<any, HTMLAttributes<HTMLFormElement>>((props, ref) => {
  const dispatch = useDispatch();
  const history = useHistory();
  const { t } = useTranslation();
  const clients = useSelector<AppState, OptionState<Client>>(state => state.session.clientSlice);
  const operators = useSelector<AppState, OptionState<Operator>>(state => state.session.operatorSlice);
  const { searchRequest } = useSelector<AppState, SearchState<SessionSearchTemplate, Session>>(state => state.session.searchSlice);

  useEffect(() => {
    if (clients.options.length === 0) {
      dispatch(clientActions.getOptionsStart({ params: "" }));
    }
    if (operators.options.length === 0) {
      dispatch(operatorActions.getOptionsStart({ params: "" }));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const formik = useFormik<SessionSearchTemplate>({
    initialValues: { maxRowCount: 25, ...searchRequest?.template },
    validate: (values) => {
      const errors: { operatorId?: string, maxRowCount?: string } = {};
      if (!values['maxRowCount']) {
        errors.maxRowCount = t('validation.notEmpty')
      } else if (!/[0-9]/.test(`${values['maxRowCount']}`)) {
        errors.maxRowCount = t('validation.onlyDigits')
      }
      if (!values['operatorId']) {
        errors.operatorId = t('validation.notEmpty')
      }
      return errors;
    },
    onSubmit: (values: SessionSearchTemplate) => {
      dispatch(searchActions.setSearchTemplate({
        searchTemplate: {
          template: values
        },
        callback: () => {
          const query = queryString.stringify(values)
          history.push({
            pathname: `/session/list`,
            search: `?${query ? query : ""}`
          })
        }
      }));
    }
  })

  return (
    <Form onSubmit={formik.handleSubmit} ref={ref}>
      <Form.Field>
        <Form.Label required>{t('session.operator.legend')}:</Form.Label>
        <Form.Control style={{
          maxWidth: "300px"
        }}>
          <ComboBox
            name="operatorId"
            isLoading={operators.isLoading}
            placeholder={t('session.operator.placeholder')}
            value={formik.values.operatorId}
            error={formik.errors.operatorId}
            onInputChange={(e) => dispatch(operatorActions.getOptionsStart({ params: e.target.value }))}
            onSelectionChange={(name, value) => formik.setFieldValue(name, value)} style={{ maxWidth: '400px' }}>
            {operators.options?.map(operator => <ComboBoxItem key={operator.value} label={operator.name} value={operator.value} />)}
          </ComboBox>
        </Form.Control>
      </Form.Field>
      <Form.Field>
        <Form.Label>{t('session.client.legend')}:</Form.Label>
        <Form.Control style={{
          maxWidth: "300px"
        }}>
          <ComboBox
            options={clients.options}
            name="clientId"
            placeholder={t('session.client.placeholder')}
            isLoading={clients.isLoading}
            value={formik.values.clientId}
            error={formik.errors.clientId}
            onInputChange={(e) => dispatch(clientActions.getOptionsStart({ params: e.target.value }))}
            getOptionName={(option: { clientName: any; }) => {
              return option.clientName;
            }}
            getOptionValue={(option: { clientId: any; }) => option.clientId}
            onSelectionChange={(name, value) => formik.setFieldValue(name, value)} style={{ maxWidth: '250px' }}/>
        </Form.Control>
      </Form.Field>
      <Form.Field>
        <Form.Label required>{t('maxRowCount')}:</Form.Label>
        <Form.Control style={{ maxWidth: "60px" }}>
          <NumberInput
            style={{ minWidth: "55px" }}
            name="maxRowCount"
            value={formik.values.maxRowCount}
            onChange={formik.handleChange}
            error={formik.errors.maxRowCount} />
        </Form.Control>
      </Form.Field>
    </Form>
  )
})

export default SessionSearchPage;