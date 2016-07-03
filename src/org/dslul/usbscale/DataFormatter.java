package org.dslul.usbscale;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataFormatter {
	byte[] data = new byte[8192];
	
	public DataFormatter(byte[] data) {
		this.data = data;
	}
	
	public List<User> getUsers() {
		List<User> users = new ArrayList<>();
		int usersNum = 0;
		
		//counts number of users
		for (int i = 0x1F00; i < 0x1F50; i+=8) {
			if(data[i] != 0)
				usersNum++;
		}

		for (int i = 0; i < usersNum; i++) {
			byte[] measurementBytes = Arrays.copyOfRange(data, i*768, i*768+768);
			byte[] userBytes = Arrays.copyOfRange(data, 0x1F00+i*8, 0x1F00+i*8+8);
			
			User user = new User(i+1, userBytes, measurementBytes);
			users.add(user);
		}
		return users;
	}
}
