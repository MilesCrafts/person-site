import argentinaChampionsClose from '../assets/images/argentina-champions-close.jpg'
import argentinaChampionsWide from '../assets/images/argentina-champions-wide.jpg'
import argentinaImage from '../assets/images/journal/argentina.jpg'
import dunkirkImage from '../assets/images/journal/dunkirk.jpg'
import fragmentsImage from '../assets/images/journal/fragments.jpg'
import libraryImage from '../assets/images/journal/library.jpg'
import portraitImage from '../assets/images/journal/portrait.jpg'
import posterImage from '../assets/images/journal/poster.jpg'
import rainImage from '../assets/images/journal/rain.jpg'
import shamelessImage from '../assets/images/journal/shameless.jpg'
import shanghaiImage from '../assets/images/journal/shanghai.jpg'
import soldierImage from '../assets/images/journal/soldier.jpg'
import solitudeImage from '../assets/images/journal/solitude.jpg'
import tacticsImage from '../assets/images/journal/tactics.jpg'
import websiteImage from '../assets/images/journal/website.jpg'

export const editorialImages = {
  portrait: portraitImage,
  poster: posterImage,
  messiClose: argentinaChampionsClose
} as const

const localMediaByFileName: Record<string, string> = {
  'argentina-champions-wide.jpg': argentinaChampionsWide,
  'argentina.jpg': argentinaImage,
  'dunkirk.jpg': dunkirkImage,
  'fragments.jpg': fragmentsImage,
  'library.jpg': libraryImage,
  'rain.jpg': rainImage,
  'shameless.jpg': shamelessImage,
  'shanghai.jpg': shanghaiImage,
  'soldier.jpg': soldierImage,
  'solitude.jpg': solitudeImage,
  'tactics.jpg': tacticsImage,
  'website.jpg': websiteImage
}

export function resolveMediaUrl(url: string | null): string | null {
  if (!url) return null
  const fileName = url.split('/').pop()
  return fileName && localMediaByFileName[fileName] ? localMediaByFileName[fileName] : url
}

export function getFeatureSlides(slug: string, imageUrl: string | null): string[] {
  const cover = resolveMediaUrl(imageUrl)
  if (!cover) return []
  return slug === 'messi-world-cup' ? [cover, editorialImages.messiClose] : [cover]
}
