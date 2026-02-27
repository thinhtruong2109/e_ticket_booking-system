import { useState, useEffect } from 'react';
import {
  Container,
  Grid,
  Paper,
  Typography,
  Box,
  TextField,
  Button,
  Divider,
  Alert,
  Tabs,
  Tab,
} from '@mui/material';
import { Save, Lock } from '@mui/icons-material';
import { useAuth } from '../../contexts/AuthContext';
import { userApi } from '../../api';
import { PageHeader } from '../../components/common';
import { getRoleLabel, getErrorMessage } from '../../utils/helpers';

const ProfilePage = () => {
  const { user, refreshUser } = useAuth();
  const [tab, setTab] = useState(0);

  // Profile form
  const [profile, setProfile] = useState({ fullName: '', phoneNumber: '' });
  const [profileLoading, setProfileLoading] = useState(false);
  const [profileMsg, setProfileMsg] = useState({ type: '', text: '' });

  // Password form
  const [password, setPassword] = useState({ currentPassword: '', newPassword: '', confirmPassword: '' });
  const [passwordLoading, setPasswordLoading] = useState(false);
  const [passwordMsg, setPasswordMsg] = useState({ type: '', text: '' });

  useEffect(() => {
    if (user) {
      setProfile({ fullName: user.fullName || '', phoneNumber: user.phoneNumber || '' });
    }
  }, [user]);

  const handleUpdateProfile = async (e) => {
    e.preventDefault();
    setProfileMsg({ type: '', text: '' });
    setProfileLoading(true);
    try {
      await userApi.updateProfile(profile);
      await refreshUser();
      setProfileMsg({ type: 'success', text: 'Profile updated successfully' });
    } catch (err) {
      setProfileMsg({ type: 'error', text: getErrorMessage(err) });
    } finally {
      setProfileLoading(false);
    }
  };

  const handleChangePassword = async (e) => {
    e.preventDefault();
    setPasswordMsg({ type: '', text: '' });

    if (password.newPassword !== password.confirmPassword) {
      setPasswordMsg({ type: 'error', text: 'Passwords do not match' });
      return;
    }

    setPasswordLoading(true);
    try {
      await userApi.changePassword({
        currentPassword: password.currentPassword,
        newPassword: password.newPassword,
      });
      setPasswordMsg({ type: 'success', text: 'Password changed successfully' });
      setPassword({ currentPassword: '', newPassword: '', confirmPassword: '' });
    } catch (err) {
      setPasswordMsg({ type: 'error', text: getErrorMessage(err) });
    } finally {
      setPasswordLoading(false);
    }
  };

  return (
    <>
      <PageHeader title="Profile" subtitle="Manage your account settings" />
      <Container maxWidth="md" sx={{ py: 4 }}>
        <Tabs
          value={tab}
          onChange={(e, v) => setTab(v)}
          sx={{ mb: 3, borderBottom: 1, borderColor: 'divider' }}
        >
          <Tab label="Profile" />
          <Tab label="Security" />
        </Tabs>

        {tab === 0 && (
          <Grid container spacing={3}>
            <Grid item xs={12} md={4}>
              <Paper sx={{ p: 3, textAlign: 'center' }}>
                <Box
                  sx={{
                    width: 80,
                    height: 80,
                    borderRadius: '50%',
                    bgcolor: 'grey.900',
                    color: 'white',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontSize: '2rem',
                    fontWeight: 700,
                    mx: 'auto',
                    mb: 2,
                  }}
                >
                  {user?.fullName?.[0]?.toUpperCase() || 'U'}
                </Box>
                <Typography variant="h6" fontWeight={600}>{user?.fullName}</Typography>
                <Typography variant="body2" color="text.secondary">{user?.email}</Typography>
                <Box sx={{ mt: 1 }}>
                  <Typography variant="overline" color="text.secondary">
                    {getRoleLabel(user?.role)}
                  </Typography>
                </Box>
              </Paper>
            </Grid>
            <Grid item xs={12} md={8}>
              <Paper sx={{ p: 3 }}>
                <Typography variant="h6" gutterBottom fontWeight={600}>
                  Personal Information
                </Typography>
                <Divider sx={{ mb: 3 }} />

                {profileMsg.text && (
                  <Alert severity={profileMsg.type} sx={{ mb: 2 }}>{profileMsg.text}</Alert>
                )}

                <form onSubmit={handleUpdateProfile}>
                  <TextField
                    label="Email"
                    fullWidth
                    value={user?.email || ''}
                    disabled
                    sx={{ mb: 2 }}
                    helperText="Email cannot be changed"
                  />
                  <TextField
                    label="Full Name"
                    fullWidth
                    value={profile.fullName}
                    onChange={(e) => setProfile({ ...profile, fullName: e.target.value })}
                    sx={{ mb: 2 }}
                  />
                  <TextField
                    label="Phone Number"
                    fullWidth
                    value={profile.phoneNumber}
                    onChange={(e) => setProfile({ ...profile, phoneNumber: e.target.value })}
                    sx={{ mb: 3 }}
                  />
                  <Button
                    type="submit"
                    variant="contained"
                    startIcon={<Save />}
                    disabled={profileLoading}
                  >
                    {profileLoading ? 'Saving...' : 'Save Changes'}
                  </Button>
                </form>
              </Paper>
            </Grid>
          </Grid>
        )}

        {tab === 1 && (
          <Paper sx={{ p: 3, maxWidth: 500 }}>
            <Typography variant="h6" gutterBottom fontWeight={600}>
              Change Password
            </Typography>
            <Divider sx={{ mb: 3 }} />

            {passwordMsg.text && (
              <Alert severity={passwordMsg.type} sx={{ mb: 2 }}>{passwordMsg.text}</Alert>
            )}

            <form onSubmit={handleChangePassword}>
              <TextField
                label="Current Password"
                type="password"
                fullWidth
                required
                value={password.currentPassword}
                onChange={(e) => setPassword({ ...password, currentPassword: e.target.value })}
                sx={{ mb: 2 }}
              />
              <TextField
                label="New Password"
                type="password"
                fullWidth
                required
                value={password.newPassword}
                onChange={(e) => setPassword({ ...password, newPassword: e.target.value })}
                sx={{ mb: 2 }}
                helperText="Min 8 characters, with uppercase, lowercase, and number"
              />
              <TextField
                label="Confirm New Password"
                type="password"
                fullWidth
                required
                value={password.confirmPassword}
                onChange={(e) => setPassword({ ...password, confirmPassword: e.target.value })}
                sx={{ mb: 3 }}
              />
              <Button
                type="submit"
                variant="contained"
                startIcon={<Lock />}
                disabled={passwordLoading}
              >
                {passwordLoading ? 'Changing...' : 'Change Password'}
              </Button>
            </form>
          </Paper>
        )}
      </Container>
    </>
  );
};

export default ProfilePage;
