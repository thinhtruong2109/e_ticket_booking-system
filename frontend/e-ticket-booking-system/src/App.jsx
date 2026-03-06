import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { ThemeProvider, CssBaseline } from '@mui/material';
import { SnackbarProvider } from 'notistack';
import theme from './theme';
import { AuthProvider } from './contexts/AuthContext';
import { MainLayout, ProtectedRoute } from './components/common';
import { OrganizerLayout, AdminLayout, StaffLayout } from './components/layouts';

// Public / Auth pages
import HomePage from './pages/HomePage';
import LoginPage from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';
import VerifyEmailPage from './pages/auth/VerifyEmailPage';
import EventListPage from './pages/events/EventListPage';
import EventDetailPage from './pages/events/EventDetailPage';
import MarketplacePage from './pages/marketplace/MarketplacePage';
import ListingDetailPage from './pages/marketplace/ListingDetailPage';

// Customer pages
import CreateBookingPage from './pages/bookings/CreateBookingPage';
import PaymentPage from './pages/bookings/PaymentPage';
import PaymentCancelPage from './pages/bookings/PaymentCancelPage';
import PaymentSuccessPage from './pages/bookings/PaymentSuccessPage';
import MyBookingsPage from './pages/bookings/MyBookingsPage';
import BookingDetailPage from './pages/bookings/BookingDetailPage';
import MyTicketsPage from './pages/tickets/MyTicketsPage';
import ProfilePage from './pages/profile/ProfilePage';
import MyListingsPage from './pages/marketplace/MyListingsPage';

// Organizer pages
import OrganizerDashboardPage from './pages/organizer/OrganizerDashboardPage';
import OrganizerEventsPage from './pages/organizer/OrganizerEventsPage';
import EventFormPage from './pages/organizer/EventFormPage';
import OrganizerPromoCodesPage from './pages/organizer/OrganizerPromoCodesPage';
import OrganizerWalletPage from './pages/organizer/OrganizerWalletPage';
import OrganizerCheckinPage from './pages/organizer/OrganizerCheckinPage';
import OrganizerVenuesPage from './pages/organizer/OrganizerVenuesPage';

// Admin pages
import AdminDashboardPage from './pages/admin/AdminDashboardPage';
import AdminUsersPage from './pages/admin/AdminUsersPage';
import AdminEventsPage from './pages/admin/AdminEventsPage';
import AdminPromoCodesPage from './pages/admin/AdminPromoCodesPage';
import AdminWalletsPage from './pages/admin/AdminWalletsPage';
import AdminCategoriesPage from './pages/admin/AdminCategoriesPage';
import AdminVenuesPage from './pages/admin/AdminVenuesPage';
import AdminTransactionsPage from './pages/admin/AdminTransactionsPage';

// Staff pages
import StaffCheckinPage from './pages/staff/StaffCheckinPage';

// Error pages
import ForbiddenPage from './pages/ForbiddenPage';
import NotFoundPage from './pages/NotFoundPage';

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <SnackbarProvider maxSnack={3} anchorOrigin={{ vertical: 'top', horizontal: 'right' }}>
        <BrowserRouter>
          <AuthProvider>
            <Routes>
              {/* ─── Public + Customer routes (MainLayout) ─── */}
              <Route element={<MainLayout />}>
                <Route path="/" element={<HomePage />} />
                <Route path="/events" element={<EventListPage />} />
                <Route path="/events/:id" element={<EventDetailPage />} />
                <Route path="/marketplace" element={<MarketplacePage />} />
                <Route path="/marketplace/:id" element={<ListingDetailPage />} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />
                <Route path="/verify-email" element={<VerifyEmailPage />} />

                {/* Authenticated customer routes */}
                <Route element={<ProtectedRoute allowedRoles={['CUSTOMER', 'ORGANIZER', 'ADMIN', 'STAFF']} />}>
                  <Route path="/my-bookings" element={<MyBookingsPage />} />
                  <Route path="/my-bookings/:id" element={<BookingDetailPage />} />
                  <Route path="/events/:eventId/booking" element={<CreateBookingPage />} />
                  <Route path="/payment/:bookingId" element={<PaymentPage />} />
                  <Route path="/payment/cancel" element={<PaymentCancelPage />} />
                  <Route path="/payment/success" element={<PaymentSuccessPage />} />
                  <Route path="/my-tickets" element={<MyTicketsPage />} />
                  <Route path="/my-listings" element={<MyListingsPage />} />
                  <Route path="/profile" element={<ProfilePage />} />
                </Route>
              </Route>

              {/* ─── Organizer portal ─── */}
              <Route element={<ProtectedRoute allowedRoles={['ORGANIZER']} />}>
                <Route element={<OrganizerLayout />}>
                  <Route path="/organizer/dashboard" element={<OrganizerDashboardPage />} />
                  <Route path="/organizer/events" element={<OrganizerEventsPage />} />
                  <Route path="/organizer/events/create" element={<EventFormPage />} />
                  <Route path="/organizer/events/:id/edit" element={<EventFormPage />} />
                  <Route path="/organizer/venues" element={<OrganizerVenuesPage />} />
                  <Route path="/organizer/promo-codes" element={<OrganizerPromoCodesPage />} />
                  <Route path="/organizer/wallet" element={<OrganizerWalletPage />} />
                  <Route path="/organizer/checkin" element={<OrganizerCheckinPage />} />
                </Route>
              </Route>

              {/* ─── Admin portal ─── */}
              <Route element={<ProtectedRoute allowedRoles={['ADMIN']} />}>
                <Route element={<AdminLayout />}>
                  <Route path="/admin/dashboard" element={<AdminDashboardPage />} />
                  <Route path="/admin/users" element={<AdminUsersPage />} />
                  <Route path="/admin/events" element={<AdminEventsPage />} />
                  <Route path="/admin/promo-codes" element={<AdminPromoCodesPage />} />
                  <Route path="/admin/wallets" element={<AdminWalletsPage />} />
                  <Route path="/admin/categories" element={<AdminCategoriesPage />} />
                  <Route path="/admin/venues" element={<AdminVenuesPage />} />
                  <Route path="/admin/transactions" element={<AdminTransactionsPage />} />
                </Route>
              </Route>

              {/* ─── Staff portal ─── */}
              <Route element={<ProtectedRoute allowedRoles={['STAFF', 'ADMIN']} />}>
                <Route element={<StaffLayout />}>
                  <Route path="/staff/checkin" element={<StaffCheckinPage />} />
                </Route>
              </Route>

              {/* ─── Error pages ─── */}
              <Route path="/403" element={<ForbiddenPage />} />
              <Route path="*" element={<NotFoundPage />} />
            </Routes>
          </AuthProvider>
        </BrowserRouter>
      </SnackbarProvider>
    </ThemeProvider>
  );
}

export default App;
