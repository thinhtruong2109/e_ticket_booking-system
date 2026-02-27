import apiClient from './apiClient';

const categoryApi = {
  getAll: () => apiClient.get('/api/event-categories'),
  getById: (id) => apiClient.get(`/api/event-categories/${id}`),
  create: (data) => apiClient.post('/api/event-categories', data),
  update: (id, data) => apiClient.put(`/api/event-categories/${id}`, data),
  delete: (id) => apiClient.delete(`/api/event-categories/${id}`),
};

export default categoryApi;
