package run.halo.links.finders.impl;

import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Category;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.theme.finders.Finder;
import run.halo.links.MyCategoryFinder;
import run.halo.links.vo.MyCategoryTreeVo;
import run.halo.links.vo.MyCategoryVo;

/**
 * A default implementation of {@link MyCategoryFinder}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
@Finder("mycategoryFinder")
public class MyCategoryFinderImpl implements MyCategoryFinder {
    private final ReactiveExtensionClient client;

    public MyCategoryFinderImpl(ReactiveExtensionClient client) {
        this.client = client;
    }

    @Override
    public Mono<MyCategoryVo> getByName(String name) {
        return client.fetch(Category.class, name)
            .map(MyCategoryVo::from);
    }

    @Override
    public Flux<MyCategoryVo> getByNames(List<String> names) {
        if (names == null) {
            return Flux.empty();
        }
        return Flux.fromIterable(names)
            .flatMap(this::getByName);
    }

    @Override
    public Mono<ListResult<MyCategoryVo>> list(Integer page, Integer size) {
        return client.list(Category.class, null,
                defaultComparator(), pageNullSafe(page), sizeNullSafe(size))
            .map(list -> {
                List<MyCategoryVo> MyCategoryVos = list.get()
                    .map(MyCategoryVo::from)
                    .collect(Collectors.toList());
                return new ListResult<>(list.getPage(), list.getSize(), list.getTotal(),
                    MyCategoryVos);
            })
            .defaultIfEmpty(new ListResult<>(page, size, 0L, List.of()));
    }

    @Override
    public Flux<MyCategoryVo> listAll() {
        return client.list(Category.class, null, defaultComparator())
            .map(MyCategoryVo::from);
    }

    @Override
    public Flux<MyCategoryTreeVo> listAsTree() {
        return this.tomyCategoryTreeVoFlux(null);
    }

    @Override
    public Flux<MyCategoryTreeVo> listAsTree(String name) {
        return this.tomyCategoryTreeVoFlux(name);
    }

    @Override
    public Flux<MyCategoryTreeVo> getTreeByName(String name){
        return listAll()
            .collectList()
            .flatMapIterable(myCategoryVos -> {
                Map<String, MyCategoryTreeVo> nameIdentityMap = myCategoryVos.stream()
                    .map(MyCategoryTreeVo::from)
                    .collect(Collectors.toMap(myCategoryVo -> myCategoryVo.getMetadata().getName(),
                        Function.identity()));

                nameIdentityMap.forEach((nameKey, value) -> {
                    List<String> children = value.getSpec().getChildren();
                    if (children == null) {
                        return;
                    }
                    for (String child : children) {
                        MyCategoryTreeVo childNode = nameIdentityMap.get(child);
                        if (childNode != null) {
                            childNode.setParentName(nameKey);
                        }
                    }
                });
                return listToMyTree(nameIdentityMap.values(), name);
            });
    }

    Flux<MyCategoryTreeVo> tomyCategoryTreeVoFlux(String name) {
        return listAll()
            .collectList()
            .flatMapIterable(myCategoryVos -> {
                Map<String, MyCategoryTreeVo> nameIdentityMap = myCategoryVos.stream()
                    .map(MyCategoryTreeVo::from)
                    .collect(Collectors.toMap(myCategoryVo -> myCategoryVo.getMetadata().getName(),
                        Function.identity()));

                nameIdentityMap.forEach((nameKey, value) -> {
                    List<String> children = value.getSpec().getChildren();
                    if (children == null) {
                        return;
                    }
                    for (String child : children) {
                        MyCategoryTreeVo childNode = nameIdentityMap.get(child);
                        if (childNode != null) {
                            childNode.setParentName(nameKey);
                        }
                    }
                });
                return listToTree(nameIdentityMap.values(), name);
            });
    }
    static List<MyCategoryTreeVo> listToMyTree(Collection<MyCategoryTreeVo> list, String name) {
        Map<String, List<MyCategoryTreeVo>> parentNameIdentityMap = list.stream()
            .filter(MyCategoryTreeVo -> MyCategoryTreeVo.getParentName() != null)
            .collect(Collectors.groupingBy(MyCategoryTreeVo::getParentName));

        list.forEach(node -> {
            // sort children
            List<MyCategoryTreeVo> children =
                parentNameIdentityMap.getOrDefault(node.getMetadata().getName(), List.of())
                    .stream()
                    .sorted(defaultTreeNodeComparator())
                    .toList();
            node.setChildren(children);
        });
        List<MyCategoryTreeVo> res = list.stream()
            .filter(v -> StringUtils.isEmpty(null) ? v.getParentName() == null
                : StringUtils.equals(v.getMetadata().getName(), null))
            .sorted(defaultTreeNodeComparator())
            .collect(Collectors.toList());

        String parentName = getParentName(res, null, name,0);
        List<MyCategoryTreeVo> result = res.stream()
            .filter(tmp -> tmp.getMetadata().getName().equals(parentName))
            .collect(Collectors.toList());
        return result;
    }
    static String getParentName(List<MyCategoryTreeVo>lists,String parentName,String aim,int deep){

        for (int i = 0; i < lists.size(); i++) {
            MyCategoryTreeVo element = lists.get(i);
            if(deep == 0){
                parentName = element.getMetadata().getName();
            }
            if(aim.equals(element.getMetadata().getName())){
                return parentName;
            }
            String name = getParentName(element.getChildren(),parentName,aim,deep +1);
            if(name != null){
                return  name;
            }
        }
        return null;
    }
    static List<MyCategoryTreeVo> listToTree(Collection<MyCategoryTreeVo> list, String name) {
        Map<String, List<MyCategoryTreeVo>> parentNameIdentityMap = list.stream()
            .filter(MyCategoryTreeVo -> MyCategoryTreeVo.getParentName() != null)
            .collect(Collectors.groupingBy(MyCategoryTreeVo::getParentName));

        list.forEach(node -> {
            // sort children
            List<MyCategoryTreeVo> children =
                parentNameIdentityMap.getOrDefault(node.getMetadata().getName(), List.of())
                    .stream()
                    .sorted(defaultTreeNodeComparator())
                    .toList();
            node.setChildren(children);
        });
        return list.stream()
            .filter(v -> StringUtils.isEmpty(name) ? v.getParentName() == null
                : StringUtils.equals(v.getMetadata().getName(), name))
            .sorted(defaultTreeNodeComparator())
            .collect(Collectors.toList());
    }

    static Comparator<MyCategoryTreeVo> defaultTreeNodeComparator() {
        Function<MyCategoryTreeVo, Integer> priority =
            category -> Objects.requireNonNullElse(category.getSpec().getPriority(), 0);
        Function<MyCategoryTreeVo, Instant> creationTimestamp =
            category -> category.getMetadata().getCreationTimestamp();
        Function<MyCategoryTreeVo, String> name =
            category -> category.getMetadata().getName();
        return Comparator.comparing(priority)
            .thenComparing(creationTimestamp)
            .thenComparing(name);
    }

    static Comparator<Category> defaultComparator() {
        Function<Category, Integer> priority =
            category -> Objects.requireNonNullElse(category.getSpec().getPriority(), 0);
        Function<Category, Instant> creationTimestamp =
            category -> category.getMetadata().getCreationTimestamp();
        Function<Category, String> name =
            category -> category.getMetadata().getName();
        return Comparator.comparing(priority)
            .thenComparing(creationTimestamp)
            .thenComparing(name)
            .reversed();
    }


    int pageNullSafe(Integer page) {
        return ObjectUtils.defaultIfNull(page, 1);
    }

    int sizeNullSafe(Integer page) {
        return ObjectUtils.defaultIfNull(page, 10);
    }
}
