import React, { useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { Page, Content, VerticalLayout, Form as FormContainer } from '../../../../components/Layout';
import { FormField, Label, Text } from '../../../../components/form/Field';
import { AppState } from '../../../store';
import { ClientUriState } from '../types';
import { useSelector, useDispatch } from 'react-redux';
import { getClientUriById } from '../state/redux/actions';

export const ClientUriViewPage: React.FC = () => {

  const dispatch = useDispatch();
  const { clientId, clientUriId } = useParams();
  const { current } = useSelector<AppState, ClientUriState>(state => state.clientUri);

  useEffect(() => {
    if (!current && clientId && clientUriId) {
      dispatch(getClientUriById(clientId, clientUriId));
    }
  }, [current, clientId, clientUriId, dispatch]);

  return (
    <Page>
      <Content>
        <FormContainer>
          <VerticalLayout>
            <FormField>
              <Label width={'200px'}>ID записи:</Label>
              <Text>{current?.clientUriId}</Text>
            </FormField>
            <FormField>
              <Label width={'200px'}>URL для переадресации:</Label>
              <Text>{current?.clientUri}</Text>
            </FormField>
          </VerticalLayout>
        </FormContainer>
      </Content>
    </Page>
  )
}