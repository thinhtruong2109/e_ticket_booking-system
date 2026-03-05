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
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Chip,
} from '@mui/material';
import { useSnackbar } from 'notistack';
import { walletApi } from '../../api';
import { LoadingScreen } from '../../components/common';
import { formatCurrency, formatDateTime, getErrorMessage } from '../../utils/helpers';

const AdminWalletsPage = () => {
  const { enqueueSnackbar } = useSnackbar();
  const [wallets, setWallets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [txDialog, setTxDialog] = useState({ open: false, userId: null, userName: '' });
  const [transactions, setTransactions] = useState([]);
  const [txLoading, setTxLoading] = useState(false);

  useEffect(() => {
    walletApi.adminGetAll()
      .then((res) => setWallets(res.data || []))
      .catch((err) => enqueueSnackbar(getErrorMessage(err), { variant: 'error' }))
      .finally(() => setLoading(false));
  }, []);

  const handleViewTransactions = async (userId, userName) => {
    setTxDialog({ open: true, userId, userName });
    setTxLoading(true);
    try {
      const res = await walletApi.adminGetTransactionsByUserId(userId);
      setTransactions(res.data || []);
    } catch (err) {
      enqueueSnackbar(getErrorMessage(err), { variant: 'error' });
    } finally {
      setTxLoading(false);
    }
  };

  const getTxTypeColor = (type) => {
    switch (type) {
      case 'REVENUE': return 'success';
      case 'WITHDRAWAL': return 'warning';
      case 'REFUND_DEDUCTION': return 'error';
      default: return 'default';
    }
  };

  if (loading) return <LoadingScreen />;

  return (
    <Box>
      <Typography variant="h5" fontWeight={700} sx={{ mb: 3 }}>Organizer Wallets</Typography>

      <TableContainer component={Paper} variant="outlined">
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Organizer</TableCell>
              <TableCell>Balance</TableCell>
              <TableCell>Total Withdrawn</TableCell>
              <TableCell>Bank</TableCell>
              <TableCell>Account</TableCell>
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {wallets.map((w) => (
              <TableRow key={w.id} hover>
                <TableCell>
                  <Typography variant="body2" fontWeight={600}>{w.user?.fullName || w.organizerName || '-'}</Typography>
                </TableCell>
                <TableCell>
                  <Typography variant="body2" fontWeight={600} color="success.main">
                    {formatCurrency(w.balance || 0)}
                  </Typography>
                </TableCell>
                <TableCell>{formatCurrency(w.totalWithdrawn || 0)}</TableCell>
                <TableCell>{w.bankName || '-'}</TableCell>
                <TableCell>{w.bankAccountNumber || '-'}</TableCell>
                <TableCell align="right">
                  <Button
                    size="small"
                    onClick={() => handleViewTransactions(w.user?.id || w.userId, w.user?.fullName || w.organizerName)}
                  >
                    View Transactions
                  </Button>
                </TableCell>
              </TableRow>
            ))}
            {wallets.length === 0 && (
              <TableRow>
                <TableCell colSpan={6} align="center" sx={{ py: 4, color: 'text.secondary' }}>
                  No wallets found
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Transactions Dialog */}
      <Dialog open={txDialog.open} onClose={() => setTxDialog({ open: false, userId: null, userName: '' })} maxWidth="md" fullWidth>
        <DialogTitle>Transactions — {txDialog.userName}</DialogTitle>
        <DialogContent>
          {txLoading ? (
            <Box sx={{ textAlign: 'center', py: 4 }}>Loading...</Box>
          ) : (
            <TableContainer>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Type</TableCell>
                    <TableCell>Amount</TableCell>
                    <TableCell>Balance After</TableCell>
                    <TableCell>Reference</TableCell>
                    <TableCell>Date</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {transactions.map((tx) => (
                    <TableRow key={tx.id}>
                      <TableCell><Chip label={tx.transactionType} size="small" color={getTxTypeColor(tx.transactionType)} /></TableCell>
                      <TableCell>
                        <Typography variant="body2" fontWeight={600} color={tx.transactionType === 'REVENUE' ? 'success.main' : 'error.main'}>
                          {tx.transactionType === 'REVENUE' ? '+' : '-'}{formatCurrency(tx.amount)}
                        </Typography>
                      </TableCell>
                      <TableCell>{formatCurrency(tx.balanceAfter)}</TableCell>
                      <TableCell sx={{ fontFamily: 'monospace', fontSize: '0.8rem' }}>{tx.referenceCode || '-'}</TableCell>
                      <TableCell>{formatDateTime(tx.createdAt)}</TableCell>
                    </TableRow>
                  ))}
                  {transactions.length === 0 && (
                    <TableRow>
                      <TableCell colSpan={5} align="center" sx={{ py: 3 }}>No transactions</TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setTxDialog({ open: false, userId: null, userName: '' })}>Close</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default AdminWalletsPage;
