import React, { useEffect } from 'react';
import { actions } from '../state/sessionSlice';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory, useLocation } from 'react-router-dom';
import { AppState } from '../../app/store/reducer';
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
      dispatch(actions.search({
        searchId,
        pageSize: 25,
        page: 1,
        loadingMessage: t('dataLoadingMessage')
      }));
    } else if (!searchId && searchRequest) {
      dispatch(actions.postSearchTemplate({ searchRequest, loadingMessage: t('dataLoadingMessage') }));
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
      onSelection={(records) => {
        if (records) {
          if (records.length === 1) {
            if (records[0] !== current) {
              dispatch(actions.setCurrentRecord({ currentRecord: records[0] }));
              dispatch(actions.selectRecords({ records }));
            }
          } else if (current) {
            dispatch(actions.setCurrentRecord({}));
            dispatch(actions.selectRecords({ records }));
          }
        }
      }}
      onPaging={(pageNumber, pageSize) => {
        if (searchId) {
          dispatch(actions.search({
            searchId,
            pageSize,
            page: pageNumber,
            loadingMessage: t('dataLoadingMessage')
          }));
        }
      }}
      onSort={(sortConfig) => {
        if (searchRequest) {
          const newSearchRequest = {
            ...searchRequest,
            listSortConfiguration: sortConfig
          }
          dispatch(actions.postSearchTemplate({ searchRequest: newSearchRequest, loadingMessage: t('dataLoadingMessage') }));
        } else
          if (pageSize && page) {
            dispatch(actions.postSearchTemplate({
              searchRequest: { template: searchTemplate as unknown as SessionSearchTemplate, listSortConfiguration: sortConfig },
              loadingMessage: t('dataLoadingMessage')
            }));
          } else {
            dispatch(actions.postSearchTemplate({
              searchRequest: {
                template: { maxRowCount: 25 },
                listSortConfiguration: sortConfig
              },
              loadingMessage: t('dataLoadingMessage')
            }));
          }
      }}
      totalRowCount={resultSetSize}
      onDoubleClick={currentRecord => current !== currentRecord ? dispatch(actions.setCurrentRecord({
        currentRecord,
        callback: () => history.push(`/ui/session/${currentRecord?.sessionId}/view`)
      })) : history.push(`/ui/session/${currentRecord?.sessionId}/view`)} />
  );
}

export default SessionListPage;