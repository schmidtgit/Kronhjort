package View;

import enums.POIType;
import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.imageio.ImageIO;

/**
 * Class designed to hold icons centralized.
 * The idea is to only have one instance of every icon, and then have
 * every other class query this to obtain a reference to the particular icon. 
 * !Important! The method "loadImages()" must be have been called at least 
 * once before any other queries can take place. 
 */
public class IconPack {
	static Image[] poiList;  //Stores default POI-icons
	static Image[] userPoiList;  //Stores userPOI-icons
	static Image[] arrowList;   //Stores arrow-icons for Directions
	//All filenames of the icons stored in res folder. Used when loading the first time
	static String[] poiFilenames = {"bar", "city", "food", "fuel",  "alert", "bicycle",
			"camera", "car", "house", "parking", "shop", "job", "star", "unknown"};
	static String[] arrowFilenames = {"left", "right", "forward", "roundabout", "finish"};
	
	/**
	 * Initializes image-arrays by loading all of the files (throuh imageIO and getResource)
	 * specified by string arrays. Call this at least once before any other method.
	 */
	public static void loadImages() {
		//Create empty image arrays
		poiList = new Image[poiFilenames.length];
		userPoiList = new Image[poiFilenames.length];
		arrowList = new Image[arrowFilenames.length];
		
		//Load all standard POI's
		for(int i = 0; i < poiFilenames.length; i++) {
			try {
				InputStream input = IconPack.class.getClassLoader().getResourceAsStream("UserPOI_" + poiFilenames[i] + ".png");
			    Image poiInputImg = ImageIO.read(input); //Import image from disk
			    Image poiScaledImg = poiInputImg.getScaledInstance(32, 32, Image.SCALE_SMOOTH); //Resizing to a suitable level 
			    poiList[i] = poiScaledImg; //flipped what is userPOI and POI currently!
			} catch (IOException | IllegalArgumentException ex) {
				//Intentionally does nothing.
			}
		}		
		//Load all userPOI's
		for(int i = 0; i < poiFilenames.length; i++) {
			try {
				InputStream input = IconPack.class.getClassLoader().getResourceAsStream("POI_" + poiFilenames[i] + ".png");
			    Image userInputImg = ImageIO.read(input);
			    Image userScaledImg = userInputImg.getScaledInstance(32, 32, Image.SCALE_SMOOTH);
			    userPoiList[i] = userScaledImg; //flipped what is userPOI and POI currently!
			} catch (IOException | IllegalArgumentException  ex) {
				//Intentionally does nothing.
			}
		}	
		//Load all arrow-icons
		for(int i = 0; i < arrowFilenames.length; i++) {
			try {
				InputStream input = IconPack.class.getClassLoader().getResourceAsStream("Direction_" + arrowFilenames[i] + ".png");
			    Image inputImg = ImageIO.read(input);
			    Image scaledImg = inputImg.getScaledInstance(32, 32, Image.SCALE_SMOOTH);
			    arrowList[i] = scaledImg;
			} catch (IOException | IllegalArgumentException ex) {
				//Intentionally does nothing.
			}
		}
	}
	
	/**
	 * Used to get reference to POI-icon.
	 * Looks up in IconPack internal imageArrays and returns the proper image. 
	 * @param type: the POIType to find a matching icon. 
	 * @return the image representing this POItype, or unknown if nothing matches. 
	 */
	public static Image POIIcon(POIType type) {
		switch(type) {
			case BAR:
				return poiList[0];
			case CITY:
				return poiList[1];
			case FOOD:
				return poiList[2];
			case FUEL:
				return poiList[3];
			case ALERT:
				return poiList[4];
			case BICYCLE:
				return poiList[5];
			case CAMERA:
				return poiList[6];
			case CAR:
				return poiList[7];
			case HOUSE:
				return poiList[8];
			case PARKING:
				return poiList[9];
			case SHOP:
				return poiList[10];
			case JOB:
				return poiList[11];
			case STAR:
				return poiList[12];
			default:
				return poiList[13]; 
		}
	}
	
	/**
	 * Used to get reference to userPOI-icon.
	 * Looks up in IconPack internal imageArrays and returns the proper image. 
	 * @param type: the POIType to find a matching icon. 
	 * @return the image representing this POItype, or unknown if nothing matches. 
	 */
	public static Image userPOIIcon(POIType type) {
		switch(type) {
			case BAR:
				return userPoiList[0];
			case CITY:
				return userPoiList[1];
			case FOOD:
				return userPoiList[2];
			case FUEL:
				return userPoiList[3];
			case ALERT:
				return userPoiList[4];
			case BICYCLE:
				return userPoiList[5];
			case CAMERA:
				return userPoiList[6];
			case CAR:
				return userPoiList[7];
			case HOUSE:
				return userPoiList[8];
			case PARKING:
				return userPoiList[9];
			case SHOP:
				return userPoiList[10];
			case JOB:
				return userPoiList[11];
			case STAR:
				return userPoiList[12];
			default:
				return userPoiList[13]; 
		}
	}
	
	/**
	 * Used to get reference to arrow-icon.
	 * Looks up in IconPack internal imageArrays and returns the proper image. 
	 * @param direction: string name of arrow direction, ex. "forward", or "left"
	 * @return the image representing this arrow. Default is forward. 
	 */
	public static Image getArrow(String direction) {
		switch(direction.toLowerCase()) {
			case "left":
				return arrowList[0];
			case "right":
				return arrowList[1];
			case "forward":
				return arrowList[2];
			case "roundabout":
				return arrowList[3];
			case "finish":
				return arrowList[4];
			default:
				return arrowList[2];
		}
	}

}
