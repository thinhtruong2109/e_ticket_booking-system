import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import utc from 'dayjs/plugin/utc';
import timezone from 'dayjs/plugin/timezone';

dayjs.extend(utc);
dayjs.extend(timezone);
dayjs.extend(relativeTime);

const SERVER_TIMEZONE = 'Asia/Ho_Chi_Minh';
const HAS_TIMEZONE_SUFFIX_REGEX = /(Z|[+-]\d{2}:?\d{2})$/i;

export const parseApiDateTime = (value) => {
  if (value === null || value === undefined) {
    return dayjs(value);
  }

  if (dayjs.isDayjs(value) || value instanceof Date || typeof value === 'number') {
    return dayjs(value);
  }

  if (typeof value === 'string') {
    const input = value.trim();
    if (!input) {
      return dayjs(input);
    }

    if (HAS_TIMEZONE_SUFFIX_REGEX.test(input)) {
      return dayjs(input);
    }

    return dayjs.tz(input, SERVER_TIMEZONE);
  }

  return dayjs(value);
};

export const toDateTimeMillis = (value) => parseApiDateTime(value).valueOf();

export const formatDate = (date) => {
  return parseApiDateTime(date).format('DD/MM/YYYY');
};

export const formatDateTime = (date) => {
  return parseApiDateTime(date).format('DD/MM/YYYY HH:mm');
};

export const formatTime = (date) => {
  return parseApiDateTime(date).format('HH:mm');
};

export const formatRelative = (date) => {
  return parseApiDateTime(date).fromNow();
};

export const formatCurrency = (amount) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount);
};

export const formatNumber = (num) => {
  return new Intl.NumberFormat('vi-VN').format(num);
};

export const getStatusColor = (status) => {
  const colors = {
    ACTIVE: 'success',
    PUBLISHED: 'success',
    CONFIRMED: 'success',
    SUCCESS: 'success',
    COMPLETED: 'success',
    SCHEDULED: 'info',
    PENDING: 'warning',
    ONGOING: 'info',
    DRAFT: 'default',
    INACTIVE: 'default',
    HOLDING: 'warning',
    FOR_SALE: 'info',
    CANCELLED: 'error',
    EXPIRED: 'error',
    FAILED: 'error',
    BANNED: 'error',
    DISABLED: 'error',
    RELEASED: 'default',
    REFUNDED: 'warning',
    SOLD: 'success',
  };
  return colors[status] || 'default';
};

export const getStatusLabel = (status) => {
  const labels = {
    ACTIVE: 'Active',
    PUBLISHED: 'Published',
    CONFIRMED: 'Confirmed',
    SUCCESS: 'Success',
    COMPLETED: 'Completed',
    SCHEDULED: 'Scheduled',
    PENDING: 'Pending',
    ONGOING: 'Ongoing',
    DRAFT: 'Draft',
    INACTIVE: 'Inactive',
    HOLDING: 'Holding',
    FOR_SALE: 'For Sale',
    CANCELLED: 'Cancelled',
    EXPIRED: 'Expired',
    FAILED: 'Failed',
    BANNED: 'Banned',
    DISABLED: 'Disabled',
    RELEASED: 'Released',
    REFUNDED: 'Refunded',
    SOLD: 'Sold',
  };
  return labels[status] || status;
};

export const getRoleLabel = (role) => {
  const labels = {
    CUSTOMER: 'Customer',
    ORGANIZER: 'Organizer',
    ADMIN: 'Admin',
    STAFF: 'Staff',
  };
  return labels[role] || role;
};

export const truncateText = (text, maxLength = 100) => {
  if (!text) return '';
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
};

export const getErrorMessage = (error) => {
  if (error.response?.data?.message) return error.response.data.message;
  if (error.response?.data?.error) return error.response.data.error;
  if (error.message) return error.message;
  return 'An unexpected error occurred';
};
