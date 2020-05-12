package com.cardsGame;

import com.cardsGame.dto.GameStateDTO;
import com.cardsGame.dto.PlayerGamePanelDTO;
import com.cardsGame.dto.ResponseDTO;
import com.cardsGame.service.GamerService;
import com.cardsGame.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller // This means that this class is a Controller
@RequestMapping(path="/demo") // This means URL's start with /demo (after Application path)
public class MainController {

    @Autowired
    private GamerService gamerService;

    @Autowired
    private PlayerService playerService;

    @PostMapping(path="/addGame", produces = { "application/json" }) // Map ONLY POST Requests
    public @ResponseBody
    ResponseDTO addNewGame (@RequestParam int numberOfPlayers, @RequestParam int numberOfCards) {
        return new ResponseDTO(null, gamerService.createGame(numberOfPlayers, numberOfCards));
    }

    @PostMapping(path="/enterGame") // Map ONLY POST Requests
    public @ResponseBody ResponseDTO enterGame (@RequestParam int numericId, @RequestParam String gameCode,
                                                @RequestParam(required = false) String nickName) {
        System.out.println("enterGame");
        return new ResponseDTO(null, gamerService.enterGame((short)numericId, gameCode, nickName));
    }

    @PostMapping(path="/distributeCards") // Map ONLY POST Requests
    public @ResponseBody ResponseDTO distributeCards (@RequestParam(required = false) Integer numberOfCardsPerPlayer, @RequestParam String gameCode) {
        System.out.println("distributeCards");
        this.gamerService.distributeCards(numberOfCardsPerPlayer, gameCode);
        return new ResponseDTO(null, numberOfCardsPerPlayer + " cards distributed per person");
    }

    @PostMapping(path="/setLeader") // Map ONLY POST Requests
    public @ResponseBody void setLeader (@RequestParam String gameCode, @RequestParam String playerCode) {
        gamerService.setLeader(gameCode, playerCode);
    }

    @PostMapping(path="/openTrump") // Map ONLY POST Requests
    public @ResponseBody ResponseDTO openTrump (@RequestParam String gameCode, @RequestParam String playerCode) {
        System.out.println("openTrump");
        gamerService.openTrump(gameCode, playerCode);
        return new ResponseDTO(null, "Trump opened");
    }

    @PostMapping(path="/setTrump") // Map ONLY POST Requests
    public @ResponseBody ResponseDTO setTrump (@RequestParam short trump, @RequestParam String gameCode, @RequestParam String playerCode) {
        System.out.println("setTrump");
        gamerService.setTrump(trump, gameCode, playerCode);
        return new ResponseDTO(null, "Trump set");
    }

    @PostMapping(path="/moveCard") // Map ONLY POST Requests
    public @ResponseBody ResponseDTO moveCard (@RequestParam short card, @RequestParam String gameCode, @RequestParam String playerCode) {
        System.out.println("moveCard");
        gamerService.moveCard(card, playerCode, gameCode);
        return new ResponseDTO(null, "Card moved");
    }

    @PostMapping(path="/chooseWinner") // Map ONLY POST Requests
    public @ResponseBody void chooseWinner (@RequestParam short winnerPlayerNumericCode,
                                            @RequestParam String gameCode, @RequestParam String adminPlayerCode) {
        System.out.println("chooseWinner");
        gamerService.chooseWinner(adminPlayerCode, winnerPlayerNumericCode, gameCode);
    }

    @GetMapping(path="/gameState")
    public @ResponseBody
    GameStateDTO getGameState(@RequestParam String gameCode,
                              @RequestParam String playerCode) {
        System.out.println("gameState");
        return gamerService.getGameState(gameCode, playerCode);
    }


    @GetMapping(path="/playerState")
    public @ResponseBody
    PlayerGamePanelDTO getPlayerState(HttpServletRequest request, @RequestParam String playerCode) {
        System.out.println("ip is " + request.getRemoteAddr());
        System.out.println("playerState " + playerCode);
        return gamerService.getPlayerStat(playerCode);
    }
}
