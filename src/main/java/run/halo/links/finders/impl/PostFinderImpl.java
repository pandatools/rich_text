package run.halo.links.finders.impl;


import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.comparator.Comparators;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.Snapshot;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.ReactiveExtensionClient;

import run.halo.app.theme.finders.Finder;

import run.halo.links.ContentWrapper;
import run.halo.links.MyPostFinder;
// import run.halo.links.MyPostPublicQueryService;
import run.halo.links.vo.ContentVo;
import run.halo.links.vo.MyListedPostVo;
import run.halo.links.vo.MyPostVo;

/**
 * A finder for {@link Post}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Finder("mypostFinder")
@AllArgsConstructor
public class PostFinderImpl implements MyPostFinder {


    private final ReactiveExtensionClient client;

    // private final MyPostPublicQueryService postPublicQueryService;

    // public Mono<MyListedPostVo> convertToListedPostVo(@NonNull Post post) {
    //     Assert.notNull(post, "Post must not be null");
    //     MyListedPostVo postVo = MyListedPostVo.from(post);
    //     postVo.setCategories(List.of());
    //     postVo.setTags(List.of());
    //     postVo.setContributors(List.of());
    //
    //     return Mono.just(postVo)
    //         .flatMap(lp -> populateStats(postVo)
    //             .doOnNext(lp::setStats)
    //             .thenReturn(lp)
    //         )
    //         .flatMap(p -> {
    //             String owner = p.getSpec().getOwner();
    //             return contributorFinder.getContributor(owner)
    //                 .doOnNext(p::setOwner)
    //                 .thenReturn(p);
    //         })
    //         .flatMap(p -> {
    //             List<String> tagNames = p.getSpec().getTags();
    //             if (CollectionUtils.isEmpty(tagNames)) {
    //                 return Mono.just(p);
    //             }
    //             return tagFinder.getByNames(tagNames)
    //                 .collectList()
    //                 .doOnNext(p::setTags)
    //                 .thenReturn(p);
    //         })
    //         .flatMap(p -> {
    //             List<String> categoryNames = p.getSpec().getCategories();
    //             if (CollectionUtils.isEmpty(categoryNames)) {
    //                 return Mono.just(p);
    //             }
    //             return categoryFinder.getByNames(categoryNames)
    //                 .collectList()
    //                 .doOnNext(p::setCategories)
    //                 .thenReturn(p);
    //         })
    //         .flatMap(p -> contributorFinder.getContributors(p.getStatus().getContributors())
    //             .collectList()
    //             .doOnNext(p::setContributors)
    //             .thenReturn(p)
    //         )
    //         .defaultIfEmpty(postVo);
    // }

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
        // System.out.println("eeeeeeeeeeeeeeeeeeeeeeeeeeeee post = " + post);
        // return Mono.empty();
        // System.out.println("dddddddddddddddddddddddddddddddd");
        // return client.get(Post.class, postName)
        //     .filter(FIXED_PREDICATE)
        //     // .flatMap(postPublicQueryService::convertToListedPostVo)
        //     .map(MyPostVo::from)
        //     .flatMap(postVo -> content(postName)
        //         .doOnNext(postVo::setContent)
        //         .thenReturn(postVo)
        //     );
    }

    @Override
    public Map<String,String> getAnnotationsByArticle(String name, String patternString) {
        MyPostVo obj = this.getByName(name).block();
        Map<String, String> result = new HashMap<>();
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
            // .flatMap(lp -> populateStats(postVo)
            //     .doOnNext(lp::setStats)
            //     .thenReturn(lp)
            // )
            // .flatMap(p -> {
            //     String owner = p.getSpec().getOwner();
            //     return contributorFinder.getContributor(owner)
            //         .doOnNext(p::setOwner)
            //         .thenReturn(p);
            // })
            // .flatMap(p -> {
            //     List<String> tagNames = p.getSpec().getTags();
            //     if (CollectionUtils.isEmpty(tagNames)) {
            //         return Mono.just(p);
            //     }
            //     return tagFinder.getByNames(tagNames)
            //         .collectList()
            //         .doOnNext(p::setTags)
            //         .thenReturn(p);
            // })
            // .flatMap(p -> {
            //     List<String> categoryNames = p.getSpec().getCategories();
            //     if (CollectionUtils.isEmpty(categoryNames)) {
            //         return Mono.just(p);
            //     }
            //     return categoryFinder.getByNames(categoryNames)
            //         .collectList()
            //         .doOnNext(p::setCategories)
            //         .thenReturn(p);
            // })
            // .flatMap(p -> contributorFinder.getContributors(p.getStatus().getContributors())
            //     .collectList()
            //     .doOnNext(p::setContributors)
            //     .thenReturn(p)
            // )
            .defaultIfEmpty(postVo);
    }

    @Override
    public Flux<MyListedPostVo> listAll() {

        // Predicate<Post> FIXED_PREDICATE = post -> post.isPublished()
        //     && Objects.equals(false, post.getSpec().getDeleted())
        //     && Post.VisibleEnum.PUBLIC.equals(post.getSpec().getVisible());


        var list = client.list(Post.class, post -> post.isPublished()
                    && Objects.equals(false, post.getSpec().getDeleted())
                    && Post.VisibleEnum.PUBLIC.equals(post.getSpec().getVisible())

                , defaultComparator())
            .concatMap(this::convertToListedPostVo);
        // Flux<MyListedPostVo> listedPostVoFlux1 =
        //     client.list(Post.class, FIXED_PREDICATE, defaultComparator())
        //         .concatMap(postPublicQueryService::convertToListedPostVo);
        //
        // // List<ListedPostVo> lists = listedPostVoFlux.collectList().block();
        // // for (ListedPostVo item : lists) {
        // //     System.out.println(item);
        //
        // // }
        // String patternString = "^check";
        // List<MyListedPostVo> path = new ArrayList<>();
        // client.list(Post.class, FIXED_PREDICATE, defaultComparator())
        //     .concatMap(postPublicQueryService::convertToListedPostVo).subscribe(
        //         ListedPostVo -> {
        //             Map<String, String> annotations =
        //                 ListedPostVo.getMetadata().getAnnotations();
        //
        //             for (String key : annotations.keySet()) {
        //                 Pattern pattern = Pattern.compile(patternString);
        //                 Matcher matcher = pattern.matcher(key);
        //                 if (matcher.find()) {
        //                     path.add(ListedPostVo);
        //                     break;
        //                 }
        //             }
        //
        //         }
        //     );


        // return listedPostVoFlux1;
        return list;
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
