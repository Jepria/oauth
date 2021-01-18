import React from 'react'
import { AppState } from '../../store/reducer';
import { connect } from 'react-redux';
import { ACCESS_DENIED, AUTHORIZATION_FAILED, BadRequest, BAD_REQUEST, NetworkError, NotFound, NOT_FOUND, ServerError } from '../rest/types';
import { ErrorDialog } from './dialog/error-dialog/ErrorDialog';
import { Translation } from 'react-i18next';


export interface ErrorNotificationProps {
  error?: NetworkError | Error | string | undefined
  children?: React.ReactNode
}

const mapStateToProps = (state: AppState, props: ErrorNotificationProps) => ({
  error: state.client.crudSlice.error 
  || state.session.searchSlice.error 
  || state.session.crudSlice.error 
  || state.session.searchSlice.error 
  || state.key.error 
  || state.clientUri.crudSlice.error
  || state.clientUri.searchSlice.error,
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
          <Translation>
            {
              (t) =>
                <>
                  <ErrorDialog
                    header={t('errodDialogHeader')}
                    errorMessage={error}
                    onClose={() => this.setState({ error: undefined })} />
                  {this.props.children}
                </>
            }
          </Translation>
        )
      } else if ((error as NetworkError).type) {
        switch ((error as NetworkError).type) {
          case BAD_REQUEST: {
            return (
              <Translation>
                {
                  (t) =>
                    <>
                      <ErrorDialog
                        header={t('errodDialogHeader')}
                        errorCode={BAD_REQUEST}
                        errorMessage={JSON.stringify((error as BadRequest).constraintViolations)}
                        onClose={() => this.setState({ error: undefined })} />
                      {this.props.children}
                    </>
                }
              </Translation>
            )
          }
          case AUTHORIZATION_FAILED: {
            return (
              <Translation>
                {
                  (t) =>
                    <>
                      <ErrorDialog
                        header={t('errodDialogHeader')}
                        errorCode={AUTHORIZATION_FAILED}
                        errorMessage="Authorization failed"
                        onClose={() => this.setState({ error: undefined })} />
                      {this.props.children}
                    </>
                }
              </Translation>
            )
          }
          case ACCESS_DENIED: {
            return (
              <Translation>
                {
                  (t) =>
                    <>
                      <ErrorDialog
                        header={t('errodDialogHeader')}
                        errorCode={ACCESS_DENIED}
                        errorMessage="Недостаточно прав доступа"
                        onClose={() => this.setState({ error: undefined })} />
                      {this.props.children}
                    </>
                }
              </Translation>
            )
          }
          case NOT_FOUND: {
            return (
              <Translation>
                {
                  (t) =>
                    <>
                      <ErrorDialog
                        header={t('errodDialogHeader')}
                        errorCode={NOT_FOUND}
                        errorMessage={(error as NotFound).url + " URL не найден"}
                        onClose={() => this.setState({ error: undefined })} />
                      {this.props.children}
                    </>
                }
              </Translation>
            )
          }
          default: {
            return (
              <Translation>
                {
                  (t) =>
                    <>
                      <ErrorDialog
                        header={t('errodDialogHeader')}
                        errorId={(error as ServerError).errorId}
                        errorCode={(error as ServerError).errorCode}
                        errorMessage={(error as ServerError).errorMessage}
                        onClose={() => this.setState({ error: undefined })} />
                      {this.props.children}
                    </>
                }
              </Translation>
            )
          }
        }
      } else {
        return (
          <Translation>
            {
              (t) =>
                <>
                  <ErrorDialog
                    header={t('errodDialogHeader')}
                    errorDescription={(error as Error).message}
                    onClose={() => this.setState({ error: undefined })} />
                </>
            }
          </Translation>
        )
      }
    } else {
      return this.props.children
    }
  }
}

export const ErrorNotification = connect(mapStateToProps)(ErrorBoundary) 