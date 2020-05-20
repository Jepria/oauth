import React, { useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { Page, Content, VerticalLayout, Form as FormContainer } from '../../../components/Layout';
import { FormField, Label, Text } from '../../../components/form/Field';
import { AppState } from '../../store';
import { SessionState } from '../types';
import { useSelector, useDispatch } from 'react-redux';
import { getSessionById } from '../state/redux/actions';

const SessionViewPage: React.FC = () => {

  const dispatch = useDispatch();
  const { sessionId } = useParams();
  const { current } = useSelector<AppState, SessionState>(state => state.session);

  useEffect(() => {
    if (!current && sessionId) {
      dispatch(getSessionById(sessionId));
    }
  }, [current, sessionId, dispatch]);

  return (
    <Page>
      <Content>
        <FormContainer>
          <VerticalLayout>
            <FormField>
              <Label>ID сессии:</Label>
              <Text>{current?.sessionId}</Text>
            </FormField>
            <FormField>
              <Label>Авторизационный код:</Label>
              <Text>{current?.authorizationCode}</Text>
            </FormField>
            <FormField>
              <Label>Дата создания записи:</Label>
              <Text>{current?.dateIns}</Text>
            </FormField>
            <FormField>
              <Label>Наименование приложения:</Label>
              <Text>{current?.client?.name}</Text>
            </FormField>
            <FormField>
              <Label>ID приложения:</Label>
              <Text>{current?.client?.value}</Text>
            </FormField>
            <FormField>
              <Label>Имя пользователя:</Label>
              <Text>{current?.operator?.name}</Text>
            </FormField>
            <FormField>
              <Label>Логин пользователя:</Label>
              <Text>{current?.operatorLogin}</Text>
            </FormField>
            <FormField>
              <Label>ID пользователя:</Label>
              <Text>{current?.operator?.value}</Text>
            </FormField>
            <FormField>
              <Label>ID токена доступа:</Label>
              <Text>{current?.accessTokenId}</Text>
            </FormField>
            <FormField>
              <Label>Дата создания токена доступа:</Label>
              <Text>{current?.accessTokenDateIns}</Text>
            </FormField>
            <FormField>
              <Label>Дата окончания действия токена доступа:</Label>
              <Text>{current?.accessTokenDateFinish}</Text>
            </FormField>
            <FormField>
              <Label>ID токена обновления:</Label>
              <Text>{current?.refreshTokenId}</Text>
            </FormField>
            <FormField>
              <Label>Дата создания токена обновления:</Label>
              <Text>{current?.refreshTokenDateIns}</Text>
            </FormField>
            <FormField>
              <Label>Дата окончания действия токена обновления:</Label>
              <Text>{current?.refreshTokenDateFinish}</Text>
            </FormField>
            <FormField>
              <Label>ID токена сессии:</Label>
              <Text>{current?.sessionTokenId}</Text>
            </FormField>
            <FormField>
              <Label>Дата создания токена сессии:</Label>
              <Text>{current?.sessionTokenDateIns}</Text>
            </FormField>
            <FormField>
              <Label>Дата окончания действия токена сессии:</Label>
              <Text>{current?.sessionTokenDateFinish}</Text>
            </FormField>
            <FormField>
              <Label>URL переадресации:</Label>
              <Text>{current?.redirectUri}</Text>
            </FormField>
          </VerticalLayout>
        </FormContainer>
      </Content>
    </Page>
  )

}

export default SessionViewPage;