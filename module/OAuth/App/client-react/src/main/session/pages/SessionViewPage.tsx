import React, { useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { Text } from '../../../components/form/Field';
import { AppState } from '../../../redux/store';
import { SessionState } from '../types';
import { useSelector, useDispatch } from 'react-redux';
import { getSessionById } from '../state/redux/actions';
import { Panel, Row, Form } from '@jfront/ui-core';

const SessionViewPage: React.FC = () => {

  const dispatch = useDispatch();
  const { sessionId } = useParams<any>();
  const { current } = useSelector<AppState, SessionState>(state => state.session);

  useEffect(() => {
    if (!current && sessionId) {
      dispatch(getSessionById(sessionId));
    }
  }, [current, sessionId, dispatch]);

  return (
    <Panel>
      <Panel.Content>
        <Form>
          <Row>
            <Form.Field>
              <Form.Label>ID сессии:</Form.Label>
              <Text>{current?.sessionId}</Text>
            </Form.Field>
            <Form.Field>
              <Form.Label>Дата создания записи:</Form.Label>
              <Text>{current?.dateIns}</Text>
            </Form.Field>
          </Row>
          <Row>
            <Form.FieldSet legend="Приложение" style={{ flexGrow: 1, margin: "0 5px", flexBasis: "50%" }}>
              <Form.Field>
                <Form.Label>ID:</Form.Label>
                <Text>{current?.client?.value}</Text>
              </Form.Field>
              <Form.Field>
                <Form.Label>Наименование:</Form.Label>
                <Text>{current?.client?.name}</Text>
              </Form.Field>
              <Form.Field>
                <Form.Label>URL переадресации:</Form.Label>
                <Text>{current?.redirectUri}</Text>
              </Form.Field>
            </Form.FieldSet>
            <Form.FieldSet legend="Пользователь" style={{ flexGrow: 1, margin: "0 5px", flexBasis: "50%"  }}>
              <Form.Field>
                <Form.Label>ID:</Form.Label>
                <Text>{current?.operator?.value}</Text>
              </Form.Field>
              <Form.Field>
                <Form.Label>Логин:</Form.Label>
                <Text>{current?.operatorLogin}</Text>
              </Form.Field>
              <Form.Field>
                <Form.Label>Имя:</Form.Label>
                <Text>{current?.operator?.name}</Text>
              </Form.Field>
            </Form.FieldSet>
          </Row>
          <Row>
            {current?.accessTokenId &&
              <Form.FieldSet legend="Токен доступа" style={{ flexGrow: 1, margin: "0 5px", flexBasis: "30%"  }}>
                <Form.Field>
                  <Form.Label>ID:</Form.Label>
                  <Text>{current?.accessTokenId}</Text>
                </Form.Field>
                <Form.Field>
                  <Form.Label>Дата создания:</Form.Label>
                  <Text>{current?.accessTokenDateIns}</Text>
                </Form.Field>
                <Form.Field>
                  <Form.Label>Дата окончания действия:</Form.Label>
                  <Text>{current?.accessTokenDateFinish}</Text>
                </Form.Field>
              </Form.FieldSet>
            }
            {current?.refreshTokenId &&
              <Form.FieldSet legend="Токен обновления" style={{ flexGrow: 1, margin: "0 5px", flexBasis: "30%"  }}>
                <Form.Field>
                  <Form.Label>ID:</Form.Label>
                  <Text>{current?.refreshTokenId}</Text>
                </Form.Field>
                <Form.Field>
                  <Form.Label>Дата создания:</Form.Label>
                  <Text>{current?.refreshTokenDateIns}</Text>
                </Form.Field>
                <Form.Field>
                  <Form.Label>Дата окончания действия:</Form.Label>
                  <Text>{current?.refreshTokenDateFinish}</Text>
                </Form.Field>
              </Form.FieldSet>
            }
            {current?.sessionTokenId &&
              <Form.FieldSet legend="Токен сессии" style={{ flexGrow: 1, margin: "0 5px", flexBasis: "30%"  }}>
                <Form.Field>
                  <Form.Label>ID:</Form.Label>
                  <Text>{current?.sessionTokenId}</Text>
                </Form.Field>
                <Form.Field>
                  <Form.Label>Дата создания:</Form.Label>
                  <Text>{current?.sessionTokenDateIns}</Text>
                </Form.Field>
                <Form.Field>
                  <Form.Label>Дата окончания действия:</Form.Label>
                  <Text>{current?.sessionTokenDateFinish}</Text>
                </Form.Field>
              </Form.FieldSet>
            }
          </Row>
        </Form>
      </Panel.Content>
    </Panel>
  )

}

export default SessionViewPage;