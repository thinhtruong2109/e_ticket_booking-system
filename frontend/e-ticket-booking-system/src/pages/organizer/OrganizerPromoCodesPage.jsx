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
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Chip,
  IconButton,
  Tooltip,
} from '@mui/material';
import { Add, Block } from '@mui/icons-material';
import { useSnackbar } from 'notistack';
import { promoCodeApi, eventApi } from '../../api';
import { LoadingScreen, StatusChip } from '../../components/common';
import { formatDateTime, formatCurrency, getErrorMessage } from '../../utils/helpers';

const OrganizerPromoCodesPage = () => {
  const { enqueueSnackbar } = useSnackbar();
  const [promos, setPromos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [myEvents, setMyEvents] = useState([]);

  const [form, setForm] = useState({
    code: '',
    discountType: 'PERCENTAGE',
    discountValue: '',
    usageLimit: '',
    validFrom: '',
    validTo: '',
    minOrderAmount: '',
    maxDiscountAmount: '',
    applicationType: 'ORGANIZER_ALL',
    eventIds: [],
  });

  const fetchPromos = async () => {
    try {
      const res = await promoCodeApi.organizerGetAll();
      setPromos(res.data || []);
    } catch (err) {
      enqueueSnackbar(getErrorMessage(err), { variant: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const fetchEvents = async () => {
    try {
      const res = await eventApi.getMyEvents();
      setMyEvents(res.data || []);
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    fetchPromos();
    fetchEvents();
  }, []);

  const handleCreate = async () => {
    try {
      const payload = {
        code: form.code.toUpperCase(),
        discountType: form.discountType,
        discountValue: parseFloat(form.discountValue),
        usageLimit: parseInt(form.usageLimit) || null,
        validFrom: form.validFrom,
        validTo: form.validTo,
        minOrderAmount: form.minOrderAmount ? parseFloat(form.minOrderAmount) : null,
        maxDiscountAmount: form.maxDiscountAmount ? parseFloat(form.maxDiscountAmount) : null,
        applicationType: form.applicationType,
        eventIds: form.applicationType === 'SPECIFIC_EVENTS' ? form.eventIds : undefined,
      };
      await promoCodeApi.organizerCreate(payload);
      enqueueSnackbar('Promo code created', { variant: 'success' });
      setDialogOpen(false);
      resetForm();
      fetchPromos();
    } catch (err) {
      enqueueSnackbar(getErrorMessage(err), { variant: 'error' });
    }
  };

  const handleDeactivate = async (id) => {
    try {
      await promoCodeApi.organizerDeactivate(id);
      enqueueSnackbar('Promo code deactivated', { variant: 'success' });
      fetchPromos();
    } catch (err) {
      enqueueSnackbar(getErrorMessage(err), { variant: 'error' });
    }
  };

  const resetForm = () => {
    setForm({
      code: '', discountType: 'PERCENTAGE', discountValue: '', usageLimit: '',
      validFrom: '', validTo: '', minOrderAmount: '', maxDiscountAmount: '',
      applicationType: 'ORGANIZER_ALL', eventIds: [],
    });
  };

  if (loading) return <LoadingScreen />;

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h5" fontWeight={700}>My Promo Codes</Typography>
        <Button variant="contained" startIcon={<Add />} onClick={() => setDialogOpen(true)}>
          Create Promo Code
        </Button>
      </Box>

      <TableContainer component={Paper} variant="outlined">
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Code</TableCell>
              <TableCell>Type</TableCell>
              <TableCell>Discount</TableCell>
              <TableCell>Scope</TableCell>
              <TableCell>Usage</TableCell>
              <TableCell>Valid Until</TableCell>
              <TableCell>Status</TableCell>
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {promos.map((p) => (
              <TableRow key={p.id} hover>
                <TableCell>
                  <Typography variant="body2" fontWeight={600} sx={{ fontFamily: 'monospace' }}>
                    {p.code}
                  </Typography>
                </TableCell>
                <TableCell>{p.discountType}</TableCell>
                <TableCell>
                  {p.discountType === 'PERCENTAGE'
                    ? `${p.discountValue}%`
                    : formatCurrency(p.discountValue)}
                </TableCell>
                <TableCell>
                  <Chip label={p.applicationType} size="small" variant="outlined" />
                </TableCell>
                <TableCell>{p.usedCount || 0} / {p.usageLimit || '∞'}</TableCell>
                <TableCell>{formatDateTime(p.validTo)}</TableCell>
                <TableCell><StatusChip status={p.status} /></TableCell>
                <TableCell align="right">
                  {p.status === 'ACTIVE' && (
                    <Tooltip title="Deactivate">
                      <IconButton size="small" color="error" onClick={() => handleDeactivate(p.id)}>
                        <Block fontSize="small" />
                      </IconButton>
                    </Tooltip>
                  )}
                </TableCell>
              </TableRow>
            ))}
            {promos.length === 0 && (
              <TableRow>
                <TableCell colSpan={8} align="center" sx={{ py: 4, color: 'text.secondary' }}>
                  No promo codes yet
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Create Dialog */}
      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Create Promo Code</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
            <TextField
              label="Code"
              value={form.code}
              onChange={(e) => setForm({ ...form, code: e.target.value.toUpperCase() })}
              fullWidth
              placeholder="e.g. SUMMER20"
            />
            <Box sx={{ display: 'flex', gap: 2 }}>
              <FormControl fullWidth>
                <InputLabel>Discount Type</InputLabel>
                <Select
                  value={form.discountType}
                  label="Discount Type"
                  onChange={(e) => setForm({ ...form, discountType: e.target.value })}
                >
                  <MenuItem value="PERCENTAGE">Percentage</MenuItem>
                  <MenuItem value="FIXED_AMOUNT">Fixed Amount</MenuItem>
                </Select>
              </FormControl>
              <TextField
                label={form.discountType === 'PERCENTAGE' ? 'Value (%)' : 'Value (VND)'}
                type="number"
                value={form.discountValue}
                onChange={(e) => setForm({ ...form, discountValue: e.target.value })}
                fullWidth
              />
            </Box>
            <TextField
              label="Usage Limit"
              type="number"
              value={form.usageLimit}
              onChange={(e) => setForm({ ...form, usageLimit: e.target.value })}
              fullWidth
            />
            <Box sx={{ display: 'flex', gap: 2 }}>
              <TextField
                label="Valid From"
                type="datetime-local"
                value={form.validFrom}
                onChange={(e) => setForm({ ...form, validFrom: e.target.value })}
                slotProps={{ inputLabel: { shrink: true } }}
                fullWidth
              />
              <TextField
                label="Valid To"
                type="datetime-local"
                value={form.validTo}
                onChange={(e) => setForm({ ...form, validTo: e.target.value })}
                slotProps={{ inputLabel: { shrink: true } }}
                fullWidth
              />
            </Box>
            <Box sx={{ display: 'flex', gap: 2 }}>
              <TextField
                label="Min Order Amount (VND)"
                type="number"
                value={form.minOrderAmount}
                onChange={(e) => setForm({ ...form, minOrderAmount: e.target.value })}
                fullWidth
              />
              <TextField
                label="Max Discount Amount (VND)"
                type="number"
                value={form.maxDiscountAmount}
                onChange={(e) => setForm({ ...form, maxDiscountAmount: e.target.value })}
                fullWidth
              />
            </Box>
            <FormControl fullWidth>
              <InputLabel>Scope</InputLabel>
              <Select
                value={form.applicationType}
                label="Scope"
                onChange={(e) => setForm({ ...form, applicationType: e.target.value, eventIds: [] })}
              >
                <MenuItem value="ORGANIZER_ALL">All My Events</MenuItem>
                <MenuItem value="SPECIFIC_EVENTS">Specific Events</MenuItem>
              </Select>
            </FormControl>
            {form.applicationType === 'SPECIFIC_EVENTS' && (
              <FormControl fullWidth>
                <InputLabel>Events</InputLabel>
                <Select
                  multiple
                  value={form.eventIds}
                  label="Events"
                  onChange={(e) => setForm({ ...form, eventIds: e.target.value })}
                  renderValue={(selected) =>
                    selected.map((id) => myEvents.find((e) => e.id === id)?.name || id).join(', ')
                  }
                >
                  {myEvents.map((ev) => (
                    <MenuItem key={ev.id} value={ev.id}>{ev.name}</MenuItem>
                  ))}
                </Select>
              </FormControl>
            )}
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => { setDialogOpen(false); resetForm(); }}>Cancel</Button>
          <Button
            variant="contained"
            onClick={handleCreate}
            disabled={!form.code || !form.discountValue || !form.validFrom || !form.validTo}
          >
            Create
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default OrganizerPromoCodesPage;
