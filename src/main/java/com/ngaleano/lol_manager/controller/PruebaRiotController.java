package com.ngaleano.lol_manager.controller;

import com.ngaleano.lol_manager.dto.RiotAccountDTO;
import com.ngaleano.lol_manager.service.RiotApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/prueba-riot")
public class PruebaRiotController {

    @Autowired
    private RiotApiService riotApiService;

    @GetMapping
    public RiotAccountDTO buscar(@RequestParam String nombre, @RequestParam String tag) {
        return riotApiService.getAccountRiot(nombre, tag);
    }
}