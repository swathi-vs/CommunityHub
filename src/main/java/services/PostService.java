package services;

import java.util.Collections;
import java.util.List;

import dao.PostDAO;
import dao.PropertyDAO;
import model.Post;
import model.ServiceResult;
import model.Tenancy;
import utils.QueryFilter;

public class PostService {

	public static final List<String> VISIBILITIES = java.util.Arrays.asList("org_wide", "property", "residents_only",
			"admins_only");

	public static ServiceResult createPost(Post post) {
		if (PropertyDAO.getById(post.getPropertyId(), post.getOrganizationId()) == null)
			return ServiceResult.notFound("Property");
		Long id = PostDAO.insertPost(post);
		if (id == null)
			return ServiceResult.error();
		post.setId(id);
		return ServiceResult.ok("Post created", post);
	}

	/** Deletes any post (canDeleteAny) or only the user's own post. */
	public static ServiceResult deletePost(Long id, Long organizationId, Long userId, boolean canDeleteAny) {
		Post post = PostDAO.getById(id, organizationId);
		if (post == null)
			return ServiceResult.notFound("Post");
		if (!canDeleteAny && !post.getAuthorUserId().equals(userId))
			return ServiceResult.forbidden("You can only delete your own posts");
		return PostDAO.deletePost(id, organizationId, userId) ? ServiceResult.ok("Post deleted") : ServiceResult.error();
	}

	public static List<Post> getPosts(Long orgId, QueryFilter filter) {
		return PostDAO.getPosts(orgId, filter);
	}

	/** Posts visible to a tenant: published, visibility "all", org-wide or for their property. */
	public static List<Post> getTenantPosts(Long orgId, Long userId, QueryFilter filter) {
		Tenancy tenancy = TenancyService.getActiveTenancy(userId, orgId);
		if (tenancy == null)
			return Collections.emptyList();
		return PostDAO.getTenantPosts(orgId, tenancy.getPropertyId(), filter);
	}

	public static boolean isVisibleToTenant(Post post, Long userId) {
		if (!VISIBILITIES.contains(post.getVisibility()) || "admins_only".equals(post.getVisibility()))
			return false;
		long now = System.currentTimeMillis();
		if (post.getPublishAt() != null && post.getPublishAt().getTime() > now)
			return false;
		if (post.getExpiresAt() != null && post.getExpiresAt().getTime() <= now)
			return false;
		if ("org_wide".equals(post.getVisibility()) || post.getPropertyId() == null)
			return true;
		Tenancy tenancy = TenancyService.getActiveTenancy(userId, post.getOrganizationId());
		return tenancy != null && post.getPropertyId().equals(tenancy.getPropertyId());
	}

	public static Post getById(Long id, Long orgId) {
		return PostDAO.getById(id, orgId);
	}

	public static ServiceResult updatePost(Post post) {
		if (post.getPublishAt() != null && post.getExpiresAt() != null && !post.getExpiresAt().after(post.getPublishAt()))
			return ServiceResult.badRequest("Expiry must be after the publish time");
		return PostDAO.updatePost(post) ? ServiceResult.ok("Post updated", post) : ServiceResult.error();
	}
}
