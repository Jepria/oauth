import React, { useEffect, HTMLAttributes } from 'react';
import { useHistory, useParams } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { Client, Option } from '../types';
import { AppState } from '../../app/store/reducer';
import { actions } from '../state/clientCrudSlice';
import { actions as roleActions } from '../state/clientRoleSlice';
import { useFormik } from 'formik';
import { GrantType, ApplicationGrantType } from '@jfront/oauth-core';
import { Form, TextInput, CheckBoxGroup, CheckBox, SelectInput, DualList } from '@jfront/ui-core';
import { Text } from '../../app/common/components/form/Field';
import { useTranslation } from 'react-i18next';
import { EntityState, OptionState } from '@jfront/core-redux-saga';
import {isUri} from "valid-url";

const ClientEditPage = React.forwardRef<HTMLFormElement, HTMLAttributes<HTMLFormElement>>((props, ref) => {
  const dispatch = useDispatch();
  const history = useHistory();
  const { clientId } = useParams<any>();
  const { t } = useTranslation();
  const { currentRecord } = useSelector<AppState, EntityState<Client>>(state => state.client.crudSlice);
  const { options, isLoading } = useSelector<AppState, OptionState<Option>>(state => state.client.roleSlice);

  const applicationTypeOptions = [
    { name: "Native", value: "native" },
    { name: "WEB application", value: "web" },
    { name: "Browser (client-side) application", value: "browser" },
    { name: "Service", value: "service" },
  ]

  useEffect(() => {
    dispatch(roleActions.getOptionsStart({ params: ""}));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (!currentRecord && clientId) {
      dispatch(actions.getRecordById({ primaryKey: clientId }));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [currentRecord, clientId, dispatch]);

  const formik = useFormik<Client>({
    initialValues: { clientId: '', clientName: '', clientNameEn: '', ...currentRecord },
    enableReinitialize: true,
    onSubmit: (values: Client) => {
      if (clientId) {
        dispatch(actions.update({
          primaryKey: clientId,
          values,
          onSuccess: (client: Client) => {
            history.push(`/ui/client/${client.clientId}/detail`);
          }
        }));
      }
    },
    validate: (values) => {
      const errors: { clientName?: string, clientNameEn?: string, applicationType?: string, loginModuleUri?: string, grantTypes?: string } = {};
      if (!values['clientName']) {
        errors.clientName = t('validation.notEmpty')
      }
      if (!values['clientNameEn']) {
        errors.clientNameEn = t('validation.notEmpty')
      }
      if (values.loginModuleUri && !isUri(values.loginModuleUri)) {
        errors.loginModuleUri = t('validation.invalidUriFormat')
      }
      if (!values['applicationType']) {
        errors.applicationType = t('validation.notEmpty')
      }
      if (!values['grantTypes'] || values['grantTypes'].length === 0) {
        errors.grantTypes = t('validation.notEmpty')
      }
      return errors;
    }
  })

  return (
    <Form onSubmit={formik.handleSubmit} ref={ref}>
      <Form.Field>
        <Form.Label>{t('client.clientId')}:</Form.Label>
        <Text>{currentRecord?.clientId}</Text>
      </Form.Field>
      <Form.Field>
        <Form.Label required>{t('client.clientName')}:</Form.Label>
        <Form.Control style={{ maxWidth: "200px" }}>
          <TextInput
            name="clientName"
            value={formik.values.clientName}
            onChange={formik.handleChange}
            error={formik.errors.clientName} />
        </Form.Control>
      </Form.Field>
      <Form.Field required>
        <Form.Label>{t('client.clientNameEn')}:</Form.Label>
        <Form.Control style={{ maxWidth: "200px" }}>
          <TextInput
            name="clientNameEn"
            value={formik.values.clientNameEn}
            onChange={formik.handleChange}
            error={formik.errors.clientNameEn} />
        </Form.Control>
      </Form.Field>
      <Form.Field>
        <Form.Label>{t('client.loginModuleUri')}:</Form.Label>
        <Form.Control style={{ maxWidth: "200px" }}>
          <TextInput
            name="loginModuleUri"
            value={formik.values.loginModuleUri}
            onChange={formik.handleChange}
            error={formik.errors.loginModuleUri} />
        </Form.Control>
      </Form.Field>
      <Form.Field>
        <Form.Label>{t('client.applicationType')}:</Form.Label>
        <Form.Control style={{ maxWidth: "200px" }}>
          <SelectInput
            options={applicationTypeOptions}
            name="applicationType"
            value={formik.values.applicationType}
            error={formik.errors.applicationType}
            onChange={formik.handleChange} />
        </Form.Control>
      </Form.Field>
      {formik.values["applicationType"] && <Form.Field>
        <Form.Label required>{t('client.grantTypes')}:</Form.Label>
        <Form.Control style={{ maxWidth: "200px" }}>
          <CheckBoxGroup
            name="grantTypes"
            values={formik.values.grantTypes}
            style={{
              minWidth: "200px",
              minHeight: "50px"
            }}
            error={Array.isArray(formik.errors.grantTypes) ? formik.errors.grantTypes.join(", ") : formik.errors.grantTypes}
            onChange={(name, value) => formik.setFieldValue("grantTypes", value)}>
            {ApplicationGrantType[formik.values["applicationType"]]?.map(grantType => ({ name: GrantType[grantType], value: grantType }))
              .map(option => <CheckBox key={String(option.value)} label={option.name} value={option.value} />)}
          </CheckBoxGroup>
        </Form.Control>
      </Form.Field>}
      {formik.values["grantTypes"]?.includes('client_credentials') &&
        <Form.Field>
          <Form.Label>{t('client.scopes')}:</Form.Label>
          <Form.Control style={{ minWidth: "300px", maxWidth: "500px" }}>
            <DualList
              options={options}
              initialValues={formik.initialValues.scope}
              placeholder="Введите имя роли"
              name="scope"
              isLoading={isLoading}
              onInputChange={e => dispatch(roleActions.getOptionsStart({ params: e.target.value }))}
              onSelectionChange={formik.setFieldValue}
              touched={formik.touched.scope}
              error={formik.errors.scope} />
          </Form.Control>
        </Form.Field>
      }
    </Form>
  )
})

export default ClientEditPage;