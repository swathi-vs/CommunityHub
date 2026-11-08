package action;

import model.Post;
import services.PostService;
import utils.ModelMerger;

public class UpdatePostAction extends BaseAction {

	private static final long serialVersionUID = 1L;

	private Long organizationId;
	private Post post = new Post();

	public void setOrganizationId(Long v) {
		this.organizationId = v;
	}

	public Post getPost() {
		return post;
	}

	public void setPost(Post v) {
		this.post = v;
	}

	@Override
	public void validate() {
		if (post.getId() == null)
			addFieldError("post.id", "Post ID is missing");
		if (post.getVisibility() != null && !PostService.VISIBILITIES.contains(post.getVisibility()))
			addFieldError("post.visibility", "Invalid visibility");
	}

	@Override
	public String execute() {
		Post existing = PostService.getById(post.getId(), organizationId);
		if (existing == null)
			return notFound("Post");
		if (!existing.getAuthorUserId().equals(currentUserId()))
			return forbidden("You can only edit your own posts");
		post.setOrganizationId(null);
		post.setPropertyId(null);
		post.setAuthorUserId(null);
		ModelMerger.merge(existing, post);
		existing.setOrganizationId(organizationId);
		return respond(PostService.updatePost(existing));
	}
}
