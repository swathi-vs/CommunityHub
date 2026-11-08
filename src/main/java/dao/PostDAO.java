package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.Post;
import utils.Db;
import utils.Log;
import utils.QueryExecutor;
import utils.QueryFilter;
import utils.UpdateBuilder;

public class PostDAO {

	/** Condition (alias po) for posts inside their publish window. */
	public static final String PUBLISHED = "(po.publish_at is null or po.publish_at <= NOW()) "
			+ "and (po.expires_at is null or po.expires_at > NOW())";

	private static Post map(ResultSet rs) throws SQLException {
		Post p = new Post();
		p.setId(rs.getLong("id"));
		p.setOrganizationId(rs.getLong("organization_id"));
		p.setPropertyId(Db.getLong(rs, "property_id"));
		p.setAuthorUserId(rs.getLong("author_user_id"));
		p.setTitle(rs.getString("title"));
		p.setBody(rs.getString("body"));
		p.setVisibility(rs.getString("visibility"));
		p.setPublishAt(rs.getTimestamp("publish_at"));
		p.setExpiresAt(rs.getTimestamp("expires_at"));
		return p;
	}

	public static Long insertPost(Post post) {
		String query = "insert into posts(organization_id, property_id, author_user_id, title, body, visibility, publish_at, expires_at) "
				+ "values (?, ?, ?, ?, ?, ?, ?, ?)";
		try (Connection con = DBConnection.getConnection();
				PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
			ps.setLong(1, post.getOrganizationId());
			Db.setLong(ps, 2, post.getPropertyId());
			ps.setLong(3, post.getAuthorUserId());
			ps.setString(4, post.getTitle());
			ps.setString(5, post.getBody());
			ps.setString(6, post.getVisibility());
			ps.setTimestamp(7, post.getPublishAt());
			ps.setTimestamp(8, post.getExpiresAt());
			ps.executeUpdate();
			return Db.generatedId(ps);
		} catch (Exception e) {
			Log.error(e);
		}
		return null;
	}

	public static boolean deletePost(Long id, Long organizationId, Long deletedBy) {
		String sql = "UPDATE posts SET deleted_at = NOW(), deleted_by_user_id = ? "
				+ "WHERE id = ? AND organization_id = ? AND deleted_at IS NULL";
		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setLong(1, deletedBy);
			ps.setLong(2, id);
			ps.setLong(3, organizationId);
			return ps.executeUpdate() > 0;
		} catch (Exception e) {
			Log.error(e);
		}
		return false;
	}

	public static List<Post> getPosts(Long orgId, QueryFilter filter) {
		return QueryExecutor.getList("po.organization_id = ? and po.deleted_at is null", orgId, filter, PostDAO::map);
	}

	/**
	 * Posts a tenant may see: published, not admins_only, and either org-wide
	 * or targeted at the tenant's property.
	 */
	public static List<Post> getTenantPosts(Long orgId, Long tenantPropertyId, QueryFilter filter) {
		List<Object> params = new ArrayList<>();
		params.add(orgId);
		params.add(tenantPropertyId);
		return QueryExecutor.getList("po.organization_id = ? and po.deleted_at is null and po.visibility <> 'admins_only' and "
				+ PUBLISHED + " and (po.visibility = 'org_wide' or po.property_id is null or po.property_id = ?)", params,
				filter, PostDAO::map);
	}

	public static Post getById(Long id, Long orgId) {
		String sql = "SELECT * FROM posts WHERE id = ? AND organization_id = ? AND deleted_at IS NULL";
		try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setLong(1, id);
			ps.setLong(2, orgId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return map(rs);
			}
		} catch (Exception e) {
			Log.error(e);
		}
		return null;
	}

	public static boolean updatePost(Post p) {
		return new UpdateBuilder("posts")
				.set("title", p.getTitle())
				.set("body", p.getBody())
				.set("visibility", p.getVisibility())
				.set("publish_at", p.getPublishAt())
				.set("expires_at", p.getExpiresAt())
				.where("id = ? AND organization_id = ?", p.getId(), p.getOrganizationId())
				.execute();
	}
}
