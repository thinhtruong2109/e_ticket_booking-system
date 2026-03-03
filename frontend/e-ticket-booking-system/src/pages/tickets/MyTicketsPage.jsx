import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Container,
  Grid,
  Paper,
  Typography,
  Box,
  Chip,
  Button,
  Divider,
} from '@mui/material';
import { QrCode2, Event, Store } from '@mui/icons-material';
import { ticketApi } from '../../api';
import { LoadingScreen, ErrorAlert, EmptyState, PageHeader, StatusChip } from '../../components/common';
import { formatCurrency, getErrorMessage } from '../../utils/helpers';

const MyTicketsPage = () => {
  const navigate = useNavigate();
  const [tickets, setTickets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchTickets();
  }, []);

  const fetchTickets = async () => {
    setLoading(true);
    try {
      const res = await ticketApi.getMyTickets();
      setTickets(Array.isArray(res.data) ? res.data : []);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <PageHeader title="My Tickets" subtitle="Your event tickets and QR codes" />
      <Container maxWidth="lg" sx={{ py: 4 }}>
        {error && <ErrorAlert message={error} onRetry={fetchTickets} />}

        {loading ? (
          <LoadingScreen />
        ) : tickets.length === 0 ? (
          <EmptyState
            title="No tickets yet"
            description="Book tickets for events to see them here"
            actionLabel="Browse Events"
            onClick={() => navigate('/events')}
          />
        ) : (
          <Grid container spacing={3}>
            {tickets.map((ticket) => (
              <Grid item xs={12} sm={6} md={4} key={ticket.id}>
                <Paper sx={{ overflow: 'hidden' }}>
                  {/* Ticket Header */}
                  <Box sx={{ bgcolor: 'grey.900', color: 'white', p: 2.5 }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                      <Typography variant="subtitle2" fontWeight={600}>
                        {ticket.eventName || 'Event'}
                      </Typography>
                      {ticket.checkedIn ? (
                        <Chip label="Used" size="small" sx={{ bgcolor: 'rgba(255,255,255,0.15)', color: 'white' }} />
                      ) : (
                        <Chip label="Valid" size="small" color="success" variant="outlined"
                          sx={{ borderColor: 'rgba(5,150,105,0.7)', color: '#34d399' }}
                        />
                      )}
                    </Box>
                    <Typography variant="caption" sx={{ color: 'grey.400' }}>
                      {ticket.ticketTypeName || ticket.ticketCode}
                    </Typography>
                  </Box>

                  {/* QR Code Area */}
                  <Box sx={{ display: 'flex', justifyContent: 'center', py: 3, bgcolor: 'grey.50' }}>
                    {ticket.qrCode ? (
                      <img src={ticket.qrCode} alt="QR Code" style={{ width: 140, height: 140 }} />
                    ) : (
                      <Box
                        sx={{
                          width: 140,
                          height: 140,
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          border: '2px dashed',
                          borderColor: 'grey.300',
                          borderRadius: 1,
                        }}
                      >
                        <QrCode2 sx={{ fontSize: 64, color: 'grey.300' }} />
                      </Box>
                    )}
                  </Box>

                  {/* Ticket Details */}
                  <Box sx={{ p: 2.5 }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                      <Typography variant="caption" color="text.secondary">Code</Typography>
                      <Typography variant="caption" fontWeight={600} fontFamily="monospace">
                        {ticket.ticketCode}
                      </Typography>
                    </Box>
                    {ticket.seatNumber && (
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                        <Typography variant="caption" color="text.secondary">Seat</Typography>
                        <Typography variant="caption" fontWeight={600}>{ticket.seatNumber}</Typography>
                      </Box>
                    )}
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                      <Typography variant="caption" color="text.secondary">Transferable</Typography>
                      <Typography variant="caption" fontWeight={600}>
                        {ticket.transferable ? 'Yes' : 'No'}
                      </Typography>
                    </Box>

                    {ticket.transferable && !ticket.checkedIn && (
                      <Button
                        fullWidth
                        variant="outlined"
                        size="small"
                        startIcon={<Store />}
                        sx={{ mt: 1.5 }}
                        onClick={() => navigate('/marketplace/create', { state: { ticketId: ticket.id } })}
                      >
                        Sell / Trade
                      </Button>
                    )}
                  </Box>
                </Paper>
              </Grid>
            ))}
          </Grid>
        )}
      </Container>
    </>
  );
};

export default MyTicketsPage;
