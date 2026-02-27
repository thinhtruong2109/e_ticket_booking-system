import apiClient from './apiClient';

const userApi = {
  getProfile: () => apiClient.get('/api/users/me'),
  updateProfile: (data) => apiClient.put('/api/users/me', data),
  changePassword: (data) => apiClient.put('/api/users/me/password', data),

  // Admin
  getAllUsers: () => apiClient.get('/api/users'),
  getUsersByRole: (role) => apiClient.get(`/api/users/role/${role}`),
  banUser: (userId) => apiClient.put(`/api/users/${userId}/ban`),
  unbanUser: (userId) => apiClient.put(`/api/users/${userId}/unban`),
  changeUserRole: (userId, role) => apiClient.put(`/api/users/${userId}/role?role=${role}`),
};

export default userApi;
