// frontend/src/App.jsx
import React, { useEffect, useState } from "react";
import { getAllUsers } from "./api";
import AddUserForm from "./AddUserForm";

function App() {
  const [users, setUsers] = useState([]);

  useEffect(() => {
    const fetchUsers = async () => {
      try {
        const usersData = await getAllUsers();
        setUsers(usersData);
      } catch (error) {
        console.error("Error fetching users:", error);
      }
    };

    fetchUsers();
  }, []);

  const handleUserAdded = (newUser) => {
    setUsers([...users, newUser]);
  };

  return (
    <div className="App">
      <header className="App-header">
        <h1>Liste des utilisateurs</h1>
        <AddUserForm onUserAdded={handleUserAdded} />
        <ul>
          {users.map((user) => (
            <li key={user.id}>
              {user.username} - {user.email} - {user.password}
            </li>
          ))}
        </ul>
      </header>
    </div>
  );
}

export default App;
