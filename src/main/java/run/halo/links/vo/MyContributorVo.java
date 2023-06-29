package run.halo.links.vo;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import run.halo.app.core.extension.User;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * A value object for {@link User}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Value
@ToString
@Builder
@EqualsAndHashCode(callSuper=false)
@GVK(group = "core.halo.run", version = "v1alpha1",
    kind = "MyContributorVo", plural = "MyContributorVo", singular = "MyContributorVo")
public class MyContributorVo extends AbstractExtension {
    String name;

    String displayName;

    String avatar;

    String bio;

    String permalink;

    /**
     * Convert {@link User} to {@link MyContributorVo}.
     *
     * @param user user extension
     * @return contributor value object
     */
    public static MyContributorVo from(User user) {
        User.UserStatus status = user.getStatus();
        String permalink = (status == null ? "" : status.getPermalink());
        return builder().name(user.getMetadata().getName())
            .displayName(user.getSpec().getDisplayName())
            .avatar(user.getSpec().getAvatar())
            .bio(user.getSpec().getBio())
            .permalink(permalink)
            .build();
    }
}
