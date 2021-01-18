import React, { useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { Text } from '../../app/common/components/form/Field';
import { AppState } from '../../app/store/reducer';
import { useSelector, useDispatch } from 'react-redux';
import { actions } from '../state/sessionCrudSlice';
import { Row, Form } from '@jfront/ui-core';
import { useTranslation } from 'react-i18next';
import { Session } from '../types';
import { EntityState } from '@jfront/core-redux-saga';

const SessionViewPage: React.FC = () => {

  const dispatch = useDispatch();
  const { sessionId } = useParams<any>();
  const { t } = useTranslation();
  const { currentRecord } = useSelector<AppState, EntityState<Session>>(state => state.session.crudSlice);

  useEffect(() => {
    if (!currentRecord && sessionId) {
      dispatch(actions.getRecordById({ primaryKey: sessionId }));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <Form>
      <Row>
        <Form.Field>
          <Form.Label>{t('session.sessionId')}:</Form.Label>
          <Text>{currentRecord?.sessionId}</Text>
        </Form.Field>
        {currentRecord?.dateIns && <Form.Field>
          <Form.Label>{t('session.dateIns')}:</Form.Label>
          <Text>{new Date(currentRecord.dateIns).toLocaleString()}</Text>
        </Form.Field>}
      </Row>
      <Row>
        <Form.FieldSet legend={t('session.client.legend')} style={{ flexGrow: 1, margin: "0 5px", flexBasis: "50%" }}>
          <Form.Field>
            <Form.Label>{t('session.client.id')}:</Form.Label>
            <Text>{currentRecord?.client?.value}</Text>
          </Form.Field>
          <Form.Field>
            <Form.Label>{t('session.client.name')}:</Form.Label>
            <Text>{currentRecord?.client?.name}</Text>
          </Form.Field>
          <Form.Field>
            <Form.Label>{t('session.client.redirectUri')}:</Form.Label>
            <Text>{currentRecord?.redirectUri}</Text>
          </Form.Field>
        </Form.FieldSet>
        <Form.FieldSet legend={t('session.operator.legend')} style={{ flexGrow: 1, margin: "0 5px", flexBasis: "50%" }}>
          <Form.Field>
            <Form.Label>{t('session.operator.id')}:</Form.Label>
            <Text>{currentRecord?.operator?.value}</Text>
          </Form.Field>
          <Form.Field>
            <Form.Label>{t('session.operator.login')}:</Form.Label>
            <Text>{currentRecord?.operatorLogin}</Text>
          </Form.Field>
          <Form.Field>
            <Form.Label>{t('session.operator.name')}:</Form.Label>
            <Text>{currentRecord?.operator?.name}</Text>
          </Form.Field>
        </Form.FieldSet>
      </Row>
      <Row>
        {currentRecord?.accessTokenId &&
          <Form.FieldSet legend={t('session.accessToken')} style={{ flexGrow: 1, margin: "0 5px", flexBasis: "30%" }}>
            <Form.Field>
              <Form.Label>{t('session.token.id')}:</Form.Label>
              <Text>{currentRecord?.accessTokenId}</Text>
            </Form.Field>
            {currentRecord.accessTokenDateIns && <Form.Field>
              <Form.Label>{t('session.token.dateIns')}:</Form.Label>
              <Text>{new Date(currentRecord.accessTokenDateIns).toLocaleString()}</Text>
            </Form.Field>}
            {currentRecord.accessTokenDateFinish && <Form.Field>
              <Form.Label>{t('session.token.dateFinish')}:</Form.Label>
              <Text>{new Date(currentRecord.accessTokenDateFinish).toLocaleString()}</Text>
            </Form.Field>}
          </Form.FieldSet>
        }
        {currentRecord?.refreshTokenId &&
          <Form.FieldSet legend={t('session.refreshToken')} style={{ flexGrow: 1, margin: "0 5px", flexBasis: "30%" }}>
            <Form.Field>
              <Form.Label>{t('session.token.id')}:</Form.Label>
              <Text>{currentRecord?.refreshTokenId}</Text>
            </Form.Field>
            {currentRecord.refreshTokenDateIns && <Form.Field>
              <Form.Label>{t('session.token.dateIns')}:</Form.Label>
              <Text>{new Date(currentRecord.refreshTokenDateIns).toLocaleString()}</Text>
            </Form.Field>}
            {currentRecord.refreshTokenDateFinish && <Form.Field>
              <Form.Label>{t('session.token.dateFinish')}:</Form.Label>
              <Text>{new Date(currentRecord.refreshTokenDateFinish).toLocaleString()}</Text>
            </Form.Field>}
          </Form.FieldSet>
        }
        {currentRecord?.sessionTokenId &&
          <Form.FieldSet legend={t('session.sessionToken')} style={{ flexGrow: 1, margin: "0 5px", flexBasis: "30%" }}>
            <Form.Field>
              <Form.Label>{t('session.token.id')}:</Form.Label>
              <Text>{currentRecord?.sessionTokenId}</Text>
            </Form.Field>
            {currentRecord.sessionTokenDateIns && <Form.Field>
              <Form.Label>{t('session.token.dateIns')}:</Form.Label>
              <Text>{new Date(currentRecord.sessionTokenDateIns).toLocaleString()}</Text>
            </Form.Field>}
            {currentRecord.sessionTokenDateFinish && <Form.Field>
              <Form.Label>{t('session.token.dateFinish')}:</Form.Label>
              <Text>{new Date(currentRecord.sessionTokenDateFinish).toLocaleString()}</Text>
            </Form.Field>}
          </Form.FieldSet>
        }
      </Row>
    </Form>
  )

}

export default SessionViewPage;