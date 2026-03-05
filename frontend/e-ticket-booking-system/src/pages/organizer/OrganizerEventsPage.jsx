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
  IconButton,
  Tooltip,
} from '@mui/material';
import { Add, Edit, Publish, Cancel, Visibility } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useSnackbar } from 'notistack';
import { eventApi } from '../../api';
import { LoadingScreen, StatusChip } from '../../components/common';
import { formatDateTime, getErrorMessage } from '../../utils/helpers';

const OrganizerEventsPage = () => {
  const navigate = useNavigate();
  const { enqueueSnackbar } = useSnackbar();
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [confirmDialog, setConfirmDialog] = useState({ open: false, action: null, event: null });

  const fetchEvents = async () => {
    try {
      const res = await eventApi.getMyEvents();
      setEvents(res.data || []);
    } catch (err) {
      enqueueSnackbar(getErrorMessage(err), { variant: 'error' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchEvents(); }, []);

  const handlePublish = async (event) => {
    try {
      await eventApi.publishEvent(event.id);
      enqueueSnackbar('Event published successfully', { variant: 'success' });
      fetchEvents();
    } catch (err) {
      enqueueSnackbar(getErrorMessage(err), { variant: 'error' });
    }
    setConfirmDialog({ open: false, action: null, event: null });
  };

  const handleCancel = async (event) => {
    try {
      await eventApi.cancelEvent(event.id);
      enqueueSnackbar('Event cancelled', { variant: 'success' });
      fetchEvents();
    } catch (err) {
      enqueueSnackbar(getErrorMessage(err), { variant: 'error' });
    }
    setConfirmDialog({ open: false, action: null, event: null });
  };

  if (loading) return <LoadingScreen />;

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h5" fontWeight={700}>My Events</Typography>
        <Button
          variant="contained"
          startIcon={<Add />}
          onClick={() => navigate('/organizer/events/create')}
        >
          Create Event
        </Button>
      </Box>

      <TableContainer component={Paper} variant="outlined">
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Name</TableCell>
              <TableCell>Category</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Created</TableCell>
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {events.map((event) => (
              <TableRow key={event.id} hover>
                <TableCell>
                  <Typography variant="body2" fontWeight={600}>{event.name}</Typography>
                </TableCell>
                <TableCell>{event.category?.name || '-'}</TableCell>
                <TableCell><StatusChip status={event.status} /></TableCell>
                <TableCell>{formatDateTime(event.createdAt)}</TableCell>
                <TableCell align="right">
                  <Box sx={{ display: 'flex', gap: 0.5, justifyContent: 'flex-end' }}>
                    <Tooltip title="View">
                      <IconButton size="small" onClick={() => navigate(`/events/${event.id}`)}>
                        <Visibility fontSize="small" />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="Edit">
                      <IconButton size="small" onClick={() => navigate(`/organizer/events/${event.id}/edit`)}>
                        <Edit fontSize="small" />
                      </IconButton>
                    </Tooltip>
                    {event.status === 'DRAFT' && (
                      <Tooltip title="Publish">
                        <IconButton
                          size="small"
                          color="success"
                          onClick={() => setConfirmDialog({ open: true, action: 'publish', event })}
                        >
                          <Publish fontSize="small" />
                        </IconButton>
                      </Tooltip>
                    )}
                    {['DRAFT', 'PUBLISHED'].includes(event.status) && (
                      <Tooltip title="Cancel">
                        <IconButton
                          size="small"
                          color="error"
                          onClick={() => setConfirmDialog({ open: true, action: 'cancel', event })}
                        >
                          <Cancel fontSize="small" />
                        </IconButton>
                      </Tooltip>
                    )}
                  </Box>
                </TableCell>
              </TableRow>
            ))}
            {events.length === 0 && (
              <TableRow>
                <TableCell colSpan={5} align="center" sx={{ py: 4, color: 'text.secondary' }}>
                  No events yet
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Confirm Dialog */}
      <Dialog open={confirmDialog.open} onClose={() => setConfirmDialog({ open: false, action: null, event: null })}>
        <DialogTitle>
          {confirmDialog.action === 'publish' ? 'Publish Event' : 'Cancel Event'}
        </DialogTitle>
        <DialogContent>
          <Typography>
            Are you sure you want to {confirmDialog.action} "{confirmDialog.event?.name}"?
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmDialog({ open: false, action: null, event: null })}>No</Button>
          <Button
            variant="contained"
            color={confirmDialog.action === 'publish' ? 'success' : 'error'}
            onClick={() =>
              confirmDialog.action === 'publish'
                ? handlePublish(confirmDialog.event)
                : handleCancel(confirmDialog.event)
            }
          >
            Yes, {confirmDialog.action}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default OrganizerEventsPage;
