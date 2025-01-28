import axios from 'axios';
import API_ENDPOINTS from "../resources/api-links";

export const getAllUsers = async () => {
    try {
        const response = await axios.get(API_ENDPOINTS.allUsers);
        return response.data;
    } catch (error) {
        console.error('Error fetching users:', error);
        throw error;
    }
};

export const getUserById = async (id) => {
    try {
        const response = await axios.get(API_ENDPOINTS.userById(id));
        return response.data;
    } catch (error) {
        console.error('Error fetching user by ID:', error);
        throw error;
    }
};

export const addUser = async (user) => {
    try {
      const response = await axios.post(API_ENDPOINTS.addUser, user, {
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
      const response = await axios.post(API_ENDPOINTS.isUserNotExists, user, {
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
        const response = await axios.post(API_ENDPOINTS.login, user);
        return response.data;
    } catch (error) {
        console.error('Error checking password:', error);
        throw error;
    }
}
