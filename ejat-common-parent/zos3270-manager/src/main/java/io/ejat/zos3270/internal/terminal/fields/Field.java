package io.ejat.zos3270.internal.terminal.fields;

import java.util.List;

/**
 * <p>Represent a working field on the screen</p>
 * 
 * @author Michael Baylis
 *
 */
public abstract class Field {

	/**
	 * start position of this field, from zero. for an 80x24 screen,  the max would be 1919
	 */
	protected int start;
	/**
	 * end position of this field, from zero. for an 80x24 screen,  the max would be 1919
	 */
	protected int end;

	/**
	 * Create a field with a start and end position
	 * 
	 * @param start - buffer address offset 0
	 * @param end - buffer address offset 0
	 */
	public Field(int start, int end) {
		this.start  = start;
		this.end    = end;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (this.start == this.end) {
			return Integer.toString(this.start);
		}
		
		return Integer.toString(this.start) + "-" + Integer.toString(end);
	}

	/**
	 * Does this field occupy a specific position in the buffer
	 * 
	 * @param position - buffer address offset 0
	 * @return true if it does
	 */
	public boolean containsPosition(int position) {
		return (start <= position && position <= end);
	}

	/**
	 * Does this field occupy a range of buffer address, even if partially
	 * 
	 * @param checkStart - buffer address, offset 0
	 * @param checkEnd - buffer address, offset 0
	 * @return true if it does
	 */
	public boolean containsPositions(int checkStart, int checkEnd) {
		return (checkStart <= end && start <= checkEnd);
	}

	/**
	 * Split the field between these positions, inclusive.   This can be result in the field 
	 * being completely removed from the original fiels, or trimmed at the start or end, or 
	 * split in the middle and a new field created in original fields
	 * 
	 * @param originalFields - The full list of fields, incase we need to remove this one, or create a new one
	 * @param splitStart - buffer address offset 0
	 * @param splitEnd - buffer address offset 0
	 */
	public abstract void split(List<Field> originalFields, int splitStart, int splitEnd);

	/**
	 * Append a printable string representation of this field
	 * 
	 * @param sb - The StringBuilder to append to
	 */
	public abstract void getFieldString(StringBuilder sb);

	/**
	 * Get the start buffer address, inclusive
	 * 
	 * @return - buffer address, offset 0
	 */
	public int getStart() {
		return this.start;
	}

	/**
	 * Get the end buffer address, inclusive
	 * 
	 * @return - buffer address, offset 0
	 */
	public int getEnd() {
		return this.end;
	}

	/**
	 * Merge this field with the following next one.
	 * 
	 * @param allFields - All the fields to remove the following field
	 * @param nextField - The field to merge with
	 */
	public abstract void merge(List<Field> allFields, Field nextField);
	
}