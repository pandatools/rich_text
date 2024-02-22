package run.halo.links.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.util.Assert;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.extension.GVK;
import java.util.List;

@Data
@SuperBuilder
@ToString
@EqualsAndHashCode(callSuper = true)
public class MySinglePageVo extends MyListedSinglePageVo{
    private ContentVo content;
    public static MySinglePageVo from(SinglePage singlePage) {
        Assert.notNull(singlePage, "The singlePage must not be null.");
        SinglePage.SinglePageSpec spec = singlePage.getSpec();
        SinglePage.SinglePageStatus pageStatus = singlePage.getStatus();
        System.out.println("singlepagemeta"+singlePage.getMetadata());
        return MySinglePageVo.builder()
            .metadata(singlePage.getMetadata())
            .spec(spec)
            .status(pageStatus)
            .contributors(List.of())
            .content(new ContentVo(null, null))
            .build();
    }
}
