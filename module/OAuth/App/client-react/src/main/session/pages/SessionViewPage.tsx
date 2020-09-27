import React, { useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { Text } from '../../../components/form/Field';
import { AppState } from '../../../redux/store';
import { SessionState } from '../types';
import { useSelector, useDispatch } from 'react-redux';
import { getSessionById } from '../state/redux/actions';
import { Panel, Row, Form } from '@jfront/ui-core';
import { useTranslation } from 'react-i18next';

const SessionViewPage: React.FC = () => {

  const dispatch = useDispatch();
  const { sessionId } = useParams<any>();
  const { t } = useTranslation();
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
              <Form.Label>{t('session.sessionId')}:</Form.Label>
              <Text>{current?.sessionId}</Text>
            </Form.Field>
            <Form.Field>
              <Form.Label>{t('session.dateIns')}:</Form.Label>
              <Text>{current?.dateIns}</Text>
            </Form.Field>
          </Row>
          <Row>
            <Form.FieldSet legend={t('session.client')} style={{ flexGrow: 1, margin: "0 5px", flexBasis: "50%" }}>
              <Form.Field>
                <Form.Label>{t('session.clientId')}:</Form.Label>
                <Text>{current?.client?.value}</Text>
              </Form.Field>
              <Form.Field>
                <Form.Label>{t('session.clientName')}:</Form.Label>
                <Text>{current?.client?.name}</Text>
              </Form.Field>
              <Form.Field>
                <Form.Label>{t('session.redirectUri')}:</Form.Label>
                <Text>{current?.redirectUri}</Text>
              </Form.Field>
            </Form.FieldSet>
            <Form.FieldSet legend={t('session.operator')} style={{ flexGrow: 1, margin: "0 5px", flexBasis: "50%"  }}>
              <Form.Field>
                <Form.Label>{t('session.operatorId')}:</Form.Label>
                <Text>{current?.operator?.value}</Text>
              </Form.Field>
              <Form.Field>
                <Form.Label>{t('session.operatorLogin')}:</Form.Label>
                <Text>{current?.operatorLogin}</Text>
              </Form.Field>
              <Form.Field>
                <Form.Label>{t('session.operatorName')}:</Form.Label>
                <Text>{current?.operator?.name}</Text>
              </Form.Field>
            </Form.FieldSet>
          </Row>
          <Row>
            {current?.accessTokenId &&
              <Form.FieldSet legend={t('session.accessToken')} style={{ flexGrow: 1, margin: "0 5px", flexBasis: "30%"  }}>
                <Form.Field>
                  <Form.Label>{t('session.accessTokenId')}:</Form.Label>
                  <Text>{current?.accessTokenId}</Text>
                </Form.Field>
                <Form.Field>
                  <Form.Label>{t('session.accessTokenDateIns')}:</Form.Label>
                  <Text>{current?.accessTokenDateIns}</Text>
                </Form.Field>
                <Form.Field>
                  <Form.Label>{t('session.accessTokenDateFinish')}:</Form.Label>
                  <Text>{current?.accessTokenDateFinish}</Text>
                </Form.Field>
              </Form.FieldSet>
            }
            {current?.refreshTokenId &&
              <Form.FieldSet legend={t('session.refreshToken')} style={{ flexGrow: 1, margin: "0 5px", flexBasis: "30%"  }}>
                <Form.Field>
                  <Form.Label>{t('session.refreshTokenId')}:</Form.Label>
                  <Text>{current?.refreshTokenId}</Text>
                </Form.Field>
                <Form.Field>
                  <Form.Label>{t('session.refreshTokenDateIns')}:</Form.Label>
                  <Text>{current?.refreshTokenDateIns}</Text>
                </Form.Field>
                <Form.Field>
                  <Form.Label>{t('session.refreshTokenDateFinish')}:</Form.Label>
                  <Text>{current?.refreshTokenDateFinish}</Text>
                </Form.Field>
              </Form.FieldSet>
            }
            {current?.sessionTokenId &&
              <Form.FieldSet legend={t('session.sessionToken')} style={{ flexGrow: 1, margin: "0 5px", flexBasis: "30%"  }}>
                <Form.Field>
                  <Form.Label>{t('session.sessionTokenId')}:</Form.Label>
                  <Text>{current?.sessionTokenId}</Text>
                </Form.Field>
                <Form.Field>
                  <Form.Label>{t('session.sessionTokenDateIns')}:</Form.Label>
                  <Text>{current?.sessionTokenDateIns}</Text>
                </Form.Field>
                <Form.Field>
                  <Form.Label>{t('session.sessionTokenDateFinish')}:</Form.Label>
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