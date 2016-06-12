package org.intrace.shared;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Round trip serialization and gzip compression for an array of strings.
 * This array is a list of intrace 'events' that are batched up for a single
 * transmission over a socket.
 * @author erikostermueller
 *
 */
public class SerializationHelper {

	/**
	 * 
	 * @param eventsForOneBurst
	 * @return
	 * @throws IOException
	 */
	public static byte[] toWire(String[] eventsForOneBurst) throws IOException {
		  ByteArrayOutputStream baos = new ByteArrayOutputStream();
		  GZIPOutputStream gzipOut = new GZIPOutputStream(baos);
		  ObjectOutputStream objectOut = new ObjectOutputStream(gzipOut);
		  objectOut.writeObject(eventsForOneBurst);
		  objectOut.close();
		  byte[] bytes = baos.toByteArray();
		  return bytes;
	}
	
	public static String[] fromWire(byte[] bytes) throws IOException, ClassNotFoundException {
	  	ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
	  	GZIPInputStream gzipIn = new GZIPInputStream(bais);
	  	ObjectInputStream objectIn = new ObjectInputStream(gzipIn);
	  	String[] myObj1 = (String[]) objectIn.readObject();
	  	objectIn.close();
	  	return myObj1;
	}


}
