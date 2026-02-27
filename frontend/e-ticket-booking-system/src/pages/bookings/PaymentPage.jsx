import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Container,
  Paper,
  Typography,
  Box,
  Button,
  Divider,
  RadioGroup,
  FormControlLabel,
  Radio,
  Alert,
  LinearProgress,
} from '@mui/material';
import { Payment, Timer } from '@mui/icons-material';
import { bookingApi, paymentApi } from '../../api';
import { LoadingScreen, ErrorAlert, PageHeader } from '../../components/common';
import { formatCurrency, formatDateTime, getErrorMessage } from '../../utils/helpers';
import { PAYMENT_METHODS } from '../../utils/constants';

const PaymentPage = () => {
  const { bookingId } = useParams();
  const navigate = useNavigate();

  const [booking, setBooking] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [paymentMethod, setPaymentMethod] = useState('VNPAY');
  const [submitting, setSubmitting] = useState(false);
  const [timeLeft, setTimeLeft] = useState(null);

  useEffect(() => {
    fetchBooking();
  }, [bookingId]);

  useEffect(() => {
    if (!booking?.holdExpiresAt) return;
    const interval = setInterval(() => {
      const now = new Date().getTime();
      const expires = new Date(booking.holdExpiresAt).getTime();
      const diff = Math.max(0, Math.floor((expires - now) / 1000));
      setTimeLeft(diff);
      if (diff === 0) {
        clearInterval(interval);
        setError('Booking has expired. Please create a new booking.');
      }
    }, 1000);
    return () => clearInterval(interval);
  }, [booking]);

  const fetchBooking = async () => {
    setLoading(true);
    try {
      const res = await bookingApi.getById(bookingId);
      setBooking(res.data);
      if (res.data.status !== 'PENDING') {
        setError(`Booking is ${res.data.status.toLowerCase()}. Cannot process payment.`);
      }
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const handlePay = async () => {
    setError('');
    setSubmitting(true);
    try {
      const res = await paymentApi.create({
        bookingId: parseInt(bookingId),
        paymentMethod,
      });

      // If payment URL returned (e.g., VNPay redirect)
      if (res.data?.paymentUrl) {
        window.location.href = res.data.paymentUrl;
      } else {
        // For demo: simulate callback
        navigate(`/bookings/${bookingId}`, { state: { paymentSuccess: true } });
      }
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  const formatTime = (secs) => {
    const m = Math.floor(secs / 60);
    const s = secs % 60;
    return `${m}:${s.toString().padStart(2, '0')}`;
  };

  if (loading) return <LoadingScreen />;

  return (
    <>
      <PageHeader title="Payment" subtitle={`Booking #${booking?.bookingCode || bookingId}`} />
      <Container maxWidth="sm" sx={{ py: 4 }}>
        {error && <ErrorAlert message={error} />}

        {timeLeft !== null && timeLeft > 0 && (
          <Alert
            severity={timeLeft < 120 ? 'warning' : 'info'}
            icon={<Timer />}
            sx={{ mb: 3 }}
          >
            Time remaining: <strong>{formatTime(timeLeft)}</strong>
          </Alert>
        )}

        {booking && (
          <Paper sx={{ p: 3, mb: 3 }}>
            <Typography variant="h6" gutterBottom fontWeight={600}>
              Order Details
            </Typography>
            <Divider sx={{ mb: 2 }} />

            {booking.bookingDetails?.map((detail, idx) => (
              <Box key={idx} sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                <Typography variant="body2">
                  {detail.ticketTypeName || `Ticket Type #${detail.ticketTypeId}`} x {detail.quantity}
                </Typography>
                <Typography variant="body2" fontWeight={500}>
                  {formatCurrency(detail.subtotal)}
                </Typography>
              </Box>
            ))}

            <Divider sx={{ my: 2 }} />

            <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
              <Typography variant="body2">Subtotal</Typography>
              <Typography variant="body2">{formatCurrency(booking.totalAmount)}</Typography>
            </Box>

            {booking.discountAmount > 0 && (
              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                <Typography variant="body2" color="success.main">Discount</Typography>
                <Typography variant="body2" color="success.main">
                  -{formatCurrency(booking.discountAmount)}
                </Typography>
              </Box>
            )}

            <Divider sx={{ my: 2 }} />

            <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
              <Typography variant="h6" fontWeight={700}>Total</Typography>
              <Typography variant="h6" fontWeight={700}>{formatCurrency(booking.finalAmount)}</Typography>
            </Box>
          </Paper>
        )}

        <Paper sx={{ p: 3, mb: 3 }}>
          <Typography variant="h6" gutterBottom fontWeight={600}>
            Payment Method
          </Typography>
          <Divider sx={{ mb: 2 }} />

          <RadioGroup value={paymentMethod} onChange={(e) => setPaymentMethod(e.target.value)}>
            {PAYMENT_METHODS.map((method) => (
              <FormControlLabel
                key={method}
                value={method}
                control={<Radio sx={{ color: 'grey.400', '&.Mui-checked': { color: 'grey.900' } }} />}
                label={
                  <Box>
                    <Typography variant="body2" fontWeight={600}>{method}</Typography>
                  </Box>
                }
                sx={{
                  mb: 1,
                  p: 1.5,
                  border: '1px solid',
                  borderColor: paymentMethod === method ? 'grey.900' : 'divider',
                  borderRadius: 1,
                  mx: 0,
                  bgcolor: paymentMethod === method ? 'grey.50' : 'white',
                }}
              />
            ))}
          </RadioGroup>
        </Paper>

        <Button
          variant="contained"
          fullWidth
          size="large"
          startIcon={<Payment />}
          onClick={handlePay}
          disabled={submitting || booking?.status !== 'PENDING' || (timeLeft !== null && timeLeft === 0)}
          sx={{ py: 1.5 }}
        >
          {submitting ? 'Processing...' : `Pay ${formatCurrency(booking?.finalAmount || 0)}`}
        </Button>
      </Container>
    </>
  );
};

export default PaymentPage;
