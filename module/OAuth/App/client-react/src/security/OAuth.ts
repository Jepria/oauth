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

  authorize = (responseType: string, state?: string): Promise<string> => {
    if (this.authorizeUrl) {
      if (!state) {
        state = Crypto.getRandomString();
      }
      let authRequest = new AuthorizationRequest(this._clientId, this._redirectUri, this._authorizeUrl, responseType, state);
      return authRequest.authorize();
    } else {
      throw new Error("authorizeUrl must me not null");
    }
  }

  getTokenWithAuthCode = (authorizationCode: string): Promise<Object> => {
    const tokenRequest = new TokenRequest(this._clientId, this._redirectUri, this._tokenUrl);
    return tokenRequest.withAuthorizationCode(authorizationCode);
  }

  refreshToken = (refreshToken: string): Promise<Object> => {
    const tokenRequest = new TokenRequest(this._clientId, this._redirectUri, this._tokenUrl);
    return tokenRequest.withRefreshToken(refreshToken);
  }

  getTokenWithUserCredentials = (username: string, password: string): Promise<Object> => {
    const tokenRequest = new TokenRequest(this._clientId, this._redirectUri, this._tokenUrl);
    return tokenRequest.withUserCredentials(username, password);
  }
}

class AuthorizationRequest {
  private _clientId: string;
  private _redirectUri: string;
  private _authorizeUrl: string;
  private _responseType: string;
  private _state: string;
  private codeVerifier: string | undefined;

  constructor(clientId: string, redirectUri: string, authorizeUrl: string, responseType: string, state: string) {
    this._clientId = clientId;
    this._redirectUri = redirectUri;
    this._authorizeUrl = authorizeUrl;
    this._responseType = responseType;
    this._state = state;
  }

  private authorizePKCE = (): Promise<string> => {
    if (window) {
      //generate code_verifier && code challenge
      this.codeVerifier = Crypto.getRandomString();
      window.sessionStorage.setItem("codeVerifier", this.codeVerifier);
      return new Promise<string>((resolve, reject) => {
        try {
          Crypto.sha256(this.codeVerifier).then(result => {
            window.sessionStorage.setItem("state", this._state);
            //build request url
            resolve(`${this._authorizeUrl}?response_type=${this._responseType}&client_id=${this._clientId}&redirect_uri=${this._redirectUri}&code_challenge=${result}&state=${this._state}`);
          });
        } catch (error) {
          reject(error);
        }
      });
    } else {
      throw new Error("Window Object is not available, authorize flow is not permitted, request token directly");
    }
  }

  private authorizeImplicit(): Promise<string> {
    if (window) {
      return new Promise<string>((resolve, reject) => {
        //build request url
        resolve(`${this._authorizeUrl}?response_type=${this._responseType}&client_id=${this._clientId}&redirect_uri=${this._redirectUri}&state=${this._state}`);
      });
    } else {
      throw new Error("Window Object is not available, authorize flow is not permitted, request token directly");
    }
  }

  authorize = (): Promise<string> => {
    if (this._responseType === "code") {
      return this.authorizePKCE();
    } else {
      return this.authorizeImplicit();
    }
  }
}

class TokenRequest {
  private _clientId: string;
  private _redirectUri: string;
  private _tokenUrl: string;

  constructor(clientId: string, redirectUri: string, tokenUrl: string) {
    this._clientId = clientId;
    this._redirectUri = redirectUri;
    this._tokenUrl = tokenUrl;
  }


  withAuthorizationCode(authorizationCode: string): Promise<Object> {
    let codeVerifier = window.sessionStorage.getItem("codeVerifier");
    return new Promise<Object>((resolve, reject) => {
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

  withRefreshToken(refreshToken: string): Promise<Object> {
    return new Promise<Object>((resolve, reject) => {
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

  withUserCredentials(username:string, password: string): Promise<Object> {
    return new Promise<Object>((resolve, reject) => {
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