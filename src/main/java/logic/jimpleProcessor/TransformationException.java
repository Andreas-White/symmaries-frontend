package logic.jimpleProcessor;

import logic.general.Utils;

public class TransformationException extends Exception {
	/**
	 * 
	 */

	// Parameterless Constructor
	public TransformationException() {
	}

	// Constructor that accepts a message
	public TransformationException(String message) {
		Utils.log(TransformationException.class, message);
		//System.exit(1);
	}
}
