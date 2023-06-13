import {createStore} from 'vuex'
import {WS} from './ws'

const {
  VUE_APP_SERVER_ADDRESS,
  VUE_APP_SERVER_WS,
} = process.env

const ws = new WS(`${VUE_APP_SERVER_WS}://${VUE_APP_SERVER_ADDRESS}/ws`, msg => {
  console.log('receive message', msg)
})

ws.connect()

export default createStore({
  state: {},
  getters: {},
  mutations: {},
  actions: {},
  modules: {}
})
