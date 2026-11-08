package action;

import services.PostService;

public class DeletePostAction extends BaseAction {

	private static final long serialVersionUID = 1L;

	private Long postId;
	private Long organizationId;

	public void setPostId(Long v) {
		this.postId = v;
	}

	public void setOrganizationId(Long v) {
		this.organizationId = v;
	}

	@Override
	public void validate() {
		if (postId == null)
			addFieldError("postId", "Post required");
	}

	@Override
	public String execute() {
		return respond(PostService.deletePost(postId, organizationId, currentUserId(),
				hasCapability("post.delete.any")));
	}
}
