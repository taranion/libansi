package org.prelle.ansi;

import java.util.List;

/**
 * Filter interface for processing and transforming AParsedElement stream fragments.
 */
public interface ANSIInputStreamFilter {

	//-------------------------------------------------------------------
	/**
	 * Determines whether this filter processes the given element.
	 *
	 * @param event The fragment to evaluate
	 * @return true if this filter should process the event; false otherwise
	 */
	boolean handles(AParsedElement event);
	
	//-------------------------------------------------------------------
	/**
	 * Processes the input fragment and returns the transformed list of fragments.
	 *
	 * @param event The fragment to process
	 * @return A list of replacement fragments (may be empty if consumed, or contain multiple fragments)
	 */
	List<AParsedElement> process(AParsedElement event);
	
}
