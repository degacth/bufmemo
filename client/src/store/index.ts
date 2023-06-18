import {createStore, MutationTree} from 'vuex'
import {WS} from './ws'
import {loadClips} from './messages'
import * as _ from 'lodash'

const {
  VUE_APP_SERVER_ADDRESS,
  VUE_APP_SERVER_WS,
} = process.env

const ws = new WS(`${VUE_APP_SERVER_WS}://${VUE_APP_SERVER_ADDRESS}/ws`,
  msg => {
    const messageName = _.chain(msg).keys().first().value()
    if (!_.keys(mutations).includes(messageName)) return

    store.commit(messageName, msg[messageName].payload)
  },
  () => {
    ws.sendMessage(loadClips)
  })

ws.connect()

export interface State {
  clips: ClipContent[]
}

const mutations: MutationTree<State> = {
  WsGotClips(state, clips: ClipContent[]) {
    state.clips = clips
  },
  WsClipboardChanged() {
    ws.sendMessage(loadClips)
  }
}

export const actionNames = {
  selectClip: 'select-clip',
}

const store = createStore<State>({
  state: {
    clips: [],
  },
  getters: {},
  mutations,
  actions: {
    clips(ctx) {
      ws.sendMessage(loadClips)
    },
    [actionNames.selectClip](ctx, clipId: String) {
      ws.sendMessage({
        WsSetClip: {
          payload: clipId
        }
      })
    }
  },
})

export default store