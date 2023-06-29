package run.halo.links.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

/**
 * Post navigation vo to hold previous and next item.
 *
 * @author guqing
 * @since 2.0.0
 */
@Value
@Builder
@EqualsAndHashCode(callSuper=false)
@GVK(group = "core.halo.run", version = "v1alpha1",
    kind = "MyNavigationPostVo", plural = "MyNavigationPostVo", singular = "MyNavigationPostVo")
public class MyNavigationPostVo extends AbstractExtension {

    @Schema(requiredMode = NOT_REQUIRED)
    MyPostVo previous;

    MyPostVo current;

    @Schema(requiredMode = NOT_REQUIRED)
    MyPostVo next;

    public boolean hasNext() {
        return next != null;
    }

    public boolean hasPrevious() {
        return previous != null;
    }

    public static MyNavigationPostVo empty() {
        return MyNavigationPostVo.builder().build();
    }
}
