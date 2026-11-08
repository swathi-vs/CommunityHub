package services;

import java.sql.Connection;
import java.util.List;
import java.util.Objects;

import dao.BedDAO;
import dao.PropertyDAO;
import dao.RoomDAO;
import dao.TenancyDAO;
import dao.UserDAO;
import model.Bed;
import model.Room;
import model.ServiceResult;
import model.Tenancy;
import utils.Db;
import utils.QueryFilter;

public class TenancyService {

	/**
	 * Creates a tenancy and marks its bed occupied. Checks that the user is a member of the
	 * organization without another active tenancy, and that the room and bed belong to the
	 * property and the bed is available.
	 */
	public static ServiceResult createTenancy(Tenancy tenancy) {
		Long orgId = tenancy.getOrganizationId();
		if (PropertyDAO.getById(tenancy.getPropertyId(), orgId) == null)
			return ServiceResult.notFound("Property");
		if (UserDAO.getById(tenancy.getUserId(), orgId) == null)
			return ServiceResult.notFound("User in this organization");
		tenancy.setDepositStatus(tenancy.getDepositStatus() == null ? "held" : tenancy.getDepositStatus());
		tenancy.setStatus(TenancyDAO.ACTIVE);
		if (tenancy.getDepositAmount() == null)
			tenancy.setDepositAmount(0.0);
		return Db.tx(con -> {
			if (TenancyDAO.hasActiveForUser(con, tenancy.getUserId(), orgId))
				return ServiceResult.conflict("User already has an active tenancy in this organization");
			ServiceResult placement = claimBed(con, tenancy);
			if (!placement.isSuccess())
				return placement;
			Long id = TenancyDAO.createTenancy(con, tenancy);
			if (id == null)
				return ServiceResult.error();
			tenancy.setId(id);
			return ServiceResult.ok("Tenancy created", tenancy);
		});
	}

	/** Validates room/bed placement for the tenancy and marks the bed occupied. */
	private static ServiceResult claimBed(Connection con, Tenancy t) throws Exception {
		Room room = RoomDAO.getById(con, t.getRoomId(), t.getOrganizationId());
		if (room == null || !room.getPropertyId().equals(t.getPropertyId()))
			return ServiceResult.badRequest("Room does not belong to this property");
		Bed bed = BedDAO.lockBed(con, t.getBedId());
		if (bed == null || !bed.getRoomId().equals(room.getId()))
			return ServiceResult.badRequest("Bed does not belong to this room");
		if (!BedDAO.AVAILABLE.equals(bed.getStatus()) || TenancyDAO.hasActiveForBed(con, bed.getId()))
			return ServiceResult.conflict("Bed is not available");
		BedDAO.setStatus(con, bed.getId(), BedDAO.OCCUPIED);
		return ServiceResult.ok();
	}

	private static boolean holdsBed(String status) {
		return TenancyDAO.ACTIVE.equals(status);
	}

	/**
	 * Applies the merged changes and keeps bed occupancy in step: terminating frees the bed,
	 * moving to another bed frees the old one and occupies the new one, and re-activating
	 * re-claims the bed.
	 */
	public static ServiceResult updateTenancy(Tenancy updated) {
		if (updated.getStartDate() != null && updated.getExpectedEndDate() != null
				&& updated.getExpectedEndDate().before(updated.getStartDate()))
			return ServiceResult.badRequest("Expected end date must be after start date");
		return Db.tx(con -> {
			Tenancy current = TenancyDAO.getById(con, updated.getId(), updated.getOrganizationId(), true);
			if (current == null)
				return ServiceResult.notFound("Tenancy");
			updated.setPropertyId(current.getPropertyId());
			updated.setUserId(current.getUserId());

			boolean heldBefore = holdsBed(current.getStatus());
			boolean holdsAfter = holdsBed(updated.getStatus());
			boolean bedChanged = !Objects.equals(current.getBedId(), updated.getBedId())
					|| !Objects.equals(current.getRoomId(), updated.getRoomId());

			if (holdsAfter && !heldBefore
					&& TenancyDAO.hasActiveForUser(con, current.getUserId(), current.getOrganizationId()))
				return ServiceResult.conflict("User already has an active tenancy in this organization");

			if (heldBefore && (!holdsAfter || bedChanged) && current.getBedId() != null)
				BedDAO.setStatus(con, current.getBedId(), BedDAO.AVAILABLE);
			if (holdsAfter && (!heldBefore || bedChanged)) {
				ServiceResult placement = claimBed(con, updated);
				if (!placement.isSuccess())
					return placement;
			}
			if (!TenancyDAO.updateTenancy(con, updated))
				return ServiceResult.error();
			return ServiceResult.ok("Tenancy updated", updated);
		});
	}

	public static Long getActiveTenancyId(Long userId, Long organizationId) {
		if (userId == null || organizationId == null)
			return null;
		return TenancyDAO.getActiveTenancyId(userId, organizationId);
	}

	public static Tenancy getActiveTenancy(Long userId, Long organizationId) {
		if (userId == null || organizationId == null)
			return null;
		return TenancyDAO.getActiveTenancy(userId, organizationId);
	}

	public static Long getUserIdByTenancy(Long tenancyId, Long organizationId) {
		return TenancyDAO.getUserIdByTenancy(tenancyId, organizationId);
	}

	public static List<Tenancy> getTenancies(Long orgId, QueryFilter filter) {
		return TenancyDAO.getTenancies(orgId, filter);
	}

	public static Tenancy getById(Long id, Long orgId) {
		return TenancyDAO.getById(id, orgId);
	}
}
