const routes = [
  {
    path: '/',
    component: () => import('layouts/MainLayout.vue'),
    children: [
      {
        path: '',
        name: 'home',
        component: () => import('pages/IndexPage.vue')
      },
      {
        path: 'applications',
        name: 'applications',
        component: () => import('pages/appmgmt/AppMgmtIndex.vue'),
      },
      {
        path: 'applications/threads',
        name: 'threads',
        component: () => import('pages/appmgmt/threads/ThreadList.vue')
      },
      {
        path: 'applications/threads/panel',
        name: 'threadPanel',
        component: () => import('pages/vueflow/panel/JDataPanel.vue')
      }
    ],
  },

  // Always leave this as last one,
  // but you can also remove it
  {
    path: '/:catchAll(.*)*',
    component: () => import('pages/ErrorNotFound.vue'),
  },
]

export default routes
