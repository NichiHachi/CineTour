import axios from 'axios';

export const getAllUsers = async () => {
    try {
        const response = await axios.get(`/users/all`);
        return response.data;
    } catch (error) {
        console.error('Error fetching users:', error);
        throw error;
    }
};

export const getUserById = async (id) => {
    try {
        const response = await axios.get(`/users/${id}`);
        return response.data;
    } catch (error) {
        console.error('Error fetching user by ID:', error);
        throw error;
    }
};

export const addUser = async (user) => {
    try {
      const response = await axios.post('/users/add', user, {
        headers: {
          'Content-Type': 'application/json'
        }
      });
      return response.data;
    } catch (error) {
      console.error('Error adding user:', error.response?.data || error.message);
      throw error;
    }
  };

export const isUsernameAvailable = async (user) => {
    try {
      const response = await axios.post('/users/isUserNotExist', user, {
        headers: {
          'Content-Type': 'application/json'
        }
      });
      return response.data;
    } catch (error) {
      console.error('Error checking username availability:', error.response?.data || error.message);
      throw error;
    }
  };

export const correctPassword = async (user) => {
    try {
        const response = await axios.post(`/users/login`, user);
        return response.data;
    } catch (error) {
        console.error('Error checking password:', error);
        throw error;
    }
}