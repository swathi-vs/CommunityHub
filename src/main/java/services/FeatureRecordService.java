package services;

import java.util.List;

import dao.FeatureRecordDAO;
import model.FeatureRecordSummary;
import model.FieldValue;

public class FeatureRecordService {

	public static List<FeatureRecordSummary> getByFormId(Long formId, Long orgId) {
		return FeatureRecordDAO.getByFormId(formId, orgId);
	}

	public static List<FeatureRecordSummary> getByFormAndUser(Long formId, Long organizationId, Long userId) {
		return FeatureRecordDAO.getByFormAndUser(formId, organizationId, userId);
	}

	/** Owner user id of the record, or null when it is not in this form and organization. */
	public static Long getRecordOwner(Long recordId, Long formId, Long orgId) {
		return FeatureRecordDAO.getRecordOwner(recordId, formId, orgId);
	}

	public static List<FieldValue> getById(Long recordId) {
		return FeatureRecordDAO.getByRecordId(recordId);
	}
}
