import {acceptHMRUpdate, defineStore} from 'pinia'

export const useAppStore = defineStore('app', {
  state: () => ({
    currentApp: null,
  }),

  getters: {
    getCurrentApp: (state) => state.currentApp,
  },

  actions: {
    clear() {
      this.currentApp = null
    },
    setCurrentApp(app) {
      this.currentApp = app
    },
  },
})

if (import.meta.hot) {
  import.meta.hot.accept(acceptHMRUpdate(useAppStore, import.meta.hot))
}
