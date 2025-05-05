package com.marco.recipe.view;

import atlantafx.base.theme.Styles;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

// Import der Model-Klassen
import com.marco.recipe.model.Ingredient;
import com.marco.recipe.model.Recipe;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;

/**
 * Stellt ein modales Dialogfenster zur Erstellung und Bearbeitung von Rezepten
 * dar.
 * Der Dialog wird programmatisch erstellt und enthält Felder für alle
 * Rezeptdetails
 * sowie eine dynamische Liste zur Verwaltung von Zutaten.
 * Gibt das erstellte oder bearbeitete Rezept als Optional zurück.
 *
 * @version 1.1
 * @author Mohammed
 */
@SuppressWarnings("All")
public class RecipeEditDialog {

    // --- Dialog Stage und Ergebnis ---
    private final Stage dialogStage;
    private Optional<Recipe> result = Optional.empty(); // Das Ergebnis des Dialogs

    // --- UI-Komponenten ---
    private TextField titleField;
    private Spinner<Integer> timeSpinner; // Spinner für numerische Eingabe der Zeit
    private ComboBox<String> difficultyComboBox;
    private TextArea instructionsArea;
    private TextArea notesArea;

    // Zutaten-Verwaltung
    private VBox ingredientsContainer; // Container für die einzelnen Zutatenzeilen
    private Button addIngredientButton;
    private ScrollPane ingredientsScrollPane; // Damit die Zutatenliste scrollbar wird

    // Buttons am unteren Rand
    private Button saveButton;
    private Button cancelButton;

    // Das aktuell bearbeitete oder neu erstellte Rezept-Objekt
    private Recipe currentRecipe;

    /**
     * Konstruktor für den RecipeEditDialog.
     *
     * @param owner Das übergeordnete Fenster (Hauptfenster), über dem der Dialog
     *              modal angezeigt werden soll.
     */
    public RecipeEditDialog(Window owner) {
        Objects.requireNonNull(owner, "Owner window cannot be null");

        dialogStage = new Stage();
        // Dialog modal machen (blockiert das Hauptfenster)
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(owner);
        dialogStage.setTitle("Rezept bearbeiten/erstellen"); // Titel wird später angepasst

        initComponents();
        layoutComponents();
        registerHandlers();
    }

    /**
     * Initialisiert die UI-Komponenten des Dialogs.
     */
    private void initComponents() {
        titleField = new TextField();
        titleField.setPromptText("Titel des Rezepts");

        // TimeSpinner ist defekt, keine ahnung warum
        // Spinner für Zeit: erlaubt nur positive Ganzzahlen oder 0
        SpinnerValueFactory<Integer> timeValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0,
                Integer.MAX_VALUE, 1);
        timeSpinner = new Spinner<>(timeValueFactory);
        timeSpinner.setEditable(true); // Erlaubt direkte Eingabe
        // Workaround, um leere Eingabe zu ermöglichen (repräsentiert 'null' Zeit)
        timeSpinner.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                timeSpinner.getValueFactory().setValue(0);
            } else {
                try {
                    Integer.parseInt(newValue);
                } catch (NumberFormatException e) {
                    // Ungültige Eingabe -> alten Wert wiederherstellen
                    timeSpinner.getEditor().setText(oldValue);
                }
            }
        });

        // ComboBox für Schwierigkeit
        ObservableList<String> difficultyOptions = FXCollections.observableArrayList(
                "Einfach", "Mittel", "Schwer");
        difficultyComboBox = new ComboBox<>(difficultyOptions);
        difficultyComboBox.setPromptText("Schwierigkeit auswählen");

        instructionsArea = new TextArea();
        instructionsArea.setPromptText("Zubereitungsschritte...");
        instructionsArea.setWrapText(true); // Automatischer Zeilenumbruch

        notesArea = new TextArea();
        notesArea.setPromptText("Optionale Notizen...");
        notesArea.setWrapText(true);

        // Zutaten-Bereich
        ingredientsContainer = new VBox(5); // Abstand zwischen Zutatenzeilen
        ingredientsContainer.setPadding(new Insets(5));

        ingredientsScrollPane = new ScrollPane(ingredientsContainer);
        ingredientsScrollPane.setFitToWidth(true);
        ingredientsScrollPane.setPrefHeight(150); // Bevorzugte Höhe für den Scrollbereich
        ingredientsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        addIngredientButton = new Button("+ Zutat hinzufügen");
        addIngredientButton.getStyleClass().add(Styles.SMALL);

        // Buttons
        saveButton = new Button("Speichern");
        saveButton.setDefaultButton(true); // Wird bei Enter ausgelöst
        saveButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ACCENT);

        cancelButton = new Button("Abbrechen");
        cancelButton.setCancelButton(true); // Wird bei Esc ausgelöst
        cancelButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.WARNING);
    }

    /**
     * Ordnet die UI-Komponenten im Dialog-Layout an.
     * Verwendet ein GridPane für die Hauptfelder und eine VBox für den
     * Zutatenbereich.
     */
    private void layoutComponents() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // Labels hinzufügen
        grid.add(new Label("Titel*:"), 0, 0);
        grid.add(new Label("Zub.-Zeit (Min):"), 0, 1);
        grid.add(new Label("Schwierigkeit:"), 0, 2);
        grid.add(new Label("Anleitung:"), 0, 3);
        grid.add(new Label("Zutaten:"), 0, 4);
        grid.add(new Label("Notizen:"), 0, 6); // Zeile 5 ist für Zutaten-Container + Button

        // Eingabefelder hinzufügen
        grid.add(titleField, 1, 0);
        grid.add(timeSpinner, 1, 1);
        grid.add(difficultyComboBox, 1, 2);

        // Anleitung über mehrere Spalten und Zeilen
        GridPane.setColumnSpan(instructionsArea, 1); // Nur in Spalte 1
        GridPane.setRowSpan(instructionsArea, 1); // Höhe von 1 Zeile (kann angepasst werden)
        GridPane.setVgrow(instructionsArea, Priority.SOMETIMES); // Erlaubt Wachstum, aber nicht übermäßig
        grid.add(instructionsArea, 1, 3);

        // Zutaten-Container und Hinzufügen-Button
        VBox ingredientsLayout = new VBox(5);
        ingredientsLayout.getChildren().addAll(ingredientsScrollPane, addIngredientButton);
        GridPane.setVgrow(ingredientsScrollPane, Priority.ALWAYS); // Scrollpane soll wachsen
        grid.add(ingredientsLayout, 1, 4); // Zutaten in Zeile 4, Spalte 1

        // Notizen
        GridPane.setColumnSpan(notesArea, 1);
        GridPane.setVgrow(notesArea, Priority.SOMETIMES);
        grid.add(notesArea, 1, 6);

        // Spaltenkonfiguration: Spalte 1 soll wachsen
        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS); // Spalte 1 (Index 1) soll horizontal wachsen
        grid.getColumnConstraints().addAll(col1, col2);

        // --- Button-Leiste am unteren Rand ---
        HBox buttonBar = new HBox(10);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setPadding(new Insets(10, 20, 20, 20)); // Abstand oben/unten/rechts/links
        // Platzhalter, um Buttons nach rechts zu schieben
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        buttonBar.getChildren().addAll(spacer, cancelButton, saveButton);

        // --- Hauptlayout des Dialogs ---
        BorderPane mainLayout = new BorderPane();
        mainLayout.setCenter(grid);
        mainLayout.setBottom(buttonBar);

        // Szene erstellen und dem Dialog zuweisen
        Scene scene = new Scene(mainLayout, 715, 650);
        dialogStage.setScene(scene); //
        dialogStage.setMinWidth(715); // Mindestbreite
        dialogStage.setMinHeight(550); // Mindesthöhe
    }

    /**
     * Registriert die Event-Handler für die Buttons.
     */
    private void registerHandlers() {
        saveButton.setOnAction(event -> handleSave());
        cancelButton.setOnAction(event -> handleCancel());
        addIngredientButton.setOnAction(event -> addIngredientRow(null)); // Fügt leere Zeile hinzu
    }

    /**
     * Zeigt den Dialog an und wartet, bis der Benutzer ihn schließt.
     * Füllt die Felder vorab, wenn ein Rezept zum Bearbeiten übergeben wird.
     *
     * @param recipe Das Rezept, das bearbeitet werden soll, oder null für ein neues
     *               Rezept.
     * @return Ein Optional, das das neue oder bearbeitete Rezept enthält, wenn der
     *         Benutzer
     *         "Speichern" geklickt hat, andernfalls Optional.empty().
     */
    public Optional<Recipe> showAndWait(Recipe recipe) {
        this.currentRecipe = recipe; // Speichere das übergebene Rezept

        if (recipe != null) {
            // Bearbeiten-Modus: Felder füllen
            dialogStage.setTitle("Rezept bearbeiten");
            populateFields(recipe);
        } else {
            // Neu-Modus: Titel anpassen und eine leere Zutatenzeile hinzufügen
            dialogStage.setTitle("Neues Rezept erstellen");
            clearFields(); // Stellt sicher, dass Felder leer sind
            addIngredientRow(null); // Füge eine initiale leere Zeile hinzu
        }

        // Ergebnis zurücksetzen vor dem Anzeigen
        this.result = Optional.empty();

        // Dialog anzeigen und warten
        dialogStage.showAndWait();

        return this.result; // Gibt das Ergebnis zurück, das in handleSave gesetzt wurde
    }

    /**
     * Füllt die Eingabefelder des Dialogs mit den Daten des übergebenen Rezepts.
     *
     * @param recipe Das Rezept, dessen Daten angezeigt werden sollen.
     */
    private void populateFields(Recipe recipe) {
        Objects.requireNonNull(recipe, "Recipe cannot be null");

        titleField.setText(recipe.getTitle());
        // Setze Spinner-Wert (prüfe auf null)
        timeSpinner.getValueFactory()
                .setValue(recipe.getPreparationTimeMinutes() != null ? recipe.getPreparationTimeMinutes() : 0);
        if (recipe.getPreparationTimeMinutes() == null) {
            timeSpinner.getEditor().setText(""); // Leere das Textfeld, wenn Zeit null ist
        }
        difficultyComboBox.setValue(recipe.getDifficulty()); // Setzt Auswahl oder null
        instructionsArea.setText(recipe.getInstructions());
        notesArea.setText(recipe.getNotes());

        // Zutatenliste füllen
        ingredientsContainer.getChildren().clear(); // Alte Zeilen entfernen
        if (recipe.getIngredients() != null) {
            for (Ingredient ingredient : recipe.getIngredients()) {
                addIngredientRow(ingredient);
            }
        }
        // Füge eine leere Zeile hinzu, wenn keine Zutaten vorhanden sind
        if (ingredientsContainer.getChildren().isEmpty()) {
            addIngredientRow(null);
        }
    }

    /**
     * Leert alle Eingabefelder des Dialogs.
     */
    private void clearFields() {
        titleField.clear();
        timeSpinner.getValueFactory().setValue(0);
        timeSpinner.getEditor().clear(); // Wichtig für den leeren Zustand
        difficultyComboBox.getSelectionModel().clearSelection();
        difficultyComboBox.setValue(null); // Setze den Wert explizit auf null
        instructionsArea.clear();
        notesArea.clear();
        ingredientsContainer.getChildren().clear(); // Zutatenliste leeren
    }

    /**
     * Fügt eine neue Zeile zur Eingabe einer Zutat hinzu.
     *
     * @param ingredient Die Zutat, deren Daten angezeigt werden sollen, oder null
     *                   für eine leere Zeile.
     */
    private void addIngredientRow(Ingredient ingredient) {
        HBox ingredientRow = new HBox(5); // Abstand zwischen Feldern in der Zeile
        ingredientRow.setAlignment(Pos.CENTER_LEFT);

        TextField qtyField = new TextField(ingredient != null ? ingredient.getQuantity() : "");
        qtyField.setPromptText("Menge");
        qtyField.setPrefWidth(70);

        TextField unitField = new TextField(ingredient != null ? ingredient.getUnit() : "");
        unitField.setPromptText("Einheit");
        unitField.setPrefWidth(80);

        TextField nameField = new TextField(ingredient != null ? ingredient.getName() : "");
        nameField.setPromptText("Zutat*");
        HBox.setHgrow(nameField, Priority.ALWAYS); // Namensfeld soll wachsen

        Button removeButton = new Button("", new FontIcon(Material2OutlinedMZ.REMOVE));
        removeButton.getStyleClass().add(Styles.DANGER);
        removeButton.setOnAction(event -> {
            // Verhindere das Löschen der letzten Zeile
            if (ingredientsContainer.getChildren().size() > 1) {
                ingredientsContainer.getChildren().remove(ingredientRow);
            } else {
                // Optional: Felder leeren statt löschen
                qtyField.clear();
                unitField.clear();
                nameField.clear();
                showAlert(Alert.AlertType.WARNING, "Letzte Zutat", "Mindestens eine Zutatenzeile wird benötigt.");
            }
        });

        ingredientRow.getChildren().addAll(qtyField, unitField, nameField, removeButton);
        ingredientsContainer.getChildren().add(ingredientRow);
    }

    /**
     * Behandelt die Aktion des "Speichern"-Buttons.
     * Validiert die Eingaben, sammelt die Daten, erstellt/aktualisiert das
     * Recipe-Objekt und schließt den Dialog, wobei das Ergebnis gesetzt wird.
     */
    private void handleSave() {
        // --- Validierung ---
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validierungsfehler", "Der Titel darf nicht leer sein.");
            titleField.requestFocus();
            return;
        }

        // Zutaten validieren (mindestens Name muss angegeben sein)
        boolean hasValidIngredient = false;
        for (Node node : ingredientsContainer.getChildren()) {
            if (node instanceof HBox) {
                HBox row = (HBox) node;
                // Annahme: Das Namensfeld ist das dritte Textfeld in der HBox
                if (row.getChildren().get(2) instanceof TextField) {
                    TextField nameField = (TextField) row.getChildren().get(2);
                    if (!nameField.getText().trim().isEmpty()) {
                        hasValidIngredient = true;
                        break; // Eine gültige Zutat reicht
                    }
                }
            }
        }
        if (!hasValidIngredient && ingredientsContainer.getChildren().size() == 1) {
            // Prüfen, ob die einzige Zeile komplett leer ist
            boolean rowIsEmpty = true;
            if (ingredientsContainer.getChildren().get(0) instanceof HBox) {
                HBox firstRow = (HBox) ingredientsContainer.getChildren().get(0);
                for (Node inputNode : firstRow.getChildren()) {
                    if (inputNode instanceof TextField && !((TextField) inputNode).getText().trim().isEmpty()) {
                        rowIsEmpty = false;
                        break;
                    }
                }
            }
            // Wenn die Zeile nicht leer ist, aber der Name fehlt, ist es ein Fehler
            if (!rowIsEmpty) {
                showAlert(Alert.AlertType.ERROR, "Validierungsfehler",
                        "Mindestens eine Zutat muss einen Namen haben, oder die Zeile muss leer sein.");
                return;
            }
            // Wenn die einzige Zeile komplett leer ist, ist es okay (Rezept ohne Zutaten)
        } else if (!hasValidIngredient && ingredientsContainer.getChildren().size() > 1) {
            showAlert(Alert.AlertType.ERROR, "Validierungsfehler", "Mindestens eine Zutat muss einen Namen haben.");
            return;
        }

        // --- Daten sammeln ---
        Recipe recipeData;
        if (currentRecipe != null) {
            // Bearbeiten-Modus: Bestehendes Objekt aktualisieren
            recipeData = currentRecipe;
        } else {
            // Neu-Modus: Neues Objekt erstellen
            recipeData = new Recipe();
        }

        recipeData.setTitle(title);
        recipeData.setInstructions(instructionsArea.getText().trim());
        recipeData.setNotes(notesArea.getText().trim());
        recipeData.setDifficulty(difficultyComboBox.getValue()); // Kann null sein

        // Zeit aus Spinner lesen (handle leere Eingabe als null)
        String timeText = timeSpinner.getEditor().getText();
        if (timeText == null || timeText.trim().isEmpty()) {
            recipeData.setPreparationTimeMinutes(null);
        } else {
            try {
                recipeData.setPreparationTimeMinutes(Integer.parseInt(timeText.trim()));
            } catch (NumberFormatException e) {
                recipeData.setPreparationTimeMinutes(null);
            }
        }

        // Zutaten sammeln
        List<Ingredient> ingredients = new ArrayList<>();
        for (Node node : ingredientsContainer.getChildren()) {
            if (node instanceof HBox) {
                HBox row = (HBox) node;
                // Annahmen über die Reihenfolge der TextFields!
                TextField qtyField = (TextField) row.getChildren().get(0);
                TextField unitField = (TextField) row.getChildren().get(1);
                TextField nameField = (TextField) row.getChildren().get(2);

                String ingredientName = nameField.getText().trim();
                // Nur Zutaten mit Namen hinzufügen
                if (!ingredientName.isEmpty()) {
                    Ingredient ingredient = new Ingredient(
                            ingredientName,
                            qtyField.getText().trim(),
                            unitField.getText().trim());
                    ingredients.add(ingredient);
                } else if (!qtyField.getText().trim().isEmpty() || !unitField.getText().trim().isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Validierungsfehler",
                            "Eine Zutat ohne Namen wurde gefunden (aber mit Menge/Einheit). Bitte geben Sie einen Namen an oder leeren Sie die Zeile.");
                    return;
                }
            }
        }
        recipeData.setIngredients(ingredients);

        // Ergebnis setzen und Dialog schließen
        this.result = Optional.of(recipeData);
        dialogStage.close();
    }

    /**
     * Behandelt die Aktion des "Abbrechen"-Buttons.
     * Schließt den Dialog, ohne das Ergebnis zu ändern (bleibt Optional.empty()).
     */
    private void handleCancel() {
        this.result = Optional.empty(); // Sicherstellen, dass kein Ergebnis zurückgegeben wird
        dialogStage.close();
    }

    /**
     * Hilfsmethode zur Anzeige von Alert-Dialogen.
     *
     * @param type    Der Typ des Alerts (z.B. ERROR, WARNING, INFORMATION).
     * @param title   Der Titel des Alert-Fensters.
     * @param message Die anzuzeigende Nachricht.
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Objects.requireNonNull(type, "Alert type cannot be null");
        Objects.requireNonNull(title, "Alert title cannot be null");
        Objects.requireNonNull(message, "Alert message cannot be null");

        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null); // Kein Header-Text
        alert.setContentText(message);
        alert.initOwner(dialogStage); // Stellt sicher, dass der Alert über dem Dialog liegt
        alert.showAndWait();
    }
}