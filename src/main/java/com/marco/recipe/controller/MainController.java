package com.marco.recipe.controller;

import atlantafx.base.controls.Message;
import atlantafx.base.theme.Styles;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

// Import der Model- und View-Klassen
import com.marco.recipe.model.DatabaseService;
import com.marco.recipe.model.Ingredient;
import com.marco.recipe.model.Recipe;
import com.marco.recipe.view.MainView;
import com.marco.recipe.view.RecipeEditDialog; // Import des Edit-Dialogs
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;

/**
 * Der Hauptcontroller der Anwendung.
 * Verbindet die {@link MainView} mit dem {@link DatabaseService}.
 * Verantwortlich für das Laden von Daten, das Reagieren auf Benutzeraktionen
 * (Button-Klicks, Listenauswahl, Suche) und das Aktualisieren der Hauptansicht.
 *
 * @version 1.1
 * @author Mohammed
 */
@SuppressWarnings("ALL")
public class MainController {

    private final MainView mainView;
    private final DatabaseService databaseService;
    private final ObservableList<Recipe> recipeObservableList; // Datenquelle für die ListView

    /**
     * Konstruktor für den MainController.
     * Benötigt Instanzen der MainView und des DatabaseService.
     *
     * @param view    Die Hauptansicht (MainView). Darf nicht null sein.
     * @param service Der Datenbankdienst (DatabaseService). Darf nicht null sein.
     */
    public MainController(MainView view, DatabaseService service) {
        this.mainView = Objects.requireNonNull(view, "MainView darf nicht null sein.");
        this.databaseService = Objects.requireNonNull(service, "DatabaseService darf nicht null sein.");

        // Initialisiert die ObservableList, die mit der ListView verbunden wird
        this.recipeObservableList = FXCollections.observableArrayList();
        // Verbindet die ObservableList mit der ListView in der MainView
        this.mainView.getRecipeListView().setItems(recipeObservableList);

        // Registriert die Event-Handler für die UI-Elemente der MainView
        registerEventHandlers();

        // Lädt die anfängliche Rezeptliste aus der Datenbank
        loadInitialRecipeList();
    }

    /**
     * Registriert die Event-Handler für die Steuerelemente in der MainView.
     */
    private void registerEventHandlers() {
        // Handler für die Auswahl in der ListView
        mainView.getRecipeListView().getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> handleListSelectionChanged(newValue));

        // Handler für die Buttons
        mainView.getNewButton().setOnAction(event -> handleNewRecipe());
        mainView.getEditButton().setOnAction(event -> handleEditRecipe());
        mainView.getDeleteButton().setOnAction(event -> handleDeleteRecipe());

        // Handler für das Suchfeld (reagiert auf Textänderungen)
        mainView.getSearchField().textProperty().addListener(
                (observable, oldValue, newValue) -> handleSearch(newValue));
    }

    /**
     * Lädt die anfängliche Liste der Rezepte (nur IDs und Titel)
     * vom DatabaseService und zeigt sie in der ListView an.
     * Dies geschieht asynchron in einem Hintergrundthread, um die GUI nicht zu
     * blockieren.
     */
    private void loadInitialRecipeList() {
        // Task erstellen, um die Daten im Hintergrund zu laden
        Task<List<Recipe>> loadTask = new Task<>() {
            @Override
            protected List<Recipe> call() throws Exception {
                // Stellt sicher, dass eine Verbindung besteht (kann SQLException werfen)
                databaseService.connect();
                return databaseService.getAllRecipesShortInfo();
            }
        };

        // Was passiert, wenn der Task erfolgreich ist
        loadTask.setOnSucceeded(event -> {
            List<Recipe> recipes = loadTask.getValue();
            recipeObservableList.setAll(recipes); // Aktualisiert die ObservableList (und damit die ListView)
            System.out.println("Rezeptliste erfolgreich geladen.");
            mainView.getRecipeListView().getSelectionModel().clearSelection();
            mainView.clearRecipeDetails();
            mainView.enableActionButtons(false);
        });

        // Was passiert, wenn der Task fehlschlägt
        loadTask.setOnFailed(event -> {
            Throwable exception = loadTask.getException();
            System.err.println("Fehler beim Laden der Rezeptliste: " + exception.getMessage());
            exception.printStackTrace(); // Stacktrace für Debugging ausgeben
            // Zeige Fehlermeldung dem Benutzer
            showErrorAlert("Datenbankfehler", "Fehler beim Laden der Rezeptliste.", exception.getMessage());
            recipeObservableList.clear(); // Liste leeren bei Fehler
        });

        // Task in einem neuen Thread starten
        new Thread(loadTask).start();
    }

    /**
     * Behandelt die Auswahländerung in der Rezeptliste.
     * Lädt die Details des ausgewählten Rezepts und zeigt sie an.
     * Aktiviert/Deaktiviert die Bearbeiten/Löschen-Buttons.
     *
     * @param selectedRecipe Das ausgewählte Recipe-Objekt oder null, wenn die
     *                       Auswahl aufgehoben wurde.
     */
    private void handleListSelectionChanged(Recipe selectedRecipe) {
        if (selectedRecipe != null) {
            // Ein Rezept wurde ausgewählt -> Lade Details und aktiviere Buttons
            mainView.enableActionButtons(true);
            loadAndDisplayRecipeDetails(selectedRecipe.getId());
        } else {
            // Kein Rezept ausgewählt -> Leere Details und deaktiviere Buttons
            mainView.clearRecipeDetails();
            mainView.enableActionButtons(false);
        }
    }

    /**
     * Lädt die vollständigen Details für ein Rezept anhand seiner ID asynchron
     * und zeigt sie in der MainView an.
     *
     * @param recipeId Die ID des Rezepts, dessen Details geladen werden sollen.
     */
    private void loadAndDisplayRecipeDetails(int recipeId) {
        // Task zum Laden der Details im Hintergrund
        Task<Optional<Recipe>> loadDetailsTask = new Task<>() {
            @Override
            protected Optional<Recipe> call() throws Exception {
                // Verbindung sicherstellen (kann Exception werfen)
                databaseService.connect();
                return databaseService.getRecipeDetails(recipeId);
            }
        };

        loadDetailsTask.setOnSucceeded(event -> {
            Optional<Recipe> recipeOptional = loadDetailsTask.getValue();
            if (recipeOptional.isPresent()) {
                // Rezept gefunden -> Details anzeigen
                displayRecipeDetailsInView(recipeOptional.get());
            } else {
                // Rezept nicht gefunden (sollte nicht passieren, wenn ID aus Liste kommt)
                System.err.println("Fehler: Rezeptdetails für ID " + recipeId + " nicht gefunden.");
                showErrorAlert("Fehler", "Rezeptdetails nicht gefunden",
                        "Das ausgewählte Rezept konnte nicht vollständig geladen werden.");
                mainView.clearRecipeDetails();
            }
        });

        loadDetailsTask.setOnFailed(event -> {
            Throwable exception = loadDetailsTask.getException();
            System.err
                    .println("Fehler beim Laden der Rezeptdetails für ID " + recipeId + ": " + exception.getMessage());
            exception.printStackTrace();
            showErrorAlert("Datenbankfehler", "Fehler beim Laden der Rezeptdetails.", exception.getMessage());
            mainView.clearRecipeDetails();
        });

        // Task starten
        new Thread(loadDetailsTask).start();
    }

    /**
     * Formatiert die Details eines Rezepts und zeigt sie in der MainView an.
     * Erstellt die notwendigen JavaFX Nodes (Labels, TextAreas etc.).
     *
     * @param recipe Das vollständige Recipe-Objekt mit Zutaten.
     */
    private void displayRecipeDetailsInView(Recipe recipe) {
        VBox detailsPane = new VBox(10); // Container für alle Detail-Elemente
        detailsPane.setPadding(new Insets(5));

        // Titel
        Label titleLabel = new Label(recipe.getTitle());
        titleLabel.getStyleClass().addAll(Styles.TITLE_2);
        titleLabel.setWrapText(true); // Titel umbrechen, falls zu lang

        // Separator für bessere Trennung
        Separator separator = new Separator(Orientation.HORIZONTAL);
        detailsPane.getChildren().addAll(titleLabel, separator);


        // Meta-Infos (Zeit, Schwierigkeit) in einer HBox
        HBox metaBox = new HBox(20); // Abstand zwischen Meta-Infos
        metaBox.setPadding(new Insets(5, 0, 10, 0)); // Etwas Abstand nach unten
        boolean metaAdded = false;
        if (recipe.getPreparationTimeMinutes() != null && recipe.getPreparationTimeMinutes() > 0) {
            Label preparationTimeLabel = new Label("Zeit: " + recipe.getPreparationTimeMinutes() + " Min.",
                    new FontIcon(Material2OutlinedAL.ACCESS_TIME));
            preparationTimeLabel.getStyleClass().add(Styles.ACCENT);
            metaBox.getChildren().add(preparationTimeLabel);
            metaAdded = true;
        }

        if (recipe.getDifficulty() != null && !recipe.getDifficulty().isEmpty()) {
            String difficulty = recipe.getDifficulty(); // Schwierigkeitsgrad für einfacheren Zugriff speichern

            // Das Label erstellen
            Label difficultyLabel = new Label("Schwierigkeit: " + difficulty,
                    new FontIcon(Material2OutlinedAL.BAR_CHART));

            // --- Bedingtes Styling ---
            // Den Wert des Schwierigkeitsgrads prüfen und den entsprechenden Style anwenden
            if ("Einfach".equals(difficulty)) { // .equals() für String-Vergleiche verwenden!
                difficultyLabel.getStyleClass().addAll(Styles.SUCCESS, Styles.ROUNDED);
            } else if ("Mittel".equals(difficulty)) {
                difficultyLabel.getStyleClass().addAll(Styles.WARNING, Styles.ROUNDED);
            } else if ("Schwer".equals(difficulty)) {
                difficultyLabel.getStyleClass().addAll(Styles.DANGER, Styles.ROUNDED);
            } else {
                difficultyLabel.getStyleClass().addAll(Styles.ACCENT, Styles.ROUNDED);
            }

            metaBox.getChildren().add(difficultyLabel);
            metaAdded = true;
        }
        if (metaAdded) {
            detailsPane.getChildren().add(metaBox);
        }

        // Zutaten
        Label ingredientsHeader = new Label("Zutaten:");
        ingredientsHeader.getStyleClass().addAll(Styles.TITLE_4, Styles.TEXT_BOLD);
        detailsPane.getChildren().add(ingredientsHeader);

        if (recipe.getIngredients() == null || recipe.getIngredients().isEmpty()) {
            detailsPane.getChildren().add(new Label("Keine Zutaten angegeben."));
        } else {
            VBox ingredientsListPane = new VBox(2); // Kleiner Abstand zwischen Zutaten
            for (Ingredient ingredient : recipe.getIngredients()) {
                // Formatierte Zutatenzeile
                String ingredientText = String.format("%s %s %s",
                        ingredient.getQuantity() != null ? ingredient.getQuantity().trim() : "",
                        ingredient.getUnit() != null ? ingredient.getUnit().trim() : "",
                        ingredient.getName().trim() // Name ist nie null
                ).trim().replaceAll("\\s+", " "); // Entferne überflüssige Leerzeichen
                Label ingredientLabel = new Label("✦ " + ingredientText); // Mit Bullet Point
                ingredientsListPane.getChildren().add(ingredientLabel);
            }
            detailsPane.getChildren().add(ingredientsListPane);
        }

        // Anleitung
        Label instructionsHeader = new Label("Anleitung:");
        instructionsHeader.getStyleClass().addAll(Styles.TITLE_4, Styles.TEXT_BOLD);
        detailsPane.getChildren().add(instructionsHeader);
        Label instructionsText = new Label(
                recipe.getInstructions() != null && !recipe.getInstructions().isEmpty() ? recipe.getInstructions()
                        : "Keine Anleitung vorhanden.");
        instructionsText.setWrapText(true); // Wichtig für lange Anleitungen
        detailsPane.getChildren().add(instructionsText);

        // Notizen (optional)
        if (recipe.getNotes() != null && !recipe.getNotes().isEmpty()) {
            Message notesText = new Message("Hinweis", recipe.getNotes(), new FontIcon(Material2OutlinedAL.INFO));
            notesText.getStyleClass().add(Styles.ACCENT);
            detailsPane.getChildren().add(notesText);
        }

        // Übergebe das erstellte VBox-Pane an die MainView zur Anzeige
        mainView.displayRecipeDetails(detailsPane);
    }

    /**
     * Behandelt den Klick auf den "Neues Rezept"-Button.
     * Öffnet den RecipeEditDialog im "Neu"-Modus.
     * Wenn der Dialog erfolgreich geschlossen wird (Speichern), wird das neue
     * Rezept in der Datenbank gespeichert und die Liste aktualisiert.
     */
    private void handleNewRecipe() {
        RecipeEditDialog dialog = new RecipeEditDialog(mainView.getRootPane().getScene().getWindow());
        Optional<Recipe> result = dialog.showAndWait(null); // null für neues Rezept

        result.ifPresent(newRecipe -> {
            // Benutzer hat gespeichert -> Speichere in DB und aktualisiere Liste
            saveOrUpdateRecipeAsync(newRecipe, true); // true für 'isNew'
        });
    }

    /**
     * Behandelt den Klick auf den "Rezept Bearbeiten"-Button.
     * Öffnet den RecipeEditDialog im "Bearbeiten"-Modus mit dem aktuell
     * ausgewählten Rezept.
     * Wenn der Dialog erfolgreich geschlossen wird, wird das geänderte
     * Rezept in der Datenbank gespeichert und die Liste/Details aktualisiert.
     */
    private void handleEditRecipe() {
        Recipe selectedRecipe = mainView.getRecipeListView().getSelectionModel().getSelectedItem();
        if (selectedRecipe == null) {
            showInfoAlert("Keine Auswahl", "Bitte wählen Sie zuerst ein Rezept zum Bearbeiten aus.");
            return;
        }

        // Lade die vollständigen Details, bevor der Dialog geöffnet wird
        // (Da die Liste nur ID/Titel enthält)
        Task<Optional<Recipe>> loadDetailsTask = new Task<>() {
            @Override
            protected Optional<Recipe> call() throws Exception {
                databaseService.connect();
                return databaseService.getRecipeDetails(selectedRecipe.getId());
            }
        };

        loadDetailsTask.setOnSucceeded(event -> {
            Optional<Recipe> recipeToEditOpt = loadDetailsTask.getValue();
            if (recipeToEditOpt.isPresent()) {
                // Details geladen, öffne Dialog
                RecipeEditDialog dialog = new RecipeEditDialog(mainView.getRootPane().getScene().getWindow());
                Optional<Recipe> result = dialog.showAndWait(recipeToEditOpt.get()); // Übergebe vollständiges Rezept

                result.ifPresent(editedRecipe -> {
                    // Benutzer hat gespeichert -> Speichere Änderungen in DB
                    saveOrUpdateRecipeAsync(editedRecipe, false); // false für 'isNew'
                });
            } else {
                showErrorAlert("Fehler", "Rezept nicht gefunden",
                        "Die Details des ausgewählten Rezepts konnten nicht geladen werden.");
            }
        });
        loadDetailsTask.setOnFailed(event -> {
            Throwable exception = loadDetailsTask.getException();
            System.err.println("Fehler beim Laden der Details vor dem Bearbeiten: " + exception.getMessage());
            exception.printStackTrace();
            showErrorAlert("Datenbankfehler", "Fehler beim Laden der Rezeptdetails.", exception.getMessage());
        });

        new Thread(loadDetailsTask).start();
    }

    /**
     * Speichert oder aktualisiert ein Rezept asynchron in der Datenbank.
     * Aktualisiert anschließend die Rezeptliste und wählt das geänderte/neue Rezept
     * aus.
     *
     * @param recipe Das zu speichernde oder zu aktualisierende Rezept.
     * @param isNew  True, wenn es ein neues Rezept ist (save), false für ein
     *               Update.
     */
    private void saveOrUpdateRecipeAsync(Recipe recipe, boolean isNew) {
        Task<Boolean> saveTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                databaseService.connect();
                if (isNew) {
                    return databaseService.saveRecipe(recipe);
                } else {
                    return databaseService.updateRecipe(recipe);
                }
            }
        };

        saveTask.setOnSucceeded(event -> {
            boolean success = saveTask.getValue();
            if (success) {
                showInfoAlert("Erfolg", "Rezept erfolgreich " + (isNew ? "gespeichert." : "aktualisiert."));
                // Lade die Liste neu, um Änderungen anzuzeigen
                refreshRecipeListAndSelect(recipe.getId()); // Wählt das Element nach dem Neuladen aus
            } else {
                showErrorAlert("Fehler", "Speichern fehlgeschlagen",
                        "Das Rezept konnte nicht " + (isNew ? "gespeichert" : "aktualisiert") + " werden.");
            }
        });

        saveTask.setOnFailed(event -> {
            Throwable exception = saveTask.getException();
            System.err.println("Fehler beim Speichern/Aktualisieren des Rezepts: " + exception.getMessage());
            exception.printStackTrace();
            showErrorAlert("Datenbankfehler",
                    "Fehler beim " + (isNew ? "Speichern" : "Aktualisieren") + " des Rezepts.", exception.getMessage());
        });

        new Thread(saveTask).start();
    }

    /**
     * Behandelt den Klick auf den "Rezept Löschen"-Button.
     * Fragt den Benutzer nach Bestätigung und löscht dann das ausgewählte
     * Rezept aus der Datenbank und der Liste.
     */
    private void handleDeleteRecipe() {
        Recipe selectedRecipe = mainView.getRecipeListView().getSelectionModel().getSelectedItem();
        if (selectedRecipe == null) {
            showInfoAlert("Keine Auswahl", "Bitte wählen Sie zuerst ein Rezept zum Löschen aus.");
            return;
        }

        // Bestätigungsdialog anzeigen
        Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationDialog.setTitle("Löschen bestätigen");
        confirmationDialog.setHeaderText("Rezept löschen");
        confirmationDialog
                .setContentText("Möchten Sie das Rezept '" + selectedRecipe.getTitle() + "' wirklich löschen?");
        confirmationDialog.initOwner(mainView.getRootPane().getScene().getWindow());

        Optional<ButtonType> result = confirmationDialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Benutzer hat bestätigt -> Löschen durchführen (asynchron)
            deleteRecipeAsync(selectedRecipe.getId());
        }
    }

    /**
     * Löscht ein Rezept asynchron aus der Datenbank.
     * Aktualisiert anschließend die Rezeptliste.
     *
     * @param recipeId Die ID des zu löschenden Rezepts.
     */
    private void deleteRecipeAsync(int recipeId) {
        Task<Boolean> deleteTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                databaseService.connect();
                return databaseService.deleteRecipe(recipeId);
            }
        };

        deleteTask.setOnSucceeded(event -> {
            boolean success = deleteTask.getValue();
            if (success) {
                showInfoAlert("Erfolg", "Rezept erfolgreich gelöscht.");
                // Lade Liste neu, Auswahl wird automatisch zurückgesetzt
                loadInitialRecipeList();
            } else {
                showErrorAlert("Fehler", "Löschen fehlgeschlagen",
                        "Das Rezept konnte nicht gelöscht werden (möglicherweise existiert es nicht mehr).");
            }
        });

        deleteTask.setOnFailed(event -> {
            Throwable exception = deleteTask.getException();
            System.err.println("Fehler beim Löschen des Rezepts: " + exception.getMessage());
            exception.printStackTrace();
            showErrorAlert("Datenbankfehler", "Fehler beim Löschen des Rezepts.", exception.getMessage());
        });

        new Thread(deleteTask).start();
    }

    /**
     * Behandelt Textänderungen im Suchfeld.
     * Ruft die Suche im DatabaseService auf und aktualisiert die ListView.
     * Führt die Suche asynchron durch.
     *
     * @param query Der aktuelle Suchbegriff aus dem Textfeld.
     */
    private void handleSearch(String query) {
        // Task für die Suche im Hintergrund
        Task<List<Recipe>> searchTask = new Task<>() {
            @Override
            protected List<Recipe> call() throws Exception {
                databaseService.connect();
                if (query == null || query.trim().isEmpty()) {
                    // Wenn Suche leer ist, alle Rezepte laden
                    return databaseService.getAllRecipesShortInfo();
                } else {
                    // Sonst nach dem Begriff suchen
                    return databaseService.searchRecipes(query.trim());
                }
            }
        };

        searchTask.setOnSucceeded(event -> {
            List<Recipe> searchResult = searchTask.getValue();
            recipeObservableList.setAll(searchResult); // Aktualisiere die Liste mit den Suchergebnissen
            // Nach der Suche die Auswahl und Details zurücksetzen
            mainView.getRecipeListView().getSelectionModel().clearSelection();
            mainView.clearRecipeDetails();
            mainView.enableActionButtons(false);
        });

        searchTask.setOnFailed(event -> {
            Throwable exception = searchTask.getException();
            System.err.println("Fehler bei der Rezeptsuche: " + exception.getMessage());
            exception.printStackTrace();
            showErrorAlert("Datenbankfehler", "Fehler bei der Suche.", exception.getMessage());
            recipeObservableList.clear(); // Liste leeren bei Suchfehler
        });

        new Thread(searchTask).start();
    }

    /**
     * Lädt die Rezeptliste neu und versucht, das Rezept mit der angegebenen ID
     * auszuwählen.
     * Wird nach dem Speichern oder Aktualisieren aufgerufen.
     *
     * @param recipeIdToSelect Die ID des Rezepts, das ausgewählt werden soll.
     */
    private void refreshRecipeListAndSelect(int recipeIdToSelect) {
        Task<List<Recipe>> refreshTask = new Task<>() {
            @Override
            protected List<Recipe> call() throws Exception {
                databaseService.connect();
                return databaseService.getAllRecipesShortInfo();
            }
        };

        refreshTask.setOnSucceeded(event -> {
            List<Recipe> recipes = refreshTask.getValue();
            recipeObservableList.setAll(recipes);

            // Versuche, das gerade bearbeitete/neue Rezept auszuwählen
            Optional<Recipe> recipeToSelect = recipes.stream()
                    .filter(r -> r.getId() == recipeIdToSelect)
                    .findFirst();

            if (recipeToSelect.isPresent()) {
                // Wichtig: Auswahl muss im JavaFX Application Thread erfolgen
                Platform.runLater(() -> {
                    mainView.getRecipeListView().getSelectionModel().select(recipeToSelect.get());
                    // Scrolle zum ausgewählten Element (optional, aber benutzerfreundlich)
                    mainView.getRecipeListView().scrollTo(recipeToSelect.get());
                    // Details werden durch den Selection Listener automatisch geladen
                });
            } else {
                // Falls nicht gefunden (sollte nicht passieren), Auswahl zurücksetzen
                Platform.runLater(() -> {
                    mainView.getRecipeListView().getSelectionModel().clearSelection();
                    mainView.clearRecipeDetails();
                    mainView.enableActionButtons(false);
                });
            }
        });

        refreshTask.setOnFailed(event -> {
            // Fehlerbehandlung wie bei loadInitialRecipeList
            Throwable exception = refreshTask.getException();
            System.err.println("Fehler beim Neuladen der Rezeptliste: " + exception.getMessage());
            exception.printStackTrace();
            showErrorAlert("Datenbankfehler", "Fehler beim Neuladen der Rezeptliste.", exception.getMessage());
            recipeObservableList.clear();
        });

        new Thread(refreshTask).start();
    }

    // --- Hilfsmethoden für Alerts ---

    /**
     * Zeigt einen Informations-Alert an.
     * 
     * @param title   Titel des Alerts.
     * @param message Nachricht des Alerts.
     */
    private void showInfoAlert(String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, title, message, null);
    }

    /**
     * Zeigt einen Fehler-Alert an.
     * 
     * @param title   Titel des Alerts.
     * @param message Nachricht des Alerts.
     * @param details Detailinformationen (z.B. Exception Message).
     */
    private void showErrorAlert(String title, String message, String details) {
        showAlert(Alert.AlertType.ERROR, title, message, details);
    }

    /**
     * Zeigt einen Alert-Dialog an. Stellt sicher, dass dies im JavaFX Application
     * Thread geschieht.
     *
     * @param type    Der Typ des Alerts.
     * @param title   Der Titel des Alerts.
     * @param message Die Hauptnachricht.
     * @param details Optionale Details, die in einem ausklappbaren Bereich
     *                angezeigt werden können.
     */
    private void showAlert(Alert.AlertType type, String title, String message, String details) {
        // Sicherstellen, dass Alerts im FX Application Thread angezeigt werden
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(message); // Hauptnachricht im Header
            if (details != null && !details.isEmpty()) {
                // Details in einem ausklappbaren Bereich anzeigen (gut für Fehlermeldungen)
                TextArea textArea = new TextArea(details);
                textArea.setEditable(false);
                textArea.setWrapText(true);
                textArea.setMaxWidth(Double.MAX_VALUE);
                textArea.setMaxHeight(Double.MAX_VALUE);
                GridPane.setVgrow(textArea, Priority.ALWAYS);
                GridPane.setHgrow(textArea, Priority.ALWAYS);

                GridPane expContent = new GridPane();
                expContent.setMaxWidth(Double.MAX_VALUE);
                expContent.add(new Label("Details:"), 0, 0);
                expContent.add(textArea, 0, 1);

                alert.getDialogPane().setExpandableContent(expContent);
                alert.getDialogPane().setExpanded(false); // Standardmäßig eingeklappt
            } else {
                alert.setContentText(null); // Kein einfacher Content-Text, wenn Header verwendet wird
            }

            // Setzt den Owner, damit der Alert über dem Hauptfenster liegt
            if (mainView.getRootPane().getScene() != null && mainView.getRootPane().getScene().getWindow() != null) {
                alert.initOwner(mainView.getRootPane().getScene().getWindow());
            }
            alert.showAndWait();
        });
    }
}