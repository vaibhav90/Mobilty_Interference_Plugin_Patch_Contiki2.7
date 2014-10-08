import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.math.BigDecimal;

public class saveRSSI extends JPanel implements Runnable{

	// Variable accessed by the parent class
	public BufferedReader in;
	public BufferedWriter out;
	public boolean active_capture;
	public File File_path; 
	
	// Constants
	static final int BYTE_DELIMITER_10 = 110;
	static final int BYTE_DELIMITER_11 = 111;
	static final int BYTE_DELIMITER_1 = 101;
	static final int BYTE_DELIMITER_2 = 102;
	static final int BYTE_DELIMITER_3 = 103;
	static final int BYTE_DELIMITER_4 = 104;
	static final int BYTE_DELIMITER_5 = 105;
	
	// Global variables
	static long count_bytes = 0;
	static long count_ms = 0;
	static int last_rssi = 0;
	static PrintWriter streamWriter = null; 

	static double speed_mote = 0;
	static double speed_mote_us = 0;
	static double count_packets_previous = 0;
	
	// Variables to build the plot
	public static boolean color = true; // Greyish or greenish
	public static int MARGIN_BOTTOM = 20; // Margin from the bottom
	public static int MARGIN_TOP = 10; // Margin from the top
	public static int MARGIN_LEFT = 20;
	public static int MARGIN_RIGHT = 20;

	
	// Getters and setters
	public boolean isActive_capture() {
		return active_capture;
	}

	public void setActive_capture(boolean active_capture) {
		if(active_capture == true){
			try {
				streamWriter = new PrintWriter(new FileWriter(File_path,true));
				count_ms = System.currentTimeMillis();
				//String log = count_ms + " " + active_capture + "\n";
				//streamWriter.append(log);
				count_bytes = 0;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else{
			//String log = System.currentTimeMillis() + " " + active_capture + "\n";
			//streamWriter.append(log);
			count_ms = 0;
			count_bytes = 0;			
			streamWriter.close();
		}
		this.active_capture = active_capture;		
	}

	
	// Constructor
	public saveRSSI(InputStream inputstr, OutputStream outputstr, File savefile) {
		in = new BufferedReader(new InputStreamReader(inputstr));
		out = new BufferedWriter(new OutputStreamWriter(outputstr));		
		File_path = savefile;		
		active_capture = false;
		speed_mote = 0;
	}

	
	// Round a double
	public static double round(double d, int decimalPlace) {
	    BigDecimal bd = new BigDecimal(d);
	    bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
	    return bd.doubleValue();
	}

	
	// Paint function
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;	 
		
		// Set white background
		int height = getHeight();
		int width = getWidth();
		g.setColor(Color.white);
		g.fillRect(0, 0, width, height); 
		
		int WIDTH_RECTANGLE = width - MARGIN_LEFT - MARGIN_RIGHT;
		int HEIGHT_RECTANGLE = height - MARGIN_TOP - MARGIN_BOTTOM;
		       
		// Empty rectangle
		GradientPaint greytowhite = new GradientPaint(MARGIN_LEFT, MARGIN_TOP, Color.WHITE, WIDTH_RECTANGLE, MARGIN_TOP, Color.lightGray, false);		
		GradientPaint greentoorange = new GradientPaint(MARGIN_LEFT, MARGIN_TOP, Color.GREEN, WIDTH_RECTANGLE, MARGIN_TOP, Color.ORANGE, false);
		if(color == true){
			g2.setPaint(greentoorange);
		}
		else{
			g2.setPaint(greytowhite);
		}
		g.fillRect(MARGIN_LEFT, MARGIN_TOP, WIDTH_RECTANGLE, HEIGHT_RECTANGLE);
		
		// Fill in rectangles
		g.setColor(Color.gray);		
		int value = (int) (WIDTH_RECTANGLE*last_rssi)/100;
		g.fillRect(MARGIN_LEFT, MARGIN_TOP, value, HEIGHT_RECTANGLE);
				
		// Label
		int font_size = 18;
		Font font = new Font("Arial", Font.BOLD, font_size);    
		g2.setFont(font);
		g.setColor(Color.black);
		int actual_rssi = last_rssi - 100;
		g.drawString(String.valueOf(actual_rssi + " dBm"), MARGIN_LEFT+(WIDTH_RECTANGLE/2), MARGIN_TOP+(HEIGHT_RECTANGLE/2)+(font_size/2));
		
		// Label kHz (speed MOTE)
		font_size = 12;
		font = new Font("Arial", Font.PLAIN, font_size);    
		g2.setFont(font);
		g.drawString(String.valueOf(round(speed_mote,2) + " Hz"), MARGIN_LEFT, height-(MARGIN_BOTTOM/2));			
	}
	

	// Handling serial input
	public boolean handleInput() throws IOException {	     
		try {
			int inputbyte;
			int p1=0, p2=0, p3=0, p4=0, p5=0;
			while ((inputbyte = in.read()) != -1) {		
				// SPEED OF THE MOTE AND PACKETS SENT MEASUREMENT
				if(inputbyte == BYTE_DELIMITER_1){					
					p1 = in.read();
					p2 = in.read();
					p3 = in.read();		
					if(p3 == BYTE_DELIMITER_2){
						speed_mote = (p1 & 0xFF) + ((p2 & 0xFF)<<8);												
						p1 = in.read();
						p2 = in.read();
						p3 = in.read();
						p4 = in.read();
						p5 = in.read();						
						if(p5 == BYTE_DELIMITER_3){	
							double count_packets_current = (p1 & 0xFF) + ((p2 & 0xFF)<<8) + ((p3 & 0xFF)<<16) + ((p4 & 0xFF)<<24);							
							speed_mote = 1/(1000000*speed_mote/32768/count_packets_current)*1000000;
							speed_mote_us = 1000000/speed_mote;							
							
							// TODO: verify if the amount of packets was correctly received
							if(count_packets_previous == count_bytes){
								//System.out.println("OK");
							}
							//System.out.println("Should have been: " + count_packets_previous + " was: " + count_bytes + " next: " + count_packets_current);
							count_bytes = 0;
							count_packets_previous = count_packets_current;
							// TODO
							
						}						
					}
				}
				// INTERFERENCE MEASUREMENT
				if(inputbyte == BYTE_DELIMITER_4){
					p1 = in.read();
					p2 = in.read();
					p3 = in.read();
					//System.out.println("p1: " + p1 + " p2: " + p2 + " p3: " + p3);
					if((p3 == BYTE_DELIMITER_5) && (p1 > 0) && (p1 < 255) && (p2 > 0) && (p2 < 255)){
						
						// Update parameters
						last_rssi = p1;
						count_bytes += p2;	
						
						// Storing the data
						if (active_capture == true) {		
							try {														
								if(speed_mote_us > 0){									
									String log = (round(speed_mote_us*p2,2)) + " " + (p1-100) + "\n";											
									streamWriter.append(log);								    
								}						
							}	
							catch (Exception e) {
								e.printStackTrace();
							}	
						}
						
						// Update the RSSI bar
						repaint();
					}				
				}
			}
		} catch (IOException e) {
			//System.err.println("Error: " + e.getMessage());
			return true;
		}
		return false;
	}

	public void run(){
		try {
			while(this.handleInput());
		} catch (IOException e) {		
			e.printStackTrace();
		}	
	}

}
