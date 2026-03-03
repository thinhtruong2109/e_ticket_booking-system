import apiClient from './apiClient';

const scheduleApi = {
  getByEvent: (eventId) => apiClient.get('/api/event-schedules', { params: { eventId } }),
  getAvailable: (eventId) => apiClient.get('/api/event-schedules/available', { params: { eventId } }),
  getById: (id) => apiClient.get(`/api/event-schedules/${id}`),
  create: (data) => apiClient.post('/api/event-schedules', data),
  cancel: (id) => apiClient.put(`/api/event-schedules/${id}/cancel`),
};

export default scheduleApi;
