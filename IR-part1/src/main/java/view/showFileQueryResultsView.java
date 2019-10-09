package view;

import controller.engineController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class showFileQueryResultsView extends Aview implements Initializable {
    private engineController myController=engineController.getInstance();
    public TableView<queryResultsColumn> tableResults;

    public TableColumn<queryResultsColumn,String> queryIDCol;
    public TableColumn<queryResultsColumn,String> queryTextCol;
    public TableColumn<queryResultsColumn,String> resultsCol;
    public Button btn_chooseDirectoryResults;
    public Button btn_saveResults;
    public TextField txtfld_queryResultsFilePath;
    LinkedHashMap<String,HashMap<String,Double>> returnResults;
    @Override
    public void initialize(URL location, ResourceBundle resource){
        queryIDCol.setCellValueFactory(new PropertyValueFactory<>("queryID"));
        queryTextCol.setCellValueFactory(new PropertyValueFactory<>("queryText"));
        resultsCol.setCellValueFactory(new PropertyValueFactory<>("hyl_viewResults"));
        LinkedHashMap<String,String> givenQueries=myController.getGivenQueries();//in order to take query content for queryID
        returnResults=myController.getResultsFromFileQuery();
        ObservableList<queryResultsColumn> data=FXCollections.observableArrayList();
        if(returnResults!=null) {
            for (String queryID : returnResults.keySet()) {
                String s=givenQueries.get(queryID);
                data.add(new queryResultsColumn(queryID,givenQueries.get(queryID), new Hyperlink()));
            }
        }
        tableResults.setItems(data);

    }
    public void handleChooseResultsPath(ActionEvent actionEvent){
        handleBrowse(txtfld_queryResultsFilePath);
    }
    /**
     * Saves to file the results of each query.
     * Results will be in the pattern: "QueryID 0 docNo. rank sim(float) runID".
     */
    public void handleSaveAllResults(){
        if (txtfld_queryResultsFilePath.getText().equals(""))
            displayInformationMessage("Please enter a path to save results.", "Path Empty");
        else {
            File allQueryResults = new File(txtfld_queryResultsFilePath.getText()+ "\\allQueriesResults.txt");
            try {

                FileWriter fw = new FileWriter(allQueryResults, false);
                BufferedWriter bw = new BufferedWriter(fw);

                String docNo,qId;
                int docRank;
                float rank;
                StringBuilder resultString = new StringBuilder();
                for (String query: returnResults.keySet()) {
                    HashMap<String, Double> eachQueryResults = returnResults.get(query);
                    for (String doc : eachQueryResults.keySet()) {
                        qId = query;
                        docNo = doc;
                        docRank = eachQueryResults.get(doc).intValue();
                        rank =eachQueryResults.get(doc).floatValue();
                        if(docRank<0 || docRank>1000)
                            docRank =1;
                        resultString.setLength(0);
                        resultString.append(qId);
                        resultString.append(" 0 ");
                        resultString.append(docNo);
                        resultString.append(" ").append(docRank).append(" ");
                        resultString.append((rank/10));
                        resultString.append(" wow");
                        bw.write(resultString.toString());
                        bw.newLine();
                        bw.flush();
                    }
                }
                //close files
                fw.close();
                bw.close();


            } catch (IOException e) {
               // e.printStackTrace();
            }
        }
    }
}
