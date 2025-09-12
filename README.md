# Introduction
**Pokémon Shiny Tracker** is an Android application that offers Pokémon players an intuitive way to log their shiny hunts across all mainline Pokémon games. Each shiny hunt log can track various details such as the number of encounters, hunting method, game of origin, game of current residence, and starting/completion dates. The app also features various sorting and filtering methods for quick searching through saved hunts and images of every shiny Pokémon (including all forms) for easy reference while shiny hunting.

### Requirements

- Any Android phone or tablet (excluding folding phones) with Android 7.0 (API level 24) or higher*.
- No internet connection is required (everything is stored locally with an SQLite database).

\*Some views utilize the 'clipToOutline' property, which requires Android 12 (API level 31) or higher. The app will still run on any device with Android 7.0 or higher, but versions below Android 12 will simply ignore the 'clipToOutline' property, having minor unwanted effects on the UI.
\*Due to how resource-heavy the Pokémon selection dialog (in the Filter menu and the Create/Edit Hunt page) is, its loading time can vary drastically depending on the device. For example, my current phone (a Google Pixel 8a with Android 16) can load the dialog in 1-2 seconds, but my old phone (an LG Stylo 6 with Android 10 and likely pretty worn-out hardware) takes almost 30 seconds to load it. I've had great difficulty tackling how to reduce the delay. I've decided to leave it as is for release, but I will definitely look further into how to better optimize the dialog (or rework it entirely) in the future.

### Installation Guide
I would've liked to publish the app on the Play Store to simplify installation, but their developer policy requires all publishers to have legal ownership (or permission from the owner) of all intellectual property featured in their apps. Since my app uses official Pokémon assets (e.g., the images used for the shiny Pokémon and the game icons), I cannot publish it on the Play Store. So, I've instead decided to make the APK file for the app available directly in this GitHub repository. Here's a step-by-step guide on how to install the Pokémon Shiny Tracker app on your Android device (an installation guide video will also be provided in the near future):
1. Have this GitHub repository open in your Android device's browser.
2. Navigate to the `app` directory and then the `release` directory.
3. Click the `app-release.apk` file.
4. In the Code/Blame section, click the button on the right with the three dots `...`, then click 'Download'.
5. Your device might say "File might be harmful" or something similar. Click 'Download anyway'.
6. Once `app-release.apk` is downloaded, open it (if your browser doesn't give you the option to open it directly, you may have to navigate to your device's Downloads folder to find the file).
7. Upon opening the file, your device should attempt to install the app, but you may get a message saying "For your security, your phone is not allowed to install unknown apps from this source." Don't be startled by this message. Click 'Settings' and then toggle 'Allow from this source'. Then, navigate back.
8. Your device should now ask if you want to install the app. Click 'Install'. Your device may also offer to scan the app for any viruses. This isn't required, but feel free to let it do so if you wish. You should now have successfully installed the app on your device!

### What is "Shiny Hunting"?
In Pokémon games, "shiny" Pokémon are Pokémon with a color palette different from their usual one. For example, a standard Pikachu is yellow, but a shiny Pikachu is orange. Although the probability of encountering a shiny Pokémon varies based on many in-game factors, the odds of encountering a shiny Pokémon are typically very low. Because of their rarity, many Pokémon players dedicate themselves to building a collection of shiny Pokémon by "shiny hunting". "Shiny hunting" refers to the act of intentionally encountering Pokémon repeatedly until a shiny variant is found.

### Why did I make this app?
Many shiny hunters, such as myself, like to track the number of encounters each of our shiny hunts takes to complete. Previously, I utilized a generic tally app to accomplish this, but it became very limiting. After shiny hunting for a few years across many different Pokémon games, it has become difficult for me to keep track of where all my shiny Pokémon are. I thought it would be handy if I could keep track of more information than just the number of encounters. Additionally, I had been wanting to find a passion project to work on, and I was specifically interested in learning about Android app development. Thus, I decided to create this app as a tool, not just for myself, but for all shiny hunters to log their shiny hunts more conveniently.

# Features
The app features two primary pages: the Home page and the Create/Edit Hunt page. The UI for both of these pages was specifically designed to dynamically resize based on the dimensions of the device and fit on both portrait and landscape orientations.

### Home Page
The Home page displays the user's saved shiny hunts using a RecyclerView. The Home page also has a button bar at the top with various options.

#### Shiny Hunt RecyclerView
Each shiny hunt "item" in the RecyclerView contains an image of the shiny Pokémon, the nickname of the Pokémon (or the species name if a nickname isn't specified), icons representing the games of origin and current residence, and a counter with decrement/increment buttons. The shiny hunt items also have two background gradients: a gray gradient for "in-progress" shiny hunts and a gold gradient for "completed" shiny hunts. The decrement/increment buttons for completed hunts are disabled to prevent the user from accidentally changing the counter value. Additionally, each shiny hunt item can be expanded via long-clicking (i.e., pressing for about 1 second). Expanding a shiny hunt item displays additional buttons for moving the shiny hunt item up or down the RecyclerView (while using the Default sorting method; this will be explained further in a later section) and for editing and deleting the shiny hunt.

#### Button Bar
The button bar at the top of the Home page offers the following options:

- **Counter Multiplier**: This button allows the user to set the global counter multiplier. This multiplier specifies the amount each shiny hunt's counter changes when decrementing/incrementing. This is handy if the shiny hunting method involves multiple encounters at a time (e.g., horde encounters) or if the user is hunting on multiple devices simultaneously.
- **Expand All**: This checkbox can be toggled to expand/unexpand all of the shiny hunt items.
- **New Hunt**: This button opens the Create Hunt page.
- **Sort**: This button opens the Sort menu, where the user can specify how they want to sort the shiny hunts. The available sorting methods are Default*, Start Date, Finish Date, Name (i.e., alphabetical order), and Generation (i.e., National Pokédex number). The user can also specify whether they want to sort in ascending or descending order.
- **Filter**: This button opens the Filter menu, where the user can apply various filters to the shiny hunt RecyclerView. Available filters are completion status (In Progress, Completed, or Both), Pokémon (including distinct forms), origin and current games, method, and ranges for start date, finish date, counter, and phase. The current games and finish date filters only display when Completed or Both is selected as the completion status filter.

\*By default, the Default sorting method follows the order the shiny hunt logs were created. However, it is the only sorting method that allows the user to reorder the shiny hunts by moving them up or down in the RecyclerView. The move up/down buttons in expanded shiny hunt items are disabled if a different sorting method is selected or if any filters are currently being applied.

### Create/Edit Hunt Page
The Create/Edit Hunt page serves as the full detail view of an individual shiny hunt. Like the shiny hunt items on the Home page, the Create/Edit Hunt page uses a gray gradient background for in-progress shiny hunts and a gold gradient background for completed shiny hunts. In Create mode (accessed via clicking the New Hunt button on the Home page), all of the fields are initialized to be empty or set to 0. In Edit mode (accessed via clicking a shiny hunt item's Edit button on the Home page), the fields are automatically filled with the data of the corresponding shiny hunt. The Create/Edit Hunt page has a button bar at the top with the following options:

- **Back**: This button returns the user to the Home page. If unsaved changes are detected, a confirmation dialog will first appear, asking the user if they are okay with returning to the Home page without saving. The same logic also occurs if the user clicks the back button on their device's navigation bar.
- **Save**: This button saves any changes the user made to the shiny hunt entry. If the user was creating a new shiny hunt entry, saving will create a new shiny hunt entry in the database. If the user was editing a preexisting shiny hunt, saving will instead update the existing shiny hunt entry in the database.
- **Delete**: This button displays a confirmation dialog asking the user if they are certain they would like to delete the shiny hunt. Confirming will drop the shiny hunt entry from the database and return the user to the Home page. This button only appears when editing a preexisting shiny hunt.

Additionally, the following fields can be edited and tracked in the Create/Edit Hunt page:

- **Pokémon**: Clicking the pokéball button opens a dialog with a list of every Pokémon. Selecting a Pokémon from this list will close the dialog and display an image of that Pokémon on the Create/Edit Hunt page. The dialog also features a search bar that automatically filters the list as the user types. The list only displays one form per Pokémon, but if the selected Pokémon has multiple forms, left/right arrows will also appear on the Create/Edit Hunt page so the user can select the desired form.
- **Nickname**: If the user nicknames their shiny Pokémon, they can enter the nickname in the "Nickname" EditText field. To accurately reflect the maximum nickname length in Pokémon games, this EditText field has a maximum character length of 12.
- **Start Date**: Clicking the calendar button next to the "Started" label opens a calendar dialog for selecting the start date of the shiny hunt. Selecting a date will display it on the Create/Edit Hunt page with an 'x' button if the user wants to unselect it.
- **Origin Game**: Clicking the controller button next to the "Hunting In" label opens a dialog with a list of every mainline Pokémon game. Selecting a game from this list will display its icon on the Create/Edit Hunt page with a black border. The icon will also have an 'x' button next to it if the user needs to unselect the game.
- **Location**: If the user wants to note the in-game location where the shiny Pokémon is being hunted, they can type it in the "Location" EditText field.
- **Counter**: The "Counter" EditText field is used to track the number of encounters for the shiny hunt. The user can modify the counter value by either typing directly into the EditText field or clicking the decrement/increment buttons. There is also a counter multiplier button for setting the multiplier for the decrement/increment buttons.
- **Phase**: The "Phase" EditText field is used to track the number of phases that occurred during the shiny hunt (a "phase" is a scenario where the hunter finds a shiny that is different from the intended target). Like the counter, the phase value can be modified by either typing directly into the EditText field or clicking the decrement/increment buttons (note: the phase does not have a phase multiplier due to how infrequently the user would need to modify the value).
- **Notes**: The "Notes" EditText field is used for noting any additional details that aren't offered by the app.
- **Completion Status**: The checkbox next to the "Hunt Completed?" label can be toggled to specify whether the shiny hunt is in progress or completed. Toggling the checkbox will automatically update the background gradient with a cross-fade animation. When the checkbox is checked (i.e., the hunt is completed), extra fields will be displayed for the finish date and the current game.
- **Finish Date**: The "Finished" field functions the same as the "Started" field, but it's for selecting the completion date of the shiny hunt.
- **Current Game**: The "Currently In" field functions the same as the "Hunting In" field, but it's for selecting the game the shiny is currently located in. The icon of the selected current game will have a gold border instead of black.
