import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Typography,
  Box,
  Button,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  FormControlLabel,
  Checkbox,
  Paper,
  Stepper,
  Step,
  StepLabel,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Grid,
  IconButton,
  Tooltip,
  Alert,
  Chip,
} from '@mui/material';
import { Add, Delete } from '@mui/icons-material';
import { useSnackbar } from 'notistack';
import { eventApi, categoryApi, venueApi, scheduleApi, ticketTypeApi, seatApi } from '../../api';
import { LoadingScreen, StatusChip } from '../../components/common';
import { formatDateTime, formatCurrency, getErrorMessage } from '../../utils/helpers';

const steps = ['Basic Info', 'Schedules', 'Ticket Types'];

const EventFormPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { enqueueSnackbar } = useSnackbar();
  const isEditing = Boolean(id);

  const [activeStep, setActiveStep] = useState(0);
  const [loading, setLoading] = useState(isEditing);
  const [saving, setSaving] = useState(false);
  const [categories, setCategories] = useState([]);
  const [venues, setVenues] = useState([]);
  const [schedules, setSchedules] = useState([]);
  const [ticketTypes, setTicketTypes] = useState([]);
  const [sections, setSections] = useState([]);
  const [eventId, setEventId] = useState(id ? parseInt(id) : null);

  // Form state
  const [form, setForm] = useState({
    name: '',
    description: '',
    categoryId: '',
    venueId: '',
    bannerImageUrl: '',
    thumbnailImageUrl: '',
    allowTicketExchange: true,
  });

  // Schedule dialog
  const [scheduleDialog, setScheduleDialog] = useState(false);
  const [scheduleForm, setScheduleForm] = useState({ startTime: '', endTime: '' });

  // Ticket type dialog
  const [ticketDialog, setTicketDialog] = useState(false);
  const [ticketForm, setTicketForm] = useState({
    name: '', description: '', price: '', totalQuantity: '', maxPerBooking: '', sectionId: '',
  });

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [catRes, venueRes] = await Promise.all([
          categoryApi.getAll(),
          venueApi.getAll(),
        ]);
        setCategories(catRes.data || []);
        setVenues(venueRes.data || []);

        if (isEditing) {
          const [eventRes, schedRes, ttRes] = await Promise.all([
            eventApi.getEventById(id),
            scheduleApi.getByEvent(id),
            ticketTypeApi.getByEvent(id),
          ]);
          const event = eventRes.data;
          setForm({
            name: event.name || '',
            description: event.description || '',
            categoryId: event.category?.id || '',
            venueId: event.venue?.id || '',
            bannerImageUrl: event.bannerImageUrl || '',
            thumbnailImageUrl: event.thumbnailImageUrl || '',
            allowTicketExchange: event.allowTicketExchange ?? true,
          });
          setSchedules(schedRes.data || []);
          setTicketTypes(ttRes.data || []);

          // Fetch sections for the venue
          if (event.venue?.id) {
            try {
              const secRes = await seatApi.getSectionsByVenue(event.venue.id);
              setSections(secRes.data || []);
            } catch { /* venue may have no sections */ }
          }
        }
      } catch (err) {
        enqueueSnackbar(getErrorMessage(err), { variant: 'error' });
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [id]);

  // Fetch sections when venue changes
  useEffect(() => {
    if (form.venueId) {
      seatApi.getSectionsByVenue(form.venueId)
        .then((res) => setSections(res.data || []))
        .catch(() => setSections([]));
    } else {
      setSections([]);
    }
  }, [form.venueId]);

  const handleSaveEvent = async () => {
    setSaving(true);
    try {
      if (isEditing) {
        await eventApi.updateEvent(eventId, form);
        enqueueSnackbar('Event updated successfully', { variant: 'success' });
      } else {
        const res = await eventApi.createEvent(form);
        const newId = res.data?.id;
        setEventId(newId);
        enqueueSnackbar('Event created! Now add schedules.', { variant: 'success' });
      }
      setActiveStep(1);
    } catch (err) {
      enqueueSnackbar(getErrorMessage(err), { variant: 'error' });
    } finally {
      setSaving(false);
    }
  };

  const handleAddSchedule = async () => {
    try {
      await scheduleApi.create({ eventId, ...scheduleForm });
      enqueueSnackbar('Schedule added', { variant: 'success' });
      const res = await scheduleApi.getByEvent(eventId);
      setSchedules(res.data || []);
      setScheduleDialog(false);
      setScheduleForm({ startTime: '', endTime: '' });
    } catch (err) {
      enqueueSnackbar(getErrorMessage(err), { variant: 'error' });
    }
  };

  const handleAddTicketType = async () => {
    try {
      await ticketTypeApi.create({
        eventId,
        name: ticketForm.name,
        description: ticketForm.description,
        price: parseFloat(ticketForm.price),
        totalQuantity: parseInt(ticketForm.totalQuantity),
        maxPerBooking: parseInt(ticketForm.maxPerBooking) || 10,
        sectionId: ticketForm.sectionId || undefined,
      });
      enqueueSnackbar('Ticket type added', { variant: 'success' });
      const res = await ticketTypeApi.getByEvent(eventId);
      setTicketTypes(res.data || []);
      setTicketDialog(false);
      setTicketForm({ name: '', description: '', price: '', totalQuantity: '', maxPerBooking: '', sectionId: '' });
    } catch (err) {
      enqueueSnackbar(getErrorMessage(err), { variant: 'error' });
    }
  };

  if (loading) return <LoadingScreen />;

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h5" fontWeight={700}>
          {isEditing ? 'Edit Event' : 'Create Event'}
        </Typography>
        <Button variant="outlined" onClick={() => navigate('/organizer/events')}>
          Back to Events
        </Button>
      </Box>

      <Stepper activeStep={activeStep} sx={{ mb: 4 }}>
        {steps.map((label) => (
          <Step key={label}>
            <StepLabel>{label}</StepLabel>
          </Step>
        ))}
      </Stepper>

      {/* Step 1: Basic Info */}
      {activeStep === 0 && (
        <Paper sx={{ p: 3 }}>
          <Grid container spacing={2}>
            <Grid size={{ xs: 12 }}>
              <TextField
                fullWidth
                label="Event Name"
                value={form.name}
                onChange={(e) => setForm({ ...form, name: e.target.value })}
                required
              />
            </Grid>
            <Grid size={{ xs: 12 }}>
              <TextField
                fullWidth
                label="Description"
                multiline
                rows={4}
                value={form.description}
                onChange={(e) => setForm({ ...form, description: e.target.value })}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <FormControl fullWidth>
                <InputLabel>Category</InputLabel>
                <Select
                  value={form.categoryId}
                  label="Category"
                  onChange={(e) => setForm({ ...form, categoryId: e.target.value })}
                >
                  {categories.map((c) => (
                    <MenuItem key={c.id} value={c.id}>{c.name}</MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <FormControl fullWidth>
                <InputLabel>Venue</InputLabel>
                <Select
                  value={form.venueId}
                  label="Venue"
                  onChange={(e) => setForm({ ...form, venueId: e.target.value })}
                >
                  {venues.map((v) => (
                    <MenuItem key={v.id} value={v.id}>{v.name} — {v.city}</MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                fullWidth
                label="Banner Image URL"
                value={form.bannerImageUrl}
                onChange={(e) => setForm({ ...form, bannerImageUrl: e.target.value })}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                fullWidth
                label="Thumbnail Image URL"
                value={form.thumbnailImageUrl}
                onChange={(e) => setForm({ ...form, thumbnailImageUrl: e.target.value })}
              />
            </Grid>
            <Grid size={{ xs: 12 }}>
              <FormControlLabel
                control={
                  <Checkbox
                    checked={form.allowTicketExchange}
                    onChange={(e) => setForm({ ...form, allowTicketExchange: e.target.checked })}
                  />
                }
                label="Allow Ticket Exchange (Secondary Market)"
              />
            </Grid>
          </Grid>
          <Box sx={{ mt: 3, display: 'flex', justifyContent: 'flex-end' }}>
            <Button variant="contained" onClick={handleSaveEvent} disabled={saving || !form.name}>
              {saving ? 'Saving...' : isEditing ? 'Update & Next' : 'Create & Next'}
            </Button>
          </Box>
        </Paper>
      )}

      {/* Step 2: Schedules */}
      {activeStep === 1 && (
        <Paper sx={{ p: 3 }}>
          {!eventId && (
            <Alert severity="warning" sx={{ mb: 2 }}>Please save the event first before adding schedules.</Alert>
          )}
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
            <Typography variant="h6">Schedules</Typography>
            <Button startIcon={<Add />} variant="outlined" onClick={() => setScheduleDialog(true)} disabled={!eventId}>
              Add Schedule
            </Button>
          </Box>
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Start Time</TableCell>
                  <TableCell>End Time</TableCell>
                  <TableCell>Status</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {schedules.map((s) => (
                  <TableRow key={s.id}>
                    <TableCell>{formatDateTime(s.startTime)}</TableCell>
                    <TableCell>{formatDateTime(s.endTime)}</TableCell>
                    <TableCell><StatusChip status={s.status} /></TableCell>
                  </TableRow>
                ))}
                {schedules.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={3} align="center" sx={{ py: 3, color: 'text.secondary' }}>
                      No schedules added yet
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </TableContainer>
          <Box sx={{ mt: 3, display: 'flex', justifyContent: 'space-between' }}>
            <Button onClick={() => setActiveStep(0)}>Back</Button>
            <Button variant="contained" onClick={() => setActiveStep(2)}>Next</Button>
          </Box>

          {/* Add Schedule Dialog */}
          <Dialog open={scheduleDialog} onClose={() => setScheduleDialog(false)} maxWidth="sm" fullWidth>
            <DialogTitle>Add Schedule</DialogTitle>
            <DialogContent>
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
                <TextField
                  label="Start Time"
                  type="datetime-local"
                  value={scheduleForm.startTime}
                  onChange={(e) => setScheduleForm({ ...scheduleForm, startTime: e.target.value })}
                  slotProps={{ inputLabel: { shrink: true } }}
                  fullWidth
                />
                <TextField
                  label="End Time"
                  type="datetime-local"
                  value={scheduleForm.endTime}
                  onChange={(e) => setScheduleForm({ ...scheduleForm, endTime: e.target.value })}
                  slotProps={{ inputLabel: { shrink: true } }}
                  fullWidth
                />
              </Box>
            </DialogContent>
            <DialogActions>
              <Button onClick={() => setScheduleDialog(false)}>Cancel</Button>
              <Button
                variant="contained"
                onClick={handleAddSchedule}
                disabled={!scheduleForm.startTime || !scheduleForm.endTime}
              >
                Add
              </Button>
            </DialogActions>
          </Dialog>
        </Paper>
      )}

      {/* Step 3: Ticket Types */}
      {activeStep === 2 && (
        <Paper sx={{ p: 3 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
            <Typography variant="h6">Ticket Types</Typography>
            <Button startIcon={<Add />} variant="outlined" onClick={() => setTicketDialog(true)} disabled={!eventId}>
              Add Ticket Type
            </Button>
          </Box>
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Name</TableCell>
                  <TableCell>Section</TableCell>
                  <TableCell>Price</TableCell>
                  <TableCell>Total Qty</TableCell>
                  <TableCell>Available</TableCell>
                  <TableCell>Max/Booking</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {ticketTypes.map((tt) => (
                  <TableRow key={tt.id}>
                    <TableCell>{tt.name}</TableCell>
                    <TableCell>
                      {tt.sectionName ? (
                        <Chip label={tt.sectionName} size="small" variant="outlined" color={tt.hasNumberedSeats ? 'info' : 'default'} />
                      ) : (
                        <Typography variant="caption" color="text.secondary">None (General)</Typography>
                      )}
                    </TableCell>
                    <TableCell>{formatCurrency(tt.price)}</TableCell>
                    <TableCell>{tt.totalQuantity}</TableCell>
                    <TableCell>{tt.availableQuantity}</TableCell>
                    <TableCell>{tt.maxPerBooking}</TableCell>
                  </TableRow>
                ))}
                {ticketTypes.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={6} align="center" sx={{ py: 3, color: 'text.secondary' }}>
                      No ticket types added yet
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </TableContainer>
          <Box sx={{ mt: 3, display: 'flex', justifyContent: 'space-between' }}>
            <Button onClick={() => setActiveStep(1)}>Back</Button>
            <Button variant="contained" onClick={() => navigate('/organizer/events')}>
              Done
            </Button>
          </Box>

          {/* Add Ticket Type Dialog */}
          <Dialog open={ticketDialog} onClose={() => setTicketDialog(false)} maxWidth="sm" fullWidth>
            <DialogTitle>Add Ticket Type</DialogTitle>
            <DialogContent>
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
                <TextField
                  label="Name"
                  value={ticketForm.name}
                  onChange={(e) => setTicketForm({ ...ticketForm, name: e.target.value })}
                  fullWidth
                />
                <TextField
                  label="Description"
                  value={ticketForm.description}
                  onChange={(e) => setTicketForm({ ...ticketForm, description: e.target.value })}
                  fullWidth
                />
                <FormControl fullWidth>
                  <InputLabel>Section (Optional)</InputLabel>
                  <Select
                    value={ticketForm.sectionId}
                    label="Section (Optional)"
                    onChange={(e) => setTicketForm({ ...ticketForm, sectionId: e.target.value })}
                  >
                    <MenuItem value="">
                      <em>None — General admission (no specific section)</em>
                    </MenuItem>
                    {sections.map((sec) => (
                      <MenuItem key={sec.id} value={sec.id}>
                        {sec.name}
                        {sec.hasNumberedSeats && ' (Numbered Seats)'}
                        {sec.capacity ? ` — Capacity: ${sec.capacity}` : ''}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
                {ticketForm.sectionId && sections.find(s => s.id === ticketForm.sectionId)?.hasNumberedSeats && (
                  <Alert severity="info" sx={{ py: 0.5 }}>
                    This section has numbered seats. Customers will need to select specific seats when booking.
                  </Alert>
                )}
                {sections.length === 0 && (
                  <Alert severity="warning" sx={{ py: 0.5 }}>
                    No sections found for this venue. You can still create a general admission ticket type.
                  </Alert>
                )}
                <TextField
                  label="Price (VND)"
                  type="number"
                  value={ticketForm.price}
                  onChange={(e) => setTicketForm({ ...ticketForm, price: e.target.value })}
                  fullWidth
                />
                <TextField
                  label="Total Quantity"
                  type="number"
                  value={ticketForm.totalQuantity}
                  onChange={(e) => setTicketForm({ ...ticketForm, totalQuantity: e.target.value })}
                  fullWidth
                />
                <TextField
                  label="Max Per Booking"
                  type="number"
                  value={ticketForm.maxPerBooking}
                  onChange={(e) => setTicketForm({ ...ticketForm, maxPerBooking: e.target.value })}
                  fullWidth
                />
              </Box>
            </DialogContent>
            <DialogActions>
              <Button onClick={() => setTicketDialog(false)}>Cancel</Button>
              <Button
                variant="contained"
                onClick={handleAddTicketType}
                disabled={!ticketForm.name || !ticketForm.price || !ticketForm.totalQuantity}
              >
                Add
              </Button>
            </DialogActions>
          </Dialog>
        </Paper>
      )}
    </Box>
  );
};

export default EventFormPage;
