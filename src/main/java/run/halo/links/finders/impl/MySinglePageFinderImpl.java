package run.halo.links.finders.impl;

import lombok.AllArgsConstructor;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.metrics.MeterUtils;
import run.halo.app.theme.finders.Finder;
import run.halo.links.MyPostFinder;
import run.halo.links.MySinglePageFinder;
import run.halo.links.vo.ContentVo;
import run.halo.links.vo.MyAnonymousUserConst;
import run.halo.links.vo.MyListedSinglePageVo;
import run.halo.links.vo.MyPostVo;
import run.halo.links.vo.MySinglePageVo;
import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Finder("mySinglePageFinder")
@AllArgsConstructor
public class MySinglePageFinderImpl implements MySinglePageFinder {
    private final ReactiveExtensionClient client;

    @Override
    public Mono<MySinglePageVo> getByName(String pageName)  {
        System.out.println("single2222");
        Mono<MySinglePageVo> obj =  client.get(SinglePage.class, pageName)
            .map(MySinglePageVo::from);

        System.out.println(obj);
        return obj;


    }

    @Override
    public Map<String,String> getAnnotationsByArticle(String name, String patternString) {
        System.out.println("getannotions111");
        MySinglePageVo obj = this.getByName(name).block();
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
}
