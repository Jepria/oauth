import React, { useEffect } from 'react';
import { setCurrentRecord, searchSessions, postSearchSessionRequest, selectRecords } from '../state/actions';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory, useLocation } from 'react-router-dom';
import { AppState } from '../../app/store';
import { SessionState, Session, SessionSearchTemplate } from '../types';
import { TextCell } from '../../app/common/components/cell/TextCell';
import { DateCell } from '../../app/common/components/cell/DateCell';
import { Grid } from '@jfront/ui-core';
import { useTranslation } from 'react-i18next';
import queryString from 'query-string';

const useQuery = () => {
  return queryString.parse(useLocation().search);
}

const SessionListPage: React.FC = () => {

  const dispatch = useDispatch();
  const history = useHistory();
  const { t } = useTranslation();
  const { pageSize, page, ...searchTemplate } = useQuery();
  const { records, current, searchId, searchRequest, resultSetSize, recordsLoading } = useSelector<AppState, SessionState>(state => state.session);

  useEffect(() => {
    if (searchId && searchRequest) {
      dispatch(searchSessions(searchId, 25, 1, t('dataLoadingMessage')));
    } else if (!searchId && searchRequest) {
      dispatch(postSearchSessionRequest(searchRequest, t('dataLoadingMessage')));
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [searchId, searchRequest, dispatch]);

  return (
    <Grid<Session>
      columns={[
        {
          Header: t('session.sessionId'),
          accessor: "sessionId",
          Cell: ({ value }: any) => <TextCell>{value}</TextCell>
        },
        {
          Header: t('session.dateIns'),
          accessor: "dateIns",
          Cell: ({ value }: any) => {
            return <DateCell>{new Date(value).toLocaleString()}</DateCell>
          }
        },
        {
          Header: t('session.operatorLogin'),
          accessor: "operatorLogin",
          Cell: ({ value }: any) => <TextCell>{value}</TextCell>
        },
        {
          Header: t('session.operatorName'),
          id: "operatorName",
          accessor: (row: Session) => row.operator?.name,
          Cell: ({ value }: any) => <TextCell>{value}</TextCell>
        },
        {
          Header: t('session.operatorId'),
          id: "operatorId",
          accessor: (row: Session) => row.operator?.value,
          Cell: ({ value }: any) => <TextCell>{value}</TextCell>
        },
        {
          Header: t('session.redirectUri'),
          accessor: "redirectUri",
          Cell: ({ value }: any) => <TextCell>{value}</TextCell>
        },
        {
          Header: t('session.clientName'),
          id: "clientName",
          accessor: (row: Session) => row.client?.name,
          Cell: ({ value }: any) => <TextCell>{value}</TextCell>
        },
        {
          Header: t('session.clientId'),
          id: "clientId",
          accessor: (row: Session) => row.client?.value,
          Cell: ({ value }: any) => <TextCell>{value}</TextCell>
        },
      ]}
      isLoading={recordsLoading}
      data={React.useMemo(() => records, [records])}
      onSelection={(selected) => {
        if (selected) {
          if (selected.length === 1) {
            if (selected[0] !== current) {
              dispatch(setCurrentRecord(selected[0]))
              dispatch(selectRecords(selected))
            }
          } else if (current) {
            dispatch(setCurrentRecord(undefined))
            dispatch(selectRecords(selected))
          }
        }
      }}
      onPaging={(pageNumber, pageSize) => {
        if (searchId) {
          dispatch(searchSessions(searchId, pageSize, pageNumber, t('dataLoadingMessage')))
        }
      }}
      onSort={(sortConfig) => {
        if (searchRequest) {
          const newSearchRequest = {
            ...searchRequest,
            listSortConfiguration: sortConfig
          }
          dispatch(postSearchSessionRequest(newSearchRequest, t('dataLoadingMessage')));
        } else 
        if (pageSize && page) {
          dispatch(postSearchSessionRequest({ template: searchTemplate as unknown as SessionSearchTemplate, listSortConfiguration: sortConfig }, t("dataLoadingMessage")));
        } else {
          dispatch(postSearchSessionRequest({ template: { maxRowCount: 25 }, listSortConfiguration: sortConfig }, t("dataLoadingMessage")));
        }
      }}
      totalRowCount={resultSetSize}
      onDoubleClick={(record) => current !== record ? dispatch(setCurrentRecord(record,
        () => history.push(`/ui/session/${record?.sessionId}/view`))) : history.push(`/ui/session/${record?.sessionId}/view`)} />
  );
}

export default SessionListPage;