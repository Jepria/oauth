import React, { useEffect } from 'react';
import { Page, Content } from '../../../../components/Layout';
import { setCurrentRecord, searchClientUri } from '../state/redux/actions';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory, useParams, useLocation } from 'react-router-dom';
import { AppState } from '../../../store';
import { ClientUriState } from '../types';
import { HistoryState } from '../../../../components/HistoryState';
import { Grid, GridTable, GridHeader, GridHeaderCell, GridBody, GridRow, GridRowCell, GridPagingBar } from '../../../../components/grid/StyledGrid';
import { TextCell } from '../../../../components/cell/TextCell';

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
          <GridTable>
            <GridHeader>
              <GridHeaderCell>ID клиентского приложения</GridHeaderCell>
              <GridHeaderCell>ID записи</GridHeaderCell>
              <GridHeaderCell>URL для переадресации</GridHeaderCell>
            </GridHeader>
            <GridBody>
              {records && records.map(record => {
                return (
                  <GridRow key={record.clientUriId}
                    onClick={() => dispatch(setCurrentRecord(record))}
                    onDoubleClick={() => current !== record ? dispatch(setCurrentRecord(record,
                      () => history.push(`/ui/client/${clientId}/client-uri/${record.clientUriId}/view`, state))) : history.push(`/ui/client/${clientId}/client-uri/${record.clientUriId}/view`, state)}
                    selected={record === current}>
                    <GridRowCell label="ID записи">
                      <TextCell>{record.clientUriId}</TextCell>
                    </GridRowCell>
                    <GridRowCell label="ID клиентского приложения">
                      <TextCell>{clientId}</TextCell>
                    </GridRowCell>
                    <GridRowCell label="URL для переадресации">
                      <TextCell>{record.clientUri}</TextCell>
                    </GridRowCell>
                  </GridRow>);
              })}
            </GridBody>
          </GridTable>
          <GridPagingBar rowCount={records?.length} totalRowCount={records?.length} onChange={() => {
            if (clientId) {
              dispatch(searchClientUri(clientId))
            }
          }} />
        </Grid>
      </Content>
    </Page>
  );
}