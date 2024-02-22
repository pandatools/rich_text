package run.halo.links.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.util.Assert;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.extension.MetadataOperator;
import java.util.List;
@Data
@SuperBuilder
@ToString
@EqualsAndHashCode
public class MyListedSinglePageVo implements MyExtensionVoOperator {
    private MetadataOperator metadata;

    private SinglePage.SinglePageSpec spec;

    private SinglePage.SinglePageStatus status;

    private MyStatsVo stats;

    private List<MyContributorVo> contributors;

    private MyContributorVo owner;

    /*.
     *
     * @param singlePage single page extension
     * @return special page value object
     */
    public static MyListedSinglePageVo from(SinglePage singlePage) {
        Assert.notNull(singlePage, "The singlePage must not be null.");
        SinglePage.SinglePageSpec spec = singlePage.getSpec();
        SinglePage.SinglePageStatus pageStatus = singlePage.getStatus();
        return MyListedSinglePageVo.builder()
            .metadata(singlePage.getMetadata())
            .spec(spec)
            .status(pageStatus)
            .contributors(List.of())
            .build();
    }

}
