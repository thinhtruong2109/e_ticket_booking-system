import apiClient from './apiClient';

const transactionHistoryApi = {
  // User
  getMyTransactions: (params) => apiClient.get('/api/transaction-histories/me', { params }),
  getByBooking: (bookingId) => apiClient.get(`/api/transaction-histories/booking/${bookingId}`),

  // Admin
  adminGetAll: (params) => apiClient.get('/api/transaction-histories/admin', { params }),
  adminGetByUserId: (userId) => apiClient.get(`/api/transaction-histories/admin/user/${userId}`),
};

export default transactionHistoryApi;
