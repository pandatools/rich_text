package run.halo.links.vo;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.util.Assert;
import run.halo.app.core.extension.content.Category;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.extension.MetadataOperator;
import java.util.List;
import java.util.Objects;

/**
 * A tree vo for {@link Category}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
@Builder
@ToString
@EqualsAndHashCode(callSuper = true)
@GVK(group = "core.halo.run", version = "v1alpha1",
    kind = "MyCategoryTreeVo", plural = "mycategorytreevos", singular = "mycategoryvo")
public class MyCategoryTreeVo
    extends AbstractExtension
    implements MyVisualizableTreeNode<MyCategoryTreeVo>, MyExtensionVoOperator {

    private MetadataOperator metadata;

    private Category.CategorySpec spec;

    private Category.CategoryStatus status;

    private List<MyCategoryTreeVo> children;

    private String parentName;

    private Integer postCount;

    /**
     * Convert {@link MyCategoryVo} to {@link MyCategoryTreeVo}.
     *
     * @param category category value object
     * @return category tree value object
     */
    public static MyCategoryTreeVo from(MyCategoryVo category) {
        Assert.notNull(category, "The category must not be null");
        return MyCategoryTreeVo.builder()
            .metadata(category.getMetadata())
            .spec(category.getSpec())
            .status(category.getStatus())
            .children(List.of())
            .postCount(Objects.requireNonNullElse(category.getPostCount(), 0))
            .build();
    }

    @Override
    public String nodeText() {
        return String.format("%s (%s)", getSpec().getDisplayName(), getPostCount());
    }
}
