import apiClient from './apiClient';

const promoCodeApi = {
  // Admin
  adminCreate: (data) => apiClient.post('/api/promo-codes/admin', data),
  adminGetAll: () => apiClient.get('/api/promo-codes/admin'),
  adminGetActive: () => apiClient.get('/api/promo-codes/admin/active'),
  adminGetById: (id) => apiClient.get(`/api/promo-codes/admin/${id}`),
  adminUpdate: (id, data) => apiClient.put(`/api/promo-codes/admin/${id}`, data),
  adminDeactivate: (id) => apiClient.put(`/api/promo-codes/admin/${id}/deactivate`),

  // Organizer
  organizerCreate: (data) => apiClient.post('/api/promo-codes/organizer', data),
  organizerGetAll: () => apiClient.get('/api/promo-codes/organizer'),
  organizerGetById: (id) => apiClient.get(`/api/promo-codes/organizer/${id}`),
  organizerUpdate: (id, data) => apiClient.put(`/api/promo-codes/organizer/${id}`, data),
  organizerDeactivate: (id) => apiClient.put(`/api/promo-codes/organizer/${id}/deactivate`),

  // Available promo codes for booking
  getAvailable: (data) => apiClient.post('/api/promo-codes/available', data),
};

export default promoCodeApi;
