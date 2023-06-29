package run.halo.links.vo;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import run.halo.app.core.extension.content.Snapshot;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * A value object for Content from {@link Snapshot}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Value
@ToString
@Builder
@EqualsAndHashCode(callSuper=false)
@GVK(group = "core.halo.run", version = "v1alpha1",
    kind = "ContentVo", plural = "ContentVo", singular = "ContentVo")
public class ContentVo extends AbstractExtension {

    String raw;

    String content;

    /**
     * Empty content object.
     */
    public static ContentVo empty() {
        return ContentVo.builder()
            .raw("")
            .content("")
            .build();
    }
}
