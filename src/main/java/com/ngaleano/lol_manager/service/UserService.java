package com.ngaleano.lol_manager.service;

import com.ngaleano.lol_manager.model.Player;
import com.ngaleano.lol_manager.model.User;

public interface UserService {

    User registerUser(User user);

    Player startLinkAccount(Long userId, String summonerName, String tagLine);

    boolean verifyLinkAccount(Long userId);
}