import apiClient from './apiClient';

const ticketTypeApi = {
  getByEvent: (eventId) => apiClient.get('/api/ticket-types', { params: { eventId } }),
  getAvailable: (eventId) => apiClient.get('/api/ticket-types/available', { params: { eventId } }),
  getById: (id) => apiClient.get(`/api/ticket-types/${id}`),
  create: (data) => apiClient.post('/api/ticket-types', data),
};

export default ticketTypeApi;
