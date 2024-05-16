package run.halo.links.finders.impl;


import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.comparator.Comparators;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Counter;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.Snapshot;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.ReactiveExtensionClient;

import run.halo.app.theme.finders.Finder;
import run.halo.app.metrics.MeterUtils;
import run.halo.links.ContentWrapper;
import run.halo.links.MyCategoryFinder;
import run.halo.links.MyPostFinder;
// import run.halo.links.MyPostPublicQueryService;
import run.halo.links.vo.CategoryTreeVo;
import run.halo.links.vo.ContentVo;
import run.halo.links.vo.MyListedPostVo;
import run.halo.links.vo.MyPostVo;
import run.halo.links.vo.MyStatsVo;

/**
 * A finder for {@link Post}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Finder("mypostFinder")
@AllArgsConstructor
public class PostFinderImpl implements MyPostFinder {
    private final MyCategoryFinder categoryFinder;

    private final ReactiveExtensionClient client;


    @Override
    public Mono<MyPostVo> getByName(String postName) {
        System.out.println("eeeeeeeeeeeeeeeeeeeeeeeeeeeee");
        return client.get(Post.class, postName)
            .map(MyPostVo::from)
            .flatMap(postVo -> {
                System.out.println(
                    "flatMapflatMapflatMapflatMap:postVo = " + postVo.getMetadata2());

                Mono<MyPostVo> myPostVoMono = content(postName)
                    .doOnNext(postVo::setContent)
                    .thenReturn(postVo);
                return myPostVoMono;
            });

    }

    @Override
    public Map<String,String> getAnnotationsByArticle(String name, String patternString) {
        System.out.println("yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy");
        MyPostVo obj = this.getByName(name).block();
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
    private boolean contains(List<String> c, List<String> keys) {
        if(c == null){
            return false;
        }
        for(String key:keys) {
            if (StringUtils.isBlank(key)) {
                continue;
            }
            if (c.contains(key)){
                return true;
            }
        }
        return false;
    }

    private <T extends MyListedPostVo> Mono<MyStatsVo> populateStats(T postVo) {
        return
            client.fetch(Counter.class, MeterUtils.nameOf(Post.class, postVo.getMetadata().getName())).map(counter -> MyStatsVo.builder()
                .visit(counter.getVisit())
                .upvote(counter.getUpvote())
                .comment(counter.getApprovedComment())
                .build()
            ).defaultIfEmpty(MyStatsVo.empty());

    }

    int pageNullSafe(Integer page) {
        return ObjectUtils.defaultIfNull(page, 1);
    }

    int sizeNullSafe(Integer size) {
        return ObjectUtils.defaultIfNull(size, 10);
    }


    Boolean InstantIsTrueYear(Instant posttime,Integer time){

        ZonedDateTime zonedDateTime = posttime.atZone(ZoneId.systemDefault());

        // 获取年份
        int year = zonedDateTime.getYear();
        return year == time ;
    }
    @Override
    public Mono<JSONObject> listByCategoryAndChildrenV2(@Nullable Integer page,
        @Nullable Integer size,
        String categoryName,
        Integer customer_year){
        System.out.println("yhnnnnnnnnnnnnnnnnnnnnn");
        // 获得该分类下的所有文章，注意，分类包括该分类和名下所有分类
        CategoryTreeVo categoryTreeVo = categoryFinder.getTreeByNameChild(categoryName);

        List<String> result = new ArrayList<>();
        categoryFinder.traverse(categoryTreeVo,result);
        System.out.println("yslhhhresult = " + result);
        Comparator<Post> comparator =  defaultComparator();
        Predicate<Post>  postPredicate = post -> contains(post.getSpec().getCategories(), result) && InstantIsTrueYear(post.getSpec().getPublishTime(),customer_year);


        Predicate<Post> FIXED_PREDICATE = post -> post.isPublished()
            && Objects.equals(false, post.getSpec().getDeleted())
            && Post.VisibleEnum.PUBLIC.equals(post.getSpec().getVisible());

        Predicate<Post> predicate = FIXED_PREDICATE
            .and(postPredicate == null ? post -> true : postPredicate);

        Mono<ListResult<MyListedPostVo>> listResultMono = client.list(Post.class, predicate,
                comparator, pageNullSafe(page), sizeNullSafe(size))
            .flatMap(list -> Flux.fromStream(list.get())
                .concatMap(post -> convertToListedPostVo(post)
                    .flatMap(postVo -> populateStats(postVo)
                        .doOnNext(postVo::setStats).thenReturn(postVo)
                    )
                )
                .collectList()
                .map(postVos -> new ListResult<>(list.getPage(), list.getSize(), list.getTotal(),
                    postVos)
                )
            )
            .defaultIfEmpty(new ListResult<>(page, size, 0L, List.of()));
        ListResult<MyListedPostVo> postvo = listResultMono.block();
        long total = postvo.getTotal();
        long totalPage = (long) Math.ceil((double)total /  size);
        JSONObject jsonObject = new JSONObject();
        System.out.println("total="+String.valueOf(total)+"totalPage="+String.valueOf(totalPage)+"page="+String.valueOf(page));
        jsonObject.put("items",postvo);
        List<Map<String, String>> pagelist = new ArrayList<>();
        if(page<=totalPage) {
            pagelist.add(this.setMap(1));

            if (totalPage <= 4 && totalPage > 1) {
                for (int i = 2; i <= totalPage; i++) {
                    pagelist.add(this.setMap(i));
                }
            } else if (totalPage <= 1) {

            } else {

                if (page == 2 || page == 1 || page == 3) {
                    pagelist.add(this.setMap(2));
                    pagelist.add(this.setMap(3));
                } else {
                    pagelist.add(this.setMap(-1));
                    pagelist.add(this.setMap(page - 1));
                    pagelist.add(this.setMap(page));
                }

                if (page < totalPage && page != 1 && page != 2) {
                    pagelist.add(this.setMap(page + 1));
                }


                if (page + 2 < totalPage) {
                    pagelist.add(this.setMap(-1));
                }
                if(page+1<totalPage){
                    pagelist.add(setMap((int) totalPage));
                }


            }
        }
        JSONArray jsonArray = new JSONArray();
        for (Map<String, String> map : pagelist) {
            JSONObject tmp3= new JSONObject(map);
            jsonArray.add(tmp3);
        }
        jsonObject.put("page",jsonArray);

        return  Mono.just(jsonObject);
    }


    @Override
    public Mono<JSONObject> listByCategoryAndChildren(@Nullable Integer page,
        @Nullable Integer size,
        String categoryName){
        System.out.println("yhnnnnnnnnnnnnnnnnnnnnn");
        // 获得该分类下的所有文章，注意，分类包括该分类和名下所有分类
        CategoryTreeVo categoryTreeVo = categoryFinder.getTreeByNameChild(categoryName);

        List<String> result = new ArrayList<>();
        categoryFinder.traverse(categoryTreeVo,result);
        System.out.println("yslhhhresult = " + result);
        Comparator<Post> comparator =  defaultComparator();
        Predicate<Post> postPredicate =  post -> contains(post.getSpec().getCategories(), result);
        Predicate<Post> FIXED_PREDICATE = post -> post.isPublished()
            && Objects.equals(false, post.getSpec().getDeleted())
            && Post.VisibleEnum.PUBLIC.equals(post.getSpec().getVisible());

        Predicate<Post> predicate = FIXED_PREDICATE
            .and(postPredicate == null ? post -> true : postPredicate);

        Mono<ListResult<MyListedPostVo>> listResultMono = client.list(Post.class, predicate,
                comparator, pageNullSafe(page), sizeNullSafe(size))
            .flatMap(list -> Flux.fromStream(list.get())
                .concatMap(post -> convertToListedPostVo(post)
                    .flatMap(postVo -> populateStats(postVo)
                        .doOnNext(postVo::setStats).thenReturn(postVo)
                    )
                )
                .collectList()
                .map(postVos -> new ListResult<>(list.getPage(), list.getSize(), list.getTotal(),
                    postVos)
                )
            )
            .defaultIfEmpty(new ListResult<>(page, size, 0L, List.of()));
        ListResult<MyListedPostVo> postvo = listResultMono.block();
        long total = postvo.getTotal();
        long totalPage = (long) Math.ceil((double)total /  size);
        JSONObject jsonObject = new JSONObject();
        System.out.println("total="+String.valueOf(total)+"totalPage="+String.valueOf(totalPage)+"page="+String.valueOf(page));
        jsonObject.put("items",postvo);
        List<Map<String, String>> pagelist = new ArrayList<>();
        if(page<=totalPage) {
            pagelist.add(this.setMap(1));

            if (totalPage <= 4 && totalPage > 1) {
                for (int i = 2; i <= totalPage; i++) {
                    pagelist.add(this.setMap(i));
                }
            } else if (totalPage <= 1) {

            } else {

                if (page == 2 || page == 1 || page == 3) {
                    pagelist.add(this.setMap(2));
                    pagelist.add(this.setMap(3));
                } else {
                    pagelist.add(this.setMap(-1));
                    pagelist.add(this.setMap(page - 1));
                    pagelist.add(this.setMap(page));
                }

                if (page < totalPage && page != 1 && page != 2) {
                    pagelist.add(this.setMap(page + 1));
                }


                if (page + 2 < totalPage) {
                    pagelist.add(this.setMap(-1));
                }
                if(page+1<totalPage){
                    pagelist.add(setMap((int) totalPage));
                }


            }
        }
        JSONArray jsonArray = new JSONArray();
        for (Map<String, String> map : pagelist) {
            JSONObject tmp3= new JSONObject(map);
            jsonArray.add(tmp3);
        }
        jsonObject.put("page",jsonArray);

        return  Mono.just(jsonObject);
    }




    Boolean hasAllData(String title, String data) {
        String[] patternArray = data.split("\\s+");
        for (String element : patternArray) {
            if (!title.contains(element)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public  Mono<JSONObject> searchPostByMixed(@Nullable Integer page,
        @Nullable Integer size,String data){
        List<String> categoryList = categoryFinder.getCategoryByNameAmbiguous(data);
        Comparator<Post> comparator =  defaultComparator();
        Predicate<Post> postPredicate = post -> post.isPublished()
            && Objects.equals(false, post.getSpec().getDeleted())
            && Post.VisibleEnum.PUBLIC.equals(post.getSpec().getVisible())
            && (this.hasAllData(post.getSpec().getTitle().toLowerCase(),data.toLowerCase())
            || this.hasCommonElement(post.getSpec().getCategories(), categoryList));



        Mono<ListResult<MyListedPostVo>> listResultMono =  client.list(Post.class, postPredicate
            , comparator,pageNullSafe(page),sizeNullSafe(size)).flatMap(list -> Flux.fromStream(list.get())
                .concatMap(post -> convertToListedPostVo(post)
                    .flatMap(postVo -> populateStats(postVo)
                        .doOnNext(postVo::setStats).thenReturn(postVo)
                    )
                )
                .collectList()
                .map(postVos -> new ListResult<>(list.getPage(), list.getSize(), list.getTotal(),
                    postVos)
                )
            )
            .defaultIfEmpty(new ListResult<>(page, size, 0L, List.of()));

        ListResult<MyListedPostVo> postvo = listResultMono.block();
        long total = postvo.getTotal();
        long totalPage = (long) Math.ceil((double)total /  size);
        List<Map<String, String>> pagelist = new ArrayList<>();
        JSONObject jsonObject = new JSONObject();
        if(page<=totalPage) {
            pagelist.add(this.setMap(1));

            if (totalPage <= 4 && totalPage > 1) {
                for (int i = 2; i <= totalPage; i++) {
                    pagelist.add(this.setMap(i));
                }
            } else if (totalPage <= 1) {

            } else {

                if (page == 2 || page == 1 || page == 3) {
                    pagelist.add(this.setMap(2));
                    pagelist.add(this.setMap(3));
                } else {
                    pagelist.add(this.setMap(-1));
                    pagelist.add(this.setMap(page - 1));
                    pagelist.add(this.setMap(page));
                }

                if (page < totalPage && page != 1 && page != 2) {
                    pagelist.add(this.setMap(page + 1));
                }


                if (page + 2 < totalPage) {
                    pagelist.add(this.setMap(-1));
                }
                if(page+1<totalPage){
                    pagelist.add(setMap((int) totalPage));
                }


            }
        }
        JSONArray jsonArray = new JSONArray();
        for (Map<String, String> map : pagelist) {
            JSONObject tmp3= new JSONObject(map);
            jsonArray.add(tmp3);
        }
        jsonObject.put("page",jsonArray);
        jsonObject.put("infos",postvo);
        return  Mono.just(jsonObject);

    }


    Boolean check_isFalse(Map<String, String> annotations){
        for (String key : annotations.keySet()) {
                if (key == "search"){
                    System.out.println("aaaaaaaaaaaaaaaa");

                    if (annotations.get(key).equals("false")){
                        return false;
                    }
                    return true;
                }

        }
        return true;
    }
    @Override
    public Mono<JSONObject> searchPostByMixedAndSearchFalse(@Nullable Integer page,
        @Nullable Integer size,String data){
        //
        List<String> categoryList = categoryFinder.getCategoryByNameAmbiguous(data);
        Comparator<Post> comparator =  defaultComparator();
        Predicate<Post> postPredicate = post -> post.isPublished()
            && Objects.equals(false, post.getSpec().getDeleted())
            && Post.VisibleEnum.PUBLIC.equals(post.getSpec().getVisible())
            && this.check_isFalse(post.getMetadata().getAnnotations())
            && (this.hasAllData(post.getSpec().getTitle().toLowerCase(),data.toLowerCase())
            || this.hasCommonElement(post.getSpec().getCategories(), categoryList)) ;



        Mono<ListResult<MyListedPostVo>> listResultMono =  client.list(Post.class, postPredicate
                , comparator,pageNullSafe(page),sizeNullSafe(size)).flatMap(list -> Flux.fromStream(list.get())
                .concatMap(post -> convertToListedPostVo(post)
                    .flatMap(postVo -> populateStats(postVo)
                        .doOnNext(postVo::setStats).thenReturn(postVo)
                    )
                )
                .collectList()
                .map(postVos -> new ListResult<>(list.getPage(), list.getSize(), list.getTotal(),
                    postVos)
                )
            )
            .defaultIfEmpty(new ListResult<>(page, size, 0L, List.of()));

        ListResult<MyListedPostVo> postvo = listResultMono.block();
        long total = postvo.getTotal();
        long totalPage = (long) Math.ceil((double)total /  size);
        List<Map<String, String>> pagelist = new ArrayList<>();
        JSONObject jsonObject = new JSONObject();
        if(page<=totalPage) {
            pagelist.add(this.setMap(1));

            if (totalPage <= 4 && totalPage > 1) {
                for (int i = 2; i <= totalPage; i++) {
                    pagelist.add(this.setMap(i));
                }
            } else if (totalPage <= 1) {

            } else {

                if (page == 2 || page == 1 || page == 3) {
                    pagelist.add(this.setMap(2));
                    pagelist.add(this.setMap(3));
                } else {
                    pagelist.add(this.setMap(-1));
                    pagelist.add(this.setMap(page - 1));
                    pagelist.add(this.setMap(page));
                }

                if (page < totalPage && page != 1 && page != 2) {
                    pagelist.add(this.setMap(page + 1));
                }


                if (page + 2 < totalPage) {
                    pagelist.add(this.setMap(-1));
                }
                if(page+1<totalPage){
                    pagelist.add(setMap((int) totalPage));
                }


            }
        }
        JSONArray jsonArray = new JSONArray();
        for (Map<String, String> map : pagelist) {
            JSONObject tmp3= new JSONObject(map);
            jsonArray.add(tmp3);
        }
        jsonObject.put("page",jsonArray);
        jsonObject.put("infos",postvo);
        return  Mono.just(jsonObject);
    }

    protected void checkBaseSnapshot(Snapshot snapshot) {
        Assert.notNull(snapshot, "The snapshot must not be null.");
        String keepRawAnno =
            MetadataUtil.nullSafeAnnotations(snapshot).get(Snapshot.KEEP_RAW_ANNO);
        if (!org.thymeleaf.util.StringUtils.equals(Boolean.TRUE.toString(), keepRawAnno)) {
            throw new IllegalArgumentException(
                String.format("The snapshot [%s] is not a base snapshot.",
                    snapshot.getMetadata().getName()));
        }
    }

    public Mono<ContentWrapper> getContent(String snapshotName, String baseSnapshotName) {
        return client.fetch(Snapshot.class, baseSnapshotName)
            .doOnNext(this::checkBaseSnapshot)
            .flatMap(baseSnapshot -> {
                if (StringUtils.equals(snapshotName, baseSnapshotName)) {
                    var contentWrapper = ContentWrapper.patchSnapshot(baseSnapshot, baseSnapshot);
                    return Mono.just(contentWrapper);
                }
                return client.fetch(Snapshot.class, snapshotName)
                    .map(snapshot -> ContentWrapper.patchSnapshot(snapshot, baseSnapshot));
            });
    }


    // @Override
    public Mono<ContentWrapper> getReleaseContent(String postName) {
        return client.get(Post.class, postName)
            .flatMap(post -> {
                String releaseSnapshot = post.getSpec().getReleaseSnapshot();
                return this.getContent(releaseSnapshot, post.getSpec().getBaseSnapshot());
            });
    }

    // @Override
    public Mono<ContentVo> content(String postName) {
        return this.getReleaseContent(postName)
            .map(wrapper -> ContentVo.builder().content(wrapper.getContent())
                .raw(wrapper.getRaw()).build());
    }


    private Mono<MyPostVo> fetchByName(String name) {
        if (StringUtils.isBlank(name)) {
            return Mono.empty();
        }
        return getByName(name)
            .onErrorResume(MyExtensionNotFoundException.class::isInstance, (error) -> Mono.empty());
    }

    public Mono<MyListedPostVo> convertToListedPostVo(@NonNull Post post) {
        Assert.notNull(post, "Post must not be null");
        MyListedPostVo postVo = MyListedPostVo.from(post);
        postVo.setCategories(List.of());
        postVo.setTags(List.of());
        postVo.setContributors(List.of());

        return Mono.just(postVo)
            .defaultIfEmpty(postVo);
    }

    @Override
    public Flux<MyListedPostVo> listAll() {


        var list = client.list(Post.class, post -> post.isPublished()
                    && Objects.equals(false, post.getSpec().getDeleted())
                    && Post.VisibleEnum.PUBLIC.equals(post.getSpec().getVisible())

                , defaultComparator())
            .concatMap(this::convertToListedPostVo);
        return list;
    }
    @Override
    public Flux<MyListedPostVo> searchPostByTitle(String title){
        var list = client.list(Post.class, post -> post.isPublished()
                    && Objects.equals(false, post.getSpec().getDeleted())
                    && Post.VisibleEnum.PUBLIC.equals(post.getSpec().getVisible())
                    && post.getSpec().getTitle().toLowerCase().contains(title.toLowerCase())
                , defaultComparator())
            .concatMap(this::convertToListedPostVo);
        return list;
    }

    @Override
    public Flux<MyListedPostVo> searchPostByCategory(String category){
        List<String> categoryList = categoryFinder.getCategoryByNameAmbiguous(category);
        System.out.println("categoryList="+categoryList.toString());
        var list = client.list(Post.class, post -> post.isPublished()
                    && Objects.equals(false, post.getSpec().getDeleted())
                    && Post.VisibleEnum.PUBLIC.equals(post.getSpec().getVisible())
                    && this.hasCommonElement(post.getSpec().getCategories(),categoryList)
                , defaultComparator())
            .concatMap(this::convertToListedPostVo);
        return list;
    }


    public Map<String, String>  setMap(int page){
        if(page<0){
            Map<String, String> tmp = new HashMap<>();
            tmp.put("label","...");
            tmp.put("type","text");
            return tmp;
        }

        Map<String, String> tmp = new HashMap<>();
        tmp.put("label",String.valueOf(page));
        tmp.put("type","button");
        tmp.put("link","/?page="+String.valueOf(page));
        return tmp;
    }

    Boolean hasCommonElement(List<String> list1, List<String> list2) {
        for (String element : list1) {
            if (list2.contains(element)) {
                return true;
            }
        }
        return false;
    }
    static Comparator<Post> defaultComparator() {
        Function<Post, Boolean> pinned =
            post -> Objects.requireNonNullElse(post.getSpec().getPinned(), false);
        Function<Post, Integer> priority =
            post -> Objects.requireNonNullElse(post.getSpec().getPriority(), 0);
        Function<Post, Instant> publishTime =
            post -> post.getSpec().getPublishTime();
        Function<Post, String> name = post -> post.getMetadata().getName();
        return Comparator.comparing(pinned)
            .thenComparing(priority)
            .thenComparing(publishTime, Comparators.nullsLow())
            .thenComparing(name)
            .reversed();
    }
}
