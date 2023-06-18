<template>
  <ul class="uk-list uk-margin-top uk-width-1-2@m uk-width-1-1@s uk-list-striped">
    <li v-for="clip in $store.state.clips" :key="clip.id" :class="currentClipCssClass(clip)">
      <div uk-grid>
        <a class="uk-width-expand uk-flex uk-flex-middle uk-link-text uk-link-reset"
           @click.prevent="selectClip(clip)"
           href="#">
          <span class="uk-text-truncate" :title="clip.content">{{ clip.content }}</span>
        </a>
        <div class="uk-width-1-4 uk-text-right uk-hidden">
          <button class="uk-icon-button uk-margin-small-right" uk-icon="pencil"></button>
          <button class="uk-icon-button uk-margin-small-right" uk-icon="check"></button>
          <button class="uk-icon-button uk-button-danger" uk-icon="close"></button>
        </div>
      </div>
    </li>
  </ul>
</template>

<script lang="ts">
import {defineComponent} from 'vue'
import * as _ from 'lodash'
import {actionNames} from '@/store'

export default defineComponent({
  methods: {
    isCurrent(clip: ClipContent): boolean {
      const {clips} = this.$store.state
      if (!clips.length) return false

      return _.first(clips)!.id == clip.id;
    },
    currentClipCssClass(clip: ClipContent): Object | undefined {
      if (this.isCurrent(clip)) return {
        'current-clip': true,
        'uk-text-bolder': true,
      }
    },
    selectClip(clip: ClipContent) {
      if (this.isCurrent(clip)) return
      this.$store.dispatch(actionNames.selectClip, clip.id)
    }
  }
});
</script>

<style lang="stylus">
.current-clip
  .uk-link-text
    cursor default
</style>
