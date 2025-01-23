package com.polytech.crud.controller;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
    public ResponseEntity<User> canLogin(@RequestBody User user, HttpServletResponse response) {
        User existingUser = service.getUserByUsername(user.getUsername());
        if (existingUser != null && existingUser.getPassword().equals(user.getPassword())) {
            // Create a new cookie with the username
            Cookie userCookie = new Cookie("username", existingUser.getUsername());
            userCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
            userCookie.setSecure(false);
            userCookie.setHttpOnly(false);
            userCookie.setPath("/");
            userCookie.setAttribute("SameSite", "Strict");

            // Add the cookie to the response
            response.addCookie(userCookie);

            // Return the user with OK status
            return ResponseEntity.ok(existingUser);
        }
        // Return unauthorized if login fails
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/profile/{username}")
    public ResponseEntity<User> getUserProfile(@PathVariable String username) {
        logger.info("Fetching profile for username: {}", username);
        User user = service.getUserByUsername(username);
        if (user != null) {
            // Don't send password in response
            user.setPassword(null);
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/all-cookies")
    public String readAllCookies(HttpServletRequest request) {

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            return Arrays.stream(cookies)
                    .map(c -> c.getName() + "=" + c.getValue()).collect(Collectors.joining(", "));
        }

        return "No cookies";
    }
}
