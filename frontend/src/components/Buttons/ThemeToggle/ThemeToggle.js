import React from 'react'
import { useTheme } from '../../../utils/ThemeContext'
import LightModeIcon from '@mui/icons-material/LightMode'
import DarkModeIcon from '@mui/icons-material/DarkMode'
import styled from 'styled-components'

const RoundButton = styled.button`
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background-color: ${({ theme }) => theme.colors.secondary};
  color: ${({ theme }) => theme.text};
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
`

const ThemeToggle = () => {
  const { isDarkMode, toggleTheme } = useTheme()

  return (
    <RoundButton onClick={toggleTheme}>
      {isDarkMode ? <LightModeIcon /> : <DarkModeIcon />}
    </RoundButton>
  )
}

export default ThemeToggle
