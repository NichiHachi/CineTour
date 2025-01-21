// frontend/src/App.jsx
import React, { useState } from "react";
import { BrowserRouter as Router, Route, Switch } from "react-router-dom";
import UserForm from "./UserForm";
import HomePage from "./HomePage";
import { UserProvider } from "./UserContext";

function App() {
  const [user, setUser] = useState(null);

  const onUserAdded = (newUser) => {
    setUser(newUser);
  };

  return (
    <UserProvider>
      <Router>
        <div className="App">
          <header className="App-header">
            <Switch>
              <Route
                path="/login"
                render={() => <UserForm onUserLogin={onUserAdded} />}
              />
              <Route path="/" component={HomePage} />
            </Switch>
          </header>
        </div>
      </Router>
    </UserProvider>
  );
}

export default App;
