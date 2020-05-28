const TEST_API_PATH: string = 'http://localhost:8082/oauth'

export const API_PATH: string = `${process.env.NODE_ENV === 'development' ? TEST_API_PATH : process.env.API_PATH ? process.env.API_PATH : process.env.PUBLIC_URL}/api`

