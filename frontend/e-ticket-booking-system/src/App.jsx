import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider, CssBaseline } from '@mui/material';
import { SnackbarProvider } from 'notistack';
import theme from './theme';
import { AuthProvider } from './contexts/AuthContext';
import { MainLayout, ProtectedRoute } from './components/common';

// Pages
import HomePage from './pages/HomePage';
import LoginPage from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';
import VerifyEmailPage from './pages/auth/VerifyEmailPage';
import EventListPage from './pages/events/EventListPage';
import EventDetailPage from './pages/events/EventDetailPage';
import CreateBookingPage from './pages/bookings/CreateBookingPage';
import PaymentPage from './pages/bookings/PaymentPage';
import MyBookingsPage from './pages/bookings/MyBookingsPage';
import BookingDetailPage from './pages/bookings/BookingDetailPage';
import MyTicketsPage from './pages/tickets/MyTicketsPage';
import ProfilePage from './pages/profile/ProfilePage';
import MarketplacePage from './pages/marketplace/MarketplacePage';
import ListingDetailPage from './pages/marketplace/ListingDetailPage';
import AdminDashboard from './pages/admin/AdminDashboard';
import CheckInPage from './pages/admin/CheckInPage';
import OrganizerDashboard from './pages/organizer/OrganizerDashboard';

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <SnackbarProvider maxSnack={3} anchorOrigin={{ vertical: 'top', horizontal: 'right' }}>
        <BrowserRouter>
          <AuthProvider>
            <Routes>
              {/* Public routes */}
              <Route element={<MainLayout />}>
                <Route path="/" element={<HomePage />} />
                <Route path="/events" element={<EventListPage />} />
                <Route path="/events/:id" element={<EventDetailPage />} />
                <Route path="/marketplace" element={<MarketplacePage />} />
                <Route path="/marketplace/:id" element={<ListingDetailPage />} />

                {/* Auth routes */}
                <Route path="/login" element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />
                <Route path="/verify-email" element={<VerifyEmailPage />} />

                {/* Customer routes */}
                <Route element={<ProtectedRoute allowedRoles={['CUSTOMER', 'ORGANIZER', 'ADMIN', 'STAFF']} />}>
                  <Route path="/bookings" element={<MyBookingsPage />} />
                  <Route path="/bookings/:id" element={<BookingDetailPage />} />
                  <Route path="/bookings/create/:eventId" element={<CreateBookingPage />} />
                  <Route path="/bookings/:id/payment" element={<PaymentPage />} />
                  <Route path="/tickets" element={<MyTicketsPage />} />
                  <Route path="/profile" element={<ProfilePage />} />
                </Route>

                {/* Admin routes */}
                <Route element={<ProtectedRoute allowedRoles={['ADMIN']} />}>
                  <Route path="/admin" element={<AdminDashboard />} />
                </Route>

                {/* Staff / Admin routes */}
                <Route element={<ProtectedRoute allowedRoles={['ADMIN', 'STAFF']} />}>
                  <Route path="/check-in" element={<CheckInPage />} />
                </Route>

                {/* Organizer routes */}
                <Route element={<ProtectedRoute allowedRoles={['ORGANIZER']} />}>
                  <Route path="/organizer" element={<OrganizerDashboard />} />
                </Route>
              </Route>

              {/* Catch all */}
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </AuthProvider>
        </BrowserRouter>
      </SnackbarProvider>
    </ThemeProvider>
  );
}

export default App;
