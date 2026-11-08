package services;

import dao.FormRelationshipDAO;
import model.ServiceResult;
import utils.Db;

public class FormRelationshipService {

	/** Links the form to the property, replacing any previously linked form. */
	public static ServiceResult associate(Long propertyId, Long formId, Long organizationId) {
		if (PropertyService.getById(propertyId, organizationId) == null)
			return ServiceResult.notFound("Property");
		if (CustomFormService.getActiveById(formId, organizationId) == null)
			return ServiceResult.notFound("Form");
		return Db.tx(con -> {
			FormRelationshipDAO.associate(con, propertyId, formId);
			return ServiceResult.ok("Form linked to property");
		});
	}

	public static Long getFormIdByPropertyId(Long propertyId) {
		return FormRelationshipDAO.getFormIdByPropertyId(propertyId);
	}
}
