import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Container,
  Grid,
  Box,
  Typography,
  Button,
  Paper,
  Divider,
  TextField,
  Alert,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  IconButton,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Chip,
  Collapse,
} from '@mui/material';
import { Add, Remove, ArrowBack, ExpandMore, ExpandLess } from '@mui/icons-material';
import { eventApi, scheduleApi, ticketTypeApi, bookingApi, promoCodeApi } from '../../api';
import { LoadingScreen, ErrorAlert, PageHeader } from '../../components/common';
import { formatCurrency, formatDateTime, getErrorMessage } from '../../utils/helpers';

const CreateBookingPage = () => {
  const { eventId } = useParams();
  const navigate = useNavigate();

  const [event, setEvent] = useState(null);
  const [schedules, setSchedules] = useState([]);
  const [ticketTypes, setTicketTypes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  // Form state
  const [selectedSchedule, setSelectedSchedule] = useState('');
  const [quantities, setQuantities] = useState({});
  const [selectedPromo, setSelectedPromo] = useState(null);
  const [promos, setPromos] = useState([]);
  const [showPromos, setShowPromos] = useState(false);
  const [loadingPromos, setLoadingPromos] = useState(false);

  useEffect(() => {
    fetchData();
  }, [eventId]);

  const fetchData = async () => {
    setLoading(true);
    try {
      const [eventRes, schedulesRes, ticketTypesRes] = await Promise.all([
        eventApi.getEventById(eventId),
        scheduleApi.getAvailable(eventId).catch(() => ({ data: [] })),
        ticketTypeApi.getAvailable(eventId).catch(() => ({ data: [] })),
      ]);
      setEvent(eventRes.data);
      const sched = Array.isArray(schedulesRes.data) ? schedulesRes.data : [];
      setSchedules(sched);
      if (sched.length === 1) setSelectedSchedule(sched[0].id);
      setTicketTypes(Array.isArray(ticketTypesRes.data) ? ticketTypesRes.data : []);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const totalAmount = ticketTypes.reduce((acc, tt) => {
    return acc + (quantities[tt.id] || 0) * tt.price;
  }, 0);

  const itemCount = Object.values(quantities).reduce((a, b) => a + b, 0);

  const handleQuantityChange = (ttId, delta, max) => {
    setQuantities((prev) => {
      const current = prev[ttId] || 0;
      const newVal = Math.max(0, Math.min(max, current + delta));
      return { ...prev, [ttId]: newVal };
    });
    setSelectedPromo(null);
  };

  const handleFetchPromos = async () => {
    if (itemCount === 0) return;
    setLoadingPromos(true);
    setShowPromos(true);
    try {
      const items = Object.entries(quantities)
        .filter(([, qty]) => qty > 0)
        .map(([ticketTypeId, quantity]) => ({ ticketTypeId: parseInt(ticketTypeId), quantity }));
      const res = await promoCodeApi.getAvailable({ eventId: parseInt(eventId), items });
      setPromos(res.data.availablePromoCodes || []);
    } catch {
      setPromos([]);
    } finally {
      setLoadingPromos(false);
    }
  };

  const discountAmount = selectedPromo?.discountAmount || 0;
  const finalAmount = totalAmount - discountAmount;

  const handleSubmit = async () => {
    if (itemCount === 0) {
      setError('Please select at least one ticket');
      return;
    }
    setError('');
    setSubmitting(true);
    try {
      const items = Object.entries(quantities)
        .filter(([, qty]) => qty > 0)
        .map(([ticketTypeId, quantity]) => ({ ticketTypeId: parseInt(ticketTypeId), quantity }));

      const data = {
        eventId: parseInt(eventId),
        items,
      };
      if (selectedSchedule) data.scheduleId = selectedSchedule;
      if (selectedPromo) data.promoCodeId = selectedPromo.id;

      const res = await bookingApi.create(data);
      navigate(`/bookings/${res.data.id}/payment`);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <LoadingScreen />;
  if (!event) return <Container sx={{ py: 4 }}><ErrorAlert message="Event not found" /></Container>;

  return (
    <>
      <PageHeader
        title="Book Tickets"
        subtitle={event.name}
        action={
          <Button startIcon={<ArrowBack />} onClick={() => navigate(`/events/${eventId}`)}
            sx={{ color: 'white', borderColor: 'rgba(255,255,255,0.3)', '&:hover': { borderColor: 'rgba(255,255,255,0.5)' } }}
            variant="outlined"
          >
            Back to Event
          </Button>
        }
      />
      <Container maxWidth="lg" sx={{ py: 4 }}>
        {error && <ErrorAlert message={error} />}

        <Grid container spacing={4}>
          <Grid item xs={12} md={8}>
            {/* Schedule Selection */}
            {schedules.length > 1 && (
              <Paper sx={{ p: 3, mb: 3 }}>
                <Typography variant="h6" gutterBottom fontWeight={600}>
                  Select Schedule
                </Typography>
                <FormControl fullWidth>
                  <InputLabel>Schedule</InputLabel>
                  <Select
                    value={selectedSchedule}
                    onChange={(e) => setSelectedSchedule(e.target.value)}
                    label="Schedule"
                  >
                    {schedules.map((s) => (
                      <MenuItem key={s.id} value={s.id}>
                        {formatDateTime(s.startTime)} - {formatDateTime(s.endTime)} ({s.availableSeats} seats left)
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Paper>
            )}

            {/* Ticket Selection */}
            <Paper sx={{ p: 3, mb: 3 }}>
              <Typography variant="h6" gutterBottom fontWeight={600}>
                Select Tickets
              </Typography>
              {ticketTypes.map((tt) => (
                <Box key={tt.id}>
                  <Box
                    sx={{
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'space-between',
                      py: 2,
                    }}
                  >
                    <Box sx={{ flex: 1 }}>
                      <Typography fontWeight={600}>{tt.name}</Typography>
                      {tt.description && (
                        <Typography variant="body2" color="text.secondary">{tt.description}</Typography>
                      )}
                      <Typography variant="body2" fontWeight={600} sx={{ mt: 0.5 }}>
                        {formatCurrency(tt.price)}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        {tt.availableQuantity} available · Max {tt.maxPerBooking} per booking
                      </Typography>
                    </Box>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <IconButton
                        size="small"
                        onClick={() => handleQuantityChange(tt.id, -1, tt.maxPerBooking)}
                        disabled={!quantities[tt.id]}
                        sx={{ border: '1px solid', borderColor: 'divider' }}
                      >
                        <Remove fontSize="small" />
                      </IconButton>
                      <Typography sx={{ minWidth: 24, textAlign: 'center', fontWeight: 600 }}>
                        {quantities[tt.id] || 0}
                      </Typography>
                      <IconButton
                        size="small"
                        onClick={() => handleQuantityChange(tt.id, 1, Math.min(tt.maxPerBooking, tt.availableQuantity))}
                        disabled={(quantities[tt.id] || 0) >= Math.min(tt.maxPerBooking, tt.availableQuantity)}
                        sx={{ border: '1px solid', borderColor: 'divider' }}
                      >
                        <Add fontSize="small" />
                      </IconButton>
                    </Box>
                  </Box>
                  <Divider />
                </Box>
              ))}
            </Paper>

            {/* Promo Code */}
            <Paper sx={{ p: 3 }}>
              <Box
                sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', cursor: 'pointer' }}
                onClick={() => (showPromos ? setShowPromos(false) : handleFetchPromos())}
              >
                <Typography variant="h6" fontWeight={600}>
                  Promo Code
                </Typography>
                {showPromos ? <ExpandLess /> : <ExpandMore />}
              </Box>
              <Collapse in={showPromos}>
                <Box sx={{ mt: 2 }}>
                  {loadingPromos ? (
                    <Typography variant="body2" color="text.secondary">Loading available promos...</Typography>
                  ) : promos.length === 0 ? (
                    <Typography variant="body2" color="text.secondary">No promo codes available for this order</Typography>
                  ) : (
                    promos.map((promo) => (
                      <Box
                        key={promo.id}
                        onClick={() => setSelectedPromo(selectedPromo?.id === promo.id ? null : promo)}
                        sx={{
                          p: 2,
                          mb: 1,
                          border: '1px solid',
                          borderColor: selectedPromo?.id === promo.id ? 'grey.900' : 'divider',
                          borderRadius: 1,
                          cursor: 'pointer',
                          bgcolor: selectedPromo?.id === promo.id ? 'grey.50' : 'white',
                          '&:hover': { bgcolor: 'grey.50' },
                        }}
                      >
                        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                          <Box>
                            <Typography fontWeight={600} variant="body2">{promo.code}</Typography>
                            {promo.description && (
                              <Typography variant="caption" color="text.secondary">{promo.description}</Typography>
                            )}
                          </Box>
                          <Chip
                            label={`-${formatCurrency(promo.discountAmount)}`}
                            size="small"
                            color="success"
                            variant="outlined"
                          />
                        </Box>
                      </Box>
                    ))
                  )}
                </Box>
              </Collapse>
            </Paper>
          </Grid>

          {/* Order Summary */}
          <Grid item xs={12} md={4}>
            <Paper sx={{ p: 3, position: 'sticky', top: 80 }}>
              <Typography variant="h6" gutterBottom fontWeight={600}>
                Order Summary
              </Typography>
              <Divider sx={{ mb: 2 }} />

              {Object.entries(quantities)
                .filter(([, qty]) => qty > 0)
                .map(([ttId, qty]) => {
                  const tt = ticketTypes.find((t) => t.id === parseInt(ttId));
                  return (
                    <Box key={ttId} sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                      <Typography variant="body2">
                        {tt?.name} x {qty}
                      </Typography>
                      <Typography variant="body2" fontWeight={500}>
                        {formatCurrency(tt?.price * qty)}
                      </Typography>
                    </Box>
                  );
                })}

              {itemCount === 0 && (
                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                  No tickets selected
                </Typography>
              )}

              <Divider sx={{ my: 2 }} />

              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                <Typography variant="body2">Subtotal</Typography>
                <Typography variant="body2" fontWeight={500}>{formatCurrency(totalAmount)}</Typography>
              </Box>

              {selectedPromo && (
                <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                  <Typography variant="body2" color="success.main">
                    Discount ({selectedPromo.code})
                  </Typography>
                  <Typography variant="body2" color="success.main" fontWeight={500}>
                    -{formatCurrency(discountAmount)}
                  </Typography>
                </Box>
              )}

              <Divider sx={{ my: 2 }} />

              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
                <Typography variant="h6" fontWeight={700}>Total</Typography>
                <Typography variant="h6" fontWeight={700}>{formatCurrency(finalAmount)}</Typography>
              </Box>

              <Button
                variant="contained"
                fullWidth
                size="large"
                onClick={handleSubmit}
                disabled={submitting || itemCount === 0}
                sx={{ py: 1.5 }}
              >
                {submitting ? 'Processing...' : 'Proceed to Payment'}
              </Button>

              <Typography variant="caption" color="text.secondary" display="block" sx={{ mt: 2, textAlign: 'center' }}>
                Booking will be held for 15 minutes
              </Typography>
            </Paper>
          </Grid>
        </Grid>
      </Container>
    </>
  );
};

export default CreateBookingPage;
