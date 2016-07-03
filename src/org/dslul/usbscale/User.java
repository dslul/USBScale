package org.dslul.usbscale;

import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class User {

	private int id;
	private List<Measurement> measurements = new ArrayList<>();
	
	private LocalDate birthDate;

	public User(int id, byte[] userdata, byte[] mesdata) {
		this.id = id;
		
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
	
	public User(int userid, List<Measurement> measurements) {
		this.id = userid;
		this.measurements = measurements;
	}

	public List<Measurement> getMeasurements() {
		return measurements;
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



}
