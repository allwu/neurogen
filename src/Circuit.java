
import java.io.*;
import java.util.*;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

public class Circuit {

	private static String filename_tpl_top = "../template_top.c";

	private String name;
	private List<Population> popl;

	public Circuit(Document xmldoc) {
		
		popl = new ArrayList<Population>();

		Element root = xmldoc.getRootElement();
		name = root.attributeValue("name");

		for (Iterator i=root.elementIterator("population"); i.hasNext(); ) {
			Element doc_pop = (Element)i.next();

			Population p = new Population(doc_pop, this);
			popl.add(p);
			String pname = doc_pop.attributeValue("name");
		}
		// enumerate population and link pre-synaptic population
		for (int i=0; i<popl.size(); i++ ) {
			
			Population p = popl.get(i);
			for (int j=0; j<p.getSyn().size(); j++) {
				Synapse s = p.getSyn().get(j);

				String pre_name = s.getPrePop();

				for (int k=0; k<popl.size(); k++) {
					if (popl.get(k).getname().equals(pre_name)) {
						popl.get(k).addSynOut(s);
					}
				}
			}

		}
	}

	public String strVarDec() {
		String res = "";
		for (int i=0; i<popl.size(); i++ ) {
			
			Population p = popl.get(i);
			res += p.getVarDec();
		}
		return res;
	}

	public void gen_top() {
		try {
			BufferedReader tmpl_in = new BufferedReader(new FileReader(filename_tpl_top));
			StringBuilder out = new StringBuilder();	
			try {
				String line = tmpl_in.readLine();

				while (line != null) {

					line = line.replace("/*CIRCUIT_NAME*/", name);
					line = line.replace("/*DECL_VAR*/", strVarDec());

					out.append(line);
					out.append("\n");

					line = tmpl_in.readLine();	
				}
				System.out.println(out.toString());
			}
			finally {
				tmpl_in.close();
			}
			String fout_name = "../"+name+"_top.c";
			PrintWriter fout = new PrintWriter(fout_name);

			fout.println(out);
			fout.close();
		} 
		catch (java.lang.Exception e) {
			System.out.println(e.getMessage());
		}
	
	}

	public void gen() {

		for (int i=0; i<popl.size(); i++) {
			Population p = popl.get(i);
			System.out.println("[Circuit] gen population: "+p.getname());
			p.gen_gupdate();
			p.gen_vupdate();
		}

		gen_top();
	}

	public String getname() { return name; }

}
