import React, { useEffect } from 'react';
import { Page, Content } from '../../../../components/Layout';
import { setCurrentRecord, searchClientUri } from '../state/redux/actions';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory, useParams, useLocation } from 'react-router-dom';
import { AppState } from '../../../store';
import { ClientUriState } from '../types';
import { TextCell, Grid } from '../../../../components/grid';
import { HistoryState } from '../../../../components/HistoryState';

export const ClientUriListPage: React.FC = () => {

  const dispatch = useDispatch();
  const history = useHistory();
  const { clientId } = useParams();
  const { records, current } = useSelector<AppState, ClientUriState>(state => state.clientUri);
  const { state } = useLocation<HistoryState>();

  useEffect(() => {
    if (clientId) {
      dispatch(searchClientUri(clientId));
    }
  }, [clientId, dispatch]);

  return (
    <Page>
      <Content>
        <Grid>
          <Grid.Table>
            <Grid.Header>
              <Grid.HeaderCell>ID клиентского приложения</Grid.HeaderCell>
              <Grid.HeaderCell>ID записи</Grid.HeaderCell>
              <Grid.HeaderCell>URL для переадресации</Grid.HeaderCell>
            </Grid.Header>
            <Grid.Body>
              {records && records.map(record => {
                return (
                  <Grid.Row key={record.clientUriId}
                    onClick={() => dispatch(setCurrentRecord(record))}
                    onDoubleClick={() => current !== record ? dispatch(setCurrentRecord(record,
                      () => history.push(`/ui/client/${clientId}/client-uri/${record.clientUriId}/view`, state))) : history.push(`/ui/client/${clientId}/client-uri/${record.clientUriId}/view`, state)}
                    selected={record === current}>
                    <Grid.Column label="ID клиентского приложения">
                      <TextCell>{clientId}</TextCell>
                    </Grid.Column>
                    <Grid.Column label="ID записи">
                      <TextCell>{record.clientUriId}</TextCell>
                    </Grid.Column>
                    <Grid.Column label="URL для переадресации">
                      <TextCell>{record.clientUri}</TextCell>
                    </Grid.Column>
                  </Grid.Row>);
              })}
            </Grid.Body>
          </Grid.Table>
          <Grid.PagingBar maxRowCount={records?.length} onChange={() => {
            if (clientId) {
              dispatch(searchClientUri(clientId))
            }
          }} />
        </Grid>
      </Content>
    </Page>
  );
}