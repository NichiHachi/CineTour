import React, { useState, useEffect } from 'react';
import { useCookies } from 'react-cookie';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import API_ENDPOINTS from "../../../resources/api-links";

function UserProfile() {
  const [cookies] = useCookies(['username']);
  const [userInfo, setUserInfo] = useState(null);
  const [error, setError] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    console.log('Effect triggered, cookies:', cookies);
    console.log('Document cookies:', document.cookie); // Vérification des cookies

    const username = cookies.username;
    
    if (!username) {
      console.log('No username cookie found, redirecting...');
      navigate('/login');
      return;
    }

    let isMounted = true;

    const fetchUserInfo = async () => {
      try {
        console.log('Fetching user info for:', username);
        const response = await axios.get(API_ENDPOINTS.profile(username), {
          withCredentials: true
        });

        console.log('Response received:', response.data);
        
        if (isMounted) {
          setUserInfo(response.data);
          setIsLoading(false);
        }
      } catch (err) {
        console.error('Fetch error:', err);
        if (isMounted) {
          setError('Failed to load user information');
          setIsLoading(false);
        }
      }
    };

    fetchUserInfo();

    return () => {
      console.log('Cleanup triggered');
      isMounted = false;
    };
  }, [cookies, navigate]);

  const handleLogout = async () => {
    try {
      await axios.post(API_ENDPOINTS.logout, {}, { withCredentials: true });

      navigate('/');
    } catch (error) {
      console.error('Logout failed:', error);
    }
  };

  if (isLoading) {
    return <div>Loading profile...</div>;
  }

  if (error) {
    return <div>Error: {error}</div>;
  }

  return (
    <div className="user-profile">
      <h2>User Profile</h2>
      <div className="profile-info">
        <p><strong>Username:</strong> {userInfo.username}</p>
        <p><strong>Email:</strong> {userInfo.email}</p>
      </div>
      <h3>Movie Search History</h3>
      {userInfo.movieSearchHistory && userInfo.movieSearchHistory.length > 0 ? (
        <ul>
          {userInfo.movieSearchHistory.map((historyItem, index) => (
            <li key={index}>
              <strong>Title:</strong> {historyItem.movieTitle}, <strong>Searched At:</strong> {new Date(historyItem.searchTime).toLocaleString()}
            </li>
          ))}
        </ul>
      ) : (
        <p>No movie search history available.</p>
      )}
      <button onClick={handleLogout}>Logout</button>
    </div>
  );
}

export default UserProfile;