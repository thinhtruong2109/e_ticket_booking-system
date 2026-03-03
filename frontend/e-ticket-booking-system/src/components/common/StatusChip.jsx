import { Chip } from '@mui/material';
import { getStatusColor, getStatusLabel } from '../../utils/helpers';

const StatusChip = ({ status, size = 'small', ...props }) => (
  <Chip
    label={getStatusLabel(status)}
    color={getStatusColor(status)}
    size={size}
    variant="outlined"
    {...props}
  />
);

export default StatusChip;
