const trimTrailingSlash = (value) => value.replace(/\/+$/, '');

const normalizePathname = (pathname) => {
  const cleaned = trimTrailingSlash(pathname || '');
  if (!cleaned || cleaned === '/') {
    return '/eticket-api';
  }

  if (cleaned.endsWith('/eticket-api')) {
    return cleaned;
  }

  if (cleaned.endsWith('/eticket')) {
    return `${cleaned}-api`;
  }

  return `${cleaned}/eticket-api`;
};

export const getApiBaseUrl = () => {
  const envUrl = import.meta.env.VITE_API_BASE_URL?.trim();

  if (!envUrl) {
    return '/eticket-api';
  }

  if (/^https?:\/\//i.test(envUrl)) {
    const url = new URL(envUrl);
    url.pathname = normalizePathname(url.pathname);
    return `${url.origin}${url.pathname}`;
  }

  return normalizePathname(envUrl);
};

export const withAppBase = (path = '') => {
  const base = import.meta.env.BASE_URL || '/';
  const normalizedBase = base.endsWith('/') ? base : `${base}/`;
  const normalizedPath = path.replace(/^\/+/, '');
  return `${normalizedBase}${normalizedPath}`;
};
