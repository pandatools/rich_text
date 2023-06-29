package run.halo.links;

import org.pf4j.PluginWrapper;
import org.springframework.stereotype.Component;
import run.halo.app.plugin.BasePlugin;
import run.halo.app.extension.SchemeManager;
import run.halo.links.vo.CategoryTreeVo;
import run.halo.links.vo.CategoryVo;
import run.halo.links.vo.ContentVo;
import run.halo.links.vo.MyContributorVo;
import run.halo.links.vo.MyListedPostVo;
import run.halo.links.vo.MyNavigationPostVo;
import run.halo.links.vo.MyPostVo;
import run.halo.links.vo.MyStatsVo;
import run.halo.links.vo.MyTagVo;

/**
 * <p>Plugin main class to manage the lifecycle of the plugin.</p>
 * <p>This class must be public and have a public constructor.</p>
 * <p>Only one main class extending {@link BasePlugin} is allowed per plugin.</p>
 *
 * @author guqing
 * @since 1.0.0
 */
@Component
public class StarterPlugins extends BasePlugin {
    private final SchemeManager schemeManager;
    public StarterPlugins(PluginWrapper wrapper, SchemeManager schemeManager) {
        super(wrapper);
        this.schemeManager = schemeManager;
    }

    @Override
    public void start() {
        schemeManager.register(CategoryVo.class);
        schemeManager.register(CategoryTreeVo.class);
        schemeManager.register(ContentVo.class);
        schemeManager.register(MyContributorVo.class);
        schemeManager.register(MyListedPostVo.class);
        schemeManager.register(MyNavigationPostVo.class);
        schemeManager.register(MyPostVo.class);
        schemeManager.register(MyStatsVo.class);
        schemeManager.register(MyTagVo.class);
        System.out.println("1111ok");
    }

    @Override
    public void stop() {
        schemeManager.unregister(schemeManager.get(CategoryVo.class));
        schemeManager.unregister(schemeManager.get(CategoryTreeVo.class));
        schemeManager.unregister(schemeManager.get(ContentVo.class));
        schemeManager.unregister(schemeManager.get(MyContributorVo.class));
        schemeManager.unregister(schemeManager.get(MyListedPostVo.class));
        schemeManager.unregister(schemeManager.get(MyNavigationPostVo.class));
        schemeManager.unregister(schemeManager.get(MyPostVo.class));
        schemeManager.unregister(schemeManager.get(MyStatsVo.class));
        schemeManager.unregister(schemeManager.get(MyTagVo.class));
        System.out.println("222stop");
    }
}
