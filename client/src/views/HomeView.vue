<template>
  <div class="home">
    <ul class="uk-list">
      <li v-for="clip in $store.state.clips">
        {{ clip }}
      </li>
    </ul>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
});
</script>
