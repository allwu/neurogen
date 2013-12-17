
import java.io.*;
import java.util.*;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

public class Population {

	private static String filename_tpl_g = "../template_gupdate.c";
	private static String filename_tpl_v = "../template_vupdate.c";
	private static String filename_tpl_gtop = "../template_gupdate_top.c";
	private static String filename_tpl_vtop = "../template_vupdate_top.c";

	private String	name;
	private int		size;
	private List<Synapse> syn;
	private List<Synapse> syn_out;

	private Circuit circuit;

	public String tSpkVar; 

	public Population(Element pop, Circuit cir) {

		syn = new ArrayList<Synapse>();
		syn_out = new ArrayList<Synapse>();
	
		circuit = cir;
		name = pop.attributeValue("name");
		size = Integer.parseInt(pop.attributeValue("size"));

		int spk_size;
		for (spk_size = 1; spk_size < size; spk_size *= 2);
		tSpkVar = "uint"+Integer.toString(spk_size);

		for (Iterator i=pop.elementIterator("synapse"); i.hasNext(); ) {

			Element doc_syn = (Element)i.next();

			Synapse s = new Synapse(doc_syn, this);
			syn.add(s);
		}
	}

	public List<Synapse> getSyn() { return syn; }
	
	public void addSynOut(Synapse _s) {
		
		_s.addPrePop(this);
		syn_out.add(_s);
	}

	private String strInputPara() {
		String res = "";

		for (int i=0; i<syn.size(); i++) {

			Synapse s = syn.get(i);
			if (!s.isInput) {
				// TODO to duplicate for unrolling
				res += "const unsigned short int spknum_"+s.getname()+",\n";
				res += "const unsigned short int* spkcell_"+s.getname()+",\n";

				res += "const unsigned short int* wt_"+s.getname()+",\n";
			}
			else {
				res += "const unsigned short int P_"+s.getname()+",\n";
				res += "unsigned long long* randin_"+s.getname()+",\n";
			}
		}

		return res;
	}

	private String strOutputPara() {
		String res = "";

		for (int i=0; i<syn.size(); i++) {

			Synapse s = syn.get(i);
			res += "unsigned short int* g_"+s.getname()+",\n";
			if (s.gettype().equals("NMDA")) {
				res += "unsigned short int* g_nmda_"+s.getname()+",\n";
			}
		}
		return res;
	}

	private String strDecayOp() {
		String res = "";

		for (int i=0; i<syn.size(); i++) {

			Synapse s = syn.get(i);
			res += s.decayOp();
		}
		return res;
	}
	
	private String strHLSpragma() {
	
		String pragma = "#pragma HLS dependence array intra false variable=";
		String res = "";

		for (int i=0; i<syn.size(); i++) {

			Synapse s = syn.get(i);
			res += pragma+"g_"+s.getname()+"\n";
			if (s.gettype().equals("NMDA"))
				res += pragma+"g_nmda_"+s.getname()+"\n";
			if (s.isInput)
				res += pragma+"randin_"+s.getname()+"\n";
		}
		return res;
	}

	private String strSpkRep(String src) {
		String res = "";

		for (int i=0; i<syn.size(); i++) {
			
			Synapse s = syn.get(i);
			if (s.isInput) continue;

			String rep = src.replace("/*SPK_NUM_VAR*/", "spknum_"+s.getname());
			rep = rep.replace("/*SPK_CELL_VAR*/", "spkcell_"+s.getname());
			rep = rep.replace("/*SPK_UPDATE*/", s.spkUpdate());

			res += rep;
		}

		return res;
	}

	private String strCurCom() {
		String res = "";

		for (int i=0; i<syn.size(); i++) {
			
			Synapse s = syn.get(i);
			if (s.isInput) continue;
			res += "unsigned int short"

		}

		return res;
	}


	public String getname() { return name; }
	public int getsize() { return size; }

	public String getVarDec() {

		String res = "";

		for (int i=0; i<syn.size(); i++) {

			Synapse s = syn.get(i);
			res += s.getVarDec();
			
		}
		res += "unsigned short int vm_"+name+"["+Integer.toString(0)+"];\n";
		res += "unsigned short int rp_"+name+"["+Integer.toString(0)+"];\n";
	
		return res;
	}

	public void gen_gupdate(){
		
		try {
			BufferedReader tmpl_in = new BufferedReader(new FileReader(filename_tpl_g));
			StringBuilder out = new StringBuilder();	
			try {
				String line = tmpl_in.readLine();

				String rep_code = "";
				boolean rep_begin = false;
				boolean rep_end = false;

				while (line != null) {

					if (line.contains("/*BEGIN_REPEAT_SPK*/")) {
						rep_begin = true;
					}
					else if (line.contains("/*END_REPEAT_SPK*/")) {
						rep_begin = false;

						rep_code = strSpkRep(rep_code);
						out.append(rep_code);
					}
					else {
						if (!rep_begin) {
							line = line.replace("/*CIRCUIT_NAME*/", circuit.getname());
							line = line.replace("/*POPULATION_SIZE*/", Integer.toString(size));
							line = line.replace("/*POPULATION_NAME*/", name);
							line = line.replace("/*INPUT_PARA*/", strInputPara());
							line = line.replace("/*OUTPUT_PARA*/", strOutputPara());
							line = line.replace("/*HLS_PRAGMA_DEPEND*/", strHLSpragma());
							line = line.replace("/*G_UPDATE*/", strDecayOp());

							out.append(line);
							out.append("\n");
						}
						else {
							rep_code += line + "\n";

						}
					}
					line = tmpl_in.readLine();	
				}
				//System.out.println(out.toString());
			}
			finally {
				tmpl_in.close();
			}
			String fout_name = "../"+circuit.getname()+"_"+name+"_g.c";
			PrintWriter fout = new PrintWriter(fout_name);

			fout.println(out);
			fout.close();
		} 
		catch (java.lang.Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public void gen_vupdate(){
		
		try {
			BufferedReader tmpl_in = new BufferedReader(new FileReader(filename_tpl_v));
			StringBuilder out = new StringBuilder();	
			try {
				String line = tmpl_in.readLine();

				while (line != null) {

					line = line.replace("/*CIRCUIT_NAME*/", circuit.getname());
					line = line.replace("/*POPULATION_SIZE*/", Integer.toString(size));
					line = line.replace("/*POPULATION_NAME*/", name);
					line = line.replace("/*OUT_SPK_TYPE*/", tSpkVar);
					//line = line.replace("/*PARA_SYNAPSE*/", strSynPara());
					//line = line.replace("/*PARA_SPK*/", strSpkPara());
					line = line.replace("/*CURRENT_COMPUTE*/", strCurCom());

					out.append(line);
					out.append("\n");

					line = tmpl_in.readLine();	
				}
				//System.out.println(out.toString());
			}
			finally {
				tmpl_in.close();
			}
			String fout_name = "../"+circuit.getname()+"_"+name+"_v.c";
			PrintWriter fout = new PrintWriter(fout_name);

			fout.println(out);
			fout.close();
		} 
		catch (java.lang.Exception e) {
			System.out.println(e.getMessage());
		}
	}
}

