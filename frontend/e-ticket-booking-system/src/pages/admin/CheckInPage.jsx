import { useState } from 'react';
import {
  Container,
  Paper,
  Typography,
  Box,
  TextField,
  Button,
  Alert,
  Card,
  CardContent,
  Grid,
  Divider,
  Chip,
} from '@mui/material';
import {
  QrCodeScanner,
  CheckCircle,
  Error as ErrorIcon,
  ConfirmationNumber,
  Person,
  Event,
} from '@mui/icons-material';
import { ticketApi } from '../../api';
import { PageHeader } from '../../components/common';
import { formatDateTime, getErrorMessage } from '../../utils/helpers';

const CheckInPage = () => {
  const [ticketCode, setTicketCode] = useState('');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');

  const handleCheckIn = async (e) => {
    e.preventDefault();
    if (!ticketCode.trim()) return;
    setLoading(true);
    setResult(null);
    setError('');
    try {
      const res = await ticketApi.checkIn(ticketCode.trim());
      setResult(res.data);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const handleReset = () => {
    setTicketCode('');
    setResult(null);
    setError('');
  };

  return (
    <>
      <PageHeader
        title="Ticket Check-In"
        subtitle="Scan or enter ticket code to check in"
        chip="Staff"
      />
      <Container maxWidth="sm" sx={{ py: 4 }}>
        <Paper sx={{ p: 4, textAlign: 'center' }}>
          <QrCodeScanner sx={{ fontSize: 64, color: 'text.secondary', mb: 2 }} />
          <Typography variant="h6" fontWeight={700} gutterBottom>
            Enter Ticket Code
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
            Type or scan the ticket code to verify and check in the attendee.
          </Typography>

          <Box component="form" onSubmit={handleCheckIn}>
            <TextField
              fullWidth
              label="Ticket Code"
              placeholder="e.g., TKT-XXXXXXXX"
              value={ticketCode}
              onChange={(e) => setTicketCode(e.target.value)}
              autoFocus
              sx={{ mb: 2 }}
              inputProps={{ style: { fontSize: 18, fontFamily: 'monospace', textAlign: 'center', letterSpacing: 2 } }}
            />
            <Box sx={{ display: 'flex', gap: 2 }}>
              <Button
                type="submit"
                variant="contained"
                size="large"
                fullWidth
                disabled={loading || !ticketCode.trim()}
              >
                {loading ? 'Checking...' : 'Check In'}
              </Button>
              <Button variant="outlined" size="large" onClick={handleReset}>
                Reset
              </Button>
            </Box>
          </Box>
        </Paper>

        {error && (
          <Alert severity="error" icon={<ErrorIcon />} sx={{ mt: 3 }}>
            {error}
          </Alert>
        )}

        {result && (
          <Card sx={{ mt: 3, border: '2px solid', borderColor: 'success.main' }}>
            <CardContent>
              <Box sx={{ textAlign: 'center', mb: 2 }}>
                <CheckCircle sx={{ fontSize: 48, color: 'success.main' }} />
                <Typography variant="h6" fontWeight={700} color="success.main">
                  Check-In Successful
                </Typography>
              </Box>
              <Divider sx={{ mb: 2 }} />
              <Grid container spacing={2}>
                <Grid item xs={12}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                    <ConfirmationNumber fontSize="small" color="action" />
                    <Typography variant="body2" color="text.secondary">Ticket</Typography>
                  </Box>
                  <Typography variant="body1" fontWeight={600} fontFamily="monospace">
                    {result.ticketCode || result.code || ticketCode}
                  </Typography>
                </Grid>
                {result.eventName && (
                  <Grid item xs={12}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                      <Event fontSize="small" color="action" />
                      <Typography variant="body2" color="text.secondary">Event</Typography>
                    </Box>
                    <Typography variant="body1" fontWeight={600}>{result.eventName}</Typography>
                  </Grid>
                )}
                {result.attendeeName && (
                  <Grid item xs={12}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                      <Person fontSize="small" color="action" />
                      <Typography variant="body2" color="text.secondary">Attendee</Typography>
                    </Box>
                    <Typography variant="body1" fontWeight={600}>{result.attendeeName}</Typography>
                  </Grid>
                )}
                {result.ticketType && (
                  <Grid item xs={12}>
                    <Chip label={result.ticketType} size="small" variant="outlined" />
                    {result.seatInfo && <Chip label={result.seatInfo} size="small" sx={{ ml: 1 }} />}
                  </Grid>
                )}
                {result.checkedInAt && (
                  <Grid item xs={12}>
                    <Typography variant="caption" color="text.secondary">
                      Checked in at {formatDateTime(result.checkedInAt)}
                    </Typography>
                  </Grid>
                )}
              </Grid>
            </CardContent>
          </Card>
        )}
      </Container>
    </>
  );
};

export default CheckInPage;
