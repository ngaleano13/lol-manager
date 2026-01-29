package com.ngaleano.lol_manager.controller;

import com.ngaleano.lol_manager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ngaleano.lol_manager.dto.LinkAccountResponseDTO;
import com.ngaleano.lol_manager.dto.RegisterUserDTO;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterUserDTO dto) {
        userService.registerUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/{userId}/link/start")
    public ResponseEntity<LinkAccountResponseDTO> startLink(@PathVariable Long userId,
            @RequestParam String name,
            @RequestParam String tag) {
        return ResponseEntity.ok(userService.startLinkAccount(userId, name, tag));
    }

    @PostMapping("/{userId}/link/verify")
    public ResponseEntity<String> verifyLink(@PathVariable Long userId) {
        boolean result = userService.verifyLinkAccount(userId);
        if (result) {
            return ResponseEntity.ok("Cuenta verificada con exito");
        } else {
            return ResponseEntity.badRequest().body("Fallo la verificacion");
        }
    }
}