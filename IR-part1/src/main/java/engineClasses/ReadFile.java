package engineClasses;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;

public class ReadFile {
    private Stemmer stemmer;

    Parse parser;
    String DocNo;
    ArrayList<String> languages;
    ArrayList<String> citiesFromDocs;
    boolean toStem;
    ArrayList<String> docTitles;

    //Constructor
    public ReadFile(boolean stem , String postingPath){
        stemmer=new Stemmer();//Tal
        parser= new Parse(stem, postingPath,false);//Tal- if readFile create file so sure it is not query
        DocNo = null;
        //  docNum=0;
        languages=new ArrayList<>();
        citiesFromDocs= new ArrayList<>();
        docTitles = new ArrayList<>();
        toStem=stem;
    }
    /**
     * Recursivly sends all files in the given directory.
     * @throws IOException
     */
    public void listFilesForFolder(String file) throws IOException {
        // create new file
        File fileToRead = new File(file);
        for (final File fileEntry : fileToRead.listFiles()) {
            if (fileEntry.isDirectory()) { //If the file is a folder- open recursively.
                listFilesForFolder(fileEntry.getPath());
            }
            else if (fileEntry.getName() == "stop_words.txt"){
                continue;
            }
            else { //File to read.
                DocNo = fileEntry.getName();
                String encoded = new String(Files.readAllBytes((Paths.get(fileEntry.getPath())))); // Reads the file content and returns it as string.
                seperateDocs(encoded);

            }
        }
    }
/*
    //queries:
    public int getNumberOfnumberTerms()
    {
        return parser.getNumberOfnumberTerms();
    }
    public int getNumOfCitiesNotCapital()
    {
        return parser.getNumOfCitiesNotCapital();
    }

    public int getNumOfCities(){
        return parser.getNumOfCities();
    }
    public int getNumOfCountries(){
        return parser.getNumOfCountries();
    }

    public  String getMaxTfCityName(){
        return parser.getMaxTfCityName();
    }
*/
    /**
     * This function gets the content of one file and separates it to documents.
     * @param fileContent - The entire text from one file. (contains a few docs)
     * @throws IOException
     */
    private void seperateDocs(String fileContent) throws IOException {
        Document doc = Jsoup.parse(fileContent);
        Elements links = doc.select("DOC");
        for(Element link: links ) {
            DocNo = link.select("DOCNO").text();

            parser.sendDocToParse(link.html(), DocNo);
        }
    }

    /**
     * Creates a list of cities that show up in the file tags.
     * @param file
     * @throws IOException
     */

    public void createCitiesList(String file) throws IOException {
        // create new file
        File fileToRead = new File(file);
        for (final File fileEntry : fileToRead.listFiles()) {
            if (fileEntry.isDirectory()) { //If the file is a folder- open recursively.
                createCitiesList(fileEntry.getPath());
            }
            else if (fileEntry.getName() == "stop_words.txt"){
                continue;
            }
            else { //File to read.
                DocNo = fileEntry.getName();
                String encoded = new String(Files.readAllBytes((Paths.get(fileEntry.getPath())))); // Reads the file content and returns it as string.
                addCity(encoded);
            }
        }
    }

    /**
     * Get city from the right tag in the document and add to to the map if it is not there yet.
     * @param fileContent - all text between
     */
    private void addCity (String fileContent){
        String language;
        Document doc = Jsoup.parse(fileContent);
        Elements links = doc.select("DOC");
        String city;
        for(Element link: links ) {
            city = link.select("F").select("[P=104]").text();
            if (city != null && !city.equals("")) {
                String[] cityName = city.split(" ");
                city = cityName[0].replaceAll("[\\(|\\)|\\ ]", "" );
                if (toStem) {//if the user choose stem, in order to see if equals to terms in documents we add the city stem word
                    stemmer.setTerm(city.toLowerCase());
                    stemmer.stem();
                    city = stemmer.getTerm();//Tal end
                }
                city = city.toUpperCase();
                if (!citiesFromDocs.contains(city) || citiesFromDocs == null)
                    citiesFromDocs.add(city);

            }
            language = link.select("F").select("[P=105]").text();
            language = language.replaceAll("[,|\\ ]", "" ).toLowerCase();
            if (!languages.contains(language)){
                languages.add(language);
            }


        }
    }
    //Returns a list of languages (from document tags).
    public ArrayList<String> getLanguageList() {
        return languages;
    }

    /**
     * Saves a list of stop words from a file. And cities from each document tag.
     * @param file -file to read from.
     * @throws IOException
     */
    public void setStopWordsAndCities(String file) throws IOException {
        parser.setCitiesFromDocs(citiesFromDocs);
        String stopWordString = new String(Files.readAllBytes((Paths.get(file + "\\stop_words.txt")))); // Reads the file content and returns it as string.
        parser.setStopWords(stopWordString);
    }
    // function to connect between myModel and indexer.
    public void sendTerms(boolean b) {
        parser.sendTerms(b);
    }
    // Getters:
    public int getNumOfDistinctTerms(){
        return parser.getNumOfDistinctTerms();
    }

    public int getTotalNumOfDocs(){
        return parser.getTotalNumberOfDocs();
    }
    //send the parser to clear dictionaries in the indexer.
    public void clearDictionary(){
        parser.clearDictionary();
    }

 //Saves a city list to file in order to load it in the next run.
    public void saveCitiesToFile(String postingFilesPath) {
        String path;
        if(toStem)
            path = postingFilesPath + "\\citiesListWithStem.txt";
        else
            path = postingFilesPath + "\\citiesList.txt";
        File citiesFile = new File(path);
        saveListsToFile(citiesFile,citiesFromDocs);

    }
    //Saves a language list to file in order to load it in the next run.
    public void savelanguagesToFile(String postingFilesPath) {
        String path;
        path = postingFilesPath + "\\languagesList.txt";
        File langFile = new File(path);
        saveListsToFile(langFile , languages);
    }
    public void saveTitlesToFile(String postingFilesPath) {
        String path;
        path = postingFilesPath + "\\docTitles.txt";
        File docTitlesFile = new File(path);
        saveListsToFile(docTitlesFile , docTitles);

    }

    //Saves lists to file in order to load them in the next run.
    public void saveListsToFile (File fileToWrite, ArrayList<String> list){
        try {
            FileWriter fw = new FileWriter(fileToWrite, true);
            BufferedWriter bw = new BufferedWriter(fw);
            for (String s : list) {
                bw.write(s);
                bw.newLine();
                bw.flush();
                bw.flush();
            }
            fw.close();
            bw.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
