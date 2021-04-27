import { connect, useDispatch, useSelector } from "react-redux";
import { ErrorNotification, ErrorNotificationProps } from "@jfront/ui-core";
import { AppState } from "../../app/store/reducer";
import { Operator, Session, SessionSearchTemplate } from "../types";
import { EntityState, OptionState, SearchState } from "@jfront/core-redux-saga";
import { actions as crudActions } from "../state/sessionCrudSlice";
import { actions as searchActions } from "../state/sessionSearchSlice";
import { actions as operatorActions } from "../state/sessionOperatorSlice";
import { actions as clientActions } from "../state/sessionClientSlice";
import { Client } from "../../client/types";

const mapStateToProps = (state: AppState, props: ErrorNotificationProps) => {
  return {
    error:
      state.session.crudSlice.error ||
      state.session.searchSlice.error ||
      state.session.operatorSlice.error||
      state.session.clientSlice.error,
    ...props,
  };
};

const ErrorBoundary = (props: ErrorNotificationProps) => {
  const dispatch = useDispatch();
  const { error: searchError } = useSelector<
    AppState,
    SearchState<SessionSearchTemplate, Session>
  >((state) => state.session.searchSlice);
  const { error: crudError } = useSelector<AppState, EntityState<Session>>(
    (state) => state.session.crudSlice
  );
  const { error: operatorError } = useSelector<AppState, OptionState<Operator>>(
    (state) => state.session.operatorSlice
  );
  const { error: clientError } = useSelector<AppState, OptionState<Client>>(
    (state) => state.session.clientSlice
  );
  const clearErrors = () => {
    if (searchError) {
      dispatch(searchActions.failure({ error: undefined }));
    }
    if (crudError) {
      dispatch(crudActions.failure({ error: undefined }));
    }
    if (clientError) {
      dispatch(clientActions.getOptionsFailure({ error: undefined }));
    }
    if (operatorError) {
      dispatch(operatorActions.getOptionsFailure({ error: undefined }));
    }
  };

  return <ErrorNotification {...props} onClose={clearErrors} />;
};

export default connect(mapStateToProps)(ErrorBoundary);
