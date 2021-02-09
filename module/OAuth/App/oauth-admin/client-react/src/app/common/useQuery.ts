import queryString from 'query-string'

export const useQuery = <T extends object = any>() => {
  return queryString.parse(window.location.search) as T;
}