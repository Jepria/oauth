import React, { HTMLAttributes } from 'react';
import { useHistory, useParams } from 'react-router-dom';
import { useFormik } from 'formik';
import { useDispatch } from 'react-redux';
import { ClientUri } from '../types';
import { createClientUri } from '../state/redux/actions';
import { Panel, Form, TextInput } from '@jfront/ui-core';
import { useTranslation } from 'react-i18next';

export const ClientUriCreatePage = React.forwardRef<any, HTMLAttributes<HTMLFormElement>>((props, ref) => {
  const dispatch = useDispatch();
  const { clientId } = useParams<any>();
  const { t } = useTranslation();
  const history = useHistory();

  const formik = useFormik<ClientUri>({
    initialValues: { clientUri: '' },
    onSubmit: (values: ClientUri) => {
      if (clientId) {
        dispatch(createClientUri(clientId, values, (clientUri: ClientUri) => {
          history.push(`/ui/client/${clientId}/client-uri/${clientUri.clientUriId}/view/`);
        }));
      }
    },
    validate: (values) => {
      const errors: { clientUri?: string } = {};

      if (!values['clientUri']) {
        errors.clientUri = 'Поле должно быть заполнено'
      }
      return errors;
    }
  });

  return (
    <Panel>
      <Panel.Content>
        <Form onSubmit={formik.handleSubmit} ref={ref}>
          <Form.Field>
            <Form.Label required>{t('clientUri.clientUri')}:</Form.Label>
            <Form.Control
              style={{ maxWidth: "200px" }}
              error={formik.errors.clientUri}>
              <TextInput
                name="clientUri"
                value={formik.values.clientUri}
                onChange={formik.handleChange}
                error={formik.errors.clientUri} />
            </Form.Control>
          </Form.Field>
        </Form>
      </Panel.Content>
    </Panel >
  )
});