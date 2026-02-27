import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Container,
  Grid,
  Box,
  Typography,
  Button,
  Chip,
  Paper,
  Divider,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Alert,
} from '@mui/material';
import {
  LocationOn,
  CalendarToday,
  ConfirmationNumber,
  ArrowBack,
  ShoppingCart,
} from '@mui/icons-material';
import { eventApi, scheduleApi, ticketTypeApi } from '../../api';
import { LoadingScreen, ErrorAlert, StatusChip } from '../../components/common';
import { formatDateTime, formatCurrency, getErrorMessage } from '../../utils/helpers';
import { useAuth } from '../../contexts/AuthContext';

const EventDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();

  const [event, setEvent] = useState(null);
  const [schedules, setSchedules] = useState([]);
  const [ticketTypes, setTicketTypes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchData();
  }, [id]);

  const fetchData = async () => {
    setLoading(true);
    setError('');
    try {
      const [eventRes, schedulesRes, ticketTypesRes] = await Promise.all([
        eventApi.getEventById(id),
        scheduleApi.getAvailable(id).catch(() => ({ data: [] })),
        ticketTypeApi.getAvailable(id).catch(() => ({ data: [] })),
      ]);
      setEvent(eventRes.data);
      setSchedules(Array.isArray(schedulesRes.data) ? schedulesRes.data : []);
      setTicketTypes(Array.isArray(ticketTypesRes.data) ? ticketTypesRes.data : []);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const handleBookNow = () => {
    if (!isAuthenticated) {
      navigate('/login', { state: { from: { pathname: `/events/${id}` } } });
      return;
    }
    navigate(`/bookings/create/${id}`);
  };

  if (loading) return <LoadingScreen />;
  if (error) return <Container maxWidth="lg" sx={{ py: 4 }}><ErrorAlert message={error} onRetry={fetchData} /></Container>;
  if (!event) return null;

  return (
    <>
      {/* Banner */}
      <Box
        sx={{
          height: { xs: 200, md: 320 },
          bgcolor: 'grey.200',
          backgroundImage: event.bannerImageUrl ? `url(${event.bannerImageUrl})` : 'none',
          backgroundSize: 'cover',
          backgroundPosition: 'center',
          position: 'relative',
          display: 'flex',
          alignItems: 'flex-end',
        }}
      >
        <Box
          sx={{
            position: 'absolute',
            inset: 0,
            background: 'linear-gradient(transparent 40%, rgba(17,24,39,0.9))',
          }}
        />
        <Container maxWidth="lg" sx={{ position: 'relative', pb: 3, pt: 2 }}>
          <Button
            startIcon={<ArrowBack />}
            onClick={() => navigate(-1)}
            sx={{ color: 'white', mb: 2, opacity: 0.8, '&:hover': { opacity: 1 } }}
          >
            Back
          </Button>
          <Box sx={{ display: 'flex', gap: 1, mb: 1 }}>
            {event.categoryName && (
              <Chip label={event.categoryName} size="small" sx={{ bgcolor: 'rgba(255,255,255,0.15)', color: 'white' }} />
            )}
            <StatusChip status={event.status} />
          </Box>
          <Typography variant="h3" color="white" fontWeight={700}>
            {event.name}
          </Typography>
        </Container>
      </Box>

      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Grid container spacing={4}>
          {/* Main content */}
          <Grid item xs={12} md={8}>
            {/* Description */}
            {event.description && (
              <Paper sx={{ p: 3, mb: 3 }}>
                <Typography variant="h6" gutterBottom fontWeight={600}>
                  About This Event
                </Typography>
                <Typography variant="body1" color="text.secondary" sx={{ whiteSpace: 'pre-line' }}>
                  {event.description}
                </Typography>
              </Paper>
            )}

            {/* Schedules */}
            {schedules.length > 0 && (
              <Paper sx={{ p: 3, mb: 3 }}>
                <Typography variant="h6" gutterBottom fontWeight={600}>
                  Schedules
                </Typography>
                <TableContainer>
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>Start Time</TableCell>
                        <TableCell>End Time</TableCell>
                        <TableCell align="right">Available Seats</TableCell>
                        <TableCell align="right">Status</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {schedules.map((schedule) => (
                        <TableRow key={schedule.id}>
                          <TableCell>{formatDateTime(schedule.startTime)}</TableCell>
                          <TableCell>{formatDateTime(schedule.endTime)}</TableCell>
                          <TableCell align="right">{schedule.availableSeats}</TableCell>
                          <TableCell align="right">
                            <StatusChip status={schedule.status} />
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              </Paper>
            )}

            {/* Ticket Types */}
            {ticketTypes.length > 0 && (
              <Paper sx={{ p: 3 }}>
                <Typography variant="h6" gutterBottom fontWeight={600}>
                  Ticket Prices
                </Typography>
                <TableContainer>
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>Type</TableCell>
                        <TableCell>Description</TableCell>
                        <TableCell align="right">Price</TableCell>
                        <TableCell align="right">Available</TableCell>
                        <TableCell align="right">Max/Booking</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {ticketTypes.map((tt) => (
                        <TableRow key={tt.id}>
                          <TableCell>
                            <Typography fontWeight={600} variant="body2">{tt.name}</Typography>
                          </TableCell>
                          <TableCell>
                            <Typography variant="body2" color="text.secondary">{tt.description || '-'}</Typography>
                          </TableCell>
                          <TableCell align="right">
                            <Typography fontWeight={600}>{formatCurrency(tt.price)}</Typography>
                          </TableCell>
                          <TableCell align="right">{tt.availableQuantity}</TableCell>
                          <TableCell align="right">{tt.maxPerBooking}</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              </Paper>
            )}
          </Grid>

          {/* Sidebar */}
          <Grid item xs={12} md={4}>
            <Paper sx={{ p: 3, position: 'sticky', top: 80 }}>
              <Typography variant="h6" gutterBottom fontWeight={600}>
                Event Info
              </Typography>
              <Divider sx={{ mb: 2 }} />

              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mb: 3 }}>
                {event.venueName && (
                  <Box sx={{ display: 'flex', gap: 1.5, alignItems: 'flex-start' }}>
                    <LocationOn sx={{ fontSize: 20, color: 'grey.500', mt: 0.2 }} />
                    <Box>
                      <Typography variant="body2" fontWeight={600}>{event.venueName}</Typography>
                      {event.venueAddress && (
                        <Typography variant="caption" color="text.secondary">{event.venueAddress}</Typography>
                      )}
                    </Box>
                  </Box>
                )}

                <Box sx={{ display: 'flex', gap: 1.5, alignItems: 'center' }}>
                  <ConfirmationNumber sx={{ fontSize: 20, color: 'grey.500' }} />
                  <Box>
                    <Typography variant="body2" fontWeight={600}>
                      {event.availableTickets} / {event.totalTickets}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">Tickets available</Typography>
                  </Box>
                </Box>
              </Box>

              {ticketTypes.length > 0 && (
                <Box sx={{ mb: 3, p: 2, bgcolor: 'grey.50', borderRadius: 1, border: '1px solid', borderColor: 'divider' }}>
                  <Typography variant="caption" color="text.secondary" display="block" sx={{ mb: 0.5 }}>
                    Starting from
                  </Typography>
                  <Typography variant="h5" fontWeight={700}>
                    {formatCurrency(Math.min(...ticketTypes.map((t) => t.price)))}
                  </Typography>
                </Box>
              )}

              {event.status === 'PUBLISHED' && event.availableTickets > 0 && (
                <Button
                  variant="contained"
                  fullWidth
                  size="large"
                  startIcon={<ShoppingCart />}
                  onClick={handleBookNow}
                  sx={{ py: 1.5 }}
                >
                  Book Now
                </Button>
              )}

              {event.status === 'PUBLISHED' && event.availableTickets === 0 && (
                <Alert severity="warning" sx={{ mt: 1 }}>
                  This event is sold out
                </Alert>
              )}

              {event.allowTicketExchange && (
                <Typography variant="caption" color="text.secondary" display="block" sx={{ mt: 2, textAlign: 'center' }}>
                  Ticket exchange is enabled for this event
                </Typography>
              )}
            </Paper>
          </Grid>
        </Grid>
      </Container>
    </>
  );
};

export default EventDetailPage;
