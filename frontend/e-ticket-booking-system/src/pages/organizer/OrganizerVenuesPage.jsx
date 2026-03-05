import { useState, useEffect } from 'react';
import {
  Typography,
  Box,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  IconButton,
  Tooltip,
  Collapse,
  Chip,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
} from '@mui/material';
import { Add, Edit, Delete, ExpandMore, ExpandLess, Chair } from '@mui/icons-material';
import { useSnackbar } from 'notistack';
import { venueApi, seatApi } from '../../api';
import { LoadingScreen } from '../../components/common';
import { getErrorMessage } from '../../utils/helpers';

const OrganizerVenuesPage = () => {
  const { enqueueSnackbar } = useSnackbar();
  const [venues, setVenues] = useState([]);
  const [loading, setLoading] = useState(true);

  // Venue dialog
  const [venueDialog, setVenueDialog] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [venueForm, setVenueForm] = useState({ name: '', address: '', city: '', country: '', totalCapacity: '' });

  // Sections expansion
  const [expandedVenue, setExpandedVenue] = useState(null);
  const [sections, setSections] = useState([]);
  const [sectionsLoading, setSectionsLoading] = useState(false);

  // Section dialog
  const [sectionDialog, setSectionDialog] = useState(false);
  const [editingSectionId, setEditingSectionId] = useState(null);
  const [sectionForm, setSectionForm] = useState({ name: '', capacity: '', hasNumberedSeats: true });
  const [selectedVenueId, setSelectedVenueId] = useState(null);

  // Bulk seat dialog
  const [seatDialog, setSeatDialog] = useState(false);
  const [seatForm, setSeatForm] = useState({
    venueId: '',
    sectionId: '',
    rows: [{ rowLabel: 'A', startNumber: 1, endNumber: 20, seatType: 'REGULAR' }],
  });

  // Seats view
  const [seatsDialog, setSeatsDialog] = useState(false);
  const [seats, setSeats] = useState([]);
  const [seatsLoading, setSeatsLoading] = useState(false);
  const [viewingVenueName, setViewingVenueName] = useState('');

  const fetchVenues = async () => {
    try {
      const res = await venueApi.getAll();
      setVenues(res.data || []);
    } catch (err) {
      enqueueSnackbar(getErrorMessage(err), { variant: 'error' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchVenues(); }, []);

  const handleSaveVenue = async () => {
    try {
      const payload = { ...venueForm, totalCapacity: parseInt(venueForm.totalCapacity) || null };
      if (editingId) {
        await venueApi.update(editingId, payload);
        enqueueSnackbar('Venue updated', { variant: 'success' });
      } else {
        await venueApi.create(payload);
        enqueueSnackbar('Venue created', { variant: 'success' });
      }
      setVenueDialog(false);
      resetVenueForm();
      fetchVenues();
    } catch (err) {
      enqueueSnackbar(getErrorMessage(err), { variant: 'error' });
    }
  };

  const handleDeleteVenue = async (id) => {
    if (!window.confirm('Are you sure you want to delete this venue and all its sections/seats?')) return;
    try {
      await venueApi.delete(id);
      enqueueSnackbar('Venue deleted', { variant: 'success' });
      fetchVenues();
    } catch (err) {
      enqueueSnackbar(getErrorMessage(err), { variant: 'error' });
    }
  };

  const toggleExpand = async (venueId) => {
    if (expandedVenue === venueId) {
      setExpandedVenue(null);
      return;
    }
    setExpandedVenue(venueId);
    await refreshSections(venueId);
  };

  const refreshSections = async (venueId) => {
    setSectionsLoading(true);
    try {
      const res = await seatApi.getSectionsByVenue(venueId);
      setSections(res.data || []);
    } catch (err) {
      enqueueSnackbar(getErrorMessage(err), { variant: 'error' });
    } finally {
      setSectionsLoading(false);
    }
  };

  const handleSaveSection = async () => {
    try {
      const payload = {
        venueId: selectedVenueId,
        name: sectionForm.name,
        capacity: parseInt(sectionForm.capacity) || null,
        hasNumberedSeats: sectionForm.hasNumberedSeats,
      };
      if (editingSectionId) {
        await seatApi.updateSection(editingSectionId, payload);
        enqueueSnackbar('Section updated', { variant: 'success' });
      } else {
        await seatApi.createSection(payload);
        enqueueSnackbar('Section added', { variant: 'success' });
      }
      setSectionDialog(false);
      resetSectionForm();
      await refreshSections(selectedVenueId);
    } catch (err) {
      enqueueSnackbar(getErrorMessage(err), { variant: 'error' });
    }
  };

  const handleDeleteSection = async (sectionId) => {
    if (!window.confirm('Delete this section and all its seats?')) return;
    try {
      await seatApi.deleteSection(sectionId);
      enqueueSnackbar('Section deleted', { variant: 'success' });
      await refreshSections(expandedVenue);
    } catch (err) {
      enqueueSnackbar(getErrorMessage(err), { variant: 'error' });
    }
  };

  const openEditSection = (s, venueId) => {
    setEditingSectionId(s.id);
    setSelectedVenueId(venueId);
    setSectionForm({ name: s.name || '', capacity: s.capacity?.toString() || '', hasNumberedSeats: s.hasNumberedSeats ?? true });
    setSectionDialog(true);
  };

  const resetSectionForm = () => {
    setEditingSectionId(null);
    setSectionForm({ name: '', capacity: '', hasNumberedSeats: true });
  };

  const handleBulkCreateSeats = async () => {
    try {
      await seatApi.bulkCreate(seatForm);
      enqueueSnackbar('Seats created successfully', { variant: 'success' });
      setSeatDialog(false);
    } catch (err) {
      enqueueSnackbar(getErrorMessage(err), { variant: 'error' });
    }
  };

  const handleViewSeats = async (venue) => {
    setViewingVenueName(venue.name);
    setSeatsLoading(true);
    setSeatsDialog(true);
    try {
      const res = await seatApi.getSeatsByVenue(venue.id);
      setSeats(res.data || []);
    } catch (err) {
      enqueueSnackbar(getErrorMessage(err), { variant: 'error' });
    } finally {
      setSeatsLoading(false);
    }
  };

  const openEditVenue = (v) => {
    setEditingId(v.id);
    setVenueForm({ name: v.name || '', address: v.address || '', city: v.city || '', country: v.country || '', totalCapacity: v.totalCapacity?.toString() || '' });
    setVenueDialog(true);
  };

  const resetVenueForm = () => {
    setEditingId(null);
    setVenueForm({ name: '', address: '', city: '', country: '', totalCapacity: '' });
  };

  const openSeatDialog = (venue) => {
    setSeatForm({ venueId: venue.id, sectionId: '', rows: [{ rowLabel: 'A', startNumber: 1, endNumber: 20, seatType: 'REGULAR' }] });
    if (expandedVenue !== venue.id) {
      toggleExpand(venue.id);
    }
    setSeatDialog(true);
  };

  const addRow = () => {
    setSeatForm({
      ...seatForm,
      rows: [...seatForm.rows, { rowLabel: '', startNumber: 1, endNumber: 20, seatType: 'REGULAR' }],
    });
  };

  const updateRow = (idx, field, value) => {
    const newRows = [...seatForm.rows];
    newRows[idx] = { ...newRows[idx], [field]: value };
    setSeatForm({ ...seatForm, rows: newRows });
  };

  const removeRow = (idx) => {
    setSeatForm({ ...seatForm, rows: seatForm.rows.filter((_, i) => i !== idx) });
  };

  if (loading) return <LoadingScreen />;

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h5" fontWeight={700}>Venue Management</Typography>
        <Button variant="contained" startIcon={<Add />} onClick={() => { resetVenueForm(); setVenueDialog(true); }}>
          Add Venue
        </Button>
      </Box>

      <TableContainer component={Paper} variant="outlined">
        <Table>
          <TableHead>
            <TableRow>
              <TableCell width={40} />
              <TableCell>Name</TableCell>
              <TableCell>Address</TableCell>
              <TableCell>City</TableCell>
              <TableCell>Capacity</TableCell>
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {venues.map((v) => (
              <Box component="tbody" key={v.id}>
                <TableRow hover>
                  <TableCell>
                    <IconButton size="small" onClick={() => toggleExpand(v.id)}>
                      {expandedVenue === v.id ? <ExpandLess /> : <ExpandMore />}
                    </IconButton>
                  </TableCell>
                  <TableCell><Typography variant="body2" fontWeight={600}>{v.name}</Typography></TableCell>
                  <TableCell>{v.address || '-'}</TableCell>
                  <TableCell>{v.city || '-'}</TableCell>
                  <TableCell>{v.totalCapacity || '-'}</TableCell>
                  <TableCell align="right">
                    <Tooltip title="Edit">
                      <IconButton size="small" onClick={() => openEditVenue(v)}><Edit fontSize="small" /></IconButton>
                    </Tooltip>
                    <Tooltip title="Delete">
                      <IconButton size="small" color="error" onClick={() => handleDeleteVenue(v.id)}><Delete fontSize="small" /></IconButton>
                    </Tooltip>
                    <Tooltip title="Add Seats">
                      <IconButton size="small" color="info" onClick={() => openSeatDialog(v)}>
                        <Chair fontSize="small" />
                      </IconButton>
                    </Tooltip>
                  </TableCell>
                </TableRow>
                <TableRow>
                  <TableCell colSpan={6} sx={{ p: 0 }}>
                    <Collapse in={expandedVenue === v.id}>
                      <Box sx={{ p: 2, bgcolor: 'grey.50' }}>
                        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                          <Typography variant="subtitle2" fontWeight={600}>Sections</Typography>
                          <Box sx={{ display: 'flex', gap: 1 }}>
                            <Button size="small" variant="outlined" onClick={() => handleViewSeats(v)}>
                              View Seats
                            </Button>
                            <Button size="small" startIcon={<Add />} onClick={() => { setSelectedVenueId(v.id); resetSectionForm(); setSectionDialog(true); }}>
                              Add Section
                            </Button>
                          </Box>
                        </Box>
                        {sectionsLoading ? (
                          <Typography variant="body2" color="text.secondary">Loading...</Typography>
                        ) : sections.length > 0 ? (
                          <Table size="small">
                            <TableHead>
                              <TableRow>
                                <TableCell>Name</TableCell>
                                <TableCell>Capacity</TableCell>
                                <TableCell>Numbered Seats</TableCell>
                                <TableCell align="right">Actions</TableCell>
                              </TableRow>
                            </TableHead>
                            <TableBody>
                              {sections.map((s) => (
                                <TableRow key={s.id}>
                                  <TableCell>{s.name}</TableCell>
                                  <TableCell>{s.capacity || '-'}</TableCell>
                                  <TableCell>
                                    <Chip label={s.hasNumberedSeats ? 'Yes' : 'No'} size="small" color={s.hasNumberedSeats ? 'primary' : 'default'} variant="outlined" />
                                  </TableCell>
                                  <TableCell align="right">
                                    <Tooltip title="Edit Section">
                                      <IconButton size="small" onClick={() => openEditSection(s, v.id)}><Edit fontSize="small" /></IconButton>
                                    </Tooltip>
                                    <Tooltip title="Delete Section">
                                      <IconButton size="small" color="error" onClick={() => handleDeleteSection(s.id)}><Delete fontSize="small" /></IconButton>
                                    </Tooltip>
                                  </TableCell>
                                </TableRow>
                              ))}
                            </TableBody>
                          </Table>
                        ) : (
                          <Typography variant="body2" color="text.secondary">No sections yet. Add a section to start organizing seats.</Typography>
                        )}
                      </Box>
                    </Collapse>
                  </TableCell>
                </TableRow>
              </Box>
            ))}
            {venues.length === 0 && (
              <TableRow><TableCell colSpan={6} align="center" sx={{ py: 4, color: 'text.secondary' }}>No venues yet. Create your first venue to get started.</TableCell></TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Venue Dialog */}
      <Dialog open={venueDialog} onClose={() => setVenueDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{editingId ? 'Edit Venue' : 'Add Venue'}</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
            <TextField label="Name" value={venueForm.name} onChange={(e) => setVenueForm({ ...venueForm, name: e.target.value })} fullWidth required />
            <TextField label="Address" value={venueForm.address} onChange={(e) => setVenueForm({ ...venueForm, address: e.target.value })} fullWidth />
            <Box sx={{ display: 'flex', gap: 2 }}>
              <TextField label="City" value={venueForm.city} onChange={(e) => setVenueForm({ ...venueForm, city: e.target.value })} fullWidth />
              <TextField label="Country" value={venueForm.country} onChange={(e) => setVenueForm({ ...venueForm, country: e.target.value })} fullWidth />
            </Box>
            <TextField label="Total Capacity" type="number" value={venueForm.totalCapacity} onChange={(e) => setVenueForm({ ...venueForm, totalCapacity: e.target.value })} fullWidth />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setVenueDialog(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleSaveVenue} disabled={!venueForm.name}>{editingId ? 'Update' : 'Create'}</Button>
        </DialogActions>
      </Dialog>

      {/* Section Dialog */}
      <Dialog open={sectionDialog} onClose={() => setSectionDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{editingSectionId ? 'Edit Section' : 'Add Section'}</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
            <TextField label="Section Name" value={sectionForm.name} onChange={(e) => setSectionForm({ ...sectionForm, name: e.target.value })} fullWidth placeholder="e.g. VIP Area, Zone A" />
            <TextField label="Capacity" type="number" value={sectionForm.capacity} onChange={(e) => setSectionForm({ ...sectionForm, capacity: e.target.value })} fullWidth />
            <FormControl fullWidth>
              <InputLabel>Numbered Seats</InputLabel>
              <Select value={sectionForm.hasNumberedSeats} label="Numbered Seats" onChange={(e) => setSectionForm({ ...sectionForm, hasNumberedSeats: e.target.value })}>
                <MenuItem value={true}>Yes - Seats with row & number</MenuItem>
                <MenuItem value={false}>No - General admission</MenuItem>
              </Select>
            </FormControl>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setSectionDialog(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleSaveSection} disabled={!sectionForm.name}>{editingSectionId ? 'Update' : 'Add'}</Button>
        </DialogActions>
      </Dialog>

      {/* Bulk Seat Dialog */}
      <Dialog open={seatDialog} onClose={() => setSeatDialog(false)} maxWidth="md" fullWidth>
        <DialogTitle>Bulk Create Seats</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
            <FormControl fullWidth>
              <InputLabel>Section</InputLabel>
              <Select value={seatForm.sectionId} label="Section" onChange={(e) => setSeatForm({ ...seatForm, sectionId: e.target.value })}>
                {sections.map((s) => <MenuItem key={s.id} value={s.id}>{s.name}</MenuItem>)}
              </Select>
            </FormControl>
            <Typography variant="subtitle2" fontWeight={600}>Rows:</Typography>
            {seatForm.rows.map((row, idx) => (
              <Box key={idx} sx={{ display: 'flex', gap: 1, alignItems: 'center', flexWrap: 'wrap' }}>
                <TextField label="Row" value={row.rowLabel} onChange={(e) => updateRow(idx, 'rowLabel', e.target.value)} sx={{ width: 80 }} />
                <TextField label="From" type="number" value={row.startNumber} onChange={(e) => updateRow(idx, 'startNumber', parseInt(e.target.value) || 0)} sx={{ width: 80 }} />
                <TextField label="To" type="number" value={row.endNumber} onChange={(e) => updateRow(idx, 'endNumber', parseInt(e.target.value) || 0)} sx={{ width: 80 }} />
                <FormControl sx={{ width: 130 }}>
                  <InputLabel>Type</InputLabel>
                  <Select value={row.seatType} label="Type" onChange={(e) => updateRow(idx, 'seatType', e.target.value)}>
                    <MenuItem value="VIP">VIP</MenuItem>
                    <MenuItem value="REGULAR">Regular</MenuItem>
                    <MenuItem value="WHEELCHAIR">Wheelchair</MenuItem>
                  </Select>
                </FormControl>
                <IconButton size="small" color="error" onClick={() => removeRow(idx)} disabled={seatForm.rows.length <= 1}>
                  <Delete fontSize="small" />
                </IconButton>
              </Box>
            ))}
            <Button size="small" startIcon={<Add />} onClick={addRow}>Add Row</Button>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setSeatDialog(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleBulkCreateSeats} disabled={!seatForm.sectionId}>Create Seats</Button>
        </DialogActions>
      </Dialog>

      {/* View Seats Dialog */}
      <Dialog open={seatsDialog} onClose={() => setSeatsDialog(false)} maxWidth="md" fullWidth>
        <DialogTitle>Seats — {viewingVenueName}</DialogTitle>
        <DialogContent>
          {seatsLoading ? (
            <Typography color="text.secondary" sx={{ py: 2 }}>Loading seats...</Typography>
          ) : seats.length > 0 ? (
            <TableContainer sx={{ mt: 1 }}>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Row</TableCell>
                    <TableCell>Number</TableCell>
                    <TableCell>Type</TableCell>
                    <TableCell>Section</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {seats.map((seat) => (
                    <TableRow key={seat.id}>
                      <TableCell>{seat.rowNumber || '-'}</TableCell>
                      <TableCell>{seat.seatNumber || '-'}</TableCell>
                      <TableCell>
                        <Chip
                          label={seat.seatType}
                          size="small"
                          color={seat.seatType === 'VIP' ? 'warning' : seat.seatType === 'WHEELCHAIR' ? 'info' : 'default'}
                          variant="outlined"
                        />
                      </TableCell>
                      <TableCell>{seat.sectionName || '-'}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          ) : (
            <Typography color="text.secondary" sx={{ py: 2 }}>No seats created yet for this venue.</Typography>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setSeatsDialog(false)}>Close</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default OrganizerVenuesPage;
