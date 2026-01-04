package com.ngaleano.lol_manager.controller;

import com.ngaleano.lol_manager.model.Player;
import com.ngaleano.lol_manager.model.User;
import com.ngaleano.lol_manager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        if (user.getEmail() == null || user.getPassword() == null) {
            throw new RuntimeException("Email y Contraseña requeridos");
        }
        return userService.registerUser(user);
    }

    @PostMapping("/{userId}/link/start")
    public Player startLink(@PathVariable Long userId,
            @RequestParam String name,
            @RequestParam String tag) {
        return userService.startLinkAccount(userId, name, tag);
    }

    @PostMapping("/{userId}/link/verify")
    public String verifyLink(@PathVariable Long userId) {
        boolean result = userService.verifyLinkAccount(userId);
        return result ? "Cuenta verificada con exito" : "Fallo la verificacion";
    }
}