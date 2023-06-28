package run.halo.links.vo;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import run.halo.app.core.extension.content.Category;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.extension.MetadataOperator;

/**
 * A value object for {@link Category}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Value
@Builder
@EqualsAndHashCode(callSuper = true)
@GVK(group = "core.halo.run", version = "v1alpha1",
    kind = "MyCategoryVo", plural = "mycategoryvos", singular = "mycategoryvo")
public class MyCategoryVo extends AbstractExtension implements MyExtensionVoOperator {

    MetadataOperator metadata;

    Category.CategorySpec spec;

    Category.CategoryStatus status;

    Integer postCount;

    /**
     * Convert {@link Category} to {@link MyCategoryVo}.
     *
     * @param category category extension
     * @return category value object
     */
    public static MyCategoryVo from(Category category) {
        return MyCategoryVo.builder()
            .metadata(category.getMetadata())
            .spec(category.getSpec())
            .status(category.getStatus())
            .postCount(category.getStatusOrDefault().visiblePostCount)
            .build();
    }
}
