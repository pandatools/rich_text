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
    kind = "CategoryVo", plural = "mycategoryvos", singular = "categoryvo")
public class CategoryVo extends AbstractExtension implements MyExtensionVoOperator {

    MetadataOperator metadata;

    Category.CategorySpec spec;

    Category.CategoryStatus status;

    Integer postCount;

    /**
     * Convert {@link Category} to {@link CategoryVo}.
     *
     * @param category category extension
     * @return category value object
     */
    public static CategoryVo from(Category category) {
        return CategoryVo.builder()
            .metadata(category.getMetadata())
            .spec(category.getSpec())
            .status(category.getStatus())
            .postCount(category.getStatusOrDefault().visiblePostCount)
            .build();
    }
}
