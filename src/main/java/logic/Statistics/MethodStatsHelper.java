package logic.Statistics;

import java.util.HashMap;

public class MethodStatsHelper {
	String methodID;
	long scfg_generation_time;
	int nb_scfg_locations;
	int nb_scfg_transitions;
	int nb_scfg_variables;
	int nb_scfg_state_variables;
	int nb_scfg_input_variables;
	int nb_scfg_contr_variables;
	int max_scfg_in_degree;
	int max_scfg_out_degree;
	String summarization;
	long model_instantiation_time;
	long synthesis_time;
	long triangularization_time;
	boolean unsat_guard;
	String usedAlgorithm;
	// String guard;
	HashMap<String, String> checkpointGuardMaps = new HashMap<String, String>();

	public String exportStats() {
		return methodID + ";" + scfg_generation_time + ";" + nb_scfg_locations + ";"+ nb_scfg_transitions + ";"
				+ nb_scfg_variables + ";"+ nb_scfg_state_variables + ";"+ nb_scfg_input_variables + ";"
				+ nb_scfg_contr_variables + ";"+ max_scfg_in_degree + ";"+ max_scfg_out_degree + ";"+ summarization
				+ ";"+ model_instantiation_time + ";"+ synthesis_time + ";"+ triangularization_time + ";"
				+ unsat_guard;
	}
	
	public static String getSheetHeader() {
		return  "methodID;scfg_generation_time;nb_scfg_locations;nb_scfg_transitions;nb_scfg_variables;nb_scfg_state_variables;"
				+ "nb_scfg_input_variables;nb_scfg_contr_variables;max_scfg_in_degree;max_scfg_out_degree;summarization, "
						+ "model_instantiation_time;synthesis_time;triangularization_time;unsat_guard";
	}

	public String exportStatsForDataset() {
		return nb_scfg_locations + "," + nb_scfg_transitions + "," + nb_scfg_variables + "," + nb_scfg_state_variables
				+ "," + nb_scfg_input_variables + "," + nb_scfg_contr_variables + "," + max_scfg_in_degree + ","
				+ max_scfg_out_degree;
	}

	
	public long getTotalTime() {
		// TODO Auto-generated method stub
		return model_instantiation_time + synthesis_time + triangularization_time;
	}

	private boolean isBottomPC(String string) {
		return StringUnicodeEncoderDecoder.encodeStringToUnicodeSequence("pc = ⊥")
				.equals(StringUnicodeEncoderDecoder.encodeStringToUnicodeSequence(string.trim()));
	}

	public boolean isSecure() {
		for (String methodPoint : checkpointGuardMaps.keySet())
			//if (methodPoint.split("%")[0].equals("dummyMainClass:dummyMainMethod") || methodPoint.contains("%ok(")) 
				if (!checkpointGuardMaps.get(methodPoint).trim().equals("tt")
						&& !isBottomPC(checkpointGuardMaps.get(methodPoint)) 
						//&& !normalize(checkpointGuardMaps.get(methodPoint)).equals("")
						)
					return false;
		return true;
	}
}
