# [Minesweeper Game](https://github.com/KingJulius/Minesweeper) 
- Built using Java and SQL.
- Uses several concepts such Server/Client Networking, Databases (Sqlite3 used), Threads and GUI.
- Functionality: Has a similar gameplay logic as the actual Minewsweeper Game. It also has the ability to start a new instance of a game, to save an instance to the database via a server-client model, opening a saved instance of the game from the database and loading the top five higscores. 

Instructions:

Setup if Eclipse has not been configured to connect to the Database

In Eclipse, Start or Load a Project, Select Project, Select Properties, Select Java Build Path, Select Libraries, Click Modulepath, Add External JARs..., And add the location of sqlite-jdbc-3.30.1.jar in order to add the driver(given). This in turn helps in connecting the SQLite Database to the Java Application. 
