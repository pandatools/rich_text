package run.halo.links.vo;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * Stats value object.
 *
 * @author guqing
 * @since 2.0.0
 */
@Value
@Builder
@EqualsAndHashCode(callSuper=false)
@GVK(group = "core.halo.run", version = "v1alpha1",
    kind = "MyStatsVo", plural = "MyStatsVo", singular = "MyStatsVo")
public class MyStatsVo extends AbstractExtension {

    Integer visit;

    Integer upvote;

    Integer comment;

    public static MyStatsVo empty() {
        return MyStatsVo.builder()
            .visit(0)
            .upvote(0)
            .comment(0)
            .build();
    }
}
