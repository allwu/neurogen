
import java.io.*;
import java.util.*;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

public class NeuroGen {

	public static void main(String[] args) {

		String filename;
		if (args.length < 1) {
			System.out.println("must specify an input xml file");
			return;
		}

		filename = args[0];

		//System.out.println(filename);

		try {
			Document xmldoc = parse(filename);

			Circuit c = new Circuit(xmldoc);

			c.gen();

		}
		//catch (IOException ex) {
		//	System.out.println(ex.getMessage());
		//}
		catch (DocumentException ex) {
			System.out.println(ex.getMessage());
		}
	}

	public static Document parse(String src) throws DocumentException {
		SAXReader reader = new SAXReader();
		Document document = reader.read(src);
		return document;
	}
}
