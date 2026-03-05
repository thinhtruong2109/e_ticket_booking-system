import { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import {
  Container,
  Paper,
  Typography,
  Box,
  Button,
  CircularProgress,
  Alert,
} from '@mui/material';
import { CheckCircle, ConfirmationNumber, ArrowBack, HourglassTop } from '@mui/icons-material';
import { paymentApi } from '../../api';
import { formatCurrency, getErrorMessage } from '../../utils/helpers';

/**
 * Trang hiển thị khi thanh toán PayOS thành công.
 * 
 * Flow: PayOS → Backend (đồng bộ trạng thái + confirm booking) → Redirect browser đến đây.
 * Backend đã xử lý trước khi redirect. Frontend lấy thêm thông tin payment để hiển thị chi tiết.
 */
const PaymentSuccessPage = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  const orderCode = searchParams.get('orderCode');
  const statusFromUrl = searchParams.get('status');

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [paymentInfo, setPaymentInfo] = useState(null);

  useEffect(() => {
    if (orderCode) {
      fetchPaymentInfo();
    } else {
      setLoading(false);
      setError('Không tìm thấy mã đơn hàng (orderCode).');
    }
  }, [orderCode]);

  const fetchPaymentInfo = async () => {
    setLoading(true);
    try {
      const res = await paymentApi.getByOrderCode(orderCode);
      setPaymentInfo(res.data);
    } catch (err) {
      // Nếu không lấy được thông tin, vẫn hiển thị status từ URL
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <Container maxWidth="sm" sx={{ py: 8, textAlign: 'center' }}>
        <CircularProgress sx={{ mb: 2 }} />
        <Typography>Đang xác nhận thanh toán...</Typography>
      </Container>
    );
  }

  const status = paymentInfo?.status || statusFromUrl;
  const isSuccess = status === 'SUCCESS';

  return (
    <Container maxWidth="sm" sx={{ py: 6 }}>
      <Paper sx={{ p: 4, textAlign: 'center' }}>
        {isSuccess ? (
          <CheckCircle sx={{ fontSize: 64, color: 'success.main', mb: 2 }} />
        ) : (
          <HourglassTop sx={{ fontSize: 64, color: 'warning.main', mb: 2 }} />
        )}

        <Typography variant="h5" fontWeight={700} gutterBottom>
          {isSuccess ? 'Thanh toán thành công!' : 'Đang xử lý thanh toán'}
        </Typography>

        <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
          {isSuccess
            ? 'Vé của bạn đã được xác nhận. Kiểm tra email để nhận vé điện tử.'
            : 'Thanh toán đang được xử lý. Vui lòng chờ trong giây lát.'}
        </Typography>

        {error && (
          <Alert severity="warning" sx={{ mb: 3, textAlign: 'left' }}>
            {error}
          </Alert>
        )}

        <Box sx={{ mb: 3, p: 2, bgcolor: 'grey.50', borderRadius: 1, textAlign: 'left' }}>
          <Typography variant="body2" color="text.secondary">
            Mã đơn hàng: <strong>{orderCode}</strong>
          </Typography>
          {paymentInfo?.bookingCode && (
            <Typography variant="body2" color="text.secondary">
              Mã booking: <strong>{paymentInfo.bookingCode}</strong>
            </Typography>
          )}
          {paymentInfo?.amount && (
            <Typography variant="body2" color="text.secondary">
              Số tiền: <strong>{formatCurrency(paymentInfo.amount)}</strong>
            </Typography>
          )}
          <Typography variant="body2" color="text.secondary">
            Trạng thái: <strong>{status}</strong>
          </Typography>
        </Box>

        <Box sx={{ display: 'flex', gap: 2, justifyContent: 'center', flexWrap: 'wrap' }}>
          {paymentInfo?.bookingId && (
            <Button
              variant="contained"
              startIcon={<ConfirmationNumber />}
              onClick={() => navigate(`/my-bookings/${paymentInfo.bookingId}`)}
            >
              Xem chi tiết đơn
            </Button>
          )}
          <Button
            variant="outlined"
            startIcon={<ArrowBack />}
            onClick={() => navigate('/my-bookings')}
          >
            Đơn đặt vé của tôi
          </Button>
        </Box>
      </Paper>
    </Container>
  );
};

export default PaymentSuccessPage;
