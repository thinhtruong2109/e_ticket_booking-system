import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Container,
  Paper,
  Typography,
  Box,
  Button,
  Divider,
  Alert,
  Grid,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
} from '@mui/material';
import { ArrowBack, ShoppingCart, SwapHoriz } from '@mui/icons-material';
import { ticketListingApi, ticketApi } from '../../api';
import { LoadingScreen, ErrorAlert, PageHeader } from '../../components/common';
import { formatCurrency, getErrorMessage } from '../../utils/helpers';
import { PAYMENT_METHODS } from '../../utils/constants';

const ListingDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [listing, setListing] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [paymentMethod, setPaymentMethod] = useState('VNPAY');

  // For trade
  const [myTickets, setMyTickets] = useState([]);
  const [tradeTicketId, setTradeTicketId] = useState('');

  useEffect(() => {
    fetchListing();
  }, [id]);

  const fetchListing = async () => {
    setLoading(true);
    try {
      const res = await ticketListingApi.getById(id);
      setListing(res.data);

      if (res.data.exchangeType === 'TRADE' || res.data.exchangeType === 'BOTH') {
        const ticketsRes = await ticketApi.getMyTickets();
        const available = (Array.isArray(ticketsRes.data) ? ticketsRes.data : [])
          .filter((t) => t.isTransferable && !t.isCheckedIn);
        setMyTickets(available);
      }
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const handlePurchase = async () => {
    setSubmitting(true);
    setError('');
    try {
      await ticketListingApi.createExchange({
        ticketListingId: parseInt(id),
        transactionType: 'PURCHASE',
        paymentMethod,
      });
      navigate('/my-bookings');
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  const handleTrade = async () => {
    if (!tradeTicketId) return;
    setSubmitting(true);
    setError('');
    try {
      await ticketListingApi.createExchange({
        ticketListingId: parseInt(id),
        transactionType: 'TRADE',
        tradeTicketId: parseInt(tradeTicketId),
      });
      navigate('/my-tickets');
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <LoadingScreen />;
  if (!listing) return <Container sx={{ py: 4 }}><ErrorAlert message="Listing not found" /></Container>;

  return (
    <>
      <PageHeader title="Ticket Listing" subtitle={listing.eventName || 'Marketplace'} />
      <Container maxWidth="md" sx={{ py: 4 }}>
        <Button startIcon={<ArrowBack />} onClick={() => navigate('/marketplace')} sx={{ mb: 3 }}>
          Back to Marketplace
        </Button>

        {error && <ErrorAlert message={error} />}

        <Grid container spacing={3}>
          <Grid item xs={12} md={7}>
            <Paper sx={{ p: 3 }}>
              <Typography variant="h6" fontWeight={600} gutterBottom>
                Listing Details
              </Typography>
              <Divider sx={{ mb: 2 }} />

              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
                {listing.eventName && (
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Typography variant="body2" color="text.secondary">Event</Typography>
                    <Typography variant="body2" fontWeight={500}>{listing.eventName}</Typography>
                  </Box>
                )}
                {listing.ticketTypeName && (
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Typography variant="body2" color="text.secondary">Ticket Type</Typography>
                    <Typography variant="body2" fontWeight={500}>{listing.ticketTypeName}</Typography>
                  </Box>
                )}
                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                  <Typography variant="body2" color="text.secondary">Exchange Type</Typography>
                  <Typography variant="body2" fontWeight={500}>{listing.exchangeType}</Typography>
                </Box>
                {listing.description && (
                  <Box>
                    <Typography variant="body2" color="text.secondary">Description</Typography>
                    <Typography variant="body2" sx={{ mt: 0.5 }}>{listing.description}</Typography>
                  </Box>
                )}
              </Box>

              <Box sx={{ mt: 3, p: 2, bgcolor: 'grey.50', borderRadius: 1, border: '1px solid', borderColor: 'divider' }}>
                <Typography variant="caption" color="text.secondary">Listed Price</Typography>
                <Typography variant="h4" fontWeight={700}>
                  {formatCurrency(listing.listingPrice)}
                </Typography>
              </Box>
            </Paper>
          </Grid>

          <Grid item xs={12} md={5}>
            {/* Purchase */}
            {(listing.exchangeType === 'SELL' || listing.exchangeType === 'BOTH') && (
              <Paper sx={{ p: 3, mb: 3 }}>
                <Typography variant="h6" fontWeight={600} gutterBottom>
                  Buy This Ticket
                </Typography>
                <Divider sx={{ mb: 2 }} />
                <FormControl fullWidth sx={{ mb: 2 }}>
                  <InputLabel>Payment Method</InputLabel>
                  <Select value={paymentMethod} onChange={(e) => setPaymentMethod(e.target.value)} label="Payment Method">
                    {PAYMENT_METHODS.map((m) => (
                      <MenuItem key={m} value={m}>{m}</MenuItem>
                    ))}
                  </Select>
                </FormControl>
                <Button
                  variant="contained"
                  fullWidth
                  startIcon={<ShoppingCart />}
                  onClick={handlePurchase}
                  disabled={submitting}
                >
                  {submitting ? 'Processing...' : `Pay ${formatCurrency(listing.listingPrice)}`}
                </Button>
              </Paper>
            )}

            {/* Trade */}
            {(listing.exchangeType === 'TRADE' || listing.exchangeType === 'BOTH') && (
              <Paper sx={{ p: 3 }}>
                <Typography variant="h6" fontWeight={600} gutterBottom>
                  Trade Your Ticket
                </Typography>
                <Divider sx={{ mb: 2 }} />
                {myTickets.length === 0 ? (
                  <Typography variant="body2" color="text.secondary">
                    You don't have any transferable tickets to trade
                  </Typography>
                ) : (
                  <>
                    <FormControl fullWidth sx={{ mb: 2 }}>
                      <InputLabel>Your Ticket</InputLabel>
                      <Select value={tradeTicketId} onChange={(e) => setTradeTicketId(e.target.value)} label="Your Ticket">
                        {myTickets.map((t) => (
                          <MenuItem key={t.id} value={t.id}>
                            {t.ticketCode} - {t.eventName || 'Ticket'}
                          </MenuItem>
                        ))}
                      </Select>
                    </FormControl>
                    <Button
                      variant="outlined"
                      fullWidth
                      startIcon={<SwapHoriz />}
                      onClick={handleTrade}
                      disabled={submitting || !tradeTicketId}
                    >
                      {submitting ? 'Processing...' : 'Propose Trade'}
                    </Button>
                  </>
                )}
              </Paper>
            )}
          </Grid>
        </Grid>
      </Container>
    </>
  );
};

export default ListingDetailPage;
