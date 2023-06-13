<template>
  <ul class="uk-list uk-margin-top uk-width-1-2@m uk-width-1-1@s uk-list-striped">
    <li v-for="clip in $store.state.clips">
      <div uk-grid>
        <div class="uk-width-expand">
          <div class="uk-text-truncate uk-text-muted uk-text-baseline">{{ clip }}</div>
        </div>
        <div class="uk-width-1-4 uk-text-right">
          <button class="uk-icon-button uk-margin-small-right" uk-icon="move"></button>
          <button class="uk-icon-button" uk-icon="close"></button>
        </div>
      </div>
    </li>
  </ul>
</template>

<script lang="ts">
import {defineComponent} from 'vue';

export default defineComponent({});
</script>
