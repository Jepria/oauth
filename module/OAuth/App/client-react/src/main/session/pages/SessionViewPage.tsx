import React, { useEffect } from 'react';
import styled from 'styled-components';
import { useParams } from 'react-router-dom';
import { FormField, Label, Text } from '../../../components/form/Field';
import { AppState } from '../../store';
import { SessionState } from '../types';
import { useSelector, useDispatch } from 'react-redux';
import { getSessionById } from '../state/redux/actions';
import { Page, Content, FormContainer, VerticalLayout } from 'jfront-components';

const FieldGroup = styled.div`
  display: flex;
  flex-grow: 1;
  flex-direction: row;
`;

const VerticalGroup = styled.fieldset`  
  display: flex;
  flex-grow: 1;
  flex-direction: column;
  margin: 5px;
  border-color: #4A66A5;
  border-style: solid;
  border-radius: 5px;
`;

const Legend = styled.legend`
  font-size: 12px;
  font-color: #4A66A5;
  padding: 0 5px;
`;

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
              <Label>Дата создания записи:</Label>
              <Text>{current?.dateIns}</Text>
            </FormField>
            <FieldGroup>
              <VerticalGroup>
                <Legend>Клиент</Legend>
                <FormField>
                  <Label>ID:</Label>
                  <Text>{current?.client?.value}</Text>
                </FormField>
                <FormField>
                  <Label>Наименование:</Label>
                  <Text>{current?.client?.name}</Text>
                </FormField>
                <FormField>
                  <Label>URL переадресации:</Label>
                  <Text>{current?.redirectUri}</Text>
                </FormField>
              </VerticalGroup>
              <VerticalGroup>
                <Legend>Пользователь</Legend>
                <FormField>
                  <Label>ID:</Label>
                  <Text>{current?.operator?.value}</Text>
                </FormField>
                <FormField>
                  <Label>Логин:</Label>
                  <Text>{current?.operatorLogin}</Text>
                </FormField>
                <FormField>
                  <Label>Имя:</Label>
                  <Text>{current?.operator?.name}</Text>
                </FormField>
              </VerticalGroup>
            </FieldGroup>
            <FieldGroup>
              {current?.accessTokenId &&
                <VerticalGroup>
                <Legend>Токен доступа</Legend>
                  <FormField>
                    <Label>ID:</Label>
                    <Text>{current?.accessTokenId}</Text>
                  </FormField>
                  <FormField>
                    <Label>Дата создания:</Label>
                    <Text>{current?.accessTokenDateIns}</Text>
                  </FormField>
                  <FormField>
                    <Label>Дата окончания действия:</Label>
                    <Text>{current?.accessTokenDateFinish}</Text>
                  </FormField>
                </VerticalGroup>
              }
              {current?.refreshTokenId &&
                <VerticalGroup>
                <Legend>Токен обновления</Legend>
                  <FormField>
                    <Label>ID:</Label>
                    <Text>{current?.refreshTokenId}</Text>
                  </FormField>
                  <FormField>
                    <Label>Дата создания:</Label>
                    <Text>{current?.refreshTokenDateIns}</Text>
                  </FormField>
                  <FormField>
                    <Label>Дата окончания действия:</Label>
                    <Text>{current?.refreshTokenDateFinish}</Text>
                  </FormField>
                </VerticalGroup>
              }
              {current?.sessionTokenId &&
                <VerticalGroup>
                <Legend>Токен сессии</Legend>
                  <FormField>
                    <Label>ID:</Label>
                    <Text>{current?.sessionTokenId}</Text>
                  </FormField>
                  <FormField>
                    <Label>Дата создания:</Label>
                    <Text>{current?.sessionTokenDateIns}</Text>
                  </FormField>
                  <FormField>
                    <Label>Дата окончания действия:</Label>
                    <Text>{current?.sessionTokenDateFinish}</Text>
                  </FormField>
                </VerticalGroup>
              }
            </FieldGroup>
          </VerticalLayout>
        </FormContainer>
      </Content>
    </Page>
  )

}

export default SessionViewPage;