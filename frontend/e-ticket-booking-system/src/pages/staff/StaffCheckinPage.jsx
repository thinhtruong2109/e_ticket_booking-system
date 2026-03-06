import { useState, useEffect } from 'react';
import {
  Typography,
  Box,
  Button,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Paper,
  Alert,
  CircularProgress,
} from '@mui/material';
import { QrCodeScanner, CheckCircle, Error as ErrorIcon } from '@mui/icons-material';
import { useSnackbar } from 'notistack';
import { eventApi, scheduleApi, bookingApi } from '../../api';
import { getErrorMessage } from '../../utils/helpers';

const StaffCheckinPage = () => {
  const { enqueueSnackbar } = useSnackbar();
  const [events, setEvents] = useState([]);
  const [schedules, setSchedules] = useState([]);
  const [selectedEvent, setSelectedEvent] = useState('');
  const [selectedSchedule, setSelectedSchedule] = useState('');
  const [ticketCode, setTicketCode] = useState('');
  const [checking, setChecking] = useState(false);
  const [result, setResult] = useState(null);

  useEffect(() => {
    const fetchEvents = async () => {
      try {
        const res = await eventApi.getAll();
        setEvents(res.data || []);
      } catch (err) {
        enqueueSnackbar(getErrorMessage(err), { variant: 'error' });
      }
    };
    fetchEvents();
  }, []);

  useEffect(() => {
    if (!selectedEvent) { setSchedules([]); setSelectedSchedule(''); return; }
    const fetchSchedules = async () => {
      try {
        const res = await scheduleApi.getByEvent(selectedEvent);
        setSchedules(res.data || []);
      } catch (err) {
        enqueueSnackbar(getErrorMessage(err), { variant: 'error' });
      }
    };
    fetchSchedules();
  }, [selectedEvent]);

  const handleCheckin = async () => {
    if (!ticketCode.trim() || !selectedSchedule) return;
    setChecking(true);
    setResult(null);
    try {
      const res = await bookingApi.checkin({ ticketCode: ticketCode.trim(), scheduleId: selectedSchedule });
      setResult({ success: true, message: res.message || 'Check-in successful!', data: res.data });
      setTicketCode('');
    } catch (err) {
      setResult({ success: false, message: getErrorMessage(err) });
    } finally {
      setChecking(false);
    }
  };

  return (
    <Box>
      <Typography variant="h5" fontWeight={700} gutterBottom>
        <QrCodeScanner sx={{ mr: 1, verticalAlign: 'middle' }} />
        Check-in Scanner
      </Typography>

      <Paper variant="outlined" sx={{ p: 3, maxWidth: 500 }}>
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5 }}>
          <FormControl fullWidth>
            <InputLabel>Event</InputLabel>
            <Select value={selectedEvent} label="Event" onChange={(e) => setSelectedEvent(e.target.value)}>
              {events.map((ev) => (
                <MenuItem key={ev.id} value={ev.id}>{ev.name}</MenuItem>
              ))}
            </Select>
          </FormControl>

          <FormControl fullWidth disabled={!selectedEvent}>
            <InputLabel>Schedule</InputLabel>
            <Select value={selectedSchedule} label="Schedule" onChange={(e) => setSelectedSchedule(e.target.value)}>
              {schedules.map((s) => (
                <MenuItem key={s.id} value={s.id}>
                  {new Date(s.startTime).toLocaleString()}
                </MenuItem>
              ))}
            </Select>
          </FormControl>

          <TextField
            label="Ticket Code"
            value={ticketCode}
            onChange={(e) => setTicketCode(e.target.value.toUpperCase())}
            onKeyDown={(e) => e.key === 'Enter' && handleCheckin()}
            placeholder="Enter ticket code"
            fullWidth
            disabled={!selectedSchedule}
            InputProps={{ sx: { fontFamily: 'monospace', fontSize: '1.1rem', letterSpacing: 2 } }}
          />

          <Button
            variant="contained"
            size="large"
            onClick={handleCheckin}
            disabled={!ticketCode.trim() || !selectedSchedule || checking}
            startIcon={checking ? <CircularProgress size={20} /> : <QrCodeScanner />}
          >
            {checking ? 'Checking...' : 'Check In'}
          </Button>

          {result && (
            <Alert
              severity={result.success ? 'success' : 'error'}
              icon={result.success ? <CheckCircle /> : <ErrorIcon />}
              sx={{ mt: 1 }}
            >
              <Typography variant="body2" fontWeight={600}>{result.message}</Typography>
              {result.data && (
                <Typography variant="caption" display="block" sx={{ mt: 0.5 }}>
                  {result.data.ticketHolder && `Holder: ${result.data.ticketHolder}`}
                  {result.data.seatLabel && ` | Seat: ${result.data.seatLabel}`}
                </Typography>
              )}
            </Alert>
          )}
        </Box>
      </Paper>
    </Box>
  );
};

export default StaffCheckinPage;
