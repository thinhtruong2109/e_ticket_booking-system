import { useState, useEffect } from 'react';
import {
  Typography,
  Box,
  Grid,
  Card,
  CardContent,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
} from '@mui/material';
import { People, Event, ConfirmationNumber, AttachMoney } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { userApi, eventApi, transactionHistoryApi } from '../../api';
import { LoadingScreen, StatusChip } from '../../components/common';
import { formatDateTime, formatCurrency, getErrorMessage } from '../../utils/helpers';

const AdminDashboardPage = () => {
  const navigate = useNavigate();
  const [users, setUsers] = useState([]);
  const [events, setEvents] = useState([]);
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [usersRes, eventsRes, txRes] = await Promise.all([
          userApi.getAllUsers(),
          eventApi.getAllEvents(),
          transactionHistoryApi.adminGetAll().catch(() => ({ data: [] })),
        ]);
        setUsers(usersRes.data || []);
        setEvents(eventsRes.data || []);
        setTransactions(txRes.data || []);
      } catch (err) {
        console.error(getErrorMessage(err));
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  if (loading) return <LoadingScreen />;

  const totalRevenue = transactions
    .filter((t) => t.transactionType === 'PAYMENT' && t.status === 'SUCCESS')
    .reduce((sum, t) => sum + (t.amount || 0), 0);

  const stats = [
    { label: 'Total Users', value: users.length, icon: <People />, color: 'primary.main' },
    { label: 'Total Events', value: events.length, icon: <Event />, color: 'info.main' },
    { label: 'Confirmed Bookings', value: transactions.filter((t) => t.status === 'SUCCESS').length, icon: <ConfirmationNumber />, color: 'success.main' },
    { label: 'Total Revenue', value: formatCurrency(totalRevenue), icon: <AttachMoney />, color: 'warning.main' },
  ];

  return (
    <Box>
      <Typography variant="h5" fontWeight={700} sx={{ mb: 3 }}>Admin Dashboard</Typography>

      <Grid container spacing={2} sx={{ mb: 4 }}>
        {stats.map((stat) => (
          <Grid size={{ xs: 12, sm: 6, md: 3 }} key={stat.label}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                  <Box sx={{ p: 1, borderRadius: 1, bgcolor: stat.color, color: 'white', display: 'flex' }}>
                    {stat.icon}
                  </Box>
                  <Box>
                    <Typography variant="caption" color="text.secondary">{stat.label}</Typography>
                    <Typography variant="h6" fontWeight={700}>{stat.value}</Typography>
                  </Box>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      {/* Recent Events */}
      <Typography variant="h6" fontWeight={600} sx={{ mb: 2 }}>Recent Events</Typography>
      <TableContainer component={Paper} variant="outlined" sx={{ mb: 4 }}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Name</TableCell>
              <TableCell>Organizer</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Created</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {events.slice(0, 5).map((event) => (
              <TableRow key={event.id} hover sx={{ cursor: 'pointer' }} onClick={() => navigate(`/events/${event.id}`)}>
                <TableCell>{event.name}</TableCell>
                <TableCell>{event.organizer?.fullName || '-'}</TableCell>
                <TableCell><StatusChip status={event.status} /></TableCell>
                <TableCell>{formatDateTime(event.createdAt)}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Recent Users */}
      <Typography variant="h6" fontWeight={600} sx={{ mb: 2 }}>Recent Users</Typography>
      <TableContainer component={Paper} variant="outlined">
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Name</TableCell>
              <TableCell>Email</TableCell>
              <TableCell>Role</TableCell>
              <TableCell>Status</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {users.slice(0, 5).map((u) => (
              <TableRow key={u.id} hover>
                <TableCell>{u.fullName}</TableCell>
                <TableCell>{u.email}</TableCell>
                <TableCell>{u.role}</TableCell>
                <TableCell><StatusChip status={u.status} /></TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
};

export default AdminDashboardPage;
