import { useState, useEffect,  } from 'react';
import {
  Container,
  Grid,
  Paper,
  Typography,
  Box,
  Button,
  Tabs,
  Tab,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  IconButton,
  Card,
  CardContent,
} from '@mui/material';
import {
  Event,
  Add,
  Edit,
  Delete,
  Publish,
  Cancel,
  ConfirmationNumber,
  Schedule,
  LocalOffer,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { eventApi, scheduleApi, ticketTypeApi, venueApi, promoCodeApi } from '../../api';
import { PageHeader, LoadingScreen, ErrorAlert, StatusChip } from '../../components/common';
import { formatDateTime, formatCurrency, getErrorMessage } from '../../utils/helpers';

const OrganizerDashboard = () => {
  const [tab, setTab] = useState(0);

  return (
    <>
      <PageHeader title="Organizer Dashboard" subtitle="Manage your events" chip="Organizer" />
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Tabs
          value={tab}
          onChange={(e, v) => setTab(v)}
          sx={{ mb: 3, borderBottom: 1, borderColor: 'divider' }}
        >
          <Tab icon={<Event />} label="My Events" iconPosition="start" />
          <Tab icon={<LocalOffer />} label="Promo Codes" iconPosition="start" />
        </Tabs>
        {tab === 0 && <MyEventsTab />}
        {tab === 1 && <OrganizerPromosTab />}
      </Container>
    </>
  );
};

/* ---- MY EVENTS TAB ---- */
const MyEventsTab = () => {
  const navigate = useNavigate();
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [eventDialog, setEventDialog] = useState({ open: false, mode: 'create', data: {}, id: null });
  const [scheduleDialog, setScheduleDialog] = useState({ open: false, eventId: null, data: { startTime: '', endTime: '' }, id: null, mode: 'create' });
  const [ticketTypeDialog, setTicketTypeDialog] = useState({ open: false, scheduleId: null, data: { name: '', price: '', totalQuantity: '', seatType: 'NONE' }, id: null, mode: 'create' });
  const [selectedEvent, setSelectedEvent] = useState(null);
  const [schedules, setSchedules] = useState([]);
  const [ticketTypes, setTicketTypes] = useState([]);
  const [venues, setVenues] = useState([]);

  useEffect(() => { fetchEvents(); fetchVenues(); }, []);

  const fetchEvents = async () => {
    setLoading(true);
    try {
      const res = await eventApi.getMyEvents();
      setEvents(Array.isArray(res.data) ? res.data : []);
    } catch (err) { setError(getErrorMessage(err)); }
    finally { setLoading(false); }
  };

  const fetchVenues = async () => {
    try { const res = await venueApi.getAll(); setVenues(Array.isArray(res.data) ? res.data : []); }
    catch (err) { /* ignore */ }
  };

  const fetchSchedules = async (eventId) => {
    try {
      const res = await scheduleApi.getByEvent(eventId);
      setSchedules(Array.isArray(res.data) ? res.data : []);
    } catch (err) { setError(getErrorMessage(err)); }
  };

  const fetchTicketTypes = async (scheduleId) => {
    try {
      const res = await ticketTypeApi.getBySchedule(scheduleId);
      setTicketTypes(Array.isArray(res.data) ? res.data : []);
    } catch (err) { setError(getErrorMessage(err)); }
  };

  const handleEventSave = async () => {
    try {
      if (eventDialog.mode === 'create') {
        await eventApi.create(eventDialog.data);
      } else {
        await eventApi.update(eventDialog.id, eventDialog.data);
      }
      setEventDialog({ open: false, mode: 'create', data: {}, id: null });
      fetchEvents();
    } catch (err) { setError(getErrorMessage(err)); }
  };

  const handlePublish = async (id) => {
    try { await eventApi.publish(id); fetchEvents(); }
    catch (err) { setError(getErrorMessage(err)); }
  };

  const handleCancel = async (id) => {
    if (!window.confirm('Cancel this event?')) return;
    try { await eventApi.cancel(id); fetchEvents(); }
    catch (err) { setError(getErrorMessage(err)); }
  };

  const handleScheduleSave = async () => {
    try {
      const payload = { eventId: scheduleDialog.eventId, ...scheduleDialog.data };
      if (scheduleDialog.mode === 'create') {
        await scheduleApi.create(payload);
      } else {
        await scheduleApi.update(scheduleDialog.id, payload);
      }
      setScheduleDialog({ open: false, eventId: null, data: { startTime: '', endTime: '' }, id: null, mode: 'create' });
      if (selectedEvent) fetchSchedules(selectedEvent.id);
    } catch (err) { setError(getErrorMessage(err)); }
  };

  const handleTicketTypeSave = async () => {
    try {
      const payload = { scheduleId: ticketTypeDialog.scheduleId, ...ticketTypeDialog.data, price: parseFloat(ticketTypeDialog.data.price), totalQuantity: parseInt(ticketTypeDialog.data.totalQuantity) };
      if (ticketTypeDialog.mode === 'create') {
        await ticketTypeApi.create(payload);
      } else {
        await ticketTypeApi.update(ticketTypeDialog.id, payload);
      }
      setTicketTypeDialog({ open: false, scheduleId: null, data: { name: '', price: '', totalQuantity: '', seatType: 'NONE' }, id: null, mode: 'create' });
      if (ticketTypeDialog.scheduleId) fetchTicketTypes(ticketTypeDialog.scheduleId);
    } catch (err) { setError(getErrorMessage(err)); }
  };

  const selectEvent = (ev) => {
    setSelectedEvent(ev);
    setSchedules([]);
    setTicketTypes([]);
    fetchSchedules(ev.id);
  };

  if (loading) return <LoadingScreen />;

  return (
    <>
      {error && <ErrorAlert message={error} />}

      {!selectedEvent ? (
        <>
          <Box sx={{ mb: 2 }}>
            <Button variant="contained" startIcon={<Add />} onClick={() => setEventDialog({
              open: true, mode: 'create', id: null,
              data: { name: '', description: '', venueId: '', categoryId: '', bannerUrl: '', thumbnailUrl: '' }
            })}>
              Create Event
            </Button>
          </Box>

          <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Name</TableCell>
                  <TableCell>Venue</TableCell>
                  <TableCell align="right">Tickets</TableCell>
                  <TableCell align="center">Status</TableCell>
                  <TableCell align="right">Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {events.map((ev) => (
                  <TableRow key={ev.id} hover>
                    <TableCell>
                      <Typography variant="body2" fontWeight={600} sx={{ cursor: 'pointer', '&:hover': { textDecoration: 'underline' } }} onClick={() => selectEvent(ev)}>
                        {ev.name}
                      </Typography>
                    </TableCell>
                    <TableCell>{ev.venueName || '-'}</TableCell>
                    <TableCell align="right">{ev.availableTickets}/{ev.totalTickets}</TableCell>
                    <TableCell align="center"><StatusChip status={ev.status} /></TableCell>
                    <TableCell align="right">
                      <Box sx={{ display: 'flex', gap: 0.5, justifyContent: 'flex-end' }}>
                        {ev.status === 'DRAFT' && (
                          <>
                            <IconButton size="small" onClick={() => setEventDialog({ open: true, mode: 'edit', id: ev.id, data: { name: ev.name, description: ev.description || '', venueId: ev.venueId || '', categoryId: ev.categoryId || '', bannerUrl: ev.bannerUrl || '', thumbnailUrl: ev.thumbnailUrl || '' } })}>
                              <Edit fontSize="small" />
                            </IconButton>
                            <IconButton size="small" color="success" onClick={() => handlePublish(ev.id)} title="Publish">
                              <Publish fontSize="small" />
                            </IconButton>
                          </>
                        )}
                        {(ev.status === 'PUBLISHED' || ev.status === 'DRAFT') && (
                          <IconButton size="small" color="error" onClick={() => handleCancel(ev.id)} title="Cancel">
                            <Cancel fontSize="small" />
                          </IconButton>
                        )}
                      </Box>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </>
      ) : (
        /* ---- Event Detail View: Schedules & Ticket Types ---- */
        <>
          <Box sx={{ mb: 2, display: 'flex', gap: 2, alignItems: 'center' }}>
            <Button variant="outlined" onClick={() => { setSelectedEvent(null); setSchedules([]); setTicketTypes([]); }}>&larr; Back</Button>
            <Typography variant="h6" fontWeight={700}>{selectedEvent.name}</Typography>
            <StatusChip status={selectedEvent.status} />
          </Box>

          <Paper sx={{ p: 3, mb: 3 }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
              <Typography variant="subtitle1" fontWeight={700}><Schedule sx={{ verticalAlign: 'middle', mr: 1 }} />Schedules</Typography>
              <Button size="small" variant="contained" startIcon={<Add />} onClick={() => setScheduleDialog({ open: true, eventId: selectedEvent.id, data: { startTime: '', endTime: '' }, id: null, mode: 'create' })}>
                Add Schedule
              </Button>
            </Box>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>ID</TableCell>
                  <TableCell>Start</TableCell>
                  <TableCell>End</TableCell>
                  <TableCell align="right">Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {schedules.map((s) => (
                  <TableRow key={s.id} hover>
                    <TableCell>{s.id}</TableCell>
                    <TableCell>{formatDateTime(s.startTime)}</TableCell>
                    <TableCell>{formatDateTime(s.endTime)}</TableCell>
                    <TableCell align="right">
                      <Button size="small" startIcon={<ConfirmationNumber />} onClick={() => { fetchTicketTypes(s.id); }}>
                        Ticket Types
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </Paper>

          {ticketTypes.length > 0 && (
            <Paper sx={{ p: 3 }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Typography variant="subtitle1" fontWeight={700}><ConfirmationNumber sx={{ verticalAlign: 'middle', mr: 1 }} />Ticket Types</Typography>
                <Button size="small" variant="contained" startIcon={<Add />} onClick={() => setTicketTypeDialog({ open: true, scheduleId: ticketTypes[0]?.scheduleId, data: { name: '', price: '', totalQuantity: '', seatType: 'NONE' }, id: null, mode: 'create' })}>
                  Add Ticket Type
                </Button>
              </Box>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Name</TableCell>
                    <TableCell align="right">Price</TableCell>
                    <TableCell align="right">Remaining</TableCell>
                    <TableCell>Seat Type</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {ticketTypes.map((tt) => (
                    <TableRow key={tt.id} hover>
                      <TableCell><Typography variant="body2" fontWeight={600}>{tt.name}</Typography></TableCell>
                      <TableCell align="right">{formatCurrency(tt.price)}</TableCell>
                      <TableCell align="right">{tt.remainingQuantity}/{tt.totalQuantity}</TableCell>
                      <TableCell>{tt.seatType}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </Paper>
          )}
        </>
      )}

      {/* Event Dialog */}
      <Dialog open={eventDialog.open} onClose={() => setEventDialog({ ...eventDialog, open: false })} maxWidth="sm" fullWidth>
        <DialogTitle>{eventDialog.mode === 'create' ? 'Create Event' : 'Edit Event'}</DialogTitle>
        <DialogContent sx={{ pt: '8px !important' }}>
          <Grid container spacing={2}>
            <Grid item xs={12}>
              <TextField label="Event Name" fullWidth required value={eventDialog.data.name || ''}
                onChange={(e) => setEventDialog({ ...eventDialog, data: { ...eventDialog.data, name: e.target.value } })} />
            </Grid>
            <Grid item xs={12}>
              <TextField label="Description" fullWidth multiline rows={3} value={eventDialog.data.description || ''}
                onChange={(e) => setEventDialog({ ...eventDialog, data: { ...eventDialog.data, description: e.target.value } })} />
            </Grid>
            <Grid item xs={6}>
              <FormControl fullWidth>
                <InputLabel>Venue</InputLabel>
                <Select value={eventDialog.data.venueId || ''}
                  onChange={(e) => setEventDialog({ ...eventDialog, data: { ...eventDialog.data, venueId: e.target.value } })} label="Venue">
                  {venues.map((v) => <MenuItem key={v.id} value={v.id}>{v.name}</MenuItem>)}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={6}>
              <TextField label="Category ID" fullWidth value={eventDialog.data.categoryId || ''}
                onChange={(e) => setEventDialog({ ...eventDialog, data: { ...eventDialog.data, categoryId: e.target.value } })} />
            </Grid>
            <Grid item xs={6}>
              <TextField label="Banner URL" fullWidth value={eventDialog.data.bannerUrl || ''}
                onChange={(e) => setEventDialog({ ...eventDialog, data: { ...eventDialog.data, bannerUrl: e.target.value } })} />
            </Grid>
            <Grid item xs={6}>
              <TextField label="Thumbnail URL" fullWidth value={eventDialog.data.thumbnailUrl || ''}
                onChange={(e) => setEventDialog({ ...eventDialog, data: { ...eventDialog.data, thumbnailUrl: e.target.value } })} />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setEventDialog({ ...eventDialog, open: false })}>Cancel</Button>
          <Button variant="contained" onClick={handleEventSave}>Save</Button>
        </DialogActions>
      </Dialog>

      {/* Schedule Dialog */}
      <Dialog open={scheduleDialog.open} onClose={() => setScheduleDialog({ ...scheduleDialog, open: false })} maxWidth="sm" fullWidth>
        <DialogTitle>{scheduleDialog.mode === 'create' ? 'Add Schedule' : 'Edit Schedule'}</DialogTitle>
        <DialogContent sx={{ pt: '8px !important' }}>
          <Grid container spacing={2}>
            <Grid item xs={6}>
              <TextField label="Start Time" type="datetime-local" fullWidth value={scheduleDialog.data.startTime}
                onChange={(e) => setScheduleDialog({ ...scheduleDialog, data: { ...scheduleDialog.data, startTime: e.target.value } })} InputLabelProps={{ shrink: true }} />
            </Grid>
            <Grid item xs={6}>
              <TextField label="End Time" type="datetime-local" fullWidth value={scheduleDialog.data.endTime}
                onChange={(e) => setScheduleDialog({ ...scheduleDialog, data: { ...scheduleDialog.data, endTime: e.target.value } })} InputLabelProps={{ shrink: true }} />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setScheduleDialog({ ...scheduleDialog, open: false })}>Cancel</Button>
          <Button variant="contained" onClick={handleScheduleSave}>Save</Button>
        </DialogActions>
      </Dialog>

      {/* Ticket Type Dialog */}
      <Dialog open={ticketTypeDialog.open} onClose={() => setTicketTypeDialog({ ...ticketTypeDialog, open: false })} maxWidth="sm" fullWidth>
        <DialogTitle>{ticketTypeDialog.mode === 'create' ? 'Add Ticket Type' : 'Edit Ticket Type'}</DialogTitle>
        <DialogContent sx={{ pt: '8px !important' }}>
          <Grid container spacing={2}>
            <Grid item xs={6}>
              <TextField label="Name" fullWidth required value={ticketTypeDialog.data.name}
                onChange={(e) => setTicketTypeDialog({ ...ticketTypeDialog, data: { ...ticketTypeDialog.data, name: e.target.value } })} />
            </Grid>
            <Grid item xs={6}>
              <TextField label="Price" type="number" fullWidth required value={ticketTypeDialog.data.price}
                onChange={(e) => setTicketTypeDialog({ ...ticketTypeDialog, data: { ...ticketTypeDialog.data, price: e.target.value } })} />
            </Grid>
            <Grid item xs={6}>
              <TextField label="Total Quantity" type="number" fullWidth required value={ticketTypeDialog.data.totalQuantity}
                onChange={(e) => setTicketTypeDialog({ ...ticketTypeDialog, data: { ...ticketTypeDialog.data, totalQuantity: e.target.value } })} />
            </Grid>
            <Grid item xs={6}>
              <FormControl fullWidth>
                <InputLabel>Seat Type</InputLabel>
                <Select value={ticketTypeDialog.data.seatType} label="Seat Type"
                  onChange={(e) => setTicketTypeDialog({ ...ticketTypeDialog, data: { ...ticketTypeDialog.data, seatType: e.target.value } })}>
                  <MenuItem value="NONE">None</MenuItem>
                  <MenuItem value="NUMBERED">Numbered</MenuItem>
                  <MenuItem value="GENERAL_ADMISSION">General Admission</MenuItem>
                </Select>
              </FormControl>
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setTicketTypeDialog({ ...ticketTypeDialog, open: false })}>Cancel</Button>
          <Button variant="contained" onClick={handleTicketTypeSave}>Save</Button>
        </DialogActions>
      </Dialog>
    </>
  );
};

/* ---- ORGANIZER PROMO CODES TAB ---- */
const OrganizerPromosTab = () => {
  const [promos, setPromos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [dialog, setDialog] = useState({ open: false, data: {} });

  useEffect(() => { fetchPromos(); }, []);

  const fetchPromos = async () => {
    setLoading(true);
    try {
      const res = await promoCodeApi.organizerGetMine();
      setPromos(Array.isArray(res.data) ? res.data : []);
    } catch (err) { setError(getErrorMessage(err)); }
    finally { setLoading(false); }
  };

  const handleCreate = async () => {
    try {
      await promoCodeApi.organizerCreate(dialog.data);
      setDialog({ open: false, data: {} });
      fetchPromos();
    } catch (err) { setError(getErrorMessage(err)); }
  };

  const handleDeactivate = async (id) => {
    try { await promoCodeApi.organizerDeactivate(id); fetchPromos(); }
    catch (err) { setError(getErrorMessage(err)); }
  };

  if (loading) return <LoadingScreen />;

  return (
    <>
      {error && <ErrorAlert message={error} />}
      <Box sx={{ mb: 2 }}>
        <Button variant="contained" startIcon={<Add />} onClick={() => setDialog({
          open: true,
          data: { code: '', description: '', discountType: 'PERCENTAGE', discountValue: 0, usageLimit: 100, validFrom: '', validTo: '', applicationType: 'ORGANIZER_ALL', eventIds: '' }
        })}>
          Create Promo
        </Button>
      </Box>
      <TableContainer component={Paper}>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Code</TableCell>
              <TableCell>Type</TableCell>
              <TableCell>Discount</TableCell>
              <TableCell>Usage</TableCell>
              <TableCell>Scope</TableCell>
              <TableCell align="center">Status</TableCell>
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {promos.map((p) => (
              <TableRow key={p.id} hover>
                <TableCell><Typography variant="body2" fontWeight={600} fontFamily="monospace">{p.code}</Typography></TableCell>
                <TableCell>{p.discountType}</TableCell>
                <TableCell>{p.discountType === 'PERCENTAGE' ? `${p.discountValue}%` : formatCurrency(p.discountValue)}</TableCell>
                <TableCell>{p.usedCount}/{p.usageLimit || '∞'}</TableCell>
                <TableCell><Chip label={p.applicationType} size="small" variant="outlined" /></TableCell>
                <TableCell align="center"><StatusChip status={p.status} /></TableCell>
                <TableCell align="right">
                  {p.status === 'ACTIVE' && (
                    <Button size="small" color="error" onClick={() => handleDeactivate(p.id)}>Deactivate</Button>
                  )}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={dialog.open} onClose={() => setDialog({ open: false, data: {} })} maxWidth="sm" fullWidth>
        <DialogTitle>Create Promo Code</DialogTitle>
        <DialogContent sx={{ pt: '8px !important' }}>
          <Grid container spacing={2}>
            <Grid item xs={6}>
              <TextField label="Code" fullWidth required value={dialog.data.code || ''}
                onChange={(e) => setDialog({ ...dialog, data: { ...dialog.data, code: e.target.value } })} />
            </Grid>
            <Grid item xs={6}>
              <FormControl fullWidth>
                <InputLabel>Scope</InputLabel>
                <Select value={dialog.data.applicationType || 'ORGANIZER_ALL'}
                  onChange={(e) => setDialog({ ...dialog, data: { ...dialog.data, applicationType: e.target.value } })} label="Scope">
                  <MenuItem value="ORGANIZER_ALL">All My Events</MenuItem>
                  <MenuItem value="SPECIFIC_EVENTS">Specific Events</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={6}>
              <FormControl fullWidth>
                <InputLabel>Discount Type</InputLabel>
                <Select value={dialog.data.discountType || 'PERCENTAGE'}
                  onChange={(e) => setDialog({ ...dialog, data: { ...dialog.data, discountType: e.target.value } })} label="Discount Type">
                  <MenuItem value="PERCENTAGE">Percentage</MenuItem>
                  <MenuItem value="FIXED_AMOUNT">Fixed Amount</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={6}>
              <TextField label="Discount Value" type="number" fullWidth value={dialog.data.discountValue || ''}
                onChange={(e) => setDialog({ ...dialog, data: { ...dialog.data, discountValue: parseFloat(e.target.value) } })} />
            </Grid>
            <Grid item xs={12}>
              <TextField label="Description" fullWidth value={dialog.data.description || ''}
                onChange={(e) => setDialog({ ...dialog, data: { ...dialog.data, description: e.target.value } })} />
            </Grid>
            {dialog.data.applicationType === 'SPECIFIC_EVENTS' && (
              <Grid item xs={12}>
                <TextField label="Event IDs (comma separated)" fullWidth value={dialog.data.eventIds || ''}
                  onChange={(e) => setDialog({ ...dialog, data: { ...dialog.data, eventIds: e.target.value } })} />
              </Grid>
            )}
            <Grid item xs={6}>
              <TextField label="Valid From" type="datetime-local" fullWidth value={dialog.data.validFrom || ''}
                onChange={(e) => setDialog({ ...dialog, data: { ...dialog.data, validFrom: e.target.value } })} InputLabelProps={{ shrink: true }} />
            </Grid>
            <Grid item xs={6}>
              <TextField label="Valid To" type="datetime-local" fullWidth value={dialog.data.validTo || ''}
                onChange={(e) => setDialog({ ...dialog, data: { ...dialog.data, validTo: e.target.value } })} InputLabelProps={{ shrink: true }} />
            </Grid>
            <Grid item xs={6}>
              <TextField label="Usage Limit" type="number" fullWidth value={dialog.data.usageLimit || ''}
                onChange={(e) => setDialog({ ...dialog, data: { ...dialog.data, usageLimit: parseInt(e.target.value) } })} />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialog({ open: false, data: {} })}>Cancel</Button>
          <Button variant="contained" onClick={handleCreate}>Create</Button>
        </DialogActions>
      </Dialog>
    </>
  );
};

export default OrganizerDashboard;
