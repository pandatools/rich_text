<template>
  <div class="box">
        <button @click="freshCDN">刷新cdn</button>
    <button @click="showBox">配置cdn</button>
  </div>

  <!-- 模态框 -->
  <div v-if="showModal" class="modal">
    <div class="modal-content">
      <span class="close" @click="showModal = false">&times;</span>
      <h3>配置 CDN</h3>
      <div>
        <label for="刷新路径">刷新路径:</label>
        <input type="text" id="object_path" v-model="object_path" placeholder="object_path" />
      </div>
      <div>
        <label for="刷新类型">刷新类型:</label>
        <input type="text" id="object_type" v-model="object_type" placeholder="object_type" />
      </div>
      <div>
        <label for="阿里云密钥id">阿里云密钥id:</label>
        <input type="text" id="access_key_id" v-model="access_key_id" placeholder="access_key_id" />
      </div>
      <div>
        <label for="阿里云密钥">密码:</label>
        <input type="text" id="access_key_secret" v-model="access_key_secret" placeholder="access_key_secret" />
      </div>
      <button @click="submitConfig">提交</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import axios from "axios";
import {onMounted, ref} from "vue";
import type {Pcdn} from "../types";

const showModal = ref(false);
const object_path = ref("");
const object_type = ref("");
const access_key_id = ref("");
const access_key_secret = ref("");

const currentPath = window.location.pathname;
const path_list = currentPath.split('/console')
let baseURL= "/"
if(path_list[0]!=""){
  baseURL = path_list[0]
}
console.log("baseurl=",baseURL)
const http = axios.create({
  baseURL: baseURL,
  timeout: 1000,
});

function getConfig() {
  return http.get("/apis/pcdn.plugin.halo.run/v1alpha1/pcdn")
    .then(response => response.data);
}
function deleteConfig(name:string){
  const url = "/apis/pcdn.plugin.halo.run/v1alpha1/pcdn" + "/" + name;
  return http.delete(url)
}

onMounted(() => {
  setConfig()
})

function showBox(){
  showModal.value = true;
}

async function setConfig(){

  const data = await getConfig();
  const items = data.items;
  console.log(items);
  
  for (const item of items) {
    if(item.metadata.annotations != undefined){
      object_path.value = item.metadata.annotations.ObjectPath
      object_type.value = item.metadata.annotations.ObjectType
      access_key_secret.value = item.metadata.annotations.secret
      access_key_id.value = item.metadata.annotations.accesskey
    }
    else{
      let response = await deleteConfig(item.metadata.name)
      console.log(response);
      console.log(item.metadata.name);
    }

  }
}

function createConfig() {
  http
    .post("/apis/pcdn.plugin.halo.run/v1alpha1/pcdn", {
      metadata: {
        generateName: "pcdn-",
        annotations: {
          accesskey: access_key_secret.value,
          secret: access_key_id.value,
          ObjectPath: object_path.value,
          ObjectType: object_type.value
        }
      },
      spec: {
        done: false,
      },
      kind: "Pcdn",
      apiVersion: "pcdn.plugin.halo.run/v1alpha1",
    })
    .then((response) => {
      
    });
}

function freshCDN() {
  const url = "https://api.ceegic.com/api/sale_sync/contract/sync/ali/pcdn";

  const payload = {
    object_path:  object_path.value,
    object_type: object_type.value,
    access_key_id: access_key_id.value,
    access_key_secret: access_key_secret.value,
  };
  const headers = {
    Authorization: "fbbdb391-a2cb-452b-b42f-e015dfdb1538",
    "Content-Type": "application/json",
  };
console.log(payload)
  
  axios
    .post(url, payload, { headers: headers })
    .then((response) => {
      console.log(response.data.message);
      alert(response.data.message);
    })
    .catch((error) => {
      alert(error);
    });
}

async function submitConfig() {
  // 在这里添加提交配置的逻辑
  console.log("object_path:", object_path.value);
  console.log("object_type:", object_type.value);
  console.log("access_key_id:", access_key_id.value);
  console.log("access_key_secret:", access_key_secret.value);
  const data = await getConfig();
  if(data.items.length != 0){
    for (const item of data.items) {
        let response = await deleteConfig(item.metadata.name)
        console.log(item.metadata.name);
    }
  }
  await createConfig();
  showModal.value = false;
}
</script>
<style scoped>
button {
  background-color: dodgerblue;
  color: white;
  width: 300px;
  height: 47px;
  border: 0;
  font-size: 16px;
  border-radius: 30px;
  display: block;
  margin: 100px auto 20px;
}

.box {
  display: flex;
  display: inline-flex;
  display: -webkit-flex;
  flex-direction: row;
}

/* 模态框样式 */
.modal {
  position: fixed;
  z-index: 1;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  overflow: auto;
  background-color: rgba(0, 0, 0, 0.4);
  border: 4px solid #888; /* 添加边框 */
}

.modal-content {
  background-color: #fefefe;
  margin: 15% auto;
  padding: 20px;
  border: 1px solid #888;
  width: 30%;
}

.close {
  color: #aaa;
  float: right;
  font-size: 28px;
  font-weight: bold;
}

.close:hover,
.close:focus {
  color: black;
  text-decoration: none;
  cursor: pointer;
}
</style>
