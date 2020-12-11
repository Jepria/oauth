export interface Client {
  clientId: string;
  clientName?: string;
  clientNameEn?: string;
  clientSecret?: string;
  applicationType?: string;
  grantTypes?: Array<string>;
  scope?: Array<Option>;
}

export interface ColumnSortConfiguration {
  columnName: string;
  sortOrder: string;
}

export interface SearchRequest<Type> {
  template: Type;
  listSortConfiguration?: Array<ColumnSortConfiguration>;
}

export interface ClientSearchTemplate {
  clientId?: string;
  clientName?: string;
  clientNameEn?: string;
  maxRowCount: number;
}

export interface ClientState {
  isLoading: boolean;
  recordsLoading: boolean;
  rolesLoading: boolean;
  current?: Client;
  selectedRecords: Array<Client>
  records: Array<Client>;
  searchId?: string;
  resultSetSize?: number;
  searchRequest?: SearchRequest<ClientSearchTemplate>;
  message?: string;
  error?: Error;
  roles?: Array<Option>
}

export interface Option {
  name: string;
  value: string;
}