package org.prelle.ansi;

/**
 * Interface for stream components that support an ANSIInputStreamFilter filter pipeline.
 */
public interface FilteringANSIStream {

	//-------------------------------------------------------------------
	/**
	 * Checks if the given filter is registered.
	 *
	 * @param filter The filter to check
	 * @return true if the filter is registered; false otherwise
	 */
	boolean hasFilter(ANSIInputStreamFilter filter);

	//-------------------------------------------------------------------
	/**
	 * Appends a filter to the end of the filter pipeline.
	 *
	 * @param filter The filter to add
	 * @return true if the filter was added successfully
	 */
	boolean addFilter(ANSIInputStreamFilter filter);

	/**
	 * Inserts a filter at the specified position in the filter pipeline.
	 *
	 * @param position Index at which to insert the filter
	 * @param filter The filter to add
	 * @return true if the filter was added successfully
	 */
	boolean addFilter(int position, ANSIInputStreamFilter filter);

	//-------------------------------------------------------------------
	/**
	 * Removes a filter from the filter pipeline.
	 *
	 * @param filter The filter to remove
	 * @return true if the filter was removed; false otherwise
	 */
	boolean removeFilter(ANSIInputStreamFilter filter);

}
