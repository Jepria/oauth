import { connect, useDispatch, useSelector } from "react-redux";
import { ErrorNotification, ErrorNotificationProps } from "@jfront/ui-core";
import { AppState } from "../../app/store/reducer";
import { Client, ClientSearchTemplate, Option } from "../types";
import { EntityState, OptionState, SearchState } from "@jfront/core-redux-saga";
import { actions as crudActions } from "../state/clientCrudSlice";
import { actions as searchActions } from "../state/clientSearchSlice";
import { actions as roleActions } from "../state/clientRoleSlice";

const mapStateToProps = (state: AppState, props: ErrorNotificationProps) => {
  return {
    error:
      state.client.crudSlice.error ||
      state.client.searchSlice.error ||
      state.client.roleSlice.error,
    ...props,
  };
};

const ErrorBoundary = (props: ErrorNotificationProps) => {
  const dispatch = useDispatch();
  const { error: searchError } = useSelector<
    AppState,
    SearchState<ClientSearchTemplate, Client>
  >((state) => state.client.searchSlice);
  const { error: crudError } = useSelector<AppState, EntityState<Client>>(
    (state) => state.client.crudSlice
  );
  const { error: roleError } = useSelector<AppState, OptionState<Option>>(
    (state) => state.client.roleSlice
  );
  const clearErrors = () => {
    if (searchError) {
      dispatch(searchActions.failure({ error: undefined }));
    }
    if (crudError) {
      dispatch(crudActions.failure({ error: undefined }));
    }
    if (roleError) {
      dispatch(roleActions.getOptionsFailure({ error: undefined }));
    }
  };

  return <ErrorNotification {...props} onClose={clearErrors} />;
};

export default connect(mapStateToProps)(ErrorBoundary);
