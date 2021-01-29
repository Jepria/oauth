import { AppState } from '../../store/reducer';
import { connect } from 'react-redux';
import { ErrorNotification, ErrorNotificationProps } from '@jfront/ui-core'

const mapStateToProps = (state: AppState, props: ErrorNotificationProps) => ({
  error: state.client.crudSlice.error 
  || state.client.searchSlice.error 
  || state.client.roleSlice.error 
  || state.session.crudSlice.error 
  || state.session.searchSlice.error 
  || state.session.clientSlice.error 
  || state.session.operatorSlice.error 
  || state.key.error 
  || state.clientUri.crudSlice.error
  || state.clientUri.searchSlice.error,
  ...props
})

export default connect(mapStateToProps)(ErrorNotification) 