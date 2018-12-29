
# Halite-3-bot

A bot to play Halite 3, an AI competition hosted by Two Sigma at halite.io.

Included are a couple batch files (**compile.bat** and **run.bat**, which uses **convert.py**) I used during the competition to quickly test my bot. They are configured for my environment specifically, but it shouldn't be too difficult to modify them.

 ### The code ###

The main bot is **MyBot.java**, which simply loops over the turns and the ships and asks other files (mainly **Strategy**) what to do. 

The most interesting code is going to be found in /dhallstr/ where I have files such as:

* **Strategy.java** which is the backbone of my bot
* **Navigation.java** which handles the modified BFS search my bot uses for pathing/mining
* **Magic.java** which handles magic numbers for all the other files to use
* **PlannedLocations.java** which uses a 3D array like-structure (except the third dimension is a **ParallelSlidingList** which is just like an array except that it is designed to work well with holding things relative to time because every turn, elements "slide" down one index. This is done O(1), don't worry)
* **TerrainGoal.java** which tells **Navigation** where is best when mining
* **DropoffGoal.java** which, of course, tells **Navigation** where is best when delivering halite

Files in /hlt/ were more or less provided in the Halite 3 Java starter kit; however, I made major modifications to many of them. Nothing too fancy is going on in here though.

## Strategy ##
I will hold back on writing about the strategy for now because the bot is still in development.