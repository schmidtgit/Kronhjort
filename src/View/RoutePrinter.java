package View;

import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;

/**
 * Printable version of a PathFinder route.
 * Relies on Direction class.
 */
public class RoutePrinter implements Printable {
	Font title = new Font("Segoe UI Semibold", Font.PLAIN, 32);
	Font text = new Font("Segoe UI Semilight", Font.PLAIN, 15);
	ArrayList<Direction> directions;  //The route to be printed
	String time, length, start, end; //Estimated time and length of route. Handed over from caller (typically NavController)
	int linesPerPage; //Holds how many lines fit on the current pageformat. 

	/**
	 * Creates RoutePrinter, which will automatically call print() once it is created and printer is ready. 
	 * @param route All the directions of the route. These are responsible for the strings and icons shown.
	 * @param estTime Formatted string of estimated time. (Expected format "t" h : "mm" min);
	 * @param estLength int representing the length of route in meters.
	 * @param start The name of the start point.
	 * @param end The name of the destination.
	 */
	public RoutePrinter(ArrayList<Direction> route, String estTime, int estLength, String start, String end) {
		directions = route;
		int kilometers = estLength / 1000; 
		int meters = estLength % 1000;
		meters = (meters / 50) * 50;
		length = "Distance: " + kilometers + "." + meters + " km";
		time = estTime;
		this.start = start;
		this.end = end;
	}

	/**
	 * Creates a title, and a subtitle of estimated time, and length and prints.
	 */
    public int print (Graphics g, PageFormat pf, int page) throws PrinterException {    	    	
    	//Find image margin, and translate to printable area
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(pf.getImageableX(), pf.getImageableY());
        double lineHeight = 40;  //Precalcutaled, and is the incrementation as offsetY.
        double pageHeight = pf.getImageableHeight() - 50; 
        if(page == 1) { linesPerPage = (int)((pageHeight-180)/lineHeight); } 
        else { linesPerPage = (int)((pageHeight-80)/lineHeight); }
        int offsetX = 80;  
        int offsetY = 80;
        int linesPrinted = 0;
        boolean somethingPrinted = false; //Flags whether of not to return this page. 
        
        //Print title and estimates on first page
    	if (page == 0) {
            g2d.setFont(title);
            g2d.setColor(NavScreen.btnBG());
            g2d.drawString("Route overview", offsetX, 50);
            g2d.setFont(text);
            g2d.setColor(new Color(180,70,100));
            g2d.drawString("From " + start, offsetX, 80);
            g2d.drawString("to " + end, offsetX, 100);
            g2d.drawString(time + " | " + length, offsetX, 120);
            offsetY = 180;
            linesPerPage = (int)((pageHeight-180)/lineHeight);
            somethingPrinted = true;
        } 
    	
    	int lineIndex = page * linesPerPage;  //Determine where in content array to start printing from. 
    	
        //Setup look for the lines of text representing directions.
        g2d.setFont(new Font("Segoe UI Semilight", Font.PLAIN, 13));
        g2d.setColor(Color.black);
        int fontHeight = g2d.getFontMetrics().getHeight();
        
        //Print all directions
        while(linesPrinted < linesPerPage && lineIndex < directions.size()) {
        	g2d.drawImage(IconPack.getArrow(directions.get(lineIndex).arrow()), offsetX, offsetY, 32, 32, null);
        	g2d.drawString(directions.get(lineIndex).toString(), offsetX + 40, offsetY+fontHeight);
            offsetY += lineHeight; //Go down one line. 
            lineIndex++;
            linesPrinted++;
            somethingPrinted = true;
        }
        
        if(somethingPrinted) {return PAGE_EXISTS; }  //Printer specific attribute, basically means "this page has to be printed".  
        else { return NO_SUCH_PAGE; } // Tells the printer client the page doesn't exist. Printing usually stops after this.
        
    }
}
