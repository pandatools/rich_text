package run.halo.links.vo;

import org.springframework.lang.NonNull;
import run.halo.app.extension.MetadataOperator;

/**
 * An operator for extension value object.
 *
 * @author guqing
 * @since 2.0.0
 */
public interface MyExtensionVoOperator {

    @NonNull
    MetadataOperator getMetadata();
}
