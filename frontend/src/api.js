// frontend/src/api.js
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
        console.log('user:', user);
        const response = await axios.post(`/users/add`, user);
        return response.data;
    } catch (error) {
        console.error('Error adding user:', error);
        throw error;
    }
};

// Ajoutez d'autres fonctions pour les autres endpoints si nécessaire