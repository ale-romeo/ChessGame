package com.chess.chessgame;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;

public class ConfigReader {
    private String serverAddress;
    private int serverPort;

    public void readConfigFromXML(String filePath) {
        try {
            File xmlFile = new File(filePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            String serverAddress = doc.getElementsByTagName("address").item(0).getTextContent();
            String serverPort = doc.getElementsByTagName("port").item(0).getTextContent();

            // Utilizzare l'indirizzo e la porta del server ottenuti dal file XML
            setServerAddress(serverAddress);
            setServerPort(Integer.parseInt(serverPort));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int port) {
        this.serverPort = port;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }
}

