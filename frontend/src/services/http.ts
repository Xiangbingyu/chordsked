import axios from 'axios'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 8000,
  withCredentials: true,
})

http.interceptors.response.use(
  (response) => response,
  (error) => {
    const requestUrl = String(error.config?.url || '')
    const isLoginRequest = requestUrl.includes('/login')

    if (error.response?.status === 401 && !isLoginRequest) {
      window.location.href = '/'
    }

    return Promise.reject(error)
  }
)

export default http
