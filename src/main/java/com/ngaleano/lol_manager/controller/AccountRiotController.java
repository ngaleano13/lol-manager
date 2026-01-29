package com.ngaleano.lol_manager.controller;

import com.ngaleano.lol_manager.dto.RiotAccountDTO;
import com.ngaleano.lol_manager.service.RiotApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account-riot")
public class AccountRiotController {

    @Autowired
    private RiotApiService riotApiService;

    @GetMapping
    public RiotAccountDTO searchAccount(@RequestParam String name, @RequestParam String tag) {
        return riotApiService.getAccountRiot(name, tag);
    }
}