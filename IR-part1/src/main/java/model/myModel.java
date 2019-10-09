package model;

import engineClasses.ReadFile;
import engineClasses.Searcher;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.*;

public class myModel implements Imodel {
    private static Imodel singleton = null;

    private String corpusPath;
    private String postingPath;
    private ReadFile readfile;
    private Searcher searcher;
    private long totalTime;
    private boolean stem;
    private File dictionaryFile;
    private HashMap<String, String> mainDictionary;
    private File dictionaryCityFile;
    private HashMap<String, String> cityDictionary;
    private ArrayList<String> chosenCities; //cities that were chosen by the user.
    private HashMap<String,Double> queryResults;
    private LinkedHashMap<String,HashMap<String,Double>> textFileQueriesResults;//<queryID,treeMap<docID,rank>
    private static int queryIDNotFromTextFile;//if the query is from text field and not from file, queryID  is serialize
    private LinkedHashMap<String,String> givenQueries;// list of <queryID,Given query content>
    private static HashMap<String,LinkedHashMap<String,String>> docDominantTFMap;
    private Thread t;
    //Constructor
    public myModel(){
        stem=false; //defult- don't use stem
        searcher=null;
        chosenCities=null;
        queryIDNotFromTextFile=500;
        textFileQueriesResults=new LinkedHashMap<>();
        givenQueries=new LinkedHashMap<>();
    }
    //saves a singeltone in order to work with the table view columns. and prevent duplication.
    public static Imodel getInstance(){
        if(singleton==null)
            singleton=new myModel();
        return singleton;
    }
    //Sets the paths that the user entered.
    public void setPaths(String corpusPath, String postingPath) {
        this.corpusPath = corpusPath;
        this.postingPath = postingPath;
    }
    //Sets the stem to be true OR false depending on what the user entered.
    public void setStem(boolean stem) {
        this.stem = stem;
    }

    /**
     * Creates The inverted index- that includes a dictionary and posting files.
     * (The main function of the program -part A)
     * @return - true if succeded.
     */
    public boolean makePosting() {
        readfile = new ReadFile(stem, postingPath);
        try {
            long startTime = System.currentTimeMillis();
            readfile.createCitiesList(corpusPath);
            readfile.saveCitiesToFile(postingPath);
            readfile.savelanguagesToFile(postingPath);
            readfile.setStopWordsAndCities(corpusPath);
            readfile.listFilesForFolder(corpusPath);
            readfile.sendTerms(true);
            long endTime = System.currentTimeMillis();
            totalTime = (endTime - startTime) / 1000;
            return true;
        }
        catch (IOException e){
            return false;
        }
    }
//Getters and setters:

    @Override
    public int totalNumOfDocs() {
        return readfile.getTotalNumOfDocs();
    }

    public int getTotalNumOfTerms() {
        return readfile.getNumOfDistinctTerms();
    }

    public long getTotalTime() {
        return totalTime;
    }

    public void clearDictionary() {
        readfile.clearDictionary();
    }

    public ObservableList<String> getLanguages() {
        ArrayList<String> lan = readfile.getLanguageList();
        ObservableList<String> ol = FXCollections.observableArrayList();
        ol.addAll(lan);
        return ol;
    }

    // Read file of cities from disk and save a list.
    public ObservableList<String> getCityList() {
        ArrayList<String> possibleCitiesList = new ArrayList<>();
        String path;
        if (stem) path = postingPath + "\\citiesListWithStem.txt";
        else path = postingPath + "\\citiesList.txt";
        File cities = new File(path);
        try {
            String line;
            FileReader fr = new FileReader(cities);
            BufferedReader reader = new BufferedReader(fr);
            while (null != (line = reader.readLine())) {
                possibleCitiesList.add(line);
            }
            fr.close();
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        ObservableList<String> olist = FXCollections.observableArrayList();
        olist.addAll(possibleCitiesList);
        return olist;
    }

    public void setChosenCities(ObservableList<String> chosenCities) {
        this.chosenCities = new ArrayList<String>();
        this.chosenCities.addAll(chosenCities);
    }

    public void loadDictionaryToMemory() throws FileNotFoundException {
        if (stem) {
            dictionaryFile = new File(postingPath + "/mainDictionaryWithStem.txt");
            dictionaryCityFile = new File(postingPath + "/cityDictionaryWithStem.txt");
        } else {
            dictionaryFile = new File(postingPath + "/mainDictionary.txt");
            dictionaryCityFile = new File(postingPath + "/cityDictionary.txt");
        }
        try {
            mainDictionary = new HashMap<>();
            cityDictionary = new HashMap<>();
            String line;
            FileReader fileReader = new FileReader(dictionaryFile);
            BufferedReader reader = new BufferedReader(fileReader);
            FileReader cityFileReader = new FileReader(dictionaryCityFile);
            BufferedReader cityReader = new BufferedReader(cityFileReader);
            while (null != (line = reader.readLine())) {
                String[] termKeyAndValue = line.split(">");
                mainDictionary.put(termKeyAndValue[0], termKeyAndValue[1]);
            }

            while (null != (line = cityReader.readLine())) {
                String[] termKeyAndValue = line.split(">");
                cityDictionary.put(termKeyAndValue[0], termKeyAndValue[1]);
            }

            fileReader.close();
            reader.close();
            cityFileReader.close();
            cityReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start running the search of the query words.
     * @param queryToSearch - which query the user entered.
     * @param queryFilePath -
     * @throws IOException
     */
    public void runSearchFromQueryDoc(String queryToSearch,String queryFilePath, boolean useSemantics) throws IOException {
        File queryFile = new File(queryFilePath);
        String queryFileContent = new String(Files.readAllBytes((Paths.get(queryFile.getPath()))));
        Document doc = Jsoup.parse(queryFileContent);
        Elements links = doc.select("top");

        for (Element link : links) {
            queryToSearch = link.select("title").text();
            if(useSemantics)
                queryToSearch = getSemantics(queryToSearch);
            String description=link.select("desc").text();

            if(description.contains("Description")){ //in order to add description words to the given query
                String [] split=description.split("Description:");
                if(split.length>1)
                    description=split[1];
                else description="";
            }
           // String narrative=link.select("narr").text();
            String queryID=link.select("num").text();
            if (queryID.length()>11)
                queryID=queryID.substring(8,11);//TAL- cannot extract <num> from query
            givenQueries.put(queryID,queryToSearch.replaceAll("\\\\", ""));//Tal

            String textToAdd = replaceStrings(description );//+" "+ narrative);
            queryToSearch = queryToSearch + queryToSearch +" "+textToAdd;//adds the descripation of query to the query title in order to retrive more relevant docs. give a learger wheight to the query words.
            startRunQuery(queryToSearch,queryID,true);
        }
        searcher.closeFiles();

    }
// Replaces strings in query for making rellevence better.
    private String replaceStrings(String text) {
        String toReturn;
        toReturn=text.replaceAll("identify", "");
        toReturn=text.replaceAll("Identify", "");
        toReturn=text.replaceAll("documents", "");
        toReturn=text.replaceAll("Documents", "");
        toReturn=text.replaceAll("discussing", "");
        toReturn=text.replaceAll("discuss", "");
        toReturn=text.replaceAll("concern", "");
        toReturn=text.replaceAll("information", "");
        toReturn=text.replaceAll("Information", "");
        toReturn=text.replaceAll("Narrative", "");
        toReturn=text.replaceAll("purpose", "");
        toReturn=text.replaceAll("relevant", "");
        toReturn=text.replaceAll("considered", "");
        toReturn=text.replaceAll("following", "");
        toReturn=text.replaceAll("Relevant", "");
        toReturn=text.replaceAll("contain", "");
        toReturn=text.replaceAll("must", "");
        toReturn=text.replaceAll("non-relevant", "");
        return toReturn;


    }

    /**
     * Run function in a new thread- runs faster and works better with the GUI.
     * @param queryToSearch
     * @param queryID
     * @param isFromQueryFile
     */
    public void startRunQuery(String queryToSearch,String queryID,boolean isFromQueryFile){
        t = new Thread(() -> {
            search(queryToSearch,queryID,isFromQueryFile);
        });
        try {
            t.start();
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void search(String queryToSearch, String queryID, boolean isFromQueryFile) {

        if (mainDictionary == null && cityDictionary == null) {
            try {
                loadDictionaryToMemory();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (searcher == null) { //the searcher object is created only once during all the program
            {
                searcher = new Searcher(corpusPath, postingPath, mainDictionary, cityDictionary, stem, chosenCities);
                docDominantTFMap = searcher.getDocDominantTFMap();
            }
        }
        try {
            queryResults = searcher.queryResults(queryToSearch);
            if (isFromQueryFile) {
                textFileQueriesResults.put(queryID, queryResults);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public LinkedHashMap<String, HashMap<String, Double>> getQueryResultsFromFileList() {
        return textFileQueriesResults;
    }

    //for single queries
    public HashMap<String, Double> getQueryResults() {

        return queryResults;
    }

    public String getNextQueryID() {
        queryIDNotFromTextFile++;
        return "" + queryIDNotFromTextFile;
    }

    public String getQueryIDNotFromTextFile() {
        return "" + queryIDNotFromTextFile;
    }


    public HashMap<String, LinkedHashMap<String, String>> getDominantPerDoc() {
        return docDominantTFMap;
    }

    public LinkedHashMap<String, String> getGivenQueries() {
        return givenQueries;
    }

    public Thread getCurrThread(){
        return t;
    }

    public String getSemantics(String query) {
        String[] queryTerms = query.split(" ");
        String semWord=null;
        StringBuilder newQuery = new StringBuilder();
        newQuery.append(query);
        for (int i = 0; i < queryTerms.length; i++) {
            try {
                String urlPath = "https://api.datamuse.com/words?rel_syn=" + queryTerms[i]; //return the specified fields.;
                URL url = new URL(urlPath);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();
                BufferedReader bw = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String getInput = null;
                StringBuffer getContents = new StringBuffer(); //the properties of the city.
                while ((getInput = bw.readLine()) != null) {
                    getContents.append(getInput);
                }
                bw.close();
                connection.disconnect();
                int numOfSemanticWordsToReturn = 4;
                int endIndex;
                String[] semanticWords = getContents.toString().split("word\":\"");
                HashSet<String> semanticsWithoutDuplicate =new HashSet<>();
                for (int j = 1; j < semanticWords.length && j < numOfSemanticWordsToReturn; j++) { //start from 1 because the first will be only [{
                    endIndex = semanticWords[j].indexOf("\"");
                    semWord = semanticWords[j].substring(0,endIndex);
                    String [] semWords= semWord.split(" ");
                    for (String word : semWords) { // Prevent duplication - if similar expressions include the same words
                        if (!semanticsWithoutDuplicate.contains(word)) {
                            semanticsWithoutDuplicate.add(word);
                            newQuery.append(" " + word);
                        }
                    }
                }
            }
            catch (ProtocolException e) {
                  //  e.printStackTrace();
                } catch (MalformedURLException e) {
                   // e.printStackTrace();
                } catch (IOException e) {
                    //e.printStackTrace();
                }
        }
        return newQuery.toString();
    }
}

