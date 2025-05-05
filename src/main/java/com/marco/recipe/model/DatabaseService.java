package com.marco.recipe.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Diese Klasse kümmert sich um alle Datenbankoperationen für die
 * Rezeptverwaltung.
 * Sie verwendet JDBC, um mit einer MySQL-Datenbank zu kommunizieren.
 * Stellt Methoden zum Erstellen, Lesen, Aktualisieren und Löschen (CRUD)
 * von Rezepten und deren Zutaten bereit.
 *
 * @version 1.1
 * @author Mohammed, Cheyenne
 */
@SuppressWarnings("All")
public class DatabaseService {

    // --- Konstanten für SQL-Befehle ---
    // Recipes Tabelle
    private static final String SELECT_ALL_RECIPES_SHORT = "SELECT recipe_id, title FROM Recipes ORDER BY title ASC";
    private static final String SELECT_RECIPE_BY_ID = "SELECT * FROM Recipes WHERE recipe_id = ?";
    private static final String INSERT_RECIPE = "INSERT INTO Recipes (title, instructions, preparation_time_minutes, difficulty, notes) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_RECIPE = "UPDATE Recipes SET title = ?, instructions = ?, preparation_time_minutes = ?, difficulty = ?, notes = ? WHERE recipe_id = ?";
    private static final String DELETE_RECIPE = "DELETE FROM Recipes WHERE recipe_id = ?";
    private static final String SEARCH_RECIPES_BY_TITLE = "SELECT recipe_id, title FROM Recipes WHERE title LIKE ? ORDER BY title ASC";

    // Ingredients Tabelle
    private static final String SELECT_INGREDIENTS_BY_RECIPE_ID = "SELECT * FROM Ingredients WHERE recipe_id = ?";
    private static final String INSERT_INGREDIENT = "INSERT INTO Ingredients (recipe_id, name, quantity, unit) VALUES (?, ?, ?, ?)";
    private static final String DELETE_INGREDIENTS_BY_RECIPE_ID = "DELETE FROM Ingredients WHERE recipe_id = ?";

    // --- Datenbankverbindungsdetails ---
    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;
    private Connection connection; // Die aktive Datenbankverbindung

    /**
     * Konstruktor für den DatabaseService.
     * Initialisiert die Verbindungsdetails. Die Verbindung selbst wird
     * erst bei Bedarf über die connect()-Methode aufgebaut.
     *
     * @param dbUrl      Die JDBC-URL für die MySQL-Datenbank (z.B.
     *                   "jdbc:mysql://localhost:3300/rezeptdb").
     * @param dbUser     Der Benutzername für die Datenbankverbindung.
     * @param dbPassword Das Passwort für die Datenbankverbindung (Kann "" sein).
     */
    public DatabaseService(String dbUrl, String dbUser, String dbPassword) {
        // Hinweis: In einer realen Anwendung sollten diese Details sicher
        // aus einer Konfigurationsdatei geladen werden, nicht fest codiert!
        this.dbUrl = Objects.requireNonNull(dbUrl, "Datenbank-URL darf nicht null sein.");
        this.dbUser = Objects.requireNonNull(dbUser, "Datenbank-Benutzer darf nicht null sein.");
        this.dbPassword = Objects.requireNonNull(dbPassword, "Datenbank-Passwort darf nicht null sein.");

        // Lade den MySQL JDBC Treiber (optional bei modernen JDBC-Versionen, aber
        // sicherheitshalber)
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Treiber nicht gefunden! Stellen Sie sicher, dass er im Classpath ist.");
            // In einer GUI-Anwendung würde man hier einen Alert anzeigen
            throw new RuntimeException("JDBC Treiber Fehler", e);
        }
    }

    // --- Verbindungsmanagement ---
    /**
     * Baut eine Verbindung zur Datenbank auf, falls noch keine besteht.
     *
     * @throws SQLException Wenn ein Fehler beim Verbindungsaufbau auftritt.
     */
    public void connect() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
                connection.setAutoCommit(true); // Standardmäßig Auto-Commit aktivieren
                System.out.println("Datenbankverbindung erfolgreich hergestellt."); // Für Debugging
            } catch (SQLException e) {
                System.err.println("Fehler beim Herstellen der Datenbankverbindung: " + e.getMessage());
                throw e; // Fehler weitergeben
            }
        }
    }

    /**
     * Schließt die Datenbankverbindung, falls sie geöffnet ist.
     * Sollte am Ende der Anwendungsnutzung aufgerufen werden.
     */
    public void disconnect() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    System.out.println("Datenbankverbindung geschlossen."); // Für Debugging
                }
            } catch (SQLException e) {
                System.err.println("Fehler beim Schließen der Datenbankverbindung: " + e.getMessage());
                // Fehler loggen oder anzeigen
            } finally {
                connection = null; // Verbindungsobjekt zurücksetzen
            }
        }
    }

    /**
     * Prüft, ob die Verbindung zur Datenbank aktiv ist.
     * Versucht bei Bedarf, die Verbindung herzustellen.
     *
     * @return true, wenn die Verbindung aktiv ist, sonst false.
     */
    private boolean ensureConnection() {
        try {
            connect(); // Versucht zu verbinden, falls nötig
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    // --- CRUD-Operationen für Rezepte und Zutaten ---

    /**
     * Ruft eine Liste aller Rezepte ab, enthält aber nur deren ID und Titel.
     * Nützlich für die Anzeige in einer Übersichtsliste.
     *
     * @return Eine Liste von Recipe-Objekten (nur mit ID und Titel befüllt).
     *         Gibt eine leere Liste zurück, wenn keine Rezepte vorhanden sind oder
     *         ein Fehler auftritt.
     */
    public List<Recipe> getAllRecipesShortInfo() {
        List<Recipe> recipes = new ArrayList<>();
        if (!ensureConnection()) {
            System.err.println("Keine Datenbankverbindung für getAllRecipesShortInfo.");
            return recipes; // Leere Liste zurückgeben
        }

        // Try-with-resources schließt Statement und ResultSet automatisch
        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(SELECT_ALL_RECIPES_SHORT)) {

            while (rs.next()) {
                Recipe recipe = new Recipe();
                recipe.setId(rs.getInt("recipe_id"));
                recipe.setTitle(rs.getString("title"));
                // Die anderen Felder bleiben leer/null im Recipe-Objekt
                recipes.add(recipe);
            }
        } catch (SQLException e) {
            System.err.println("SQL Fehler beim Abrufen der Rezeptliste: " + e.getMessage());
        }
        return recipes;
    }

    /**
     * Ruft alle Details für ein einzelnes Rezept anhand seiner ID ab,
     * einschließlich der zugehörigen Zutaten.
     *
     * @param recipeId Die ID des gesuchten Rezepts.
     * @return Ein Optional<Recipe>, das das gefundene Rezept enthält,
     *         oder Optional.empty(), wenn kein Rezept mit dieser ID gefunden wurde
     *         oder ein Fehler auftrat.
     */
    public Optional<Recipe> getRecipeDetails(int recipeId) {
        if (!ensureConnection()) {
            System.err.println("Keine Datenbankverbindung für getRecipeDetails.");
            return Optional.empty();
        }

        Recipe recipe = null;

        // 1. Rezeptdetails abrufen
        try (PreparedStatement pstmtRecipe = connection.prepareStatement(SELECT_RECIPE_BY_ID)) {
            pstmtRecipe.setInt(1, recipeId);
            try (ResultSet rsRecipe = pstmtRecipe.executeQuery()) {
                if (rsRecipe.next()) {
                    recipe = new Recipe();
                    recipe.setId(rsRecipe.getInt("recipe_id"));
                    recipe.setTitle(rsRecipe.getString("title"));
                    recipe.setInstructions(rsRecipe.getString("instructions"));
                    // Behandlung von Integer (kann null sein)
                    int time = rsRecipe.getInt("preparation_time_minutes");
                    recipe.setPreparationTimeMinutes(rsRecipe.wasNull() ? null : time);
                    recipe.setDifficulty(rsRecipe.getString("difficulty"));
                    recipe.setNotes(rsRecipe.getString("notes"));
                    // Initialisiere leere Zutatenliste, wird später gefüllt
                    recipe.setIngredients(new ArrayList<>());
                } else {
                    // Kein Rezept mit dieser ID gefunden
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Fehler beim Abrufen der Rezeptdetails für ID " + recipeId + ": " + e.getMessage());
            return Optional.empty(); // Fehler -> leeres Optional
        }

        // 2. Zutaten für das gefundene Rezept abrufen (nur wenn Rezept gefunden wurde)
        if (recipe != null) {
            try (PreparedStatement pstmtIngredients = connection.prepareStatement(SELECT_INGREDIENTS_BY_RECIPE_ID)) {
                pstmtIngredients.setInt(1, recipeId);
                try (ResultSet rsIngredients = pstmtIngredients.executeQuery()) {
                    while (rsIngredients.next()) {
                        Ingredient ingredient = new Ingredient();
                        ingredient.setId(rsIngredients.getInt("ingredient_id"));
                        ingredient.setRecipeId(rsIngredients.getInt("recipe_id")); // redundant, aber vollständig
                        ingredient.setName(rsIngredients.getString("name"));
                        ingredient.setQuantity(rsIngredients.getString("quantity"));
                        ingredient.setUnit(rsIngredients.getString("unit"));
                        recipe.addIngredient(ingredient); // Füge Zutat zum Rezeptobjekt hinzu
                    }
                }
            } catch (SQLException e) {
                System.err.println(
                        "SQL Fehler beim Abrufen der Zutaten für Rezept ID " + recipeId + ": " + e.getMessage());
                // Rezept wurde gefunden, aber Zutaten konnten nicht geladen werden.
                // Gebe das Rezept trotzdem zurück, aber mit potenziell leerer Zutatenliste.
                // Alternativ könnte man auch hier Optional.empty() zurückgeben.
            }
        }

        return Optional.ofNullable(recipe);
    }

    /**
     * Speichert ein neues Rezept und dessen Zutaten in der Datenbank.
     * Verwendet eine Transaktion, um sicherzustellen, dass entweder alles
     * oder nichts gespeichert wird.
     *
     * @param recipe Das zu speichernde Recipe-Objekt. Die ID des Objekts wird
     *               nach erfolgreichem Speichern gesetzt. Die IDs der Zutaten
     *               werden ignoriert.
     * @return true, wenn das Speichern erfolgreich war, sonst false.
     */
    public boolean saveRecipe(Recipe recipe) {
        if (!ensureConnection() || recipe == null) {
            System.err.println("Keine DB-Verbindung oder ungültiges Rezept zum Speichern.");
            return false;
        }

        boolean success = false;
        try {
            // 1. Transaktion starten
            connection.setAutoCommit(false);

            // 2. Rezept speichern und generierte ID holen
            int generatedRecipeId = -1;
            try (PreparedStatement pstmtRecipe = connection.prepareStatement(INSERT_RECIPE,
                    Statement.RETURN_GENERATED_KEYS)) {
                pstmtRecipe.setString(1, recipe.getTitle());
                pstmtRecipe.setString(2, recipe.getInstructions());
                // Behandlung von Integer (kann null sein)
                if (recipe.getPreparationTimeMinutes() != null) {
                    pstmtRecipe.setInt(3, recipe.getPreparationTimeMinutes());
                } else {
                    pstmtRecipe.setNull(3, Types.INTEGER);
                }
                pstmtRecipe.setString(4, recipe.getDifficulty());
                pstmtRecipe.setString(5, recipe.getNotes());

                int affectedRows = pstmtRecipe.executeUpdate();

                if (affectedRows == 0) {
                    throw new SQLException("Rezept konnte nicht eingefügt werden, keine Zeilen betroffen.");
                }

                // Generierte ID abrufen
                try (ResultSet generatedKeys = pstmtRecipe.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        generatedRecipeId = generatedKeys.getInt(1);
                        recipe.setId(generatedRecipeId); // Setze die ID im übergebenen Objekt!
                    } else {
                        throw new SQLException("Rezept eingefügt, aber keine ID erhalten.");
                    }
                }
            }

            // 3. Zutaten speichern (nur wenn Rezept-ID gültig ist)
            if (generatedRecipeId > 0 && recipe.getIngredients() != null && !recipe.getIngredients().isEmpty()) {
                try (PreparedStatement pstmtIngredient = connection.prepareStatement(INSERT_INGREDIENT)) {
                    for (Ingredient ingredient : recipe.getIngredients()) {
                        pstmtIngredient.setInt(1, generatedRecipeId); // Verwende generierte Rezept-ID
                        pstmtIngredient.setString(2, ingredient.getName());
                        pstmtIngredient.setString(3, ingredient.getQuantity());
                        pstmtIngredient.setString(4, ingredient.getUnit());
                        pstmtIngredient.addBatch(); // Füge zur Batch-Verarbeitung hinzu
                    }
                    pstmtIngredient.executeBatch(); // Führe alle Inserts aus
                }
            }

            // 4. Transaktion bestätigen (Commit)
            connection.commit();
            success = true;
            System.out.println("Rezept erfolgreich gespeichert (ID: " + generatedRecipeId + ")");

        } catch (SQLException e) {
            System.err.println("SQL Fehler beim Speichern des Rezepts: " + e.getMessage());
            // Bei Fehler: Transaktion zurückrollen
            try {
                if (connection != null) {
                    System.err.println("Rollback wird ausgeführt...");
                    connection.rollback();
                }
            } catch (SQLException ex) {
                System.err.println("Fehler beim Rollback: " + ex.getMessage());
            }
            success = false;
        } finally {
            // Auto-Commit wieder aktivieren (wichtig!)
            try {
                if (connection != null) {
                    connection.setAutoCommit(true);
                }
            } catch (SQLException e) {
                System.err.println("Fehler beim Reaktivieren von AutoCommit: " + e.getMessage());
            }
        }
        return success;
    }

    /**
     * Aktualisiert ein vorhandenes Rezept und dessen Zutaten in der Datenbank.
     * Verwendet eine Transaktion. Erst werden alte Zutaten gelöscht,
     * dann das Rezept aktualisiert und schließlich die neuen Zutaten eingefügt.
     *
     * @param recipe Das zu aktualisierende Recipe-Objekt. Muss eine gültige ID
     *               haben.
     * @return true, wenn die Aktualisierung erfolgreich war, sonst false.
     */
    public boolean updateRecipe(Recipe recipe) {
        if (!ensureConnection() || recipe == null || recipe.getId() <= 0) {
            System.err.println("Keine DB-Verbindung, ungültiges Rezept oder fehlende ID zum Aktualisieren.");
            return false;
        }

        boolean success = false;
        try {
            // 1. Transaktion starten
            connection.setAutoCommit(false);

            // 2. Alte Zutaten löschen
            try (PreparedStatement pstmtDeleteIngredients = connection
                    .prepareStatement(DELETE_INGREDIENTS_BY_RECIPE_ID)) {
                pstmtDeleteIngredients.setInt(1, recipe.getId());
                pstmtDeleteIngredients.executeUpdate();
                // Kein Fehler, wenn keine Zutaten vorhanden waren
            }

            // 3. Rezeptdaten aktualisieren
            try (PreparedStatement pstmtUpdateRecipe = connection.prepareStatement(UPDATE_RECIPE)) {
                pstmtUpdateRecipe.setString(1, recipe.getTitle());
                pstmtUpdateRecipe.setString(2, recipe.getInstructions());
                if (recipe.getPreparationTimeMinutes() != null) {
                    pstmtUpdateRecipe.setInt(3, recipe.getPreparationTimeMinutes());
                } else {
                    pstmtUpdateRecipe.setNull(3, Types.INTEGER);
                }
                pstmtUpdateRecipe.setString(4, recipe.getDifficulty());
                pstmtUpdateRecipe.setString(5, recipe.getNotes());
                pstmtUpdateRecipe.setInt(6, recipe.getId()); // WHERE-Bedingung

                int affectedRows = pstmtUpdateRecipe.executeUpdate();
                if (affectedRows == 0) {
                    // Wichtig: Wenn das Rezept nicht existiert, schlägt das Update fehl.
                    // Wir könnten hier einen Fehler werfen oder einfach false zurückgeben.
                    throw new SQLException(
                            "Rezept mit ID " + recipe.getId() + " konnte nicht gefunden oder aktualisiert werden.");
                }
            }

            // 4. Neue Zutaten einfügen (falls vorhanden)
            if (recipe.getIngredients() != null && !recipe.getIngredients().isEmpty()) {
                try (PreparedStatement pstmtIngredient = connection.prepareStatement(INSERT_INGREDIENT)) {
                    for (Ingredient ingredient : recipe.getIngredients()) {
                        pstmtIngredient.setInt(1, recipe.getId()); // Verwende die existierende Rezept-ID
                        pstmtIngredient.setString(2, ingredient.getName());
                        pstmtIngredient.setString(3, ingredient.getQuantity());
                        pstmtIngredient.setString(4, ingredient.getUnit());
                        pstmtIngredient.addBatch();
                    }
                    pstmtIngredient.executeBatch();
                }
            }

            // 5. Transaktion bestätigen (Commit)
            connection.commit();
            success = true;
            System.out.println("Rezept erfolgreich aktualisiert (ID: " + recipe.getId() + ")");

        } catch (SQLException e) {
            System.err.println(
                    "SQL Fehler beim Aktualisieren des Rezepts (ID: " + recipe.getId() + "): " + e.getMessage());
            // Bei Fehler: Transaktion zurückrollen
            try {
                if (connection != null) {
                    System.err.println("Rollback wird ausgeführt...");
                    connection.rollback();
                }
            } catch (SQLException ex) {
                System.err.println("Fehler beim Rollback: " + ex.getMessage());
            }
            success = false;
        } finally {
            // Auto-Commit wieder aktivieren
            try {
                if (connection != null) {
                    connection.setAutoCommit(true);
                }
            } catch (SQLException e) {
                System.err.println("Fehler beim Reaktivieren von AutoCommit: " + e.getMessage());
            }
        }
        return success;
    }

    /**
     * Löscht ein Rezept und dessen zugehörige Zutaten aus der Datenbank.
     * Die Zutaten werden dank "ON DELETE CASCADE" in der Datenbank automatisch
     * mitgelöscht.
     *
     * @param recipeId Die ID des zu löschenden Rezepts.
     * @return true, wenn das Löschen erfolgreich war (mindestens eine Zeile
     *         betroffen), sonst false.
     */
    public boolean deleteRecipe(int recipeId) {
        if (!ensureConnection() || recipeId <= 0) {
            System.err.println("Keine DB-Verbindung oder ungültige ID zum Löschen.");
            return false;
        }

        try (PreparedStatement pstmt = connection.prepareStatement(DELETE_RECIPE)) {
            pstmt.setInt(1, recipeId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Rezept erfolgreich gelöscht (ID: " + recipeId + ")");
                return true;
            } else {
                System.out.println("Kein Rezept zum Löschen gefunden (ID: " + recipeId + ")");
                return false; // Kein Rezept mit dieser ID gefunden
            }
        } catch (SQLException e) {
            System.err.println("SQL Fehler beim Löschen des Rezepts (ID: " + recipeId + "): " + e.getMessage());
            return false;
        }
    }

    /**
     * Sucht nach Rezepten, deren Titel den angegebenen Suchbegriff enthält
     * (Groß-/Kleinschreibung ignorierend).
     * Gibt eine Liste von Rezepten zurück, die nur ID und Titel enthalten.
     *
     * @param query Der Suchbegriff.
     * @return Eine Liste passender Recipe-Objekte (nur mit ID und Titel).
     *         Gibt eine leere Liste zurück, wenn nichts gefunden wird oder ein
     *         Fehler auftritt.
     */
    public List<Recipe> searchRecipes(String query) {
        List<Recipe> recipes = new ArrayList<>();
        if (!ensureConnection() || query == null) {
            System.err.println("Keine DB-Verbindung oder ungültige Suchanfrage.");
            return recipes;
        }

        // Try-with-resources für PreparedStatement und ResultSet
        try (PreparedStatement pstmt = connection.prepareStatement(SEARCH_RECIPES_BY_TITLE)) {
            pstmt.setString(1, "%" + query + "%"); // Füge Wildcards hinzu für Teilstring-Suche
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Recipe recipe = new Recipe();
                    recipe.setId(rs.getInt("recipe_id"));
                    recipe.setTitle(rs.getString("title"));
                    recipes.add(recipe);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Fehler bei der Rezeptsuche nach '" + query + "': " + e.getMessage());
        }
        return recipes;
    }

}