import React, { useEffect } from 'react';
import { setCurrentRecord, searchClientUri } from '../state/redux/actions';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory, useParams, useLocation } from 'react-router-dom';
import { AppState } from '../../../../redux/store';
import { ClientUriState, ClientUri } from '../types';
import { HistoryState } from '../../../../components/HistoryState';
import { TextCell } from '../../../../components/cell/TextCell';
import { Grid } from '@jfront/ui-core';

export const ClientUriListPage: React.FC = () => {

  const dispatch = useDispatch();
  const history = useHistory();
  const { clientId } = useParams<any>();
  const { records, current } = useSelector<AppState, ClientUriState>(state => state.clientUri);
  const { state } = useLocation<HistoryState>();

  useEffect(() => {
    if (clientId) {
      dispatch(searchClientUri(clientId));
    }
  }, [clientId, dispatch]);

  return (
    <Grid<ClientUri>
      columns={[
        {
          Header: "ID",
          accessor: "clientUriId",
          Cell: ({ value }: any) => <TextCell>{value}</TextCell>
        },
        {
          Header: "URL для переадресации",
          accessor: "clientUri",
          Cell: ({ value }: any) => <TextCell>{value}</TextCell>
        },
      ]}
      data={React.useMemo(() => records, [records])}
      onSelection={(selected) => {
        if (selected) {
          if (selected.length === 1) {
            if (selected[0] !== current) {
              dispatch(setCurrentRecord(selected[0]))
            }
          } else if (current) {
            dispatch(setCurrentRecord(undefined))
          }
        }
      }}
      onDoubleClick={(record) => current !== record ? dispatch(setCurrentRecord(record,
        () => history.push(`/ui/client/${clientId}/client-uri/${record?.clientUriId}/view`, state)))
        : history.push(`/ui/client/${clientId}/client-uri/${record?.clientUriId}/view`,
          state)} />
  );
}