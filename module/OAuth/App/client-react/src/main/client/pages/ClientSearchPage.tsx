import React, { HTMLAttributes, useImperativeHandle } from 'react';
import { useHistory } from 'react-router-dom';
import { FormField, Label } from '../../../components/form/Field';
import { Formik, Form, Field } from 'formik';
import { TextInput } from '../../../components/form/input/TextInput';
import { useSelector, useDispatch } from 'react-redux';
import { ClientSearchTemplate } from '../types';
import { postSearchClientRequest } from '../state/redux/actions';
import { AppState } from '../../store';
import { Page, Content, FormContainer } from 'jfront-components';

const ClientSearchPage = React.forwardRef<any, HTMLAttributes<HTMLFormElement>>((props, ref) => {
  const dispatch = useDispatch();
  const history = useHistory();
  const searchTemplate = useSelector<AppState, ClientSearchTemplate | undefined>(state => state.client.searchRequest?.template);
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
            innerRef={formik => formikRef = formik}
            initialValues={searchTemplate ? searchTemplate : {}}
            onSubmit={(values: ClientSearchTemplate) => {
              dispatch(postSearchClientRequest({
                template: values
              }));
              history.push('/ui/client/list');
            }}>
            <Form {...props}>
              <FormField>
                <Label width={'250px'}>Имя клиентского приложения:</Label>
                <Field name="clientName" as={TextInput} />
              </FormField>
              <FormField>
                <Label width={'250px'}>Имя клиентского приложения(англ):</Label>
                <Field name="clientNameEn" as={TextInput} />
              </FormField>
            </Form>
          </Formik>
        </FormContainer>
      </Content>
    </Page>
  )
})

export default ClientSearchPage;