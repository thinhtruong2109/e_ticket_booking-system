import { useState } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import {
  AppBar,
  Toolbar,
  Typography,
  Button,
  IconButton,
  Menu,
  MenuItem,
  Box,
  Container,
  Divider,
  Avatar,
  Chip,
  useMediaQuery,
  useTheme,
  Drawer,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  ListItemButton,
} from '@mui/material';
import {
  Menu as MenuIcon,
  Person,
  ConfirmationNumber,
  BookOnline,
  Dashboard,
  Logout,
  Event,
  Store,
  Home,
  Login as LoginIcon,
  PersonAdd,
  ExpandMore,
} from '@mui/icons-material';
import { useAuth } from '../../contexts/AuthContext';
import { getRoleLabel } from '../../utils/helpers';

const Header = () => {
  const { user, isAuthenticated, isAdmin, isOrganizer, isStaff, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));

  const [anchorEl, setAnchorEl] = useState(null);
  const [mobileOpen, setMobileOpen] = useState(false);

  const handleMenuOpen = (event) => setAnchorEl(event.currentTarget);
  const handleMenuClose = () => setAnchorEl(null);

  const handleLogout = () => {
    handleMenuClose();
    logout();
    navigate('/');
  };

  const navItems = [
    { label: 'Home', path: '/', icon: <Home /> },
    { label: 'Events', path: '/events', icon: <Event /> },
    { label: 'Marketplace', path: '/marketplace', icon: <Store /> },
  ];

  const userMenuItems = [
    { label: 'Profile', path: '/profile', icon: <Person /> },
    { label: 'My Tickets', path: '/my-tickets', icon: <ConfirmationNumber /> },
    { label: 'My Bookings', path: '/my-bookings', icon: <BookOnline /> },
  ];

  if (isAdmin) {
    userMenuItems.unshift({ label: 'Admin Dashboard', path: '/admin', icon: <Dashboard /> });
  }
  if (isOrganizer) {
    userMenuItems.unshift({ label: 'Organizer Dashboard', path: '/organizer', icon: <Dashboard /> });
  }
  if (isStaff) {
    userMenuItems.unshift({ label: 'Check-in Scanner', path: '/staff/check-in', icon: <ConfirmationNumber /> });
  }

  const mobileDrawer = (
    <Box sx={{ width: 280, pt: 2 }}>
      {isAuthenticated && (
        <Box sx={{ px: 2, pb: 2 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 1 }}>
            <Avatar sx={{ bgcolor: 'primary.main', width: 40, height: 40 }}>
              {user?.fullName?.[0]?.toUpperCase() || 'U'}
            </Avatar>
            <Box>
              <Typography variant="subtitle2" fontWeight={600} color="text.primary">
                {user?.fullName}
              </Typography>
              <Chip label={getRoleLabel(user?.role)} size="small" sx={{ height: 20, fontSize: '0.7rem' }} />
            </Box>
          </Box>
          <Divider />
        </Box>
      )}
      <List>
        {navItems.map((item) => (
          <ListItem key={item.path} disablePadding>
            <ListItemButton
              selected={location.pathname === item.path}
              onClick={() => {
                navigate(item.path);
                setMobileOpen(false);
              }}
            >
              <ListItemIcon sx={{ minWidth: 40 }}>{item.icon}</ListItemIcon>
              <ListItemText primary={item.label} />
            </ListItemButton>
          </ListItem>
        ))}
      </List>
      {isAuthenticated && (
        <>
          <Divider sx={{ my: 1 }} />
          <List>
            {userMenuItems.map((item) => (
              <ListItem key={item.path} disablePadding>
                <ListItemButton
                  onClick={() => {
                    navigate(item.path);
                    setMobileOpen(false);
                  }}
                >
                  <ListItemIcon sx={{ minWidth: 40 }}>{item.icon}</ListItemIcon>
                  <ListItemText primary={item.label} />
                </ListItemButton>
              </ListItem>
            ))}
            <ListItem disablePadding>
              <ListItemButton onClick={handleLogout}>
                <ListItemIcon sx={{ minWidth: 40 }}>
                  <Logout />
                </ListItemIcon>
                <ListItemText primary="Logout" />
              </ListItemButton>
            </ListItem>
          </List>
        </>
      )}
      {!isAuthenticated && (
        <>
          <Divider sx={{ my: 1 }} />
          <List>
            <ListItem disablePadding>
              <ListItemButton onClick={() => { navigate('/login'); setMobileOpen(false); }}>
                <ListItemIcon sx={{ minWidth: 40 }}><LoginIcon /></ListItemIcon>
                <ListItemText primary="Login" />
              </ListItemButton>
            </ListItem>
            <ListItem disablePadding>
              <ListItemButton onClick={() => { navigate('/register'); setMobileOpen(false); }}>
                <ListItemIcon sx={{ minWidth: 40 }}><PersonAdd /></ListItemIcon>
                <ListItemText primary="Register" />
              </ListItemButton>
            </ListItem>
          </List>
        </>
      )}
    </Box>
  );

  return (
    <AppBar position="sticky">
      <Container maxWidth="lg">
        <Toolbar disableGutters sx={{ minHeight: { xs: 56, md: 64 } }}>
          {isMobile && (
            <IconButton color="inherit" edge="start" onClick={() => setMobileOpen(true)} sx={{ mr: 1 }}>
              <MenuIcon />
            </IconButton>
          )}

          <Typography
            variant="h6"
            sx={{
              fontWeight: 700,
              letterSpacing: '-0.3px',
              cursor: 'pointer',
              fontSize: { xs: '1rem', md: '1.125rem' },
            }}
            onClick={() => navigate('/')}
          >
            E-Ticket
          </Typography>

          {!isMobile && (
            <Box sx={{ display: 'flex', gap: 0.5, ml: 4 }}>
              {navItems.map((item) => (
                <Button
                  key={item.path}
                  color="inherit"
                  onClick={() => navigate(item.path)}
                  sx={{
                    opacity: location.pathname === item.path ? 1 : 0.7,
                    fontWeight: location.pathname === item.path ? 600 : 400,
                    '&:hover': { opacity: 1 },
                  }}
                >
                  {item.label}
                </Button>
              ))}
            </Box>
          )}

          <Box sx={{ flexGrow: 1 }} />

          {!isMobile && (
            <>
              {isAuthenticated ? (
                <>
                  <Box
                    onClick={handleMenuOpen}
                    sx={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: 1,
                      cursor: 'pointer',
                      py: 0.5,
                      px: 1,
                      borderRadius: 1,
                      '&:hover': { bgcolor: 'rgba(255,255,255,0.1)' },
                    }}
                  >
                    <Avatar sx={{ width: 32, height: 32, bgcolor: 'rgba(255,255,255,0.2)', fontSize: '0.875rem' }}>
                      {user?.fullName?.[0]?.toUpperCase() || 'U'}
                    </Avatar>
                    <Typography variant="body2" color="inherit" sx={{ fontWeight: 500 }}>
                      {user?.fullName}
                    </Typography>
                    <ExpandMore sx={{ fontSize: 18, opacity: 0.7 }} />
                  </Box>
                  <Menu
                    anchorEl={anchorEl}
                    open={Boolean(anchorEl)}
                    onClose={handleMenuClose}
                    PaperProps={{
                      sx: { mt: 1, minWidth: 200, border: '1px solid', borderColor: 'divider' },
                    }}
                    transformOrigin={{ horizontal: 'right', vertical: 'top' }}
                    anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}
                  >
                    <Box sx={{ px: 2, py: 1 }}>
                      <Typography variant="subtitle2" fontWeight={600}>{user?.fullName}</Typography>
                      <Typography variant="caption" color="text.secondary">{user?.email}</Typography>
                    </Box>
                    <Divider />
                    {userMenuItems.map((item) => (
                      <MenuItem
                        key={item.path}
                        onClick={() => {
                          handleMenuClose();
                          navigate(item.path);
                        }}
                      >
                        <ListItemIcon>{item.icon}</ListItemIcon>
                        {item.label}
                      </MenuItem>
                    ))}
                    <Divider />
                    <MenuItem onClick={handleLogout}>
                      <ListItemIcon><Logout fontSize="small" /></ListItemIcon>
                      Logout
                    </MenuItem>
                  </Menu>
                </>
              ) : (
                <Box sx={{ display: 'flex', gap: 1 }}>
                  <Button color="inherit" onClick={() => navigate('/login')} sx={{ opacity: 0.9 }}>
                    Login
                  </Button>
                  <Button
                    variant="outlined"
                    onClick={() => navigate('/register')}
                    sx={{
                      borderColor: 'rgba(255,255,255,0.3)',
                      color: 'white',
                      '&:hover': { borderColor: 'rgba(255,255,255,0.5)', bgcolor: 'rgba(255,255,255,0.05)' },
                    }}
                  >
                    Register
                  </Button>
                </Box>
              )}
            </>
          )}
        </Toolbar>
      </Container>

      <Drawer anchor="left" open={mobileOpen} onClose={() => setMobileOpen(false)}>
        {mobileDrawer}
      </Drawer>
    </AppBar>
  );
};

const Footer = () => (
  <Box
    component="footer"
    sx={{
      bgcolor: 'grey.900',
      color: 'grey.400',
      py: 4,
      mt: 'auto',
    }}
  >
    <Container maxWidth="lg">
      <Box sx={{ display: 'flex', flexWrap: 'wrap', justifyContent: 'space-between', gap: 3 }}>
        <Box>
          <Typography variant="h6" color="white" fontWeight={700} gutterBottom>
            E-Ticket
          </Typography>
          <Typography variant="body2" sx={{ maxWidth: 300 }}>
            Your trusted platform for booking event tickets. Discover, book, and enjoy amazing events.
          </Typography>
        </Box>
        <Box>
          <Typography variant="subtitle2" color="grey.300" gutterBottom fontWeight={600}>
            Quick Links
          </Typography>
          <Typography variant="body2" sx={{ mb: 0.5 }}>Events</Typography>
          <Typography variant="body2" sx={{ mb: 0.5 }}>Marketplace</Typography>
          <Typography variant="body2" sx={{ mb: 0.5 }}>Help Center</Typography>
        </Box>
        <Box>
          <Typography variant="subtitle2" color="grey.300" gutterBottom fontWeight={600}>
            Legal
          </Typography>
          <Typography variant="body2" sx={{ mb: 0.5 }}>Terms of Service</Typography>
          <Typography variant="body2" sx={{ mb: 0.5 }}>Privacy Policy</Typography>
          <Typography variant="body2" sx={{ mb: 0.5 }}>Refund Policy</Typography>
        </Box>
      </Box>
      <Divider sx={{ my: 3, borderColor: 'grey.800' }} />
      <Typography variant="caption" sx={{ color: 'grey.500' }}>
        &copy; 2026 E-Ticket. All rights reserved.
      </Typography>
    </Container>
  </Box>
);

const MainLayout = () => {
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <Header />
      <Box component="main" sx={{ flexGrow: 1 }}>
        <Outlet />
      </Box>
      <Footer />
    </Box>
  );
};

export default MainLayout;
