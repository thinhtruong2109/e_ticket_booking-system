import { Box, Typography, Chip } from '@mui/material';

const PageHeader = ({ title, subtitle, action, chip }) => (
  <Box
    sx={{
      bgcolor: 'grey.900',
      color: 'white',
      py: { xs: 4, md: 5 },
      px: 2,
    }}
  >
    <Box sx={{ maxWidth: 'lg', mx: 'auto' }}>
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: 2 }}>
        <Box>
          {chip && (
            <Chip
              label={chip}
              size="small"
              sx={{
                mb: 1.5,
                bgcolor: 'rgba(255,255,255,0.1)',
                color: 'grey.300',
                fontSize: '0.7rem',
                letterSpacing: '0.1em',
                textTransform: 'uppercase',
              }}
            />
          )}
          <Typography variant="h3" color="white" fontWeight={700}>
            {title}
          </Typography>
          {subtitle && (
            <Typography variant="body1" sx={{ color: 'grey.400', mt: 1 }}>
              {subtitle}
            </Typography>
          )}
        </Box>
        {action && <Box>{action}</Box>}
      </Box>
    </Box>
  </Box>
);

export default PageHeader;
