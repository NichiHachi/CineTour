import React, { useState, useEffect } from 'react';
import { useCookies } from 'react-cookie';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import API_ENDPOINTS from './resources/api-links';

function UserProfile() {
  const [cookies] = useCookies(['username']);
  const [userInfo, setUserInfo] = useState(null);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchUserInfo = async () => {
      if (cookies.username) {
        try {
          const response = await axios.get(API_ENDPOINTS.profile(cookies.username), {
            withCredentials: true
          });
          setUserInfo(response.data);
        } catch (err) {
          setError('Failed to load user information');
          console.error('Error fetching user info:', err);
        }
      } else {
        navigate('/login');
      }
    };

    fetchUserInfo();
  }, [cookies.username, navigate]);

  if (!cookies.username) {
    return <div>Redirecting to login...</div>;
  }

  if (error) {
    return <div>{error}</div>;
  }

  if (!userInfo) {
    return <div>Loading...</div>;
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
    </div>
  );
}

export default UserProfile;
