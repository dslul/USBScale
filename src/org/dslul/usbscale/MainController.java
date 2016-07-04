package org.dslul.usbscale;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import eu.hansolo.medusa.Gauge;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class MainController {
	
	Scale scale;
	List<User> users = new ArrayList<>();
	private final ObservableList<Measurement> data = FXCollections.observableArrayList();
	
	public MainController() {
		scale = new Scale(0x04d9, 0x8010);
	}
	
	public void initialize() {
		colDate.setCellValueFactory(new PropertyValueFactory<>("datetime"));
		colWeight.setCellValueFactory(new PropertyValueFactory<>("weight"));
		colWater.setCellValueFactory(new PropertyValueFactory<>("water"));
		colBodyfat.setCellValueFactory(new PropertyValueFactory<>("bodyfat"));
		colMuscle.setCellValueFactory(new PropertyValueFactory<>("muscle"));
		table.setItems(data);

		comboUsers.getSelectionModel().selectedIndexProperty().addListener(
							(ov, oldval, val) -> {if((int)val!=-1) populateTable(val);});
		progressbar.setProgress(0.0);
		retrieveUsersFromDB();
	}
	
	private void populateTable(Number newvalue) {
		User user = users.get((int)newvalue);
		txtName.setText(user.getName());
		lblAge.setText(user.getAge());
		lblHeight.setText(String.valueOf(user.getHeight()));
		lblGender.setText(user.getGender()==User.Gender.MALE?"Maschile":"Femminile");
		lblActivity.setText(String.valueOf(user.getActivity()));
		
		List<Measurement> measurements = user.getMeasurements();
		data.clear();
		for(Measurement mes : measurements) {
			data.add(mes);
		}
		Measurement lastmes = user.getLastMeasurement();
		gaugeWeight.setValue(lastmes.getWeight());
		gaugeWater.setValue(lastmes.getWater());
		gaugeBodyfat.setValue(lastmes.getBodyfat());
		gaugeMuscle.setValue(lastmes.getMuscle());
	}
	
	void retrieveUsersFromDB() {
		DBManager db = new DBManager();
		if(db.isEmpty() == false) {
			users = db.getSavedUsers();
			populateChoicebox();
		}
		db.close();
	}
	

	void populateChoicebox() {
		ObservableList<String> userNames = FXCollections.observableArrayList();
		for (User user : users) {
			userNames.add(user.getName());
		}
		comboUsers.getItems().clear();
		comboUsers.getItems().addAll(userNames);
		comboUsers.getSelectionModel().selectFirst();
	}
	
	
	class DataThread implements Runnable {
		public void run() {
			btnDownload.setDisable(true);
			Runnable task = () -> { while(scale.getProgress()<100 && scale.isConnected()) 
				progressbar.setProgress(scale.getProgress()); };
			//byte[] data = scale.getDataFromFile("/home/daniele/Scrivania/dump.bin");
			byte[] data = new byte[8192];
			try {				
				Thread t = new Thread(task);
				t.start();
				data = scale.getData();
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
			DataFormatter formatter = new DataFormatter(data);		
			DBManager db = new DBManager();
			db.addUsers(formatter.getUsers());
			db.close();
			//add users to choicebox
			Platform.runLater(() -> retrieveUsersFromDB());;
			
			btnDownload.setDisable(false);
		}
	}

	

	
	
	/*-**************************
	 * FXML EVENTS AND ELEMENTS *
	 ****************************/

	@FXML
    private BorderPane mainPane;
	@FXML
    private GridPane gaugePane;

	@FXML
	private Gauge gaugeWeight;
	@FXML
	private Gauge gaugeWater;
	@FXML
	private Gauge gaugeBodyfat;
	@FXML
	private Gauge gaugeMuscle;

    @FXML
    private Button btnDownload;
    @FXML
    private Button btnSaveName;

    @FXML
    private Label lblHeight;
    @FXML
    private Label lblAge;
    @FXML
    private Label lblGender;
    @FXML
    private Label lblActivity;
    
    @FXML
    private TextField txtName;

    @FXML
    private ChoiceBox<String> comboUsers;
    
    @FXML
    private ProgressBar progressbar;

    @FXML
    private TableView<Measurement> table;

    @FXML
    private TableColumn<Measurement, LocalDate> colDate;
    @FXML
    private TableColumn<Measurement, Double> colWeight;
    @FXML
    private TableColumn<Measurement, Double> colWater;
    @FXML
    private TableColumn<Measurement, Double> colBodyfat;
    @FXML
    private TableColumn<Measurement, Double> colMuscle;
	
	@FXML
	void eventDownload() {
		//byte[] data = scale.getDataFromFile("/home/daniele/Scrivania/dump.bin");
		Thread t = new Thread(new DataThread());
		t.start();
	}
	
	@FXML
	void eventSaveName() {
		if(comboUsers.getSelectionModel().getSelectedItem() != null) {
			int index = comboUsers.getSelectionModel().getSelectedIndex();
			User user = users.get(index);
			user.setName(txtName.getText());
			comboUsers.getItems().set(index, user.getName());
			comboUsers.getSelectionModel().select(index);;
		}
	}
	
	
	@FXML
	void eventSaveToFile() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Salva");
		fileChooser.setInitialFileName("bilancia_" + LocalDateTime.now().toString() + ".csv");
		File file = fileChooser.showSaveDialog((Stage)mainPane.getScene().getWindow());
        if (file != null) {
        	Writer fileWriter;
			try {
				fileWriter = new FileWriter(file);
				for(Measurement meas : data) {
					fileWriter.write(meas.toCsvString());
					fileWriter.write(System.getProperty("line.separator"));
				}
				fileWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
	}
	
}
