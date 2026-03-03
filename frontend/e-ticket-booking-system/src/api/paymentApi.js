import apiClient from './apiClient';

const paymentApi = {
  create: (data) => apiClient.post('/api/payments', data),
  getByBooking: (bookingId) => apiClient.get(`/api/payments/booking/${bookingId}`),
};

export default paymentApi;
