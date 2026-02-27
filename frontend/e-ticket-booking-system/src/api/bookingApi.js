import apiClient from './apiClient';

const bookingApi = {
  create: (data) => apiClient.post('/api/bookings', data),
  cancel: (id) => apiClient.delete(`/api/bookings/${id}`),
  getMyBookings: () => apiClient.get('/api/bookings/my-bookings'),
  getById: (id) => apiClient.get(`/api/bookings/${id}`),
};

export default bookingApi;
