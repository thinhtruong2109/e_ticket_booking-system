import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Container,
  Grid,
  Typography,
  Box,
  Button,
  Chip,
  Card,
  CardContent,
  CardMedia,
  Paper,
} from '@mui/material';
import {
  Search,
  ConfirmationNumber,
  TrendingUp,
  Security,
  ArrowForward,
} from '@mui/icons-material';
import { eventApi, categoryApi } from '../api';
import EventCard from '../components/events/EventCard';
import { LoadingScreen } from '../components/common';

const HomePage = () => {
  const navigate = useNavigate();
  const [events, setEvents] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      try {
        const [evRes, catRes] = await Promise.all([
          eventApi.getAll(),
          categoryApi.getAll(),
        ]);
        setEvents(Array.isArray(evRes.data) ? evRes.data.slice(0, 6) : []);
        setCategories(Array.isArray(catRes.data) ? catRes.data : []);
      } catch {
        /* silent */
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  const features = [
    { icon: <Search sx={{ fontSize: 40 }} />, title: 'Discover Events', desc: 'Browse thousands of events near you — concerts, sports, theatre and more.' },
    { icon: <ConfirmationNumber sx={{ fontSize: 40 }} />, title: 'Instant E-Tickets', desc: 'Get your e-tickets instantly after booking. No printing needed.' },
    { icon: <TrendingUp sx={{ fontSize: 40 }} />, title: 'Ticket Marketplace', desc: 'Buy, sell or trade tickets safely with other fans through our marketplace.' },
    { icon: <Security sx={{ fontSize: 40 }} />, title: 'Secure Payments', desc: 'Pay with confidence via VNPay, MoMo or Stripe with full buyer protection.' },
  ];

  if (loading) return <LoadingScreen />;

  return (
    <>
      {/* Hero */}
      <Box sx={{
        bgcolor: '#111827',
        color: '#fff',
        py: { xs: 8, md: 12 },
        textAlign: 'center',
        position: 'relative',
        overflow: 'hidden',
      }}>
        <Box sx={{
          position: 'absolute', top: 0, left: 0, right: 0, bottom: 0,
          background: 'radial-gradient(circle at 50% 0%, rgba(99,102,241,0.15), transparent 70%)',
        }} />
        <Container maxWidth="md" sx={{ position: 'relative', zIndex: 1 }}>
          <Typography variant="h2" fontWeight={800} sx={{ mb: 2, fontSize: { xs: '2rem', md: '3rem' } }}>
            Your Next Experience
            <br />
            <Box component="span" sx={{ color: '#818cf8' }}>Starts Here</Box>
          </Typography>
          <Typography variant="h6" sx={{ color: '#9ca3af', mb: 4, fontWeight: 400, maxWidth: 600, mx: 'auto' }}>
            Discover, book, and manage tickets for the events you love. Fast, secure, and hassle-free.
          </Typography>
          <Box sx={{ display: 'flex', gap: 2, justifyContent: 'center', flexWrap: 'wrap' }}>
            <Button
              variant="contained"
              size="large"
              endIcon={<ArrowForward />}
              onClick={() => navigate('/events')}
              sx={{ px: 4, py: 1.5, fontSize: '1rem', bgcolor: '#6366f1', '&:hover': { bgcolor: '#4f46e5' } }}
            >
              Browse Events
            </Button>
            <Button
              variant="outlined"
              size="large"
              onClick={() => navigate('/marketplace')}
              sx={{ px: 4, py: 1.5, fontSize: '1rem', color: '#fff', borderColor: 'rgba(255,255,255,0.3)', '&:hover': { borderColor: '#fff', bgcolor: 'rgba(255,255,255,0.05)' } }}
            >
              Marketplace
            </Button>
          </Box>
        </Container>
      </Box>

      {/* Categories */}
      {categories.length > 0 && (
        <Container maxWidth="lg" sx={{ py: 6 }}>
          <Typography variant="h5" fontWeight={700} gutterBottom>Browse by Category</Typography>
          <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
            Find events that match your interests
          </Typography>
          <Box sx={{ display: 'flex', gap: 1.5, flexWrap: 'wrap' }}>
            {categories.map((cat) => (
              <Chip
                key={cat.id}
                label={cat.name}
                onClick={() => navigate(`/events?category=${cat.id}`)}
                variant="outlined"
                sx={{ fontSize: '0.9rem', py: 2.5, px: 1, borderRadius: 2, cursor: 'pointer', '&:hover': { bgcolor: '#111827', color: '#fff', borderColor: '#111827' } }}
              />
            ))}
          </Box>
        </Container>
      )}

      {/* Featured Events */}
      {events.length > 0 && (
        <Box sx={{ bgcolor: '#f8f9fa', py: 6 }}>
          <Container maxWidth="lg">
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
              <Box>
                <Typography variant="h5" fontWeight={700}>Featured Events</Typography>
                <Typography variant="body1" color="text.secondary">Don't miss out on these popular events</Typography>
              </Box>
              <Button endIcon={<ArrowForward />} onClick={() => navigate('/events')}>View All</Button>
            </Box>
            <Grid container spacing={3}>
              {events.map((event) => (
                <Grid item xs={12} sm={6} md={4} key={event.id}>
                  <EventCard event={event} />
                </Grid>
              ))}
            </Grid>
          </Container>
        </Box>
      )}

      {/* Features */}
      <Container maxWidth="lg" sx={{ py: 8 }}>
        <Box sx={{ textAlign: 'center', mb: 5 }}>
          <Typography variant="h5" fontWeight={700}>Why Choose Us</Typography>
          <Typography variant="body1" color="text.secondary">
            Everything you need for a seamless event experience
          </Typography>
        </Box>
        <Grid container spacing={4}>
          {features.map((f, i) => (
            <Grid item xs={12} sm={6} md={3} key={i}>
              <Paper sx={{ p: 3, textAlign: 'center', height: '100%', bgcolor: 'transparent', boxShadow: 'none', border: '1px solid', borderColor: 'divider', borderRadius: 3 }}>
                <Box sx={{ color: '#111827', mb: 2 }}>{f.icon}</Box>
                <Typography variant="subtitle1" fontWeight={700} gutterBottom>{f.title}</Typography>
                <Typography variant="body2" color="text.secondary">{f.desc}</Typography>
              </Paper>
            </Grid>
          ))}
        </Grid>
      </Container>

      {/* CTA */}
      <Box sx={{ bgcolor: '#111827', color: '#fff', py: 8, textAlign: 'center' }}>
        <Container maxWidth="sm">
          <Typography variant="h5" fontWeight={700} sx={{ mb: 2 }}>Ready to Get Started?</Typography>
          <Typography variant="body1" sx={{ color: '#9ca3af', mb: 3 }}>
            Join thousands of event-goers and organizers on our platform.
          </Typography>
          <Button
            variant="contained"
            size="large"
            onClick={() => navigate('/register')}
            sx={{ px: 5, py: 1.5, bgcolor: '#6366f1', '&:hover': { bgcolor: '#4f46e5' } }}
          >
            Create Free Account
          </Button>
        </Container>
      </Box>
    </>
  );
};

export default HomePage;
