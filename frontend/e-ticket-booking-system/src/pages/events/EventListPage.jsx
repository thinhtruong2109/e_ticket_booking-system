import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import {
  Container,
  Grid,
  Box,
  TextField,
  InputAdornment,
  Chip,
  Typography,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
} from '@mui/material';
import { Search } from '@mui/icons-material';
import { eventApi, categoryApi } from '../../api';
import EventCard from '../../components/events/EventCard';
import { PageHeader, LoadingScreen, EmptyState, ErrorAlert } from '../../components/common';
import { getErrorMessage } from '../../utils/helpers';

const EventListPage = () => {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();

  const [events, setEvents] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchName, setSearchName] = useState(searchParams.get('name') || '');
  const [selectedCategory, setSelectedCategory] = useState(searchParams.get('categoryId') || '');

  useEffect(() => {
    categoryApi.getAll().then((res) => setCategories(res.data)).catch(() => {});
  }, []);

  useEffect(() => {
    fetchEvents();
  }, [selectedCategory]);

  const fetchEvents = async (name) => {
    setLoading(true);
    setError('');
    try {
      const params = {};
      if (selectedCategory) params.categoryId = selectedCategory;
      if (name || searchName) params.name = name || searchName;
      const res = await eventApi.getPublishedEvents(params);
      setEvents(Array.isArray(res.data) ? res.data : res.data.content || []);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    fetchEvents(searchName);
  };

  const handleCategoryChange = (catId) => {
    setSelectedCategory(catId === selectedCategory ? '' : catId);
  };

  return (
    <>
      <PageHeader title="Events" subtitle="Discover and book tickets for upcoming events" />
      <Container maxWidth="lg" sx={{ py: 4 }}>
        {/* Filters */}
        <Box sx={{ mb: 4 }}>
          <form onSubmit={handleSearch}>
            <TextField
              fullWidth
              placeholder="Search events..."
              value={searchName}
              onChange={(e) => setSearchName(e.target.value)}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <Search sx={{ color: 'grey.400' }} />
                  </InputAdornment>
                ),
              }}
              sx={{ mb: 2 }}
            />
          </form>

          {categories.length > 0 && (
            <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
              <Chip
                label="All"
                onClick={() => setSelectedCategory('')}
                variant={selectedCategory === '' ? 'filled' : 'outlined'}
                sx={{
                  bgcolor: selectedCategory === '' ? 'grey.900' : undefined,
                  color: selectedCategory === '' ? 'white' : undefined,
                }}
              />
              {categories.map((cat) => (
                <Chip
                  key={cat.id}
                  label={cat.name}
                  onClick={() => handleCategoryChange(String(cat.id))}
                  variant={selectedCategory === String(cat.id) ? 'filled' : 'outlined'}
                  sx={{
                    bgcolor: selectedCategory === String(cat.id) ? 'grey.900' : undefined,
                    color: selectedCategory === String(cat.id) ? 'white' : undefined,
                  }}
                />
              ))}
            </Box>
          )}
        </Box>

        {error && <ErrorAlert message={error} onRetry={fetchEvents} />}

        {loading ? (
          <LoadingScreen />
        ) : events.length === 0 ? (
          <EmptyState
            title="No events found"
            description="Try adjusting your search or filter criteria"
          />
        ) : (
          <Grid container spacing={3}>
            {events.map((event) => (
              <Grid item xs={12} sm={6} md={4} key={event.id}>
                <EventCard event={event} />
              </Grid>
            ))}
          </Grid>
        )}
      </Container>
    </>
  );
};

export default EventListPage;
