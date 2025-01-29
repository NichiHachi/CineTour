import React, { useState, useEffect } from 'react'
import { useCookies } from 'react-cookie'
import { useNavigate } from 'react-router-dom'
import axios from 'axios'
import API_ENDPOINTS from '../../../resources/api-links'
import GlowContainer from '../../../components/GlowContainer/GlowContainer'
import Glow from '../../../components/Glow/Glow'
import './UserProfile.css'
import RevealText from '../../../components/TextEffects/RevealText/RevealText'

function UserProfile() {
  const [cookies] = useCookies(['username'])
  const [userInfo, setUserInfo] = useState(null)
  const [error, setError] = useState(null)
  const [isLoading, setIsLoading] = useState(true)
  const navigate = useNavigate()

  useEffect(() => {
    console.log('Effect triggered, cookies:', cookies)
    console.log('Document cookies:', document.cookie) // Vérification des cookies

    const username = cookies.username

    if (!username) {
      console.log('No username cookie found, redirecting...')
      navigate('/login')
      return
    }

    let isMounted = true

    const fetchUserInfo = async () => {
      try {
        console.log('Fetching user info for:', username)
        const response = await axios.get(API_ENDPOINTS.profile(username), {
          withCredentials: true,
        })

        console.log('Response received:', response.data)

        if (isMounted) {
          setUserInfo(response.data)
          setIsLoading(false)
        }
      } catch (err) {
        console.error('Fetch error:', err)
        if (isMounted) {
          setError('Failed to load user information')
          setIsLoading(false)
        }
      }
    }

    fetchUserInfo()

    return () => {
      console.log('Cleanup triggered')
      isMounted = false
    }
  }, [cookies, navigate])

  const handleLogout = async () => {
    try {
      await axios.post(API_ENDPOINTS.logout, {}, { withCredentials: true })

      navigate('/')
    } catch (error) {
      console.error('Logout failed:', error)
    }
  }

  if (isLoading) {
    return <div>Loading profile...</div>
  }

  if (error) {
    return <div>Error: {error}</div>
  }

  return (
    <GlowContainer className="user-profile-container">
      <Glow className="user-profile">
        <div className="profile-info">
          <div className="profile-header">
            <h2>Bonjour, {userInfo.username}</h2>
            {userInfo.email}
          </div>

          <h3>Historique de recherche</h3>
          {userInfo.movieSearchHistory &&
          userInfo.movieSearchHistory.length > 0 ? (
            <div className="profile-movies">
              {userInfo.movieSearchHistory.map((historyItem, index) => (
                <div className="profile-movie" key={index}>
                  {historyItem.movieTitle},{' '}
                  {new Date(historyItem.searchTime).toLocaleString()}
                </div>
              ))}
            </div>
          ) : (
            <div>No movie search history available.</div>
          )}
          <button onClick={handleLogout}>Logout</button>
        </div>
      </Glow>
    </GlowContainer>
  )
}

export default UserProfile
