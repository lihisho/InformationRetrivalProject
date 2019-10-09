package view;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.InputStream;

public class showDicView extends Aview {
    public ListView dictionaryView;

    public void setDictionaryView(ObservableList<String>lines){
        dictionaryView.setItems(lines);
    }

}
