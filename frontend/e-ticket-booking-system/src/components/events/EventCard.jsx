import { useNavigate } from 'react-router-dom';
import {
  Card,
  CardContent,
  CardMedia,
  Typography,
  Box,
  Chip,
  CardActionArea,
} from '@mui/material';
import { CalendarToday, LocationOn, ConfirmationNumber } from '@mui/icons-material';
import { formatDate, formatCurrency, truncateText } from '../../utils/helpers';
import StatusChip from '../common/StatusChip';

const EventCard = ({ event }) => {
  const navigate = useNavigate();

  return (
    <Card
      sx={{
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
        transition: 'all 0.2s ease',
        '&:hover': {
          transform: 'translateY(-2px)',
          boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
        },
      }}
    >
      <CardActionArea onClick={() => navigate(`/events/${event.id}`)}>
        <CardMedia
          component="div"
          sx={{
            height: 180,
            bgcolor: 'grey.200',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            position: 'relative',
            backgroundImage: event.thumbnailImageUrl ? `url(${event.thumbnailImageUrl})` : 'none',
            backgroundSize: 'cover',
            backgroundPosition: 'center',
          }}
        >
          {!event.thumbnailImageUrl && (
            <Typography variant="h4" sx={{ color: 'grey.400', fontWeight: 700 }}>
              E
            </Typography>
          )}
          {event.categoryName && (
            <Chip
              label={event.categoryName}
              size="small"
              sx={{
                position: 'absolute',
                top: 12,
                left: 12,
                bgcolor: 'rgba(17, 24, 39, 0.85)',
                color: 'white',
                fontSize: '0.7rem',
                fontWeight: 500,
              }}
            />
          )}
          {event.status && event.status !== 'PUBLISHED' && (
            <Box sx={{ position: 'absolute', top: 12, right: 12 }}>
              <StatusChip status={event.status} />
            </Box>
          )}
        </CardMedia>
        <CardContent sx={{ flexGrow: 1, p: 2.5 }}>
          <Typography variant="h6" gutterBottom sx={{ fontWeight: 600, fontSize: '1rem', lineHeight: 1.4 }}>
            {truncateText(event.name, 60)}
          </Typography>

          {event.description && (
            <Typography variant="body2" color="text.secondary" sx={{ mb: 2, lineHeight: 1.5 }}>
              {truncateText(event.description, 80)}
            </Typography>
          )}

          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.8 }}>
            {event.venueName && (
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.75 }}>
                <LocationOn sx={{ fontSize: 16, color: 'grey.400' }} />
                <Typography variant="caption" color="text.secondary">
                  {event.venueName}
                </Typography>
              </Box>
            )}
            {event.availableTickets !== undefined && (
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.75 }}>
                <ConfirmationNumber sx={{ fontSize: 16, color: 'grey.400' }} />
                <Typography variant="caption" color="text.secondary">
                  {event.availableTickets} tickets available
                </Typography>
              </Box>
            )}
          </Box>
        </CardContent>
      </CardActionArea>
    </Card>
  );
};

export default EventCard;
