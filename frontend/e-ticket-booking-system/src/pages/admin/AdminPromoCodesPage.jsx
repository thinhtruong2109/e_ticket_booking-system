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
import { promoCodeApi } from '../../api';
import { LoadingScreen, StatusChip } from '../../components/common';
import { formatDateTime, formatCurrency, getErrorMessage } from '../../utils/helpers';

const AdminPromoCodesPage = () => {
  const { enqueueSnackbar } = useSnackbar();
  const [promos, setPromos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [dialogOpen, setDialogOpen] = useState(false);

  const [form, setForm] = useState({
    code: '',
    discountType: 'PERCENTAGE',
    discountValue: '',
    usageLimit: '',
    validFrom: '',
    validTo: '',
    minOrderAmount: '',
    maxDiscountAmount: '',
  });

  const fetchPromos = async () => {
    try {
      const res = await promoCodeApi.adminGetAll();
      setPromos(res.data || []);
    } catch (err) {
      enqueueSnackbar(getErrorMessage(err), { variant: 'error' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchPromos(); }, []);

  const handleCreate = async () => {
    try {
      await promoCodeApi.adminCreate({
        code: form.code.toUpperCase(),
        discountType: form.discountType,
        discountValue: parseFloat(form.discountValue),
        usageLimit: parseInt(form.usageLimit) || null,
        validFrom: form.validFrom,
        validTo: form.validTo,
        minOrderAmount: form.minOrderAmount ? parseFloat(form.minOrderAmount) : null,
        maxDiscountAmount: form.maxDiscountAmount ? parseFloat(form.maxDiscountAmount) : null,
        applicationType: 'GLOBAL',
      });
      enqueueSnackbar('Global promo code created', { variant: 'success' });
      setDialogOpen(false);
      setForm({ code: '', discountType: 'PERCENTAGE', discountValue: '', usageLimit: '', validFrom: '', validTo: '', minOrderAmount: '', maxDiscountAmount: '' });
      fetchPromos();
    } catch (err) {
      enqueueSnackbar(getErrorMessage(err), { variant: 'error' });
    }
  };

  const handleDeactivate = async (id) => {
    try {
      await promoCodeApi.adminDeactivate(id);
      enqueueSnackbar('Promo code deactivated', { variant: 'success' });
      fetchPromos();
    } catch (err) {
      enqueueSnackbar(getErrorMessage(err), { variant: 'error' });
    }
  };

  if (loading) return <LoadingScreen />;

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h5" fontWeight={700}>Promo Code Management</Typography>
        <Button variant="contained" startIcon={<Add />} onClick={() => setDialogOpen(true)}>
          Create Global Promo
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
              <TableCell>Created By</TableCell>
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
                  {p.discountType === 'PERCENTAGE' ? `${p.discountValue}%` : formatCurrency(p.discountValue)}
                </TableCell>
                <TableCell><Chip label={p.applicationType} size="small" variant="outlined" /></TableCell>
                <TableCell>{p.createdBy?.fullName || '-'}</TableCell>
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
                <TableCell colSpan={9} align="center" sx={{ py: 4, color: 'text.secondary' }}>
                  No promo codes
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Create Global Promo Dialog */}
      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Create Global Promo Code</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
            <TextField
              label="Code"
              value={form.code}
              onChange={(e) => setForm({ ...form, code: e.target.value.toUpperCase() })}
              fullWidth
              placeholder="e.g. GLOBAL10"
            />
            <Box sx={{ display: 'flex', gap: 2 }}>
              <FormControl fullWidth>
                <InputLabel>Discount Type</InputLabel>
                <Select value={form.discountType} label="Discount Type" onChange={(e) => setForm({ ...form, discountType: e.target.value })}>
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
            <TextField label="Usage Limit" type="number" value={form.usageLimit} onChange={(e) => setForm({ ...form, usageLimit: e.target.value })} fullWidth />
            <Box sx={{ display: 'flex', gap: 2 }}>
              <TextField label="Valid From" type="datetime-local" value={form.validFrom} onChange={(e) => setForm({ ...form, validFrom: e.target.value })} slotProps={{ inputLabel: { shrink: true } }} fullWidth />
              <TextField label="Valid To" type="datetime-local" value={form.validTo} onChange={(e) => setForm({ ...form, validTo: e.target.value })} slotProps={{ inputLabel: { shrink: true } }} fullWidth />
            </Box>
            <Box sx={{ display: 'flex', gap: 2 }}>
              <TextField label="Min Order Amount" type="number" value={form.minOrderAmount} onChange={(e) => setForm({ ...form, minOrderAmount: e.target.value })} fullWidth />
              <TextField label="Max Discount Amount" type="number" value={form.maxDiscountAmount} onChange={(e) => setForm({ ...form, maxDiscountAmount: e.target.value })} fullWidth />
            </Box>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleCreate} disabled={!form.code || !form.discountValue || !form.validFrom || !form.validTo}>
            Create
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default AdminPromoCodesPage;
