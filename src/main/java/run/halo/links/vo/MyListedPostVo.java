package run.halo.links.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.util.Assert;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.extension.MetadataOperator;
import java.util.List;

/**
 * A value object for {@link Post}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
// @SuperBuilder
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper=false)
@GVK(group = "core.halo.run", version = "v1alpha1",
    kind = "MyListedPostVo", plural = "MyListedPostVo", singular = "MyListedPostVo")
public class MyListedPostVo extends AbstractExtension implements MyExtensionVoOperator {

    private MetadataOperator metadata;

    private Post.PostSpec spec;

    private Post.PostStatus status;

    private List<CategoryVo> categories;

    private List<MyTagVo> tags;

    private List<MyContributorVo> contributors;

    private MyContributorVo owner;

    private MyStatsVo stats;

    /**
     * Convert {@link Post} to {@link MyListedPostVo}.
     *
     * @param post post extension
     * @return post value object
     */
    public static MyListedPostVo from(Post post) {
        Assert.notNull(post, "The post must not be null.");
        Post.PostSpec spec = post.getSpec();
        Post.PostStatus postStatus = post.getStatusOrDefault();
        return MyListedPostVo.builder()
            .metadata(post.getMetadata())
            .spec(spec)
            .status(postStatus)
            .categories(List.of())
            .tags(List.of())
            .contributors(List.of())
            .build();
    }

}
