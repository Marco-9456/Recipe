package com.marco.recipe.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Repräsentiert ein Kochrezept mit Titel, Anleitung, Zutaten und weiteren
 * Details.
 *
 * @version 1.1
 * @author Mohammed
 */
@SuppressWarnings("All")
public class Recipe {

    // --- Attribute ---

    /** Eindeutige ID des Rezepts (aus der Datenbank). */
    private int id;

    /** Titel des Rezepts (z.B. "Apfelkuchen"). */
    private String title;

    /** Zubereitungsanleitung als Text. */
    private String instructions;

    /** Zubereitungszeit in Minuten. Kann null sein, wenn nicht angegeben. */
    private Integer preparationTimeMinutes; // Integer statt int, um null zu erlauben

    /** Schwierigkeitsgrad (z.B. "Einfach", "Mittel", "Schwer"). */
    private String difficulty;

    /** Optionale Notizen zum Rezept. */
    private String notes;

    /** Liste der Zutaten für dieses Rezept. Wird als ArrayList initialisiert. */
    private List<Ingredient> ingredients;

    // --- Konstruktoren ---

    /**
     * Standardkonstruktor. Initialisiert die Zutatenliste als leere ArrayList
     * und Strings als leere Strings.
     */
    public Recipe() {
        this.title = "";
        this.instructions = "";
        this.difficulty = "";
        this.notes = "";
        this.ingredients = new ArrayList<>(); // Wichtig: Liste immer initialisieren!
    }

    /**
     * Konstruktor zum Erstellen eines neuen Rezepts (ohne ID).
     *
     * @param title                  Der Titel des Rezepts. Darf nicht null sein.
     * @param instructions           Die Zubereitungsanleitung. Kann null sein.
     * @param preparationTimeMinutes Die Zubereitungszeit in Minuten. Kann null
     *                               sein.
     * @param difficulty             Der Schwierigkeitsgrad. Kann null sein.
     * @param notes                  Optionale Notizen. Kann null sein.
     */
    public Recipe(String title, String instructions, Integer preparationTimeMinutes, String difficulty, String notes) {
        this(); // Ruft Standardkonstruktor auf (initialisiert Liste und leere Strings)
        this.title = Objects.requireNonNull(title, "Titel darf nicht null sein.");
        this.instructions = instructions != null ? instructions : "";
        this.preparationTimeMinutes = preparationTimeMinutes; // null ist erlaubt
        this.difficulty = difficulty != null ? difficulty : "";
        this.notes = notes != null ? notes : "";
    }

    /**
     * Vollständiger Konstruktor zum Erstellen eines Rezept-Objekts mit allen Daten
     * (z.B. beim Laden aus der Datenbank).
     *
     * @param id                     Die Datenbank-ID des Rezepts.
     * @param title                  Der Titel des Rezepts. Darf nicht null sein.
     * @param instructions           Die Zubereitungsanleitung. Kann null sein.
     * @param preparationTimeMinutes Die Zubereitungszeit in Minuten. Kann null
     *                               sein.
     * @param difficulty             Der Schwierigkeitsgrad. Kann null sein.
     * @param notes                  Optionale Notizen. Kann null sein.
     * @param ingredients            Die Liste der Zutaten. Sollte nicht null sein;
     *                               wird ggf. als leere Liste initialisiert.
     */
    public Recipe(int id, String title, String instructions, Integer preparationTimeMinutes, String difficulty,
            String notes, List<Ingredient> ingredients) {
        this(title, instructions, preparationTimeMinutes, difficulty, notes); // Ruft anderen Konstruktor auf
        this.id = id;
        // Stellt sicher, dass die Zutatenliste nicht null ist
        this.ingredients = (ingredients != null) ? ingredients : new ArrayList<>();
    }

    // --- Getter und Setter ---

    /**
     * Gibt die Datenbank-ID des Rezepts zurück.
     * 
     * @return Die ID des Rezepts.
     */
    public int getId() {
        return id;
    }

    /**
     * Setzt die Datenbank-ID des Rezepts.
     * 
     * @param id Die neue ID.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gibt den Titel des Rezepts zurück.
     * 
     * @return Der Titel (nie null).
     */
    public String getTitle() {
        return title;
    }

    /**
     * Setzt den Titel des Rezepts.
     * 
     * @param title Der neue Titel. Darf nicht null sein.
     */
    public void setTitle(String title) {
        this.title = Objects.requireNonNull(title, "Titel darf nicht null sein.");
    }

    /**
     * Gibt die Zubereitungsanleitung zurück.
     * 
     * @return Die Anleitung (kann leer sein, aber nicht null).
     */
    public String getInstructions() {
        return instructions;
    }

    /**
     * Setzt die Zubereitungsanleitung.
     * 
     * @param instructions Die neue Anleitung. Null wird zu leerem String.
     */
    public void setInstructions(String instructions) {
        this.instructions = instructions != null ? instructions : "";
    }

    /**
     * Gibt die Zubereitungszeit in Minuten zurück.
     * 
     * @return Die Zeit in Minuten oder null, wenn nicht gesetzt.
     */
    public Integer getPreparationTimeMinutes() {
        return preparationTimeMinutes;
    }

    /**
     * Setzt die Zubereitungszeit in Minuten.
     * 
     * @param preparationTimeMinutes Die neue Zeit oder null.
     */
    public void setPreparationTimeMinutes(Integer preparationTimeMinutes) {
        this.preparationTimeMinutes = preparationTimeMinutes;
    }

    /**
     * Gibt den Schwierigkeitsgrad zurück.
     * 
     * @return Der Schwierigkeitsgrad (kann leer sein, aber nicht null).
     */
    public String getDifficulty() {
        return difficulty;
    }

    /**
     * Setzt den Schwierigkeitsgrad.
     * 
     * @param difficulty Der neue Schwierigkeitsgrad. Null wird zu leerem String.
     */
    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty != null ? difficulty : "";
    }

    /**
     * Gibt die Notizen zurück.
     * 
     * @return Die Notizen (kann leer sein, aber nicht null).
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Setzt die Notizen.
     * 
     * @param notes Die neuen Notizen. Null wird zu leerem String.
     */
    public void setNotes(String notes) {
        this.notes = notes != null ? notes : "";
    }

    /**
     * Gibt die Liste der Zutaten zurück.
     * Garantiert, dass nie null zurückgegeben wird (mindestens eine leere Liste).
     * 
     * @return Die Liste der Zutaten (List<Ingredient>).
     */
    public List<Ingredient> getIngredients() {
        // Stellt sicher, dass die Liste nie null ist, falls sie extern manipuliert
        // wurde
        if (this.ingredients == null) {
            this.ingredients = new ArrayList<>();
        }
        return ingredients;
    }

    /**
     * Setzt die komplette Liste der Zutaten.
     * 
     * @param ingredients Die neue Liste von Zutaten. Wenn null übergeben wird,
     *                    wird eine leere Liste gesetzt.
     */
    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = (ingredients != null) ? ingredients : new ArrayList<>();
    }

    // --- Hilfsmethoden für Zutaten ---

    /**
     * Fügt eine einzelne Zutat zur Liste hinzu.
     * Stellt sicher, dass die Liste initialisiert ist.
     * 
     * @param ingredient Die hinzuzufügende Zutat. Darf nicht null sein.
     */
    public void addIngredient(Ingredient ingredient) {
        Objects.requireNonNull(ingredient, "Hinzuzufügende Zutat darf nicht null sein.");
        if (this.ingredients == null) {
            this.ingredients = new ArrayList<>();
        }
        // Optional: Setze die recipeId der Zutat, wenn dieses Rezept bereits eine ID
        // hat
        if (this.id > 0) {
            ingredient.setRecipeId(this.id);
        }
        this.ingredients.add(ingredient);
    }

    /**
     * Entfernt eine einzelne Zutat aus der Liste.
     * 
     * @param ingredient Die zu entfernende Zutat.
     * @return true, wenn die Zutat entfernt wurde, sonst false.
     */
    public boolean removeIngredient(Ingredient ingredient) {
        if (this.ingredients != null && ingredient != null) {
            return this.ingredients.remove(ingredient);
        }
        return false;
    }

    // --- Überschriebene Methoden ---

    /**
     * Gibt den Titel des Rezepts zurück.
     * Diese Methode ist nützlich, um Recipe-Objekte direkt
     * in einer JavaFX ListView anzuzeigen, die standardmäßig toString() verwendet.
     *
     * @return Der Titel des Rezepts.
     */
    @Override
    public String toString() {
        return title; // Zeigt den Titel in der ListView an
    }

    /**
     * Vergleicht dieses Rezept mit einem anderen Objekt auf Gleichheit.
     * Zwei Rezepte gelten als gleich, wenn ihre IDs (sofern > 0) übereinstimmen.
     * Falls IDs nicht verglichen werden können, werden Titel und Zutatenliste
     * verglichen.
     *
     * @param o Das Objekt, mit dem verglichen werden soll.
     * @return true, wenn die Objekte als gleich betrachtet werden, sonst false.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Recipe recipe = (Recipe) o;
        // Wenn beide IDs gesetzt sind (>0), vergleiche nur IDs
        if (id > 0 && recipe.id > 0) {
            return id == recipe.id;
        }
        // Ansonsten vergleiche Titel und Inhalt der Zutatenliste
        // (Hinweis: Der Vergleich von Listeninhalten kann aufwändig sein)
        return Objects.equals(title, recipe.title) &&
                Objects.equals(ingredients, recipe.ingredients); // Vergleicht Inhalt der Listen
    }

    /**
     * Erzeugt einen Hashcode für das Rezept.
     * Basiert auf der ID, wenn sie gesetzt ist (>0), ansonsten auf Titel und
     * Zutatenliste.
     *
     * @return Der Hashcode für dieses Objekt.
     */
    @Override
    public int hashCode() {
        // Wenn ID gesetzt, nutze diese für den Hashcode
        if (id > 0) {
            return Objects.hash(id);
        }
        // Ansonsten nutze Titel und Zutatenliste
        return Objects.hash(title, ingredients);
    }
}
