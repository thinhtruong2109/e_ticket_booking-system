import { useState, useRef, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Box, TextField, Button, Typography, Alert, Paper, Link } from '@mui/material';
import { authApi } from '../../api';
import { getErrorMessage } from '../../utils/helpers';

const VerifyEmailPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const email = location.state?.email || '';

  const [otp, setOtp] = useState(['', '', '', '', '', '']);
  const [loading, setLoading] = useState(false);
  const [resending, setResending] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const inputRefs = useRef([]);

  useEffect(() => {
    if (!email) {
      navigate('/register');
    }
  }, [email, navigate]);

  const handleChange = (index, value) => {
    if (value.length > 1) return;
    const newOtp = [...otp];
    newOtp[index] = value;
    setOtp(newOtp);

    if (value && index < 5) {
      inputRefs.current[index + 1]?.focus();
    }
  };

  const handleKeyDown = (index, e) => {
    if (e.key === 'Backspace' && !otp[index] && index > 0) {
      inputRefs.current[index - 1]?.focus();
    }
  };

  const handlePaste = (e) => {
    e.preventDefault();
    const pasted = e.clipboardData.getData('text').trim().slice(0, 6);
    if (/^\d+$/.test(pasted)) {
      const newOtp = [...otp];
      pasted.split('').forEach((char, i) => {
        if (i < 6) newOtp[i] = char;
      });
      setOtp(newOtp);
      inputRefs.current[Math.min(pasted.length, 5)]?.focus();
    }
  };

  const handleVerify = async () => {
    const otpCode = otp.join('');
    if (otpCode.length !== 6) {
      setError('Please enter all 6 digits');
      return;
    }

    setError('');
    setLoading(true);
    try {
      await authApi.verifyEmail({ email, otp: otpCode });
      setSuccess('Email verified successfully! Redirecting...');
      setTimeout(() => navigate('/login'), 2000);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const handleResend = async () => {
    setResending(true);
    setError('');
    try {
      await authApi.resendOtp({ email });
      setSuccess('New OTP sent to your email');
      setOtp(['', '', '', '', '', '']);
      inputRefs.current[0]?.focus();
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setResending(false);
    }
  };

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        bgcolor: 'background.default',
        p: 2,
      }}
    >
      <Paper sx={{ maxWidth: 440, width: '100%', p: { xs: 3, sm: 5 }, textAlign: 'center' }}>
        <Typography variant="h5" fontWeight={700} sx={{ letterSpacing: '-0.3px', mb: 1 }}>
          E-Ticket
        </Typography>
        <Typography variant="overline" color="text.secondary" display="block" sx={{ mb: 3 }}>
          Email Verification
        </Typography>

        <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
          We sent a 6-digit verification code to
        </Typography>
        <Typography variant="body1" fontWeight={600} sx={{ mb: 3 }}>
          {email}
        </Typography>

        {error && <Alert severity="error" sx={{ mb: 2, textAlign: 'left' }}>{error}</Alert>}
        {success && <Alert severity="success" sx={{ mb: 2, textAlign: 'left' }}>{success}</Alert>}

        <Box sx={{ display: 'flex', gap: 1, justifyContent: 'center', mb: 3 }} onPaste={handlePaste}>
          {otp.map((digit, idx) => (
            <TextField
              key={idx}
              inputRef={(el) => (inputRefs.current[idx] = el)}
              value={digit}
              onChange={(e) => handleChange(idx, e.target.value.replace(/\D/g, ''))}
              onKeyDown={(e) => handleKeyDown(idx, e)}
              inputProps={{
                maxLength: 1,
                style: {
                  textAlign: 'center',
                  fontSize: '1.5rem',
                  fontWeight: 700,
                  fontFamily: 'monospace',
                  padding: '12px 0',
                  width: 40,
                },
              }}
              variant="outlined"
            />
          ))}
        </Box>

        <Typography variant="caption" color="text.secondary" display="block" sx={{ mb: 3 }}>
          Code expires in <strong>5 minutes</strong>
        </Typography>

        <Button
          variant="contained"
          fullWidth
          size="large"
          onClick={handleVerify}
          disabled={loading || otp.some((d) => !d)}
          sx={{ py: 1.5, mb: 2 }}
        >
          {loading ? 'Verifying...' : 'Verify Email'}
        </Button>

        <Typography variant="body2" color="text.secondary">
          Didn&apos;t receive the code?{' '}
          <Link
            component="button"
            variant="body2"
            fontWeight={600}
            onClick={handleResend}
            disabled={resending}
            sx={{ color: 'text.primary' }}
          >
            {resending ? 'Sending...' : 'Resend'}
          </Link>
        </Typography>

        <Box sx={{ mt: 3, p: 2, bgcolor: 'grey.50', borderRadius: 1, border: '1px solid', borderColor: 'divider' }}>
          <Typography variant="caption" color="text.secondary">
            <strong style={{ color: '#374151' }}>Security notice:</strong> Never share this code. E-Ticket staff will
            never ask for your verification code.
          </Typography>
        </Box>
      </Paper>
    </Box>
  );
};

export default VerifyEmailPage;
