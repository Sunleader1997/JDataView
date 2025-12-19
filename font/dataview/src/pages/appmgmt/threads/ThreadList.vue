<script setup>
import {useAppStore} from "stores/app-store.js";
import {ref} from "vue";
import axios from "axios";

const appStore = useAppStore()
const currentApp = appStore.currentApp
const splitterModel = ref(20)
const activeTab = ref('')
const threads = ref([])
const onTabChange = function (thread) {
  console.log(thread)
}
// const loadMethodCallTree = function (appName, threadId) {
//   axios
//     .post("/jdv/api/javaApp/getMethodTree", {
//       appName,
//       threadId
//     })
//     .then(response => {
//       threads.methodTree = response.data
//     })
// }
const loadAppThreadList = function () {
  axios
    .post("/jdv/api/javaApp/getTreadList", currentApp)
    .then(response => {
      threads.value = {
        ...response.data,
        active: false,
        methodTree: []
      }
    })
}
loadAppThreadList()
</script>

<template>
  <q-page class="home-index bg-dark">
    <q-card>
      <q-splitter
        v-model="splitterModel"
      >
        <template v-slot:before>
          <q-tabs
            v-model="activeTab"
            dense
            class="text-grey"
            active-color="primary"
            indicator-color="primary"
            vertical
            @update="onTabChange"
          >
            <q-tab v-for="thread in threads" :name="thread.threadId" :label="thread.threadName"/>
          </q-tabs>
        </template>
        <template v-slot:after>
          <q-tab-panels v-model="activeTab" animated swipeable vertical transition-prev="jump-up"
                        transition-next="jump-up">
            <q-tab-panel v-for="thread in threads" :name="thread.threadId">
              <div class="text-h6">Mails</div>
              Lorem ipsum dolor sit amet consectetur adipisicing elit.
            </q-tab-panel>
          </q-tab-panels>
        </template>
      </q-splitter>
    </q-card>
  </q-page>
</template>

<style scoped>

</style>
