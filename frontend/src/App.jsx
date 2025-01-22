import React from "react";
import { BrowserRouter as Router, Route, Switch } from "react-router-dom";
import { CookiesProvider } from 'react-cookie';
import UserForm from "./UserForm";
import HomePage from "./HomePage";
import UserProfile from "./UserProfile";
import { UserProvider } from "./UserContext";

function App() {
  return (
    <CookiesProvider>
      <UserProvider>
        <Router>
          <div className="App">
            <header className="App-header">
              <Switch>
                <Route exact path="/" component={HomePage} />
                <Route path="/login" component={UserForm} />
                <Route path="/profile" component={UserProfile} />
              </Switch>
            </header>
          </div>
        </Router>
      </UserProvider>
    </CookiesProvider>
  );
}

export default App;