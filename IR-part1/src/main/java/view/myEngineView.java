package view;
//import com.sun.tools.internal.xjc.reader.xmlschema.bindinfo.BIConversion;
import engineClasses.Searcher;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.*;
import javafx.scene.control.TextField;

import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ResourceBundle;

public class myEngineView extends Aview implements Initializable {

    @FXML
    private AnchorPane engine;

    public String corpusPath;
    public String postingPath;
    public TextField txtfld_corpusPath;
    public TextField txtfld_postingPath;
    public Button btn_chooseDirectoryCorpus;
    public Button btn_chooseDirectoryPosting;
    public Button btn_chooseDirectoryToQuery;
    public Button btn_runSearch;
    public TextField txtfld_query;
    public TextField txtfld_queryFilePath;
    public Button btn_makeDictionary;
    public Button btn_clear;
    public Button btn_loadPostAndDic;
    public Button btn_showDic;
    public ChoiceBox languageChoice;
    public ListView<String> citiesChoice;
    public boolean toStem;
    public CheckBox chbx_toStem;
    public CheckBox chbx_doSemantics;
    public Thread t;


    //when press btn_makeDictionary
    public void handleMakeDictionary(ActionEvent actionEvent) { //the function that starts when pressing button makeDictionary
        String corpus = getTxtFieldText(txtfld_corpusPath);
        String posting = getTxtFieldText(txtfld_postingPath);

        if (posting == null || corpus == null) {
            displayInformationMessage("you must enter the paths in order to start", "error");
        } else {
            corpusPath = corpus;
            postingPath = posting;
            txtfld_corpusPath.setDisable(true);
            txtfld_postingPath.setDisable(true);
            toStem = chbx_toStem.isSelected();
            myController.setStem(toStem);
            myController.setPaths(corpus, posting);//set the paths to
            btn_chooseDirectoryToQuery.setDisable(true);
            btn_runSearch.setDisable(true);
            txtfld_query.setDisable(true);
            txtfld_queryFilePath.setDisable(true);
            btn_chooseDirectoryCorpus.setDisable(true);
            btn_chooseDirectoryPosting.setDisable(true);
            btn_makeDictionary.setDisable(true);
            btn_loadPostAndDic.setDisable(true);
            btn_clear.setDisable(true);
            btn_showDic.setDisable(true);
            languageChoice.setDisable(true);
            t = new Thread(() -> {
                myController.makePosting();
            });
            try {
                t.start();
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //boolean indexMade = myController.makePosting();
            FXMLLoader fxmlLoader = new FXMLLoader();
            //  if(indexMade) {
            try {
                InputStream is = this.getClass().getResource("/postingResults.fxml").openStream();
                Parent actionScreen = fxmlLoader.load(is);
                Aview postingResultsView = fxmlLoader.getController();
                postingResultsView.setMyController(this.myController);
                Scene newScene = new Scene(actionScreen, 600, 400);
                Stage curStage = new Stage();
                curStage.setScene(newScene);
                ((postingResultsView) postingResultsView).showResults(myController.getTotalNumOfDocs(), myController.getTotalNumOfTerms(), myController.getTotalTime(), corpusPath, postingPath);
                setLanguageChoice();
                setCitiesChoice();
                citiesChoice.refresh();
                curStage.showAndWait();
                buttonsDisable(false);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //when press Browser to corpus path
    public void handleChooseCorpus(ActionEvent actionEvent){
        handleBrowse(txtfld_corpusPath);
    }

    //when press Browser to posting path
    public void handleChoosePosting(ActionEvent actionEvent){
        handleBrowse(txtfld_postingPath);
    }

    //when press Browse to query file
    public void handleChooseQueryPath(ActionEvent actionEvent){
        handleTextBrowse(txtfld_queryFilePath);
    }


    //set all languages from texts to choice box
    public void setLanguageChoice(){
        ObservableList<String> languages;
        languages=myController.getLanguages();
        languageChoice.setItems(languages);
    }
    //set all cities from document labels to choice box.
    public void setCitiesChoice() {
        ObservableList<String> cities;
        cities = myController.getCityList();
        citiesChoice.setItems(cities);
        citiesChoice.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    public void setChosenCities() {
        ObservableList<String> chosenCities=FXCollections.observableArrayList();
        if (!citiesChoice.getSelectionModel().isEmpty()) {
            chosenCities = citiesChoice.getSelectionModel().getSelectedItems();
            System.out.println(chosenCities);
        }
        myController.setChosenCities(chosenCities);
    }

    //when press btn_clear
    public void handleClearButton(ActionEvent actionEvent) {
        myController.clearDictionary();
        displayInformationMessage("Clear has been done", "Done");
    }

    //when press check box stem
    public void handleStemCheckBox() {
        toStem=chbx_toStem.isSelected();
    }
    //when press showDictionary
    public void handleShowDictionary(ActionEvent actionEvent) {
        String corpus = getTxtFieldText(txtfld_corpusPath);
        String posting = getTxtFieldText(txtfld_postingPath);
        if (posting == null || corpus == null)
            displayInformationMessage("you must enter the paths in order to start", "error");
        File dictionaryFile;
        if (toStem)
            dictionaryFile = new File(txtfld_postingPath.getText() + "/mainDictionaryWithStem.txt");
        else
            dictionaryFile = new File(txtfld_postingPath.getText() + "/mainDictionary.txt");
        try {
            FileReader fileReader = new FileReader(dictionaryFile);
            BufferedReader reader = new BufferedReader(fileReader);
            ObservableList<String> lines = FXCollections.observableArrayList();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] s = line.split(">");
                String[] termDetails = s[1].split(",");
                String termToInsert = s[0] + ":" + termDetails[1];//term in mainDictionary looks: hello> df,total tf ,maxtf,pointer. we need to show only hello>total tf
                lines.add(termToInsert.toString());
            }
            FXMLLoader fxmlLoader = new FXMLLoader();
            try {
                InputStream is = this.getClass().getResource("/showDic.fxml").openStream();
                Parent actionScreen = fxmlLoader.load(is);
                Aview showDicView = fxmlLoader.getController();
                showDicView.setMyController(this.myController);
                Scene newScene = new Scene(actionScreen, 600, 500);
                Stage curStage = new Stage();
                curStage.setScene(newScene);
                ((showDicView) showDicView).setDictionaryView(lines);//Tal
                curStage.showAndWait();

            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (FileNotFoundException e) {
            displayInformationMessage("File not found", "error");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //When press on bring mainDictionary to memory
    public void handelGetDictionary(ActionEvent actionEvent) {
        String corpus = getTxtFieldText(txtfld_corpusPath);
        String posting = getTxtFieldText(txtfld_postingPath);
        if (posting == null || corpus == null) {
            displayInformationMessage("you must enter the paths in order to start", "error");
        } else {
            toStem = chbx_toStem.isSelected();
            myController.setStem(toStem);
            myController.setPaths(corpus, posting);
            try {
                myController.loadDictionaryToMemory();
                setCitiesChoice();
                btn_runSearch.setDisable(false);
                displayInformationMessage("Dictionary was loaded", "Done");
            }
            catch (FileNotFoundException e) {
                displayInformationMessage("File not found", "error");
            }
        }
    }


    //when press on run search
    public void handleRunSearch(ActionEvent actionEvent) {
        setChosenCities();
        String corpus = getTxtFieldText(txtfld_corpusPath);
        String posting = getTxtFieldText(txtfld_postingPath);
        String queryFilePath = getTxtFieldText(txtfld_queryFilePath);
        String queryToSearch = getTxtFieldText(txtfld_query);
        if (posting == null || corpus == null) {
            displayInformationMessage("You must enter the paths in order to start", "error");
        }
        if(queryFilePath==null && queryToSearch==null)
            displayInformationMessage("Please enter a query to search", "error");
        else if (queryFilePath!=null && queryToSearch!=null)
            displayInformationMessage("Please choose only one of the options to insert a query!", "error");

        else {
            corpusPath = corpus;
            postingPath = posting;
            boolean useSemantics= chbx_doSemantics.isSelected(); //LIHI
            toStem = chbx_toStem.isSelected();
            myController.setStem(toStem);
            myController.setPaths(corpus, posting);//set the paths to
            buttonsDisable(true);
            if (queryFilePath != null) {
                try {
                    myController.runSearchFromQueryDoc(queryToSearch,queryFilePath,useSemantics);
                    myController.setResultsFromFileQuery();//load the results to controller before open table view
                    openQueryResultFromFile();
                    buttonsDisable(false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                myController.startRunQuery(queryToSearch,myController.getNextQueryID(),false);
                openQueryResultView();
                buttonsDisable(false);
            }
        }
    }

    public void buttonsDisable(boolean state){
        txtfld_corpusPath.setDisable(state);
        txtfld_postingPath.setDisable(state);
        languageChoice.setDisable(state);
        btn_chooseDirectoryPosting.setDisable(state);
        btn_chooseDirectoryCorpus.setDisable(state);
        btn_makeDictionary.setDisable(state);
        btn_chooseDirectoryToQuery.setDisable(state);
        btn_runSearch.setDisable(state);
        txtfld_query.setDisable(state);
        txtfld_queryFilePath.setDisable(state);
        btn_clear.setDisable(state);
        btn_loadPostAndDic.setDisable(state);
        btn_showDic.setDisable(state);

    }

    public void openQueryResultView(){
        myController.setQueryResults();//set in controller linkedHashMap of results [queryID,treeMap<string,double>rankedDocs]
        myController.setMapDominantPerDoc();
        FXMLLoader fxmlLoader = new FXMLLoader();
        try {
            InputStream is = this.getClass().getResource("/showQueryResult.fxml").openStream();
            Parent actionScreen = fxmlLoader.load(is);
            Aview queryResult = fxmlLoader.getController();
            queryResult.setMyController(this.myController);
            Scene newScene = new Scene(actionScreen, 600, 400);
            Stage curStage = new Stage();
            curStage.setScene(newScene);
            curStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void openQueryResultFromFile(){
        myController.setResultsFromFileQuery();//set in controller linkedHashMap of results [queryID,treeMap<string,double>rankedDocs]
        myController.setGivenQueries();//set in contrillor arrayList of all the given queries in order to show on results
        myController.setMapDominantPerDoc();
        FXMLLoader fxmlLoader = new FXMLLoader();
        try {
            InputStream is = this.getClass().getResource("/showFileQueryResults.fxml").openStream();
            Parent actionScreen = fxmlLoader.load(is);
            Aview queryResult = fxmlLoader.getController();
            queryResult.setMyController(this.myController.getInstance());
            Scene newScene = new Scene(actionScreen, 600, 500);
            //Stage curStage = (Stage) btn_makeDictionary.getScene().getWindow();
            Stage curStage = new Stage();
            curStage.setScene(newScene);
            curStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Tal

    public Thread gerCurrThread(){
        return myController.getCurrThread();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btn_runSearch.setDisable(true);
        //TODO: delete!
        txtfld_queryFilePath.setText("C:\\Users\\lihi\\IdeaProjects\\old revisions\\IR-part1\\queries.txt");
        txtfld_postingPath.setText("C:\\Users\\lihi\\IdeaProjects\\old revisions\\IR-part1\\temp");
        txtfld_corpusPath.setText("C:\\Users\\lihi\\IdeaProjects\\old revisions\\IR-part1\\corpus\\corpus");
    }
}
