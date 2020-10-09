import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MultipleSelectionModel;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import networks.Consts;
import networks.SpeedChecker;
import networks.Tools;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class ClientWindowController {

    private static Socket clientSocket;
    private static DataInputStream in;
    private static DataOutputStream out;
    private static DataInputStream inListOfFiles;
    private static DataOutputStream outListOfFiles;
    private static Thread listOfServerFilesThread;

    private ObservableList<String> listOfFiles = FXCollections.observableArrayList();

    @FXML
    private JFXListView serverContentListView;

    @FXML
    private JFXButton uploadButton;

    @FXML
    private Label serverAnswerLabel;

    @FXML
    private Label speedLabel;

    @FXML
    private JFXButton loadFromServerButton;

    public static Socket clientSocketGetter() {
        return clientSocket;
    }

    public static DataInputStream DataInputStreamGetter() {
        return in;
    }

    public static DataOutputStream DataOutputStreamGetter() {
        return out;
    }

    private SpeedChecker speedChecker;

    @FXML
    void initialize() {
        setSocketBuffers();
        setTimerThread();
        setEvents();
        setListOfServerFilesThread();
    }

    private void setSocketBuffers() {
        try {
            clientSocket = new Socket(Consts.DEFAULT_SERVER_IP, Consts.DEFAULT_SERVER_PORT);
            Socket listOfFilesSocket = new Socket(Consts.DEFAULT_SERVER_IP, Consts.DEFAULT_SERVER_PORT);
            speedChecker = new SpeedChecker();

            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());

            inListOfFiles = new DataInputStream(listOfFilesSocket.getInputStream());
            outListOfFiles = new DataOutputStream(listOfFilesSocket.getOutputStream());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setTimerThread() {
        Thread timerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Tools.sleepSec(Consts.THREE_SEC);
                    var is = speedChecker.getInstantSpeed();
                    var as = speedChecker.getAverageSpeed();
                    if(is != 0 && as != 0)
                        Platform.runLater(() -> speedLabel.setText(String.format(
                                " %s %d Inst: %.3f Mb/s, Aver: %.3f Mb/s\n", clientSocket.getInetAddress(),
                                clientSocket.getPort(), is, as))
                        );
                }
            }
        });
        timerThread.start();
    }

    private void setEvents() {
        setUploadToServerButtonEvent();
        setLoadFromServerButtonEvent();
    }

    private void setListOfServerFilesThread() {
        serverContentListView.setItems(listOfFiles);

        listOfServerFilesThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!listOfServerFilesThread.isInterrupted()) {
                    try {
                        if (clientSocket.isClosed())
                            break;

                        Tools.sendBytes(outListOfFiles, "getServerFilesList".getBytes(), Tools.Settings.SERVICE);

                        if (clientSocket.isClosed())
                            break;
                        byte[] fileList = new byte[0];
                        fileList = Tools.getBytes(inListOfFiles, Tools.Settings.SERVICE, null, speedChecker);

                        String fileListString = new String(fileList);
                        ArrayList<String> fileListArray = new ArrayList<>(Arrays.asList(fileListString.split(" ")));

                        for (int i = 0; i < listOfFiles.size(); i++) {
                            String currentFile = listOfFiles.get(i);

                            if (!fileListArray.contains(currentFile))
                                Platform.runLater((() -> listOfFiles.remove(currentFile)));
                        }

                        for (int i = 0; i < fileListArray.size(); i++) {
                            String currentFile = fileListArray.get(i);

                            if (!listOfFiles.contains(currentFile))
                                Platform.runLater((() -> listOfFiles.add(currentFile)));
                        }

                        Tools.sleepSec(Consts.FIVE_SEC);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        listOfServerFilesThread.start();
    }

    private void setUploadToServerButtonEvent() {

        uploadButton.setOnAction(event -> {

            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(null);
            if (file == null) {
                return;
            }

            Platform.runLater((() -> serverAnswerLabel.setText("Start uploading " + file.getAbsolutePath())));

            Thread uploadThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Tools.sendBytes(out, "loadToServer".getBytes(), Tools.Settings.SERVICE);
                        Tools.sendBytes(out, file.getAbsolutePath().getBytes(), Tools.Settings.DATA);


                        byte[] serverAnswer = new byte[0];
                        serverAnswer = Tools.getBytes(in, Tools.Settings.SERVICE, null, speedChecker);

                        byte[] finalServerAnswer = serverAnswer;
                        Platform.runLater((() -> serverAnswerLabel.setText(new String(finalServerAnswer))));
                    } catch (IOException e) {
                        e.printStackTrace();
                        speedChecker.reset();
                    }
                }
            });
            uploadThread.start();
        });
    }

    private void setLoadFromServerButtonEvent() {
        loadFromServerButton.setOnAction(event -> {
            MultipleSelectionModel selectedFile = serverContentListView.getSelectionModel();
            Object selectedFileObject = selectedFile.getSelectedItem();
            if (selectedFileObject == null) {
                Platform.runLater((() -> serverAnswerLabel.setText("File is not chosen!")));
                return;
            }
            String selectedFileString = selectedFileObject.toString();

            DirectoryChooser directoryChooser = new DirectoryChooser();
            File dir = directoryChooser.showDialog(null);
            if (dir == null) {
                return;
            }
            Consts.DEFAULT_MULTI_CLIENT_PATH = dir.getAbsolutePath() + "\\";

            Platform.runLater((() -> serverAnswerLabel.setText("Start loading from server " + selectedFileString)));
            Thread loadFromServerThread = new Thread(new Runnable() {

                @Override
                public void run() {

                    try {
                        Tools.sendBytes(out, "loadFromServer".getBytes(), Tools.Settings.SERVICE);
                        Tools.sendBytes(out, selectedFileString.getBytes(), Tools.Settings.SERVICE);

                        byte[] answer = new byte[0];
                        answer = Tools.getBytes(in, Tools.Settings.SERVICE, null, speedChecker);

                        if (new String(answer).equals("found")) {
                            byte[] fileName = new byte[0];
                            fileName = Tools.getBytes(in, Tools.Settings.DATA, Consts.DEFAULT_MULTI_CLIENT_PATH, speedChecker);

                            if (fileName == null) {
                                Platform.runLater((() -> serverAnswerLabel.setText("Problems with loading file " + selectedFileString)));
                            } else {
                                byte[] finalFileName = fileName;
                                Platform.runLater((() -> serverAnswerLabel.setText(new String(finalFileName) + " successfully loaded from server!")));
                            }
                        } else {
                            Platform.runLater((() -> serverAnswerLabel.setText(selectedFileString + " not found on server and can't upload from server. Try again!")));
                        }
                        speedChecker.reset();
                    } catch (IOException e) {
                        speedChecker.reset();
                        e.printStackTrace();
                    }
                }
            });
            loadFromServerThread.start();
        });
    }
}
