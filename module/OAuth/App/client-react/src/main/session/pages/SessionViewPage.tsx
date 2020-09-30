import React, { useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { Text } from '../../../components/form/Field';
import { AppState } from '../../../redux/store';
import { SessionState } from '../types';
import { useSelector, useDispatch } from 'react-redux';
import { getSessionById } from '../state/redux/actions';
import { Row, Form } from '@jfront/ui-core';
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
    <Form>
      <Row>
        <Form.Field>
          <Form.Label>{t('session.sessionId')}:</Form.Label>
          <Text>{current?.sessionId}</Text>
        </Form.Field>
        {current?.dateIns && <Form.Field>
          <Form.Label>{t('session.dateIns')}:</Form.Label>
          <Text>{new Date(current.dateIns).toLocaleString()}</Text>
        </Form.Field>}
      </Row>
      <Row>
        <Form.FieldSet legend={t('session.client.legend')} style={{ flexGrow: 1, margin: "0 5px", flexBasis: "50%" }}>
          <Form.Field>
            <Form.Label>{t('session.client.id')}:</Form.Label>
            <Text>{current?.client?.value}</Text>
          </Form.Field>
          <Form.Field>
            <Form.Label>{t('session.client.name')}:</Form.Label>
            <Text>{current?.client?.name}</Text>
          </Form.Field>
          <Form.Field>
            <Form.Label>{t('session.client.redirectUri')}:</Form.Label>
            <Text>{current?.redirectUri}</Text>
          </Form.Field>
        </Form.FieldSet>
        <Form.FieldSet legend={t('session.operator.legend')} style={{ flexGrow: 1, margin: "0 5px", flexBasis: "50%" }}>
          <Form.Field>
            <Form.Label>{t('session.operator.id')}:</Form.Label>
            <Text>{current?.operator?.value}</Text>
          </Form.Field>
          <Form.Field>
            <Form.Label>{t('session.operator.login')}:</Form.Label>
            <Text>{current?.operatorLogin}</Text>
          </Form.Field>
          <Form.Field>
            <Form.Label>{t('session.operator.name')}:</Form.Label>
            <Text>{current?.operator?.name}</Text>
          </Form.Field>
        </Form.FieldSet>
      </Row>
      <Row>
        {current?.accessTokenId &&
          <Form.FieldSet legend={t('session.accessToken')} style={{ flexGrow: 1, margin: "0 5px", flexBasis: "30%" }}>
            <Form.Field>
              <Form.Label>{t('session.token.id')}:</Form.Label>
              <Text>{current?.accessTokenId}</Text>
            </Form.Field>
            {current.accessTokenDateIns && <Form.Field>
              <Form.Label>{t('session.token.dateIns')}:</Form.Label>
              <Text>{new Date(current.accessTokenDateIns).toLocaleString()}</Text>
            </Form.Field>}
            {current.accessTokenDateFinish && <Form.Field>
              <Form.Label>{t('session.token.dateFinish')}:</Form.Label>
              <Text>{new Date(current.accessTokenDateFinish).toLocaleString()}</Text>
            </Form.Field>}
          </Form.FieldSet>
        }
        {current?.refreshTokenId &&
          <Form.FieldSet legend={t('session.refreshToken')} style={{ flexGrow: 1, margin: "0 5px", flexBasis: "30%" }}>
            <Form.Field>
              <Form.Label>{t('session.token.id')}:</Form.Label>
              <Text>{current?.refreshTokenId}</Text>
            </Form.Field>
           {current.refreshTokenDateIns && <Form.Field>
              <Form.Label>{t('session.token.dateIns')}:</Form.Label>
              <Text>{new Date(current.refreshTokenDateIns).toLocaleString()}</Text>
            </Form.Field>}
            {current.refreshTokenDateFinish && <Form.Field>
              <Form.Label>{t('session.token.dateFinish')}:</Form.Label>
              <Text>{new Date(current.refreshTokenDateFinish).toLocaleString()}</Text>
            </Form.Field>}
          </Form.FieldSet>
        }
        {current?.sessionTokenId &&
          <Form.FieldSet legend={t('session.sessionToken')} style={{ flexGrow: 1, margin: "0 5px", flexBasis: "30%" }}>
            <Form.Field>
              <Form.Label>{t('session.token.id')}:</Form.Label>
              <Text>{current?.sessionTokenId}</Text>
            </Form.Field>
            {current.sessionTokenDateIns && <Form.Field>
              <Form.Label>{t('session.token.dateIns')}:</Form.Label>
              <Text>{new Date(current.sessionTokenDateIns).toLocaleString()}</Text>
            </Form.Field>}
            {current.sessionTokenDateFinish && <Form.Field>
              <Form.Label>{t('session.token.dateFinish')}:</Form.Label>
              <Text>{new Date(current.sessionTokenDateFinish).toLocaleString()}</Text>
            </Form.Field>}
          </Form.FieldSet>
        }
      </Row>
    </Form>
  )

}

export default SessionViewPage;