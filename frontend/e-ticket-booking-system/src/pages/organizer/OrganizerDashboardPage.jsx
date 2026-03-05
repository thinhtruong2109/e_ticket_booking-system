import { useState, useEffect } from 'react';
import {
  Grid,
  Card,
  CardContent,
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
} from '@mui/material';
import { Event, AttachMoney, ConfirmationNumber, AccountBalanceWallet } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { eventApi, walletApi } from '../../api';
import { LoadingScreen } from '../../components/common';
import { StatusChip } from '../../components/common';
import { formatCurrency, formatDateTime, getErrorMessage } from '../../utils/helpers';

const OrganizerDashboardPage = () => {
  const navigate = useNavigate();
  const [events, setEvents] = useState([]);
  const [wallet, setWallet] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [eventsRes, walletRes] = await Promise.all([
          eventApi.getMyEvents(),
          walletApi.getMyWallet().catch(() => ({ data: null })),
        ]);
        setEvents(eventsRes.data || []);
        setWallet(walletRes.data);
      } catch (err) {
        console.error(getErrorMessage(err));
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  if (loading) return <LoadingScreen />;

  const activeEvents = events.filter((e) => ['PUBLISHED', 'ONGOING'].includes(e.status));
  const confirmedBookingsCount = events.reduce((sum, e) => sum + (e.confirmedBookings || 0), 0);

  const stats = [
    { label: 'Active Events', value: activeEvents.length, icon: <Event />, color: 'primary.main' },
    { label: 'Total Events', value: events.length, icon: <ConfirmationNumber />, color: 'info.main' },
    { label: 'Wallet Balance', value: formatCurrency(wallet?.balance || 0), icon: <AccountBalanceWallet />, color: 'success.main' },
    { label: 'Total Withdrawn', value: formatCurrency(wallet?.totalWithdrawn || 0), icon: <AttachMoney />, color: 'warning.main' },
  ];

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h5" fontWeight={700}>Dashboard</Typography>
      </Box>

      {/* Stats Cards */}
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

      {/* Quick Actions */}
      <Box sx={{ display: 'flex', gap: 2, mb: 4, flexWrap: 'wrap' }}>
        <Button variant="contained" onClick={() => navigate('/organizer/events/create')}>
          Create New Event
        </Button>
        <Button variant="outlined" onClick={() => navigate('/organizer/wallet')}>
          Go to Wallet
        </Button>
        <Button variant="outlined" onClick={() => navigate('/organizer/promo-codes')}>
          Manage Promo Codes
        </Button>
      </Box>

      {/* Recent Events */}
      <Typography variant="h6" fontWeight={600} sx={{ mb: 2 }}>Recent Events</Typography>
      <TableContainer component={Paper} variant="outlined">
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Name</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Created</TableCell>
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {events.slice(0, 5).map((event) => (
              <TableRow key={event.id} hover>
                <TableCell>{event.name}</TableCell>
                <TableCell><StatusChip status={event.status} /></TableCell>
                <TableCell>{formatDateTime(event.createdAt)}</TableCell>
                <TableCell align="right">
                  <Button size="small" onClick={() => navigate(`/organizer/events/${event.id}/edit`)}>
                    Edit
                  </Button>
                </TableCell>
              </TableRow>
            ))}
            {events.length === 0 && (
              <TableRow>
                <TableCell colSpan={4} align="center" sx={{ py: 4, color: 'text.secondary' }}>
                  No events yet. Create your first event!
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
};

export default OrganizerDashboardPage;
