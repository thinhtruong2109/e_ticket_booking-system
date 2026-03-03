import apiClient from './apiClient';

const ticketApi = {
  getMyTickets: () => apiClient.get('/api/tickets/my-tickets'),
  getByBooking: (bookingId) => apiClient.get(`/api/tickets/booking/${bookingId}`),
  getByCode: (ticketCode) => apiClient.get(`/api/tickets/code/${ticketCode}`),
  checkIn: (data) => apiClient.post('/api/tickets/check-in', data),
};

export default ticketApi;
