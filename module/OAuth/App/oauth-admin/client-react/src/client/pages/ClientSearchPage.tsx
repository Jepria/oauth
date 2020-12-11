import React, { HTMLAttributes } from 'react';
import { useHistory } from 'react-router-dom';
import { useFormik } from 'formik';
import { useSelector, useDispatch } from 'react-redux';
import { ClientSearchTemplate } from '../types';
import { postSearchClientRequest } from '../state/actions';
import { AppState } from '../../app/store';
import { Form, TextInput, NumberInput } from '@jfront/ui-core';
import { useTranslation } from 'react-i18next';
import queryString from 'query-string';

const ClientSearchPage = React.forwardRef<any, HTMLAttributes<HTMLFormElement>>((props, ref) => {
  const dispatch = useDispatch();
  const history = useHistory();
  const { t } = useTranslation();
  const searchTemplate = useSelector<AppState, ClientSearchTemplate | undefined>(state => state.client.searchRequest?.template);

  const formik = useFormik<ClientSearchTemplate>({
    initialValues: searchTemplate ? searchTemplate : { maxRowCount: 25 },
    validate: (values) => {
      const errors: { maxRowCount?: string } = {};
      if (!values['maxRowCount']) {
        errors.maxRowCount = t('validation.notEmpty')
      } else if (!/[0-9]/.test(`${values['maxRowCount']}`)) {
        errors.maxRowCount = t('validation.onlyDigits')
      }
      return errors;
    },
    onSubmit: (values: ClientSearchTemplate) => {
      dispatch(postSearchClientRequest({
        template: values
      }, t("dataLoadingMessage"), () => {
        const query = queryString.stringify(values)
        history.push(`/ui/client/list?pageSize=25&page=1${query ? "&" + query : ""}`)
      }));
    }
  })

  return (
    <Form onSubmit={formik.handleSubmit} ref={ref}>
      <Form.Field>
        <Form.Label>{t('client.clientId')}:</Form.Label>
        <Form.Control style={{ maxWidth: "200px" }}>
          <TextInput
            name="clientId"
            value={formik.initialValues.clientId}
            onChange={formik.handleChange}
            error={formik.errors.clientId} />
        </Form.Control>
      </Form.Field>
      <Form.Field>
        <Form.Label>{t('client.clientName')}:</Form.Label>
        <Form.Control style={{ maxWidth: "200px" }}>
          <TextInput
            name="clientName"
            value={formik.values.clientName}
            onChange={formik.handleChange}
            error={formik.errors.clientName} />
        </Form.Control>
      </Form.Field>
      <Form.Field>
        <Form.Label>{t('client.clientNameEn')}:</Form.Label>
        <Form.Control style={{ maxWidth: "200px" }}>
          <TextInput
            name="clientNameEn"
            value={formik.values.clientNameEn}
            onChange={formik.handleChange}
            error={formik.errors.clientId} />
        </Form.Control>
      </Form.Field>
      <Form.Field>
        <Form.Label required>{t('maxRowCount')}:</Form.Label>
        <Form.Control
          style={{ maxWidth: "60px" }}
          error={formik.errors.maxRowCount}>
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

export default ClientSearchPage;