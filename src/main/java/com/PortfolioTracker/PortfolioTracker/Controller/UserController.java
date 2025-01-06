package com.PortfolioTracker.PortfolioTracker.Controller;

import com.PortfolioTracker.PortfolioTracker.Entity.Users;
import com.PortfolioTracker.PortfolioTracker.Repository.UserRepository;
import com.PortfolioTracker.PortfolioTracker.Service.JWTService;
import com.PortfolioTracker.PortfolioTracker.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private JWTService jwtService;


    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    // 1. Add a new user (signup)
    @PostMapping("/add")
    public String addUser(@RequestBody Users user) {
        // Check if the username already exists
        Users existingUser = userRepository.findByUserName(user.getUserName());
        if (existingUser != null) {
            return "Username already exists";
        }
        user.setUuid(null);
        user.setPassword(encoder.encode(user.getPassword()));
        // Save new user
        userRepository.save(user);
        return jwtService.generateToken(user.getUserName());
    }

    // 2. Delete a user with the username
    @DeleteMapping("/delete/{username}")
    public String deleteUser(@PathVariable String username) {
        Users user = userRepository.findByUserName(username);
        if (user != null) {
            userRepository.delete(user);
            return "User deleted successfully.";
        } else {
            return "User not found.";
        }
    }

    // 3. Get all users
    @GetMapping("/all")
    public List<Users> getAllUsers() {
        return userRepository.findAll();
    }

    // 2. User login (validate username and password)
    @PostMapping("/login")
    public String loginUser(@RequestBody Users user) {
        // Find user by username
        Users existingUser = userRepository.findByUserName(user.getUserName());
        if (existingUser == null) {
            return "User not found";
        }

//        // Check if the password matches
//        Users foundUser = existingUser;
//        if (!foundUser.getPassword().equals(user.getPassword())) {
//            return "Invalid credentials";
//        }

        // Login successful
        return userService.verify(user);
    }

    // 4. Update the user password
    @PutMapping("/update/{username}")
    public String updateUserPassword(@PathVariable String username, @RequestBody Users updatedUser) {
        Users user = userRepository.findByUserName(username);
        if (user != null) {
            Users existingUser = user;
            // Update the user's password
            existingUser.setPassword(updatedUser.getPassword());
            userRepository.save(existingUser);
            return "Password updated successfully.";
        } else {
            return "User not found.";
        }
    }

    @GetMapping("/{username}")
    public Users getCurrentUser(@PathVariable String username){
       return userService.getCurrentUser(username);
    }

}
