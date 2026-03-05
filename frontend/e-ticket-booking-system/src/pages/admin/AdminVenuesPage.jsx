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
  Grid,
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

const AdminVenuesPage = () => {
  const { enqueueSnackbar } = useSnackbar();
  const [venues, setVenues] = useState([]);
  const [loading, setLoading] = useState(true);

  // Venue dialog
  const [venueDialog, setVenueDialog] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [venueForm, setVenueForm] = useState({ name: '', address: '', city: '', country: '', capacity: '' });

  // Sections expansion
  const [expandedVenue, setExpandedVenue] = useState(null);
  const [sections, setSections] = useState([]);
  const [sectionsLoading, setSectionsLoading] = useState(false);

  // Section dialog
  const [sectionDialog, setSectionDialog] = useState(false);
  const [sectionForm, setSectionForm] = useState({ name: '', capacity: '', hasNumberedSeats: true });
  const [selectedVenueId, setSelectedVenueId] = useState(null);

  // Bulk seat dialog
  const [seatDialog, setSeatDialog] = useState(false);
  const [seatForm, setSeatForm] = useState({
    venueId: '',
    sectionId: '',
    seatType: 'REGULAR',
    rows: [{ rowLabel: 'A', startNumber: 1, endNumber: 20, seatType: 'REGULAR' }],
  });

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
      const payload = { ...venueForm, capacity: parseInt(venueForm.capacity) || 0 };
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

  const handleAddSection = async () => {
    try {
      await seatApi.createSection({
        venueId: selectedVenueId,
        name: sectionForm.name,
        capacity: parseInt(sectionForm.capacity) || 0,
        hasNumberedSeats: sectionForm.hasNumberedSeats,
      });
      enqueueSnackbar('Section added', { variant: 'success' });
      setSectionDialog(false);
      setSectionForm({ name: '', capacity: '', hasNumberedSeats: true });
      // Refresh sections
      const res = await seatApi.getSectionsByVenue(selectedVenueId);
      setSections(res.data || []);
    } catch (err) {
      enqueueSnackbar(getErrorMessage(err), { variant: 'error' });
    }
  };

  const handleBulkCreateSeats = async () => {
    try {
      await seatApi.bulkCreate(seatForm);
      enqueueSnackbar('Seats created', { variant: 'success' });
      setSeatDialog(false);
    } catch (err) {
      enqueueSnackbar(getErrorMessage(err), { variant: 'error' });
    }
  };

  const openEditVenue = (v) => {
    setEditingId(v.id);
    setVenueForm({ name: v.name || '', address: v.address || '', city: v.city || '', country: v.country || '', capacity: v.capacity?.toString() || '' });
    setVenueDialog(true);
  };

  const resetVenueForm = () => {
    setEditingId(null);
    setVenueForm({ name: '', address: '', city: '', country: '', capacity: '' });
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
              <>
                <TableRow key={v.id} hover>
                  <TableCell>
                    <IconButton size="small" onClick={() => toggleExpand(v.id)}>
                      {expandedVenue === v.id ? <ExpandLess /> : <ExpandMore />}
                    </IconButton>
                  </TableCell>
                  <TableCell><Typography variant="body2" fontWeight={600}>{v.name}</Typography></TableCell>
                  <TableCell>{v.address || '-'}</TableCell>
                  <TableCell>{v.city || '-'}</TableCell>
                  <TableCell>{v.capacity || '-'}</TableCell>
                  <TableCell align="right">
                    <Tooltip title="Edit"><IconButton size="small" onClick={() => openEditVenue(v)}><Edit fontSize="small" /></IconButton></Tooltip>
                    <Tooltip title="Delete"><IconButton size="small" color="error" onClick={() => handleDeleteVenue(v.id)}><Delete fontSize="small" /></IconButton></Tooltip>
                    <Tooltip title="Add Seats">
                      <IconButton size="small" color="info" onClick={() => { setSeatForm({ ...seatForm, venueId: v.id }); toggleExpand(v.id); setSeatDialog(true); }}>
                        <Chair fontSize="small" />
                      </IconButton>
                    </Tooltip>
                  </TableCell>
                </TableRow>
                <TableRow key={`expand-${v.id}`}>
                  <TableCell colSpan={6} sx={{ p: 0 }}>
                    <Collapse in={expandedVenue === v.id}>
                      <Box sx={{ p: 2, bgcolor: 'grey.50' }}>
                        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                          <Typography variant="subtitle2" fontWeight={600}>Sections</Typography>
                          <Button size="small" startIcon={<Add />} onClick={() => { setSelectedVenueId(v.id); setSectionDialog(true); }}>
                            Add Section
                          </Button>
                        </Box>
                        {sectionsLoading ? (
                          <Typography variant="body2" color="text.secondary">Loading...</Typography>
                        ) : sections.length > 0 ? (
                          <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                            {sections.map((s) => (
                              <Chip key={s.id} label={`${s.name} (Cap: ${s.capacity})`} variant="outlined" />
                            ))}
                          </Box>
                        ) : (
                          <Typography variant="body2" color="text.secondary">No sections</Typography>
                        )}
                      </Box>
                    </Collapse>
                  </TableCell>
                </TableRow>
              </>
            ))}
            {venues.length === 0 && (
              <TableRow><TableCell colSpan={6} align="center" sx={{ py: 4, color: 'text.secondary' }}>No venues</TableCell></TableRow>
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
            <TextField label="Capacity" type="number" value={venueForm.capacity} onChange={(e) => setVenueForm({ ...venueForm, capacity: e.target.value })} fullWidth />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setVenueDialog(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleSaveVenue} disabled={!venueForm.name}>{editingId ? 'Update' : 'Create'}</Button>
        </DialogActions>
      </Dialog>

      {/* Section Dialog */}
      <Dialog open={sectionDialog} onClose={() => setSectionDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Add Section</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
            <TextField label="Section Name" value={sectionForm.name} onChange={(e) => setSectionForm({ ...sectionForm, name: e.target.value })} fullWidth placeholder="e.g. VIP Area, Zone A" />
            <TextField label="Capacity" type="number" value={sectionForm.capacity} onChange={(e) => setSectionForm({ ...sectionForm, capacity: e.target.value })} fullWidth />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setSectionDialog(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleAddSection} disabled={!sectionForm.name}>Add</Button>
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
              <Box key={idx} sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
                <TextField label="Row" value={row.rowLabel} onChange={(e) => updateRow(idx, 'rowLabel', e.target.value)} sx={{ width: 80 }} />
                <TextField label="From" type="number" value={row.startNumber} onChange={(e) => updateRow(idx, 'startNumber', parseInt(e.target.value))} sx={{ width: 80 }} />
                <TextField label="To" type="number" value={row.endNumber} onChange={(e) => updateRow(idx, 'endNumber', parseInt(e.target.value))} sx={{ width: 80 }} />
                <FormControl sx={{ width: 120 }}>
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
    </Box>
  );
};

export default AdminVenuesPage;
