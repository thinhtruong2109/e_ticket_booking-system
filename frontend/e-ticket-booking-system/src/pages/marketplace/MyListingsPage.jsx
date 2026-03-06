import { useState, useEffect } from 'react';
import {
  Typography,
  Box,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  IconButton,
  Tooltip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Chip,
} from '@mui/material';
import { Cancel, Visibility } from '@mui/icons-material';
import { useSnackbar } from 'notistack';
import { ticketListingApi } from '../../api';
import { LoadingScreen, StatusChip, EmptyState } from '../../components/common';
import { formatCurrency, formatDateTime, getErrorMessage } from '../../utils/helpers';

const MyListingsPage = () => {
  const { enqueueSnackbar } = useSnackbar();
  const [listings, setListings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [cancelDialog, setCancelDialog] = useState({ open: false, id: null });

  const fetchListings = async () => {
    try {
      const res = await ticketListingApi.getMyListings();
      setListings(res.data || []);
    } catch (err) {
      enqueueSnackbar(getErrorMessage(err), { variant: 'error' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchListings(); }, []);

  const handleCancel = async () => {
    try {
      await ticketListingApi.cancel(cancelDialog.id);
      enqueueSnackbar('Listing cancelled', { variant: 'success' });
      setCancelDialog({ open: false, id: null });
      fetchListings();
    } catch (err) {
      enqueueSnackbar(getErrorMessage(err), { variant: 'error' });
    }
  };

  if (loading) return <LoadingScreen />;

  if (listings.length === 0) {
    return (
      <Box sx={{ py: 4 }}>
        <Typography variant="h5" fontWeight={700} gutterBottom>My Listings</Typography>
        <EmptyState message="You haven't listed any tickets for resale yet." />
      </Box>
    );
  }

  return (
    <Box sx={{ py: 4 }}>
      <Typography variant="h5" fontWeight={700} gutterBottom>My Listings</Typography>

      <TableContainer component={Paper} variant="outlined">
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Event</TableCell>
              <TableCell>Ticket Type</TableCell>
              <TableCell>Asking Price</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Listed At</TableCell>
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {listings.map((l) => (
              <TableRow key={l.id} hover>
                <TableCell>
                  <Typography variant="body2" fontWeight={600}>{l.eventName || l.event?.name || '-'}</Typography>
                </TableCell>
                <TableCell>{l.ticketTypeName || l.ticketType?.name || '-'}</TableCell>
                <TableCell sx={{ fontWeight: 600 }}>{formatCurrency(l.askingPrice || l.price)}</TableCell>
                <TableCell>
                  <StatusChip status={l.status} />
                </TableCell>
                <TableCell>{formatDateTime(l.createdAt)}</TableCell>
                <TableCell align="right">
                  {l.status === 'FOR_SALE' && (
                    <Tooltip title="Cancel Listing">
                      <IconButton color="error" size="small" onClick={() => setCancelDialog({ open: true, id: l.id })}>
                        <Cancel fontSize="small" />
                      </IconButton>
                    </Tooltip>
                  )}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={cancelDialog.open} onClose={() => setCancelDialog({ open: false, id: null })}>
        <DialogTitle>Cancel Listing?</DialogTitle>
        <DialogContent>
          <Typography>This listing will be removed from the marketplace. The ticket will be returned to your account.</Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCancelDialog({ open: false, id: null })}>Keep</Button>
          <Button color="error" variant="contained" onClick={handleCancel}>Cancel Listing</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default MyListingsPage;
