package run.halo.links;

import org.springframework.lang.NonNull;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.ListResult;
import run.halo.links.vo.MyListedPostVo;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.Predicate;

public interface MyPostPublicQueryService {
    Predicate<Post> FIXED_PREDICATE = post -> post.isPublished()
        && Objects.equals(false, post.getSpec().getDeleted())
        && Post.VisibleEnum.PUBLIC.equals(post.getSpec().getVisible());

    /**
     * Converts post to listed post vo.
     *
     * @param post post must not be null
     * @return listed post vo
     */
    Mono<MyListedPostVo> convertToListedPostVo(@NonNull Post post);
}
