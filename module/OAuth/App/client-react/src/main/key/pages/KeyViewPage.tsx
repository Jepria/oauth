import React, { useEffect } from 'react';
import { Text } from '../../../components/form/Field';
import { AppState } from '../../../redux/store';
import { KeyState } from '../types';
import { useSelector, useDispatch } from 'react-redux';
import { getKey } from '../state/redux/actions';
import { Panel, Form } from '@jfront/ui-core';

const KeyViewPage: React.FC = () => {

  const dispatch = useDispatch();
  const { current } = useSelector<AppState, KeyState>(state => state.key);

  useEffect(() => {
    if (!current) {
      dispatch(getKey());
    }
  }, [current, dispatch]);

  return (
    <Panel>
      <Panel.Content>
        <Form>
          <Form.Field>
            <Form.Label>ID:</Form.Label>
            <Text>{current?.keyId}</Text>
          </Form.Field>
          <Form.Field>
            <Form.Label>Публичный ключ:</Form.Label>
            <Text width="200px">{current?.publicKey}</Text>
          </Form.Field>
          <Form.Field>
            <Form.Label>Дата создания:</Form.Label>
            <Text>{current?.dateIns}</Text>
          </Form.Field>
        </Form>
      </Panel.Content>
    </Panel>
  )

}

export default KeyViewPage;