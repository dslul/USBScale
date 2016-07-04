package org.dslul.usbscale;

import java.nio.ByteBuffer;
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

	public User(int id, byte[] userdata, byte[] mesdata) {
		
		this.id = id;
		
		this.name = "Utente " + String.valueOf(id);
		
		//get height
		this.height = (int)userdata[1];
		
		//get gender
		if(userdata[4] == 1)
			this.gender = Gender.MALE;
		else
			this.gender = Gender.FEMALE;
		
		//get activity
		this.height = (int)userdata[5];
		
		//get birth date
		byte[] birthb = new byte[2];
		birthb[0] = userdata[2];
		birthb[1] = userdata[3];
		int birth = ByteBuffer.wrap(birthb).getShort();
		birthDate = LocalDate.of((((birth&0xFE00) >> 9) + 1920), (int)((birth&0x01E0) >> 5), (birth&0x001F));

		//get measurements (60 is the maximum)
		for (int i = 0; i < 60; i++) {
			
			byte[] weight = new byte[2];
			weight[0] = mesdata[i*2];
			weight[1] = mesdata[i*2+1];
			
			byte[] bodyfat = new byte[2];
			bodyfat[0] = mesdata[128+i*2];
			bodyfat[1] = mesdata[128+i*2+1];
			
			byte[] water = new byte[2];
			water[0] = mesdata[256+i*2];
			water[1] = mesdata[256+i*2+1];
			
			byte[] muscle = new byte[2];
			muscle[0] = mesdata[384+i*2];
			muscle[1] = mesdata[384+i*2+1];
			
			byte[] dateb = new byte[2];
			dateb[0] = mesdata[512+i*2];
			dateb[1] = mesdata[512+i*2+1];

			byte hour   = mesdata[640+i*2];
			byte minute = mesdata[640+i*2+1];
			
			if(dateb[0] == (byte)0x0)
				break;
			
			int date = ByteBuffer.wrap(dateb).getShort();

			Measurement meas = new Measurement(ByteBuffer.wrap(weight).getShort()/10.0,
												ByteBuffer.wrap(bodyfat).getShort()/10.0,
												ByteBuffer.wrap(water).getShort()/10.0,
												ByteBuffer.wrap(muscle).getShort()/10.0,
												((date&0xFE00) >> 9) + 1920,  //year
												(date&0x01E0) >> 5,  //month
												date&0x001F,         //day
												(int)hour,
												(int)minute);
			
			measurements.add(meas);
		}
	}
	
	public int getActivity() {
		return activity;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		try {
			DBManager db = new DBManager();
			db.changeUserName(this, name);
			this.name = name;
			db.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Gender getGender() {
		return gender;
	}
	
	
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
