import { useState, useEffect, useMemo } from 'react';
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
  CircularProgress,
} from '@mui/material';
import { Add, Remove, ArrowBack, ExpandMore, ExpandLess, EventSeat, CheckCircle } from '@mui/icons-material';
import { eventApi, scheduleApi, ticketTypeApi, bookingApi, promoCodeApi, seatApi } from '../../api';
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

  // Seat selection state
  const [availableSeats, setAvailableSeats] = useState([]);
  const [selectedSeatIds, setSelectedSeatIds] = useState([]);
  const [loadingSeats, setLoadingSeats] = useState(false);

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

  // Check if any selected ticket type requires numbered seats
  const requiresSeats = useMemo(() => {
    return ticketTypes.some(
      (tt) => (quantities[tt.id] || 0) > 0 && tt.hasNumberedSeats === true
    );
  }, [ticketTypes, quantities]);

  // Calculate how many seats are needed for numbered-seat ticket types
  const requiredSeatCount = useMemo(() => {
    return ticketTypes.reduce((acc, tt) => {
      if (tt.hasNumberedSeats === true) {
        return acc + (quantities[tt.id] || 0);
      }
      return acc;
    }, 0);
  }, [ticketTypes, quantities]);

  // Build per-section seat requirements: { sectionId: { sectionName, required } }
  const seatRequirementsBySection = useMemo(() => {
    const map = {};
    ticketTypes.forEach((tt) => {
      if ((quantities[tt.id] || 0) > 0 && tt.hasNumberedSeats && tt.sectionId) {
        if (!map[tt.sectionId]) {
          map[tt.sectionId] = { sectionName: tt.sectionName, required: 0 };
        }
        map[tt.sectionId].required += (quantities[tt.id] || 0);
      }
    });
    return map;
  }, [ticketTypes, quantities]);

  // Get allowed section IDs from selected ticket types (for seat filtering)
  const allowedSectionIds = useMemo(() => {
    return Object.keys(seatRequirementsBySection).map(Number);
  }, [seatRequirementsBySection]);

  // Count selected seats per section
  const selectedSeatsBySection = useMemo(() => {
    const map = {};
    for (const seatId of selectedSeatIds) {
      const seat = availableSeats.find((s) => s.id === seatId);
      if (seat && seat.sectionId) {
        map[seat.sectionId] = (map[seat.sectionId] || 0) + 1;
      }
    }
    return map;
  }, [selectedSeatIds, availableSeats]);

  // Fetch available seats when schedule is selected and seats are needed
  useEffect(() => {
    if (requiresSeats && selectedSchedule) {
      fetchAvailableSeats();
    } else {
      setAvailableSeats([]);
      setSelectedSeatIds([]);
    }
  }, [requiresSeats, selectedSchedule]);

  const fetchAvailableSeats = async () => {
    if (!selectedSchedule) return;
    setLoadingSeats(true);
    try {
      const res = await seatApi.getAvailableSeats(selectedSchedule);
      const seats = Array.isArray(res.data) ? res.data : [];
      setAvailableSeats(seats);
    } catch (err) {
      setAvailableSeats([]);
    } finally {
      setLoadingSeats(false);
    }
  };

  // Filter seats to only show those in allowed sections
  const filteredSeats = useMemo(() => {
    if (allowedSectionIds.length === 0) return availableSeats;
    return availableSeats.filter((s) => allowedSectionIds.includes(s.sectionId));
  }, [availableSeats, allowedSectionIds]);

  // Group seats by section and row for display
  const seatsBySection = useMemo(() => {
    const map = {};
    for (const seat of filteredSeats) {
      const sectionKey = seat.sectionName || 'General';
      if (!map[sectionKey]) map[sectionKey] = {};
      const rowKey = seat.rowNumber || '-';
      if (!map[sectionKey][rowKey]) map[sectionKey][rowKey] = [];
      map[sectionKey][rowKey].push(seat);
    }
    // Sort seats within each row by seatNumber
    for (const section of Object.values(map)) {
      for (const row of Object.keys(section)) {
        section[row].sort((a, b) => {
          const numA = parseInt(a.seatNumber) || 0;
          const numB = parseInt(b.seatNumber) || 0;
          return numA - numB;
        });
      }
    }
    return map;
  }, [filteredSeats]);

  const handleSeatToggle = (seatId) => {
    const seat = filteredSeats.find((s) => s.id === seatId);
    if (!seat) return;

    setSelectedSeatIds((prev) => {
      // Deselect
      if (prev.includes(seatId)) {
        return prev.filter((id) => id !== seatId);
      }
      // Check global limit
      if (prev.length >= requiredSeatCount) return prev;
      // Check per-section limit
      if (seat.sectionId && seatRequirementsBySection[seat.sectionId]) {
        const currentInSection = prev.filter((id) => {
          const s = availableSeats.find((x) => x.id === id);
          return s && s.sectionId === seat.sectionId;
        }).length;
        if (currentInSection >= seatRequirementsBySection[seat.sectionId].required) {
          return prev; // Section limit reached
        }
      }
      return [...prev, seatId];
    });
  };

  const handleQuantityChange = (ttId, delta, max) => {
    setQuantities((prev) => {
      const current = prev[ttId] || 0;
      const newVal = Math.max(0, Math.min(max, current + delta));
      return { ...prev, [ttId]: newVal };
    });
    setSelectedPromo(null);
    setSelectedSeatIds([]);
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
    if (requiresSeats && !selectedSchedule) {
      setError('Please select a schedule for seat-based tickets');
      return;
    }
    if (requiresSeats && selectedSeatIds.length !== requiredSeatCount) {
      setError(`Please select ${requiredSeatCount} seat(s). Currently selected: ${selectedSeatIds.length}`);
      return;
    }
    // Validate per-section seat counts match ticket type requirements
    if (requiresSeats) {
      for (const [sectionId, req] of Object.entries(seatRequirementsBySection)) {
        const selected = selectedSeatsBySection[Number(sectionId)] || 0;
        if (selected !== req.required) {
          setError(`Section "${req.sectionName}" requires ${req.required} seat(s), but ${selected} selected`);
          return;
        }
      }
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
      if (requiresSeats && selectedSeatIds.length > 0) {
        data.seatIds = selectedSeatIds;
      }

      const res = await bookingApi.create(data);
      navigate(`/payment/${res.data.id}`);
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
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <Typography fontWeight={600}>{tt.name}</Typography>
                        {tt.hasNumberedSeats && (
                          <Chip label="Seat selection" size="small" icon={<EventSeat />} variant="outlined" color="info" sx={{ height: 22, '& .MuiChip-label': { fontSize: '0.7rem' } }} />
                        )}
                      </Box>
                      {tt.description && (
                        <Typography variant="body2" color="text.secondary">{tt.description}</Typography>
                      )}
                      {tt.sectionName && (
                        <Typography variant="caption" color="primary">{tt.sectionName}</Typography>
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

            {/* Seat Selection - only shown when ticket types require numbered seats */}
            {requiresSeats && selectedSchedule && (
              <Paper sx={{ p: 3, mb: 3 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <EventSeat color="primary" />
                    <Typography variant="h6" fontWeight={600}>
                      Select Seats
                    </Typography>
                  </Box>
                  <Chip
                    label={`${selectedSeatIds.length} / ${requiredSeatCount} selected`}
                    color={selectedSeatIds.length === requiredSeatCount ? 'success' : 'default'}
                    size="small"
                  />
                </Box>

                {loadingSeats ? (
                  <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
                    <CircularProgress size={32} />
                  </Box>
                ) : filteredSeats.length === 0 ? (
                  <Alert severity="warning">No seats available for this schedule</Alert>
                ) : (
                  <>
                    {/* Per-section selection requirements */}
                    {Object.keys(seatRequirementsBySection).length > 1 && (
                      <Alert severity="info" sx={{ mb: 2 }}>
                        Select seats from each section matching your ticket types.
                      </Alert>
                    )}
                    {Object.entries(seatsBySection).map(([sectionName, rows]) => {
                      // Find matching section requirement
                      const sectionEntry = Object.entries(seatRequirementsBySection).find(
                        ([, val]) => val.sectionName === sectionName
                      );
                      const sectionId = sectionEntry ? Number(sectionEntry[0]) : null;
                      const sectionReq = sectionEntry ? sectionEntry[1].required : 0;
                      const sectionSelected = sectionId ? (selectedSeatsBySection[sectionId] || 0) : 0;
                      const sectionFull = sectionReq > 0 && sectionSelected >= sectionReq;

                      return (
                        <Box key={sectionName} sx={{ mb: 3 }}>
                          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1 }}>
                            <Typography variant="subtitle2" fontWeight={600} color="text.secondary">
                              {sectionName}
                            </Typography>
                            {sectionReq > 0 && (
                              <Chip
                                label={`${sectionSelected} / ${sectionReq} seats`}
                                size="small"
                                color={sectionSelected === sectionReq ? 'success' : 'default'}
                                variant="outlined"
                              />
                            )}
                          </Box>
                          {Object.entries(rows).map(([rowLabel, seats]) => (
                            <Box key={rowLabel} sx={{ display: 'flex', alignItems: 'center', mb: 1, gap: 0.5 }}>
                              <Typography
                                variant="caption"
                                fontWeight={600}
                                sx={{ minWidth: 28, textAlign: 'center', color: 'text.secondary' }}
                              >
                                {rowLabel}
                              </Typography>
                              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                                {seats.map((seat) => {
                                  const isSelected = selectedSeatIds.includes(seat.id);
                                  const isDisabled = !seat.available;
                                  // Disable if this section's quota is full (unless deselecting)
                                  const isSectionFull = !isSelected && sectionFull;
                                  const isGlobalFull = !isSelected && selectedSeatIds.length >= requiredSeatCount;
                                  return (
                                    <Button
                                      key={seat.id}
                                      size="small"
                                      variant={isSelected ? 'contained' : 'outlined'}
                                      disabled={isDisabled || isSectionFull || isGlobalFull}
                                      onClick={() => handleSeatToggle(seat.id)}
                                      sx={{
                                        minWidth: 36,
                                    height: 32,
                                    p: 0,
                                    fontSize: '0.7rem',
                                    fontWeight: 600,
                                    ...(isDisabled && {
                                      bgcolor: 'grey.300',
                                      color: 'grey.500',
                                      borderColor: 'grey.300',
                                    }),
                                    ...(isSelected && {
                                      bgcolor: 'primary.main',
                                      color: 'white',
                                    }),
                                  }}
                                >
                                  {seat.seatNumber}
                                </Button>
                              );
                            })}
                          </Box>
                        </Box>
                      ))}
                    </Box>
                      );
                    })}
                  </>
                )}

                {/* Seat legend */}
                <Box sx={{ display: 'flex', gap: 3, mt: 2, pt: 2, borderTop: '1px solid', borderColor: 'divider' }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                    <Box sx={{ width: 16, height: 16, border: '1px solid', borderColor: 'primary.main', borderRadius: 0.5 }} />
                    <Typography variant="caption">Available</Typography>
                  </Box>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                    <Box sx={{ width: 16, height: 16, bgcolor: 'primary.main', borderRadius: 0.5 }} />
                    <Typography variant="caption">Selected</Typography>
                  </Box>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                    <Box sx={{ width: 16, height: 16, bgcolor: 'grey.300', borderRadius: 0.5 }} />
                    <Typography variant="caption">Taken</Typography>
                  </Box>
                </Box>
              </Paper>
            )}

            {requiresSeats && !selectedSchedule && itemCount > 0 && (
              <Alert severity="info" sx={{ mb: 3 }}>
                Please select a schedule to choose your seats
              </Alert>
            )}

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

              {requiresSeats && selectedSeatIds.length > 0 && (
                <Box sx={{ mt: 1, mb: 1 }}>
                  <Typography variant="caption" color="text.secondary" fontWeight={600}>
                    Selected seats:
                  </Typography>
                  <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5, mt: 0.5 }}>
                    {selectedSeatIds.map((seatId) => {
                      const seat = filteredSeats.find((s) => s.id === seatId);
                      return (
                        <Chip
                          key={seatId}
                          label={seat ? `${seat.rowNumber}${seat.seatNumber}` : seatId}
                          size="small"
                          variant="outlined"
                          color="primary"
                          sx={{ height: 22 }}
                        />
                      );
                    })}
                  </Box>
                </Box>
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
                disabled={submitting || itemCount === 0 || (requiresSeats && selectedSeatIds.length !== requiredSeatCount)}
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
