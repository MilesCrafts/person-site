<script setup lang="ts">
import type { Profile } from '../types/api'

defineProps<{ profile: Profile }>()
</script>

<template>
  <section class="home-about">
    <div class="home-about-label"><p>ABOUT</p><span>PERSONAL FILE · 001</span></div>
    <div class="home-about-signature">
      <p>THE EDITOR</p>
      <div class="signature-wrap"><h2>lekang</h2><span></span></div>
      <p class="signature-caption">EDITOR · DEVELOPER · OBSERVER</p>
    </div>
    <div class="home-about-profile">
      <p class="about-intro">{{ profile.bio }}</p>
      <p class="profile-lines">{{ profile.manifesto }}</p>
      <p class="about-location">SHANGHAI · 2026</p>
      <RouterLink to="/about">MORE ABOUT ME <span>→</span></RouterLink>
    </div>
  </section>
</template>
