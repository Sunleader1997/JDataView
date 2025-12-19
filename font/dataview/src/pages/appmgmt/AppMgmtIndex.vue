<script setup>
import {ref, toRaw} from 'vue'
import {useAppStore} from "stores/app-store.js";
import {useRouter} from "vue-router";
import axios from "axios";
// use
const router = useRouter()
const appStore = useAppStore();
// data
const javaApps = ref([])
// function
// 跳转到 APP 的线程列表
const jumpToThreadList = function (javaApp) {
  const appRow = toRaw(javaApp);
  appStore.setCurrentApp(appRow)
  router.push({name: 'threads'})
}
const loadJavaApp = function () {
  axios
    .get("/jdv/api/javaApp/getJavaApps")
    .then(response=>{
      javaApps.value = response.data
    })
}
// init
loadJavaApp()
</script>

<template>
  <q-page class="home-index bg-dark">
    <q-list dark bordered separator class="full-width">
      <q-item clickable v-ripple v-for="javaApp in javaApps" :key="javaApp" @click="jumpToThreadList(javaApp)">
        <q-item-section>
          <q-item-label lines="1">{{ javaApp.appName }}</q-item-label>
          <q-item-label caption>{{ javaApp.host }} | {{ javaApp.pid }}</q-item-label>
        </q-item-section>
      </q-item>
    </q-list>
  </q-page>
</template>

<style scoped>
.home-index {
  height: calc(100vh - 50px);
}
</style>
