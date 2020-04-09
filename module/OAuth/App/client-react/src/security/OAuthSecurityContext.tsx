import React, { createContext, useEffect, useReducer, useContext } from 'react';
import OAuth from './OAuth';
import * as Crypto from './Crypto';

export interface ISecurityProviderProps {
  clientId: string,
  redirectUri: string,
  oauthContextPath: string
}

interface ISecurityContext {
  accessToken?: string;
  authorize(): void
}

type TokenResponse = {
  token_type: string;
  expires_in: bigint;
  access_token: string;
  refresh_token?: string;
}

const OAuthSecurityContext = createContext<ISecurityContext | null>(null);

const useOAuth = () => {
  const context = useContext(OAuthSecurityContext) as ISecurityContext;

  if (!context.accessToken ){
    context.authorize();
  }

  return context;
}

const withOAuth = (WrappedComponent: React.ComponentType)  => {
  return class extends React.Component {
    static contextType = OAuthSecurityContext;

    componentDidMount() {
      if (!this.context.accessToken) {
        this.context.authorize();
      }
    }

    render() {
      return (
        <WrappedComponent {...this.props}/>
      );
    }
  }
}

const OAuthProtectedFragment: React.FC = ({children}) => {

  const context = useContext(OAuthSecurityContext) as ISecurityContext;

  if (!context.accessToken ){
    context.authorize();
  }

  return (
    <React.Fragment>
      {children}
    </React.Fragment>
  );
}


type OAuthState = {
  isLoading: boolean;
  error?: Error;
  accessToken?: string;
}

type Action =
  | { type: "pending" }
  | { type: "success" }
  | { type: "tokenResponse", result: TokenResponse }
  | { type: "failure", error: Error }

const OAuthStateReducer = (state: OAuthState, action: Action) => {
  switch (action.type) {
    case 'pending':
      return { isLoading: true };
    case 'success':
      return { isLoading: false };
    case 'tokenResponse':
      return { isLoading: false, accessToken: action.result.access_token, refreshToken: action.result.refresh_token };
    case 'failure':
      return { isLoading: false, error: action.error };
  }
}

const OAuthSecurityProvider: React.FC<ISecurityProviderProps> = ({ clientId, oauthContextPath, redirectUri, children }) => {

  const [{ isLoading, accessToken, error }, dispatch] = useReducer(OAuthStateReducer, { isLoading: false })

  let isOAuthRoute = window.location.pathname.endsWith('/oauth') || window.location.pathname.endsWith("/oauth/");

  const oauth = new OAuth(clientId, redirectUri, oauthContextPath + "/authorize", oauthContextPath + "/token");

  const getToken = (authCode: string): Promise<TokenResponse> => {
    console.log(`getToken-${authCode}`);
    return oauth.getTokenWithAuthCode(authCode) as Promise<TokenResponse>;
  }

  const authorize = () => {
    console.log(`getAuthorizationUrl`);
    dispatch({ type: 'pending' });
    oauth.authorize('code', Crypto.toBase64Url(`path=${window.location.pathname + window.location.search}&otp=${Crypto.getRandomString()}`))
      .then(result => {
        window.location.replace(result);
      }).catch(error => {
        dispatch({type: 'failure', error})
      });
  }

  useEffect(() => {
    if (isOAuthRoute) {
      console.log(`getTokenFromProvider`);
      let queryParams = new URLSearchParams(window.location.search);
      let authCode = queryParams.get('code');
      let state = queryParams.get('state');
      if (window.sessionStorage.getItem("state") === state) {
        getToken(authCode as string).then(result => {
          if (result.token_type === 'Bearer') {
            let stateParams = new URLSearchParams(Crypto.fromBase64Url(state));
            window.history.replaceState(window.history.state, '', stateParams.get("path"));
            dispatch({type: 'tokenResponse', result})
          } else {
            dispatch({type: 'failure', error: new Error("Unsupported token type")});
          }
        });
      } else {
        dispatch({type: 'failure', error: new Error("State is malformed")});
      }
    }
  });

  if (isLoading || isOAuthRoute) {
    return (<span>Loading</span>);
  } if (error) {
    return (<span>{error.message}</span>);
  } else {
    return (
      <OAuthSecurityContext.Provider value={{ accessToken, authorize }}>
        {children}
      </OAuthSecurityContext.Provider>
    );
  }
}

export { OAuthSecurityProvider, OAuthSecurityContext, OAuthProtectedFragment, useOAuth, withOAuth }