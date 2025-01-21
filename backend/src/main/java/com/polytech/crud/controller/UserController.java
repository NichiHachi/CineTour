package com.polytech.crud.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.polytech.crud.entity.User;
import com.polytech.crud.service.UserService;

@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService service;

    @PostMapping("/add")
    public User addUser(@RequestBody User user) {
        logger.info("User added:  {}", user);
        return service.saveUser(user);
    }

    @GetMapping("/all")
    public List<User> findAllUsers() {
        logger.info("All users");
        return service.getAllUsers();
    }

    @GetMapping("/{id}")
    public User findUserById(@PathVariable Long id) {
        return service.getUserById(id);
    }

    @GetMapping("/{username}")
    public User findUserByUsername(@PathVariable String username) {
        return service.getUserByUsername(username);
    }

    @PutMapping("/update")
    public User updateUser(@RequestBody User user) {
        return service.updateUser(user);
    }

    @DeleteMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        return service.deleteUserById(id);
    }

    @PostMapping("/isUserNotExist")
    public Boolean isUserNotExist(@RequestBody User user) {
        User existingUser = service.getUserByUsername(user.getUsername());
        logger.info("User:  {}", user);
        logger.info("Username:  {}", user.getUsername());
        logger.info("User:  {}", existingUser);
        logger.info("User exist:  {}", existingUser == null);
        return existingUser == null;
    }

    @PostMapping("/login")
    public User canLogin(@RequestBody User user) {
        User existingUser = service.getUserByUsername(user.getUsername());
        if (existingUser != null && existingUser.getPassword().equals(user.getPassword())) {
            return existingUser;
        }
        return null;
    }
}
