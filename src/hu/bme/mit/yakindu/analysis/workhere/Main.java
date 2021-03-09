package hu.bme.mit.yakindu.analysis.workhere;

import java.util.ArrayList;
import java.util.Properties;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.junit.Test;
import org.yakindu.sct.model.sgraph.Effect;
import org.yakindu.sct.model.sgraph.Reaction;
import org.yakindu.sct.model.sgraph.ReactionProperty;
import org.yakindu.sct.model.sgraph.State;
import org.yakindu.sct.model.sgraph.Transition;
import org.yakindu.sct.model.sgraph.Statechart;
import org.yakindu.sct.model.stext.stext.impl.EventDefinitionImpl;
import org.yakindu.sct.model.stext.stext.impl.VariableDefinitionImpl;

import hu.bme.mit.model2gml.Model2GML;
import hu.bme.mit.yakindu.analysis.modelmanager.ModelManager;

public class Main {
	@Test
	public void test() {
		main(new String[0]);
	}
	
	public static void main(String[] args) {
		ModelManager manager = new ModelManager();
		Model2GML model2gml = new Model2GML();
		
		// Loading model
		EObject root = manager.loadModel("model_input/example.sct");
		
		// Reading model
		Statechart s = (Statechart) root;
		TreeIterator<EObject> iterator = s.eAllContents();
		ArrayList<String> variableNames = new ArrayList<String>();
		ArrayList<String> eventNames = new ArrayList<String>();
		while (iterator.hasNext()) {
			EObject content = iterator.next();
			if(content instanceof EventDefinitionImpl) {
				EventDefinitionImpl edi = (EventDefinitionImpl) content;
				eventNames.add(edi.getName());
			} else if(content instanceof VariableDefinitionImpl) {
				VariableDefinitionImpl vdi = (VariableDefinitionImpl) content;
				variableNames.add(vdi.getName());
			}
		}
		
		// Generate code
		System.out.println("public class RunStatechart {\n\n" + 
							"\tpublic static void main(String[} args) throws IOException {\n" +
							"\t\tExampleStatemachine s = new ExampleStatemachine();\n" +
							"\t\ts.setTimer(new TimerService());\n" +
							"\t\tRuntimeService.getInstance().registerStatemachine(s, 200);\n" +
							"\t\ts.init();\n" +
							"\t\ts.enter();\n" +
							"\t\tboolean running = true;\n" +
							"\t\tScanner scanner = new Scanner(System.in);\n" +
							"\t\twhile(running) {\n" +
							"\t\t\tString input = Scanner.next();\n" +
							"\t\t\tswitch(input) {");
		
		for(String name: eventNames) {
			String capitalizedName = name.substring(0, 1).toUpperCase() + name.substring(1);
			System.out.println("\t\t\t\tcase \"" + name + "\":\n" +
								"\t\t\t\t\ts.raise" + capitalizedName + "();\n" +
								"\t\t\t\t\ts.runCycle();\n" +
								"\t\t\t\t\tbreak;");
		}
		System.out.println("\t\t\t\tcase \"exit\":\n" +
							"\t\t\t\t\trunning = false;\n" +
							"\t\t\t\t\tbreak;\n" +
							"\t\t\t\tdefault:\n" +
							"\t\t\t\t\tbreak;\n" +
							"\t\t\t}\n" +
							"\t\t\tprint(s);\n" +
							"\t\t}\n" +
							"\t\tscanner.close();\n" +
							"\t\tSystem.exit(0);\n" +
							"\t}\n");
		
		System.out.println("\tpublic static void print(IExampleStatemachine s) {");
		for(String name: variableNames) {
			String capitalizedName = name.substring(0, 1).toUpperCase() + name.substring(1);
			System.out.println("\t\tSystem.out.println(\"" + capitalizedName.charAt(0) + " = \" + s.getSCInterface().get" + capitalizedName + "());");
		}
		System.out.println("\t}");
		System.out.println("}");
		
		// Transforming the model into a graph representation
		String content = model2gml.transform(root);
		// and saving it
		manager.saveFile("model_output/graph.gml", content);
	}
}
