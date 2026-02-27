import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Container,
  Grid,
  Paper,
  Typography,
  Box,
  Button,
  Divider,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
  Tabs,
  Tab,
  IconButton,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
} from '@mui/material';
import {
  People,
  Event,
  Category,
  LocalOffer,
  Block,
  CheckCircle,
  Edit,
} from '@mui/icons-material';
import { userApi, eventApi, categoryApi, promoCodeApi } from '../../api';
import { PageHeader, LoadingScreen, ErrorAlert, StatusChip } from '../../components/common';
import { formatDateTime, getRoleLabel, getErrorMessage, formatCurrency } from '../../utils/helpers';
import { ROLES } from '../../utils/constants';

const AdminDashboard = () => {
  const navigate = useNavigate();
  const [tab, setTab] = useState(0);

  return (
    <>
      <PageHeader title="Admin Dashboard" subtitle="System management" chip="Admin" />
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Tabs
          value={tab}
          onChange={(e, v) => setTab(v)}
          sx={{ mb: 3, borderBottom: 1, borderColor: 'divider' }}
        >
          <Tab icon={<People />} label="Users" iconPosition="start" />
          <Tab icon={<Event />} label="Events" iconPosition="start" />
          <Tab icon={<Category />} label="Categories" iconPosition="start" />
          <Tab icon={<LocalOffer />} label="Promo Codes" iconPosition="start" />
        </Tabs>

        {tab === 0 && <UsersTab />}
        {tab === 1 && <EventsTab />}
        {tab === 2 && <CategoriesTab />}
        {tab === 3 && <PromoCodesTab />}
      </Container>
    </>
  );
};

/* ---- USERS TAB ---- */
const UsersTab = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [roleDialog, setRoleDialog] = useState({ open: false, userId: null, currentRole: '' });

  useEffect(() => { fetchUsers(); }, []);

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const res = await userApi.getAllUsers();
      setUsers(Array.isArray(res.data) ? res.data : []);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const handleBan = async (userId) => {
    try { await userApi.banUser(userId); fetchUsers(); }
    catch (err) { setError(getErrorMessage(err)); }
  };

  const handleUnban = async (userId) => {
    try { await userApi.unbanUser(userId); fetchUsers(); }
    catch (err) { setError(getErrorMessage(err)); }
  };

  const handleRoleChange = async () => {
    try {
      await userApi.changeUserRole(roleDialog.userId, roleDialog.currentRole);
      setRoleDialog({ open: false, userId: null, currentRole: '' });
      fetchUsers();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  if (loading) return <LoadingScreen />;

  return (
    <>
      {error && <ErrorAlert message={error} />}
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>ID</TableCell>
              <TableCell>Name</TableCell>
              <TableCell>Email</TableCell>
              <TableCell>Role</TableCell>
              <TableCell align="center">Status</TableCell>
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {users.map((u) => (
              <TableRow key={u.id} hover>
                <TableCell>{u.id}</TableCell>
                <TableCell><Typography variant="body2" fontWeight={600}>{u.fullName}</Typography></TableCell>
                <TableCell><Typography variant="body2" color="text.secondary">{u.email}</Typography></TableCell>
                <TableCell>
                  <Chip label={getRoleLabel(u.role)} size="small" variant="outlined" />
                </TableCell>
                <TableCell align="center"><StatusChip status={u.status} /></TableCell>
                <TableCell align="right">
                  <Box sx={{ display: 'flex', gap: 0.5, justifyContent: 'flex-end' }}>
                    <IconButton
                      size="small"
                      onClick={() => setRoleDialog({ open: true, userId: u.id, currentRole: u.role })}
                      title="Change Role"
                    >
                      <Edit fontSize="small" />
                    </IconButton>
                    {u.status !== 'BANNED' ? (
                      <IconButton size="small" color="error" onClick={() => handleBan(u.id)} title="Ban">
                        <Block fontSize="small" />
                      </IconButton>
                    ) : (
                      <IconButton size="small" color="success" onClick={() => handleUnban(u.id)} title="Unban">
                        <CheckCircle fontSize="small" />
                      </IconButton>
                    )}
                  </Box>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={roleDialog.open} onClose={() => setRoleDialog({ open: false, userId: null, currentRole: '' })}>
        <DialogTitle>Change User Role</DialogTitle>
        <DialogContent sx={{ minWidth: 300, pt: '8px !important' }}>
          <FormControl fullWidth>
            <InputLabel>Role</InputLabel>
            <Select
              value={roleDialog.currentRole}
              onChange={(e) => setRoleDialog({ ...roleDialog, currentRole: e.target.value })}
              label="Role"
            >
              {Object.values(ROLES).map((r) => (
                <MenuItem key={r} value={r}>{getRoleLabel(r)}</MenuItem>
              ))}
            </Select>
          </FormControl>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setRoleDialog({ open: false, userId: null, currentRole: '' })}>Cancel</Button>
          <Button variant="contained" onClick={handleRoleChange}>Save</Button>
        </DialogActions>
      </Dialog>
    </>
  );
};

/* ---- EVENTS TAB ---- */
const EventsTab = () => {
  const navigate = useNavigate();
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => { fetchEvents(); }, []);

  const fetchEvents = async () => {
    setLoading(true);
    try {
      const res = await eventApi.getAllEvents();
      setEvents(Array.isArray(res.data) ? res.data : []);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <LoadingScreen />;

  return (
    <>
      {error && <ErrorAlert message={error} />}
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>ID</TableCell>
              <TableCell>Name</TableCell>
              <TableCell>Category</TableCell>
              <TableCell>Venue</TableCell>
              <TableCell align="right">Tickets</TableCell>
              <TableCell align="center">Status</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {events.map((e) => (
              <TableRow key={e.id} hover sx={{ cursor: 'pointer' }} onClick={() => navigate(`/events/${e.id}`)}>
                <TableCell>{e.id}</TableCell>
                <TableCell><Typography variant="body2" fontWeight={600}>{e.name}</Typography></TableCell>
                <TableCell>{e.categoryName || '-'}</TableCell>
                <TableCell>{e.venueName || '-'}</TableCell>
                <TableCell align="right">{e.availableTickets}/{e.totalTickets}</TableCell>
                <TableCell align="center"><StatusChip status={e.status} /></TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </>
  );
};

/* ---- CATEGORIES TAB ---- */
const CategoriesTab = () => {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [dialog, setDialog] = useState({ open: false, mode: 'create', data: { name: '', description: '', iconUrl: '' }, id: null });

  useEffect(() => { fetchCategories(); }, []);

  const fetchCategories = async () => {
    setLoading(true);
    try {
      const res = await categoryApi.getAll();
      setCategories(Array.isArray(res.data) ? res.data : []);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async () => {
    try {
      if (dialog.mode === 'create') {
        await categoryApi.create(dialog.data);
      } else {
        await categoryApi.update(dialog.id, dialog.data);
      }
      setDialog({ open: false, mode: 'create', data: { name: '', description: '', iconUrl: '' }, id: null });
      fetchCategories();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this category?')) return;
    try { await categoryApi.delete(id); fetchCategories(); }
    catch (err) { setError(getErrorMessage(err)); }
  };

  if (loading) return <LoadingScreen />;

  return (
    <>
      {error && <ErrorAlert message={error} />}
      <Box sx={{ mb: 2 }}>
        <Button variant="contained" onClick={() => setDialog({ open: true, mode: 'create', data: { name: '', description: '', iconUrl: '' }, id: null })}>
          Add Category
        </Button>
      </Box>
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>ID</TableCell>
              <TableCell>Name</TableCell>
              <TableCell>Description</TableCell>
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {categories.map((c) => (
              <TableRow key={c.id} hover>
                <TableCell>{c.id}</TableCell>
                <TableCell><Typography variant="body2" fontWeight={600}>{c.name}</Typography></TableCell>
                <TableCell><Typography variant="body2" color="text.secondary">{c.description || '-'}</Typography></TableCell>
                <TableCell align="right">
                  <Button
                    size="small"
                    onClick={() => setDialog({ open: true, mode: 'edit', data: { name: c.name, description: c.description || '', iconUrl: c.iconUrl || '' }, id: c.id })}
                  >
                    Edit
                  </Button>
                  <Button size="small" color="error" onClick={() => handleDelete(c.id)}>Delete</Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={dialog.open} onClose={() => setDialog({ ...dialog, open: false })} maxWidth="sm" fullWidth>
        <DialogTitle>{dialog.mode === 'create' ? 'Add Category' : 'Edit Category'}</DialogTitle>
        <DialogContent sx={{ pt: '8px !important' }}>
          <TextField label="Name" fullWidth required value={dialog.data.name}
            onChange={(e) => setDialog({ ...dialog, data: { ...dialog.data, name: e.target.value } })} sx={{ mb: 2 }} />
          <TextField label="Description" fullWidth multiline rows={2} value={dialog.data.description}
            onChange={(e) => setDialog({ ...dialog, data: { ...dialog.data, description: e.target.value } })} sx={{ mb: 2 }} />
          <TextField label="Icon URL" fullWidth value={dialog.data.iconUrl}
            onChange={(e) => setDialog({ ...dialog, data: { ...dialog.data, iconUrl: e.target.value } })} />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialog({ ...dialog, open: false })}>Cancel</Button>
          <Button variant="contained" onClick={handleSave}>Save</Button>
        </DialogActions>
      </Dialog>
    </>
  );
};

/* ---- PROMO CODES TAB ---- */
const PromoCodesTab = () => {
  const [promos, setPromos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [dialog, setDialog] = useState({ open: false, data: {} });

  useEffect(() => { fetchPromos(); }, []);

  const fetchPromos = async () => {
    setLoading(true);
    try {
      const res = await promoCodeApi.adminGetAll();
      setPromos(Array.isArray(res.data) ? res.data : []);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const handleDeactivate = async (id) => {
    try { await promoCodeApi.adminDeactivate(id); fetchPromos(); }
    catch (err) { setError(getErrorMessage(err)); }
  };

  const handleCreate = async () => {
    try {
      await promoCodeApi.adminCreate({ ...dialog.data, applicationType: 'GLOBAL' });
      setDialog({ open: false, data: {} });
      fetchPromos();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  if (loading) return <LoadingScreen />;

  return (
    <>
      {error && <ErrorAlert message={error} />}
      <Box sx={{ mb: 2 }}>
        <Button variant="contained" onClick={() => setDialog({
          open: true,
          data: { code: '', description: '', discountType: 'PERCENTAGE', discountValue: 0, minOrderAmount: 0, maxDiscountAmount: 0, usageLimit: 100, validFrom: '', validTo: '', applicationType: 'GLOBAL' }
        })}>
          Create Global Promo
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
        <DialogTitle>Create Global Promo Code</DialogTitle>
        <DialogContent sx={{ pt: '8px !important' }}>
          <Grid container spacing={2}>
            <Grid item xs={6}>
              <TextField label="Code" fullWidth required value={dialog.data.code || ''}
                onChange={(e) => setDialog({ ...dialog, data: { ...dialog.data, code: e.target.value } })} />
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
            <Grid item xs={6}>
              <TextField label="Usage Limit" type="number" fullWidth value={dialog.data.usageLimit || ''}
                onChange={(e) => setDialog({ ...dialog, data: { ...dialog.data, usageLimit: parseInt(e.target.value) } })} />
            </Grid>
            <Grid item xs={12}>
              <TextField label="Description" fullWidth value={dialog.data.description || ''}
                onChange={(e) => setDialog({ ...dialog, data: { ...dialog.data, description: e.target.value } })} />
            </Grid>
            <Grid item xs={6}>
              <TextField label="Valid From" type="datetime-local" fullWidth value={dialog.data.validFrom || ''}
                onChange={(e) => setDialog({ ...dialog, data: { ...dialog.data, validFrom: e.target.value } })} InputLabelProps={{ shrink: true }} />
            </Grid>
            <Grid item xs={6}>
              <TextField label="Valid To" type="datetime-local" fullWidth value={dialog.data.validTo || ''}
                onChange={(e) => setDialog({ ...dialog, data: { ...dialog.data, validTo: e.target.value } })} InputLabelProps={{ shrink: true }} />
            </Grid>
            <Grid item xs={6}>
              <TextField label="Min Order Amount" type="number" fullWidth value={dialog.data.minOrderAmount || ''}
                onChange={(e) => setDialog({ ...dialog, data: { ...dialog.data, minOrderAmount: parseFloat(e.target.value) } })} />
            </Grid>
            <Grid item xs={6}>
              <TextField label="Max Discount Amount" type="number" fullWidth value={dialog.data.maxDiscountAmount || ''}
                onChange={(e) => setDialog({ ...dialog, data: { ...dialog.data, maxDiscountAmount: parseFloat(e.target.value) } })} />
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

export default AdminDashboard;
