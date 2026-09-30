const COVER_FRAME_COUNT = 6

export function getCoverFrameVariant(slug: string) {
  let hash = 2166136261
  for (let index = 0; index < slug.length; index += 1) {
    hash ^= slug.charCodeAt(index)
    hash = Math.imul(hash, 16777619)
  }
  return (hash >>> 0) % COVER_FRAME_COUNT
}
