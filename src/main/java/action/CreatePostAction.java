package action;

import model.Post;
import services.PostService;

public class CreatePostAction extends BaseAction {

	private static final long serialVersionUID = 1L;

	private Post post = new Post();

	public Post getPost() {
		return post;
	}

	public void setPost(Post post) {
		this.post = post;
	}

	@Override
	public void validate() {
		if (blank(post.getTitle()))
			addFieldError("post.title", "Title required");
		if (blank(post.getBody()))
			addFieldError("post.body", "Body required");
		if (!PostService.VISIBILITIES.contains(post.getVisibility()))
			addFieldError("post.visibility", "Visibility must be one of " + PostService.VISIBILITIES);
		if (post.getPublishAt() != null && post.getExpiresAt() != null
				&& !post.getExpiresAt().after(post.getPublishAt()))
			addFieldError("post.expiresAt", "Expiry must be after the publish time");
	}

	@Override
	public String execute() {
		post.setAuthorUserId(currentUserId());
		return respond(PostService.createPost(post));
	}
}
