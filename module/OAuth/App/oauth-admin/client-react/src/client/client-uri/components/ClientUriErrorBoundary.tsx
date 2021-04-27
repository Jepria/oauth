import { connect, useDispatch, useSelector } from "react-redux";
import { ErrorNotification, ErrorNotificationProps } from "@jfront/ui-core";
import { AppState } from "../../../app/store/reducer";
import { ClientUri, ClientUriSearchState } from "../types";
import { EntityState } from "@jfront/core-redux-saga";
import { actions as crudActions } from "../state/clientUriCrudSlice";
import { actions as searchActions } from "../state/clientUriSearchSlice";

const mapStateToProps = (state: AppState, props: ErrorNotificationProps) => {
  return {
    error: state.clientUri.crudSlice.error || state.clientUri.searchSlice.error,
    ...props,
  };
};

const ErrorBoundary = (props: ErrorNotificationProps) => {
  const dispatch = useDispatch();
  const { error: searchError } = useSelector<AppState, ClientUriSearchState>(
    (state) => state.clientUri.searchSlice
  );
  const { error: crudError } = useSelector<AppState, EntityState<ClientUri>>(
    (state) => state.clientUri.crudSlice
  );
  const clearErrors = () => {
    if (searchError) {
      dispatch(searchActions.failure({ error: undefined }));
    }
    if (crudError) {
      dispatch(crudActions.failure({ error: undefined }));
    }
  };

  return <ErrorNotification {...props} onClose={clearErrors} />;
};

export default connect(mapStateToProps)(ErrorBoundary);
