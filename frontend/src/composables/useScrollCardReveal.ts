import { nextTick, onBeforeUnmount } from 'vue'

export function useScrollCardReveal() {
  let cleanup: (() => void) | undefined

  const reveal = async (scope?: ParentNode) => {
    cleanup?.()
    cleanup = undefined
    await nextTick()

    const targets = [...(scope ?? document).querySelectorAll<HTMLElement>('[data-scroll-card]')]
    if (!targets.length || window.matchMedia('(prefers-reduced-motion: reduce)').matches) return

    try {
      const [{ gsap }, { ScrollTrigger }] = await Promise.all([
        import('gsap'),
        import('gsap/ScrollTrigger')
      ])
      gsap.registerPlugin(ScrollTrigger)
      gsap.set(targets, { autoAlpha: 0, y: 34, scale: .985 })

      const animations = targets.map((target, index) => gsap.to(target, {
        autoAlpha: 1,
        y: 0,
        scale: 1,
        duration: .68,
        delay: (index % 3) * .045,
        ease: 'power3.out',
        scrollTrigger: {
          trigger: target,
          start: 'top 88%',
          once: true
        }
      }))

      ScrollTrigger.refresh()
      cleanup = () => {
        animations.forEach(animation => {
          animation.scrollTrigger?.kill()
          animation.kill()
        })
        gsap.set(targets, { clearProps: 'opacity,visibility,transform' })
      }
    } catch {
      targets.forEach(target => target.removeAttribute('style'))
    }
  }

  onBeforeUnmount(() => cleanup?.())
  return { reveal }
}
