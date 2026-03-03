import apiClient from './apiClient';

const eventApi = {
  // Public
  getPublishedEvents: (params) => apiClient.get('/api/events', { params }),
  getEventById: (id) => apiClient.get(`/api/events/${id}`),

  // Organizer/Admin
  createEvent: (data) => apiClient.post('/api/events', data),
  updateEvent: (id, data) => apiClient.put(`/api/events/${id}`, data),
  publishEvent: (id) => apiClient.put(`/api/events/${id}/publish`),
  cancelEvent: (id) => apiClient.put(`/api/events/${id}/cancel`),
  getMyEvents: () => apiClient.get('/api/events/my-events'),

  // Admin
  getAllEvents: () => apiClient.get('/api/events/all'),
};

export default eventApi;
