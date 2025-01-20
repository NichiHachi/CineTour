import { createGlobalStyle } from 'styled-components'

const GlobalStyle = createGlobalStyle`
  * {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
  }

  body {
    background-color: ${({ theme }) => theme.colors.background};
    color: ${({ theme }) => theme.colors.text};
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen',
      'Ubuntu', 'Cantarell', 'Fira Sans', 'Droid Sans', 'Helvetica Neue',
      sans-serif;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    transition: all 0.3s ease;
  }

  h1 {
    ${({ theme }) => theme.typography.h1}
  }

  h2 {
    ${({ theme }) => theme.typography.h2}
  }

  h3 {
    ${({ theme }) => theme.typography.h3}
  }

  p {
    ${({ theme }) => theme.typography.body}
  }

  a {
    color: inherit;
    text-decoration: none;
  }
`

export default GlobalStyle
