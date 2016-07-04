package org.dslul.usbscale;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.usb.UsbConfiguration;
import javax.usb.UsbControlIrp;
import javax.usb.UsbDevice;
import javax.usb.UsbDeviceDescriptor;
import javax.usb.UsbEndpoint;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbInterface;
import javax.usb.UsbPipe;

public class Scale {
	
	private byte[] data = new byte[8192];
	private short vendorId;
	private short productId;
	
	private boolean connected = true;
	private double progress = 0;

	public Scale(int vendorId, int productId) {
		this.vendorId = (short)vendorId;
		this.productId = (short)productId;
		
	}
	
	public byte[] getData() throws Exception {
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		progress = 0;
    	connected = true;
		
		UsbDevice device = findDevice(UsbHostManager.getUsbServices().getRootUsbHub(),vendorId,productId);
        if(device != null) {
        	System.out.println("device found: " + device);
        	
        	UsbConfiguration configuration = device.getActiveUsbConfiguration();
        	UsbInterface iface = configuration.getUsbInterface((byte) 0);
        	//release and claim
        	iface.claim(usbInterface -> true);
        	
        	UsbControlIrp irp = device.createUsbControlIrp((byte)0x21, (byte)0x09, (short)0x0300, (short)0x0);
        	byte[] packet = {0x10,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
        	irp.setData(packet);
        	device.syncSubmit(irp);
        	irp.waitUntilComplete();
        	System.out.println("Receiving data...");
        	
        	
        	List<UsbEndpoint> points=iface.getUsbEndpoints();
            UsbPipe pipe = points.get(0).getUsbPipe();

            if(!pipe.isOpen()){
                System.out.println("need open pipe");
                pipe.open();
            }

    		while(outputStream.size() < 8192) {
        	    byte[] tmp = new byte[8];
        	    pipe.syncSubmit(tmp);
        	    try {
					outputStream.write(tmp);
				} catch (IOException e) {
					e.printStackTrace();
				}
        	    System.out.print(".");
        	    progress = outputStream.size()/8192.0;
    		}
			System.out.println("Total size: " + outputStream.size());
    		pipe.close();
    		iface.release();
    		data = outputStream.toByteArray();
        } else {
        	connected = false;
        	throw new Exception("Bilancia non collegata");
        }
        return data;
	}
	
	public byte[] getDataFromFile(String file) {
		byte[] filedata = new byte[8192];
		try {
			FileInputStream finput = new FileInputStream(file);
			finput.read(filedata);
			finput.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return filedata;
	}
	
	public void saveToFile(byte[] filedata) {
		try {
        	FileOutputStream fos = new FileOutputStream("/home/daniele/Scrivania/dump.bin");
        	fos.write(filedata);
        	fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public double getProgress() {
		return progress;
	}
	
	public boolean isConnected() {
		return connected;
	}
	
	public UsbDevice findDevice(UsbHub hub, short vendorId, short productId)
	{
	    for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices())
	    {
	        UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
	        if (desc.idVendor() == vendorId && desc.idProduct() == productId) return device;
	        if (device.isUsbHub())
	        {
	            device = findDevice((UsbHub) device, vendorId, productId);
	            if (device != null) return device;
	        }
	    }
	    return null;
	}
	
	
}
