package view;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.InputStream;

public class postingResultsView extends Aview{
    public TextField txtfld_numOfDocs;
    public TextField txtfld_numOfDistinctTerms;
    public TextField txtfld_timeOfProcess;
    public String corpusPath;
    public String postingPath;


    public void showResults(int numOfDocs, int numOfDistinctTerms, long timeOfProcess, String corpusPath,String postingPath){
        this.corpusPath=corpusPath;
        this.postingPath=postingPath;
        txtfld_numOfDocs.setText(""+numOfDocs);
        txtfld_numOfDistinctTerms.setText( ""+numOfDistinctTerms);
        txtfld_timeOfProcess.setText(""+timeOfProcess);

    }


}
