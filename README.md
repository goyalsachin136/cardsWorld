# Cards game backend 
Backend application for multiplayer online cards game built using mysql database and spring boot

You can find the ui code https://github.com/goyalsachin136/CardsGame/

#Read to understand spring boot
 https://spring.io/guides/gs/accessing-data-mysql/#initial for basic spring boot application 

#Architecture

Application starts from AccessingDataMysqlApplication class

This is a mvc application using spring boot

Flow starts from controller(MainController) --> service(service package) --> repository(repository package) 

Using mysql as persistent store


#Tables in mysql

####1) Game

    private Long id; --> id column in database

    private String code; // n digit all small alphabet unique game code

    private Short currentPlayer; // Player who has to move -- numeric code

    private Short trumpCard; // HEARTS(0), DIAMOND(1), SPADE(2), CLUBS(3) --> bracket number represent 
    //card number / 13

    private String trumpSetByPlayerCode; // player code who has set trump

    private Boolean isTrumpOpen; // is trump opened in game 

    private Short numberOfPlayers; // number of players in game

    private Short numberOfCards; // 1 to 52 --> total number of cards in game

    private Boolean canGameBeStarted; // can game be started (For eg. do not start the game till all cards
                                      // are distributed)

    private String gameType; // 508 is a game type, there can be many game types
    
    
####2) Player

    private Integer id; // id column in database

    private Short numericCode; // numeric id assigned to player choose by player during game entry (1 to 4)

    private String gameCode; // game code

    private String code; // player unique code (unique code by which one can get player data)

    private String cardsLeft; // comma separated card numbers --> these number denotes the card left with
                              //a player

    private String nickName; // nick name of player chosed by player during game entry
    
    
####3) Move

    private Long id; // For every game move (card move by a player there is a entry in move table)

    private String gameCode; // game code for which move is done

    private Short card; // card number moved by player

    private String playerCode; // player code who has moved his/her card

    private Boolean isActive; // soft delete flag (if we want to undo a move in future)
    
    
####4) CardSet (// n moves in a round will make a card set which will decide the winner)

    private Long id; 

    private String gameCode; // game code 

    private String moveIds; // comma separated --> Move id (Move table id) in sequential order of moves of player

    private Boolean isCurrentSet; // if this flag is 1 means this is the ongoing round in a game (One game
                                  //can have atmost 1 active card set, During start and after game ends active 
                                  //card set for a game will be 0)

    private String winnerPlayerCode; // every card set has a winner player -- player code who has won the card set

    private Long bestMoveIdTillNow; // using this to find best move of a card set --> we cannot simply 
                                    // compare cards after set is over as if trump is opened between a set 
                                    // and at that time if somebody has moved trump card then that card will not
                                    // be a trump card
    
    
    
    
#How to setup on local machine

1) https://spring.io/guides/gs/accessing-data-mysql
2) install java 8
3) install Gradle 6.3 or higher version
4) install mysql 5.7 or higher

##ide 
Use intellij or atom or any other good ide


##Build  code using
gradle clean build

##Run code using
java -jar  java -jar build/libs/accessing-data-mysql-0.0.1-SNAPSHOT.jar  
