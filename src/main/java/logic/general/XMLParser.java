package logic.general;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class XMLParser {

	private Document document;

	public XMLParser() {
	}

	private void addElement(String[] attributes, Node tag, HashMap<String, List<String[]>> output) {
		String[] elementsValues = new String[attributes.length];
		for (int i = 0; i < attributes.length; i++)
			elementsValues[i] = tag.getAttributes().getNamedItem(attributes[i]).getNodeValue().replaceAll(";", "")
			.trim();

		if (output.get(tag.getNodeName()) == null)
			output.put(tag.getNodeName(), new ArrayList<String[]>());
		//System.err.println(tag.getNodeName()+" " + elementsValues[0] + ":" + elementsValues[1]);
		output.get(tag.getNodeName()).add(elementsValues);
	}

	// String sourceChilds =
	// document.getElementsByTagName(data).item(0).getChildNodes().item(1).getAttributes().getNamedItem("class")


	public HashMap<String, List<String[]>> getSinkOrSources(String data, HashMap<String, List<String[]>> output) {
		if (document == null) {
			Utils.logErr(getClass(), "The XML file is not loaded or is empty");
			return null;
		}

		NodeList allSourceChilds = document.getElementsByTagName(data);
		//.item(0).getChildNodes();
		for(int index1 =0; index1 < allSourceChilds.getLength(); index1++) {
			NodeList sourceChilds = allSourceChilds.item(index1).getChildNodes();
			for (int index = 0; index < sourceChilds.getLength(); index++) {
				if (sourceChilds.item(index).getAttributes() != null) {
					if (sourceChilds.item(index).getNodeName().equals(Utils.XMLField))
						addElement(new String[] { "class", "name" }, sourceChilds.item(index), output);
					else if (sourceChilds.item(index).getNodeName().equals(Utils.XMLParameter))
						addElement(new String[] { "class", "method", "parameter" }, sourceChilds.item(index), output);
					else if (sourceChilds.item(index).getNodeName().equals(Utils.XMlReturnValue))
						addElement(new String[] { "class", "method" }, sourceChilds.item(index), output);
					else
						Utils.logErr(getClass(), "Unknown type: " + sourceChilds.item(index).getNodeName());
				}
			}
		}
		return output;
	}

	public HashMap<String, List<String[]>> getAllSinksOrSources(HashMap<String, List<String[]>> output, boolean isHigh, boolean isSource) {
		if (document == null) {
			Utils.logErr(getClass(), "The XML file is not loaded or is empty");
			return null;
		}
		NodeList allSourcesSinkTypes = document.getElementsByTagName("assign");
		for(int index1 =0; index1 < allSourcesSinkTypes.getLength(); index1++) {
			String domain = allSourcesSinkTypes.item(index1).getAttributes().getNamedItem("domain").getNodeValue();
			if(isHigh?domain.equals("high"):domain.equals("low")) {
				String handle = allSourcesSinkTypes.item(index1).getAttributes().getNamedItem("handle").getNodeValue();
				Node handleNode = findTagElement("assignable","handle",handle);
				if(handleNode!=null) {
					NodeList handleChilds = handleNode.getChildNodes();
					//for (int index = 0; index < sourceChilds.getLength(); index++) 
					ArrayList<Node> sinkSourceNodes = findTagElement(handleChilds, isSource?"source":"sink");
					for(Node node: sinkSourceNodes) {
						for(Node ss : findNonNullChilds(node)){
							if (ss.getNodeName().equals(Utils.XMLField))
								addElement(new String[] { "class", "name" }, ss, output);
							else if (ss.getNodeName().equals(Utils.XMLParameter))
								addElement(new String[] { "class", "method", "parameter" }, ss, output);
							else if (ss.getNodeName().equals(Utils.XMlReturnValue))
								addElement(new String[] { "class", "method" }, ss, output);
							else if (ss.getNodeName().equals(Utils.XMlMethod))
								addElement(new String[] { "class", "method" }, ss, output);
							else if (ss.getNodeName().equals(Utils.XMLReference))
								addElement(new String[] { "class", "method" }, ss, output);
							else
								Utils.logErr(getClass(), "Unknown type: " + ss.getNodeName());
						}
					}
				} else
				{
					Utils.logErr(getClass(), "No  Handle Is Defined For The Tage "+ handle);
				}
			}
		}
		return output;
	}

	private ArrayList<Node> findTagElement(NodeList sourceChilds, String tag) {
		ArrayList<Node> nodeList = new ArrayList<Node>();
		for(int index2 =0; index2 < sourceChilds.getLength(); index2++) 
			if(sourceChilds.item(index2).getAttributes()!=null &&
			sourceChilds.item(index2).getNodeName().equals(tag))
				nodeList.add(sourceChilds.item(index2));
		return nodeList;
	}


	private ArrayList<Node> findNonNullChilds(Node node) {
		ArrayList<Node> nodeList = new ArrayList<Node>();
		for(int index2 =0; index2 < node.getChildNodes().getLength(); index2++) 
			if(node.getChildNodes().item(index2).getAttributes()!=null)
				nodeList.add(node.getChildNodes().item(index2));
		return nodeList;
	}

	Node findTagElement(String element, String attr, String value){
		for(int index2 =0; index2 < document.getElementsByTagName(element).getLength(); index2++) 
			if(document.getElementsByTagName(element).item(index2).getAttributes().getNamedItem(attr).getNodeValue().equals(value))
				return document.getElementsByTagName(element).item(index2);
		return null;
	}

	public boolean readXMLFile(String path) {
		File file = new File(path);
		if(!new File(path).exists()) {
			Utils.log(getClass(),"The xml file " + path + " does not exist!");
			return false;
		}
		Utils.log(this.getClass(), "Loading the source/sink file " + path);
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder;
		try {
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
			document = documentBuilder.parse(file);
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

}
