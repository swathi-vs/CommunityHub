package services;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dao.CustomFieldOptionDAO;
import dao.FeatureRecordDAO;
import dao.FieldRecordAssociationDAO;
import dao.FieldRecordAssociationDAO.FieldSpec;
import dao.PropertyRecordDAO;
import model.ColumnValue;
import model.CustomFieldOption;
import model.FieldValue;
import model.ServiceResult;
import utils.Db;
import utils.Json;

/**
 * Validates and stores custom-form submissions. Payload format:
 * {"fields":[{"columnName":"str_col_1","value":"..."}, {"fieldKey":"floors","value":"3"}]}
 * Each entry may identify its field by columnName or fieldKey. Only the form's active fields
 * are accepted, values are converted to the field's type and required fields are enforced.
 */
public class RecordService {

	private static final int MAX_STRING = 255;

	public static ServiceResult savePropertyRecord(Long propertyId, Long orgId, Long formId, String payload) {
		if (propertyId == null || orgId == null || formId == null)
			return ServiceResult.badRequest("Property, organization and form are required");
		ServiceResult parsed = validate(formId, payload);
		if (!parsed.isSuccess())
			return parsed;
		return Db.tx(con -> savePropertyRecord(con, propertyId, orgId, formId, parsed));
	}

	/** Saves a property record inside the caller's transaction. */
	public static ServiceResult savePropertyRecord(Connection con, Long propertyId, Long orgId, Long formId,
			ServiceResult validated) throws Exception {
		@SuppressWarnings("unchecked")
		List<ColumnValue> values = (List<ColumnValue>) validated.getData();
		PropertyRecordDAO.save(con, propertyId, orgId, formId, values);
		return ServiceResult.ok("Property details saved");
	}

	public static ServiceResult saveFeatureRecord(Long formId, Long organizationId, Long tenancyId, String payload) {
		if (formId == null || organizationId == null || tenancyId == null)
			return ServiceResult.badRequest("Form, organization and tenancy are required");
		ServiceResult parsed = validate(formId, payload);
		if (!parsed.isSuccess())
			return parsed;
		@SuppressWarnings("unchecked")
		List<ColumnValue> values = (List<ColumnValue>) parsed.getData();
		Long id = FeatureRecordDAO.save(formId, organizationId, tenancyId, values);
		if (id == null)
			return ServiceResult.error();
		Map<String, Object> data = new HashMap<>();
		data.put("recordId", id);
		return ServiceResult.ok("Record saved", data);
	}

	public static List<FieldValue> getPropertyRecord(Long propertyId) {
		return PropertyRecordDAO.getByPropertyId(propertyId);
	}

	public static List<FieldValue> getFeatureRecord(Long recordId) {
		return FeatureRecordDAO.getByRecordId(recordId);
	}

	/** On success the result's data is the List of validated ColumnValues. */
	public static ServiceResult validate(Long formId, String payload) {
		JsonArray fields;
		try {
			JsonObject json = Json.GSON.fromJson(payload, JsonObject.class);
			fields = json == null ? null : json.getAsJsonArray("fields");
		} catch (Exception e) {
			return ServiceResult.badRequest("Payload must be JSON like {\"fields\":[{\"columnName\":...,\"value\":...}]}");
		}
		if (fields == null || fields.size() == 0)
			return ServiceResult.badRequest("No fields submitted");

		List<FieldSpec> specs = FieldRecordAssociationDAO.getActiveFields(formId);
		if (specs.isEmpty())
			return ServiceResult.badRequest("Form has no active fields");
		Map<String, FieldSpec> byColumn = new HashMap<>();
		Map<String, FieldSpec> byKey = new HashMap<>();
		for (FieldSpec s : specs) {
			byColumn.put(s.columnName, s);
			byKey.put(s.fieldKey, s);
		}

		List<ColumnValue> values = new ArrayList<>();
		Set<String> seen = new HashSet<>();
		try {
		for (JsonElement el : fields) {
			if (!el.isJsonObject())
				return ServiceResult.badRequest("Each field must be an object");
			JsonObject f = el.getAsJsonObject();
			FieldSpec spec = null;
			if (f.has("columnName") && !f.get("columnName").isJsonNull())
				spec = byColumn.get(f.get("columnName").getAsString());
			else if (f.has("fieldKey") && !f.get("fieldKey").isJsonNull())
				spec = byKey.get(f.get("fieldKey").getAsString());
			if (spec == null)
				return ServiceResult.badRequest("Unknown field in payload");
			if (!seen.add(spec.columnName))
				return ServiceResult.badRequest("Field submitted twice: " + spec.fieldLabel);
			JsonElement rawEl = f.get("value");
			if (rawEl != null && !rawEl.isJsonNull() && !rawEl.isJsonPrimitive())
				return ServiceResult.badRequest("Value for " + spec.fieldLabel + " must be a single value");
			String raw = (rawEl == null || rawEl.isJsonNull()) ? null : rawEl.getAsString();
			if (raw != null && raw.trim().isEmpty())
				raw = null;
			if (raw == null) {
				if (spec.required)
					return ServiceResult.badRequest(spec.fieldLabel + " is required");
				values.add(new ColumnValue(spec.columnName, null));
				continue;
			}
			Object converted;
			try {
				converted = convert(spec, raw);
			} catch (IllegalArgumentException e) {
				return ServiceResult.badRequest("Invalid value for " + spec.fieldLabel + ": " + e.getMessage());
			}
			values.add(new ColumnValue(spec.columnName, converted));
		}
		} catch (IllegalStateException | UnsupportedOperationException e) {
			return ServiceResult.badRequest("Malformed field entry in payload");
		}
		for (FieldSpec s : specs) {
			if (s.required && !seen.contains(s.columnName))
				return ServiceResult.badRequest(s.fieldLabel + " is required");
		}
		return ServiceResult.ok("Valid", values);
	}

	private static Object convert(FieldSpec spec, String raw) {
		String col = spec.columnName;
		if (col.startsWith("str_")) {
			if (raw.length() > MAX_STRING)
				throw new IllegalArgumentException("must be at most " + MAX_STRING + " characters");
			if ("enum".equals(spec.fieldType)) {
				boolean allowed = false;
				for (CustomFieldOption o : CustomFieldOptionDAO.getByFieldDefinitionId(spec.fieldDefinitionId)) {
					if (raw.equals(o.getOptionValue())) {
						allowed = true;
						break;
					}
				}
				if (!allowed)
					throw new IllegalArgumentException("not one of the allowed options");
			}
			return raw;
		}
		if (col.startsWith("text_"))
			return raw;
		try {
			if (col.startsWith("int_"))
				return Long.parseLong(raw.trim());
			if (col.startsWith("dec_"))
				return Double.parseDouble(raw.trim());
			if (col.startsWith("bool_")) {
				String b = raw.trim().toLowerCase();
				if (!b.equals("true") && !b.equals("false"))
					throw new IllegalArgumentException("must be true or false");
				return Boolean.parseBoolean(b);
			}
			if (col.startsWith("datetime_")) {
				String t = raw.trim().replace('T', ' ');
				if (t.length() == 16)
					t = t + ":00";
				return java.sql.Timestamp.valueOf(t);
			}
			if (col.startsWith("date_"))
				return java.sql.Date.valueOf(raw.trim());
			if (col.startsWith("time_")) {
				String t = raw.trim();
				if (t.length() == 5)
					t = t + ":00";
				return java.sql.Time.valueOf(t);
			}
		} catch (IllegalArgumentException e) {
			if (e.getMessage() != null && e.getMessage().startsWith("must be"))
				throw e;
			throw new IllegalArgumentException("wrong format for " + spec.fieldType);
		}
		throw new IllegalArgumentException("unsupported field type");
	}
}
