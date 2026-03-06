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
  TextField,
  Chip,
  InputAdornment,
} from '@mui/material';
import { Search } from '@mui/icons-material';
import { useSnackbar } from 'notistack';
import { transactionHistoryApi } from '../../api';
import { LoadingScreen, StatusChip } from '../../components/common';
import { formatDateTime, formatCurrency, getErrorMessage } from '../../utils/helpers';

const AdminTransactionsPage = () => {
  const { enqueueSnackbar } = useSnackbar();
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [userIdFilter, setUserIdFilter] = useState('');

  const fetchTransactions = async () => {
    setLoading(true);
    try {
      let res;
      if (userIdFilter.trim()) {
        res = await transactionHistoryApi.adminGetByUserId(userIdFilter.trim());
      } else {
        res = await transactionHistoryApi.adminGetAll();
      }
      setTransactions(res.data || []);
    } catch (err) {
      enqueueSnackbar(getErrorMessage(err), { variant: 'error' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchTransactions(); }, []);

  const handleSearch = (e) => {
    e.preventDefault();
    fetchTransactions();
  };

  const getTypeColor = (type) => {
    switch (type) {
      case 'PAYMENT': return 'error';
      case 'REFUND': return 'warning';
      case 'PAYOUT': return 'info';
      case 'COMMISSION': return 'secondary';
      default: return 'default';
    }
  };

  return (
    <Box>
      <Typography variant="h5" fontWeight={700} gutterBottom>Transaction History</Typography>

      <Box component="form" onSubmit={handleSearch} sx={{ mb: 3, maxWidth: 400 }}>
        <TextField
          label="Filter by User ID"
          size="small"
          fullWidth
          value={userIdFilter}
          onChange={(e) => setUserIdFilter(e.target.value)}
          placeholder="Enter user ID to filter..."
          InputProps={{
            endAdornment: (
              <InputAdornment position="end">
                <Search sx={{ cursor: 'pointer' }} onClick={fetchTransactions} />
              </InputAdornment>
            ),
          }}
        />
      </Box>

      {loading ? (
        <LoadingScreen />
      ) : (
        <TableContainer component={Paper} variant="outlined">
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>ID</TableCell>
                <TableCell>User</TableCell>
                <TableCell>Type</TableCell>
                <TableCell>Amount</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Booking ID</TableCell>
                <TableCell>Description</TableCell>
                <TableCell>Date</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {transactions.map((tx) => (
                <TableRow key={tx.id} hover>
                  <TableCell>{tx.id}</TableCell>
                  <TableCell>{tx.userId || tx.userEmail || '-'}</TableCell>
                  <TableCell>
                    <Chip label={tx.type || tx.transactionType || '-'} color={getTypeColor(tx.type || tx.transactionType)} size="small" />
                  </TableCell>
                  <TableCell sx={{ fontWeight: 600 }}>{formatCurrency(tx.amount)}</TableCell>
                  <TableCell>
                    <StatusChip status={tx.status || 'COMPLETED'} />
                  </TableCell>
                  <TableCell>{tx.bookingId || '-'}</TableCell>
                  <TableCell sx={{ maxWidth: 200, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                    {tx.description || '-'}
                  </TableCell>
                  <TableCell>{formatDateTime(tx.createdAt)}</TableCell>
                </TableRow>
              ))}
              {transactions.length === 0 && (
                <TableRow>
                  <TableCell colSpan={8} align="center" sx={{ py: 4, color: 'text.secondary' }}>
                    No transactions found
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}
    </Box>
  );
};

export default AdminTransactionsPage;
