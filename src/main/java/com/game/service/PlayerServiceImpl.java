package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class PlayerServiceImpl implements PlayerService {

    private PlayerRepository playerRepository;

    public PlayerServiceImpl() {

    }

    @Autowired
    public PlayerServiceImpl(PlayerRepository playerRepository) {
        super();
        this.playerRepository = playerRepository;
    }

    @Override
    public List<Player> getPlayersList(String name, String title, Race race, Profession profession,
                                       Long after, Long before, Boolean banned, Integer minExperience,
                                       Integer maxExperience, Integer minLevel, Integer maxLevel) {
        List<Player> players = new ArrayList<>();
        playerRepository.findAll().forEach( player -> {

            if (name != null && !player.getName().contains(name)) return;
            if (title != null && !player.getTitle().contains(title)) return;
            if (race != null && player.getRace() != race) return;
            if (profession != null && player.getProfession() != profession) return;
            if (after != null && player.getBirthday().getTime() < after) return;
            if (before != null && player.getBirthday().getTime() > before) return;
            if (banned != null && player.getBanned().booleanValue() != banned.booleanValue()) return;
            if (minExperience != null && player.getExperience().compareTo(minExperience) < 0) return;
            if (maxExperience != null && player.getExperience().compareTo(maxExperience) > 0) return;
            if (minLevel != null && player.getLevel().compareTo(minLevel) < 0) return;
            if (maxLevel != null && player.getLevel().compareTo(maxLevel) > 0) return;


            players.add(player);
        });

        return players;
    }

    @Override
    public Player createPlayer(Player player) {
        return playerRepository.save(player);
    }

    @Override
    public Player editPlayer(Player oldPlayer, Player newPlayer) throws IllegalArgumentException {


            final String name = newPlayer.getName();
        if (name != null) {
            if (isNameValid(name)) {
                oldPlayer.setName(name);
            } else {
                throw new IllegalArgumentException();
            }
        }
        final String title = newPlayer.getTitle();
        if (title != null) {
            if (isTitleValid(title)) {
                oldPlayer.setTitle(title);
            } else {
                throw new IllegalArgumentException();
            }
        }
        if (newPlayer.getRace() != null) {
            oldPlayer.setRace(newPlayer.getRace());
        }
        if (newPlayer.getProfession() != null) {
            oldPlayer.setProfession(newPlayer.getProfession());
        }
        final Date birthday = newPlayer.getBirthday();
        if (birthday != null) {
            if (isBirthdayValid(birthday)) {
                oldPlayer.setBirthday(newPlayer.getBirthday());
            } else {
                throw new IllegalArgumentException();
            }
        }
        if (newPlayer.getBanned() != null)
            oldPlayer.setBanned(newPlayer.getBanned());

        if (newPlayer.getExperience() != null) {
            if (isExperienceValid(newPlayer.getExperience())) {
                oldPlayer.setExperience(newPlayer.getExperience());

                oldPlayer.setLevel((int) (Math.sqrt(2500 + 200 * oldPlayer.getExperience()) - 50) / 100);
                oldPlayer.setUntilNextLevel(50 * (oldPlayer.getLevel() + 1) * (oldPlayer.getLevel() + 2) - oldPlayer.getExperience());

            }
            else {
                throw new IllegalArgumentException();
            }
        }
        


        playerRepository.save(oldPlayer);
        return oldPlayer;
    }

    @Override
    public void removePlayer(Player player) {
        playerRepository.delete(player);
    }

    @Override
    public Player getPlayerById(Long id) {
        return playerRepository.findById(id).orElse(null);
    }


    @Override
    public List<Player> sortPlayers(List<Player> players, PlayerOrder order) {
        if (order != null) {
            players.sort((player1, player2) -> {
                switch (order) {
                    case ID: return player1.getId().compareTo(player2.getId());
                    case NAME: return player1.getName().compareTo(player2.getName());
                    case EXPERIENCE: return player1.getExperience().compareTo(player2.getExperience());
                    case BIRTHDAY: return player1.getBirthday().compareTo(player2.getBirthday());
                    case LEVEL: return player1.getLevel().compareTo(player2.getLevel());
                    default: return 0;
                }
            });
        }
        return players;
    }

    @Override
    public List<Player> getPage(List<Player> players, Integer pageNumber, Integer pageSize) {
        final int page = pageNumber == null ? 0 : pageNumber;
        final int size = pageSize == null ? 3 : pageSize;
        final int from = page * size;
        int to = from + size;
        if (to > players.size()) to = players.size();
        return players.subList(from, to);
    }

    @Override
    public boolean isPlayerValid(Player player) {
        return player != null && isNameValid(player.getName())
                && isTitleValid(player.getTitle())
                && player.getRace() != null
                && player.getProfession() != null
                && isBirthdayValid(player.getBirthday())
                && isExperienceValid(player.getExperience());
    }

    private boolean isNameValid(String name) {
        return name != null && !name.isEmpty() && name.length() <= 12;
    }

    private boolean isTitleValid(String title) {
        return title != null && title.length() <= 30;
    }

    private boolean isBirthdayValid(Date birthday) {
        Calendar after = Calendar.getInstance();
        after.set(Calendar.YEAR, 2000);
        Calendar before = Calendar.getInstance();
        before.set(Calendar.YEAR, 3000);
        return birthday != null && birthday.getTime() >= after.getTimeInMillis()
                && birthday.getTime() <= before.getTimeInMillis()
                && birthday.getTime() > 0;
    }

    private boolean isExperienceValid(Integer experience) {
        return experience != null && experience >= 0 && experience <= 10_000_000;
    }

}
