package run.halo.links.vo;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

@Data
@EqualsAndHashCode(callSuper = true)
@GVK(kind = "Pcdn", group = "pcdn.plugin.halo.run",
    version = "v1alpha1", singular = "pcdn", plural = "pcdn")
public class Pcdn extends AbstractExtension {

    @Schema(requiredMode = REQUIRED)
    private PcdnSpec spec;

    @Data
    public static class PcdnSpec {

        @Schema(requiredMode = REQUIRED)
        private String accesskey;
        @Schema(requiredMode = REQUIRED)
        private String secret;
        @Schema(requiredMode = REQUIRED)
        private String ObjectPath;
        @Schema(requiredMode = REQUIRED)
        private String ObjectType;
        @Schema(defaultValue = "false")
        private Boolean done;
    }
}