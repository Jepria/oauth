export const BASE_PATH: string = `${window.location.origin}/${window.location.pathname.length === 1 ? window.location.pathname : window.location.pathname.split('/')[0]}`
export const API_PATH: string = `${BASE_PATH}/api`

export const TEST_BASE_PATH: string = 'http://localhost:8082/oauth'
export const TEST_API_PATH: string = 'http://localhost:8082/oauth/api'