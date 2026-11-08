package services;

import dao.BedDAO;
import dao.TenancyDAO;
import model.Bed;
import model.ServiceResult;
import utils.Db;

public class BedService {

	public static ServiceResult getBeds(Long roomId, Long propertyId, Long organizationId, String status) {
		if (RoomService.getRoom(roomId, propertyId, organizationId) == null)
			return ServiceResult.notFound("Room");
		return ServiceResult.ok("Beds", BedDAO.getBeds(roomId, status));
	}

	public static ServiceResult createBed(Bed bed, Long propertyId, Long organizationId) {
		if (RoomService.getRoom(bed.getRoomId(), propertyId, organizationId) == null)
			return ServiceResult.notFound("Room");
		Long id = BedDAO.insertBed(bed, organizationId, propertyId);
		if (id == null)
			return ServiceResult.error();
		bed.setId(id);
		bed.setStatus(BedDAO.AVAILABLE);
		return ServiceResult.ok("Bed created", bed);
	}

	/**
	 * Updates label and/or status. Occupancy is managed by tenancies, so a bed cannot be set
	 * to "occupied" manually, and an occupied bed's status cannot be changed here.
	 */
	public static ServiceResult updateBed(Bed bed, Long organizationId) {
		if (BedDAO.getById(bed.getId(), organizationId) == null)
			return ServiceResult.notFound("Bed");
		if (BedDAO.OCCUPIED.equals(bed.getStatus()))
			return ServiceResult.badRequest("Beds become occupied by creating a tenancy");
		return Db.tx(con -> {
			Bed current = BedDAO.lockBed(con, bed.getId());
			if (current == null)
				return ServiceResult.notFound("Bed");
			if (bed.getStatus() != null && !bed.getStatus().equals(current.getStatus())
					&& TenancyDAO.hasActiveForBed(con, bed.getId()))
				return ServiceResult.conflict("Bed is held by an active tenancy");
			if (!BedDAO.updateBed(con, bed))
				return ServiceResult.badRequest("Nothing to update");
			return ServiceResult.ok("Bed updated");
		});
	}
}
