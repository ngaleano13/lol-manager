package com.ngaleano.lol_manager.service;

import com.ngaleano.lol_manager.dto.LinkAccountResponseDTO;
import com.ngaleano.lol_manager.dto.RegisterUserDTO;
import com.ngaleano.lol_manager.dto.UserProfileDTO;

public interface UserService {
    void registerUser(RegisterUserDTO dto);

    LinkAccountResponseDTO startLinkAccount(Long userId, String summonerName, String tagLine);

    boolean verifyLinkAccount(Long userId);

    UserProfileDTO getUserProfile(Long userId);
}