import apiClient from './apiClient';

const ticketListingApi = {
  getAll: () => apiClient.get('/api/ticket-listings'),
  getById: (id) => apiClient.get(`/api/ticket-listings/${id}`),
  getMyListings: () => apiClient.get('/api/ticket-listings/my-listings'),
  create: (data) => apiClient.post('/api/ticket-listings', data),
  cancel: (id) => apiClient.delete(`/api/ticket-listings/${id}`),

  // Exchanges
  createExchange: (data) => apiClient.post('/api/ticket-listings/exchanges', data),
  completeExchange: (id) => apiClient.put(`/api/ticket-listings/exchanges/${id}/complete`),
  cancelExchange: (id) => apiClient.delete(`/api/ticket-listings/exchanges/${id}`),
};

export default ticketListingApi;
