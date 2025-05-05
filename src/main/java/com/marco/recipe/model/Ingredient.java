package com.marco.recipe.model;

import java.util.Objects;

/**
 * Repräsentiert eine einzelne Zutat eines Rezepts.
 * Enthält Informationen über Name, Menge und Einheit.
 *
 * @version 1.1
 * @author Marco's Recipe Application Team
 */
@SuppressWarnings("All")
public class Ingredient {

    // --- Attribute ---

    /** Eindeutige ID der Zutat (aus der Datenbank). */
    private int id;

    /** ID des Rezepts, zu dem diese Zutat gehört. */
    private int recipeId;

    /** Name der Zutat (z.B. "Mehl", "Zucker"). */
    private String name;

    /**
     * Mengenangabe als String, um flexible Eingaben wie "1/2", "eine Prise"
     * oder "250" zu ermöglichen.
     */
    private String quantity;

    /** Einheit der Mengenangabe (z.B. "g", "ml", "Stück", "EL"). */
    private String unit;

    // --- Konstruktoren ---

    /**
     * Standardkonstruktor (wird oft von Frameworks benötigt oder für leere
     * Objekte).
     * Initialisiert Strings als leere Strings, um NullPointerExceptions zu
     * vermeiden.
     */
    public Ingredient() {
        this.name = "";
        this.quantity = "";
        this.unit = "";
    }

    /**
     * Konstruktor zum Erstellen einer neuen Zutat (ohne IDs, da diese
     * typischerweise von der Datenbank generiert werden).
     *
     * @param name     Der Name der Zutat. Darf nicht null sein.
     * @param quantity Die Mengenangabe (als String). Kann null sein.
     * @param unit     Die Einheit der Menge. Kann null sein.
     */
    public Ingredient(String name, String quantity, String unit) {
        this.name = Objects.requireNonNull(name, "Name darf nicht null sein."); // Grundlegende Validierung
        this.quantity = quantity != null ? quantity : ""; // Null-Werte durch Leerstrings ersetzen
        this.unit = unit != null ? unit : ""; // Null-Werte durch Leerstrings ersetzen
    }

    /**
     * Vollständiger Konstruktor zum Erstellen eines Zutat-Objekts mit allen Daten
     * (z.B. beim Laden aus der Datenbank).
     *
     * @param id       Die Datenbank-ID der Zutat.
     * @param recipeId Die Datenbank-ID des zugehörigen Rezepts.
     * @param name     Der Name der Zutat. Darf nicht null sein.
     * @param quantity Die Mengenangabe (als String). Kann null sein.
     * @param unit     Die Einheit der Menge. Kann null sein.
     */
    public Ingredient(int id, int recipeId, String name, String quantity, String unit) {
        this(name, quantity, unit); // Ruft den anderen Konstruktor auf
        this.id = id;
        this.recipeId = recipeId;
    }

    // --- Getter und Setter ---

    /**
     * Gibt die Datenbank-ID der Zutat zurück.
     * 
     * @return Die ID der Zutat.
     */
    public int getId() {
        return id;
    }

    /**
     * Setzt die Datenbank-ID der Zutat.
     * (Wird normalerweise nur intern oder beim Laden aus der DB benötigt).
     * 
     * @param id Die neue ID.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gibt die Datenbank-ID des zugehörigen Rezepts zurück.
     * 
     * @return Die ID des Rezepts.
     */
    public int getRecipeId() {
        return recipeId;
    }

    /**
     * Setzt die Datenbank-ID des zugehörigen Rezepts.
     * 
     * @param recipeId Die neue Rezept-ID.
     */
    public void setRecipeId(int recipeId) {
        this.recipeId = recipeId;
    }

    /**
     * Gibt den Namen der Zutat zurück.
     * 
     * @return Der Name der Zutat (nie null).
     */
    public String getName() {
        return name;
    }

    /**
     * Setzt den Namen der Zutat.
     * 
     * @param name Der neue Name. Darf nicht null sein.
     */
    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "Name darf nicht null sein.");
    }

    /**
     * Gibt die Mengenangabe als String zurück.
     * 
     * @return Die Mengenangabe (kann ein leerer String sein, aber nicht null).
     */
    public String getQuantity() {
        return quantity;
    }

    /**
     * Setzt die Mengenangabe.
     * 
     * @param quantity Die neue Mengenangabe. Null-Werte werden in leere Strings
     *                 umgewandelt.
     */
    public void setQuantity(String quantity) {
        this.quantity = quantity != null ? quantity : "";
    }

    /**
     * Gibt die Einheit der Menge zurück.
     * 
     * @return Die Einheit (kann ein leerer String sein, aber nicht null).
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Setzt die Einheit der Menge.
     * 
     * @param unit Die neue Einheit. Null-Werte werden in leere Strings umgewandelt.
     */
    public void setUnit(String unit) {
        this.unit = unit != null ? unit : "";
    }

    // --- Überschriebene Methoden ---

    /**
     * Gibt eine textuelle Repräsentation der Zutat zurück,
     * nützlich für Debugging oder einfache Textausgaben.
     * Format: "Menge Einheit Name" (z.B. "250 g Mehl", "1 Prise Salz")
     *
     * @return Eine formatierte Zeichenkette der Zutat.
     */
    @Override
    public String toString() {
        // Baut den String zusammen, lässt leere Teile weg
        StringBuilder sb = new StringBuilder();
        if (quantity != null && !quantity.trim().isEmpty()) {
            sb.append(quantity.trim()).append(" ");
        }
        if (unit != null && !unit.trim().isEmpty()) {
            sb.append(unit.trim()).append(" ");
        }
        sb.append(name.trim());
        return sb.toString();
    }

    /**
     * Vergleicht diese Zutat mit einem anderen Objekt auf Gleichheit.
     * Zwei Zutaten gelten als gleich, wenn ihre IDs (sofern > 0) übereinstimmen
     * oder wenn alle anderen Felder (Name, Menge, Einheit, Rezept-ID)
     * übereinstimmen.
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
        Ingredient that = (Ingredient) o;
        // Wenn beide IDs gesetzt sind (>0), vergleiche nur IDs
        if (id > 0 && that.id > 0) {
            return id == that.id;
        }
        // Ansonsten vergleiche die relevanten Felder
        return recipeId == that.recipeId &&
                Objects.equals(name, that.name) &&
                Objects.equals(quantity, that.quantity) &&
                Objects.equals(unit, that.unit);
    }

    /**
     * Erzeugt einen Hashcode für die Zutat.
     * Basiert auf der ID, wenn sie gesetzt ist (>0), ansonsten auf den anderen
     * Feldern.
     *
     * @return Der Hashcode für dieses Objekt.
     */
    @Override
    public int hashCode() {
        // Wenn ID gesetzt, nutze diese für den Hashcode
        if (id > 0) {
            return Objects.hash(id);
        }
        // Ansonsten nutze die anderen relevanten Felder
        return Objects.hash(recipeId, name, quantity, unit);
    }
}
