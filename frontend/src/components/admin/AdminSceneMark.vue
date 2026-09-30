<script setup lang="ts">
defineProps<{ variant: 'overview' | 'library' | 'editor' }>()
</script>

<template>
  <div class="scene-mark" :data-variant="variant" aria-hidden="true">
    <span class="antenna" /><span class="head"><i /><i /></span><span class="body" />
    <span class="arm left" /><span class="arm right" />
    <span class="prop">{{ variant === 'overview' ? 'HI' : variant === 'library' ? '///' : '✎' }}</span>
  </div>
</template>

<style scoped lang="scss">
@use '../../assets/styles/variables' as *;
.scene-mark{position:relative;width:66px;height:62px;flex:0 0 66px}.head{position:absolute;z-index:2;top:10px;left:20px;width:34px;height:25px;border:2px solid $text-primary;border-radius:11px;background:#fff;transform:rotate(-2deg)}.head i{position:absolute;top:9px;width:4px;height:5px;border-radius:50%;background:$accent}.head i:first-child{left:9px}.head i:last-child{right:9px}.antenna{position:absolute;top:2px;left:37px;width:2px;height:10px;background:$text-primary;transform:rotate(10deg)}.antenna:after{position:absolute;top:-3px;left:-2px;width:6px;height:6px;border-radius:50%;background:$yellow;content:''}.body{position:absolute;z-index:1;top:34px;left:25px;width:28px;height:22px;border:2px solid $text-primary;border-radius:8px 8px 11px 11px;background:$accent-subtle}.arm{position:absolute;z-index:0;top:37px;width:19px;height:7px;border:2px solid $text-primary;border-radius:999px;background:#fff}.arm.left{left:9px;transform:rotate(-28deg)}.arm.right{right:1px;transform:rotate(28deg)}.prop{position:absolute;z-index:3;right:-2px;bottom:0;display:grid;width:25px;height:29px;place-items:center;border:1px solid $text-primary;border-radius:4px;color:$text-primary;background:$yellow;font-size:7px;font-weight:800;transform:rotate(5deg)}
.scene-mark[data-variant='overview'] .arm.right{top:24px;right:3px;transform:rotate(-55deg)}.scene-mark[data-variant='overview'] .prop{display:none}.scene-mark[data-variant='library'] .prop{right:4px;width:34px;background:#fff;box-shadow:-4px 4px 0 $accent-subtle}.scene-mark[data-variant='library'] .head{transform:rotate(3deg)}.scene-mark[data-variant='editor'] .prop{right:0;width:9px;height:35px;border-radius:999px;background:$coral;font-size:11px;transform:rotate(36deg)}.scene-mark[data-variant='editor'] .arm.right{right:3px;transform:rotate(-25deg)}
</style>
