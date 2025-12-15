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
      <div class="text-h6 class-name">{{ data.label }}</div>
      <div class="text-subtitle2 class-name">{{ data.description }} {{x}},{{y}}</div>
    </q-card-section>
    <q-separator dark/>
    <q-card-actions vertical>
      <q-btn v-for="(handle,index) in data.handles" flat :key="handle.id" no-caps align="between">
        <div>
          <a class="return-type">{{handle.returnType}}</a>
          <a class="method-name">{{handle.methodName}}</a>
          <a class="parameters">{{`(${handle.parameters.join(',')})`}}</a>
        </div>
        <q-badge color="blue">
          {{handle.cost}}ms
        </q-badge>
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
.class-name{
  color: #a5a5a5;
}
.return-type{
  color: #d3b677;
  padding-right: 0.5rem;
}
.method-name{
  color: #3f9be7;
}
.parameters{
  color: #a5a5a5;
  padding-right: 0.5rem;
}
</style>
