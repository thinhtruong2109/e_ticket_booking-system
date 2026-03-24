import axios from 'axios';
import { getApiBaseUrl, withAppBase } from '../utils/url';

const API_BASE_URL = getApiBaseUrl();

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // Tự động gửi cookie
});

// Response interceptor: unwrap ApiResponse & handle token refresh
apiClient.interceptors.response.use(
  (response) => {
    // Unwrap backend ApiResponse wrapper { success, message, data } → keep only data
    if (
      response.data &&
      typeof response.data === 'object' &&
      'success' in response.data &&
      'data' in response.data
    ) {
      response.data = response.data.data;
    }
    return response;
  },
  async (error) => {
    const originalRequest = error.config;
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      try {
        // Gọi refresh, server tự đọc refreshToken cookie và set lại accessToken cookie
        await axios.post(`${API_BASE_URL}/api/auth/refresh-token`, {}, { withCredentials: true });
        return apiClient(originalRequest);
      } catch {
        window.location.href = withAppBase('login');
      }
    }
    return Promise.reject(error);
  }
);

export default apiClient;
