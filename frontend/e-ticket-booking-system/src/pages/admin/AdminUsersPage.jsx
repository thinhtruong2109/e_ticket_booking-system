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
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Chip,
  IconButton,
  Tooltip,
} from '@mui/material';
import { Block, CheckCircle, SwapHoriz } from '@mui/icons-material';
import { useSnackbar } from 'notistack';
import { userApi } from '../../api';
import { LoadingScreen, StatusChip } from '../../components/common';
import { getRoleLabel, getErrorMessage } from '../../utils/helpers';
import { ROLES } from '../../utils/constants';

const AdminUsersPage = () => {
  const { enqueueSnackbar } = useSnackbar();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filterRole, setFilterRole] = useState('');
  const [roleDialog, setRoleDialog] = useState({ open: false, user: null, newRole: '' });

  const fetchUsers = async () => {
    try {
      const res = filterRole
        ? await userApi.getUsersByRole(filterRole)
        : await userApi.getAllUsers();
      setUsers(res.data || []);
    } catch (err) {
      enqueueSnackbar(getErrorMessage(err), { variant: 'error' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchUsers(); }, [filterRole]);

  const handleBan = async (userId) => {
    try {
      await userApi.banUser(userId);
      enqueueSnackbar('User banned', { variant: 'success' });
      fetchUsers();
    } catch (err) {
      enqueueSnackbar(getErrorMessage(err), { variant: 'error' });
    }
  };

  const handleUnban = async (userId) => {
    try {
      await userApi.unbanUser(userId);
      enqueueSnackbar('User unbanned', { variant: 'success' });
      fetchUsers();
    } catch (err) {
      enqueueSnackbar(getErrorMessage(err), { variant: 'error' });
    }
  };

  const handleChangeRole = async () => {
    try {
      await userApi.changeUserRole(roleDialog.user.id, roleDialog.newRole);
      enqueueSnackbar(`Role changed to ${roleDialog.newRole}`, { variant: 'success' });
      setRoleDialog({ open: false, user: null, newRole: '' });
      fetchUsers();
    } catch (err) {
      enqueueSnackbar(getErrorMessage(err), { variant: 'error' });
    }
  };

  if (loading) return <LoadingScreen />;

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h5" fontWeight={700}>User Management</Typography>
        <FormControl size="small" sx={{ minWidth: 160 }}>
          <InputLabel>Filter by Role</InputLabel>
          <Select value={filterRole} label="Filter by Role" onChange={(e) => setFilterRole(e.target.value)}>
            <MenuItem value="">All Roles</MenuItem>
            {Object.values(ROLES).map((role) => (
              <MenuItem key={role} value={role}>{getRoleLabel(role)}</MenuItem>
            ))}
          </Select>
        </FormControl>
      </Box>

      <TableContainer component={Paper} variant="outlined">
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>ID</TableCell>
              <TableCell>Full Name</TableCell>
              <TableCell>Email</TableCell>
              <TableCell>Role</TableCell>
              <TableCell>Status</TableCell>
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {users.map((u) => (
              <TableRow key={u.id} hover>
                <TableCell>{u.id}</TableCell>
                <TableCell>
                  <Typography variant="body2" fontWeight={600}>{u.fullName}</Typography>
                </TableCell>
                <TableCell>{u.email}</TableCell>
                <TableCell>
                  <Chip label={getRoleLabel(u.role)} size="small" />
                </TableCell>
                <TableCell><StatusChip status={u.status} /></TableCell>
                <TableCell align="right">
                  <Box sx={{ display: 'flex', gap: 0.5, justifyContent: 'flex-end' }}>
                    {u.status === 'ACTIVE' && (
                      <Tooltip title="Ban">
                        <IconButton size="small" color="error" onClick={() => handleBan(u.id)}>
                          <Block fontSize="small" />
                        </IconButton>
                      </Tooltip>
                    )}
                    {u.status === 'BANNED' && (
                      <Tooltip title="Unban">
                        <IconButton size="small" color="success" onClick={() => handleUnban(u.id)}>
                          <CheckCircle fontSize="small" />
                        </IconButton>
                      </Tooltip>
                    )}
                    <Tooltip title="Change Role">
                      <IconButton
                        size="small"
                        onClick={() => setRoleDialog({ open: true, user: u, newRole: u.role })}
                      >
                        <SwapHoriz fontSize="small" />
                      </IconButton>
                    </Tooltip>
                  </Box>
                </TableCell>
              </TableRow>
            ))}
            {users.length === 0 && (
              <TableRow>
                <TableCell colSpan={6} align="center" sx={{ py: 4, color: 'text.secondary' }}>
                  No users found
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Change Role Dialog */}
      <Dialog open={roleDialog.open} onClose={() => setRoleDialog({ open: false, user: null, newRole: '' })}>
        <DialogTitle>Change User Role</DialogTitle>
        <DialogContent>
          <Typography sx={{ mb: 2 }}>
            Change role for <strong>{roleDialog.user?.fullName}</strong> ({roleDialog.user?.email})
          </Typography>
          <FormControl fullWidth>
            <InputLabel>New Role</InputLabel>
            <Select
              value={roleDialog.newRole}
              label="New Role"
              onChange={(e) => setRoleDialog({ ...roleDialog, newRole: e.target.value })}
            >
              {Object.values(ROLES).map((role) => (
                <MenuItem key={role} value={role}>{getRoleLabel(role)}</MenuItem>
              ))}
            </Select>
          </FormControl>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setRoleDialog({ open: false, user: null, newRole: '' })}>Cancel</Button>
          <Button
            variant="contained"
            onClick={handleChangeRole}
            disabled={roleDialog.newRole === roleDialog.user?.role}
          >
            Change Role
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default AdminUsersPage;
