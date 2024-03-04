# UTTT-For-Students
This is an implementation of the Ultimate Tic Tac Toe game. The game is written in Java using JavaFX, FontAwesoneFX by Jens Deters and JFoenix.

![Screenshot of gameplay](/UTTT%20game%20screenshot.png)

## Game rules
Here is a nice explanation of the rules of the game https://www.thegamegal.com/2018/09/01/ultimate-tic-tac-toe/

## Bots
The game can be played as either human or bot, and any combination can be used. human-human, human-bot, bot-human or bot-bot.

The bots must follow the IBot interface in the BLL package. The game uses reflection for loading class files from the Bots folder under BLL. So to implement your own bot, you simple create a bot that extends IBot and put the files in the BLL folder.

When the game starts it creates a list of the bot names in the project root folder. This is for usage with online tournament tools.

The bots provided with the game are very simple and you can probably beat them relatively easy, however they show the basic idea behind the IBot interface and how to interact with the game state. 

## Simulation
The game can also simulate games. This way if you choose bot vs bot, you can simulate many games as fast as you processor allows it and this way you can check if your AI/bot is better than other bots.

## YouTube on setup in IntelliJ
https://www.youtube.com/watch?v=WU1eJXllIgU
