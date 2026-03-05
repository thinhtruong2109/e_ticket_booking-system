import apiClient from './apiClient';

const venueApi = {
  getAll: () => apiClient.get('/api/venues'),
  getById: (id) => apiClient.get(`/api/venues/${id}`),
  search: (city) => apiClient.get('/api/venues/search', { params: { city } }),
  create: (data) => apiClient.post('/api/venues', data),
  update: (id, data) => apiClient.put(`/api/venues/${id}`, data),
  delete: (id) => apiClient.delete(`/api/venues/${id}`),
};

export default venueApi;
