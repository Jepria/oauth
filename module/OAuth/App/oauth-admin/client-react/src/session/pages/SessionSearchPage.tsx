import React, { HTMLAttributes, useEffect } from 'react';
import { useHistory } from 'react-router-dom';
import { useFormik } from 'formik';
import { useSelector, useDispatch } from 'react-redux';
import { SessionSearchTemplate, SessionState } from '../types';
import { postSearchSessionRequest, getClients, getOperators } from '../state/actions';
import { AppState } from '../../app/store';
import { Form, ComboBox, NumberInput, ComboBoxItem } from '@jfront/ui-core';
import { useTranslation } from 'react-i18next';
import queryString from 'query-string';

const SessionSearchPage = React.forwardRef<any, HTMLAttributes<HTMLFormElement>>((props, ref) => {
  const dispatch = useDispatch();
  const history = useHistory();
  const { t } = useTranslation();
  const { clients, operators, searchRequest, clientsLoading, operatorsLoading } = useSelector<AppState, SessionState>(state => state.session);

  useEffect(() => {
    dispatch(getOperators(""));
    dispatch(getClients(""));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const formik = useFormik<SessionSearchTemplate>({
    initialValues: { maxRowCount: 25, ...searchRequest?.template, operatorId: undefined, clientId: undefined },
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
      dispatch(postSearchSessionRequest({
        template: values
      }, t('dataLoadingMessage'), () => {
        const query = queryString.stringify(values)
        history.push(`/ui/session/list?pageSize=25&page=1${query ? "&" + query : ""}`)
      }));
    }
  })

  return (
    <Form onSubmit={formik.handleSubmit} ref={ref}>
      <Form.Field>
        <Form.Label required>{t('session.operator.legend')}:</Form.Label>
        <Form.Control>
          <ComboBox
            name="operatorId"
            isLoading={operatorsLoading}
            placeholder={t('session.operator.placeholder')}
            value={formik.values.operatorId}
            error={formik.errors.operatorId}
            onInputChange={(e: { target: { value: string | undefined; }; }) => dispatch(getOperators(e.target.value))}
            onSelectionChange={formik.setFieldValue} style={{ maxWidth: '250px' }}>
            {operators?.map(operator => <ComboBoxItem key={operator.value} label={operator.name} value={operator.value} />)}
          </ComboBox>
        </Form.Control>
      </Form.Field>
      <Form.Field>
        <Form.Label>{t('session.client.legend')}:</Form.Label>
        <Form.Control>
          <ComboBox
            options={clients ? clients : []}
            name="clientId"
            placeholder={t('session.client.placeholder')}
            isLoading={clientsLoading}
            value={formik.values.clientId}
            error={formik.errors.clientId}
            onInputChange={(e: { target: { value: string | undefined; }; }) => dispatch(getClients(e.target.value))}
            getOptionName={(option: { clientName: any; }) => {
              return option.clientName;
            }}
            getOptionValue={(option: { clientId: any; }) => option.clientId}
            onSelectionChange={formik.setFieldValue} style={{ maxWidth: '250px' }} />
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