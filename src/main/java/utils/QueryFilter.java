package utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class QueryFilter {

	private static final int MAX_PAGE_SIZE = 100;

	private final EntityRegistry.EntityDef def;
	private final Map<String, Object> conditions = new LinkedHashMap<>();
	private String orderColumn;
	private String orderDir;
	private int limit = 20;
	private int offset = 0;

	public QueryFilter(String entity) {
		this.def = EntityRegistry.get(entity);
		this.orderColumn = def.sortColumns.get(def.defaultSort);
		this.orderDir = def.defaultDir;
	}

	public QueryFilter add(String key, Object value) {
		if (value == null)
			return this;
		if (value instanceof String && ((String) value).trim().isEmpty())
			return this;
		if (!def.filterColumns.containsKey(key)) {
			Log.warn("Filter not allowed: " + key + " for " + def.table);
			return this;
		}
		conditions.put(key, value);
		return this;
	}

	public QueryFilter orderBy(String key, String direction) {
		if (key != null && def.sortColumns.containsKey(key))
			this.orderColumn = def.sortColumns.get(key);
		if (direction != null) {
			String dir = direction.trim().toUpperCase();
			if (dir.equals("ASC") || dir.equals("DESC"))
				this.orderDir = dir;
		}
		return this;
	}

	public QueryFilter page(int page, int size) {
		if (page < 1)
			page = 1;
		if (size < 1)
			size = 20;
		if (size > MAX_PAGE_SIZE)
			size = MAX_PAGE_SIZE;
		this.limit = size;
		this.offset = (page - 1) * size;
		return this;
	}

	String getTable() {
		return def.table;
	}

	String getAlias() {
		return def.alias;
	}

	String getSelectCols() {
		return def.select;
	}

	String buildJoinClause() {
		StringBuilder sb = new StringBuilder();
		if (!def.baseJoin.isEmpty())
			sb.append(" ").append(def.baseJoin);
		Set<String> added = new LinkedHashSet<>();
		for (String key : conditions.keySet()) {
			String join = def.filterJoins.get(key);
			if (join != null && added.add(join))
				sb.append(" ").append(join);
		}
		return sb.toString();
	}

	String buildWhereClause() {
		StringBuilder where = new StringBuilder();
		for (String key : conditions.keySet())
			where.append(" and ").append(def.filterColumns.get(key)).append(" = ?");
		return where.toString();
	}

	List<Object> getParamValues() {
		return new ArrayList<>(conditions.values());
	}

	String getOrderColumn() {
		return orderColumn != null ? orderColumn : def.alias + ".id";
	}

	String getOrderDir() {
		return orderDir;
	}

	int getLimit() {
		return limit;
	}

	int getOffset() {
		return offset;
	}
}
