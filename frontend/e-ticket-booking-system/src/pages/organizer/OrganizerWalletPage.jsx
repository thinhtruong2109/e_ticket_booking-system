import { useState, useEffect } from 'react';
import {
  Typography,
  Box,
  Button,
  Card,
  CardContent,
  Grid,
  TextField,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Chip,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Alert,
} from '@mui/material';
import { AccountBalanceWallet, AccountBalance, TrendingDown } from '@mui/icons-material';
import { useSnackbar } from 'notistack';
import { walletApi } from '../../api';
import { LoadingScreen } from '../../components/common';
import { formatCurrency, formatDateTime, getErrorMessage } from '../../utils/helpers';

const OrganizerWalletPage = () => {
  const { enqueueSnackbar } = useSnackbar();
  const [wallet, setWallet] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filterType, setFilterType] = useState('');

  // Bank info form
  const [bankForm, setBankForm] = useState({ bankName: '', bankAccountNumber: '', bankAccountHolder: '' });
  const [bankEditing, setBankEditing] = useState(false);

  // Withdraw dialog
  const [withdrawDialog, setWithdrawDialog] = useState(false);
  const [withdrawAmount, setWithdrawAmount] = useState('');

  const fetchData = async () => {
    try {
      const [walletRes, txRes] = await Promise.all([
        walletApi.getMyWallet(),
        walletApi.getMyTransactions(filterType ? { type: filterType } : {}),
      ]);
      const w = walletRes.data;
      setWallet(w);
      setBankForm({
        bankName: w?.bankName || '',
        bankAccountNumber: w?.bankAccountNumber || '',
        bankAccountHolder: w?.bankAccountHolder || '',
      });
      setTransactions(txRes.data || []);
    } catch (err) {
      console.error(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchData(); }, [filterType]);

  const handleUpdateBank = async () => {
    try {
      await walletApi.updateBankInfo(bankForm);
      enqueueSnackbar('Bank info updated', { variant: 'success' });
      setBankEditing(false);
      fetchData();
    } catch (err) {
      enqueueSnackbar(getErrorMessage(err), { variant: 'error' });
    }
  };

  const handleWithdraw = async () => {
    try {
      await walletApi.withdraw({ amount: parseFloat(withdrawAmount) });
      enqueueSnackbar('Withdrawal successful', { variant: 'success' });
      setWithdrawDialog(false);
      setWithdrawAmount('');
      fetchData();
    } catch (err) {
      enqueueSnackbar(getErrorMessage(err), { variant: 'error' });
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
      <Typography variant="h5" fontWeight={700} sx={{ mb: 3 }}>My Wallet</Typography>

      {/* Balance Cards */}
      <Grid container spacing={2} sx={{ mb: 4 }}>
        <Grid size={{ xs: 12, sm: 4 }}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                <AccountBalanceWallet color="primary" />
                <Box>
                  <Typography variant="caption" color="text.secondary">Current Balance</Typography>
                  <Typography variant="h5" fontWeight={700}>{formatCurrency(wallet?.balance || 0)}</Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>
        <Grid size={{ xs: 12, sm: 4 }}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                <TrendingDown color="warning" />
                <Box>
                  <Typography variant="caption" color="text.secondary">Total Withdrawn</Typography>
                  <Typography variant="h5" fontWeight={700}>{formatCurrency(wallet?.totalWithdrawn || 0)}</Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>
        <Grid size={{ xs: 12, sm: 4 }}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                <AccountBalance color="info" />
                <Box>
                  <Typography variant="caption" color="text.secondary">Bank</Typography>
                  <Typography variant="body1" fontWeight={600}>
                    {wallet?.bankName || 'Not set'}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    {wallet?.bankAccountNumber || '—'}
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Actions */}
      <Box sx={{ display: 'flex', gap: 2, mb: 4 }}>
        <Button
          variant="contained"
          onClick={() => setWithdrawDialog(true)}
          disabled={!wallet?.bankName || (wallet?.balance || 0) < 10000}
        >
          Withdraw
        </Button>
        <Button variant="outlined" onClick={() => setBankEditing(true)}>
          {wallet?.bankName ? 'Update Bank Info' : 'Set Up Bank Info'}
        </Button>
      </Box>

      {!wallet?.bankName && (
        <Alert severity="info" sx={{ mb: 3 }}>
          Please set up your bank information before making withdrawals.
        </Alert>
      )}

      {/* Bank Info Edit Dialog */}
      <Dialog open={bankEditing} onClose={() => setBankEditing(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Bank Information</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
            <TextField
              label="Bank Name"
              value={bankForm.bankName}
              onChange={(e) => setBankForm({ ...bankForm, bankName: e.target.value })}
              fullWidth
              placeholder="e.g. Vietcombank"
            />
            <TextField
              label="Account Number"
              value={bankForm.bankAccountNumber}
              onChange={(e) => setBankForm({ ...bankForm, bankAccountNumber: e.target.value })}
              fullWidth
            />
            <TextField
              label="Account Holder"
              value={bankForm.bankAccountHolder}
              onChange={(e) => setBankForm({ ...bankForm, bankAccountHolder: e.target.value.toUpperCase() })}
              fullWidth
              placeholder="e.g. NGUYEN VAN A"
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setBankEditing(false)}>Cancel</Button>
          <Button
            variant="contained"
            onClick={handleUpdateBank}
            disabled={!bankForm.bankName || !bankForm.bankAccountNumber || !bankForm.bankAccountHolder}
          >
            Save
          </Button>
        </DialogActions>
      </Dialog>

      {/* Withdraw Dialog */}
      <Dialog open={withdrawDialog} onClose={() => setWithdrawDialog(false)} maxWidth="xs" fullWidth>
        <DialogTitle>Withdraw Funds</DialogTitle>
        <DialogContent>
          <Box sx={{ mt: 1 }}>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
              Available: {formatCurrency(wallet?.balance || 0)} | Min: {formatCurrency(10000)}
            </Typography>
            <TextField
              label="Amount (VND)"
              type="number"
              value={withdrawAmount}
              onChange={(e) => setWithdrawAmount(e.target.value)}
              fullWidth
            />
            {wallet?.bankName && (
              <Box sx={{ mt: 2, p: 1.5, bgcolor: 'grey.50', borderRadius: 1 }}>
                <Typography variant="caption" color="text.secondary">Transfer to:</Typography>
                <Typography variant="body2" fontWeight={600}>{wallet.bankName}</Typography>
                <Typography variant="body2">{wallet.bankAccountNumber}</Typography>
                <Typography variant="body2">{wallet.bankAccountHolder}</Typography>
              </Box>
            )}
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setWithdrawDialog(false)}>Cancel</Button>
          <Button
            variant="contained"
            onClick={handleWithdraw}
            disabled={!withdrawAmount || parseFloat(withdrawAmount) < 10000 || parseFloat(withdrawAmount) > (wallet?.balance || 0)}
          >
            Confirm Withdrawal
          </Button>
        </DialogActions>
      </Dialog>

      {/* Transaction History */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h6" fontWeight={600}>Transaction History</Typography>
        <FormControl size="small" sx={{ minWidth: 160 }}>
          <InputLabel>Filter</InputLabel>
          <Select value={filterType} label="Filter" onChange={(e) => setFilterType(e.target.value)}>
            <MenuItem value="">All</MenuItem>
            <MenuItem value="REVENUE">Revenue</MenuItem>
            <MenuItem value="WITHDRAWAL">Withdrawal</MenuItem>
            <MenuItem value="REFUND_DEDUCTION">Refund Deduction</MenuItem>
          </Select>
        </FormControl>
      </Box>

      <TableContainer component={Paper} variant="outlined">
        <Table>
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
              <TableRow key={tx.id} hover>
                <TableCell>
                  <Chip label={tx.transactionType} size="small" color={getTxTypeColor(tx.transactionType)} />
                </TableCell>
                <TableCell>
                  <Typography
                    variant="body2"
                    fontWeight={600}
                    color={tx.transactionType === 'REVENUE' ? 'success.main' : 'error.main'}
                  >
                    {tx.transactionType === 'REVENUE' ? '+' : '-'}{formatCurrency(tx.amount)}
                  </Typography>
                </TableCell>
                <TableCell>{formatCurrency(tx.balanceAfter)}</TableCell>
                <TableCell>
                  <Typography variant="body2" sx={{ fontFamily: 'monospace', fontSize: '0.8rem' }}>
                    {tx.referenceCode || '-'}
                  </Typography>
                </TableCell>
                <TableCell>{formatDateTime(tx.createdAt)}</TableCell>
              </TableRow>
            ))}
            {transactions.length === 0 && (
              <TableRow>
                <TableCell colSpan={5} align="center" sx={{ py: 4, color: 'text.secondary' }}>
                  No transactions yet
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
};

export default OrganizerWalletPage;
