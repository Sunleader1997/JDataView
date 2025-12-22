<script setup>
import {useAppStore} from "stores/app-store.js";
import {ref} from "vue";
import axios from "axios";

const appStore = useAppStore()
// 当前的 javaApp
const currentApp = appStore.currentApp
// 左侧初始宽度
const splitterModel = ref(250)
// 当前选中的 tab
const activeTab = ref('')
// 该app下的所有线程
const threads = ref([])
// 线程堆栈内容
const methodCallTree = ref([])
// 选择的 thread 发生切换的时候，请求获取堆栈
const onTabChange = function (threadId) {
  loadMethodCallTree(currentApp.appName, threadId)
}
const selectedMethod = ref(null)
// 获取堆栈
const loadMethodCallTree = function (appName, threadId) {
  axios
    .post("/jdv/api/javaApp/getMethodTree", {
      appName,
      threadId
    })
    .then(response => {
      methodCallTree.value = response.data
    })
}
// 获取线程列表
const loadAppThreadList = function (done) {
  axios
    .post("/jdv/api/javaApp/getTreadList", currentApp)
    .then(response => {
      const threadList = response.data;
      // 修改数据结构
      threads.value = threadList.map(thread => {
        return {
          ...thread,
          methodTree: [],
          active: false
        }
      })
      if (done) {
        done()
      }
    })
}
const showDate = function (timestamp) {
  const date = new Date(timestamp);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  });
}
loadAppThreadList()
</script>

<template>
  <q-page class="home-index bg-dark flex">
    <q-card dark class="flex full-width">
      <q-splitter v-model="splitterModel" unit="px" after-class="flex">
        <template v-slot:before>
          <q-tabs
            v-model="activeTab"
            dense
            class="text-grey scroll"
            active-color="primary"
            indicator-color="primary"
            vertical
            @update:model-value="onTabChange"
          >
            <q-pull-to-refresh @refresh="loadAppThreadList" bg-color="black">
              <q-tab v-for="thread in threads" :name="thread.threadId" :label="thread.threadName"
                     :key="thread.threadId"/>
            </q-pull-to-refresh>
          </q-tabs>
        </template>
        <template v-slot:after>
          <q-timeline class="q-ml-md" color="secondary">
            <q-timeline-entry
              v-for="methodCallItem in methodCallTree"
              :key="methodCallItem.id"
              :subtitle="showDate(methodCallItem.startTime)"
            >
              <div>
                <q-tree dark :nodes="[{...methodCallItem}]" node-key="id" v-model:selected="selectedMethod">
                  <template v-slot:default-header="prop">
                    <div class="row q-gutter-md">
                      <div class="text-weight-bold class-name">{{ prop.node.className }}</div>
                      <div class="text-weight-bold method-name">{{ prop.node.methodName }}</div>
                      <q-badge outline color="primary" :label="prop.node.cost + 'ms'"/>
                    </div>
                  </template>
                  <q-tooltip
                    transition-show="flip-right"
                    transition-hide="flip-left"
                  >
                    Here I am!
                  </q-tooltip>
                </q-tree>
              </div>
            </q-timeline-entry>

          </q-timeline>
        </template>
      </q-splitter>
    </q-card>
  </q-page>
</template>

<style scoped>
.class-name {
  color: #a5a5a5;
}

.method-name {
  color: #3f9be7;
}
</style>
