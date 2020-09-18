import React, { useEffect } from 'react';
import { setCurrentRecord, searchSessions, postSearchSessionRequest } from '../state/redux/actions';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory } from 'react-router-dom';
import { AppState } from '../../store';
import { SessionState, Session } from '../types';
import { TextCell } from '../../../components/cell/TextCell';
import { DateCell } from '../../../components/cell/DateCell';
import { Grid } from '@jfront/ui-core';

const SessionListPage: React.FC = () => {

  const dispatch = useDispatch();
  const history = useHistory();
  const { records, current, searchId, searchRequest, resultSetSize } = useSelector<AppState, SessionState>(state => state.session);

  useEffect(() => {
    if (searchId && searchRequest) {
      dispatch(searchSessions(searchId, 25, 1));
    } else if (!searchId && searchRequest) {
      dispatch(postSearchSessionRequest(searchRequest));
    } else {
      dispatch(postSearchSessionRequest({ template: { maxRowCount: 25 } }));
    }
  }, [searchId, searchRequest, dispatch]);

  return (
    <Grid<Session>
      columns={[
        {
          Header: "ID сессии",
          accessor: "sessionId",
          Cell: ({ value }: any) => <TextCell>{value}</TextCell>
        },
        {
          Header: "Дата создания",
          accessor: "dateIns",
          Cell: ({ value }: any) => {
            return <DateCell>{new Date(value).toLocaleString()}</DateCell>
          }
        },
        {
          Header: "Логин оператора",
          accessor: "operatorLogin",
          Cell: ({ value }: any) => <TextCell>{value}</TextCell>
        },
        {
          Header: "Имя оператора",
          id: "operatorName",
          accessor: (row: Session) => row.operator?.name,
          Cell: ({ value }: any) => <TextCell>{value}</TextCell>
        },
        {
          Header: "ID оператора",
          id: "operatorId",
          accessor: (row: Session) => row.operator?.value,
          Cell: ({ value }: any) => <TextCell>{value}</TextCell>
        },
        {
          Header: "URL переадресации",
          accessor: "redirectUri",
          Cell: ({ value }: any) => <TextCell>{value}</TextCell>
        },
        {
          Header: "Наименование приложения",
          id: "clientName",
          accessor: (row: Session) => row.client?.name,
          Cell: ({ value }: any) => <TextCell>{value}</TextCell>
        },
        {
          Header: "ID приложения",
          id: "clientId",
          accessor: (row: Session) => row.client?.value,
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
      onPaging={(pageNumber, pageSize) => {
        if (searchId) {
          dispatch(searchSessions(searchId, pageSize, pageNumber + 1))
        }
      }}
      onSort={(sortConfig) => {
        if (searchRequest) {
          const newSearchRequest = {
            ...searchRequest,
            listSortConfiguration: sortConfig
          }
          dispatch(postSearchSessionRequest(newSearchRequest));
        } else {
          dispatch(postSearchSessionRequest({ template: { maxRowCount: 25 }, listSortConfiguration: sortConfig }));
        }
      }}
      totalRowCount={resultSetSize}
      onDoubleClick={(record) => current !== record ? dispatch(setCurrentRecord(record,
        () => history.push(`/ui/session/${record?.sessionId}/view`))) : history.push(`/ui/session/${record?.sessionId}/view`)} />
  );
}

export default SessionListPage;