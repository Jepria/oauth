export interface Client {
  clientId?: string;
  clientName: string;
  clientNameEn?: string;
  clientSecret?: string;
  applicationType: string;
  grantTypes: Array<string>;
}

export interface ColumnSortConfiguration {
  columnName: string;
  sortOrder: string;
}

export interface SearchRequest<Type> {
  template: Type;
  listSortConfiguration?: ColumnSortConfiguration;
}

export interface ClientSearchTemplate {
  clientId?: string;
  clientName?: string;
  clientNameEn?: string;
  maxRowCount?: number;
}

export interface ClientState {
  isLoading: boolean;
  current?: Client;
  records?: Array<Client>;
  searchId?: string;
  resultSetSize?: number;
  searchRequest?: SearchRequest<ClientSearchTemplate>;
  message?: string;
  error?: Error;
}