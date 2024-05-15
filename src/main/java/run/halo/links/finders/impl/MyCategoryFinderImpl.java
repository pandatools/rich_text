package run.halo.links.finders.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import run.halo.links.vo.CategoryTreeVo;
import run.halo.links.vo.CategoryVo;
import run.halo.links.vo.MySinglePageVo;

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
    public Mono<CategoryVo> getByName(String name) {
        return client.fetch(Category.class, name)
            .map(CategoryVo::from);
    }

    @Override
    public Flux<CategoryVo> getByNames(List<String> names) {
        if (names == null) {
            return Flux.empty();
        }
        return Flux.fromIterable(names)
            .flatMap(this::getByName);
    }

    @Override
    public Mono<ListResult<CategoryVo>> list(Integer page, Integer size) {
        return client.list(Category.class, null,
                defaultComparator(), pageNullSafe(page), sizeNullSafe(size))
            .map(list -> {
                List<CategoryVo> categoryVos = list.get()
                    .map(CategoryVo::from)
                    .collect(Collectors.toList());
                return new ListResult<>(list.getPage(), list.getSize(), list.getTotal(),
                    categoryVos);
            })
            .defaultIfEmpty(new ListResult<>(page, size, 0L, List.of()));
    }

    @Override
    public Flux<CategoryVo> listAll() {
        return client.list(Category.class, null, defaultComparator())
            .map(CategoryVo::from);
    }
    @Override
    public List<String> getCategoryByNameAmbiguous(String name){

        Predicate<Category> postPredicate = category -> category.getSpec().getDisplayName().toLowerCase().contains(name.toLowerCase());
        return client.list(Category.class, postPredicate, defaultComparator())
            .map(CategoryVo::from)
            .flatMap(categoryVo -> Mono.just(categoryVo.getMetadata().getName())).collectList()
            .block();

    }
    @Override
    public Flux<CategoryTreeVo> listAsTree() {
        return this.tomyCategoryTreeVoFlux(null);
    }

    @Override
    public Flux<CategoryTreeVo> listAsTree(String name) {
        return this.tomyCategoryTreeVoFlux(name);
    }

    @Override
    public Flux<CategoryTreeVo> getTreeByName(String name){
        //根据名称找到从root下所有的树
        return listAll()
            .collectList()
            .flatMapIterable(myCategoryVos -> {
                Map<String, CategoryTreeVo> nameIdentityMap = myCategoryVos.stream()
                    .map(CategoryTreeVo::from)
                    .collect(Collectors.toMap(myCategoryVo -> myCategoryVo.getMetadata().getName(),
                        Function.identity()));

                nameIdentityMap.forEach((nameKey, value) -> {
                    List<String> children = value.getSpec().getChildren();
                    if (children == null) {
                        return;
                    }
                    for (String child : children) {
                        CategoryTreeVo childNode = nameIdentityMap.get(child);
                        if (childNode != null) {
                            childNode.setParentName(nameKey);
                        }
                    }
                });
                return listToMyTree(nameIdentityMap.values(), name);
            });
    }

    @Override
    public Flux<CategoryTreeVo> getCompatriot(String name) {

        System.out.println("use_getCompatriot111");
        return listAll()
            .collectList()
            .flatMapIterable(categoryVos -> {
                Map<String, CategoryTreeVo> nameIdentityMap = categoryVos.stream()
                    .map(CategoryTreeVo::from)
                    .collect(Collectors.toMap(categoryVo -> categoryVo.getMetadata().getName(),
                        Function.identity()));

                Collection<CategoryTreeVo> result = new ArrayList<>();
                List<String> aims = new ArrayList<>();
                List<String> all_childrens = new ArrayList<>();
                Set<String> all_nodes = new HashSet<>(nameIdentityMap.keySet());
                for (String key : nameIdentityMap.keySet()) {
                    CategoryTreeVo value = nameIdentityMap.get(key);
                    List<String> children = value.getSpec().getChildren();
                    for (String child : children) {
                        if (child.equals(name)) {
                            aims = children;
                            break;
                        }
                    }

                    all_childrens.addAll(children);

                }

                Set<String> set_all_childrens = new HashSet<>(all_childrens);
                all_nodes.removeAll(all_childrens); //只剩下top


                if(aims.isEmpty()&&all_nodes.contains(name)){ //aim 为top
                    aims = new ArrayList<>(all_nodes);
                }


                for (String aim : aims) {
                    CategoryTreeVo categoryTreeVo = nameIdentityMap.get(aim);
                    if (categoryTreeVo != null) {
                        result.add(categoryTreeVo);
                    }
                }

                return result;
            });
    }


    @Override
    public  Mono<List<CategoryTreeVo>> getTreeByNamePart(String targetname)  {
        //根据名称找到从根节点到该节点的路径
        Flux<CategoryTreeVo> categoryTreeVoFlux = listAll()
            .collectList()
            .flatMapIterable(categoryVos -> {
                Map<String, CategoryTreeVo> nameIdentityMap = categoryVos.stream()
                    .map(CategoryTreeVo::from)
                    .collect(Collectors.toMap(categoryVo -> categoryVo.getMetadata().getName(),
                        Function.identity()));

                nameIdentityMap.forEach((nameKey, value) -> {
                    List<String> children = value.getSpec().getChildren();
                    if (children == null) {
                        return;
                    }
                    for (String child : children) {
                        CategoryTreeVo childNode = nameIdentityMap.get(child);
                        if (childNode != null) {
                            childNode.setParentName(nameKey);
                        }
                    }
                });
                return listToTree(nameIdentityMap.values(), null);
            });


        List<CategoryTreeVo> path = new ArrayList<>();

        List<CategoryTreeVo> listMono = categoryTreeVoFlux.collectList().block();
        for (CategoryTreeVo categoryTree:listMono) {
            findPathToTopParent(categoryTree, targetname, path);
            if(!path.isEmpty()) break;
        }

        Collections.reverse(path);
        Mono<List<CategoryTreeVo>> just = Mono.just(path);
        return just;
    }


    @Override
    public CategoryTreeVo getTreeByNameChild(String targetname){
        //找到树的某一个节点，可以获得该节点下所有孩子
        Flux<CategoryTreeVo> categoryTreeVoFlux = listAll()
            .collectList()
            .flatMapIterable(categoryVos -> {
                Map<String, CategoryTreeVo> nameIdentityMap = categoryVos.stream()
                    .map(CategoryTreeVo::from)
                    .collect(Collectors.toMap(categoryVo -> categoryVo.getMetadata().getName(),
                        Function.identity()));

                nameIdentityMap.forEach((nameKey, value) -> {
                    List<String> children = value.getSpec().getChildren();
                    if (children == null) {
                        return;
                    }
                    for (String child : children) {
                        CategoryTreeVo childNode = nameIdentityMap.get(child);
                        if (childNode != null) {
                            childNode.setParentName(nameKey);
                        }
                    }
                });
                return listToTree(nameIdentityMap.values(), null);
            });


        List<CategoryTreeVo> path = new ArrayList<>();

        List<CategoryTreeVo> listMono = categoryTreeVoFlux.collectList().block();
        for (CategoryTreeVo categoryTree:listMono) {
            findPathToTopParent(categoryTree, targetname, path);
            if(!path.isEmpty()) break;
        }


        for(CategoryTreeVo p:path){
            if(p.getMetadata().getName().equals(targetname)){
                return p;
            }
        }
        return null;
    }
    static Boolean findPathToTopParent(CategoryTreeVo root, String targetName, List<CategoryTreeVo> result) {
        if (root == null) {
            return false;
        }
        if (StringUtils.equals(root.getMetadata().getName(), targetName)) {
            result.add(root);
            return true;
        }
        for (CategoryTreeVo child : root.getChildren()) {
            if (findPathToTopParent(child, targetName, result)) {
                result.add(root);
                return true;
            }
        }
        return false;
    }


    Flux<CategoryTreeVo> tomyCategoryTreeVoFlux(String name) {

        return listAll()
            .collectList()
            .flatMapIterable(myCategoryVos -> {
                Map<String, CategoryTreeVo> nameIdentityMap = myCategoryVos.stream()
                    .map(CategoryTreeVo::from)
                    .collect(Collectors.toMap(myCategoryVo -> myCategoryVo.getMetadata().getName(),
                        Function.identity()));

                nameIdentityMap.forEach((nameKey, value) -> {
                    List<String> children = value.getSpec().getChildren();
                    if (children == null) {
                        return;
                    }
                    for (String child : children) {
                        CategoryTreeVo childNode = nameIdentityMap.get(child);
                        if (childNode != null) {
                            childNode.setParentName(nameKey);
                        }
                    }
                });
                return listToTree(nameIdentityMap.values(), name);
            });
    }
    static List<CategoryTreeVo> listToMyTree(Collection<CategoryTreeVo> list, String name) {
        Map<String, List<CategoryTreeVo>> parentNameIdentityMap = list.stream()
            .filter(CategoryTreeVo -> CategoryTreeVo.getParentName() != null)
            .collect(Collectors.groupingBy(CategoryTreeVo::getParentName));

        list.forEach(node -> {
            // sort children
            List<CategoryTreeVo> children =
                parentNameIdentityMap.getOrDefault(node.getMetadata().getName(), List.of())
                    .stream()
                    .sorted(defaultTreeNodeComparator())
                    .toList();
            node.setChildren(children);
        });
        List<CategoryTreeVo> res = list.stream()
            .filter(v -> StringUtils.isEmpty(null) ? v.getParentName() == null
                : StringUtils.equals(v.getMetadata().getName(), null))
            .sorted(defaultTreeNodeComparator())
            .collect(Collectors.toList());

        String parentName = getParentName(res, null, name,0);
        List<CategoryTreeVo> result = res.stream()
            .filter(tmp -> tmp.getMetadata().getName().equals(parentName))
            .collect(Collectors.toList());
        return result;
    }


    
    static String getParentName(List<CategoryTreeVo>lists,String parentName,String aim,int deep){

        for (CategoryTreeVo element : lists) {
            if (deep == 0) {
                parentName = element.getMetadata().getName();
            }
            if (aim.equals(element.getMetadata().getName())) {
                return parentName;
            }
            String name = getParentName(element.getChildren(), parentName, aim, deep + 1);
            if (name != null) {
                return name;
            }
        }
        return null;
    }
    @Override
     public void traverse(CategoryTreeVo node, List<String> result) {
        if (node == null) {
            return;
        }

        result.add(node.getMetadata().getName());

        List<CategoryTreeVo> children = node.getChildren();
        if (children != null) {
            for (CategoryTreeVo child : children) {
                traverse(child, result);
            }
        }
    }
    @Override
    public Map<String,String> getAnnotationsByCategory(String name, String patternString){

        CategoryVo obj = this.getByName(name).block();
        Map<String, String> result = new LinkedHashMap<>();
        Map<String, String> annotations = obj.getMetadata().getAnnotations();
        for (String key : annotations.keySet()) {
            Pattern pattern = Pattern.compile(patternString);
            Matcher matcher = pattern.matcher(key);
            if (matcher.find()) {
                result.put(key, annotations.get(key));
            }
        }
        return result;
    }
    static List<CategoryTreeVo> listToTree(Collection<CategoryTreeVo> list, String name) {
        Map<String, List<CategoryTreeVo>> parentNameIdentityMap = list.stream()
            .filter(CategoryTreeVo -> CategoryTreeVo.getParentName() != null)
            .collect(Collectors.groupingBy(CategoryTreeVo::getParentName));

        list.forEach(node -> {
            // sort children
            List<CategoryTreeVo> children =
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

    static Comparator<CategoryTreeVo> defaultTreeNodeComparator() {
        Function<CategoryTreeVo, Integer> priority =
            category -> Objects.requireNonNullElse(category.getSpec().getPriority(), 0);
        Function<CategoryTreeVo, Instant> creationTimestamp =
            category -> category.getMetadata().getCreationTimestamp();
        Function<CategoryTreeVo, String> name =
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
