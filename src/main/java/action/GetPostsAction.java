package action;

import model.Post;
import services.PostService;
import utils.QueryFilter;
import utils.TenantScoped;

public class GetPostsAction extends BaseAction implements TenantScoped {

	private static final long serialVersionUID = 1L;

	private Long organizationId;
	private Long postId;
	private String visibility;
	private Long propertyId;
	private int page = 1, size = 20;
	private String orderBy, orderDir;
	private Long scopedUserId;

	@Override
	public void setScopedUserId(Long v) {
		this.scopedUserId = v;
	}

	public void setOrganizationId(Long v) {
		this.organizationId = v;
	}

	public void setPostId(Long v) {
		this.postId = v;
	}

	public void setVisibility(String v) {
		this.visibility = v;
	}

	public void setPropertyId(Long v) {
		this.propertyId = v;
	}

	public void setPage(int v) {
		this.page = v;
	}

	public void setSize(int v) {
		this.size = v;
	}

	public void setOrderBy(String v) {
		this.orderBy = v;
	}

	public void setOrderDir(String v) {
		this.orderDir = v;
	}

	@Override
	public String execute() {
		if (postId != null) {
			Post post = PostService.getById(postId, organizationId);
			if (post == null || (scopedUserId != null && !PostService.isVisibleToTenant(post, scopedUserId)))
				return notFound("Post");
			return ok("Post", post);
		}
		if (scopedUserId != null) {
			QueryFilter filter = new QueryFilter("posts").orderBy(orderBy, orderDir).page(page, size);
			return ok("Posts", PostService.getTenantPosts(organizationId, scopedUserId, filter));
		}
		QueryFilter filter = new QueryFilter("posts").add("visibility", visibility).add("property_id", propertyId)
				.orderBy(orderBy, orderDir).page(page, size);
		return ok("Posts", PostService.getPosts(organizationId, filter));
	}
}
