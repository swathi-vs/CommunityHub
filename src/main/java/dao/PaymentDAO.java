package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import model.Payment;
import utils.Db;
import utils.QueryExecutor;
import utils.QueryFilter;

public class PaymentDAO {

	private static Payment map(ResultSet rs) throws SQLException {
		Payment p = new Payment();
		p.setId(rs.getLong("id"));
		p.setOrganizationId(rs.getLong("organization_id"));
		p.setInvoiceId(rs.getLong("invoice_id"));
		p.setAmount(rs.getDouble("amount"));
		p.setMethod(rs.getString("method"));
		p.setReference(rs.getString("reference"));
		p.setPaidAt(rs.getTimestamp("paid_at"));
		p.setRecordedUserId(Db.getLong(rs, "recorded_by_user_id"));
		p.setNotes(rs.getString("notes"));
		return p;
	}

	public static Long createPayment(Connection con, Payment payment) throws SQLException {
		String query = "insert into payments(organization_id, invoice_id, amount, method, reference, paid_at, recorded_by_user_id, notes) "
				+ "values(?,?,?,?,?,?,?,?)";
		try (PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
			ps.setLong(1, payment.getOrganizationId());
			ps.setLong(2, payment.getInvoiceId());
			ps.setDouble(3, payment.getAmount());
			ps.setString(4, payment.getMethod());
			ps.setString(5, payment.getReference());
			ps.setTimestamp(6, payment.getPaidAt());
			Db.setLong(ps, 7, payment.getRecordedUserId());
			ps.setString(8, payment.getNotes());
			ps.executeUpdate();
			return Db.generatedId(ps);
		}
	}

	public static List<Payment> getPayments(Long orgId, QueryFilter filter) {
		return QueryExecutor.getList("pay.organization_id = ?", orgId, filter, PaymentDAO::map);
	}
}
