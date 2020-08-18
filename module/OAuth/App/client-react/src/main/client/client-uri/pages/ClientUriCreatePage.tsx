import React, { HTMLAttributes, useImperativeHandle } from 'react';
import { useHistory, useParams } from 'react-router-dom';
import { FormField, Label } from '../../../../components/form/Field';
import { Formik, Form, Field, FieldProps } from 'formik';
import { TextInput } from '../../../../components/form/input/TextInput';
import { useDispatch } from 'react-redux';
import { ClientUri } from '../types';
import { createClientUri } from '../state/redux/actions';
import { Page, Content, FormContainer } from '@jfront/ui-core';

export const ClientUriCreatePage = React.forwardRef<any, HTMLAttributes<HTMLFormElement>>((props, ref) => {
  const dispatch = useDispatch();
  const { clientId } = useParams();
  const history = useHistory();
  let formikRef: any;

  useImperativeHandle(ref, () => ({
    handleSubmit: () => {
      formikRef.handleSubmit();
    }
  }));

  return (
    <Page>
      <Content>
        <FormContainer>
          <Formik
            innerRef={instance => formikRef = instance}
            initialValues={{ clientUri: '' }}
            onSubmit={(values: ClientUri) => {
              if (clientId) {
                dispatch(createClientUri(clientId, values, (clientUri: ClientUri) => {
                  history.push(`/ui/client/${clientId}/client-uri/${clientUri.clientUriId}/view/`);
                }));
              }
            }}
            validate={(values) => {
              const errors: { clientUri?: string } = {};

              if (!values['clientUri']) {
                errors.clientUri = 'Поле должно быть заполнено'
              }
              return errors;
            }}>
            <Form {...props}>
              <FormField>
                <Label width={'200px'}>URL для переадресации:</Label>
                <Field name="clientUri">
                  {(props: FieldProps) => (
                    <TextInput
                      name={props.field.name}
                      value={props.field.value}
                      onChange={props.field.onChange}
                      onBlur={props.field.onBlur}
                      touched={props.meta.touched}
                      error={props.meta.error} />
                  )}
                </Field>
              </FormField>
            </Form>
          </Formik>
        </FormContainer>
      </Content>
    </Page>
  )
});