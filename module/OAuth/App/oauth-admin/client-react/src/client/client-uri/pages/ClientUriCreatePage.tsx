import React, { HTMLAttributes } from 'react';
import { useHistory, useParams } from 'react-router-dom';
import { useFormik } from 'formik';
import { useDispatch } from 'react-redux';
import { ClientUri, ClientUriCreateDto } from '../types';
import { actions } from '../state/clientUriCrudSlice';
import { Form, TextInput } from '@jfront/ui-core';
import { useTranslation } from 'react-i18next';

export const ClientUriCreatePage = React.forwardRef<any, HTMLAttributes<HTMLFormElement>>((props, ref) => {
  const dispatch = useDispatch();
  const { clientId } = useParams<any>();
  const { t } = useTranslation();
  const history = useHistory();

  const formik = useFormik<ClientUriCreateDto>({
    initialValues: { clientUri: '' },
    onSubmit: (values: ClientUriCreateDto) => {
      values.clientId = clientId;
      if (clientId) {
        dispatch(actions.create({
          values,
          onSuccess: (clientUri: ClientUri) => {
            history.push(`/client/${clientId}/client-uri/${clientUri.clientUriId}/detail`);
          }
        }));
      }
    },
    validate: (values) => {
      const errors: { clientUri?: string } = {};
      if (!values.clientUri) {
        errors.clientUri = t('validation.notEmpty')
      }
      return errors;
    }
  });

  return (
    <Form onSubmit={formik.handleSubmit} ref={ref}>
      <Form.Field>
        <Form.Label required>{t('clientUri.clientUri')}:</Form.Label>
        <Form.Control
          style={{ maxWidth: "400px" }}
          error={formik.errors.clientUri}>
          <TextInput
            name="clientUri"
            value={formik.values.clientUri}
            onChange={formik.handleChange}
            error={formik.errors.clientUri} />
        </Form.Control>
      </Form.Field>
    </Form>
  )
});