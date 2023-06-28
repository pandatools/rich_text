package run.halo.links;

import java.util.List;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Category;
import run.halo.app.extension.ListResult;

import run.halo.links.vo.MyCategoryTreeVo;
import run.halo.links.vo.MyCategoryVo;

/**
 * A finder for {@link Category}.
 *
 * @author guqing
 * @since 2.0.0
 */
public interface MyCategoryFinder {

    Mono<MyCategoryVo> getByName(String name);

    Flux<MyCategoryVo> getByNames(List<String> names);

    Mono<ListResult<MyCategoryVo>> list(@Nullable Integer page, @Nullable Integer size);

    Flux<MyCategoryVo> listAll();

    Flux<MyCategoryTreeVo> listAsTree();

    Flux<MyCategoryTreeVo> listAsTree(String name);

    Flux<MyCategoryTreeVo> getTreeByName(String name);
}
