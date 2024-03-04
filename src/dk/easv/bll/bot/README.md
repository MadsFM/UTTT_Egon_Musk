# Bots should go here
There are some examples on how to create simple bots:
 - RandomBot.java
   This bot gets a list of valid moves and plays totally random. Good for testing against.
   
 - DrunkenBot.java
   Drunken bot is a subclass of LocalPrioritisedListBot. 
   It plays the corners first, then middle edges and lastly middle. This is the worst strategy in ordinary tic-tac-toe.
   However when playing against its superclass it wins every time, but loses to random.
   
 - LocalPrioritisedListBot.java
   Holds an array of preferres moves that are sorted by best move first. Tries middle first, then middle edge, and lastly corners. Wins confidently against random.
   
 - PrioListOnSteroids.java
   Is a subclass of LocalPrioritisedListBot.
   It plays as its superclass with the exception that it checks wether there is a winning move. If it has the chance to win, it will take that. It wins against all other
   versions of the prio list bots.
   
 - ExampleSneakyBot.java
   This bot is a starter bot for doing more serious AI.
   It holds methods for simulating a game.
   In its current state it simple plays a game against a random bot if the result of the game is a win, it goes for that play. This is not a very good strategy, however
   it can easily be extended to be more powerful.
