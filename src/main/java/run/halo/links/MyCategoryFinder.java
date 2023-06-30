package run.halo.links;

import java.util.List;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Category;
import run.halo.app.extension.ListResult;

import run.halo.links.vo.CategoryTreeVo;
import run.halo.links.vo.CategoryVo;

/**
 * A finder for {@link Category}.
 *
 * @author guqing
 * @since 2.0.0
 */
public interface MyCategoryFinder {

    Mono<CategoryVo> getByName(String name);

    Flux<CategoryVo> getByNames(List<String> names);

    Mono<ListResult<CategoryVo>> list(@Nullable Integer page, @Nullable Integer size);

    Flux<CategoryVo> listAll();

    Flux<CategoryTreeVo> listAsTree();

    Flux<CategoryTreeVo> listAsTree(String name);

    Flux<CategoryTreeVo> getTreeByName(String name);

    Mono<List<CategoryTreeVo>> getTreeByNamePart(String name);

    CategoryTreeVo getTreeByNameChild(String name);

    void traverse(CategoryTreeVo node, List<String> result) ;
}
