import React from 'react'
import { AppState } from '../redux/store';
import { connect } from 'react-redux';
import { ACCESS_DENIED, AUTHORIZATION_FAILED, BadRequest, BAD_REQUEST, NetworkError, NotFound, NOT_FOUND, ServerError } from '../rest/types';
import { ErrorDialog } from '../components/dialog/ErrorDialog';


export interface ErrorNotificationProps {
  error?: NetworkError | Error | string | undefined
  children?: React.ReactNode
}

const mapStateToProps = (state: AppState, props: ErrorNotificationProps) => ({
  error: state.client.error || state.session.error || state.key.error || state.clientUri.error,
  ...props
})

interface ErrorBoundaryState {
  error: NetworkError | Error | string | undefined
}

class ErrorBoundary extends React.Component<ErrorNotificationProps, ErrorBoundaryState> {
  constructor(props: ErrorNotificationProps) {
    super(props);
    this.state = { error: props.error }
  }

  static getDerivedStateFromError(error: any) {
    return { error: error }
  }

  render() {
    const { error } = this.state;
    if (error) {
      if (typeof error === "string") {
        return (
          <>
            <ErrorDialog
              errorMessage={error}
              onClose={() => this.setState({ error: undefined })} />
            {this.props.children}
          </>
        )
      } else if ((error as NetworkError).type) {
        switch ((error as NetworkError).type) {
          case BAD_REQUEST: {
            return (
              <>
                <ErrorDialog
                  errorCode={BAD_REQUEST}
                  errorMessage={JSON.stringify((error as BadRequest).constraintViolations)}
                  onClose={() => this.setState({ error: undefined })} />
                {this.props.children}
              </>
            )
          }
          case AUTHORIZATION_FAILED: {
            return (
              <>
                <ErrorDialog
                  errorCode={AUTHORIZATION_FAILED}
                  errorMessage="Authorization failed"
                  onClose={() => this.setState({ error: undefined })} />
                {this.props.children}
              </>
            )
          }
          case ACCESS_DENIED: {
            return (
              <>
                <ErrorDialog
                  errorCode={ACCESS_DENIED}
                  errorMessage="Недостаточно прав доступа"
                  onClose={() => this.setState({ error: undefined })} />
                {this.props.children}
              </>
            )
          }
          case NOT_FOUND: {
            return (
              <>
                <ErrorDialog
                  errorCode={NOT_FOUND}
                  errorMessage={(error as NotFound).url + " URL не найден"}
                  onClose={() => this.setState({ error: undefined })} />
                {this.props.children}
              </>
            )
          }
          default: {
            return (
              <>
                <ErrorDialog
                  errorId={(error as ServerError).errorId}
                  errorCode={(error as ServerError).errorCode}
                  errorMessage={(error as ServerError).errorMessage}
                  onClose={() => this.setState({ error: undefined })} />
                {this.props.children}
              </>
            )
          }
        }
      } else {
        return (
          <>
            <ErrorDialog
              errorDescription={(error as Error).message}
              onClose={() => this.setState({ error: undefined })} />
          </>
        )
      }
    } else {
      return this.props.children
    }
  }
}

export const ErrorNotification = connect(mapStateToProps)(ErrorBoundary) 