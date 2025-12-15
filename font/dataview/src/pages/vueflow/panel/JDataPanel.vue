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
    position: {x: 250, y: 5},
    data: {
      label: 'TestController.class',
      description: 'by SunYaxing',
      handles: [
        "String demo(String name)"
      ]
    },
  },

  // default node, you can omit `type: 'default'` as it's the fallback type
  {
    id: '2',
    type: 'special',
    position: {x: 100, y: 100},
    data: {
      label: 'TestService.class',
      description: 'by SunYaxing',
      handles: [
        "String test(String demo1)",
        "String test2(String demo1)"
      ]
    },
  },

  // An output node, specified by using `type: 'output'`
  {
    id: '3',
    type: 'output',
    position: {x: 400, y: 200},
    data: {label: 'Node 3'},
  },

  // this is a custom node
  // we set it by using a custom type name we choose, in this example `special`
  // the name can be freely chosen, there are no restrictions as long as it's a string
  {
    id: '4',
    type: 'special', // <-- this is the custom node type name
    position: {x: 400, y: 200},
    data: {
      label: 'JDataViewServerApplication.class',
      description: 'by SunYaxing',
      handles: [
        "void org.sunyaxing.imagine.jdataview.TestService()",
        "class java.lang.String test(java.lang.String demo1)",
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
