package server.framework;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

public final class ICSFeedParser {
	
	public static File downloadICSFile(URL link) throws IOException {
		File f = new File(link + ".ics");
        URLConnection con; 
        DataInputStream dis;
        FileOutputStream fos;
        byte[] fileData;
        try {
            con = link.openConnection();
            dis = new DataInputStream(con.getInputStream());
            fileData = new byte[con.getContentLength()];
            for (int x = 0; x < fileData.length; x++) {
                fileData[x] = dis.readByte();
            }
            dis.close();
            fos = new FileOutputStream(f); 
            fos.write(fileData);
            fos.close();
        }
        catch(IOException io) {
            System.out.println(io);
        }
        
        return f;
	}

	public static String[] getCalendarData(File f) throws IOException{
		Scanner parser = new Scanner(f);
		parser.useDelimiter(Pattern.compile("\\n"));
		String current = null;
		String servid = null;
		String calname = null;
		String[] splitter = null;
		String[] calendarData = new String[2];
		boolean flag = true;

		while (parser.hasNext() && flag) {
			current = parser.next();
			splitter = current.split(":");

			if (splitter[0].equals("PRODID") && servid == null) {
				servid = current.split(":")[1];
				calendarData[0] = servid;
			} else if (splitter[0].equals("X-WR-CALNAME") && calname == null) {
				calname = current.split(":")[1];
				calendarData[1] = calname;
				flag = false;
			}
		}
		parser.close();
		return calendarData;
	}

	public static Event[] getEvents(File f) throws FileNotFoundException, IOException {
		DateFormat df = DateFormat.getDateInstance();
		Scanner parser = new Scanner(f);
		parser.useDelimiter(Pattern.compile("\\n"));
		String current = null;
		String[] splitter = null;
		String[] eventData = new String[4];
		ArrayList<Event> eventList = new ArrayList<Event>();
		boolean flag = true;

		while (parser.hasNext() && flag) {
			current = parser.next();

			if (current.equals("BEGIN:VEVENT\r")) {
				flag = true;
			}

			while (flag == true) {

				current = parser.next();
				splitter = current.split(":");

				Date start = null;
				Date end = null;
				if (splitter[0].equals("SUMMARY")) {
					eventData[0] = splitter[1];
				} else if (splitter[0].equals("LOCATION")) {
					eventData[1] = splitter[1];
				} else if (splitter[0].equals("DTSTART")) {
					eventData[2] = splitter[1];

					try {
						start = df.parse(splitter[1]);
					} catch (ParseException e) {
						e.printStackTrace();
					}

				} else if (splitter[0].equals("DTEND")) {
					eventData[3] = splitter[1];

					try {
						end = df.parse(splitter[1]);
					} catch (ParseException e) {
						e.printStackTrace();
					}

				} else if (current.equals("END:VEVENT\r")) {
					flag = false;
					Event e = new Event(eventData[0], eventData[1], start, end);
					eventList.add(e);
				}

			}

		}
		parser.close();

		// Convert to static Event[]
		Event[] events = new Event[eventList.size()];
		events = eventList.toArray(events);
		return events;
	}
}
