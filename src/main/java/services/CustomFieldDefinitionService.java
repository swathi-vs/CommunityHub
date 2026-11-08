package services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dao.CustomFieldDefinitionDAO;
import dao.CustomFieldOptionDAO;
import dao.FeatureRecordDAO;
import dao.FieldRecordAssociationDAO;
import dao.PropertyRecordDAO;
import model.CustomFieldDefinition;
import model.CustomFieldOption;
import model.FieldRecordAssociation;
import model.ServiceResult;
import utils.Db;

public class CustomFieldDefinitionService {

	private static final Map<String, String> PREFIX = new HashMap<>();
	private static final Map<String, Integer> MAX = new HashMap<>();

	static {
		slot("text", "str_col_", 10);
		slot("enum", "str_col_", 10);
		slot("number", "int_col_", 5);
		slot("decimal", "dec_col_", 3);
		slot("boolean", "bool_col_", 5);
		slot("date", "date_col_", 3);
		slot("time", "time_col_", 2);
		slot("textarea", "text_col_", 3);
	}

	private static void slot(String type, String prefix, int max) {
		PREFIX.put(type, prefix);
		MAX.put(type, max);
	}

	public static boolean isValidType(String fieldType) {
		return fieldType != null && PREFIX.containsKey(fieldType);
	}

	/** Creates the field, its column mapping and enum options in one transaction. */
	public static ServiceResult createFieldDefinition(CustomFieldDefinition field, List<CustomFieldOption> options) {
		String prefix = PREFIX.get(field.getFieldType());
		int max = MAX.get(field.getFieldType());
		return Db.tx(con -> {
			if (CustomFieldDefinitionDAO.fieldKeyExists(con, field.getFormId(), field.getFieldKey()))
				return ServiceResult.conflict("A field with this key already exists in the form");
			List<String> used = FieldRecordAssociationDAO.getUsedColumns(con, field.getFormId(), prefix);
			String columnName = null;
			for (int i = 1; i <= max; i++) {
				if (!used.contains(prefix + i)) {
					columnName = prefix + i;
					break;
				}
			}
			if (columnName == null)
				return ServiceResult.conflict(
						"Maximum of " + max + " " + field.getFieldType() + " fields reached for this form");

			Long fieldId = CustomFieldDefinitionDAO.createFieldDefinition(con, field);
			if (fieldId == null)
				return ServiceResult.error();
			FieldRecordAssociation fra = new FieldRecordAssociation();
			fra.setFormId(field.getFormId());
			fra.setFieldDefinitionId(fieldId);
			fra.setColumnName(columnName);
			FieldRecordAssociationDAO.associate(con, fra);
			if ("enum".equals(field.getFieldType()) && options != null) {
				for (CustomFieldOption option : options) {
					option.setFieldDefinitionId(fieldId);
					CustomFieldOptionDAO.createFieldOption(con, option);
				}
			}
			field.setId(fieldId);
			field.setColumnName(columnName);
			field.setIsActive(true);
			field.setOptions(options);
			return ServiceResult.ok("Field created", field);
		});
	}

	/**
	 * Deactivates the field and its column mapping, and clears the column in the form's
	 * records so the slot can be reused by a new field without showing old data.
	 */
	public static ServiceResult deactivateFieldDefinition(Long id, Long formId, Long organizationId) {
		if (CustomFieldDefinitionDAO.getById(id, formId, organizationId) == null)
			return ServiceResult.notFound("Field definition");
		return Db.tx(con -> {
			String column = FieldRecordAssociationDAO.getActiveColumn(con, id);
			CustomFieldDefinitionDAO.deactivate(con, id, organizationId);
			FieldRecordAssociationDAO.deactivateForField(con, id);
			if (column != null) {
				FeatureRecordDAO.clearColumn(con, formId, column);
				PropertyRecordDAO.clearColumn(con, formId, column);
			}
			return ServiceResult.ok("Field deleted");
		});
	}

	public static CustomFieldDefinition getById(Long id, Long formId, Long organizationId) {
		return CustomFieldDefinitionDAO.getById(id, formId, organizationId);
	}

	public static List<CustomFieldDefinition> getByFormId(Long formId) {
		List<CustomFieldDefinition> fields = CustomFieldDefinitionDAO.getByFormId(formId);
		for (CustomFieldDefinition field : fields) {
			if ("enum".equals(field.getFieldType()))
				field.setOptions(CustomFieldOptionDAO.getByFieldDefinitionId(field.getId()));
		}
		return fields;
	}
}
