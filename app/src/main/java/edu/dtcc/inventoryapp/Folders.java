package edu.dtcc.inventoryapp;

import java.util.ArrayList;

/**
 * Created by Kevin on 11/27/17.
 */

public class Folders {
    static private String folderID;
    static private String folderName;
    static private String folderParent;

    public Folders(String folderID, String folderName, String folderParent) {
        this.folderID = folderID;
        this.folderName = folderName;
        this.folderParent = folderParent;
    }

    public String getfolderID() {
        return folderID;
    }
    public void setfolderID(String folderID) {
        this.folderID = folderID;
    }
    public String getfolderName() {
        return folderName;
    }
    public void setfolderName(String folderName) {
        this.folderName = folderName;
    }
    public String getfolderParent() {
        return folderParent;
    }
    public void setfolderParent(String folderParent) {
        this.folderParent = folderParent;
    }



}
