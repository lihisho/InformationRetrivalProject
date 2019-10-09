package view;

import controller.engineController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.InputStream;

public abstract class Aview {

    protected engineController myController;

    /**
     * opens a new scene above the primary stage
     * @param fxmlName - the fmxl we wish to load.
     * @param btn - a button which appears on the current scene. used in order to get the current stage.
     */
    public void openNewWindow(String fxmlName, Button btn , int width, int height){
        FXMLLoader fxmlLoader = new FXMLLoader();
        try{
            InputStream is= this.getClass().getResource(fxmlName).openStream();
            Parent parent = fxmlLoader.load(is);
            Aview newView = fxmlLoader.getController();
            newView.setMyController(this.myController);
            Scene newScene = new Scene(parent,width,height);
            Stage curStage = (Stage) btn.getScene().getWindow();
            curStage.setScene(newScene);
            curStage.show();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setMyController(engineController controller) {this.myController =controller;}

    public void displayInformationMessage(String alertMessage, String title){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(alertMessage);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
    public void handleBrowse(TextField txtfldToInsert){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        directoryChooser.setTitle( "Choose directory");
        File toOpen = directoryChooser.showDialog(null);

        if(toOpen!=null)
            txtfldToInsert.setText(toOpen.getAbsolutePath());
    }

    public void handleTextBrowse(TextField txtfldToInsert){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.setTitle( "Choose File");
        File toOpen = fileChooser.showOpenDialog(null);

        if(toOpen!=null)
            txtfldToInsert.setText(toOpen.getAbsolutePath());

    }
    public String getTxtFieldText(TextField textFieldToGet){
        if(!textFieldToGet.getText().equals(""))
            return textFieldToGet.getText();
        return null;
    }



}