import apiClient from './apiClient';

const paymentApi = {
  create: (data) => apiClient.post('/api/payments', data),
  getByBooking: (bookingId) => apiClient.get(`/api/payments/booking/${bookingId}`),
  getByOrderCode: (orderCode) => apiClient.get(`/api/payments/payos/${orderCode}`),
  cancelByOrderCode: (orderCode) => apiClient.put(`/api/payments/payos/${orderCode}/cancel`),
};

export default paymentApi;
