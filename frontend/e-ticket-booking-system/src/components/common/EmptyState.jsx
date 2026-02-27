import { Alert, Box, Typography, Button } from '@mui/material';
import { ErrorOutline } from '@mui/icons-material';

const EmptyState = ({ icon, title, description, action, actionLabel, onClick }) => (
  <Box
    sx={{
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      py: 8,
      px: 2,
      textAlign: 'center',
    }}
  >
    <Box sx={{ color: 'grey.300', mb: 2 }}>
      {icon || <ErrorOutline sx={{ fontSize: 64 }} />}
    </Box>
    <Typography variant="h6" gutterBottom>
      {title || 'No data found'}
    </Typography>
    {description && (
      <Typography variant="body2" color="text.secondary" sx={{ maxWidth: 400, mb: 3 }}>
        {description}
      </Typography>
    )}
    {actionLabel && onClick && (
      <Button variant="contained" onClick={onClick}>
        {actionLabel}
      </Button>
    )}
  </Box>
);

const ErrorAlert = ({ message, onRetry }) => (
  <Alert
    severity="error"
    sx={{ my: 2 }}
    action={
      onRetry ? (
        <Button color="inherit" size="small" onClick={onRetry}>
          Retry
        </Button>
      ) : undefined
    }
  >
    {message || 'Something went wrong. Please try again.'}
  </Alert>
);

export { EmptyState, ErrorAlert };
