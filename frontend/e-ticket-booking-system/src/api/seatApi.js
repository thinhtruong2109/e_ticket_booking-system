import apiClient from './apiClient';

const seatApi = {
  createSection: (data) => apiClient.post('/api/seats/sections', data),
  getSectionsByVenue: (venueId) => apiClient.get(`/api/seats/sections/venue/${venueId}`),
  createSeat: (data) => apiClient.post('/api/seats', data),
  getSeatsByVenue: (venueId) => apiClient.get(`/api/seats/venue/${venueId}`),
  getAvailableSeats: (scheduleId) => apiClient.get('/api/seats/available', { params: { scheduleId } }),
};

export default seatApi;
