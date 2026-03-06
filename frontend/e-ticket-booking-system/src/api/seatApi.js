import apiClient from './apiClient';

const seatApi = {
  createSection: (data) => apiClient.post('/api/seats/sections', data),
  getSectionsByVenue: (venueId) => apiClient.get(`/api/seats/sections/venue/${venueId}`),
  updateSection: (id, data) => apiClient.put(`/api/seats/sections/${id}`, data),
  deleteSection: (id) => apiClient.delete(`/api/seats/sections/${id}`),
  createSeat: (data) => apiClient.post('/api/seats', data),
  getSeatsByVenue: (venueId) => apiClient.get(`/api/seats/venue/${venueId}`),
  getAvailableSeats: (scheduleId) => apiClient.get('/api/seats/available', { params: { scheduleId } }),
  bulkCreate: (data) => apiClient.post('/api/seats/bulk', data),
};

export default seatApi;
