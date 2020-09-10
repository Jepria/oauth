import React, { useEffect } from 'react';
import { FormField, Label, Text } from '../../../components/form/Field';
import { AppState } from '../../store';
import { KeyState } from '../types';
import { useSelector, useDispatch } from 'react-redux';
import { getKey } from '../state/redux/actions';
import { Page, Content, FormContainer, VerticalLayout } from '@jfront/ui-core';

const KeyViewPage: React.FC = () => {

  const dispatch = useDispatch();
  const { current } = useSelector<AppState, KeyState>(state => state.key);

  useEffect(() => {
    if (!current) {
      dispatch(getKey());
    }
  }, [current, dispatch]);

  return (
    <Page>
      <Content>
        <FormContainer>
          <VerticalLayout>
            <FormField>
              <Label width="150px">ID:</Label>
              <Text>{current?.keyId}</Text>
            </FormField>
            <FormField>
              <Label width="150px">Публичный ключ:</Label>
              <Text width="200px">{current?.publicKey}</Text>
            </FormField>
            <FormField>
              <Label width="150px">Дата создания:</Label>
              <Text>{current?.dateIns}</Text>
            </FormField>
          </VerticalLayout>
        </FormContainer>
      </Content>
    </Page>
  )

}

export default KeyViewPage;