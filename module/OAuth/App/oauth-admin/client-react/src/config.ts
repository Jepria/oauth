const TEST_API_PATH: string = 'http://localhost:8080/oauth-admin'

export const API_PATH: string = `${process.env.NODE_ENV === 'development' ? TEST_API_PATH : process.env.API_PATH ? process.env.API_PATH : process.env.PUBLIC_URL}/api`

