import { Box, Typography, Button, Container } from '@mui/material';
import { Block } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';

const ForbiddenPage = () => {
  const navigate = useNavigate();

  return (
    <Container maxWidth="sm">
      <Box
        sx={{
          minHeight: '60vh',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          textAlign: 'center',
          py: 8,
        }}
      >
        <Block sx={{ fontSize: 80, color: 'error.main', mb: 2 }} />
        <Typography variant="h3" fontWeight={700} gutterBottom>
          403
        </Typography>
        <Typography variant="h5" color="text.secondary" gutterBottom>
          Access Denied
        </Typography>
        <Typography variant="body1" color="text.secondary" sx={{ mb: 4, maxWidth: 400 }}>
          You don't have permission to access this page. Please contact an administrator if you believe this is an error.
        </Typography>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Button variant="contained" onClick={() => navigate('/')}>
            Go to Home
          </Button>
          <Button variant="outlined" onClick={() => navigate(-1)}>
            Go Back
          </Button>
        </Box>
      </Box>
    </Container>
  );
};

export default ForbiddenPage;
