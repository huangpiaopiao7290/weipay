import Vue from 'vue'
import App from './App.vue'
// import Vuex from 'vuex'
import VueRouter from 'vue-router'


// import store from './store/index'
import router from './router/index'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css';
// 二维码生成器
import VueQriously from 'vue-qriously'



Vue.config.productionTip = false

// Vue.use(Vuex)
Vue.use(VueRouter)
Vue.use(ElementUI)
Vue.use(VueQriously)
  
new Vue({
  el: '#app',
  router:router,
  // store,
  render: h => h(App),

  beforeCreate(){
    Vue.prototype.$bus = this
  }
})
