<script lang="ts" setup>
import { component as ckeditor } from '@mayasabha/ckeditor4-vue3'
import { ref,watch } from 'vue'

const props = withDefaults(
  defineProps<{
    raw?: string;
    content: string;
  }>(),
  {
    raw: "",
    content: "",
  }
);

const editorData = ref("test");

// const emit = defineEmits<{
//   (event: "update:raw", value: string): void;
//   (event: "update:content", value: string): void;
//   (event: "update", value: string): void;
// }>();
const emit = defineEmits(["update:raw","update:content","update"])
watch(editorData, (newVal, oldVal) => {
  console.log('editorData 发生了变化！sss新值:', newVal, '旧值:', oldVal,typeof(newVal));
  emit("update:raw", newVal);
  emit("update:content", newVal);
  emit("update", newVal);
  console.log('editorData 发生了变化！sss新值:', props.raw, '旧值:', props.content);
});
</script>

<template>
  <ckeditor :model-value="raw"  v-model="editorData" ></ckeditor>
</template>
