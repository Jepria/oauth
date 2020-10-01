export interface ClientUri {
  clientUriId: number;
  clientUri: string;
}

export interface ClientUriCreateDto {
  clientUri: string;
}

export interface ClientUriState {
  isLoading: boolean;
  current?: ClientUri;
  records: Array<ClientUri>;
  selectedRecords: Array<ClientUri>;
  message?: string;
  error?: Error;
}