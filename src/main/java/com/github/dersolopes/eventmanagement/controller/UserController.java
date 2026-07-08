package com.github.dersolopes.eventmanagement.controller;

import com.github.dersolopes.eventmanagement.entity.User;
import com.github.dersolopes.eventmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository repository;

    @PostMapping
    public User criar(@RequestBody User user) {
        return repository.save(user);
    }
}
