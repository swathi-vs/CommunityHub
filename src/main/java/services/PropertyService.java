package services;

import java.util.List;

import dao.BedDAO;
import dao.FormRelationshipDAO;
import dao.PropertyDAO;
import dao.PropertyTypeDAO;
import dao.RoomDAO;
import dao.TenancyDAO;
import model.Property;
import model.ServiceResult;
import utils.Db;
import utils.QueryFilter;

public class PropertyService {

	/**
	 * Creates the property. When formId and payload are given, also links the form and saves
	 * the property's custom details, all in one transaction.
	 */
	public static ServiceResult createProperty(Property property, Long formId, String payload) {
		if (!PropertyTypeDAO.exists(property.getPropertyTypeId()))
			return ServiceResult.notFound("Property type");
		boolean withDetails = payload != null && !payload.trim().isEmpty();
		ServiceResult validated = null;
		if (withDetails) {
			if (CustomFormService.getActiveById(formId, property.getOrganizationId()) == null)
				return ServiceResult.notFound("Form");
			validated = RecordService.validate(formId, payload);
			if (!validated.isSuccess())
				return validated;
		}
		final ServiceResult details = validated;
		return Db.tx(con -> {
			Long propertyId = PropertyDAO.insertProperty(con, property);
			if (propertyId == null)
				return ServiceResult.error();
			if (withDetails) {
				FormRelationshipDAO.associate(con, propertyId, formId);
				RecordService.savePropertyRecord(con, propertyId, property.getOrganizationId(), formId, details);
			}
			property.setId(propertyId);
			property.setStatus("draft");
			return ServiceResult.ok("Property created", property);
		});
	}

	public static Property getById(Long id, Long organizationId) {
		return PropertyDAO.getById(id, organizationId);
	}

	public static List<Property> getProperties(Long organizationId, QueryFilter filter) {
		return PropertyDAO.getProperties(organizationId, filter);
	}

	/** Soft-deletes the property with its rooms, beds and form link. Blocked while tenancies are active. */
	public static ServiceResult deleteProperty(Long id, Long organizationId) {
		if (PropertyDAO.getById(id, organizationId) == null)
			return ServiceResult.notFound("Property");
		return Db.tx(con -> {
			if (TenancyDAO.hasActiveForProperty(con, id))
				return ServiceResult.conflict("Property has active tenancies. Terminate them first.");
			BedDAO.softDeleteByProperty(con, id);
			RoomDAO.softDeleteByProperty(con, id);
			FormRelationshipDAO.deactivateForProperty(con, id);
			if (!PropertyDAO.softDelete(con, id, organizationId))
				return ServiceResult.notFound("Property");
			return ServiceResult.ok("Property deleted");
		});
	}

	public static ServiceResult updateProperty(Property property) {
		if (property.getPropertyTypeId() != null && !PropertyTypeDAO.exists(property.getPropertyTypeId()))
			return ServiceResult.notFound("Property type");
		return PropertyDAO.updateProperty(property) ? ServiceResult.ok("Property updated", property)
				: ServiceResult.error();
	}
}
