
import java.io.*;
import java.util.*;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

public class Synapse {
	private String name;
	private String type;	
	private String pre_pop;
	private String rev_e;
	private String tau;
	private String A;
	public int wt_type; // 0: no wt; 1: func; 2: indirect; 3: direct

	private static String tmpl_randinput = 
		"randin_NAME[i] = randin_NAME[i] * 1103515245 + 12345;\n" +
		"unsigned int rk_NAME = (randin_NAME[i]/65536u)&((unsigned long)8191);\n" +
		"if (rk_NAME < P_NAME) \n" +
		"g_NAME[i] -= (g_NAME[i] >> TAU) - REPL_A;\n" +
		"else \n" + 
		"g_NAME[i] -= (g_NAME[i] >> TAU);\n";

	public boolean isFuncWt;
	public boolean isInput;

	private Population pre;
	private Population post;

	public Synapse(Element syn, Population _post) {
		name = syn.attributeValue("name");
		pre_pop = syn.element("pre").getText();

		if (syn.element("weights") == null)
			wt_type = 0;
		else if (syn.element("weights").getText().equals("FUNC"))
			wt_type = 1;
		else if (syn.element("weights").getText().equals("INDIRECT"))
			wt_type = 2;
		else
			wt_type = 3;
		
		post = _post;
		
		Element channel = syn.element("type");

		type = channel.getText();

		if (pre_pop.equals("INPUT")) {
			System.out.println(name+" is input");
			isInput = true;
		}
		else
			isInput = false;

		if (channel.attributeValue("egaba") != null)
			rev_e = channel.attributeValue("egaba");
		else
			rev_e = "e_zero";

		if (channel.attributeValue("A") != null)
			A = channel.attributeValue("A");
		else
			A = "A_zero";

		if (channel.attributeValue("tau") != null)
			tau = channel.attributeValue("tau");
		else
			tau = "tau_zero";

		System.out.println("Add Synapse["+name+"] with type["+type+"], pre-synaptic population is "+pre_pop);
	}

	public String gettype() { return type; }
	public String getname() { return name; }
	public String getPrePop() { return pre_pop; }

	public String getVarDec() {

		String res = "";
		
		int post_size = post.getsize();

		if (!isInput) {
			int pre_size = pre.getsize();

			// TODO to duplicate for unrolling
			res += "unsigned short int spknum_"+name+";\n";
			res += "unsigned short int spkcell_"+name+"["+Integer.toString(pre_size)+"];\n";

			if (wt_type == 1)
				res += "unsigned short int wt_"+name+"["+Integer.toString(post_size*2)+"];\n";
			else if (wt_type == 2) {
				res += "unsigned short int wt_one_"+name+"["+Integer.toString(post_size)+"];\n";
				res += "unsigned short int wt_two_"+name+"["+Integer.toString(pre_size*2)+"];\n";
			}
			else if (wt_type == 3) {
				res += "unsigned short int wt_"+name+"["+Integer.toString(post_size*pre_size)+"];\n";
			}
		}
		else {
			res += "unsigned short int P_"+name+";\n";
			res += "unsigned long long randin_"+name+"["+Integer.toString(post_size)+"];\n";
		}
		res += "unsigned short int g_"+name+"["+Integer.toString(post_size)+"];\n";
		if (type.equals("NMDA")) {
			res += "unsigned short int g_nmda_"+name+"["+Integer.toString(post_size)+"];\n";
		}
		return res;
	}

	public String decayOp() {
		String res = "";
		if (isInput) {
			String randin = tmpl_randinput.replace("NAME", name);
			randin = randin.replace("TAU", tau);
			randin = randin.replace("REPL_A", A);
			res += randin;
		}
		else {
			res += "g_"+name+" -= g_"+name+"[i]>>"+tau+";\n";
			if (type.equals("NMDA")) {
				res += "g_nmda_"+name+" -= g_nmda_"+name+"[i]>>"+tau+";\n";
			}
		}
		return res;	
	}

	public String spkUpdate() {
		String res = "";
		
		if (wt_type == 1) {
			// TODO: should be pre.getsize();
			res += "g_"+name+"[i] += ";
			if (type.equals("NMDA"))
				res += "("+A+" - g_nmda_"+name+"[j])";
			else
				res += A;

			res += " / wt_"+name+"["+post.getsize()+"+i-j];\n";
		}

		return res;
	}

	public void addPrePop(Population _p) {
		pre = _p;
	}
}
