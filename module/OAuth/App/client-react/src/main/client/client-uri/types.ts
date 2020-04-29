export interface ClientUri {
  clientUriId?: number;
  clientUri: string;
}

export interface ClientUriState {
  isLoading: boolean;
  current?: ClientUri;
  records?: Array<ClientUri>;
  message?: string;
  error?: Error;
}