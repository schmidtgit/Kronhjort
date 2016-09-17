package View;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import Controller.Main;
import Model.Address;
import Model.POI;

/**
 * A JComboBox with auto-suggestions based on addresses and cities stored in the model.
 * Expects that model keeps an sorted array.
 */
public class AddressFinder extends JComboBox<String> {
	private static final long serialVersionUID = 16052016L;
	private static final int MIN_LENGTH = 2;
	private static final int MAX_SUGGESTION = 50;
	
	//Regex-related
	private static Pattern addressPattern = Pattern.compile("(?<street>[a-zæøåéèëüÿöä'´.\\-;:\\/\\(\\)0-9 ]*?)(?<house>[1-9]+[0-9]*[a-zA-Z]?[ ]?[0-9.]*(th|tv|mf|[a-z])?)");
	private static Pattern splitPattern = Pattern.compile("(?<address>.*?)[ ,]*(?<postcode>[0-9]{4})(?<city>.*)?");

	//Stored information
	private Address[] aList;
	private POI[] cList;
	private Point2D.Float currentPoint;

	/**
 	* A JComboBox with auto-suggestions based on addresses and cities stored in the model.
 	* Expects that model keeps an sorted array.
 	*/
	public AddressFinder() {
		setEditable(true);
		getEditor().getEditorComponent().addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {}
			
			@Override
			public void keyReleased(KeyEvent e) {}
			
			@Override
			public void keyPressed(KeyEvent e) {
				//Get new suggestions when a digit, a character, enter or right arrow is used in the input field.
				if (!(!Character.isLetter(e.getKeyChar()) && !Character.isDigit(e.getKeyChar()) && e.getKeyCode() != KeyEvent.VK_ENTER && e.getKeyCode() != KeyEvent.VK_RIGHT)) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							search();
						}
					});
				}
			}
		});
		
		//Get information from Model
		aList = Main.model().addresses();
		cList = Main.model().cities();
	}
	
	@Override
	public String toString() {
		return getEditor().getItem().toString();
	}

	/**
	 * Switches information from this AddressFinder and the given AddressFinder.
	 * @param af The AddressFinder to switch information with.
	 */
	public void switchBox(AddressFinder af) {
		this.currentPoint = null;
		af.currentPoint = null;
		
		String s = this.toString();
		this.setSelectedItem(af.toString());
		af.setSelectedItem(s);
	}

	/**
	 * Returns the current found address or city as a Point2D.
	 * @return Can be either a POI or an Address object.
	 */
	public Point2D getPoint() {
		updatePoint();
		return currentPoint;
	}
	
	/**
	 * Set the text in the shown text field.
	 * @param s This string will be shown in the text field and used for searching.
	 */
	public void setText(String s) {
		setSelectedItem(s);
	}

	/**
	 * Updates suggestions based on the user input.
	 */
	private void search() {
		String input = toString();
		currentPoint = null;
		removeAllItems();
		setSelectedItem(input);
		if (input.length() < MIN_LENGTH) {return;};
		
		input = input.toLowerCase().trim();

		//Get city suggestions
		ArrayList<POI> cities = new ArrayList<POI>();
		for (POI poi : cList) {
			String lowercaseName = poi.name().toLowerCase();
			if (lowercaseName.startsWith(input)) {
				cities.add(poi);
				addItem(poi.name() + " [CITY]");
			}
		}

		//Get address suggestions
		List<Address> suggestions = getSuggestions(input);
		
		//Evaluate results
		if (suggestions.size() == 0 && cities.size() == 1) {
			//Case: Only one city possible... Auto-complete.
			currentPoint = (Point2D.Float) cities.get(0);
			setText(currentPoint.toString());
			hidePopup();
		} else if (suggestions.size() == 1 && cities.size() == 0) {
			//Case: Only one address possible... Auto-complete.
			if(currentPoint != null) {
				currentPoint = (Point2D.Float) suggestions.get(0);
				setText(currentPoint.toString());
				hidePopup();
			} else {
				hidePopup();
				showPopup();
			}
		} else if (suggestions.size() != 0 || cities.size() != 0) {
			//Case: More than one suggestions, show the user.
			hidePopup();
			showPopup();
		} else {
			//Case: Nothing found... hide suggestions.
			hidePopup();
		}
	}

	/**
	 * Returns the first index that matches the given key.
	 * @param key The prefix of the wished address.
	 * @return The index of the first match, or -1 for nothing found.
	 */
	private int binarySearch(String key) {
		if (aList == null || aList.length < 1)
			return -1;
		int l = 0, m = 0, h = aList.length - 1;
		String comp = null;
		while (l <= h) {
			m = l + (h - l) / 2;
			comp = aList[m].street();
			if(comp.length() < key.length()) {
				comp = comp.substring(0, comp.length()).toLowerCase();
			} else {
				comp = comp.substring(0, key.length()).toLowerCase();
			}
			
			if (key.compareTo(comp) < 0) {
				h = m - 1;
			} else if (key.compareTo(comp) > 0) {
				l = m + 1;
			} else
				break;
		}
		if (!comp.equals(key)) {
			return -1;
		}
		while (m > 0 && aList[m-1].street().toLowerCase().startsWith(key)) {
			m--;
		}
		return m;
	}
	
	/**
	 * Finds address suggestions based on the input string.
	 * @param input Raw input string from the user expected.
	 * @return A list of addresses that match the user input.
	 */
	private List<Address> getSuggestions(String input){
		// Initial REGEX split
		Matcher regexMatch = splitPattern.matcher(input);
		short postcode = 0;
		String street = null;
		String house = null;
		if(regexMatch.matches()) {
			try {
				postcode = Short.parseShort(regexMatch.group("postcode"));
			} catch (NumberFormatException e) {
				postcode = 0;
			}
			input = regexMatch.group("address").trim();
		}
				
		// Secondary REGEX split		
		regexMatch = addressPattern.matcher(input);
		if(regexMatch.matches()) {
			street = regexMatch.group("street").toLowerCase();
			house = regexMatch.group("house");
		} else {
			street = input;
		}
		street = street.trim();
		
		// Getting address suggestions
		List<Address> suggestions = new ArrayList<>();
		int index = binarySearch(street);
		int sugCount = 0;
		if(index >= 0) {
			if(house == null) {
				suggestions = roadMatches(street, index);
				if (suggestions.size() > 1) {
					for(Address a : suggestions) {
						if(sugCount < MAX_SUGGESTION) {
							addItem(a.street());
							sugCount++;	
						}
					}
				} else if (postcode == 0) {
					suggestions = postcodeMatches(street, index);
					for(Address a : suggestions){
						if(sugCount < MAX_SUGGESTION) {
							addItem(a.street() + ", " + a.postcode() + " " + a.city());
							sugCount++;
						}
					}
				} else {
					suggestions = housenumberMatches(street, postcode, house, index);
					for(Address a : suggestions) {
						if(sugCount < MAX_SUGGESTION) {
							addItem(a.toString());
							sugCount++;
						}
					}
				}
			} else {
				suggestions = housenumberMatches(street, postcode, house, index);
				for(Address a : suggestions) {
					if(sugCount < MAX_SUGGESTION) {
						addItem(a.toString());
						sugCount++;
					}
				}
			}
		}
		return suggestions;
	}

	/**
	 * Return unique street names only.
	 * @param key The street name to match
	 * @param i The index to start searching from
	 * @return Might be an empty list.
	 */
	private List<Address> roadMatches(String key, int i) {
		ArrayList<Address> tmp = new ArrayList<>();
		tmp.add(aList[i]);
		while (aList[i].street().toLowerCase().startsWith(key)) {
			if (!(tmp.get(tmp.size() - 1).street().toLowerCase().equals(aList[i].street().toLowerCase()))) {
				tmp.add(aList[i]);
			}
			i++;
		}
		return tmp;
	}

	/**
	 * Return unique post codes only.
	 * @param key The string to match.
	 * @param i The index to start searching from.
	 * @return Might be an empty list.
	 */
	private List<Address> postcodeMatches(String key, int i) {
		ArrayList<Address> tmp = new ArrayList<>();
		tmp.add(aList[i]);
		while (aList[i].street().toLowerCase().startsWith(key)) {
			if (tmp.get(tmp.size() - 1).postcode() != aList[i].postcode()) {
				tmp.add(aList[i]);
			}
			i++;
		}
		return tmp;
	}

	/**
	 * Returns all addresses that matches the given information.
	 * @param street The street name to compare against.
	 * @param postcode Returns only addresses located in this postcode. 0 for everything.
	 * @param house Returns only house numbers starting with this.
	 * @param i The index to start searching from.
	 * @return
	 */
	private List<Address> housenumberMatches(String street, short postcode, String house, int i) {
		ArrayList<Address> tmp = new ArrayList<>();
		if(postcode != 0) {
			while (i < aList.length-1 && (!aList[i].street().toLowerCase().startsWith(street) || aList[i].postcode() != postcode)) {
				i++;
			}
		}
		while (aList[i].street().toLowerCase().equals(street)) {
			if (postcode == 0 || aList[i].postcode() == postcode) {
				if (house == null || aList[i].housenumber().toLowerCase().startsWith(house)) {
					tmp.add(aList[i]);
				}
			}
			i++;
		}
		return tmp;
	}
	
	/**
	 * Updates the current "best suggestions". Called by getPoint().
	 */
	private void updatePoint() {
		if(currentPoint != null) {return;}
		String input = toString().toLowerCase().trim();
		// Initial REGEX split
		Matcher regexMatch = splitPattern.matcher(input);
		short postcode = 0;
		String street = null;
		String house = null;
		if(regexMatch.matches()) {
			try {
				postcode = Short.parseShort(regexMatch.group("postcode"));
			} catch (NumberFormatException e) {
				postcode = 0;
			}
				input = regexMatch.group("address").trim();
			}
				
		// Secondary REGEX split		
		regexMatch = addressPattern.matcher(input);
		if(regexMatch.matches()) {
			street = regexMatch.group("street").toLowerCase();
			house = regexMatch.group("house");
		} else {
			street = input;
		}
		street = street.trim();
		
		//Sort 'em
		List<Address> suggestions;
		int index = binarySearch(street);
		if(index >= 0) {
			suggestions = housenumberMatches(street, postcode, house, index);
			for (Address a : suggestions) {
				if(a.housenumber().toLowerCase().equals(house.toLowerCase())) {
					currentPoint = a;
					return;
				}
			}
		}
		for (POI poi : cList) {
			if (input.equalsIgnoreCase(poi.name()+" [CITY]")) {
				currentPoint = poi;
				return;
			}
		}
	}
}
