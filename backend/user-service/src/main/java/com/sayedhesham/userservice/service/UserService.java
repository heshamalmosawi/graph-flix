package com.sayedhesham.userservice.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sayedhesham.userservice.dto.UserDTO;
import com.sayedhesham.userservice.dto.UserPatchDTO;
import com.sayedhesham.userservice.model.User;
import com.sayedhesham.userservice.repository.UserRepository;

@Service
public class UserService {

    private static final String USER_NOT_FOUND = "User not found";

    private final UserRepository userRepo;

    public UserService(UserRepository userRepository) {
        this.userRepo = userRepository;
    }

    public List<UserDTO> getAllUsers() {
        return userRepo.findAll()
                .stream()
                .map(user -> UserDTO.builder()
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build())
                .toList();
    }

    public UserDTO getById(String id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));

        return UserDTO.builder()
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    public UserDTO update(String id, UserPatchDTO user) {
        User existingUser = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));

        updateBasicFields(existingUser, user);
        updateRole(existingUser, user.getRole());

        User updatedUser = userRepo.save(existingUser);
        return UserDTO.builder()
                .name(updatedUser.getName())
                .email(updatedUser.getEmail())
                .role(updatedUser.getRole())
                .build();
    }

    private void updateBasicFields(User existingUser, UserPatchDTO user) {
        if (user.getName() != null && !user.getName().isEmpty()) {
            existingUser.setName(user.getName());
        }
        if (user.getEmail() != null && user.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            existingUser.setEmail(user.getEmail());
        }
    }

    private void updateRole(User existingUser, String role) {
        if (role == null) {
            return;
        }
        if (role.equals("user") || role.equals("admin")) {
            existingUser.setRole(role);
        } else {
            throw new RuntimeException("Invalid role");
        }
    }

    public void delete(String id) {
        User existingUser = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));
        userRepo.delete(existingUser);
    }
}
