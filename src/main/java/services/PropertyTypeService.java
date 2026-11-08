package services;

import java.util.List;

import dao.PropertyTypeDAO;
import model.PropertyType;

public class PropertyTypeService {

	public static List<PropertyType> getPropertyTypes() {

		return PropertyTypeDAO.getPropertyTypes();
	}
}