package run.halo.links;

import org.pf4j.PluginWrapper;
import org.springframework.stereotype.Component;
import run.halo.app.plugin.BasePlugin;
import run.halo.app.extension.SchemeManager;
import run.halo.links.vo.MyCategoryTreeVo;
import run.halo.links.vo.MyCategoryVo;

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
        schemeManager.register(MyCategoryVo.class);
        schemeManager.register(MyCategoryTreeVo.class);
        System.out.println("1111ok");
    }

    @Override
    public void stop() {
        schemeManager.unregister(schemeManager.get(MyCategoryVo.class));
        schemeManager.unregister(schemeManager.get(MyCategoryTreeVo.class));
        System.out.println("222stop");
    }
}
