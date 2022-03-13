package logic.general;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class SecuritySummariesXMLParser {

	private Document document;
	
	public SecuritySummariesXMLParser() {
		
	}

	public boolean readSecuritySummariesXMLFile(String path) {
		File file = new File(path);
		if(!new File(path).exists()) {
			Utils.log(getClass(),"The xml file " + path + " does not exist!");
			return false;
		}
		Utils.log(this.getClass(), "Loading the securitySummaries file " + path);
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder;
		try {
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
			FileInputStream fis = new FileInputStream(file);
			InputSource is = new InputSource(fis);
			document = documentBuilder.parse(is);
		} catch (ParserConfigurationException|SAXException e) {
			Utils.logErr(getClass(), "Could not parse the xml file " + file);
			return false;
		} catch (FileNotFoundException es) {
			Utils.logErr(getClass(), "Could not load the xml file " + file);
			Utils.logErr(getClass(),  es.getMessage());
			return false;
		} catch (IOException e) {
			Utils.logErr(getClass(), "Could not open the xml file " + file);
			return false;
		}
		return true;
	}
	
	//element = executed, attr = isExecuted , value = yes/no
	Node findTagElement(String element, String attr, String value){
		for(int index2 =0; index2 < document.getElementsByTagName(element).getLength(); index2++) 
			if(document.getElementsByTagName(element).item(index2).getAttributes().getNamedItem(attr).getNodeValue().equals(value))
				return document.getElementsByTagName(element).item(index2);
		return null;
	}
	
	
	
}
