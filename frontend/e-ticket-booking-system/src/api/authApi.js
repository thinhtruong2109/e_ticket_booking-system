import apiClient from './apiClient';

const authApi = {
  register: (data) => apiClient.post('/api/auth/register', data),
  login: (data) => apiClient.post('/api/auth/login', data),
  verifyEmail: (data) => apiClient.post('/api/auth/verify-email', data),
  resendOtp: (data) => apiClient.post('/api/auth/resend-otp', data),
  refreshToken: (data) => apiClient.post('/api/auth/refresh-token', data),
  logout: () => apiClient.post('/api/auth/logout'),
};

export default authApi;
