# Server-client for file transfer
File transfer over TCP with calculation of data transfer rate
# How is it work
* The server is passed in parameters the port number on which it will wait for incoming connections from clients.
* The client is passed in parameters a relative or absolute path to the file to be sent. The file name does not exceed 4096 bytes in UTF-8 encoding. The file size is no more than 1 terabyte.
* The client is also passed in the parameters the DNS name (or IP address) and the server port number. я   * The client sends the file name in UTF-8 encoding, the size of the file and its contents to the server. TCP is used for transmission. Think of the transfer protocol yourself (i.e. the programs of different students may be incompatible).
* The server saves the received file to a subdirectory of its current directory. The name of the file is, if possible, the same as the name supplied by the client.
* In the process of receiving data from the client, the server once every 3 seconds displays the instantaneous reception speed and the average speed per session to the console. Speeds are displayed separately for each active client. If the client was active for less than 3 seconds, the speed will still be displayed for him once. The speed here means the number of bytes transmitted per unit of time.
* After successfully saving the entire file, the server checks whether the size of the received data matches the size transmitted by the client, informs the client about the success or failure of the operation, and then closes the connection.
* The client displays a message indicating whether the file transfer was successful.
* All used OS resources are correctly released as soon as they are no longer needed.
* The server can work in parallel with several clients. Immediately after accepting a connection from one client, the server waits for the next clients.
* In case of an error, the server terminates the connection with the client. At the same time, he continues to serve other customers.
# How is it implemented
1. The client sends only one file, the path to which is passed as a parameter from the command line and then disconnects
2. The client does not disconnect itself. Added commands for the client:
   * "quit" - to disconnect
   * "loadToServer" + [path to file] - to load file to server directory from client directory
   * "getServerFilesList" - to get server list of files from server directory
   * "loadFromServer" + [name of file from server list of files] - to load file from server list to client directory
3. JavaFX GUI for client and server
