package org.dslul.usbscale;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dslul.usbscale.User.Gender;

public class DataDecoder {
	private byte[] data = new byte[8192];
	
	public DataDecoder(byte[] data) {
		this.data = data;
	}
	
	public List<User> getDecodedUsers() {
		List<User> users = new ArrayList<>();
		List<Integer> userids = new ArrayList<>();
		
		//counts number of active users
		for (int i = 0x1F00; i < 0x1F50; i+=8) {
			if(data[i] != 0)
				userids.add((int) data[i]);
		}

		for (int id : userids) {
			byte[] measurementBytes = Arrays.copyOfRange(data, (id-1)*768, (id-1)*768+768);
			byte[] userBytes = Arrays.copyOfRange(data, 0x1F00+(id-1)*8, 0x1F00+(id-1)*8+8);
			
			User user = getDecodedUser(id, userBytes, measurementBytes);
			users.add(user);
		}
		return users;
	}
	
	private User getDecodedUser(int id, byte[] userdata, byte[] mesdata) {
		int userid;
		String username;
		int userheight;
		Gender usergender;
		int useractivity;
		String userbirth;
		List<Measurement> measurements = new ArrayList<>();
		
		userid = id;
		
		username = "Utente " + String.valueOf(id);
		
		//get height
		userheight = (int)(userdata[1]&0xFF);
		
		//get gender
		if(((userdata[4]&0xF0) >> 4) == 0)
			usergender = Gender.MALE;
		else
			usergender = Gender.FEMALE;
		
		//get activity
		useractivity = (int)(userdata[4]&0x0F);
		
		//get birth date
		byte[] birthb = new byte[2];
		birthb[0] = userdata[2];
		birthb[1] = userdata[3];
		int birth = ByteBuffer.wrap(birthb).getShort();
		//userbirth = LocalDate.of((((birth&0xFE00) >> 9) + 1920), (int)((birth&0x01E0) >> 5), (birth&0x001F));
		userbirth = String.valueOf(birth&0x001F) + "/" + String.valueOf((birth&0x01E0) >> 5) + 
				"/" + String.valueOf(((birth&0xFE00) >> 9) + 1920);
		
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
		User user = new User(userid, username, userbirth, userheight, 
				usergender.name(), useractivity, measurements);
		return user;
	}
	
}
