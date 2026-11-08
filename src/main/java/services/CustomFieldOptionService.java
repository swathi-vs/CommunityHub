package services;

import dao.CustomFieldOptionDAO;
import model.CustomFieldDefinition;
import model.CustomFieldOption;
import model.ServiceResult;

public class CustomFieldOptionService {

	public static ServiceResult createFieldOption(CustomFieldOption option, Long formId, Long organizationId) {
		CustomFieldDefinition field = CustomFieldDefinitionService.getById(option.getFieldDefinitionId(), formId,
				organizationId);
		if (field == null)
			return ServiceResult.notFound("Field definition");
		if (!"enum".equals(field.getFieldType()))
			return ServiceResult.badRequest("Options can only be added to enum fields");
		Long id = CustomFieldOptionDAO.createFieldOption(option);
		if (id == null)
			return ServiceResult.error();
		option.setId(id);
		return ServiceResult.ok("Option created", option);
	}
}
