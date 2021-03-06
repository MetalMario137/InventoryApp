package edu.dtcc.inventoryapp;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private Context context;
    private Activity activity;
    private DialogBox dialogBox;
    private FolderContents folderContents;
    private FolderData folderData;
    private FileData fileData;
    private FileMenu fileMenu;
    private DatabaseAdder dbAdder;
    private DatabaseReader dbReader;
    private DatabaseUpdater dbUpdater;
    private DatabaseDeleter dbDeleter;
    private String rootDirectory;
    private String currentDirectory;
    private String itemType;
    private String item;
    private int indexOfItem;

    //Used for listView of content names
    ArrayList<String> list = new ArrayList<>();

    // custom list adapter that will handle the list
    CustomListAdapter adapter;

    // ListView for displaying the list
    ListView listView;

    /* Main is primarily used for managing buttons, dialog box buttons, and dealing with the ListView */
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        updateContextAndActivity();

        folderData = new FolderData();
        folderData.setParent("Home");
        updateListView();
    }

    private void createListView(String directory){
        //Get home directory values
        dbReader = new DatabaseReader(context);
        folderContents = dbReader.getFolderContent(directory);
        list = (ArrayList<String>) folderContents.getNames().clone();

        // initialize custom adapter
        adapter = new CustomListAdapter(list, folderContents, context, activity);

        // initialize the list and set the adapter
        listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(adapter);

        // onClick listener for the listView (not the edit buttons)
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                //determines what is clicked, and takes the correct action
                String selectedItem = list.get(position);
                boolean isFolder = folderContents.getTypes().get(position).equals("Folder");

                    if (isFolder)
                        openFolderContents(selectedItem);
                    else
                        openFileContents(selectedItem);
            }
        });
    }

    private void openFolderContents(String directory) {
        folderData = new FolderData();
        updateRoot(directory);
        updateListView();
    }

    private void openFileContents(String file) {
        setContentView(R.layout.file_display);
        fileMenu = new FileMenu(context, activity);
        fileMenu.displayFileData(file);
        fileMenu.openFile();
    }

    private void updateContextAndActivity(){
        context = getApplicationContext();
        activity = MainActivity.this;
    }
    private void updateListView(){
        String currentParent = folderData.getParent();
        createListView(currentParent);
        updateActionBar();
    }
    private void updateRoot(String rootDirectory) {folderData.setParent(rootDirectory);}

    private void updateActionBar() {
        getSupportActionBar().setTitle(folderData.getParent());
    }

    private void getRootDirectory(){
        if (!(folderData.getParent().equals("Home"))) {
            if (fileMenu.fileIsOpen())
            {
                fileMenu.closeFile();
            }
            else
            {
                currentDirectory = folderData.getParent();
                rootDirectory = dbReader.findPreviousDirectory(currentDirectory);
                updateRoot(rootDirectory);
            }

            setContentView(R.layout.activity_main);
            updateListView();
        }
        else
        {
            setContentView(R.layout.activity_main);
            updateContextAndActivity();
            updateRoot("Home");
            updateListView();
        }
    }

    private void getItemAndType(){
        indexOfItem = adapter.getCurrentIndex();
        itemType = folderContents.getTypes().get(indexOfItem);
        item = folderContents.getNames().get(indexOfItem);
    }

    // === Dialog box methods below === //
    private void newDialogBox(int layout){
        dialogBox = new DialogBox(activity);
        dialogBox.newDialogBox(layout);
    }

    public void settingsBoxButtons(View button) {
        getItemAndType();
        dbDeleter = new DatabaseDeleter(context);
        dbUpdater = new DatabaseUpdater(context);
        dialogBox = adapter.getDialogBox();

        if (itemType.equals("Folder")) {
            switch (button.getId()) {
                case R.id.editBtn:
                    dialogBox.endDialogBox();
                    newDialogBox(R.layout.rename_folder_dialog);

                    break;
                case R.id.deleteBtn:
                    dbDeleter.deleteFolder(item);
                    updateListView();
                case R.id.closeBtn:
                    dialogBox.endDialogBox();
                    break;
            }
        } else {
            switch (button.getId()) {
                case R.id.editBtn: theToasting("Function still in development");
                    break;
                case R.id.deleteBtn:
                    dbDeleter.deleteFile(item);
                    updateListView();
                case R.id.closeBtn:
                    dialogBox.endDialogBox();
                    break;
            }
        }
    }

    public void createContentBoxButtons(View button){
        switch (button.getId()) {
            case R.id.create_new_content:
                newDialogBox(R.layout.create_content_dialog);
                break;
            case R.id.create_folder:
                dialogBox.endDialogBox();
                newDialogBox(R.layout.create_folder_dialog);
                break;
            case R.id.create_file:
                setContentView(R.layout.create_file);
            case R.id.close:
                dialogBox.endDialogBox();
                break;
        }
    }

    public void createFolderBoxButtons(View button) {
        switch (button.getId()) {
            case R.id.submit_new_file_btn: createNewFolder(); break;
            case R.id.cancelBtn: dialogBox.endDialogBox(); break;
        }
    }

    public void renameFolderBoxButtons(View button) {
        updateContextAndActivity();
        switch (button.getId()) {
            case R.id.submit_new_file_btn:
                try {
                    dialogBox.collectNewFolderName(folderData);
                    folderData.setOldName(item);
                    dbUpdater.updateFolderName(folderData);
                    updateListView();
                } catch (Exception e){
                    System.out.println(e.getMessage());
                }
            case R.id.cancelBtn: dialogBox.endDialogBox(); break;
        }
    }

    public void createFileMenu(View button) {
        String currentParent = folderData.getParent();
        fileMenu = new FileMenu(context, activity);
        switch (button.getId()) {
            case R.id.submit_new_file_btn: fileMenu.createNewFile(currentParent);
            case R.id.cancel_file_btn: setContentView(R.layout.activity_main);
                createListView(currentParent);
        }
    }

    private void createNewFolder(){
        try {
            dbAdder = new DatabaseAdder(context);
            dialogBox.collectFolderName(folderData);
            dbAdder.createNewFolderInDB(folderData);
            dialogBox.endDialogBox();
            updateListView();
        } catch (NoSuchFieldError e) {
            theToasting("Field cannot be empty");
        }
    }

    // === Menu navigation below === //

    // method that loads the main menu screen
    public void toMainMenu(View button) {
        setContentView(R.layout.activity_main);
        updateContextAndActivity();
        updateRoot("Home");
        updateListView();
    }

    //TESTING. Remove later method that loads the DB testing screen
    public void changeMenu(View button){
        updateContextAndActivity();

        switch (button.getId()) {
            case R.id.db_testing: setContentView(R.layout.sql_testing); break;
            case R.id.back_button:
                getRootDirectory();
                break;
        }

        updateContextAndActivity();
    }

    //TESTING. Remove later. Buttons for the db_testing menu
    public void sqlOption(View button) {
        updateContextAndActivity();

        DatabaseObjects testing = new DatabaseObjects(context, activity);

        try {
            switch (button.getId()) {
                case R.id.select_all:
                    testing.selectAll();
                    break;
                case R.id.select_testing:
                    testing.getFromDB();
                    break;
                case R.id.add_folder:
                    break;
                case R.id.delete_all:
                    testing.deleteEverything();
                    new FolderContents();
                    break;
                case R.id.get_contents:
                    folderContents = dbReader.getFolderContent("Home");

                    for (int x = 0; x < folderContents.getNames().size(); x++) {
                        System.out.println("Folder: " + folderContents.getNames().get(x) + "\tType: " + folderContents.getTypes().get(x));
                    }

                    break;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    // method that creates toast messages with the passed string
    private void theToasting(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
