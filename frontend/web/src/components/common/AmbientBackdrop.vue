<template>
  <div class="ambient-backdrop" :class="`ambient-backdrop--${variant}`" aria-hidden="true">
    <div class="ambient-backdrop__wash" />
    <div class="ambient-backdrop__aurora" />
    <div class="ambient-backdrop__beam ambient-backdrop__beam--left" />
    <div class="ambient-backdrop__beam ambient-backdrop__beam--right" />
    <div class="ambient-backdrop__orb ambient-backdrop__orb--one" />
    <div class="ambient-backdrop__orb ambient-backdrop__orb--two" />
    <div class="ambient-backdrop__orb ambient-backdrop__orb--three" />
    <div class="ambient-backdrop__orb ambient-backdrop__orb--four" />
    <div class="ambient-backdrop__ring ambient-backdrop__ring--one" />
    <div class="ambient-backdrop__ring ambient-backdrop__ring--two" />
    <div class="ambient-backdrop__mesh" />
    <div class="ambient-backdrop__grid" />
  </div>
</template>

<script setup>
defineProps({
  variant: {
    type: String,
    default: 'public'
  }
})
</script>

<style scoped>
.ambient-backdrop {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
  z-index: 0;
}

.ambient-backdrop__wash,
.ambient-backdrop__aurora,
.ambient-backdrop__grid,
.ambient-backdrop__mesh,
.ambient-backdrop__beam,
.ambient-backdrop__ring,
.ambient-backdrop__orb {
  position: absolute;
}

.ambient-backdrop__wash {
  inset: 0;
  background:
    radial-gradient(circle at 12% 16%, rgba(0, 113, 227, 0.12), transparent 28%),
    radial-gradient(circle at 86% 18%, rgba(41, 151, 255, 0.08), transparent 24%),
    radial-gradient(circle at 72% 82%, rgba(191, 219, 254, 0.22), transparent 26%);
  animation: washShift 24s ease-in-out infinite alternate;
}

.ambient-backdrop__aurora {
  inset: -18%;
  background:
    radial-gradient(circle at 22% 32%, rgba(255, 255, 255, 0.56), transparent 24%),
    radial-gradient(circle at 76% 28%, rgba(0, 113, 227, 0.07), transparent 20%),
    radial-gradient(circle at 62% 78%, rgba(226, 232, 240, 0.34), transparent 24%);
  filter: blur(42px);
  opacity: 0.9;
  animation: auroraDrift 28s ease-in-out infinite;
}

.ambient-backdrop__beam {
  width: 40vw;
  min-width: 280px;
  max-width: 520px;
  height: 110vh;
  top: -10vh;
  border-radius: 999px;
  filter: blur(34px);
  opacity: 0.28;
  mix-blend-mode: screen;
}

.ambient-backdrop__beam--left {
  left: -12vw;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.52), rgba(0, 113, 227, 0.08));
  transform: rotate(-18deg);
  animation: beamSweepLeft 26s ease-in-out infinite;
}

.ambient-backdrop__beam--right {
  right: -14vw;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.18), rgba(148, 163, 184, 0.18));
  transform: rotate(16deg);
  animation: beamSweepRight 30s ease-in-out infinite;
}

.ambient-backdrop__grid {
  inset: 0;
  background-image: linear-gradient(rgba(255, 255, 255, 0.16) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.16) 1px, transparent 1px);
  background-size: 44px 44px;
  mask-image: linear-gradient(180deg, rgba(0, 0, 0, 0.18), transparent 85%);
  opacity: 0.24;
  animation: gridFloat 34s linear infinite;
}

.ambient-backdrop__mesh {
  inset: 0;
  background:
    radial-gradient(circle at center, rgba(255, 255, 255, 0.16) 0 1px, transparent 1px 100%);
  background-size: 26px 26px;
  mask-image: radial-gradient(circle at center, rgba(0, 0, 0, 0.28), transparent 78%);
  opacity: 0.16;
  animation: meshPulse 22s ease-in-out infinite;
}

.ambient-backdrop__orb {
  border-radius: 999px;
  filter: blur(28px);
  animation: floatOrb 18s ease-in-out infinite;
}

.ambient-backdrop__orb--one {
  top: 72px;
  left: -48px;
  width: 220px;
  height: 220px;
  background: rgba(0, 113, 227, 0.18);
}

.ambient-backdrop__orb--two {
  top: 24%;
  right: -52px;
  width: 260px;
  height: 260px;
  background: rgba(255, 255, 255, 0.44);
  animation-delay: -6s;
}

.ambient-backdrop__orb--three {
  bottom: 8%;
  left: 38%;
  width: 200px;
  height: 200px;
  background: rgba(125, 195, 255, 0.12);
  animation-delay: -12s;
}

.ambient-backdrop__orb--four {
  bottom: -40px;
  right: 18%;
  width: 180px;
  height: 180px;
  background: rgba(255, 255, 255, 0.3);
  animation-delay: -9s;
}

.ambient-backdrop__ring {
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.24);
  background: radial-gradient(circle at center, rgba(255, 255, 255, 0.2), transparent 70%);
  filter: blur(0.2px);
  opacity: 0.28;
}

.ambient-backdrop__ring--one {
  top: 18%;
  left: 52%;
  width: 320px;
  height: 320px;
  animation: ringPulse 18s ease-in-out infinite;
}

.ambient-backdrop__ring--two {
  bottom: 12%;
  left: 10%;
  width: 240px;
  height: 240px;
  animation: ringPulse 22s ease-in-out infinite reverse;
}

.ambient-backdrop--auth .ambient-backdrop__wash {
  background:
    radial-gradient(circle at 16% 18%, rgba(0, 113, 227, 0.14), transparent 26%),
    radial-gradient(circle at 84% 20%, rgba(255, 255, 255, 0.58), transparent 24%),
    radial-gradient(circle at 76% 88%, rgba(125, 195, 255, 0.12), transparent 24%);
}

.ambient-backdrop--console .ambient-backdrop__wash {
  background:
    radial-gradient(circle at 8% 14%, rgba(0, 113, 227, 0.1), transparent 24%),
    radial-gradient(circle at 92% 12%, rgba(255, 255, 255, 0.34), transparent 22%),
    radial-gradient(circle at 82% 78%, rgba(148, 163, 184, 0.08), transparent 24%);
}

@keyframes floatOrb {
  0%,
  100% {
    transform: translate3d(0, 0, 0) scale(1);
  }

  50% {
    transform: translate3d(18px, -14px, 0) scale(1.06);
  }
}

@keyframes washShift {
  0% {
    transform: translate3d(0, 0, 0) scale(1);
  }

  100% {
    transform: translate3d(0, -1.2%, 0) scale(1.03);
  }
}

@keyframes auroraDrift {
  0%,
  100% {
    transform: translate3d(-1%, 0, 0) rotate(0deg);
  }

  50% {
    transform: translate3d(2%, -2%, 0) rotate(6deg);
  }
}

@keyframes beamSweepLeft {
  0%,
  100% {
    transform: rotate(-18deg) translate3d(0, 0, 0);
  }

  50% {
    transform: rotate(-12deg) translate3d(30px, -24px, 0);
  }
}

@keyframes beamSweepRight {
  0%,
  100% {
    transform: rotate(16deg) translate3d(0, 0, 0);
  }

  50% {
    transform: rotate(10deg) translate3d(-26px, 18px, 0);
  }
}

@keyframes ringPulse {
  0%,
  100% {
    transform: scale(1);
    opacity: 0.18;
  }

  50% {
    transform: scale(1.08);
    opacity: 0.32;
  }
}

@keyframes gridFloat {
  0% {
    transform: translate3d(0, 0, 0);
  }

  100% {
    transform: translate3d(-18px, 12px, 0);
  }
}

@keyframes meshPulse {
  0%,
  100% {
    opacity: 0.12;
  }

  50% {
    opacity: 0.2;
  }
}

@media (prefers-reduced-motion: reduce) {
  .ambient-backdrop__wash,
  .ambient-backdrop__aurora,
  .ambient-backdrop__beam,
  .ambient-backdrop__orb,
  .ambient-backdrop__ring,
  .ambient-backdrop__grid,
  .ambient-backdrop__mesh {
    animation: none;
  }
}
</style>
