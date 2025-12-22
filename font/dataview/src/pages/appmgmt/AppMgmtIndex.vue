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
    .then(response => {
      javaApps.value = response.data
    })
}
const clearAppMsg = function (javaApp) {
  axios
    .post("/jdv/api/javaApp/clearAppMsg", javaApp)
    .then(response => {
      console.log(response)
      loadJavaApp()
    })
}
const attachApp = function (javaApp) {
  javaApp.loading = true
  axios
    .post("/jdv/api/javaApp/attach", javaApp)
    .then(response => {
      console.log(response)
      javaApp.loading = false
      loadJavaApp()
    }).catch(error => {
      console.log(error)
      javaApp.loading = false
    })
}
const detach = function (javaApp) {
  javaApp.loading = true
  axios
    .post("/jdv/api/javaApp/detach", javaApp)
    .then(response => {
      console.log(response)
      javaApp.loading = false
      loadJavaApp()
    }).catch(error => {
    console.log(error)
    javaApp.loading = false
  })
}
// init
loadJavaApp()
</script>

<template>
  <q-page class="home-index bg-dark">
    <q-bar class="bg-black text-white">
      <q-btn dark icon="refresh" dense @click="loadJavaApp()"/>
      <q-space/>
    </q-bar>
    <q-list dark bordered separator class="full-width">
      <q-slide-item dark left-color="red" v-for="javaApp in javaApps" :key="javaApp" @left="clearAppMsg(javaApp)">
        <template v-slot:left>
          <div class="row items-center">
            <q-icon left name="clear"/>
            清空日志
          </div>
        </template>
        <q-item v-ripple @click="jumpToThreadList(javaApp)" :clickable="javaApp.hasLog">
          <q-item-section>
            <q-item-label lines="1">{{ javaApp.appName }}</q-item-label>
            <q-item-label caption :class="javaApp.alive?'caption-running':'caption-death'">
              {{ javaApp.alive ? 'RUNNING' : 'DEATH' }} | {{ javaApp.pid }}
            </q-item-label>
          </q-item-section>
          <q-item-section side top>
            <div class="text-grey-8 q-gutter-xs">
              <q-btn v-if="javaApp.hasAttached" dense flat icon="output" color="red" @click.stop="detach(javaApp)" :loading="javaApp.loading"/>
              <q-btn v-else dense flat icon="input" color="primary" @click.stop="attachApp(javaApp)" :loading="javaApp.loading"/>
            </div>
          </q-item-section>
        </q-item>
      </q-slide-item>
    </q-list>
  </q-page>
</template>

<style scoped>
.home-index {
  height: calc(100vh - 50px);
}

.caption-running {
  color: green;
}

.caption-death {
  color: red;
}
</style>
