package org.prelle.ansi;

import java.util.List;

/**
 * 
 */
public interface ANSIInputStreamFilter {

	//-------------------------------------------------------------------
	/**
	 * Does the filter feel responsible for handling this element?
	 * @param element
	 * @return
	 */
	boolean handles(AParsedElement event);
	
	//-------------------------------------------------------------------
	/**
	 * @param event
	 * @return The events that can be passed on - may be empty or more than one
	 */
	List<AParsedElement> process(AParsedElement event) ;
	
}
