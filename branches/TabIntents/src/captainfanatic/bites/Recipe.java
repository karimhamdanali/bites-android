package captainfanatic.bites;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import android.util.Xml;

public class Recipe {
	
	String mName;
	ArrayList<String> ingredients;
	ArrayList<String> methods;
	
	
	
	@Override
	public String toString() {
		return mName; 
	}

	public Recipe() {
		ingredients = new ArrayList<String>();
		methods = new ArrayList<String>();
		mName = "New Recipe";
		ingredients.add("apples");
		ingredients.add("sugar");
		methods.add("cut up apples");
		methods.add("add sugar");
	}
	
	/**
	 * Dump member data into an xml string
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	public String Push() throws IllegalArgumentException, IllegalStateException, IOException {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter strWriter = new StringWriter();
		serializer.setOutput(strWriter);
		serializer.startDocument(null, null);
		serializer.startTag(null, "Recipe");
		
		for (String ingredient : ingredients) {
			serializer.startTag(null, "Ingredient");
			serializer.text(ingredient);
			serializer.endTag(null, "Ingredient");
		}
		
		for (String method : methods) {
			serializer.startTag(null, "Method");
			serializer.text(method);
			serializer.endTag(null, "Method");
		}
		
		serializer.endTag(null, "Recipe");
		serializer.endDocument();
		return strWriter.toString();
	}
	
	public void Pull(String strXml) throws XmlPullParserException, IOException {
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		XmlPullParser parser = factory.newPullParser();
		parser.setInput(new StringReader(strXml));
		int eventType = parser.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.START_TAG) {
				if (parser.getName().equals("Ingredient")) {
					eventType = parser.next();
					if (eventType == XmlPullParser.TEXT) {
						ingredients.add(parser.getText());
					}
				}
				else if (parser.getName().equals("Method")) {
					eventType = parser.next();
					if (eventType == XmlPullParser.TEXT) {
						methods.add(parser.getText());
					}
				}
			}
			eventType = parser.next();
		}
	} 
}
