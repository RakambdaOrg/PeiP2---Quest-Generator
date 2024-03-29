package fr.polytech.di.questgenerator.objects.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Use to read the objectives xml file.
 * <p>
 * Created by COUCHOUD Thomas & COLEAU Victor.
 */
public class XMLStringObjectiveHandler extends DefaultHandler
{
	private ArrayList<XMLStringObjectiveCategory> categories;
	private LinkedList<XMLStringObjectiveCategory> currentCategories;
	private String currentElement;

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
	{
		super.startElement(uri, localName, qName, attributes);
		switch(qName)
		{
			case "strings":
				categories = new ArrayList<>();
				currentCategories = new LinkedList<>();
				break;
			case "category":
				currentCategories.add(new XMLStringObjectiveCategory(currentCategories.isEmpty() ? null : currentCategories.getLast(), attributes.getValue("value")));
				break;
			case "element":
				currentElement = attributes.getValue("value");
				break;
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException
	{
		super.endElement(uri, localName, qName);
		switch(qName)
		{
			case "category":
				if(currentCategories.size() > 1)
				{
					XMLStringObjectiveCategory subcategory = currentCategories.pollLast();
					currentCategories.getLast().addSubcategory(subcategory);
				}
				else
					categories.add(currentCategories.pollLast());
				break;
			case "element":
				currentCategories.getLast().addValue(currentElement);
				break;
		}
	}

	/**
	 * Get all the categories.
	 *
	 * @return The categories.
	 */
	public ArrayList<XMLStringObjectiveCategory> getCategories()
	{
		return this.categories;
	}
}
