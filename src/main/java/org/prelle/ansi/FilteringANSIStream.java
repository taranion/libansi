package org.prelle.ansi;

/**
 * 
 */
public interface FilteringANSIStream {


	//-------------------------------------------------------------------
	boolean hasFilter(ANSIInputStreamFilter filter) ;

	//-------------------------------------------------------------------
	boolean addFilter(ANSIInputStreamFilter filter);
	boolean addFilter(int position, ANSIInputStreamFilter filter);
	
	//-------------------------------------------------------------------
	boolean removeFilter(ANSIInputStreamFilter filter);

}
