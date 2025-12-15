<script setup>
import {ref} from 'vue'
import {VueFlow} from '@vue-flow/core'

// these components are only shown as examples of how to use a custom node or edge
// you can find many examples of how to create these custom components in the examples page of the docs
import SpecialNode from './components/SpecialNode.vue'
import SpecialEdge from './components/SpecialEdge.vue'
import {Background} from "@vue-flow/background";
import {ControlButton, Controls} from "@vue-flow/controls";
import {MiniMap} from "@vue-flow/minimap";

// these are our nodes
const nodes = ref([
  // an input node, specified by using `type: 'input'`
  {
    id: '1',
    type: 'special',
    position: {x: 256, y: 0},
    data: {
      label: 'TestController.class',
      description: 'by SunYaxing',
      handles: [
        {
          id: "m1",
          returnType: "String",
          methodName: "demo",
          parameters: [
            "String demo1"
          ],
          cost: 90
        }
      ]
    },
  },

  // default node, you can omit `type: 'default'` as it's the fallback type
  {
    id: '2',
    type: 'special',
    position: {x: 640, y: 0},
    data: {
      label: 'TestService.class',
      description: 'by SunYaxing',
      handles: [
        {
          id: "m1",
          returnType: "String",
          methodName: "test",
          parameters: [
            "String demo1"
          ],
          cost: 20
        },
        {
          id: "m2",
          returnType: "String",
          methodName: "test2",
          parameters: [
            "String demo1"
          ],
          cost: 20
        },
      ]
    },
  },

  // this is a custom node
  // we set it by using a custom type name we choose, in this example `special`
  // the name can be freely chosen, there are no restrictions as long as it's a string
  {
    id: '3',
    type: 'special', // <-- this is the custom node type name
    position: {x: 400, y: 200},
    data: {
      label: 'JDataViewServerApplication.class',
      description: 'by SunYaxing',
      handles: [
        {
          id: "m1",
          returnType: "void",
          methodName: "TestService",
          parameters: [],
          cost: 20
        },
        {
          id: "m2",
          returnType: "class",
          methodName: "test",
          parameters: [
            "String demo1"
          ],
          cost: 120
        },
      ]
    },
  },
])

// these are our edges
const edges = ref([
  // default bezier edge
  // consists of an edge id, source node id and target node id
  {
    id: 'e1->2',
    type: 'special',
    source: '1',
    sourceHandle: 's-0',
    target: '2',
    targetHandle: 't-0',
    animated: true,
    data: {
      hello: 'world',
    }
  },

  // set `animated: true` to create an animated edge path
  {
    id: 'e2->e2',
    type: 'special',
    source: '2',
    sourceHandle: 's-0',
    target: '2',
    targetHandle: 't-1',
    animated: true,
    data: {
      hello: 'world',
    }
  },
])
</script>

<template>
  <q-page>
    <VueFlow class="jdv-vueflow dark" min-zoom="0.1" max-zoom="2" interactive :nodes="nodes" :edges="edges" snap-to-grid :snapGrid="[16,16]">
      <Background pattern-color="#aaa" :gap="16" />
      <Controls position="top-left">
        <ControlButton title="Shuffle Node Positions">
        </ControlButton>
      </Controls>
      <MiniMap />
      <!-- bind your custom node type to a component by using slots, slot names are always `node-<type>` -->
      <template #node-special="specialNodeProps">
        <SpecialNode v-bind="specialNodeProps"/>
      </template>

      <!-- bind your custom edge type to a component by using slots, slot names are always `edge-<type>` -->
      <template #edge-special="specialEdgeProps">
        <SpecialEdge v-bind="specialEdgeProps"/>
      </template>
    </VueFlow>
  </q-page>
</template>

<style scoped>
.jdv-vueflow{
  height: calc(100vh - 50px);
}
.jdv-vueflow.dark {
  background:#2d3748;
  color:#fffffb
}
</style>
