import React, { useEffect, HTMLAttributes } from 'react';
import { useHistory, useParams } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { Client, ClientState } from '../types';
import { AppState } from '../../../redux/store';
import { getClientById, updateClient, getRoles } from '../state/redux/actions';
import { useFormik } from 'formik';
import { GrantType, ApplicationGrantType } from '@jfront/oauth-core';
import { Form, TextInput, CheckBoxGroup, CheckBox, SelectInput,  } from '@jfront/ui-core';
import { DualList } from '@jfront/ui-dual-list';
import { Text } from '../../../components/form/Field';
import { useTranslation } from 'react-i18next';

const ClientEditPage = React.forwardRef<HTMLFormElement, HTMLAttributes<HTMLFormElement>>((props, ref) => {
  const dispatch = useDispatch();
  const history = useHistory();
  const { clientId } = useParams<any>();
  const { t } = useTranslation();
  const { current, roles, rolesLoading } = useSelector<AppState, ClientState>(state => state.client);

  const applicationTypeOptions = [
    { name: "Native", value: "native" },
    { name: "WEB application", value: "web" },
    { name: "Browser (client-side) application", value: "browser" },
    { name: "Service", value: "service" },
  ]

  useEffect(() => {
    dispatch(getRoles(""));
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (!current && clientId) {
      dispatch(getClientById(clientId, t("dataLoadingMessage")));
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [current, clientId, dispatch]);

  const formik = useFormik<Client>({
    initialValues: { clientId: '', clientName: '', clientNameEn: '', ...current },
    enableReinitialize: true,
    onSubmit: (values: Client) => {
      if (clientId) {
        dispatch(updateClient(clientId, values, t("saveMessage"), (client: Client) => {
          history.push(`/ui/client/${client.clientId}/view/`);
        }));
      }
    },
    validate: (values) => {
      const errors: { clientName?: string, applicationType?: string, grantTypes?: string } = {};
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

  return (
    <Form onSubmit={formik.handleSubmit} ref={ref}>
      <Form.Field>
        <Form.Label>{t('client.clientId')}:</Form.Label>
        <Text>{current?.clientId}</Text>
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
      <Form.Field>
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
              options={roles ? roles : []}
              initialValues={formik.initialValues.scope}
              placeholder="Введите имя роли"
              name="scope"
              isLoading={rolesLoading}
              onInputChange={e => dispatch(getRoles(e.target.value))}
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