import React, { useEffect } from 'react';
import { setCurrentRecord, searchClients, postSearchClientRequest } from '../state/redux/actions';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory } from 'react-router-dom';
import { AppState } from '../../store';
import { ClientState } from '../types';
import { GrantType, ApplicationType } from '../../../security/OAuth';
import { TextCell } from '../../../components/cell/TextCell';
import { JepGrid, JepGridTable, JepGridHeader, JepGridHeaderCell, JepGridBody, JepGridRow, JepGridRowCell, JepGridPagingBar, Page, Content } from 'jfront-components';

export const ClientListPage: React.FC = () => {

  const dispatch = useDispatch();
  const history = useHistory();
  const { records, current, searchId, searchRequest, resultSetSize } = useSelector<AppState, ClientState>(state => state.client);

  useEffect(() => {
    if (searchId && searchRequest) {
      dispatch(searchClients(searchId, 25, 1));
    } else if (!searchId && searchRequest) {
      dispatch(postSearchClientRequest(searchRequest));
    } else {
      dispatch(postSearchClientRequest({template: {maxRowCount: 25}}));
    }
  }, [searchId, searchRequest, dispatch]);

  return (
    <Page>
      <Content>
        <JepGrid>
          <JepGridTable>
            <JepGridHeader>
              <JepGridHeaderCell>ID клиентского приложения</JepGridHeaderCell>
              <JepGridHeaderCell>Секретное слово</JepGridHeaderCell>
              <JepGridHeaderCell>Наименование</JepGridHeaderCell>
              <JepGridHeaderCell>Наименование (англ)</JepGridHeaderCell>
              <JepGridHeaderCell>Тип приложения</JepGridHeaderCell>
              <JepGridHeaderCell>Разрешенные типы авторизации</JepGridHeaderCell>
            </JepGridHeader>
            <JepGridBody>
              {records ? records.map(record => {
                return (
                  <JepGridRow key={record.clientId}
                    onClick={() => dispatch(setCurrentRecord(record))}
                    onDoubleClick={() => current !== record ? dispatch(setCurrentRecord(record,
                      () => history.push(`/ui/client/${record.clientId}/view`))) : history.push(`/ui/client/${record.clientId}/view`)}
                    selected={record === current}>
                    <JepGridRowCell label="ID приложения">
                      <TextCell>{record.clientId}</TextCell>
                    </JepGridRowCell>
                    <JepGridRowCell label="Секретное слово">
                      <TextCell>{record.clientSecret}</TextCell>
                    </JepGridRowCell>
                    <JepGridRowCell label="Наименование">
                      <TextCell>{record.clientName}</TextCell>
                    </JepGridRowCell>
                    <JepGridRowCell label="Наименование (англ.)">
                      <TextCell>{record.clientNameEn}</TextCell>
                    </JepGridRowCell>
                    <JepGridRowCell label="Тип приложения">
                      <TextCell>{ApplicationType[record.applicationType]}</TextCell>
                    </JepGridRowCell>
                    <JepGridRowCell label="Разрешения на авторизацию">
                      <TextCell wrapText>{record.grantTypes.map((grantType) => GrantType[grantType]).join(', ')}</TextCell>
                    </JepGridRowCell>
                  </JepGridRow>);
              }) : null}
            </JepGridBody>
          </JepGridTable>
          <JepGridPagingBar rowCount={records?.length} totalRowCount={resultSetSize} onRefresh={(page, pageSize) => {
            if (searchId) {
              dispatch(searchClients(searchId, pageSize, page))
            }
          }} />
        </JepGrid>
      </Content>
    </Page>
  );
}