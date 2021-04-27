import React, { useEffect, useState } from "react";
import { actions as searchActions } from "../state/sessionSearchSlice";
import { actions as crudActions } from "../state/sessionCrudSlice";
import { useDispatch, useSelector } from "react-redux";
import { useHistory } from "react-router-dom";
import { AppState } from "../../app/store/reducer";
import { Session, SessionSearchTemplate } from "../types";
import { TextCell } from "../../app/common/components/cell/TextCell";
import { DateCell } from "../../app/common/components/cell/DateCell";
import { Grid } from "@jfront/ui-core";
import { useTranslation } from "react-i18next";
import { EntityState, SearchState } from "@jfront/core-redux-saga";
import { useQuery } from "../../app/common/useQuery";

const SessionListPage: React.FC = () => {
  const dispatch = useDispatch();
  const history = useHistory();
  const { t } = useTranslation();
  const { ...template } = useQuery();
  const { currentRecord, selectedRecords } = useSelector<AppState, EntityState<Session>>(
    (state) => state.session.crudSlice
  );
  const { records, searchRequest, resultSetSize, isLoading } = useSelector<
    AppState,
    SearchState<SessionSearchTemplate, Session>
  >((state) => state.session.searchSlice);

  return (
    <Grid<Session>
      columns={[
        {
          Header: t("session.sessionId"),
          accessor: "sessionId",
          Cell: ({ value }: any) => <TextCell>{value}</TextCell>,
        },
        {
          Header: t("session.dateIns"),
          accessor: "dateIns",
          Cell: ({ value }: any) => {
            return <DateCell>{new Date(value).toLocaleString()}</DateCell>;
          },
        },
        {
          Header: t("session.operatorLogin"),
          accessor: "operatorLogin",
          Cell: ({ value }: any) => <TextCell>{value}</TextCell>,
        },
        {
          Header: t("session.operatorName"),
          id: "operatorName",
          accessor: (row: Session) => row.operator?.name,
          Cell: ({ value }: any) => <TextCell>{value}</TextCell>,
        },
        {
          Header: t("session.operatorId"),
          id: "operatorId",
          accessor: (row: Session) => row.operator?.value,
          Cell: ({ value }: any) => <TextCell>{value}</TextCell>,
        },
        {
          Header: t("session.redirectUri"),
          accessor: "redirectUri",
          Cell: ({ value }: any) => <TextCell>{value}</TextCell>,
        },
        {
          Header: t("session.clientName"),
          id: "clientName",
          accessor: t("session.clientNameField"),
          Cell: ({ value }: any) => <TextCell>{value}</TextCell>,
        },
        {
          Header: t("session.clientId"),
          id: "clientId",
          accessor: (row: Session) => row.clientId,
          Cell: ({ value }: any) => <TextCell>{value}</TextCell>,
        },
      ]}
      isLoading={isLoading}
      data={React.useMemo(() => records, [records])}
      onSelection={(records) => {
        if (records) {
          if (records.join() !== selectedRecords.join()){
            dispatch(crudActions.setCurrentRecord({} as any));
            dispatch(crudActions.selectRecords({ selectedRecords: records }));
          }
          if (records.length === 1) {
            if (records[0] !== currentRecord) {
              dispatch(crudActions.setCurrentRecord({ currentRecord: records[0] }));
              dispatch(crudActions.selectRecords({ selectedRecords: records }));
            }
          }
        }
      }}
      fetchData={(pageNumber, pageSize, sortConfigs) => {
        const newSearchRequest = {
          template: {
            maxRowCount: 25,
            ...template,
            ...searchRequest?.template,
          },
          listSortConfiguration: sortConfigs,
        };
        dispatch(
          searchActions.search({
            searchTemplate: newSearchRequest,
            pageNumber,
            pageSize,
          })
        );
      }}
      manualPaging
      manualSort
      totalRowCount={resultSetSize}
      onDoubleClick={(current) =>
        current !== currentRecord
          ? dispatch(
              crudActions.setCurrentRecord({
                currentRecord: current,
                callback: () =>
                  history.push(`/session/${current?.sessionId}/view`),
              })
            )
          : history.push(`/session/${current?.sessionId}/view`)
      }
    />
  );
};

export default SessionListPage;
