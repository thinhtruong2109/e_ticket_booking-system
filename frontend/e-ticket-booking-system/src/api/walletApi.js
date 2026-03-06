import apiClient from './apiClient';

const walletApi = {
  // Organizer
  getMyWallet: () => apiClient.get('/api/organizer/wallet'),
  updateBankInfo: (data) => apiClient.put('/api/organizer/wallet/bank-info', data),
  withdraw: (data) => apiClient.post('/api/organizer/wallet/withdraw', data),
  getMyTransactions: (params) => apiClient.get('/api/organizer/wallet/transactions', { params }),

  // Admin
  adminGetAll: () => apiClient.get('/api/organizer/wallet/admin/all'),
  adminGetByUserId: (userId) => apiClient.get(`/api/organizer/wallet/admin/user/${userId}`),
  adminGetTransactionsByUserId: (userId) =>
    apiClient.get(`/api/organizer/wallet/admin/user/${userId}/transactions`),
};

export default walletApi;
