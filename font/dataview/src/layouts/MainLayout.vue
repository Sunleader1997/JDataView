<template>
  <q-layout view="lHh Lpr lFf">
    <q-header elevated>
      <q-bar class="bg-black text-white">
        <q-breadcrumbs>
          <q-breadcrumbs-el icon="home" to="/"/>
          <q-breadcrumbs-el label="JavaApp" icon="widgets" to="/applications"/>
          <q-breadcrumbs-el :label="`PID(`+appStore.currentApp?.pid+`)`" icon="format_list_numbered" to="/applications/threads"/>
        </q-breadcrumbs>
        <q-space />
      </q-bar>
    </q-header>

    <!--    <q-drawer v-model="leftDrawerOpen" show-if-above bordered>-->
    <!--      <q-list>-->
    <!--        <q-item-label header> Essential Links </q-item-label>-->

    <!--        <EssentialLink v-for="link in linksList" :key="link.title" v-bind="link" />-->
    <!--      </q-list>-->
    <!--    </q-drawer>-->

    <q-page-container>
      <router-view/>
    </q-page-container>
  </q-layout>
</template>

<script setup>
import {useAppStore} from "stores/app-store.js";

const appStore = useAppStore()
</script>
