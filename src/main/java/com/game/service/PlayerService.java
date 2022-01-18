package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;

import java.util.List;

public interface PlayerService {
    List<Player> getPlayersList(
            String name,
            String title,
            Race race,
            Profession profession,
            Long after,
            Long before,
            Boolean banned,
            Integer minExperience,
            Integer maxExperience,
            Integer minLevel,
            Integer maxLevel
    );
    Player createPlayer(Player player);
    Player editPlayer(Player oldPlayer, Player newPlayer);
    void removePlayer(Player player);
    Player getPlayerById(Long id);
    List<Player> getPage(List<Player> players, Integer pageNumber, Integer pageSize);
    List<Player> sortPlayers(List<Player> players, PlayerOrder order);

    boolean isPlayerValid(Player player);
}
