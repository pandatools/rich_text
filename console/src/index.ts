import {markRaw} from "vue";
import HomeView from "./components/HomeView.vue";
 import {IconPlug} from "@halo-dev/components";
import {definePlugin} from "@halo-dev/console-shared";

export default definePlugin({
    routes: [                                 // Console 控制台路由定义
      {
        parentName: "Root",
        route: {
          path: "/pcdn",
          name: "Pcdn",
          component: HomeView,
          meta: {
            title: "DNS缓存刷新",
            searchable: true,
            menu: {
              name: "DNS缓存刷新",
              group: "content",
              icon: markRaw(IconPlug),
              priority: 40
            },
          },
        },
      }
    ]  ,

});
