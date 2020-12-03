import React, { HTMLAttributes, useEffect } from 'react';
import { useHistory } from 'react-router-dom';
import { useFormik } from 'formik';
import { useDispatch, useSelector } from 'react-redux';
import { Client, ClientState } from '../types';
import { createClient, getRoles } from '../state/redux/actions';
import { GrantType, ApplicationGrantType } from '@jfront/oauth-core';
import { SelectInput, TextInput, CheckBoxGroup, CheckBox, Form} from '@jfront/ui-core';
import { DualList } from '@jfront/ui-dual-list';
import { AppState } from '../../../redux/store';
import { useTranslation } from 'react-i18next';

const ClientCreatePage = React.forwardRef<HTMLFormElement, HTMLAttributes<HTMLFormElement>>((props, ref) => {
  const dispatch = useDispatch();
  const history = useHistory();
  const { t } = useTranslation();
  const { roles, rolesLoading } = useSelector<AppState, ClientState>(state => state.client);

  const formik = useFormik<Client>({
    initialValues: { clientId: '', clientName: '', clientNameEn: '', applicationType: 'web', grantTypes: [] },
    onSubmit: (values: Client) => {
      dispatch(createClient(values, t("saveMessage"), (client: Client) => {
        history.push(`/ui/client/${client.clientId}/view/`);
      }));
    },
    validate: (values) => {
      const errors: { clientId?: string, clientName?: string, applicationType?: string, grantTypes?: string } = {};
      if (!values['clientId']) {
        errors.clientId = t('validation.notEmpty')
      } else {
        if (!/[A-Za-z0-9]/.test(values['clientId'])) {
          errors.clientId = t('validation.onlySymbolsAndDigits')
        }
        if (values['clientId'].length > 32) {
          errors.clientId = t('validation.maxLength')
        }
      }
      if (!values['clientName']) {
        errors.clientName = t('validation.notEmpty')
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

  const applicationTypeOptions = [
    { name: "WEB application", value: "web" },
    { name: "Browser (client-side) application", value: "browser" },
    { name: "Service", value: "service" },
    { name: "Native", value: "native" },
  ]


  useEffect(() => {
    dispatch(getRoles(""));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);


  return (
    <Form onSubmit={formik.handleSubmit} ref={ref}>
      <Form.Field>
        <Form.Label required>{t('client.clientId')}:</Form.Label>
        <Form.Control
          style={{ maxWidth: "200px" }}
          error={formik.errors.clientId}>
          <TextInput
            name="clientId"
            value={formik.values.clientId}
            onChange={formik.handleChange}
            error={formik.errors.clientId} />
        </Form.Control>
      </Form.Field>
      <Form.Field>
        <Form.Label required>{t('client.clientName')}:</Form.Label>
        <Form.Control
          style={{ maxWidth: "200px" }}
          error={formik.errors.clientName}>
          <TextInput
            name="clientName"
            value={formik.values.clientName}
            onChange={formik.handleChange}
            error={formik.errors.clientName} />
        </Form.Control>
      </Form.Field>
      <Form.Field>
        <Form.Label>{t('client.clientNameEn')}:</Form.Label>
        <Form.Control
          style={{ maxWidth: "200px" }}
          error={formik.errors.clientNameEn}>
          <TextInput
            name="clientNameEn"
            value={formik.values.clientNameEn}
            onChange={formik.handleChange}
            error={formik.errors.clientNameEn} />
        </Form.Control>
      </Form.Field>
      <Form.Field>
        <Form.Label>{t('client.applicationType')}:</Form.Label>
        <Form.Control
          style={{ maxWidth: "200px" }}
          error={formik.errors.applicationType}>
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
        <Form.Control
          style={{ maxWidth: "200px" }}
          error={Array.isArray(formik.errors.grantTypes) ? formik.errors.grantTypes.join(", ") : formik.errors.grantTypes}>
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
          <Form.Control
            style={{ minWidth: "300px", maxWidth: "500px" }}
            error={formik.errors.scope}>
            <DualList
              options={roles ? roles : []}
              placeholder="Введите имя роли"
              name="scope"
              isLoading={rolesLoading}
              onInputChange={e => dispatch(getRoles(e.target.value))}
              onSelectionChange={formik.setFieldValue}
              touched={formik.touched.scope}
              error={formik.errors.scope}
              style={{ height: "200px" }} />
          </Form.Control>
        </Form.Field>
      }
    </Form>
  )
});

export default ClientCreatePage;