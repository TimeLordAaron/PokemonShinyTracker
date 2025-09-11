# Introduction
**Pokémon Shiny Tracker** is an Android application that offers Pokémon players an intuitive way to log their shiny hunts across all mainline Pokémon games. Each shiny hunt log can track various details such as the number of encounters, hunting method, game of origin, game of current residence, and starting/completion dates. The app also features various sorting and filtering methods for quick searching through saved hunts and images of every shiny Pokémon (including all forms) for easy reference while shiny hunting.

### Requirements

- Any Android phone or tablet (excluding folding phones) with Android 7.0 (API level 24) or higher*.
- No internet connection is required (everything is stored locally with an SQLite database).

\*Some views utilize the 'clipToOutline' property, which requires Android 12 (API level 31) or higher. The app will still run on any device with Android 7.0 or higher, but versions below Android 12 will simply ignore the 'clipToOutline' property, having minor unwanted effects on the UI.

### What is "Shiny Hunting"?
In Pokémon games, "shiny" Pokémon are Pokémon with a color palette different from their usual one. For example, a standard Pikachu is yellow, but a shiny Pikachu is orange. Although the probability of encountering a shiny Pokémon varies based on many in-game factors, the odds of encountering a shiny Pokémon are typically very low. Because of their rarity, many Pokémon players dedicate themselves to building a collection of shiny Pokémon by "shiny hunting". "Shiny hunting" refers to the act of intentionally encountering Pokémon repeatedly until a shiny variant is found.

### Why did I make this app?
Many shiny hunters, such as myself, like to track the number of encounters each of our shiny hunts takes to complete. Previously, I utilized a generic tally app to accomplish this, but it became very limiting. After shiny hunting for a few years across many different Pokémon games, it has become difficult for me to keep track of where all my shiny Pokémon are. I thought it would be handy if I could keep track of more information than just the number of encounters. Additionally, I had been wanting to find a passion project to work on, and I was specifically interested in learning about Android app development. Thus, I decided to create this app as a tool, not just for myself, but for all shiny hunters to log their shiny hunts more conveniently.

# Features
The app features two primary pages: the Home page and the Create/Edit Hunt page. The UI for both of these pages was specifically designed to dynamically resize based on the dimensions of the device and fit on both portrait and landscape orientations.

### Home Page
The Home page displays the user's saved shiny hunts using a RecyclerView. The Home page also has a button bar at the top with various options.

#### Shiny Hunt RecyclerView
Each shiny hunt "item" in the RecyclerView contains an image of the shiny Pokémon, the nickname of the Pokémon (or the species name if a nickname isn't specified), icons representing the games of origin and current residence, and a counter with decrement/increment buttons. The shiny hunt items also have two background gradients: a gray gradient for "in progress" shiny hunts and a gold gradient for "completed" shiny hunts. The decrement/increment buttons for completed hunts are disabled to prevent the user from accidentally changing the counter value. Additionally, each shiny hunt item can be expanded via long-clicking (i.e., pressing for about 1 second). Expanding a shiny hunt item displays additional buttons for moving the shiny hunt item up or down the RecyclerView (while using the Default sorting method; this will be explained further in a later section) and for editing and deleting the shiny hunt.

#### Button Bar
The button bar at the top of the Home page offers the following options:

- **Counter Multiplier**: This button allows the user to set the global counter multiplier. This multiplier specifies the amount each shiny hunt's counter changes when decrementing/incrementing. This is handy if the shiny hunting method involves multiple encounters at a time (e.g., horde encounters) or if the user is hunting on multiple devices simultaneously.
- **Expand All**: This checkbox can be toggled to expand/unexpand all of the shiny hunt items.
- **New Hunt**: This button opens the Create Hunt page.
- **Sort**: This button opens the Sort menu, where the user can specify how they want to sort the shiny hunts. The available sorting methods are Default*, Start Date, Finish Date, Name (i.e., alphabetical order), and Generation (i.e., National Pokédex number). The user can also specify whether they want to sort in ascending or descending order.
- **Filter**: This button opens the Filter menu, where the user can apply various filters to the shiny hunt RecyclerView. Available filters are completion status (In Progress, Completed, or Both), Pokémon (including distinct forms), origin and current games, method, and ranges for start date, finish date, counter, and phase. The current games and finish date filters only display when Completed or Both is selected as the completion status filter.

\*By default, the Default sorting method follows the order the shiny hunt logs were created. However, it is the only sorting method that allows the user to reorder the shiny hunts by moving them up or down in the RecyclerView. The move up/down buttons in expanded shiny hunt items are disabled if a different sorting method is selected or if any filters are currently being applied.
