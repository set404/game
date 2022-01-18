package com.game.controller;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PlayerController {
    private PlayerService playerService;

    public PlayerController() {
    }

    @Autowired
    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @RequestMapping(path = "/rest/players", method = RequestMethod.GET)
    public ResponseEntity<List<Player>> getAllPlayers(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "race", required = false) Race race,
            @RequestParam(value = "profession", required = false) Profession profession,
            @RequestParam(value = "after", required = false) Long after,
            @RequestParam(value = "before", required = false) Long before,
            @RequestParam(value = "banned", required = false) Boolean banned,
            @RequestParam(value = "minExperience", required = false) Integer minExperience,
            @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
            @RequestParam(value = "minLevel", required = false) Integer minLevel,
            @RequestParam(value = "maxLevel", required = false) Integer maxLevel,
            @RequestParam(value = "order", required = false) PlayerOrder order,
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false) Integer pageSize
            ) {
            final List<Player> players = playerService.getPlayersList(name, title, race, profession, after, before, banned, minExperience,
                maxExperience, minLevel, maxLevel);
            final List<Player> sortedPlayers = playerService.sortPlayers(players, order);
            final List<Player> pagePlayers = playerService.getPage(sortedPlayers, pageNumber, pageSize);
            return new ResponseEntity<>(pagePlayers, HttpStatus.OK);
    }

    @RequestMapping(path = "/rest/players/count", method = RequestMethod.GET)
    public Integer getPlayersCount(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "race", required = false) Race race,
            @RequestParam(value = "profession", required = false) Profession profession,
            @RequestParam(value = "after", required = false) Long after,
            @RequestParam(value = "before", required = false) Long before,
            @RequestParam(value = "banned", required = false) Boolean banned,
            @RequestParam(value = "minExperience", required = false) Integer minExperience,
            @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
            @RequestParam(value = "minLevel", required = false) Integer minLevel,
            @RequestParam(value = "maxLevel", required = false) Integer maxLevel
    ) {
        return playerService.getPlayersList(name, title, race, profession, after, before, banned, minExperience,
                maxExperience, minLevel, maxLevel).size();
    }

    @RequestMapping(path = "/rest/players", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Player> createPlayer(@RequestBody Player player) {
        if (!playerService.isPlayerValid(player)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (player.getBanned() == null) player.setBanned(false);

        player.setLevel((int) (Math.sqrt(2500 + 200 * player.getExperience())-50)/100);
        player.setUntilNextLevel(50 * (player.getLevel()+1) * (player.getLevel()+2) - player.getExperience());

        final Player savedPlayer = playerService.createPlayer(player);
        return new ResponseEntity<>(savedPlayer, HttpStatus.OK);
    }

    @RequestMapping(path = "/rest/players/{id}", method = RequestMethod.GET)
    public ResponseEntity<Player> getPlayerById(@PathVariable(value = "id") String pathId) {
        try {
            Long.parseLong(pathId);
        }
        catch (Exception e) {return new ResponseEntity<>(HttpStatus.BAD_REQUEST);}
        final Long id = Long.parseLong(pathId);
        if (id <= 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        final Player player = playerService.getPlayerById(id);
        if (player == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(player, HttpStatus.OK);
    }

    @RequestMapping(path = "/rest/players/{id}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Player> updatePlayer(
            @PathVariable(value = "id") String pathId,
            @RequestBody Player player
    ) {
        final Long id = pathId == null ? null : Long.parseLong(pathId);
        if (id == null || id <= 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        final ResponseEntity<Player> entity = getPlayerById(pathId);
        final Player savedPlayer = entity.getBody();
        if (savedPlayer == null) {
            return entity;
        }
        final Player result;
        try {
            result = playerService.editPlayer(savedPlayer, player);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(path = "/rest/players/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Player> removePlayer(@PathVariable(value = "id") String pathId) {
        try {
            Long.parseLong(pathId);
        }
        catch (Exception e) {return new ResponseEntity<>(HttpStatus.BAD_REQUEST);}
        final ResponseEntity<Player> entity = getPlayerById(pathId);
        final Player savedPlayer = entity.getBody();
        if (savedPlayer == null) {
            return entity;
        }
        playerService.removePlayer(savedPlayer);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
