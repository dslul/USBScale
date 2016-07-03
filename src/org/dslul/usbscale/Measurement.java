package org.dslul.usbscale;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class Measurement {
	
	double weight;
	double bodyfat;
	double water;
	double muscle;	
	LocalDateTime datetime;

	
	public Measurement(double weight, double bodyfat, double water, double muscle, int year, int month, int day,
						int hour, int minute) {
		this.weight = weight;
		this.bodyfat = bodyfat;
		this.water = water;
		this.muscle = muscle;
		this.datetime = LocalDateTime.of(year, month, day, hour, minute);
//		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
//		this.datetime.format(formatter);
	}
	
	
	public Measurement(double weight, double bodyfat, double water, double muscle, LocalDateTime datetime) {
		this.weight = weight;
		this.bodyfat = bodyfat;
		this.water = water;
		this.muscle = muscle;
		this.datetime = datetime;
	}


	public double getWeight() {
		return weight;
	}

	public double getBodyfat() {
		return bodyfat;
	}

	public double getWater() {
		return water;
	}

	public double getMuscle() {
		return muscle;
	}
	
	public LocalDateTime getDatetime() {
		return datetime;
	}


	@Override
	public String toString() {
		return "Measurement [weight=" + weight + ", bodyfat=" + bodyfat + ", water=" + water + 
				", muscle=" + muscle + ", date=" + 
				datetime.toString()	+ "]";
	}
	
	public String toCsvString() {
		return weight + "," + bodyfat + "," + water + "," + muscle + "," + datetime.toString();
	}


	public int getUnixTimestamp() {
		return (int) (Timestamp.valueOf(datetime).getTime()/1000);
	}

}
