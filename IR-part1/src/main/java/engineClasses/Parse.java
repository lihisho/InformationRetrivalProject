package engineClasses;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
//import engineClasses.Searcher.*;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

public class Parse {
    HashMap<String, HashMap<String, ArrayList<Integer>>> wordsInDoc;
    HashMap<String, String> months;//all versions of month names.
    HashSet<String> stopWords; // list of stop words.
    private ArrayList<String> docsDetails; // Save docID, maxTF, distinct words in doc, length.
    private String[] splittedText;
    private Stemmer stemmer;
    private Indexer indexer;
    private int distinctWords;
    private int docCounter; // Counts the number of docs.
    private int wordIterator; // Goes over all the words in the doc.
    private String docID; // The file where the doc is in.
    private int maxTf; // The most common term in the current document.
    private boolean toStem; //If user chose to use stemming or not.
    private int totalNumOfDocs;
    private HashMap<String,HashMap<String,Double>> docDominantTFMap;//<docID,HashMap<term,tf>

    /**
     * Constructor for parser.
     */
    //we create parse object when get corpus for index propose and when get query
    public Parse(boolean stem, String postingPath,boolean isQuery) {
        //Initialize Data Structures and fields:
        wordsInDoc = new HashMap<>();
        months = new HashMap<>();
        docsDetails =new ArrayList<>();
        stemmer=new Stemmer();
        toStem= stem;
        if(!isQuery) {// make indexer object only if the propose is parse corpus and make inverted index
            indexer = new Indexer(toStem, postingPath);
            indexer.setPostingPath(postingPath);
        }
        initializeMonth();
        wordIterator = 0;
        maxTf = 0;
        distinctWords=0;
        docCounter=0;
        totalNumOfDocs=0;
        docID="null";
        docDominantTFMap=new HashMap<>();

    }
/*
    //queries:
    public int getNumOfTerms (){

        return wordsInDoc.size();
    }
    public int getNumOfCitiesNotCapital (){

        return indexer.getCityNotCapitalCount();
    }
    public int getNumOfCities(){
        return indexer.getNumOfCities();
    }
    public int getNumOfCountries(){
        return indexer.getNumOfCountries();
    }

    public int getNumberOfnumberTerms()
    {
        return indexer.num_of_NumberTerms;
    }
    public  String getMaxTfCityName(){
        return indexer.getMaxTfCityName();
    }
*/
    /**
     * Saves a list of stop words from a file.
     * @param stopWordsString -file to read from.
     * @throws IOException
     */
    public void setStopWords(String stopWordsString){
        stopWords=new HashSet<String>();
        String [] splittedString = stopWordsString.split("\r\n");
        for (int i=0; i<splittedString.length; i++)
            stopWords.add(splittedString[i]);
        // System.out.println(stopWords.size());
    }

    // Initialize the map with names of months.
    private void initializeMonth(){
        months.put("January", "01");
        months.put("Jan", "01");
        months.put("january", "01");
        months.put("jan", "01");
        months.put("JAN", "01");
        months.put("February", "02");
        months.put("Feb", "02");
        months.put("february", "02");
        months.put("feb", "02");
        months.put("FEBRUARY", "02");
        months.put("FEB", "02");
        months.put("March", "03");
        months.put("Mar", "03");
        months.put("march", "03");
        months.put("mar", "03");
        months.put("MARCH", "03");
        months.put("MAR", "03");
        months.put("April", "04");
        months.put("Apr", "04");
        months.put("april", "04");
        months.put("apr", "04");
        months.put("APRIL", "04");
        months.put("APR", "04");
        months.put("May", "05");
        months.put("may", "05");
        months.put("MAY", "05");
        months.put("June", "06");
        months.put("Jun", "06");
        months.put("june", "06");
        months.put("jun", "06");
        months.put("JUNE", "06");
        months.put("JUN", "06");
        months.put("July", "07");
        months.put("Jul", "07");
        months.put("july", "07");
        months.put("jul", "07");
        months.put("JULY", "07");
        months.put("JUL", "07");
        months.put("August", "08");
        months.put("Aug", "08");
        months.put("august", "08");
        months.put("aug", "08");
        months.put("AUGUST", "08");
        months.put("AUG", "08");
        months.put("September", "09");
        months.put("Sep", "09");
        months.put("september", "09");
        months.put("sep", "09");
        months.put("SEPTEMBER", "09");
        months.put("SEP", "09");
        months.put("October", "10");
        months.put("Oct", "10");
        months.put("october", "10");
        months.put("oct", "10");
        months.put("OCTOBER", "10");
        months.put("OCT", "10");
        months.put("November", "11");
        months.put("Nov", "11");
        months.put("november", "11");
        months.put("nov", "11");
        months.put("NOVEMBER", "11");
        months.put("NOV", "11");
        months.put("December", "12");
        months.put("Dec", "12");
        months.put("december", "12");
        months.put("dec", "12");
        months.put("DECEMBER", "12");
        months.put("DEC", "12");
    }
    // A function used to reset the relevant fields after parsing of  a bunch of files.
    public void resetsFields(){
        wordsInDoc.clear();
        docsDetails.clear();
        wordIterator=0;
        maxTf = 0;
        distinctWords = 0;
    }

    /**
     * Get the content between the TEXT label and send it to parse.
     *
     * @param docContent- Content between DOC label
     * @param DocNo - Doc ID in the directory.
     *
     */
    public void sendDocToParse(String docContent, String DocNo ){
        docCounter++;
        totalNumOfDocs++;
        docID=DocNo;
        StringBuilder documentDetails =new StringBuilder();
        Document doc = Jsoup.parse(docContent);
        String text = doc.select("TEXT").text();
        String city =  doc.select("F").select("[P=104]").text();
        if (city != null && !city.equals("")) {
            String[] cityName = city.split(" ");
            city = cityName[0].toUpperCase();
        }

        wordIterator = 0;
        maxTf = 0;
        distinctWords = 0;
        parser(text);
        makeMapWithTF();

// create a document details list.
        documentDetails.append(DocNo);
        documentDetails.append("|");
        documentDetails.append(maxTf);
        documentDetails.append("|");
        documentDetails.append(distinctWords);
        documentDetails.append("|");
        documentDetails.append(splittedText.length); // Length of each doc. Used for ranking.
        documentDetails.append("|");
        documentDetails.append(city);
        docsDetails.add(documentDetails.toString()); //Save information about the current document.
        if (docCounter == 8000) { // every number of docs, send them to indexer.
            sendTerms(false);
        }
    }

    /**
     * Saves a map with the TF of every entity in the doc.
     * Saves the 5 entities with the largest Tf rank.
     */
    public void makeMapWithTF()  { //TODO: probably stem enters words in lowercase that dont come up here.
        for(String term:wordsInDoc.keySet()) {
            if (Character.isUpperCase(term.charAt(0))) {
                //for (String docID : wordsInDoc.get(term).keySet()) {
                //  if (docID.equals(this.docID)) {
                if (wordsInDoc.get(term).containsKey(docID)) {
                    int tf = wordsInDoc.get(term).get(docID).size();
                    if (docDominantTFMap.containsKey(docID)) {//the doc is already in the map
                        HashMap<String, Double> list = docDominantTFMap.get(docID);
                        if (list.size() < 5) {
                            list.put(term, (double) tf);
                            docDominantTFMap.put(docID, list);
                        } else findPlaceOfWordAndInsert(term, tf, list);
                    } else {
                        HashMap<String, Double> list = new HashMap<>();
                        list.put(term, (double) tf);
                        docDominantTFMap.put(docID, list);
                    }
                }
            }
        }
    }




    // Find if a term is one of the 5 largest tf entities and insert it to the list if so.
    public void findPlaceOfWordAndInsert(String word,int tf,HashMap<String, Double> dominant){
        TreeMap <String,Double> sortedDocsByRank=new TreeMap<>(new valueComparator(dominant));
        sortedDocsByRank.putAll(dominant);
        String last="";
        for(String s:sortedDocsByRank.keySet())  {
            last=s;
        }
        if(tf>dominant.get(last)){
            dominant.remove(last);
            dominant.put(word,(double) tf);
        }
    }

    /**
     * Sends the current terms list to the indexer, in order to start creating posting files.
     * @param last
     */
    protected void sendTerms (boolean last){
        indexer.tempPosting(wordsInDoc, docsDetails,last);
        indexer.writeTfForDoc(docDominantTFMap,last);
        docDominantTFMap.clear();
        wordsInDoc.clear();
        docsDetails.clear();
        docCounter = 0;
        if (last) {
            indexer.makeCityMapFromURL();
            indexer.mergePostings();
        }
    }
    /**
     * Main function of the parse stage. First take of reduandant symbols, then check the kind of the token.
     * @param textToParse - String needed to parse.
     */
    public void parser (String textToParse){
        int i = -1;
        splittedText = (textToParse.replaceAll("[,|;|#|!|*|@|^|?]", "")).split("\\||\\s|\\[|\\]|- |--|\\=|\\:|\\+|\\<|\\>|\\(|\\)|\n|\t|\\{|\\}|\\...");// Remove all irellevant signs from string.

        while (wordIterator < splittedText.length) {
            i++;
            if (splittedText[wordIterator].equals("")) {
                wordIterator++;
                continue;
            }
            if (isStopWord(clearRedundant(splittedText[wordIterator]))) {
                wordIterator++;
                continue;
            }
            if (isDate()) {
                wordIterator++;
                continue;
            }

            if (isExpression()) {
                wordIterator++;
                continue;
            }

            if (isPrice()) {
                wordIterator++;
                continue;
            }

            if (isNumber()) {
                wordIterator++;
                continue;
            }

            if (checkCL(splittedText[wordIterator])) {
                wordIterator++;
                continue;
            }


        }//while
    }

    /**
     * Adds a new term to the temp dictionary. If it exists, counter++. else, add new value.
     *
     * @param term - A new term to add to the list.
     */
    public void addToMap (String term){
        int tf = 0;
        term = clearRedundant(term);
        if (!term.equals("") && !isStopWord(clearRedundant(term))) {
            if (wordsInDoc.containsKey(term)) { //If term is in the list.
                if (wordsInDoc.get(term).containsKey(docID)) { // if the term was found in the current document.
                    wordsInDoc.get(term).get(docID).add(wordIterator);
                } else {
                    ArrayList<Integer> positions = new ArrayList<>();
                    positions.add(wordIterator);
                    wordsInDoc.get(term).put(docID, positions);
                    distinctWords++;
                }
            } else {
                HashMap<String, ArrayList<Integer>> docList = new HashMap<>();
                ArrayList<Integer> positions = new ArrayList<>();
                positions.add(wordIterator);
                docList.put(docID, positions);
                wordsInDoc.put(term, docList);
                distinctWords++;
            }
            tf = wordsInDoc.get(term).get(docID).size();
            if (tf > maxTf) {
                maxTf = tf;
            }
        }
    }

    /**
     * Checks if a given term is in a price structure.
     * @return true if the term fits the price rules.
     */
    public boolean isPrice () {
        int currIterator = this.wordIterator;
        String numToInsert = "";
        double num;
        Object check = null;
        boolean hasNext = true;
        if (currIterator + 1 > splittedText.length - 1) hasNext = false;
        if (splittedText[currIterator].length() > 1 && splittedText[currIterator].charAt(0) == '$') { // Check every number that is a price, represented by $ sign.
            check = isDouble(splittedText[currIterator].substring(1));
            if (check != null) {
                num = (double) check;
                if (hasNext && (splittedText[currIterator + 1].equals( "million") || splittedText[currIterator + 1].equals("Million")  || splittedText[currIterator + 1].equals("MILLION") )) {
                    addToMap(cutZero(num) + " M Dollars");
                    currIterator++;
                    return true;
                } else if (hasNext && (splittedText[currIterator + 1].equals( "billion") || splittedText[currIterator + 1].equals( "Billion") || splittedText[currIterator + 1].equals( "BILLION"))) {
                    //numToInsert = splittedText[currIterator].substring(1, splittedText[currIterator].length() - 1);
                    num = num * 1000;
                    addToMap(cutZero(num) + " M Dollars");
                    currIterator++;
                    return true;
                } else if (hasNext && (splittedText[currIterator + 1].equals("trillion") || splittedText[currIterator + 1].equals( "Trillion") || splittedText[currIterator + 1].equals("TRILLION"))) {
                    //numToInsert = splittedText[currIterator].substring(1, splittedText[currIterator].length() - 1);
                    num = num * 1000000;
                    addToMap(cutZero(num) + " M Dollars");
                    currIterator++;
                    return true;
                }
                if (num <1000000) {//else there is no range word- exmp:$3000000=>3 M Dollars
                    numToInsert = cutZero(num);
                } else // If Price is larger than 1 Million, add th 'M' sign and divide by million.
                    numToInsert = cutZero(num / 1000000) + " M";
                addToMap(numToInsert + " Dollars");
                return true;
            }//$number
            return false;
        }//$450000
        else {
            check = isDouble(splittedText[currIterator]);
            if (check != null) {
                num = (double) check;
                if (num <1000000) {//Tal
                    numToInsert = cutZero(num);
                } else // If Price is larger than 1 Million, add th 'M' sign and divide by million.
                    numToInsert = cutZero(num / 1000000) + " M";
                // Check if the next token is a fraction and add it to the number before.
                if (hasNext && isFraction()) {
                    numToInsert = numToInsert + " " + splittedText[currIterator + 1];
                    currIterator++;
                }
                // Check the range word after each number and add the 'M' letter that represents Million.
                if (hasNext && (splittedText[currIterator + 1].equals("m") || splittedText[currIterator + 1].equals("million") || splittedText[currIterator + 1].equals( "Million") || splittedText[currIterator + 1].equals( "MILLION"))) {
                    numToInsert = numToInsert + " M";
                    currIterator++;
                } else if (hasNext && (splittedText[currIterator + 1].equals("bn") || splittedText[currIterator + 1].equals("billion") || splittedText[currIterator + 1].equals( "Billion") || splittedText[currIterator + 1].equals( "BILLION"))) {
                    num = num * 1000;
                    numToInsert = cutZero(num) + " M";
                    currIterator++;
                } else if (hasNext && (splittedText[currIterator + 1].equals("tr") || splittedText[currIterator + 1].equals("trillion") || splittedText[currIterator + 1].equals( "Trillion") || splittedText[currIterator + 1].equals( "TRILLION"))) {
                    num = num * 1000000;
                    numToInsert = cutZero(num) + " M";
                    currIterator++;
                }
                if (currIterator + 1 > splittedText.length - 1) hasNext = false;

                if (hasNext && (splittedText[currIterator + 1].equals ("U.S.") || splittedText[currIterator + 1].equals("u.s."))) {
                    currIterator++;
                    if (currIterator + 1 > splittedText.length - 1) hasNext = false;
                }
                if (hasNext && (splittedText[currIterator + 1].equals("Dollars") || splittedText[currIterator + 1].equals("Dollar") || splittedText[currIterator + 1] .equals( "dollars") || splittedText[currIterator + 1] .equals( "dollar") || splittedText[currIterator + 1].equals( "DOLLARS") || splittedText[currIterator + 1].equals( "DOLLAR"))) {
                    addToMap(numToInsert + " Dollars");
                    currIterator++;
                    wordIterator = currIterator;
                    return true;
                }

                if (hasNext && (splittedText[currIterator + 1].equals("Pounds") || splittedText[currIterator + 1].equals("pounds") || splittedText[currIterator + 1].equals( "POUNDS"))) {
                    addToMap(numToInsert + " Pounds");
                    currIterator++;
                    wordIterator = currIterator;
                    return true;
                }
            }//not number
            // Check if the tokens are number of pounds and save as one term.
            else if (splittedText[currIterator].equals( "Pounds" )|| splittedText[currIterator].equals( "pounds") || splittedText[currIterator].equals( "POUNDS")) {
                if (hasNext && splittedText[currIterator + 1].length()>0 && (splittedText[currIterator + 1].charAt(splittedText[currIterator + 1].length() - 1) == 'm')) {
                    check = isDouble(splittedText[currIterator].substring(0, splittedText[currIterator].length() - 2));
                    if (check != null) {
                        numToInsert = cutZero((double) check);
                        addToMap(numToInsert + " M Pounds");
                        currIterator++;
                        wordIterator = currIterator;
                        return true;
                    }
                } else if (hasNext) {
                    check = isDouble(splittedText[currIterator]);
                    if (check != null) {
                        addToMap(splittedText[currIterator] + " Pounds");
                        currIterator++;
                        wordIterator = currIterator;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isDate () {
        String token = "";
        String dayToInsert = "";
        int day = isInt(splittedText[wordIterator]);
        if (day > 0 && day <= 31) {
            if (day < 10) dayToInsert = "0" + day;
            else dayToInsert = "" + day;
            if (wordIterator + 1 < splittedText.length  && months.containsKey(splittedText[wordIterator + 1])) {
                token = months.get(splittedText[wordIterator + 1]) + "-" + dayToInsert;
                if (wordIterator + 2 <= splittedText.length - 1 && (splittedText[wordIterator + 2].matches("[0-9]+") && (splittedText[wordIterator + 2].length() == 2 || splittedText[wordIterator + 2].length() == 4))) {
                    token = token + "-" + splittedText[wordIterator + 2];
                    wordIterator++;
                }
                addToMap(token);
                wordIterator++;
                return true;
            }
        } else if (months.containsKey(splittedText[wordIterator])) {
            if (wordIterator + 1 <= splittedText.length - 1) {
                day = isInt((splittedText[wordIterator + 1]));
                if (day > 0 && day <= 31) {
                    if (day < 10) dayToInsert = "-0" + day;
                    else dayToInsert = "-" + day;
                } else if (splittedText[wordIterator + 1].matches("[0-9]+") && splittedText[wordIterator + 1].length() == 4)
                    token = splittedText[wordIterator + 1] + "-";
                token = token + months.get(splittedText[wordIterator]) + dayToInsert;
                addToMap(token);
                wordIterator++;
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the token is number- int or double
     *
     * @return True if the token is number. False if not.
     */
    public boolean isNumber () {
        int currIterator = wordIterator;
        double num;
        boolean hasNext = true;
        Object check = isDouble(splittedText[currIterator]);
        if (check != null) {
            num = (double) check;
            String numToInsert;
            if (isFraction()) {
                String[] parts = splittedText[currIterator + 1].split("/");
                num = num + (double) (Integer.parseInt(parts[0]) / Integer.parseInt(parts[1]));
                currIterator++;
            }
            if (currIterator + 1 > splittedText.length - 1) hasNext = false;
            if (num < 1000) {
                numToInsert = checkRangeWord(num, currIterator);
                if (numToInsert != null) {
                    currIterator++;
                    wordIterator = currIterator;
                    addToMap(numToInsert);
                } else if (hasNext && (splittedText[currIterator + 1].equals( "percentage") || splittedText[currIterator + 1].equals( "percent") || splittedText[currIterator + 1].equals( "Percent") || splittedText[currIterator + 1].equals( "Percentage") || splittedText[currIterator + 1].equals( "PERCENT") || splittedText[currIterator + 1].equals( "PERCENTAGE"))) {
                    addToMap(numToInsert + "%");
                    currIterator++;
                    wordIterator = currIterator;
                    return true;
                } else {
                    addToMap(cutZero(num));
                    wordIterator = currIterator;
                    return true;
                }
            }//1000
            else if (num < 1000000) {
                if (hasNext && (splittedText[currIterator + 1].equals( "percentage") || splittedText[currIterator + 1].equals( "percent") || splittedText[currIterator + 1].equals( "Percent") || splittedText[currIterator + 1].equals( "Percentage") || splittedText[currIterator + 1].equals( "PERCENT") || splittedText[currIterator + 1].equals( "PERCENTAGE"))) {
                    addToMap(num + "%");
                    currIterator++;
                    wordIterator = currIterator;
                    return true;
                } else {
                    num = num / 1000;
                    addToMap(cutZero(num) + "K");
                    wordIterator = currIterator;
                    return true;
                }
            }//1000000
            else if (num < 1000000000) {
                num = num / 1000000;
                addToMap(cutZero(num) + "M");
                wordIterator = currIterator;
                return true;
            }//1000000000
            else {
                num = num / 1000000000;
                addToMap(cutZero(num) + "B");
                wordIterator = currIterator;
                return true;
            }
            return true;
        }
        return false;

    }

    /**
     * Checks if a token is an expression with "-", or a range of numbers.
     * @return True if token was added to terms. False if not.
     */
    public boolean isExpression () {

        if (splittedText[wordIterator].contains("-")) {
            StringBuilder expToInsert = new StringBuilder();
            StringBuilder curr = new StringBuilder();
            String withoutPunct;
            boolean first_word;
            if ((splittedText[wordIterator].charAt(0)) == '-' || (splittedText[wordIterator].charAt((splittedText[wordIterator].length()) - 1) == '-'))
                return false;
            for (int i = 0; i < splittedText[wordIterator].length(); i++) {
                if (!(splittedText[wordIterator].charAt(i) == '-'))
                    curr.append(splittedText[wordIterator].charAt(i));
                if (splittedText[wordIterator].charAt(i) == '-' || splittedText[wordIterator].length() - 1 == i) {
                    if (isDouble(curr.toString()) != null) {
                        //addToMap(curr.toString());
                        expToInsert.append(curr);
                        expToInsert.append("-");

                        curr.setLength(0);
                    } else {
                        //checkCL(curr.toString());
                        withoutPunct = curr.toString().replaceAll("\\p{Punct}", "");
                        if (!withoutPunct.equals(""))
                            expToInsert.append(withoutPunct);
                        expToInsert.append("-");
                        curr.setLength(0);
                    }
                }
            }
            if (!expToInsert.toString().equals(""))
                expToInsert.deleteCharAt(expToInsert.length() - 1);
            if(!expToInsert.toString().equals("-"))  // Takes care of the term: "-" that inserts only - into dictionary.
                addToMap(expToInsert.toString());
            return true;

        } else {
            if (splittedText[wordIterator].equals( "Between") || splittedText[wordIterator].equals( "between"))
                if (wordIterator + 3 < splittedText.length)
                    if (isDouble(splittedText[wordIterator + 1]) != null && (splittedText[wordIterator + 2].equals( "and") || splittedText[wordIterator + 2].equals( "And") || splittedText[wordIterator + 2].equals( "AND")) && isDouble(splittedText[wordIterator + 3]) != null) {
                        addToMap(cutZero((double) isDouble(splittedText[wordIterator + 1])));
                        addToMap(cutZero((double) isDouble(splittedText[wordIterator + 3])));
                        addToMap(splittedText[wordIterator + 1] + "-" + splittedText[wordIterator + 3]);
                        return true;
                    }
        }
        return false;
    }

    /**
     * This function Checks if the given token is with Upper-case\Lower-case letters, and saves the token by the next rules:
     * 1) If word has been saved with Upper-case and inserting Lower-case: save only Lower case.
     * 2) If word has been saved with Upper-case and inserting Upper-case: save only Upper case.
     * 3) If word has been saved with Lower-case and inserting Lower-case: save only Lower case.
     * 4) If word has been saved with Lower-case and inserting Upper-case: save only Lower case.
     * 5) If word was never saved: insert all letters as the first letter in the word.
     * @param tokenToInsert
     * @return True- if word was added to hashmap.
     */
    public boolean checkCL (String tokenToInsert){
        boolean isUpperCase = false;
        if (toStem) {// true if the user press button
            if (Character.isUpperCase(tokenToInsert.charAt(0))) {
                isUpperCase = true;
                tokenToInsert = tokenToInsert.toLowerCase();
            }
            stemmer.setTerm(tokenToInsert);
            stemmer.stem();
            tokenToInsert = stemmer.getTerm();
            if (isUpperCase) tokenToInsert.toUpperCase();
        }
        tokenToInsert = tokenToInsert.replaceAll("\\p{Punct}", "");
        if (tokenToInsert.equals("")) return true;
        if (Character.isUpperCase(tokenToInsert.charAt(0))) {
            if (wordsInDoc.containsKey(tokenToInsert.toLowerCase())) addToMap(tokenToInsert.toLowerCase());
            else if (wordsInDoc.containsKey(tokenToInsert.toUpperCase())) addToMap(tokenToInsert.toUpperCase());
            else addToMap(tokenToInsert.toUpperCase());
        } else {
            if (wordsInDoc.containsKey(tokenToInsert.toUpperCase())) {
                HashMap<String, ArrayList<Integer>> value = wordsInDoc.remove(tokenToInsert.toUpperCase());
                wordsInDoc.put(tokenToInsert.toLowerCase(), value);
            }
            addToMap(tokenToInsert.toLowerCase());
        }
        return true;
    }
    //checks if a given term is a stop word - return true.
    private boolean isStopWord (String word){
        if (stopWords.contains(word.toLowerCase())) return true;
        return false;
    }

    /**
     * Returns the number to insert and checks if there is .00 redundant
     *
     * @param num- double number to test
     * @return string without redundants
     */
    public String cutZero ( double num){
        if (num % 1 == 0) return String.valueOf(num).substring(0, String.valueOf(num).length() - 2);
        else return String.valueOf(num);
    }

    /**
     * Checks if a given string is an int .
     * @param toCheck - string to check.
     * @return If the given string is a number, return it. else return -1.
     */
    public int isInt (String toCheck){
        try {
            int num = Integer.parseInt(toCheck);
            return num;
        } catch (Exception e) {
            return -1;
        }
    }
    // Checks if a given String is of type double (or int - will return the double value).
    public Object isDouble (String toCheck){
        try {
            Double num = Double.parseDouble(toCheck);
            return num;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Check if a given token is a fraction.
     * @return True if fraction, False if not.
     */

    public boolean isFraction () {
        try {
            if (wordIterator + 1 < splittedText.length - 1) {
                if (wordIterator + 1 <= splittedText.length - 1 && splittedText[wordIterator + 1].contains("/")) {
                    String[] parts = splittedText[wordIterator + 1].split("/");
                    if (parts.length == 2) { //the next part is only to check if parseInt is working or failing.
                        int mone = Integer.parseInt(parts[0]);
                        int mechane = Integer.parseInt(parts[1]);
                        if (mone != 0 && mechane != 0) return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check the range word after each number and add the compatible letter.
     * @param num- the number in the text to add to the list
     * @return true or false if there is a range word
     */
    private String checkRangeWord ( double num, int currIterator){
        String numToInsert;
        if (currIterator + 1 > splittedText.length - 1) return null;
        numToInsert = cutZero(num);

        if (splittedText[currIterator + 1].equals("Thousand") || splittedText[currIterator + 1].equals("thousand") || splittedText[currIterator + 1].equals("THOUSAND")) {
            return (numToInsert + "K");
        }
        if (splittedText[currIterator + 1].equals("Million") || splittedText[currIterator + 1].equals("million") || splittedText[currIterator + 1].equals("MILLION")) {
            return (numToInsert + "M");
        }
        if (splittedText[currIterator + 1].equals("Billion") || splittedText[currIterator + 1].equals("billion") || splittedText[currIterator + 1].equals("BILLION")) {
            return (numToInsert + "B");
        }
        if (splittedText[currIterator + 1].equals("Trillion") || splittedText[currIterator + 1].equals("trillion") || splittedText[currIterator + 1].equals("TRILLION")) {
            num = num * 100;
            DecimalFormat f = new DecimalFormat("#0.00");
            String s = f.format(Double.valueOf(num));
            if (s.charAt(s.length() - 1) == '0' && s.charAt(s.length() - 2) == '0')
                numToInsert = s.substring(0, s.length() - 3);
            //numToInsert = cutZero(num);
            return (numToInsert + "B");
        }
        return null;
    }


    // Clears redundant symbols in a string.
    private String clearRedundant (String toClear){
        if (toClear != null && toClear.endsWith(".")) {
            toClear = toClear.substring(0, toClear.length() - 1);
        }
        return toClear;
    }


    public int getTotalNumberOfDocs(){
        return totalNumOfDocs;
    }

    public int getNumOfDistinctTerms(){
        return indexer.getNumOfDistinctTerms();
    }
    //Passes the indexer a function to delete files of the index.
    public void clearDictionary(){
        indexer.clearDictionary();
    }

    public void setCitiesFromDocs(ArrayList<String> citiesFromDocs) {
        indexer.setCitiesFromDocs(citiesFromDocs);
    }
}


