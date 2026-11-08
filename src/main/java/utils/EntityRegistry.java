package utils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Per-entity query metadata for list endpoints: table, alias, base join, selected columns,
 * allowed filters (with the column and optional join each needs) and allowed sort columns.
 * Only keys registered here can reach SQL, so filter and sort input is never concatenated raw.
 */
public class EntityRegistry {

	public static class EntityDef {
		final String table;
		final String alias;
		final String baseJoin;
		final String select;
		final Map<String, String> filterColumns = new LinkedHashMap<>();
		final Map<String, String> filterJoins = new HashMap<>();
		final Map<String, String> sortColumns = new LinkedHashMap<>();
		final String defaultSort;
		final String defaultDir;

		EntityDef(String table, String alias, String baseJoin, String select, String defaultSort, String defaultDir) {
			this.table = table;
			this.alias = alias;
			this.baseJoin = baseJoin;
			this.select = select;
			this.defaultSort = defaultSort;
			this.defaultDir = defaultDir;
		}

		EntityDef filter(String key, String column) {
			filterColumns.put(key, column);
			return this;
		}

		EntityDef filter(String key, String column, String join) {
			filterColumns.put(key, column);
			filterJoins.put(key, join);
			return this;
		}

		EntityDef sort(String key, String column) {
			sortColumns.put(key, column);
			return this;
		}
	}

	private static final Map<String, EntityDef> entities = new HashMap<>();

	static {
		entities.put("users", new EntityDef("users", "u", "join user_roles ur on ur.user_id = u.id",
				"distinct u.id, u.full_name, u.email, u.phone, u.role, u.gender, u.nationality, u.date_of_birth, "
						+ "u.status, u.created_at",
				"created_at", "DESC")
				.filter("status", "u.status")
				.filter("full_name", "u.full_name")
				.filter("email", "u.email")
				.filter("role_id", "ur.role_id")
				.filter("property_id", "ur.property_id")
				.filter("role", "rl.name", "join roles rl on rl.id = ur.role_id")
				.sort("created_at", "u.created_at")
				.sort("full_name", "u.full_name")
				.sort("email", "u.email"));

		entities.put("properties", new EntityDef("properties", "p", "",
				"p.id, p.organization_id, p.property_type_id, p.name, p.address_line1, p.city, p.state, p.pincode, p.status",
				"created_at", "DESC")
				.filter("status", "p.status")
				.filter("city", "p.city")
				.filter("property_type_id", "p.property_type_id")
				.sort("created_at", "p.created_at")
				.sort("name", "p.name")
				.sort("city", "p.city"));

		entities.put("tenancies", new EntityDef("tenancies", "t", "",
				"t.id, t.organization_id, t.property_id, t.user_id, t.room_id, t.bed_id, t.start_date, "
						+ "t.expected_end_date, t.deposit_amount, t.deposit_status, t.status",
				"start_date", "DESC")
				.filter("status", "t.status")
				.filter("user_id", "t.user_id")
				.filter("property_id", "t.property_id")
				.filter("tenant_name", "tu.full_name", "join users tu on tu.id = t.user_id")
				.filter("room_number", "r.room_number", "join rooms r on r.id = t.room_id")
				.sort("created_at", "t.created_at")
				.sort("start_date", "t.start_date")
				.sort("expected_end_date", "t.expected_end_date"));

		entities.put("invoices", new EntityDef("invoices", "i", "",
				"i.id, i.organization_id, i.property_id, i.tenancy_id, i.user_id, i.invoice_number, i.invoice_type, "
						+ "i.description, i.total_amount, i.amount_paid, i.due_date, i.status",
				"due_date", "ASC")
				.filter("status", "i.status")
				.filter("invoice_type", "i.invoice_type")
				.filter("user_id", "i.user_id")
				.filter("tenancy_id", "i.tenancy_id")
				.filter("property_id", "i.property_id")
				.sort("created_at", "i.created_at")
				.sort("due_date", "i.due_date")
				.sort("total_amount", "i.total_amount")
				.sort("amount_paid", "i.amount_paid"));

		entities.put("payments", new EntityDef("payments", "pay", "",
				"pay.id, pay.organization_id, pay.invoice_id, pay.amount, pay.method, pay.reference, pay.paid_at, "
						+ "pay.recorded_by_user_id, pay.notes",
				"paid_at", "DESC")
				.filter("method", "pay.method")
				.filter("invoice_id", "pay.invoice_id")
				.filter("user_id", "pi.user_id", "join invoices pi on pi.id = pay.invoice_id")
				.sort("created_at", "pay.created_at")
				.sort("paid_at", "pay.paid_at")
				.sort("amount", "pay.amount"));

		entities.put("posts", new EntityDef("posts", "po", "",
				"po.id, po.organization_id, po.property_id, po.author_user_id, po.title, po.body, po.visibility, "
						+ "po.publish_at, po.expires_at",
				"created_at", "DESC")
				.filter("visibility", "po.visibility")
				.filter("property_id", "po.property_id")
				.filter("author_user_id", "po.author_user_id")
				.sort("created_at", "po.created_at")
				.sort("publish_at", "po.publish_at")
				.sort("expires_at", "po.expires_at"));

		entities.put("forms", new EntityDef("custom_forms", "cf", "",
				"cf.id, cf.organization_id, cf.form_name, cf.description, cf.is_active, cf.created_at",
				"created_at", "DESC")
				.filter("is_active", "cf.is_active")
				.sort("created_at", "cf.created_at")
				.sort("form_name", "cf.form_name"));
	}

	public static EntityDef get(String entity) {
		EntityDef def = entities.get(entity);
		if (def == null)
			throw new IllegalArgumentException("Unknown entity: " + entity);
		return def;
	}
}
