<template>
  <section class="home-page page-intro w-full pb-8 pt-6 md:pt-9">
    <div class="home-hero-panel relative mx-auto w-full max-w-[1160px] overflow-hidden rounded-[34px] border border-sense-teal/10 bg-white shadow-soft dark:border-white/10 dark:bg-[#132630]">
      <div class="pointer-events-none absolute inset-y-0 left-0 w-[56%] bg-[radial-gradient(circle_at_left_center,rgba(242,234,220,0.58),transparent_58%)]" />
      <div class="pointer-events-none absolute inset-y-0 right-0 w-[44%] bg-[radial-gradient(circle_at_center,rgba(223,241,234,0.7),transparent_68%)] dark:bg-[radial-gradient(circle_at_center,rgba(66,113,117,0.22),transparent_68%)]" />

      <div class="relative grid min-h-[500px] gap-0 lg:grid-cols-[minmax(0,1fr)_minmax(420px,0.9fr)]">
        <div class="flex items-center px-8 py-10 sm:px-10 md:px-12 lg:px-14 lg:py-12">
          <div class="max-w-[430px] space-y-5">
            <h1 class="home-title text-[3.55rem] font-semibold leading-[0.94] tracking-[-0.055em] text-[#2d2f33] dark:text-white sm:text-[4.85rem]">
              AmitySpace
            </h1>

            <p class="home-lead max-w-[340px] text-[1.34rem] font-medium leading-[1.4] text-[#373b40] dark:text-slate-100">
              We help you find public spaces that feel calmer, safer, and easier to enjoy.
            </p>

            <p class="home-copy max-w-[380px] text-[1.02rem] leading-[1.72] text-[#666b70] dark:text-slate-100">
              Not sure where to go, or when it might feel less overwhelming? Looking for a place that feels more manageable for you or someone you care about? We help you plan with more confidence, so getting out and staying connected feels a little easier.
            </p>

            <RouterLink
              to="/map"
              class="inline-flex rounded-[9px] bg-[#709f9a] px-7 py-[13px] text-[14px] font-semibold text-white transition hover:bg-sense-teal dark:bg-[#709f9a] dark:text-sense-night dark:hover:bg-sense-mint"
            >
              Get Started
            </RouterLink>
          </div>
        </div>

        <div class="flex items-end justify-center border-t border-sense-teal/8 px-5 pb-5 pt-0 lg:border-l lg:border-t-0 lg:px-0 lg:pb-0">
          <div class="home-hero-image-shell h-full w-full max-w-[560px] overflow-hidden rounded-[18px] border border-sense-teal/8 lg:max-w-none lg:rounded-none lg:border-0">
            <img
              src="/landing-hero-reference.jpg"
              alt="Two people sitting and talking in a calm cafe setting."
              class="home-hero-image h-full w-full object-cover object-center"
              loading="eager"
            />
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router';
</script>
