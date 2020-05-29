import React, { useEffect } from 'react';
import { Page, Content } from '../../../../components/Layout';
import { setCurrentRecord, searchClientUri } from '../state/redux/actions';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory, useParams, useLocation } from 'react-router-dom';
import { AppState } from '../../../store';
import { ClientUriState } from '../types';
import { HistoryState } from '../../../../components/HistoryState';
import { TextCell } from '../../../../components/cell/TextCell';
import { JepGrid, JepGridTable, JepGridHeader, JepGridHeaderCell, JepGridBody, JepGridRow, JepGridRowCell, JepGridPagingBar } from 'jfront-components';

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
        <JepGrid>
          <JepGridTable>
            <JepGridHeader>
              <JepGridHeaderCell>ID клиентского приложения</JepGridHeaderCell>
              <JepGridHeaderCell>ID записи</JepGridHeaderCell>
              <JepGridHeaderCell>URL для переадресации</JepGridHeaderCell>
            </JepGridHeader>
            <JepGridBody>
              {records && records.map(record => {
                return (
                  <JepGridRow key={record.clientUriId}
                    onClick={() => dispatch(setCurrentRecord(record))}
                    onDoubleClick={() => current !== record ? dispatch(setCurrentRecord(record,
                      () => history.push(`/ui/client/${clientId}/client-uri/${record.clientUriId}/view`, state))) : history.push(`/ui/client/${clientId}/client-uri/${record.clientUriId}/view`, state)}
                    selected={record === current}>
                    <JepGridRowCell label="ID записи">
                      <TextCell>{record.clientUriId}</TextCell>
                    </JepGridRowCell>
                    <JepGridRowCell label="ID клиентского приложения">
                      <TextCell>{clientId}</TextCell>
                    </JepGridRowCell>
                    <JepGridRowCell label="URL для переадресации">
                      <TextCell>{record.clientUri}</TextCell>
                    </JepGridRowCell>
                  </JepGridRow>);
              })}
            </JepGridBody>
          </JepGridTable>
          <JepGridPagingBar rowCount={records?.length} totalRowCount={records?.length} onChange={() => {
            if (clientId) {
              dispatch(searchClientUri(clientId))
            }
          }} />
        </JepGrid>
      </Content>
    </Page>
  );
}