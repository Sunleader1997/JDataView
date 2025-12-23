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
    .get("/api/javaApp/getJavaApps")
    .then(response => {
      javaApps.value = response.data
    })
}
const clearAppMsg = function (javaApp) {
  axios
    .post("/api/javaApp/clearAppMsg", javaApp)
    .then(response => {
      console.log(response)
      loadJavaApp()
    })
}
const attachJavaApp = ref({})
const attachApp = function () {
  attachJavaApp.value.loading = true
  axios
    .post("/api/javaApp/attach", attachJavaApp.value)
    .then(response => {
      console.log(response)
      attachJavaApp.value.loading = false
      attachDialog.value = false
      loadJavaApp()
    }).catch(error => {
      console.log(error)
      attachJavaApp.value.loading = false
    })
}
const detach = function (javaApp) {
  javaApp.loading = true
  axios
    .post("/api/javaApp/detach", javaApp)
    .then(response => {
      console.log(response)
      javaApp.loading = false
      loadJavaApp()
    }).catch(error => {
    console.log(error)
    javaApp.loading = false
  })
}
const attachDialog = ref(false)
const openAttachDialog = function (javaApp){
  attachDialog.value = true
  attachJavaApp.value = {...toRaw(javaApp), scanPackage: ''}
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
              <q-btn v-else dense flat icon="input" color="primary" @click.stop="openAttachDialog(javaApp)"/>
            </div>
          </q-item-section>
        </q-item>
      </q-slide-item>
    </q-list>
    <q-dialog v-model="attachDialog" persistent backdrop-filter="blur(4px)">
      <q-card dark style="min-width: 350px">
        <q-card-section>
          <div class="text-h6">Scan Package</div>
        </q-card-section>

        <q-card-section class="q-pt-none">
          <q-input dark dense standout v-model="attachJavaApp.scanPackage" autofocus placeholder="com.xxx.xxx"/>
        </q-card-section>

        <q-card-actions align="right" class="text-primary">
          <q-btn flat label="Cancel" v-close-popup />
          <q-btn flat label="Attach" @click="attachApp()" :loading="attachJavaApp.loading"/>
        </q-card-actions>
      </q-card>
    </q-dialog>
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
