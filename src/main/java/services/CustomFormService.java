package services;

import java.util.List;

import dao.CustomFieldDefinitionDAO;
import dao.CustomFormDAO;
import dao.FieldRecordAssociationDAO;
import dao.FormRelationshipDAO;
import model.CustomForm;
import model.ServiceResult;
import utils.Db;
import utils.QueryFilter;

public class CustomFormService {

	public static ServiceResult createForm(CustomForm form) {
		Long id = CustomFormDAO.createForm(form);
		if (id == null)
			return ServiceResult.error();
		form.setId(id);
		form.setIsActive(true);
		return ServiceResult.ok("Form created", form);
	}

	/** Deactivates the form together with its fields, column mappings and property links. */
	public static ServiceResult deactivateForm(Long id, Long organizationId) {
		return Db.tx(con -> {
			if (!CustomFormDAO.deactivateForm(con, id, organizationId))
				return ServiceResult.notFound("Form");
			CustomFieldDefinitionDAO.deactivateForForm(con, id);
			FieldRecordAssociationDAO.deactivateForForm(con, id);
			FormRelationshipDAO.deactivateForForm(con, id);
			return ServiceResult.ok("Form deleted");
		});
	}

	public static List<CustomForm> getForms(Long orgId, QueryFilter filter) {
		return CustomFormDAO.getForms(orgId, filter);
	}

	public static CustomForm getById(Long id, Long orgId) {
		if (id == null || orgId == null)
			return null;
		return CustomFormDAO.getById(id, orgId);
	}

	/** An active form of the organization, or null. */
	public static CustomForm getActiveById(Long id, Long orgId) {
		CustomForm form = getById(id, orgId);
		return form != null && Boolean.TRUE.equals(form.getIsActive()) ? form : null;
	}
}
