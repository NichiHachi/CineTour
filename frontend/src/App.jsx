// frontend/src/App.jsx
import React from "react";
import { BrowserRouter as Router, Route, Switch } from "react-router-dom";
import UserForm from "./UserForm";
import HomePage from "./HomePage";
import { UserProvider } from "./UserContext";

function App() {
  return (
    <UserProvider>
      <Router>
        <div className="App">
          <header className="App-header">
            <Switch>
              <Route path="/login" component={UserForm} />
              <Route path="/" component={HomePage} />
            </Switch>
          </header>
        </div>
      </Router>
    </UserProvider>
  );
}

export default App;
