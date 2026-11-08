package model;

/**
 * A validated value for one record slot column. The column name has been checked against
 * the form's active field mappings, so it is safe to place in SQL.
 */
public class ColumnValue {

	private final String column;
	private final Object value;

	public ColumnValue(String column, Object value) {
		this.column = column;
		this.value = value;
	}

	public String getColumn() {
		return column;
	}

	public Object getValue() {
		return value;
	}
}
