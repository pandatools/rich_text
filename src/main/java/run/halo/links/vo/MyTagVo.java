package run.halo.links.vo;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import run.halo.app.core.extension.content.Tag;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.extension.MetadataOperator;

/**
 * A value object for {@link Tag}.
 */
@Value
@Builder
@EqualsAndHashCode(callSuper=false)
@GVK(group = "core.halo.run", version = "v1alpha1",
    kind = "MyTagVo", plural = "MyTagVo", singular = "MyTagVo")
public class MyTagVo extends AbstractExtension implements MyExtensionVoOperator {

    MetadataOperator metadata;

    Tag.TagSpec spec;

    Tag.TagStatus status;

    Integer postCount;

    /**
     * Convert {@link Tag} to {@link MyTagVo}.
     *
     * @param tag tag extension
     * @return tag value object
     */
    public static MyTagVo from(Tag tag) {
        Tag.TagSpec spec = tag.getSpec();
        Tag.TagStatus status = tag.getStatusOrDefault();
        return MyTagVo.builder()
            .metadata(tag.getMetadata())
            .spec(spec)
            .status(status)
            .postCount(tag.getStatusOrDefault().getVisiblePostCount())
            .build();
    }
}
