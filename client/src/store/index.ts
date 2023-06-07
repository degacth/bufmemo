import { createStore } from 'vuex'

const {
  VUE_APP_SERVER_ADDRESS,
  VUE_APP_SERVER_WS,
} = process.env

const wsAddress = `${VUE_APP_SERVER_WS}://${VUE_APP_SERVER_ADDRESS}/ws`
const ws = new WebSocket(wsAddress)

ws.onopen = () => console.log('opened')
ws.onmessage = event => console.log(JSON.parse(event.data))
ws.onclose = console.log
ws.onerror = console.error

document.onclick = e => ws.send("hello")

export default createStore({
  state: {
  },
  getters: {
  },
  mutations: {
  },
  actions: {
  },
  modules: {
  }
})
