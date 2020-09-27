import React, { useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { Text } from '../../../../components/form/Field';
import { AppState } from '../../../../redux/store';
import { ClientUriState } from '../types';
import { useSelector, useDispatch } from 'react-redux';
import { getClientUriById } from '../state/redux/actions';
import { Form } from '@jfront/ui-core';

export const ClientUriViewPage: React.FC = () => {

  const dispatch = useDispatch();
  const { clientId, clientUriId } = useParams<any>();
  const { current } = useSelector<AppState, ClientUriState>(state => state.clientUri);

  useEffect(() => {
    if (!current && clientId && clientUriId) {
      dispatch(getClientUriById(clientId, clientUriId));
    }
  }, [current, clientId, clientUriId, dispatch]);

  return (
    <Form>
      <Form.Field>
        <Form.Label >ID записи:</Form.Label>
        <Text>{current?.clientUriId}</Text>
      </Form.Field>
      <Form.Field>
        <Form.Label>URL для переадресации:</Form.Label>
        <Text>{current?.clientUri}</Text>
      </Form.Field>
    </Form>
  )
}