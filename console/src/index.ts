import {definePlugin} from "@halo-dev/console-shared";
import {markRaw} from "vue";
import WangEditor from "./components/HomeView.vue";


export default definePlugin({
  extensionPoints: {
    // @ts-ignore
    "editor:create": () => {
      return [
        {
          name: "CeegEditor",
          displayName: "CeegEditor",
          component: markRaw(WangEditor),
          rawType: "html",
        },
      ];
    },
  },
});
