import { useState, useEffect } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import {
  Container,
  Paper,
  Typography,
  Box,
  Button,
  Divider,
  Grid,
  Alert,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
} from '@mui/material';
import { ArrowBack, Payment, ConfirmationNumber } from '@mui/icons-material';
import { bookingApi, ticketApi, paymentApi } from '../../api';
import { LoadingScreen, ErrorAlert, PageHeader, StatusChip } from '../../components/common';
import { formatCurrency, formatDateTime, getErrorMessage } from '../../utils/helpers';

const BookingDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const location = useLocation();

  const [booking, setBooking] = useState(null);
  const [tickets, setTickets] = useState([]);
  const [payment, setPayment] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const showSuccess = location.state?.paymentSuccess;

  useEffect(() => {
    fetchData();
  }, [id]);

  const fetchData = async () => {
    setLoading(true);
    try {
      const [bookingRes, ticketsRes] = await Promise.all([
        bookingApi.getById(id),
        ticketApi.getByBooking(id).catch(() => ({ data: [] })),
      ]);
      setBooking(bookingRes.data);
      setTickets(Array.isArray(ticketsRes.data) ? ticketsRes.data : []);

      try {
        const payRes = await paymentApi.getByBooking(id);
        setPayment(payRes.data);
      } catch {}
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <LoadingScreen />;
  if (error) return <Container sx={{ py: 4 }}><ErrorAlert message={error} /></Container>;
  if (!booking) return null;

  return (
    <>
      <PageHeader
        title={`Booking #${booking.bookingCode}`}
        action={
          <Button startIcon={<ArrowBack />} onClick={() => navigate('/my-bookings')}
            sx={{ color: 'white', borderColor: 'rgba(255,255,255,0.3)', '&:hover': { borderColor: 'rgba(255,255,255,0.5)' } }}
            variant="outlined"
          >
            Back to Bookings
          </Button>
        }
      />
      <Container maxWidth="lg" sx={{ py: 4 }}>
        {showSuccess && (
          <Alert severity="success" sx={{ mb: 3 }}>
            Payment processed successfully! Your tickets are being generated.
          </Alert>
        )}

        <Grid container spacing={3}>
          <Grid item xs={12} md={8}>
            {/* Booking Details */}
            <Paper sx={{ p: 3, mb: 3 }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Typography variant="h6" fontWeight={600}>Order Details</Typography>
                <StatusChip status={booking.status} />
              </Box>
              <Divider sx={{ mb: 2 }} />

              {booking.bookingDetails?.map((detail, idx) => (
                <Box key={idx} sx={{ display: 'flex', justifyContent: 'space-between', py: 1 }}>
                  <Box>
                    <Typography variant="body2" fontWeight={600}>
                      {detail.ticketTypeName || `Ticket Type #${detail.ticketTypeId}`}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      {formatCurrency(detail.unitPrice)} x {detail.quantity}
                    </Typography>
                  </Box>
                  <Typography variant="body2" fontWeight={500}>
                    {formatCurrency(detail.subtotal)}
                  </Typography>
                </Box>
              ))}

              <Divider sx={{ my: 2 }} />
              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
                <Typography variant="body2">Subtotal</Typography>
                <Typography variant="body2">{formatCurrency(booking.totalAmount)}</Typography>
              </Box>
              {booking.discountAmount > 0 && (
                <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
                  <Typography variant="body2" color="success.main">Discount</Typography>
                  <Typography variant="body2" color="success.main">-{formatCurrency(booking.discountAmount)}</Typography>
                </Box>
              )}
              <Divider sx={{ my: 1 }} />
              <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                <Typography fontWeight={700}>Total</Typography>
                <Typography fontWeight={700}>{formatCurrency(booking.finalAmount)}</Typography>
              </Box>
            </Paper>

            {/* Tickets */}
            {tickets.length > 0 && (
              <Paper sx={{ p: 3 }}>
                <Typography variant="h6" gutterBottom fontWeight={600}>
                  Your Tickets
                </Typography>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Ticket Code</TableCell>
                      <TableCell>Type</TableCell>
                      <TableCell>Seat</TableCell>
                      <TableCell align="center">Checked In</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {tickets.map((ticket) => (
                      <TableRow key={ticket.id}>
                        <TableCell>
                          <Typography variant="body2" fontFamily="monospace" fontWeight={600}>
                            {ticket.ticketCode}
                          </Typography>
                        </TableCell>
                        <TableCell>{ticket.ticketTypeName || '-'}</TableCell>
                        <TableCell>{ticket.seatNumber || 'N/A'}</TableCell>
                        <TableCell align="center">
                          {ticket.isCheckedIn ? (
                            <StatusChip status="COMPLETED" />
                          ) : (
                            <StatusChip status="PENDING" />
                          )}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </Paper>
            )}
          </Grid>

          <Grid item xs={12} md={4}>
            {/* Payment Info */}
            <Paper sx={{ p: 3, mb: 3 }}>
              <Typography variant="h6" gutterBottom fontWeight={600}>
                Payment
              </Typography>
              <Divider sx={{ mb: 2 }} />
              {payment ? (
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Typography variant="body2" color="text.secondary">Method</Typography>
                    <Typography variant="body2" fontWeight={500}>{payment.paymentMethod}</Typography>
                  </Box>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Typography variant="body2" color="text.secondary">Status</Typography>
                    <StatusChip status={payment.status} />
                  </Box>
                  {payment.transactionId && (
                    <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                      <Typography variant="body2" color="text.secondary">Transaction ID</Typography>
                      <Typography variant="body2" fontFamily="monospace" fontSize="0.75rem">
                        {payment.transactionId}
                      </Typography>
                    </Box>
                  )}
                  {payment.paidAt && (
                    <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                      <Typography variant="body2" color="text.secondary">Paid at</Typography>
                      <Typography variant="body2">{formatDateTime(payment.paidAt)}</Typography>
                    </Box>
                  )}
                </Box>
              ) : booking.status === 'PENDING' ? (
                <Box>
                  <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                    Payment pending
                  </Typography>
                  <Button
                    variant="contained"
                    fullWidth
                    startIcon={<Payment />}
                    onClick={() => navigate(`/bookings/${id}/payment`)}
                  >
                    Pay Now
                  </Button>
                </Box>
              ) : (
                <Typography variant="body2" color="text.secondary">No payment information</Typography>
              )}
            </Paper>

            {/* Booking Meta */}
            <Paper sx={{ p: 3 }}>
              <Typography variant="h6" gutterBottom fontWeight={600}>
                Info
              </Typography>
              <Divider sx={{ mb: 2 }} />
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                  <Typography variant="body2" color="text.secondary">Event</Typography>
                  <Typography variant="body2" fontWeight={500}>
                    {booking.eventName || `#${booking.eventId}`}
                  </Typography>
                </Box>
                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                  <Typography variant="body2" color="text.secondary">Created</Typography>
                  <Typography variant="body2">{formatDateTime(booking.createdAt)}</Typography>
                </Box>
                {booking.holdExpiresAt && booking.status === 'PENDING' && (
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Typography variant="body2" color="text.secondary">Expires</Typography>
                    <Typography variant="body2" color="warning.main">
                      {formatDateTime(booking.holdExpiresAt)}
                    </Typography>
                  </Box>
                )}
              </Box>
            </Paper>
          </Grid>
        </Grid>
      </Container>
    </>
  );
};

export default BookingDetailPage;
