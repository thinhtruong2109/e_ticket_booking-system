import { useState, useEffect } from 'react';
import {
  Typography,
  Box,
  Button,
  TextField,
  Paper,
  Card,
  CardContent,
  Alert,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Grid,
} from '@mui/material';
import { CheckCircle, Error as ErrorIcon, QrCodeScanner } from '@mui/icons-material';
import { useSnackbar } from 'notistack';
import { ticketApi, eventApi, scheduleApi } from '../../api';
import { getErrorMessage, formatDateTime } from '../../utils/helpers';

const OrganizerCheckinPage = () => {
  const { enqueueSnackbar } = useSnackbar();
  const [events, setEvents] = useState([]);
  const [schedules, setSchedules] = useState([]);
  const [selectedEvent, setSelectedEvent] = useState('');
  const [selectedSchedule, setSelectedSchedule] = useState('');
  const [ticketCode, setTicketCode] = useState('');
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    eventApi.getMyEvents().then((res) => setEvents(res.data || [])).catch(console.error);
  }, []);

  useEffect(() => {
    if (selectedEvent) {
      scheduleApi.getByEvent(selectedEvent).then((res) => {
        setSchedules(res.data || []);
        setSelectedSchedule('');
      }).catch(console.error);
    }
  }, [selectedEvent]);

  const handleCheckIn = async () => {
    if (!ticketCode.trim()) return;
    setSubmitting(true);
    setError('');
    setResult(null);
    try {
      const res = await ticketApi.checkIn({
        ticketCode: ticketCode.trim(),
        scheduleId: selectedSchedule || undefined,
      });
      setResult(res.data);
      enqueueSnackbar('Check-in successful!', { variant: 'success' });
      setTicketCode('');
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Box>
      <Typography variant="h5" fontWeight={700} sx={{ mb: 3 }}>
        <QrCodeScanner sx={{ mr: 1, verticalAlign: 'middle' }} />
        Event Check-in
      </Typography>

      {/* Event / Schedule Selection */}
      <Paper sx={{ p: 3, mb: 3 }}>
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, sm: 6 }}>
            <FormControl fullWidth>
              <InputLabel>Select Event</InputLabel>
              <Select
                value={selectedEvent}
                label="Select Event"
                onChange={(e) => setSelectedEvent(e.target.value)}
              >
                {events.map((ev) => (
                  <MenuItem key={ev.id} value={ev.id}>{ev.name}</MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <FormControl fullWidth disabled={!selectedEvent}>
              <InputLabel>Select Schedule</InputLabel>
              <Select
                value={selectedSchedule}
                label="Select Schedule"
                onChange={(e) => setSelectedSchedule(e.target.value)}
              >
                {schedules.map((s) => (
                  <MenuItem key={s.id} value={s.id}>
                    {formatDateTime(s.startTime)} — {formatDateTime(s.endTime)}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>
        </Grid>
      </Paper>

      {/* Ticket Code Input */}
      <Paper sx={{ p: 3, mb: 3 }}>
        <Typography variant="subtitle1" fontWeight={600} sx={{ mb: 2 }}>
          Enter Ticket Code
        </Typography>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <TextField
            fullWidth
            value={ticketCode}
            onChange={(e) => setTicketCode(e.target.value)}
            placeholder="e.g. TKT1A2B3C4D5E"
            onKeyDown={(e) => e.key === 'Enter' && handleCheckIn()}
            slotProps={{
              input: { sx: { fontFamily: 'monospace', fontSize: '1.1rem', letterSpacing: 1 } },
            }}
          />
          <Button
            variant="contained"
            onClick={handleCheckIn}
            disabled={submitting || !ticketCode.trim()}
            sx={{ minWidth: 120 }}
          >
            {submitting ? 'Checking...' : 'Check In'}
          </Button>
        </Box>
      </Paper>

      {/* Result */}
      {error && (
        <Alert severity="error" icon={<ErrorIcon />} sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {result && (
        <Card sx={{ border: '2px solid', borderColor: 'success.main' }}>
          <CardContent>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
              <CheckCircle color="success" sx={{ fontSize: 32 }} />
              <Typography variant="h6" fontWeight={700} color="success.main">
                Check-in Successful!
              </Typography>
            </Box>
            <Grid container spacing={2}>
              <Grid size={{ xs: 12, sm: 6 }}>
                <Typography variant="caption" color="text.secondary">Ticket Code</Typography>
                <Typography variant="body1" fontWeight={600} sx={{ fontFamily: 'monospace' }}>
                  {result.ticketCode}
                </Typography>
              </Grid>
              <Grid size={{ xs: 12, sm: 6 }}>
                <Typography variant="caption" color="text.secondary">Event</Typography>
                <Typography variant="body1" fontWeight={600}>{result.eventName}</Typography>
              </Grid>
              <Grid size={{ xs: 12, sm: 6 }}>
                <Typography variant="caption" color="text.secondary">Attendee</Typography>
                <Typography variant="body1">{result.ownerName || result.attendeeName || '-'}</Typography>
              </Grid>
              <Grid size={{ xs: 12, sm: 6 }}>
                <Typography variant="caption" color="text.secondary">Ticket Type</Typography>
                <Typography variant="body1">{result.ticketTypeName || '-'}</Typography>
              </Grid>
              {result.seatInfo && (
                <Grid size={{ xs: 12, sm: 6 }}>
                  <Typography variant="caption" color="text.secondary">Seat</Typography>
                  <Typography variant="body1">{result.seatInfo}</Typography>
                </Grid>
              )}
              <Grid size={{ xs: 12, sm: 6 }}>
                <Typography variant="caption" color="text.secondary">Checked In At</Typography>
                <Typography variant="body1">{formatDateTime(result.checkedInAt || new Date())}</Typography>
              </Grid>
            </Grid>
          </CardContent>
        </Card>
      )}
    </Box>
  );
};

export default OrganizerCheckinPage;
