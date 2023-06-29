package run.halo.links.vo;

import com.fasterxml.jackson.databind.util.BeanUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;
import run.halo.app.core.extension.content.Post;
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
@ToString
@EqualsAndHashCode(callSuper = true)
@GVK(group = "core.halo.run", version = "v1alpha1",
    kind = "MyPostVo", plural = "MyPostVo", singular = "MyPostVo")
public class MyPostVo extends MyListedPostVo {

    private ContentVo content;

    private MetadataOperator metadata2;

    /**
     * Convert {@link Post} to {@link MyPostVo}.
     *
     * @param post post extension
     * @return post value object
     */
    public static MyPostVo from(Post post) {
        Assert.notNull(post, "The post must not be null.");
        Post.PostSpec spec = post.getSpec();
        Post.PostStatus postStatus = post.getStatusOrDefault();
        // return MyPostVo.builder()
        //     .metadata(post.getMetadata())
        //     .spec(spec)
        //     .status(postStatus)
        //     .categories(List.of())
        //     .tags(List.of())
        //     .contributors(List.of())
        //     .content(new ContentVo(null, null))
        //     .build();

        System.out.println("post.getMetadata()post.getMetadata()post.getMetadata() = " + post.getMetadata());
        MyPostVo myPostVo = new MyPostVo();
        myPostVo.setMetadata(post.getMetadata());
        myPostVo.setMetadata2(post.getMetadata());
        myPostVo.setSpec(spec);
        myPostVo.setStats(MyStatsVo.empty());
        myPostVo.setCategories(List.of());
        myPostVo.setTags(List.of());
        myPostVo.setContributors(List.of());
        myPostVo.setContent(new ContentVo(null, null));
        return myPostVo;
    }


    // public MyPostVo() {
    //     super();
    // }

    // public MyPostVo() {
    // }

    /**
     * Convert {@link Post} to {@link MyPostVo}.
     */
    public static MyPostVo from(MyListedPostVo postVo) {
        MyPostVo myPostVo = new MyPostVo();
        BeanUtils.copyProperties(postVo,myPostVo);
        myPostVo.setContent(new ContentVo("", ""));
        myPostVo.setMetadata(postVo.getMetadata());
        myPostVo.setMetadata2(postVo.getMetadata());
        return myPostVo;
        // return builder()
        //     .metadata(postVo.getMetadata())
        //     .spec(postVo.getSpec())
        //     .status(postVo.getStatus())
        //     .categories(postVo.getCategories())
        //     .tags(postVo.getTags())
        //     .contributors(postVo.getContributors())
        //     .owner(postVo.getOwner())
        //     .stats(postVo.getStats())
        //     .content(new ContentVo("", ""))
        //     .build();
    }
}
