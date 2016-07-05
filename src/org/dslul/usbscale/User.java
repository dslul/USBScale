package org.dslul.usbscale;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class User {

	private int id;
	private List<Measurement> measurements = new ArrayList<>();

	private String name;
	private LocalDate birthDate;
	private int height;
	private Gender gender;
	private int activity;
	//private LocalDate lastDownload;
	
	public enum Gender {MALE,FEMALE}

	
	public User(int userid, String name, String birthdate, int height, 
			String gender, int activity, List<Measurement> measurements) {
		this.id = userid;
		this.name = name;
		this.birthDate = LocalDate.parse(birthdate, DateTimeFormatter.ofPattern("d/M/uuuu"));
		this.height = height;
		this.gender = Gender.valueOf(gender);
		this.activity = activity;
		this.measurements = measurements;
	}
	
	
	
	public int getActivity() {
		return activity;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) throws SQLException {
		DBManager db = new DBManager();
		db.changeUserName(this, name);
		this.name = name;
		db.close();
	}

	public Gender getGender() {
		return gender;
	}
	
	
	

	public List<Measurement> getMeasurements() {
		return measurements;
	}

	public int getHeight() {
		return height;
	}

	public Gender getSex() {
		return gender;
	}

	@Override
	public String toString() {
		return "User " + getId() + " " + birthDate.toString() + "[measurements=" + measurements + "]";
	}

	public boolean hasMeasurements() {
		if(measurements.size() > 0)
			return true;
		else
			return false;
	}

	public int getId() {
		return id;
	}

	public Measurement getLastMeasurement() {
		return Collections.max(measurements, Comparator.comparing(c -> c.getDatetime()));
	}

	public String getBirthDate() {
		return birthDate.format(DateTimeFormatter.ofPattern("d/M/uuuu"));
	}

	public String getAge() {
		return String.valueOf(ChronoUnit.YEARS.between(birthDate, LocalDate.now()));
	}



}
