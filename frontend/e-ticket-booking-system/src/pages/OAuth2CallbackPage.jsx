import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import LoadingScreen from '../components/common/LoadingScreen';

export default function OAuth2CallbackPage() {
  const { refreshUser } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    refreshUser().then((user) => {
      if (user) navigate('/');
      else navigate('/login?error=true');
    });
    // eslint-disable-next-line
  }, []);

  return <LoadingScreen message="Đang đăng nhập với Google..." />;
}
