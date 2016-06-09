# Need More Cookies

This repository stored the JAVA code for the android application "Needs More Cookies". 

The application can be used to create shopping lists and share them with other users. All the communication between clients and server are encrypted with using HTTPS protocol.

The application can be separated in three main layers: UI logic, business logic and low-level logic. 

The following classes are part of the UI logic layer:

- LogInActivity : This activity could be in both UI and business logic. The aim of this activity is to log the user in the system.
- MainActivity: This is the main activity of the application, it controls most of the UI and business logic of the app. Its jobs consists of controlling the main UI as well as to work with the lower layers of the system
- Items: The Items class works more or less the same as the MainActivity class, but it does not do as much work as it.
- MyRecyclerAdapter : The aim of this class is to carry all the logic of the Recycler adapter UI element, as well as the card view elements.
- ItemRecyclerAdapter : Same aim as the class before but for the items UI.

The following classes are part of the Business logic:

- User_Info : This class stores data about the current user session.
- Shopping_List : This class contains the Shopping Lists objects.
- Item: This class contains the Items objects.
- DB_Helper: This class acts as a High-Level interface for the SQL database

The following classes are part of the low-level logic:
- SQLiteDB: This class interacts with the SQL database.
- Update_Android: This class contains a service which is used to establish communication with the server in order to update the contents of the application.
- Update_Server: This class contains a service which is userd to send updated information to the server.


All the other activities that are not mentioned are not the main part of the application (which does not mean that they are not important) 

