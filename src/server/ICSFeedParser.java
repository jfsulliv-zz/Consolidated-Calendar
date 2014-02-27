package server;
import java.awt.Event;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;

public final class ICSFeedParser {

	public static String[] getCalendarData(File feed) throws FileNotFoundException {
		Scanner parser = new Scanner(feed);
		parser.useDelimiter(Pattern.compile("\\n"));
		String current = null;
		String servid = null;
		String calname = null;
		String[] splitter = null;
		String[] calendarData = new String[2];
		boolean flag = true;

		while(parser.hasNext() && flag){
			current = parser.next();
			splitter = current.split(":");

			if(splitter[0].equals("PRODID") && servid == null){
				servid = current.split(":")[1];
				calendarData[0] = servid;
			} 
			else if(splitter[0].equals("X-WR-CALNAME") && calname == null){
				calname = current.split(":")[1];
				calendarData[1] = calname;
				flag = false;
			}
		}
		parser.close();
		return calendarData;
	}

	public static Event[] getEvents(File feed) throws FileNotFoundException {
		Scanner parser = new Scanner(feed);
		parser.useDelimiter(Pattern.compile("\\n"));
		String current = null;
		String[] splitter = null;
		String[] eventData = new String[4];
		ArrayList<Event> eventList = new ArrayList<Event>();
		boolean flag = true;

		while(parser.hasNext() && flag){
			current = parser.next();

			if(current.equals("BEGIN:VEVENT\r")){
				flag = true;
			}

			while(flag == true){

				current = parser.next();
				splitter = current.split(":");

				if(splitter[0].equals("SUMMARY")){
					eventData[0] = splitter[1];
				}else if(splitter[0].equals("LOCATION")){
					eventData[1] = splitter[1];
				}else if(splitter[0].equals("DTSTART")){
					eventData[2] = splitter[1];
				}else if(splitter[0].equals("DTEND")){
					eventData[3] = splitter[1];
				}else if(current.equals("END:VEVENT\r")){
					flag = false;
					//need to parse string into dates
					//need to pass calendar owner
					Event e = new Event(eventData[0], eventData[1], eventData[2], eventData[3]);
					eventList.add(e);
				}

			}
		}
	}
}

