<script setup>
import { computed } from 'vue'
import { Position, Handle } from '@vue-flow/core'

const props = defineProps([
  'id',
  'data',
  'position',
  'type',
  'events',
  'selected',
  'resizing',
  'dragging',
  'connectable',
  'dimensions',
  'isValidTargetPos',
  'isValidSourcePos',
  'parent',
  'parentNodeId',
  'zIndex',
  'targetPosition',
  'sourcePosition',
  'label',
  'dragHandle',
  'pluginDraggable',
])

const x = computed(() => `${Math.round(props.position.x)}px`)
const y = computed(() => `${Math.round(props.position.y)}px`)
</script>

<template>
  <q-card class="jdv-class-node" dark>
    <q-card-section>
      <div class="text-h6">{{ data.label }}</div>
      <div class="text-subtitle2">{{ data.description }} {{x}},{{y}}</div>
    </q-card-section>
    <q-separator dark/>
    <q-card-actions vertical>
      <q-btn v-for="(handle,index) in data.handles" flat :label="handle" :key="handle" no-caps>
        <Handle type="target" :position="Position.Left" :id="'t-'+index"/>
        <Handle type="source" :position="Position.Right" :id="'s-'+index" />
      </q-btn>
    </q-card-actions>
  </q-card>
</template>
<style scoped>
.jdv-class-node{
  min-width: 160px;
}
</style>
