package engineClasses;

import jdk.nashorn.internal.ir.annotations.Ignore;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class Indexer {
    private HashMap<String,String> cityMap;
    private ArrayList<String> citiesFromDocs;
    private HashMap<String,String> dictionary; // Save array of 2 ints??
    private HashMap<String,String> cityDictionary;
    private String postingFilesPath;//path to save posting files
    private PriorityQueue<String> termsQueue;
    private File MergedPostingFile;
    private File MergedCityPostingFile;
    int postingFileNumber; //Represent number of temporary posting file.
    private RandomAccessFile writeMergePostingWithPosition;
    private RandomAccessFile writeCitiesPosting;
    private int maxTfCity; // For checking which city show max tomes on 1 doc.
    private String maxTfCityName; //For checking which city show max tomes on 1 doc.
    private boolean toStem;//if the user chose to use stem or not.
    private int cityNotCapitalCount;
    private String tempPostingPath;
    private Stemmer stemmer;//Tal
    public int num_of_NumberTerms;
    // private int numOfCountries; // count number of countries that show in the corpus.

    //Constructor
    public Indexer(boolean stem, String postingPath){
        num_of_NumberTerms=0;
        dictionary = new HashMap<>();
        cityDictionary = new HashMap<>();
        cityMap =new HashMap<>();
        //countries =new ArrayList<>();
        stemmer =new Stemmer();
        postingFileNumber=1;
        maxTfCity=0;
        maxTfCityName= "";
        toStem=stem;
        cityNotCapitalCount=0;
        postingFilesPath=postingPath;
        termsQueue =new PriorityQueue<>(new compareTerms());
        File postingDir;
        if(toStem){
            tempPostingPath=postingFilesPath+"\\tempWithStem";
            postingDir = new File(tempPostingPath);
            MergedPostingFile=new File(postingFilesPath+"\\mergedFileWithStem.txt");
            MergedCityPostingFile=new File(postingFilesPath+"\\mergedCityPostingFileWithStem.txt");

        }
        else {
            tempPostingPath=postingFilesPath+"\\temp";
            postingDir = new File(tempPostingPath);
            MergedPostingFile = new File(postingFilesPath + "\\mergedFile.txt");
            MergedCityPostingFile=new File(postingFilesPath+"\\mergedCityPostingFile.txt");

        }

        postingDir.mkdir();

        try {
            writeMergePostingWithPosition =new RandomAccessFile(MergedPostingFile, "rw");
            writeCitiesPosting =new RandomAccessFile(MergedCityPostingFile, "rw");

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
/*
    public int getCityNotCapitalCount(){
        return cityNotCapitalCount;
    }
    public int getNumOfCities(){
        return citiesFromDocs.size();
    }
    public int getNumOfCountries(){
        return numOfCountries;
    }
    public  String getMaxTfCityName(){
        return maxTfCityName;
    }
*/
    /**
     * Set the folder path where the posting files will be saved.
     * @param path -path to read from\ write to.
     */
    public void setPostingPath(String path) {
        postingFilesPath=path;
    }

    /**
     * read the terms that were sent from parsing and creates temp posting list
     * @param wordsInDoc- all terms that was save during the parsing of each doc
     * @param docsDetails- list of documents and details
     * @param last- boolean if the last documents of the corpus was sent
     */
    public void tempPosting(HashMap<String, HashMap<String, ArrayList<Integer>>> wordsInDoc, ArrayList<String> docsDetails,boolean last){
        writeDocumentDetails(docsDetails,last);
        int df=0;
        int totalTF=0;
        StringBuilder postingRec=new StringBuilder();
        String path= tempPostingPath+"\\"+postingFileNumber+".txt"; //save temp postings.
        File postingFile=new File(path);
        TreeMap<String, HashMap<String, ArrayList<Integer>>> sortedTerm=new TreeMap<>(new compareTerms());
        sortedTerm.putAll(wordsInDoc);
        try {
            FileWriter filer = new FileWriter(path, true);
            BufferedWriter bw = new BufferedWriter(filer);
            for(String term:sortedTerm.keySet()){
                for(String docID:wordsInDoc.get(term).keySet()) { //pass over all docs.
                    postingRec.append(postingRecord(docID,wordsInDoc.get(term).get(docID)));
                    totalTF+=wordsInDoc.get(term).get(docID).size(); //add up TF of all documents.
                }
                df=wordsInDoc.get(term).size(); //Number of docs the term appears in.

                addToDicAndPostingTmp(postingRec,df,totalTF,term,bw);
                totalTF=0;
                postingRec.setLength(0);
            }
            postingFileNumber++;
            filer.close();
            bw.close();

            termsQueue.clear();

        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Write a list of documents with their details to a file.
     * @param docsDetails - docID, maxTF, DF, city label.
     */
    private void writeDocumentDetails(ArrayList<String> docsDetails,boolean last) {
        //  System.out.println("write doc details");
        String path;
        if(toStem){
            path = postingFilesPath + "\\documentDetailsWithStem.txt";
        }
        else
            path = postingFilesPath + "\\documentDetails.txt";
        File docsFile = new File(path);

        try {
            //docsFile.createNewFile();
            FileWriter fw = new FileWriter(path, true);
            BufferedWriter bw = new BufferedWriter(fw);
            for (String doc : docsDetails) {
                bw.write(doc);
                bw.newLine();
                bw.flush();
            }
            if(last) {
                fw.close();
                bw.close();
            }

        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
    //Write to file: for each doc, the 5 most popular entities. ordered by Tf number from largest to smallest.
    public void writeTfForDoc(HashMap<String,HashMap<String,Double>> docDominantTFMap,boolean last) {
        String path;
        if(toStem){
            path = postingFilesPath + "/mapTFWithStem.txt";
        }
        else
            path = postingFilesPath + "/mapTF.txt";
        File docsFile = new File(path);
        try {

            FileWriter fw = new FileWriter(path, true);
            BufferedWriter bw = new BufferedWriter(fw);

            for (String doc : docDominantTFMap.keySet()) {
                HashMap<String,Double> sortedTermsByTF= sortMap(docDominantTFMap.get(doc));
                //TreeMap <String,Double> sortedTermsByTF=new TreeMap<>(new valueComparator(docDominantTFMap.get(doc)));
                //sortedTermsByTF.putAll(docDominantTFMap.get(doc));
                StringBuilder s=new StringBuilder();
                s.append(doc);
                s.append(">");
                for(String term:sortedTermsByTF.keySet()) {
                    s.append(term);
                    s.append(",");
                    s.append((sortedTermsByTF.get(term)).intValue());
                    s.append(",");
                }
                bw.write(s.toString().substring(0,s.length()-1));
                bw.newLine();
                bw.flush();
            }
            if(last) {
                fw.close();
                bw.close();
            }

        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
    private HashMap<String,Double> sortMap(Map<String, Double> unsortMap) {

        List<Map.Entry<String, Double>> list = new LinkedList<>(unsortMap.entrySet());
        Collections.sort(list, new rankComparator());
        HashMap<String, Double> sortedDocs = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : list) {
            sortedDocs.put(entry.getKey(), entry.getValue());
        }
        return sortedDocs;
    }

    /**
     * Write a list of terms with their details from dictionary to a file.
     */

    private void saveDictionariesToFile () {
        String pathDictionary;
        String pathCityDic;
        TreeMap<String,String> mainDic=new TreeMap<>(new compareTerms());//in order to sort dictionaries
        mainDic.putAll(dictionary);
        TreeMap<String,String> cityDic=new TreeMap<>(new compareTerms());
        cityDic.putAll(cityDictionary);
        if(toStem){
            pathDictionary = postingFilesPath + "\\mainDictionaryWithStem.txt";
            pathCityDic=postingFilesPath + "\\cityDictionaryWithStem.txt";
        }
        else {
            pathDictionary = postingFilesPath + "\\mainDictionary.txt";
            pathCityDic=postingFilesPath + "\\cityDictionary.txt";
        }

        File dicFile = new File(pathDictionary);
        File cityDicFile=new File(pathCityDic);
        try {
            FileWriter fwD = new FileWriter(pathDictionary, true);
            FileWriter fwC = new FileWriter(cityDicFile, true);

            BufferedWriter bwD = new BufferedWriter(fwD);
            BufferedWriter bwC = new BufferedWriter(fwC);

            for (String term : mainDic.keySet()) {
                bwD.write(term);
                bwD.write(">"+ mainDic.get(term));
                bwD.newLine();
                bwD.flush();
            }
            fwD.close();
            bwD.close();
            for (String cityTerm : cityDic.keySet()) {
                bwC.write(cityTerm);
                bwC.write(">"+ cityDic.get(cityTerm));
                bwC.newLine();
                bwC.flush();
            }
            fwC.close();
            bwC.close();

        }
        catch (IOException e){
            e.printStackTrace();
        }
/* check number terms.
        for (String s:dictionary.keySet()){
            try {
                double d = Double.parseDouble(s);
            } catch (NumberFormatException | NullPointerException nfe) {
                continue;
            }
            num_of_NumberTerms++;
        }
*/
    }

    /**
     * Create a record for a specific term to save in the posting file.
     * @param termPositions- contains list of docs that the terms appear in, and the postions of the term in the doc
     * @return  record to insert to posting file
     */
    private StringBuilder postingRecord(String docID, ArrayList<Integer> termPositions) {
        int tf=0;
        StringBuilder recordToInsert=new StringBuilder();
        tf=termPositions.size();
        recordToInsert.append("{");
        recordToInsert.append(docID);
        recordToInsert.append("|");
        recordToInsert.append(tf);
        recordToInsert.append("|");
        if (!termPositions.isEmpty())
            recordToInsert.append(termPositions.get(0).toString());
        return recordToInsert;

    }

    /**
     * add the term to dictionary and writes the record to the temp posting file
     * @param postingRec- to be insert- DocID, tf, positions
     * @param df- total number of documents
     * @param totalTF- total number of occurences
     * @param term- to be insert
     * @param bw
     */
    private void addToDicAndPostingTmp(StringBuilder postingRec,int df,int totalTF, String term,BufferedWriter bw ){
        String upperTerm = term.toUpperCase();
        String value;
        if(citiesFromDocs.contains(upperTerm)){// check if the term is one of the cities we saved from the document tags.
            value= cityDictionary.get(upperTerm);
            if(value!=null) {
                String [] generalDocDetails = value.split(",");
                cityDictionary.put(upperTerm,(Integer.parseInt(generalDocDetails[0]) + df) + ","+(Integer.parseInt(generalDocDetails[1])+totalTF));
            }
            else
                cityDictionary.put(upperTerm,df+","+totalTF);
        }
        else {
            if (Character.isLowerCase(term.charAt(0))) { //if the term is in lower case letters.
                value = dictionary.get(term);
                if (value != null) {
                    String[] generalDocDetails = value.split(",");
                    dictionary.put(term, (Integer.parseInt(generalDocDetails[0]) + df) + "," + (Integer.parseInt(generalDocDetails[1]) + totalTF));
                }
                else { //If it doesn't exist in lower case, check upper case.
                    value = dictionary.get(term.toUpperCase());
                    if (value != null) { //If found - save only lower case term.
                        String[] generalDocDetails = value.split(",");
                        dictionary.remove(term.toUpperCase());
                        dictionary.put(term, (Integer.parseInt(generalDocDetails[0]) + df) + "," + (Integer.parseInt(generalDocDetails[1]) + totalTF));
                    }
                    else
                        dictionary.put(term, df + "," + totalTF);
                }
            }
            else { //if the new term is in upper case letters.
                value = dictionary.get(term.toLowerCase());
                if (value != null) { //If found - save only lower case term.
                    String[] generalDocDetails = value.split(",");
                    term=term.toLowerCase();
                    dictionary.put(term, (Integer.parseInt(generalDocDetails[0]) + df) + "," + (Integer.parseInt(generalDocDetails[1]) + totalTF));
                }
                else{
                    value= dictionary.get(term);
                    if (value != null){
                        String[] generalDocDetails = value.split(",");
                        dictionary.put(term, (Integer.parseInt(generalDocDetails[0]) + df) + "," + (Integer.parseInt(generalDocDetails[1]) + totalTF));
                    }
                    else
                        dictionary.put(term, df + "," + totalTF);
                }
            }
        }
        try {
            bw.write(term+">"+postingRec);
            bw.newLine();
            bw.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read all the temp posting files that were created and merge them unto one sortedDocsByRank posting file
     */
    protected void mergePostings()  {
        File postingDir = new File(tempPostingPath);
        postingDir.mkdir();
        String [] filesList = postingDir.list();
        long rowPosition;
        Scanner[] scanners = new Scanner[filesList.length];
        String[][] linesToCompare = new String[filesList.length][2];
        String word = "";
        StringBuilder postingRec = new StringBuilder();
        boolean isLowerCase=false; // If the word is in lower case, save only as a lower case term.
        //Initialize arrays. Read first line of each file. Add terms into priority queue.
        for (int a=0; a<scanners.length; a++) {
            try {
                scanners[a] = new Scanner(new File(tempPostingPath+"\\"+filesList[a])).useDelimiter("\r\n");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            if (scanners[a].hasNext()) {
                linesToCompare[a] = scanners[a].next().split(">");
                if (Character.isLowerCase(linesToCompare[a][0].charAt(0))) {
                    if (!termsQueue.contains(linesToCompare[a][0])) {
                        if (termsQueue.contains(linesToCompare[a][0].toUpperCase())) {
                            termsQueue.remove(linesToCompare[a][0].toUpperCase());
                            termsQueue.add(linesToCompare[a][0]);
                        }
                        else
                            termsQueue.add(linesToCompare[a][0]);
                    }
                }
                else {
                    if (!termsQueue.contains(linesToCompare[a][0]) && !termsQueue.contains(linesToCompare[a][0].toLowerCase()))
                        termsQueue.add(linesToCompare[a][0]);
                }
            }
        }
        while (!termsQueue.isEmpty()) {
            isLowerCase=false;
            word = termsQueue.poll();
            // if (countries.contains(word.toUpperCase()))
            // numOfCountries++;
            for (int i = 0; i < linesToCompare.length; i++) {
                if (linesToCompare[i] != null && linesToCompare[i][0].toLowerCase().equals(word.toLowerCase())) {//compare between 2 words in lower case to prevent duplications
                    if(Character.isLowerCase(linesToCompare[i][0].charAt(0)))
                        isLowerCase=true;
                    postingRec.append(linesToCompare[i][1]);
                    if (scanners[i].hasNext()) {
                        linesToCompare[i] = scanners[i].next().split(">");
                        if (Character.isLowerCase(linesToCompare[i][0].charAt(0))) {
                            if (!termsQueue.contains(linesToCompare[i][0])) {
                                if (termsQueue.contains(linesToCompare[i][0].toUpperCase())) {
                                    termsQueue.remove(linesToCompare[i][0].toUpperCase());
                                    termsQueue.add(linesToCompare[i][0]);
                                }
                                else
                                termsQueue.add(linesToCompare[i][0]);
                            }
                        }
                        else {
                            if (!termsQueue.contains(linesToCompare[i][0]) && !termsQueue.contains(linesToCompare[i][0].toLowerCase()))
                                termsQueue.add(linesToCompare[i][0]);
                        }
                    }
                    else {
                        linesToCompare[i] = null;
                        scanners[i].close();
                    }
                }
            }//for
            try {
                String wordUpper = word.toUpperCase();
                if(citiesFromDocs.contains(wordUpper)){
                    String cityProperties=cityMap.get(wordUpper);
                    if (cityProperties == null) {
                        cityNotCapitalCount++;
                        cityProperties="";
                    }
                    rowPosition = writeCitiesPosting.getFilePointer(); //Saves the position by bytes before entering a new row. //After: use seek to get to the specific place.
                    writeCitiesPosting.write((cityProperties+postingRec+"\r\n").getBytes());
                    String value = cityDictionary.get(wordUpper);

                    if (value != null)
                        cityDictionary.put(wordUpper,value+","+ rowPosition);


                    //if(cityMap.containsKey(wordUpper))
                    //  checkForMostPopularCity(wordUpper, postingRec.toString());

                }
                else {
                    rowPosition = writeMergePostingWithPosition.getFilePointer(); //saves the position by bytes before entering a new row. //After: use seek to get to the specific place.
                    writeMergePostingWithPosition.write((postingRec + "\r\n").getBytes());
                    String value = dictionary.get(word);
                    if (value != null) {
                        if (isLowerCase)
                            word = word.toLowerCase();
                        dictionary.put(word, value + "," + rowPosition);
                    }
                }
                postingRec.setLength(0);
            }
            catch (IOException e) {
                System.out.println("for our use: problem with randomAccessFile in merging");
                e.printStackTrace();
            }
        }
        try {
            writeMergePostingWithPosition.close();
            writeCitiesPosting.close();
            // Delete all temporary files and folders.
            for(String tempfile:filesList){
                File toDelete=new File(tempPostingPath+"\\"+tempfile);
                toDelete.delete();
            }
            File folderToDelete =new File(postingDir.getPath());
            folderToDelete.delete();

        }
        catch (IOException e) {
            System.out.println("for our use: problem with randomAccessFile in merging");
            e.printStackTrace();
        }
        saveDictionariesToFile();
    }
    /*
    /**
     * This function checks which city name has the max number of occurrences in a document.
     * @param cityPostings
     */
/*
    private void checkForMostPopularCity(String term, String cityPostings) {
        String[] splittedCityString = cityPostings.split("\\{");
        StringBuilder maxTFCityDoc= new StringBuilder();
        // split city properties to get the max TF from all documents.
        String[] splittedCityDocumentString;
        for (String s : splittedCityString) {
            if(!s.equals("")&& s!=null) {
                splittedCityDocumentString = s.split("\\|");
                int cityTf = Integer.parseInt(splittedCityDocumentString[1]);
                if (maxTfCity < cityTf) {
                    maxTfCity = cityTf;
                    maxTFCityDoc.append("DocID: ");
                    maxTFCityDoc.append(splittedCityDocumentString[0]);
                    maxTFCityDoc.append(",Term: ");
                    maxTFCityDoc.append(term);
                    maxTFCityDoc.append(",TF: ");
                    maxTFCityDoc.append(splittedCityDocumentString[1]);
                    maxTFCityDoc.append(",Positions in doc: ");
                    maxTFCityDoc.append(splittedCityDocumentString[2]);
                    maxTfCityName=maxTFCityDoc.toString();
                    maxTFCityDoc.setLength(0);
                }
            }
        }
    }
    */
    // Returns the number of distinct terms in the whole corpus.
    public int getNumOfDistinctTerms(){
        return dictionary.size();
    }

    /**
     * Clears files saved for the current corpus: doc Details, dictionary and posting files(including stemming).
     */
    public void clearDictionary(){
        File documentDetailsFile = new File(postingFilesPath+"\\documentDetails.txt");
        if(documentDetailsFile.exists())
            documentDetailsFile.delete();
        File documentDetailsFileWithStem = new File(postingFilesPath+"\\documentDetailsWithStem.txt");
        if(documentDetailsFileWithStem.exists())
            documentDetailsFileWithStem.delete();
        File dictionaryFile= new File(postingFilesPath+"\\mainDictionary.txt");
        if(dictionaryFile.exists())
            dictionaryFile.delete();
        File dictionaryFileWithStem= new File(postingFilesPath+"\\mainDictionaryWithStem.txt");
        if(dictionaryFileWithStem.exists())
            dictionaryFileWithStem.delete();
        File mergeFile=new File(postingFilesPath+ "\\mergedFile.txt");
        if(mergeFile.exists())
            mergeFile.delete();
        File mergeFileWithStem=new File(postingFilesPath+ "\\mergedFileWithStem.txt");
        if(mergeFileWithStem.exists())
            mergeFileWithStem.delete();
        File cityMerge=new File(postingFilesPath+"\\mergedCityPostingFile.txt");
        if(cityMerge.exists())
            cityMerge.delete();
        File cityMergeStem=new File(postingFilesPath+"\\mergedCityPostingFileWithStem.txt");
        if(cityMergeStem.exists())
            cityMergeStem.delete();
        File cityDic=new File(postingFilesPath+"\\cityDictionary.txt");//Tal
        if(cityDic.exists())
            cityDic.delete();
        File cityDicStem=new File(postingFilesPath+"\\cityDictionaryWithStem.txt");//Tal
        if(cityDicStem.exists())
            cityDicStem.delete();
        File cityList=new File(postingFilesPath+"\\citiesList.txt");//Tal
        if(cityList.exists())
            cityList.delete();
        File mapTF=new File(postingFilesPath+"\\mapTF.txt");//Tal
        if(mapTF.exists())
            mapTF.delete();
        File cityListWithStem=new File(postingFilesPath+"\\citiesListWithStem.txt");//Tal
        if(cityListWithStem.exists())
            cityListWithStem.delete();
        File mapTFWithStem=new File(postingFilesPath+"\\mapTFWithStem.txt");//Tal
        if(mapTFWithStem.exists())
            mapTFWithStem.delete();
        File languageList=new File(postingFilesPath+"\\languagesList.txt");//Tal
        if(languageList.exists())
            languageList.delete();

        dictionary.clear();
        cityDictionary.clear();
    }

    /**
     * Builds map of capital cities and countries from API
     */

    public  void makeCityMapFromURL(){
        // city prop: "curency,country,population
        String curency = "";
        String country = "";
        String population = "";
        String city = "";
        StringBuilder cityProp = new StringBuilder();

        try {
            String urlPath = "http://restcountries.eu/rest/v2/all?fields=capital;name;population;currencies"; //return the specified fields.;
            URL url = new URL(urlPath);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int responsecode = connection.getResponseCode();

            BufferedReader bw = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String getInput = null;
            StringBuffer getContents = new StringBuffer(); //the properties of the city.
            while ((getInput = bw.readLine()) != null) {
                getContents.append(getInput);
            }
            bw.close();
            connection.disconnect();
            int startIndex;
            int endIndex;
            String[] allCapitals = getContents.toString().split("currencies");
            for (int i = 1; i < allCapitals.length; i++) {
                // System.out.println(allCapitals[i]);
                startIndex = allCapitals[i].indexOf("capital") + 10;
                endIndex = allCapitals[i].indexOf("\"", startIndex);
                if ((endIndex - startIndex) != 0)
                    city = allCapitals[i].substring(startIndex, endIndex);
                startIndex = allCapitals[i].indexOf("code") + 7;
                endIndex = allCapitals[i].indexOf("\"", startIndex);
                if ((endIndex - startIndex) != 0)
                    curency = allCapitals[i].substring(startIndex, endIndex);
                startIndex = allCapitals[i].indexOf("name", endIndex + 7) + 7;
                endIndex = allCapitals[i].indexOf("\"", startIndex);
                if ((endIndex - startIndex) != 0)
                    country = allCapitals[i].substring(startIndex, endIndex);
                startIndex = allCapitals[i].indexOf("population") + 12;
                endIndex = allCapitals[i].indexOf("}", startIndex);
                if ((endIndex - startIndex) != 0)
                    population = allCapitals[i].substring(startIndex, endIndex);
                //System.out.println(city + "," + curency + "," + country + "," + population);
                double num = Double.parseDouble(population);
                String populationNum;
                if(num<1000){
                    populationNum=""+num;
                }
                else if (num < 1000000) {
                    num = num / 1000;
                    populationNum = cutZero(num) + "K";
                }//1000000
                else if (num < 1000000000) {
                    num = num / 1000000;
                    populationNum = cutZero(num) + "M";
                }//1000000000
                else {
                    num = num / 1000000000;
                    populationNum = cutZero(num) + "B";
                }
                cityProp.setLength(0);
                cityProp.append(curency);
                cityProp.append(",");
                cityProp.append(country);
                cityProp.append(",");
                cityProp.append(populationNum);
                if(toStem) {//Tal start
                    stemmer.setTerm(city.toLowerCase());
                    stemmer.stem();
                    city = stemmer.getTerm();//Tal end
                }
                cityMap.put(city.toUpperCase(), cityProp.toString());
                //countries.add(country.toUpperCase());
            }
        } catch (IOException e) {
            e.printStackTrace();

        }
    }
    // This method checks if a double value is an integer( meaning that it is in a pattern of : ##.0) and cuts of the decimal.
    public static String cutZero(double num){
        if (num % 1 == 0) return String.valueOf(num).substring(0, String.valueOf(num).length() - 2);
        else return String.valueOf(num);
    }
    //Set a list if cities that showed up in the corpus.
    public void setCitiesFromDocs(ArrayList<String> citiesFromDocs) {
        this.citiesFromDocs=citiesFromDocs;
    }
}

// This class is used to compare terms in a couple of places during the program. sorts: numbers first and then letters.
class compareTerms implements Comparator<Object>{

    @Override
    public int compare(Object o1, Object o2) {
        int comparison = 0;
        String s1 = (String) o1;
        String s2 = (String) o2;
        for (int i = 0; i < s1.length() && i < s2.length(); i++) {
            char c1 = s1.toLowerCase().charAt(i);
            char c2 = s2.toLowerCase().charAt(i);
            if (Character.isDigit(c1) && Character.isLetter(c2))
                return -1;
            if (Character.isLetter(c1) && Character.isDigit(c2))//c1 is letter and c2 is not, so c2 comes first
                return 1;

            comparison = (int) c1 - (int) c2;

            if (comparison != 0) return comparison;
        }
        if (s1.length() > s2.length()) //s2 comes before s1
            return 1;
        else if (s1.length() < s2.length()) //s1 comes before s2
            return -1;
        else return 0;
    }
}
