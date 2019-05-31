package sample;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.stage.Modality;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ResourceBundle;
import javafx.stage.Stage;
import jmtp.*;

//TODO: when copying files over, make a folder for each new playlist?
//TODO: able to delete playlists + music from phone?

public class Controller implements Initializable {
    private String iTunesPath = "C:\\Users\\" + System.getProperty("user.name") + "\\Music\\iTunes";
    private String outputPath = "C:\\Users\\" + System.getProperty("user.name") + "\\Desktop\\test output";

    private int totalItems = 0;
    private int countSelected = 0;

    private PortableDeviceManager deviceManager;
    private PortableDevice device;

    @FXML  // fx:id="lvPlaylists";
    private ListView<ListViewCheckItem> lvPlaylists = new ListView();

    @FXML  // fx:id="btnSettings";
    private Button btnSettings;

    @FXML  // fx:id="lblPath";
    private Label lblPath;

    @FXML // fx:id="prgsBar";
    private ProgressBar prgsBar;

    @FXML  // fx:id="lblPhone";
    private Label lblPhone;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadListView();
        changeLabelPath(iTunesPath);
    }

    public void loadListView() {
        detectPhone();
        lvPlaylists.getItems().clear();
        lvPlaylists.setCellFactory(CheckBoxListCell.forListView(ListViewCheckItem::onProperty));

        try{
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(iTunesPath+"\\iTunes Music Library.xml"));
            document.getDocumentElement().normalize();
            System.out.println("Root element :" + document.getDocumentElement().getNodeName());

            NodeList nList = document.getElementsByTagName("dict");
            System.out.println(nList.getLength());
            System.out.println("----------------------------");
            for(int i = 0; i < nList.getLength(); i++){
                Node nNode = nList.item(i);

                //System.out.println("\nCurrent Element :" + nNode.getNodeName());
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element eElement = (Element) nNode;
                    //System.out.println(eElement.getTextContent());
                    //System.out.println(eElement.getElementsByTagName("key").item(0).getTextContent());
                    //System.out.println(eElement.getElementsByTagName("integer").item(0).getTextContent());
                    if(eElement.getElementsByTagName("key").item(0).getTextContent().equalsIgnoreCase("playlist id")){
                        String playlistName = eElement.getElementsByTagName("string").item(1).getTextContent();
                        int numOfTracks = 0;
                        switch(playlistName){
                            case "TV Shows":
                            case "Audiobooks":
                            case "Genius":
                            case "Podcasts":
                            case "Movies":
                            case "Music":
                            case "Downloaded":
                            {
                                break;
                            }
                            default:
                            {
                                //System.out.println(eElement.getElementsByTagName("array").item(0).getTextContent());
                                NodeList nList2 = eElement.getElementsByTagName("array");
                                for(int j = 0; j < nList2.getLength(); j++) {
                                    Node nNode2 = nList2.item(j);
                                    if(nNode2.getNodeType() == nNode2.ELEMENT_NODE){
                                        Element id = (Element) nNode2;
                                        String stringIDs = id.getTextContent();
                                        String filtered = stringIDs.replaceAll("[^0-9]"," ");
                                        //System.out.println(filtered);
                                        String delim = "[ ]+";
                                        String[] tokenIDs = filtered.trim().split(delim);
                                        //System.out.println(tokenIDs.length);
                                        for ( String entry : tokenIDs ) {
                                            //System.out.println(entry);
                                            numOfTracks++;
                                        }
                                        System.out.println(eElement.getElementsByTagName("string").item(1).getTextContent());

                                        // Each entry contains on/off, playlist name, number of tracks, string array of track IDs
                                        ListViewCheckItem newItem = new ListViewCheckItem(eElement.getElementsByTagName("string").item(1).getTextContent(), false,numOfTracks, tokenIDs);
                                        newItem.onProperty().addListener((obs, wasOn, isNowOn) -> {
                                            System.out.println(newItem.getName() + " changed on state from " + wasOn + " to " + isNowOn);
                                        });
                                        lvPlaylists.getItems().add(newItem);
                                    }
                                }
                                break;
                            }
                        }
                    }

                }
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
/*
        //--- per entry
        ListViewCheckItem a = new ListViewCheckItem("test", false);
        // JUST FOR CHECKING, NOT NEEDED TO RUN CHECK MARK
        a.onProperty().addListener((obs, wasOn, isNowOn) -> {
            System.out.println(a.getName() + " changed on state from " + wasOn + " to " + isNowOn);
        });
        lvPlaylists.getItems().add(a);
        //---
 */
    }

    public void openSettingsWindow(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("settingsWindow.fxml"));
            Parent root1 = fxmlLoader.load();
            Stage stage = new Stage();
            stage.initOwner(btnSettings.getScene().getWindow());
            stage.setTitle("AttuneMe");
            stage.setScene(new Scene(root1));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);//This is important if you don't want the user to interact with other windows
            stage.showAndWait();

            SettingsController controller = fxmlLoader.getController();
            if(controller.getText().length() > 3){
                changeLabelPath(controller.getText());
                loadListView();
            }
            //stage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    public void transferPlaylists(){
        // if no items are selected, don't do anything
        for (ListViewCheckItem entry : lvPlaylists.getItems()){
            if(entry.isOn()){
                countSelected++;
            }
        }
        if (countSelected == 0 || deviceManager.getDevices().length == 0) {
            return;
        }

        PortableDeviceFolderObject targetFolder = null;
        try{
            deviceManager = new PortableDeviceManager();
            device = deviceManager.getDevices()[0];
            device.open();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(iTunesPath+"\\iTunes Music Library.xml"));
            document.getDocumentElement().normalize();

            for (ListViewCheckItem entry : lvPlaylists.getItems()){
                totalItems += entry.getTracks();
            }
            double progressTick = 100.0/totalItems;
            for(ListViewCheckItem entry : lvPlaylists.getItems()){
                if(entry.isOn()){
                    String m3uInitial = "#EXTM3U";
                    String edit = entry.getName().replaceAll("/","");
                    System.out.println(edit);
                    Files.write(Paths.get(iTunesPath+"\\"+ edit + ".m3u"),m3uInitial.getBytes(),StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                    //Files.write(Paths.get(iTunesPath+"\\"+ entry.getName() + ".m3u"),m3uInitial.getBytes(),StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                    NodeList nList = document.getElementsByTagName("dict");
                    System.out.println("----------------------------");
                    for ( String trackID : entry.getIDs() ) {
                        for(int i = 0; i < nList.getLength(); i++){
                            Node nNode = nList.item(i);
                            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                                Element eElement = (Element) nNode;
                                if(eElement.getElementsByTagName("integer").item(0).getTextContent().equalsIgnoreCase(trackID)
                                        && eElement.getElementsByTagName("key").getLength() >= 10){
                                    String filePath = eElement.getElementsByTagName("string").item(eElement.getElementsByTagName("string").getLength()-1).getTextContent();
                                    filePath = filePath.trim().replaceAll("\\bfile://localhost/\\b"," ");
                                    filePath = filePath.trim().replaceAll("%20"," ");
                                    // in case of non-english characters in XML file, reconvert it
                                    filePath = java.net.URLDecoder.decode(filePath, StandardCharsets.UTF_8);
                                    //System.out.println("found " + filePath);
                                    File srcFile = new File(filePath);

                                    // Modified From https://stackoverflow.com/questions/29645275/desktop-java-app-copy-and-transfer-android-data-via-usb
                                    // Iterate over deviceObjects
                                    if(targetFolder == null){
                                        for (PortableDeviceObject object : device.getRootObjects()) {
                                            // If the object is a storage object
                                            if (object instanceof PortableDeviceStorageObject) {
                                                PortableDeviceStorageObject storage = (PortableDeviceStorageObject) object;
                                                for (PortableDeviceObject o2 : storage.getChildObjects()) {
                                                    //TODO: change to Music folder after tests are done
                                                    if(o2.getOriginalFileName().equalsIgnoreCase("Music"))
                                                    {
                                                        PortableDeviceFolderObject storage2 = (PortableDeviceFolderObject) o2;
                                                        // check if folder contains other folders already
                                                        if(storage2.getChildObjects().length>0){
                                                            boolean found = false;
                                                            // search if folder contains this playlist's folder
                                                            for(PortableDeviceObject o3 : storage2.getChildObjects()){
                                                                //if (o3.getName().equalsIgnoreCase(entry.getName())){
                                                                if (o3.getName().equalsIgnoreCase(edit)){
                                                                    System.out.println("Folder already in device");
                                                                    targetFolder = (PortableDeviceFolderObject) o3;
                                                                    found = true;
                                                                    break;
                                                                }
                                                            }
                                                            // if not found, create it
                                                            if(!found){
                                                                //targetFolder = storage2.createFolderObject(entry.getName());
                                                                targetFolder = storage2.createFolderObject(edit);
                                                            }
                                                        }
                                                        // no folders inside, create new folder
                                                        else{
                                                            targetFolder = storage2.createFolderObject(edit);
                                                            //targetFolder = storage2.createFolderObject(entry.getName());
                                                        }
                                                        break;
                                                    }
                                                }
                                                break;
                                            }
                                        }
                                    }
                                    String m3uEntry = "\n./"+srcFile.getName();
                                    //String m3uEntry = "\n./"+entry.getName()+"/"+srcFile.getName();
                                    Files.write(Paths.get(iTunesPath+"\\"+ edit + ".m3u"),m3uEntry.getBytes(),StandardOpenOption.CREATE,StandardOpenOption.APPEND);
                                    //Files.write(Paths.get(iTunesPath+"\\"+ entry.getName() + ".m3u"),m3uEntry.getBytes(),StandardOpenOption.CREATE,StandardOpenOption.APPEND);
                                    copyFileFromComputerToDeviceFolder(srcFile,targetFolder);
                                    prgsBar.setProgress(prgsBar.getProgress()+progressTick);
                                }
                            }
                        }
                    }
                    String m3uEnd = "\n";
                    Files.write(Paths.get(iTunesPath+"\\"+ edit + ".m3u"),m3uEnd.getBytes(),StandardOpenOption.CREATE,StandardOpenOption.APPEND);
                    //Files.write(Paths.get(iTunesPath+"\\"+ entry.getName() + ".m3u"),m3uEnd.getBytes(),StandardOpenOption.CREATE,StandardOpenOption.APPEND);
                    File srcFile = new File(iTunesPath+"\\"+ edit + ".m3u");
                    //File srcFile = new File(iTunesPath+"\\"+ entry.getName() + ".m3u");
                    // m3u file, transfer to phone then delete from computer
                    copyFileFromComputerToDeviceFolder(srcFile,targetFolder);
                    Files.deleteIfExists(Paths.get(iTunesPath+"\\"+ edit + ".m3u"));
                    //Files.deleteIfExists(Paths.get(iTunesPath+"\\"+ entry.getName() + ".m3u"));
                    targetFolder = null;
                }
            }
            prgsBar.setProgress(100);
            totalItems = 0;
            countSelected = 0;
            doneMessageBox();
            device.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void detectPhone(){
        deviceManager = new PortableDeviceManager();
        if(deviceManager.getDevices().length > 0){
            System.out.println("entering");
            device = deviceManager.getDevices()[0];
            device.open();
            lblPhone.setText("Phone: " + device.getModel());
            device.close();
        }
        else{
            lblPhone.setText("No Phone Detected");
        }
    }

    // Modified From https://stackoverflow.com/questions/29645275/desktop-java-app-copy-and-transfer-android-data-via-usb
    private static void copyFileFromComputerToDeviceFolder(File srcFile,PortableDeviceFolderObject targetFolder)
    {
        BigInteger bigInteger1 = new BigInteger("123456789");
        try {
            if(targetFolder.getChildObjects().length == 0){
                targetFolder.addAudioObject(srcFile, "jj", "jj", bigInteger1);
            }
            else{
                for(PortableDeviceObject entry : targetFolder.getChildObjects()){
                    if(entry.getOriginalFileName().equalsIgnoreCase(srcFile.getName())){
                        System.out.println("File: " + srcFile.getName() + " is already on device.");
                        return;
                    }
                }
                targetFolder.addAudioObject(srcFile, "jj", "jj", bigInteger1);
            }
        } catch (Exception e) {
            System.out.println("Exception e = " + e);
        }
    }

    private void changeLabelPath(String newPath){
        iTunesPath = newPath;
        lblPath.setText(iTunesPath);
    }

    private void doneMessageBox(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("completeWindow.fxml"));
            Parent root1 = fxmlLoader.load();
            Stage stage = new Stage();
            //stage.initOwner(sda.getScene().getWindow());
            stage.setTitle("AttuneMe");
            stage.setScene(new Scene(root1));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);//This is important if you don't want the user to interact with other windows
            stage.showAndWait();
            prgsBar.setProgress(0);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
