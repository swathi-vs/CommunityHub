package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import model.Invoice;
import utils.Db;
import utils.Log;
import utils.QueryExecutor;
import utils.QueryFilter;
import utils.UpdateBuilder;

public class InvoiceDAO {

	private static Invoice map(ResultSet rs) throws SQLException {
		Invoice inv = new Invoice();
		inv.setId(rs.getLong("id"));
		inv.setOrganizationId(rs.getLong("organization_id"));
		inv.setPropertyId(rs.getLong("property_id"));
		inv.setTenancyId(rs.getLong("tenancy_id"));
		inv.setUserId(rs.getLong("user_id"));
		inv.setInvoiceNumber(rs.getString("invoice_number"));
		inv.setInvoiceType(rs.getString("invoice_type"));
		inv.setDescription(rs.getString("description"));
		inv.setTotalAmount(rs.getDouble("total_amount"));
		inv.setAmountPaid(rs.getDouble("amount_paid"));
		inv.setDueDate(rs.getDate("due_date"));
		inv.setStatus(rs.getString("status"));
		return inv;
	}

	public static Long createInvoice(Invoice invoice) {
		String query = "insert into invoices(organization_id, property_id, tenancy_id, user_id, invoice_number, invoice_type, "
				+ "description, total_amount, amount_paid, due_date, status, created_by) values(?,?,?,?,?,?,?,?,0,?,?,?)";
		try (Connection con = DBConnection.getConnection();
				PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
			ps.setLong(1, invoice.getOrganizationId());
			ps.setLong(2, invoice.getPropertyId());
			ps.setLong(3, invoice.getTenancyId());
			ps.setLong(4, invoice.getUserId());
			ps.setString(5, invoice.getInvoiceNumber());
			ps.setString(6, invoice.getInvoiceType());
			ps.setString(7, invoice.getDescription());
			ps.setDouble(8, invoice.getTotalAmount());
			ps.setDate(9, invoice.getDueDate());
			ps.setString(10, invoice.getStatus());
			Db.setLong(ps, 11, invoice.getCreatedBy());
			ps.executeUpdate();
			return Db.generatedId(ps);
		} catch (Exception e) {
			Log.error(e);
		}
		return null;
	}

	public static List<Invoice> getInvoices(Long orgId, QueryFilter filter) {
		return QueryExecutor.getList("i.organization_id = ?", orgId, filter, InvoiceDAO::map);
	}

	public static Invoice getById(Long id, Long orgId) {
		try (Connection con = DBConnection.getConnection()) {
			return getById(con, id, orgId, false);
		} catch (Exception e) {
			Log.error(e);
		}
		return null;
	}

	public static Invoice getById(Connection con, Long id, Long orgId, boolean forUpdate) throws SQLException {
		if (id == null || orgId == null)
			return null;
		String sql = "SELECT * FROM invoices WHERE id = ? AND organization_id = ?" + (forUpdate ? " FOR UPDATE" : "");
		try (PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setLong(1, id);
			ps.setLong(2, orgId);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? map(rs) : null;
			}
		}
	}

	public static void applyPayment(Connection con, Long invoiceId, double newAmountPaid, String status)
			throws SQLException {
		try (PreparedStatement ps = con
				.prepareStatement("update invoices set amount_paid = ?, status = ? where id = ?")) {
			ps.setDouble(1, newAmountPaid);
			ps.setString(2, status);
			ps.setLong(3, invoiceId);
			ps.executeUpdate();
		}
	}

	/** Updates editable fields. amount_paid is only changed by recording payments. */
	public static boolean updateInvoice(Invoice inv) {
		return new UpdateBuilder("invoices")
				.set("description", inv.getDescription())
				.set("invoice_type", inv.getInvoiceType())
				.set("total_amount", inv.getTotalAmount())
				.set("due_date", inv.getDueDate())
				.set("status", inv.getStatus())
				.where("id = ? AND organization_id = ?", inv.getId(), inv.getOrganizationId())
				.execute();
	}
}
