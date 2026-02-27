import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Container,
  Grid,
  Paper,
  Typography,
  Box,
  Button,
  Chip,
  Divider,
} from '@mui/material';
import { ShoppingCart, SwapHoriz } from '@mui/icons-material';
import { ticketListingApi } from '../../api';
import { LoadingScreen, ErrorAlert, EmptyState, PageHeader } from '../../components/common';
import { formatCurrency, getErrorMessage } from '../../utils/helpers';
import { useAuth } from '../../contexts/AuthContext';

const MarketplacePage = () => {
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const [listings, setListings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchListings();
  }, []);

  const fetchListings = async () => {
    setLoading(true);
    try {
      const res = await ticketListingApi.getAll();
      setListings(Array.isArray(res.data) ? res.data : []);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const handleBuy = (listingId) => {
    if (!isAuthenticated) {
      navigate('/login', { state: { from: { pathname: '/marketplace' } } });
      return;
    }
    navigate(`/marketplace/${listingId}`);
  };

  return (
    <>
      <PageHeader
        title="Marketplace"
        subtitle="Buy and trade tickets from other users"
        action={
          isAuthenticated && (
            <Button
              variant="outlined"
              onClick={() => navigate('/marketplace/my-listings')}
              sx={{ color: 'white', borderColor: 'rgba(255,255,255,0.3)', '&:hover': { borderColor: 'rgba(255,255,255,0.5)' } }}
            >
              My Listings
            </Button>
          )
        }
      />
      <Container maxWidth="lg" sx={{ py: 4 }}>
        {error && <ErrorAlert message={error} onRetry={fetchListings} />}

        {loading ? (
          <LoadingScreen />
        ) : listings.length === 0 ? (
          <EmptyState
            title="No listings available"
            description="There are no tickets available for sale or trade right now"
          />
        ) : (
          <Grid container spacing={3}>
            {listings.map((listing) => (
              <Grid item xs={12} sm={6} md={4} key={listing.id}>
                <Paper sx={{ p: 0, overflow: 'hidden' }}>
                  <Box sx={{ p: 2.5 }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                      <Typography variant="subtitle2" fontWeight={600}>
                        {listing.eventName || 'Event Ticket'}
                      </Typography>
                      <Chip
                        label={listing.exchangeType}
                        size="small"
                        variant="outlined"
                        icon={listing.exchangeType === 'TRADE' ? <SwapHoriz /> : undefined}
                      />
                    </Box>
                    {listing.ticketTypeName && (
                      <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                        {listing.ticketTypeName}
                      </Typography>
                    )}
                    {listing.description && (
                      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                        {listing.description}
                      </Typography>
                    )}

                    <Divider sx={{ my: 1.5 }} />

                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <Box>
                        <Typography variant="caption" color="text.secondary">Price</Typography>
                        <Typography variant="h6" fontWeight={700}>
                          {formatCurrency(listing.listingPrice)}
                        </Typography>
                      </Box>
                      <Button
                        variant="contained"
                        size="small"
                        startIcon={<ShoppingCart />}
                        onClick={() => handleBuy(listing.id)}
                      >
                        {listing.exchangeType === 'TRADE' ? 'Trade' : 'Buy'}
                      </Button>
                    </Box>
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

export default MarketplacePage;
