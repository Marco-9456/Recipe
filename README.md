# Recipe Management App (School Project)

Hey! This is a JavaFX application I built for a school project. It's a desktop app that helps you keep track of your recipes. You can add new ones, look up old ones, change them, or delete them. It saves everything in a MySQL database.

---

## What it Can Do 🍳

* **See All Recipes**: Shows a list of all the recipes I've added.
* **Recipe Details**: When I click on a recipe, I can see:
    * Its name (Title)
    * How long it takes to make (Preparation Time in minutes)
    * How hard it is (Difficulty: Einfach, Mittel, Schwer)
    * All the ingredients (with amounts and units like "g" or "ml")
    * The steps to cook it (Instructions)
    * Any extra notes I've made
* **Add New Recipes**: There's a pop-up window to type in all the info for a new recipe.
* **Edit Recipes**: I can change anything about a recipe I've already saved.
* **Delete Recipes**: If I don't want a recipe anymore, I can remove it.
* **Search**: I can type in the title of a recipe to find it quickly.
* **Manage Ingredients**: While adding or editing a recipe, I can add, change, or remove ingredients easily.
* **Change Themes**: I added a few different looks for the app (like Dracula, Nord, Cupertino, etc.) because why not?
* **Keeps Working Smoothly**: I tried to make sure the app doesn't freeze when it's loading or saving stuff from the database.
* **Database Link**: It uses a MySQL database to keep all the recipe info safe.

---

## How I Organized The Code 🗂️

I tried to follow the MVC (Model-View-Controller) pattern, which is a common way to organize code:

* **`com.marco.recipe` (Main Folder)**
    * `Launcher.java`: This is the class that actually starts the app.
    * `MainApp.java`: This is the main JavaFX class that sets up the window, database connection, and gets everything going.
* **`com.marco.recipe.model` (The "Brain" - Data Stuff)**
    * `Recipe.java`: This file describes what a "Recipe" is (title, instructions, ingredients, etc.).
    * `Ingredient.java`: This describes what an "Ingredient" is (name, how much, unit).
    * `DatabaseService.java`: This class does all the talking to the MySQL database – saving, loading, updating, and deleting recipes and ingredients.
* **`com.marco.recipe.view` (The "Look" - What I See)**
    * `MainView.java`: This creates the main window of the app – the list of recipes, the area where details show up, the search bar, and the buttons.
    * `RecipeEditDialog.java`: This is the pop-up window I use for adding new recipes or changing old ones. It also has the section for adding/removing ingredients.
* **`com.marco.recipe.controller` (The "Manager" - Connects View and Model)**
    * `MainController.java`: This class is like the manager. It listens for when I click buttons or select things in the list, tells the `DatabaseService` what to do, and then tells the `MainView` to update what I see.
* **Other Important Files:**
    * `pom.xml`: This is a Maven file. It lists all the external code libraries I used and how to build the project.
    * `module-info.java`: This is a Java thing for organizing code into modules.

---

## Getting it Running 🛠️

If you want to run this:

1.  **MySQL Database**:
    * You need a MySQL server running.
    * You'll need to create a database. I called mine `rezeptdb`.
    * Inside that database, I need two tables:
        * `Recipes` table with columns: `recipe_id` (INT, Primary Key, Auto Increment), `title` (VARCHAR), `instructions` (TEXT), `preparation_time_minutes` (INT, can be empty), `difficulty` (VARCHAR, can be empty), `notes` (TEXT, can be empty).
        * `Ingredients` table with columns: `ingredient_id` (INT, Primary Key, Auto Increment), `recipe_id` (INT, links to Recipes.recipe\_id, and if a recipe is deleted, its ingredients are too), `name` (VARCHAR), `quantity` (VARCHAR, can be empty), `unit` (VARCHAR, can be empty).
2.  **Database Connection Info**:
    * I put the database connection details in `MainApp.java`.
        ```java
        String dbUrl = "jdbc:mysql://localhost:3300/rezeptdb"; // Change this if your DB is somewhere else or named differently
        String dbUser = "root"; // Change this if you use a different username
        String dbPassword = ""; // Change this if you have a password for your DB user
        ```
      You'll probably need to change these to match your MySQL setup.
3.  **JDBC Driver**: The app uses the MySQL JDBC driver to talk to the database. It's loaded in `DatabaseService.java`.

---

## How to Start the App ▶️

1.  Make sure you have Java (I used JDK 11) and Maven.
2.  Set up the MySQL database like I explained above.
3.  Change the database username/password in `MainApp.java` if you need to.
4.  Open the project in an IDE like IntelliJ IDEA or Eclipse.
5.  Build it with Maven.
6.  Then you can run the `Launcher.java` or `MainApp.java` class to start it.

---

## Libraries I Used 📚

I used some external libraries to help build this (they are all listed in the `pom.xml` file):

* **JavaFX**: For all the buttons, lists, and windows.
* **AtlantaFX**: For the cool themes.
* **ControlsFX**: For some extra UI bits.
* **Ikonli**: For the icons on the buttons.
* **MySQL Connector/J**: To connect to the MySQL database.

---

That's pretty much it! It was a fun project to work on.