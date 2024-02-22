package run.halo.links;

import reactor.core.publisher.Mono;
import run.halo.links.vo.MySinglePageVo;
import java.util.Map;

public interface MySinglePageFinder {
    Map<String,String> getAnnotationsByArticle(String name, String patternString);
    Mono<MySinglePageVo> getByName(String pageName);
}
