package com.example.accessingdatamysql.service.impl;

import com.example.accessingdatamysql.dto.GameStateDTO;
import com.example.accessingdatamysql.dto.PlayerGamePanelDTO;
import com.example.accessingdatamysql.enums.CardType;
import com.example.accessingdatamysql.enums.GameType;
import com.example.accessingdatamysql.model.CardSet;
import com.example.accessingdatamysql.model.Game;
import com.example.accessingdatamysql.model.Move;
import com.example.accessingdatamysql.model.Player;
import com.example.accessingdatamysql.pojo.CardPOJO;
import com.example.accessingdatamysql.pojo.CardSetDTO;
import com.example.accessingdatamysql.pojo.PlayerInfoDTO;
import com.example.accessingdatamysql.repository.GameRepository;
import com.example.accessingdatamysql.service.CardSetService;
import com.example.accessingdatamysql.service.GamerService;
import com.example.accessingdatamysql.service.MoveService;
import com.example.accessingdatamysql.service.PlayerService;
import com.example.accessingdatamysql.util.CommonUtil;
import com.pusher.rest.Pusher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.smartcardio.Card;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class GamerServiceImpl implements GamerService {

    public static Pusher pusher;

    @PostConstruct
    public void init() {
        Pusher pusher = new Pusher("989784", "ea0535911cc427f0e599", "c7245a77988d004032a6");
        pusher.setCluster("mt1");
        pusher.setEncrypted(true);
        this.pusher = pusher;
    }


    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private MoveService moveService;

    @Autowired
    private CardSetService cardSetService;

    @Override
    public String createGame(int numberOfPlayers, int numberOfCards) {
        System.out.println("createGame numberOfCards " + numberOfCards + " numberOfPlayers " + numberOfPlayers);
        if (numberOfPlayers < 2 || numberOfPlayers > 4) {
            throw new RuntimeException("invalid player count " + numberOfPlayers);
        }

        if (numberOfCards > 52 || numberOfCards < 0) {
            throw new RuntimeException("invalid number of cards " + numberOfCards);
        }

        if (numberOfCards % numberOfPlayers != 0) {
            throw new RuntimeException(numberOfCards + " cards cannot be equally distributed among " + numberOfPlayers
                    + " players");
        }

        Game game = gameRepository.save(Game.builder()
                .code(CommonUtil.getSmallCapRandomString((short)6))
                .currentPlayer((short)1)
                .numberOfPlayers((short)numberOfPlayers)
                .numberOfCards((short)numberOfCards)
                .gameType(GameType.FIVE_ZERO_EIGHT.name())
                .build()
        );

        this.playerService.createPlayersInGame(game.getCode(), game.getNumberOfPlayers());
        return game.getCode();
    }

    @Override
    public String enterGame(short numericCode, String gameCode, String nickName) {
        Game game = this.gameRepository.findByCode(gameCode);
        if (null == game) {
            throw new RuntimeException("Invalid game code " + gameCode);
        }
        if (game.getNumberOfPlayers() < numericCode) {
            throw new RuntimeException("Only " + game.getNumberOfPlayers() + " can play in this game . " +
                    "Invalid numericCode " + numericCode);
        }
        Player player = this.playerService.getByGameCodeAndNumericCode(gameCode, numericCode);
        if (null == player) {
            throw new RuntimeException("Invalid game code " + gameCode + " or invalid numericCode " + numericCode);
        } else if (null != player.getCode()) {
            throw new RuntimeException("Player already entered for this id " + numericCode);
        } else {
            if (StringUtils.isEmpty(nickName)) {
                throw new RuntimeException("Please choose nick name");
            }
            player.setCode(CommonUtil.getSmallCapRandomString((short)6));
            if (!StringUtils.isEmpty(nickName) && nickName.length() > 10) {
                throw new RuntimeException("Choose smaller nick name");
            }
            player.setNickName(nickName);
            String playerCode = this.playerService.updatePlayer(player).getCode();
            GamerServiceImpl.pusher.trigger(gameCode, "player-entered",
                    Collections.singletonMap("message", "welcome " + nickName)
            );
            return playerCode;
        }
    }


    /**
     * leader can set trump and setLeader should work only if there is no move in progress
     * @param gameCode
     * @param playerCode
     */
    @Override
    public void setLeader(String gameCode, String playerCode) {
        Game game = this.gameRepository.findByCode(gameCode);
        if (null == game) {
            throw new RuntimeException("Invalid game code");
        }
        Player player = this.playerService.getByCode(playerCode);

        if (null == player) {
            throw new RuntimeException("Invalid player code");
        }
        // TODO check if there is no set pending in CardSet
        game.setCurrentPlayer(player.getNumericCode());
        this.gameRepository.save(game);
    }

    /**
     * Trump from ui will be actual number + 1
     * @param trump
     * @param gameCode
     * @param playerCode
     */
    @Override
    public void setTrump(short trump, String gameCode, String playerCode) {
        trump = (short) (trump - 1);
        Game game = this.gameRepository.findByCode(gameCode);
        if (null == game) {
            throw new RuntimeException("Invalid game code");
        }
        if (null != game.getTrumpCard()) {
            throw new RuntimeException("trump already set");
        }
        if (trump <  0 || trump > 3) {
            throw new RuntimeException("invalid trump card");
        }
        game.setTrumpCard(trump);
        Player player = this.playerService.getByCode(playerCode);

        if (null == player) {
            throw new RuntimeException("Invalid player code");
        }
        game.setTrumpSetByPlayerCode(player.getCode());
        game.setCurrentPlayer(player.getNumericCode());
        this.gameRepository.save(game);
        pusher.trigger(gameCode, "set-trump",
                Collections.singletonMap("message", "Trump set by player " + player.getNickName())
        );
    }

    @Override
    public void openTrump(String gameCode, String playerCode) {
        Game game = this.gameRepository.findByCode(gameCode);
        if (null == game) {
            throw new RuntimeException("Invalid game code");
        }
        if (null == game.getTrumpCard()) {
            throw new RuntimeException("Trump not yet set");
        }
        if (!playerCode.equalsIgnoreCase(game.getTrumpSetByPlayerCode())) {
            throw new RuntimeException("Not privileged to open trump card");
        }
        if (Boolean.TRUE.equals(game.getIsTrumpOpen())) {
            throw new RuntimeException("Trump already open");
        }
        game.setIsTrumpOpen(true);
        this.gameRepository.save(game);
        Player player = this.playerService.getByCode(playerCode);
        pusher.trigger(gameCode, "open-trump",
                Collections.singletonMap("message", "Trump opened by player " + player.getNickName())
        );
    }

    private Set<Short> getAlreadyDistributedCards(String gameCode) {
        List<Player> players = this.playerService.getByGameCode(gameCode);
        List<Short> alreadyDistributedCards = new ArrayList<>();
        for (Player player: players) {
            List<Short> distributedCards = player.getAllCards();
            if (!CollectionUtils.isEmpty(distributedCards)) {
                alreadyDistributedCards.addAll(distributedCards);
            }
        }
        return new HashSet<>(alreadyDistributedCards);
    }

    private List<Short> getCardsAfterRemovingNSmallCards(short smallCardsNumber) {
        //start from 1 --> which is duggi
        Set<Short> allCards = new HashSet<>();
        for (short i =0; i <52; i++) {
            allCards.add(i);
        }

        short initialStartIndex = 1;
        short startIndex = initialStartIndex;
        while (smallCardsNumber != 0) {
            allCards.remove(startIndex);
            startIndex = (short) (startIndex + 13);
            if (startIndex > 51) {
                initialStartIndex = (short) (initialStartIndex + 1);
                startIndex = initialStartIndex;
            }
            smallCardsNumber--;
        }
        return new ArrayList<>(allCards);
    }

    private List<Short> getUndistributedCards(Set<Short> alreadyDistributedCards,
                                              Short totalCards) {
        short smallCardsNumber = (short) (52 - totalCards);
        Set<Short> allValidCardSet = getCardsAfterRemovingNSmallCards(smallCardsNumber)
                .stream().collect(Collectors.toSet());
        List<Short> undistributedCards = new ArrayList<>();
        for (Short validCard: allValidCardSet) {
            if (!alreadyDistributedCards.contains(validCard)) {
                undistributedCards.add(validCard);
            }
        }
        return undistributedCards;
    }

    /**
     *
     * @param numberOfCardsPerPlayer if left empty it will distribute all remaining cards
     */
    @Override
    public void distributeCards(Integer numberOfCardsPerPlayer, String gameCode) {
        short leaderPlayer = (short) 1;
        Game game = this.gameRepository.findByCode(gameCode);
        if (null == game) {
            throw new RuntimeException("Enter game code or generate game code, generate player code. After that distribute cards");
        }
        //only if there is no move
        Set<Short> alreadyDistributedCards = getAlreadyDistributedCards(gameCode);
        List<Short> undistributedCards = getUndistributedCards(alreadyDistributedCards, game.getNumberOfCards());

        if (undistributedCards.isEmpty()) {
            throw new RuntimeException("All cards already distributed");
        }
        Collections.shuffle(undistributedCards);

        List<Player> players = this.playerService.getByGameCode(gameCode);

        if (CollectionUtils.isEmpty(players)) {
            throw new RuntimeException("Invalid game code " + gameCode);
        }

        Map<Short, Player> numericIdToPlayerMap = players.stream()
                .collect(Collectors.toMap(Player::getNumericCode, Function.identity()));

        int totalUndistributedCards = undistributedCards.size();

        numberOfCardsPerPlayer = null != numberOfCardsPerPlayer ? numberOfCardsPerPlayer
                : totalUndistributedCards/players.size();
        int totalCardsToBeDistributed = numberOfCardsPerPlayer * players.size();

        short currentPlayerWhichWillGetCard =  (short)(leaderPlayer != players.size() ? leaderPlayer + 1 : 1);

        int counter = 0;
        if (totalCardsToBeDistributed > totalUndistributedCards) {
            throw new RuntimeException("Only " + totalUndistributedCards/players.size() +
                    " cards per person are left");
        }
        while (totalUndistributedCards != 0 && totalCardsToBeDistributed != 0) {
            numericIdToPlayerMap.get(currentPlayerWhichWillGetCard).addCard(undistributedCards.get(counter));

            currentPlayerWhichWillGetCard =  (short) (currentPlayerWhichWillGetCard != players.size()
                    ? currentPlayerWhichWillGetCard + 1 : 1);

            totalUndistributedCards--;
            totalCardsToBeDistributed--;
            counter++;
        }
        if (totalCardsToBeDistributed == totalUndistributedCards) {
            game.setCanGameBeStarted(true);
            this.gameRepository.save(game);
        }
        this.playerService.updatePlayers(players);
        pusher.trigger(gameCode, "distribute-cards",
                Collections.singletonMap("message", numberOfCardsPerPlayer + " Cards per player distributed ")
        );
    }


    /**
     * Move validation should be there or make a method to undo move on approval of admin
     * @param card
     * @param playerCode
     * @param gameCode
     */
    @Override
    public void moveCard(short card, String playerCode, String gameCode) {
        Game game = this.gameRepository.findByCode(gameCode);
        if (null == game) {
            throw new RuntimeException("Invalid game code");
        }
        if (!Boolean.TRUE.equals(game.getCanGameBeStarted())) {
            throw new RuntimeException(("Game has not started yet. Please distribute all cards"));
        }
        Player player = this.playerService.getByCode(playerCode);
        if (null == player) {
            throw new RuntimeException("Invalid player code");
        }
        if (null == game.getCurrentPlayer()) {
            throw new RuntimeException("First decide winner of current game set");
        }
        if (!player.getNumericCode().equals(game.getCurrentPlayer())) {
            Player playerToMove = this.playerService.getByGameCodeAndNumericCode(game.getCode(), game.getCurrentPlayer());
            throw new RuntimeException("Invalid move. Currently "
                    + (null != playerToMove ? playerToMove.getNickName(): "player " + game.getCurrentPlayer()) + " has to move"
            );
        }

        CardSet cardSet = this.cardSetService.getActiveCardSet(gameCode);

        if (null != cardSet && !CollectionUtils.isEmpty(cardSet.getAllMoveIds())) {
            List<Move> moves = this.moveService.getByIds(cardSet.getAllMoveIds());
            Move move = moves.get(0);
            CardType requiredCardType = CommonUtil.getCardType(move.getCard());
            if (requiredCardType.equals(CommonUtil.getCardType(card))) {
                //okay
            } else {
                List<Short> allCards = player.getAllCards();
                CardType cardTypeFound = allCards.stream().map(cardNumber -> CommonUtil.getCardType(cardNumber))
                        .filter(cardTypeHere -> requiredCardType.equals(cardTypeHere)).findFirst().orElse(null);
                if (null != cardTypeFound) {
                    throw new RuntimeException("Please move " + cardTypeFound.name() + " Please move carefully ");
                }
            }
        }
        this.playerService.removeCard(playerCode, card);

        Move thisMove = this.moveService.createMove(gameCode, card, playerCode);



        //After every move check winner and set in card_set
        //calculate winner
        String winnerPlayerCodeTillNowForThisSet = this.getWinner(gameCode, thisMove, game.getTrumpCard(), game.getIsTrumpOpen());


        boolean isMoveEndMove = isMoveEndMoveOfSet(game.getNumberOfPlayers(), cardSet);

        if (isMoveEndMove) {
            game.setCurrentPlayer(null);

            new Thread(() -> {
                Player winnerPlayer = this.playerService.getByCode(winnerPlayerCodeTillNowForThisSet);
                System.out.println("Calculating winner in new thread");
                try {
                    Thread.sleep(5000);
                } catch (Exception ex) {
                    //System.out.println;
                }
                this.chooseWinner(gameCode, winnerPlayer.getNumericCode());
            }).start();
        } else {
            game.setCurrentPlayer(game.getNextPlayerToMove());
        }
        this.gameRepository.save(game);
        System.out.println("move done");
        Map<String, String> pusherMessage = new HashMap<>();
        pusherMessage.put("message", player.getNickName() + " has moved" );
        Player nextPlayer = null != game.getCurrentPlayer() ?
                this.playerService.getByGameCodeAndNumericCode(game.getCode(), game.getCurrentPlayer()) :
                this.playerService.getByCode(winnerPlayerCodeTillNowForThisSet);
        pusherMessage.put("playerToMove", nextPlayer.getNickName());
        pusher.trigger(gameCode, "move-event", pusherMessage);
        System.out.println("push done");
    }

    //moves in order
    //best player code
    private String getWinner(String gameCode, Move latestMove, Short trumpCard, Boolean isTrumpOpen) {
        CardSet cardSet = this.cardSetService.getActiveCardSet(gameCode);
        Long bestMoveIdTillNow = null;

        if (null != cardSet) {
            bestMoveIdTillNow = cardSet.getBestMoveIdTillNow();
        }

        if (null == bestMoveIdTillNow) {
            //1st move
            this.cardSetService.updateCardSet(gameCode, latestMove.getId(), latestMove.getId());
            return latestMove.getPlayerCode();
        }

        List<Move> movesToCompare = new ArrayList<>();
        Move bestMoveTillNow = this.moveService.getByIds(Collections.singletonList(cardSet.getBestMoveIdTillNow())).get(0);
        movesToCompare.add(bestMoveTillNow);
        movesToCompare.add(latestMove);

        if (null != trumpCard && Boolean.TRUE.equals(isTrumpOpen)) {
            CardType trumpCardType = CardType.getFromIndex(trumpCard);

            boolean bestMoveInTrumpPresent = movesToCompare.stream()
                    .filter(moveHere -> CommonUtil.getCardType(moveHere.getCard()).equals(trumpCardType))
                    .max(Move::compare)
                    .isPresent();
            if (bestMoveInTrumpPresent) {
                Move bestMove = movesToCompare.stream()
                        .filter(moveHere -> CommonUtil.getCardType(moveHere.getCard()).equals(trumpCardType))
                        .max(Move::compare).get();
                this.cardSetService.updateCardSet(gameCode, latestMove.getId(), bestMove.getId());
                return bestMove.getPlayerCode();
            }
        }
        //Move move = movesToCompare.get(0);
        CardType cardTypeRequired = CommonUtil.getCardType(bestMoveTillNow.getCard());
        Move bestMove = movesToCompare.stream()
                .filter(moveHere -> CommonUtil.getCardType(moveHere.getCard()).equals(cardTypeRequired))
                .max(Move::compare)
                .get();
        this.cardSetService.updateCardSet(gameCode, latestMove.getId(), bestMove.getId());
        return bestMove.getPlayerCode();
    }

    // Only admin can choose winner for a set
    // be default admin is player with numeric code 1
    @Override
    public void chooseWinner(String adminPlayerCode, short winnerPlayerNumericCode, String gameCode) {
        Player player = this.playerService.getByCode(adminPlayerCode);
        if (null == player) {
            throw new RuntimeException("Invalid adminPlayerCode");
        }
        if (!player.getNumericCode().equals((short)1)) {
            throw new RuntimeException("Only admin can choose winner");
        }
        this.chooseWinner(gameCode, winnerPlayerNumericCode);
    }

    private void chooseWinner(String gameCode, short winnerPlayerNumericCode) {
        Player winnerPlayer = this.playerService.getByGameCodeAndNumericCode(gameCode, winnerPlayerNumericCode);
        if (null == winnerPlayer) {
            throw new RuntimeException("Invalid winner numeric code");
        }
        Game game = this.gameRepository.findByCode(gameCode);
        if (null == game) {
            throw new RuntimeException("Invalid game code " + gameCode);
        }
        CardSet cardSet = this.cardSetService.getActiveCardSet(gameCode);

        List<Move> moves = this.moveService.getByGameCode(gameCode);

        // if no active cardSet in progress and game is initialized
        if (null == cardSet && !moves.isEmpty()) {
            throw new RuntimeException("No card set in progress. Player " + game.getCurrentPlayer() + " has to start game");
        }

        // game initlialising condition
        if (null == cardSet && moves.isEmpty()) {
            game.setCurrentPlayer(winnerPlayerNumericCode);
            return;
        }

        if (!isMoveEndMoveOfSet(game.getNumberOfPlayers(), cardSet)) {
            throw new RuntimeException("Card set still in progress, admin cannot choose winner");
        }
        cardSet.setIsCurrentSet(false);
        cardSet.setWinnerPlayerCode(winnerPlayer.getCode());
        this.cardSetService.updateCardSet(cardSet);

        game.setCurrentPlayer(winnerPlayerNumericCode);
        this.gameRepository.save(game);
        pusher.trigger(gameCode, "move-event", Collections.singletonMap("message", winnerPlayer.getNickName() + " has won this set" ));
    }

    private static boolean isMoveEndMoveOfSet(int totalPlayers, CardSet cardSet) {
        if (null == cardSet || CollectionUtils.isEmpty(cardSet.getAllMoveIds())) {
            return false;
        }
        if (cardSet.getAllMoveIds().size() == totalPlayers) {
            return true;
        }
        return false;
    }

    private List<PlayerInfoDTO> getFromCardSetListAndPlayersList(List<CardSet> cardSets, List<Player> players,
                                                                 Map<Short, Integer> playerNumericCodeToPointsMap,
                                                                 Player requestedPlayer,
                                                                 Short currentPlayer) {
       Map<String, Long> playerCodeToSetsWonCount = cardSets.stream()
               .filter(cardSet -> null != cardSet.getWinnerPlayerCode())
               .collect(Collectors.groupingBy(CardSet::getWinnerPlayerCode, Collectors.counting()));

        List<PlayerInfoDTO> playerInfoDTOS = new ArrayList<>();
        boolean isNumericCodeResetDone = false;
        short counter = 3;
       for (Player player: players) {
           PlayerInfoDTO playerInfoDTO = new PlayerInfoDTO();
           if (player.getNumericCode().equals(currentPlayer)) {
               playerInfoDTO.setToMove(true);
           }
           //playerInfoDTO.setNumericCode(player.getNumericCode());
           if (isNumericCodeResetDone) {
               if (counter == 5) {
                   counter = (short) 1;
               }
               playerInfoDTO.setNumericCode(counter++);
           }
           playerInfoDTO.setSetsWon(playerCodeToSetsWonCount.containsKey(player.getCode())
                   ? playerCodeToSetsWonCount.get(player.getCode()).intValue() : 0);
           playerInfoDTO.setCardsLeft((short) player.getAllCards().size());
           playerInfoDTO.setPoints(playerNumericCodeToPointsMap.get(player.getNumericCode()));
           playerInfoDTO.setNickName(player.getNickName());
           playerInfoDTOS.add(playerInfoDTO);
           if (requestedPlayer.getCode().equals(player.getCode())) {
               playerInfoDTO.setNumericCode(counter++);
               isNumericCodeResetDone = true;
           }
       }
       for (PlayerInfoDTO playerInfoDTO: playerInfoDTOS) {
           if (null == playerInfoDTO.getNumericCode()) {
               if (counter == 5) {
                   counter = (short) 1;
               }
               playerInfoDTO.setNumericCode(counter++);
           }
       }
       Collections.sort(playerInfoDTOS, Comparator.comparing(PlayerInfoDTO::getNumericCode));
       return playerInfoDTOS;
    }

    private Map<String, Integer> getPlayerCodeToPointsMap(List<CardSet> cardSets, String gameCode) {
        //to show stat
        Map<String, Integer> playerCodeToPointMap = new HashMap<>();
        Map<Long, Move> moveIdToMoveMap = this.moveService.getByGameCode(gameCode).stream()
                .collect(Collectors.toMap(Move::getId, Function.identity()));
        cardSets.forEach(cardSet -> {
            if (null != cardSet.getWinnerPlayerCode()) {
                cardSet.getAllMoveIds().forEach(
                        moveId -> {
                            Move move = moveIdToMoveMap.get(moveId);
                            Short card = move.getCard();
                            Integer point = GameType.getPointForFIVE_ZERO_EIGHT(card);
                            if (playerCodeToPointMap.containsKey(cardSet.getWinnerPlayerCode())) {
                                playerCodeToPointMap.put(cardSet.getWinnerPlayerCode(),
                                        playerCodeToPointMap.get(cardSet.getWinnerPlayerCode()) + point);
                            } else {
                                playerCodeToPointMap.put(cardSet.getWinnerPlayerCode(), point);
                            }
                        }
                );
            }
        });
        return playerCodeToPointMap;
    }

    @Override
    public GameStateDTO getGameState(String gameCode, String playerCode) {
        GameStateDTO gameStateDTO = new GameStateDTO();

        Game game = this.gameRepository.findByCode(gameCode);
        if (null == game) {
            throw new RuntimeException("Invalid game code " + gameCode);
        }

        Player player = this.playerService.getByCode(playerCode);
        if (null == player) {
            throw new RuntimeException("Generate player code");
        }
        gameStateDTO.setGameCode(gameCode);
        gameStateDTO.setPlayerToMove(game.getCurrentPlayer());
        gameStateDTO.setTrumpCard(Boolean.TRUE.equals(game.getIsTrumpOpen())
                ? CardType.getFromIndex(game.getTrumpCard()).name() : null);

        List<Player> players = this.playerService.getByGameCode(gameCode);
        /*Map<String, Short> playerCodeToNumericCode = players.stream()
                .filter(code -> null != code)
                .collect(Collectors.toMap(Player::getCode, Player::getNumericCode));*/
        Map<String, Short> playerCodeToNumericCode = new HashMap<>();

        Map<Short, String> numericCodeToNickNameMap = new HashMap<>();

        for (Player playerHere: players) {
            if (null != playerHere.getCode())
                playerCodeToNumericCode.put(playerHere.getCode(), playerHere.getNumericCode());
            if (null!= playerHere.getNumericCode() && null != playerHere.getNickName()) {
                numericCodeToNickNameMap.put(playerHere.getNumericCode(), playerHere.getNickName());
            }
        }

        if (null != game.getTrumpSetByPlayerCode()) {
            Short numericCode = playerCodeToNumericCode.get(game.getTrumpSetByPlayerCode());
            if (null != numericCode && numericCodeToNickNameMap.containsKey(numericCode)) {
                gameStateDTO.setTrumpDeclaredBy(numericCodeToNickNameMap.get(numericCode));
            } else {
                gameStateDTO.setTrumpDeclaredBy(String.valueOf(playerCodeToNumericCode.get(game.getTrumpSetByPlayerCode())));
            }
        }
        gameStateDTO.setCanGameBeStarted(game.getCanGameBeStarted());
        //gameStateToDisplay


        List<CardSet> cardSets = this.cardSetService.getByGameCode(gameCode);

        Map<Short, Integer> playerNumericCodeToPointsMap = new HashMap<>();

        if (cardSets.size() == (game.getNumberOfCards()/ game.getNumberOfPlayers())) {
            Map<String, Integer> playerCodeToPointsMap = this.getPlayerCodeToPointsMap(cardSets, gameCode);
            for (String playerCodeHere: playerCodeToPointsMap.keySet()) {
                playerNumericCodeToPointsMap.put(playerCodeToNumericCode.get(playerCodeHere),
                        playerCodeToPointsMap.get(playerCodeHere));
            }
        }
        gameStateDTO.setPlayerInfoDTOS(this.getFromCardSetListAndPlayersList(cardSets, players, playerNumericCodeToPointsMap,
                player, game.getCurrentPlayer()));




        CardSet activeCardSet = cardSets.stream().filter(cardSet -> Boolean.TRUE.equals(cardSet.getIsCurrentSet()))
                .findFirst().orElse(null);

        /*if (null == activeCardSet) {
            // waiting for 1st move of set
            //return gameStateDTO;
            // no active card set in progress
        } else if (null != activeCardSet && !activeCardSet.getAllMoveIds().isEmpty()) {
            // card set in progress
            List<Move> moves = this.moveService.getByIds(activeCardSet.getAllMoveIds());
            gameStateDTO.setCardSetDTOS(this.getFromMoves(moves, playerCodeToNumericCode, numericCodeToNickNameMap));
        }*/

        List<Move> moves = this.moveService.getByIds(null != activeCardSet ? activeCardSet.getAllMoveIds() : Collections.emptyList());
        gameStateDTO.setCardSetDTOS(this.getFromMoves(moves, players, playerCodeToNumericCode, numericCodeToNickNameMap,
                player));

        //List<CardSet> cardSets = this.cardSetService.getByGameCode(gameCode);

        // no move still
        gameStateDTO.setGameStateToDisplay("");
        if (cardSets.isEmpty()) {

            if (players.get(0).getAllCards().size() != game.getNumberOfCards() / game.getNumberOfPlayers()) {
                gameStateDTO.setGameStateToDisplay("Admin distribute all cards please\n");
            }

            if (null == game.getTrumpCard()) {
                gameStateDTO.setGameStateToDisplay(gameStateDTO.getGameStateToDisplay().concat(" Choose trump \n"));
                return gameStateDTO;
            }

            if (null == game.getCurrentPlayer()) {
                gameStateDTO.setGameStateToDisplay(gameStateDTO.getGameStateToDisplay().concat(" set leader for game start\n"));
            }

            if (null != game.getCurrentPlayer()) {
                if (numericCodeToNickNameMap.containsKey(game.getCurrentPlayer())) {
                    gameStateDTO.setGameStateToDisplay(gameStateDTO.getGameStateToDisplay()
                            .concat(numericCodeToNickNameMap.get(game.getCurrentPlayer()) + " it's your move\n"));
                } else {
                    gameStateDTO.setGameStateToDisplay(gameStateDTO.getGameStateToDisplay()
                            .concat(" Player " + game.getCurrentPlayer() + " it's your move\n"));
                }
            }
        } else if (null != game.getCurrentPlayer()) {
            if (numericCodeToNickNameMap.containsKey(game.getCurrentPlayer())) {
                gameStateDTO.setGameStateToDisplay(gameStateDTO.getGameStateToDisplay()
                        .concat(numericCodeToNickNameMap.get(game.getCurrentPlayer()) + " it's your move\n"));
            } else {
                gameStateDTO.setGameStateToDisplay(gameStateDTO.getGameStateToDisplay()
                        .concat(" Player " + game.getCurrentPlayer() + " it's your move\n"));
            }
        } else if (null == game.getCurrentPlayer()) {
            gameStateDTO.setGameStateToDisplay(gameStateDTO.getGameStateToDisplay()
                    .concat(" Admin  choose winner for current set \n"));
        }

        boolean canNewGameBeStarted = true;
        if (Boolean.TRUE.equals(gameStateDTO.getCanGameBeStarted())) {
            if (null != gameStateDTO.getPlayerInfoDTOS()) {
                for (PlayerInfoDTO playerInfoDTO: gameStateDTO.getPlayerInfoDTOS()) {
                    if (playerInfoDTO.getCardsLeft() == 0) {
                        continue;
                    }
                    canNewGameBeStarted = false;
                }
            }
        }
        if (canNewGameBeStarted) {
            gameStateDTO.setCanGameBeStarted(false);
        }
        return gameStateDTO;
    }

    private List<CardSetDTO> getFromMoves(List<Move> moves, List<Player> players, Map<String, Short> playerCodeToNumericCode,
                                          Map<Short, String> numericCodeToNickNameMap, Player player) {
        List<CardSetDTO> cardSetDTOS = new ArrayList<>();
        players.sort(Comparator.comparing(Player::getNumericCode));

        Map<Short, Move> playerNumericCodeToMoveMap = moves.stream()
                .collect(Collectors.toMap(move -> playerCodeToNumericCode.get(move.getPlayerCode()), Function.identity()));

        boolean isNumericCodeResetDone = false;
        short counter = 3;

        for (Player playerHere : players) {
            if (player.getCode().equals(playerHere.getCode())) {
                isNumericCodeResetDone = true;
            }
            if (isNumericCodeResetDone) {
                if (counter == 5) {
                    counter = 1;
                }
            }
            Move move = playerNumericCodeToMoveMap.get(playerHere.getNumericCode());
            cardSetDTOS.add(new CardSetDTO(null != move ? CommonUtil.getDisplayStringForCard(move.getCard()) : null,
                    isNumericCodeResetDone ? counter++ : null, null != move ? CommonUtil.getCardType(move.getCard()) : null, playerHere.getNickName()));
        }

        for (CardSetDTO cardSetDTO : cardSetDTOS) {
            if (counter == 5) {
                counter = 1;
            }
            cardSetDTO.setPlayerNumericCode(counter++);
        }
        cardSetDTOS.sort(Comparator.comparing(CardSetDTO::getPlayerNumericCode));
        return cardSetDTOS;
    }

    @Override
    public PlayerGamePanelDTO getPlayerStat(String playerCode) {
        System.out.println("getPlayerStat "+  playerCode);
        PlayerGamePanelDTO playerGamePanelDTO = new PlayerGamePanelDTO();
        Player player = this.playerService.getByCode(playerCode);
        if (null == player) {
            throw new RuntimeException("Invalid player code");
        }
        playerGamePanelDTO.setPlayerNumericCode(player.getNumericCode());
        List<CardPOJO> cardPOJOS = player.getAllCards().stream()
                .map(card -> new CardPOJO(card, CommonUtil.getCardIndex(card), CommonUtil.getCardType(card),
                        CommonUtil.getDisplayStringForCard(card)))
                .collect(Collectors.toList());
        Map<CardType, List<CardPOJO>> cardTypeListMap = cardPOJOS.stream()
                .collect(Collectors.groupingBy(cardPOJO -> cardPOJO.getCardType(),
                        Collectors.toList()));
        cardTypeListMap.values().forEach(list -> list.sort(Comparator.comparing(CardPOJO::getCard)));

        playerGamePanelDTO.setCardTypeToCardDisplayStringMap(cardTypeListMap);
        playerGamePanelDTO.setNickName(player.getNickName());
        Game game = this.gameRepository.findByCode(player.getGameCode());
        if (playerCode.equals(game.getTrumpSetByPlayerCode())) {
            playerGamePanelDTO.setTrumpCard(CardType.getFromIndex(game.getTrumpCard()).name());
        }
        playerGamePanelDTO.setPlayerCode(playerCode);
        playerGamePanelDTO.setOpenTrumpButton(!Boolean.TRUE.equals(game.getIsTrumpOpen()));

        List<CardSet> cardSets = this.cardSetService.getByGameCode(player.getGameCode());

        playerGamePanelDTO.setSetsWon((short) cardSets.stream()
                .filter(cardSet -> playerCode.equals(cardSet.getWinnerPlayerCode())).count());
        playerGamePanelDTO.setAdminPlayerNumericCode((short) 1);
        return playerGamePanelDTO;
    }
}
