import { useSearchParams, useNavigate } from 'react-router-dom';
import {
  Container,
  Paper,
  Typography,
  Box,
  Button,
} from '@mui/material';
import { Cancel, ArrowBack, Replay } from '@mui/icons-material';

/**
 * Trang hiển thị khi user huỷ thanh toán PayOS.
 * 
 * Flow: PayOS → Backend (xử lý huỷ payment + booking + trả ghế) → Redirect browser đến đây.
 * Backend đã xử lý mọi thứ trước khi redirect, frontend chỉ cần hiển thị kết quả.
 */
const PaymentCancelPage = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  const orderCode = searchParams.get('orderCode');
  const status = searchParams.get('status');

  return (
    <Container maxWidth="sm" sx={{ py: 6 }}>
      <Paper sx={{ p: 4, textAlign: 'center' }}>
        <Cancel sx={{ fontSize: 64, color: 'warning.main', mb: 2 }} />

        <Typography variant="h5" fontWeight={700} gutterBottom>
          Thanh toán đã bị huỷ
        </Typography>

        <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
          Bạn đã huỷ thanh toán. Đơn đặt vé và ghế đã được giải phóng.
          Bạn có thể đặt lại bất cứ lúc nào.
        </Typography>

        {orderCode && (
          <Box sx={{ mb: 3, p: 2, bgcolor: 'grey.50', borderRadius: 1, textAlign: 'left' }}>
            <Typography variant="body2" color="text.secondary">
              Mã đơn hàng: <strong>{orderCode}</strong>
            </Typography>
            {status && (
              <Typography variant="body2" color="text.secondary">
                Trạng thái: <strong>{status}</strong>
              </Typography>
            )}
          </Box>
        )}

        <Box sx={{ display: 'flex', gap: 2, justifyContent: 'center', flexWrap: 'wrap' }}>
          <Button
            variant="outlined"
            startIcon={<ArrowBack />}
            onClick={() => navigate('/bookings')}
          >
            Đơn đặt vé của tôi
          </Button>
          <Button
            variant="contained"
            startIcon={<Replay />}
            onClick={() => navigate('/events')}
          >
            Đặt vé mới
          </Button>
        </Box>
      </Paper>
    </Container>
  );
};

export default PaymentCancelPage;
