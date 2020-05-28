import axios from 'axios';
import * as Crypto from './Crypto';

export const GrantType: { [id: string]: string; } = {
  'authorization_code': 'Authorization code',
  'implicit': 'Implicit',
  'password': 'User credentials',
  'client_credentials': 'Client credentials',
  'refresh_token': 'Refresh token'
}

export const ApplicationType: { [id: string]: string; } = {
  'native': 'Native',
  'web': 'Web application',
  'browser': 'Browser application',
  'service': 'Service',
}

export const ApplicationGrantType: { [id: string]: Array<string>; } = {
  'native': ['authorization_code', 'implicit', 'password', 'refresh_token'],
  'web': ['authorization_code', 'implicit', 'password', 'client_credentials', 'refresh_token'],
  'browser': ['authorization_code', 'implicit', 'password'],
  'service': ['client_credentials', 'refresh_token']
}

export type ApplicationState = {
  currentPath: string;
  codeVerifier?: string;
  expiresIn?: Date;
}

export type TokenResponse = {
  token_type: string;
  expires_in: bigint;
  access_token: string;
  refresh_token?: string;
}

export class OAuth {

  private _clientId: string;
  private _redirectUri: string;
  private _authorizeUrl: string;
  private _tokenUrl: string;

  constructor(clientId: string, redirectUri: string, authorizeUrl: string, tokenUrl: string) {
    this._clientId = clientId;
    this._redirectUri = Crypto.toBase64Url(redirectUri);
    this._authorizeUrl = authorizeUrl;
    this._tokenUrl = tokenUrl;
  }

  get сlientId(): string {
    return this._clientId;
  }

  get redirectUri(): string {
    return this._redirectUri;
  }

  get authorizeUrl(): string {
    return this._authorizeUrl;
  }

  get tokenUrl(): string {
    return this._tokenUrl;
  }

  authorize = (responseType: string, currentPath: string): Promise<string> => {
    if (this.authorizeUrl) {
      let authRequest = new AuthorizationRequest(this._clientId, this._redirectUri, this._authorizeUrl, responseType, currentPath);
      return authRequest.authorize();
    } else {
      throw new Error("authorizeUrl must me not null");
    }
  }

  getTokenWithAuthCode = (authorizationCode: string, nonce: string): Promise<TokenResponse> => {
    const tokenRequest = new TokenRequest(this._clientId, this._redirectUri, this._tokenUrl, nonce);
    return tokenRequest.withAuthorizationCode(authorizationCode);
  }

  refreshToken = (refreshToken: string, nonce: string): Promise<TokenResponse> => {
    const tokenRequest = new TokenRequest(this._clientId, this._redirectUri, this._tokenUrl);
    return tokenRequest.withRefreshToken(refreshToken);
  }

  getTokenWithUserCredentials = (username: string, password: string, nonce: string): Promise<TokenResponse> => {
    const tokenRequest = new TokenRequest(this._clientId, this._redirectUri, this._tokenUrl);
    return tokenRequest.withUserCredentials(username, password);
  }
}

class AuthorizationRequest {
  private _clientId: string;
  private _redirectUri: string;
  private _authorizeUrl: string;
  private _responseType: string;
  private _state: ApplicationState;
  private codeVerifier: string | undefined;

  constructor(clientId: string, redirectUri: string, authorizeUrl: string, responseType: string, currentPath: string) {
    this._clientId = clientId;
    this._redirectUri = redirectUri;
    this._authorizeUrl = authorizeUrl;
    this._responseType = responseType;
    this._state = {currentPath: currentPath};
  }

  private authorizePKCE = (nonce: string): Promise<string> => {
    if (window) {
      //generate code_verifier && code challenge
      this.codeVerifier = Crypto.getRandomString();
      this._state.codeVerifier = this.codeVerifier;
      window.sessionStorage.setItem(nonce, JSON.stringify(this._state));
      return new Promise<string>((resolve, reject) => {
        try {
          Crypto.sha256(this.codeVerifier).then(result => {
            //build request url
            resolve(`${this._authorizeUrl}?response_type=${this._responseType}&client_id=${this._clientId}&redirect_uri=${this._redirectUri}&code_challenge=${result}&state=${nonce}`);
          });
        } catch (error) {
          reject(error);
        }
      });
    } else {
      throw new Error("Window Object is not available, authorize flow is not permitted, request token directly");
    }
  }

  private authorizeImplicit(nonce: string): Promise<string> {
    if (window) {
      window.sessionStorage.setItem(nonce, JSON.stringify(this._state));
      return new Promise<string>((resolve, reject) => {
        //build request url
        resolve(`${this._authorizeUrl}?response_type=${this._responseType}&client_id=${this._clientId}&redirect_uri=${this._redirectUri}&state=${nonce}`);
      });
    } else {
      throw new Error("Window Object is not available, authorize flow is not permitted, request token directly");
    }
  }

  authorize = (): Promise<string> => {
    let nonce = Crypto.getRandomString();
    let date = new Date();
    date.setMinutes(date.getMinutes() + 5);
    this._state.expiresIn = date;
    if (this._responseType === "code") {
      return this.authorizePKCE(nonce);
    } else {
      return this.authorizeImplicit(nonce);
    }
  }
}

class TokenRequest {
  private _clientId: string;
  private _redirectUri: string;
  private _tokenUrl: string;
  private _nonce: string | undefined;

  constructor(clientId: string, redirectUri: string, tokenUrl: string, nonce?: string) {
    this._clientId = clientId;
    this._redirectUri = redirectUri;
    this._tokenUrl = tokenUrl;
    this._nonce = nonce;
  }


  withAuthorizationCode(authorizationCode: string): Promise<TokenResponse> {
    if (!this._nonce) {
      throw new Error("nonce is undefined");
    }
    let stringState = window.sessionStorage.getItem(this._nonce);
    if (!stringState) {
      throw new Error("state not found");
    }
    let state: ApplicationState = JSON.parse(stringState);
    let codeVerifier = state.codeVerifier;
    window.sessionStorage.removeItem(this._nonce);
    return new Promise<TokenResponse>((resolve, reject) => {
      axios.post(
        this._tokenUrl, 
        `grant_type=authorization_code&client_id=${encodeURIComponent(this._clientId)}&redirect_uri=${encodeURIComponent(this._redirectUri)}&code=${encodeURIComponent(authorizationCode)}&code_verifier=${encodeURIComponent(codeVerifier as string)}`,
        {
          headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/x-www-form-urlencoded'
          }
        }
      ).then(response => resolve(response.data)).catch(error => {
        reject(error);
      });
    });
  }  

  withRefreshToken(refreshToken: string): Promise<TokenResponse> {
    return new Promise<TokenResponse>((resolve, reject) => {
      axios.post(
        this._tokenUrl, 
        `grant_type=refresh_token&client_id=${encodeURIComponent(this._clientId)}&refresh_token=${encodeURIComponent(refreshToken)}`,
        {
          headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/x-www-form-urlencoded'
          }
        }
      ).then(response => resolve(response.data)).catch(error => {
        reject(error);
      });
    });
  }

  withUserCredentials(username:string, password: string): Promise<TokenResponse> {
    return new Promise<TokenResponse>((resolve, reject) => {
      axios.post(
        this._tokenUrl, 
        `grant_type=password&client_id=${encodeURIComponent(this._clientId)}&username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`,
        {
          headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/x-www-form-urlencoded'
          }
        }
      ).then(response => resolve(response.data)).catch(error => {
        reject(error);
      });
    });
  }
}