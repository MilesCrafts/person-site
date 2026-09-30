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

export type Category = 'FOOTBALL' | 'CINEMA' | 'BOOKS' | 'NOTES'
export type JournalSection = 'ALL' | Category | 'ALBUM'
export type Topic = 'MESSI' | 'TACTICS' | 'ARGENTINA' | 'WAR FILM' | 'TV SERIES' | 'FILM NOTES' | 'LATIN AMERICA' | 'BORGES' | 'SOLITUDE' | 'SHANGHAI' | 'BUILDING' | 'FRAGMENTS' | 'CITY' | 'DAILY LIFE' | 'FOOTBALL' | 'CINEMA'

export interface ArticleSection {
  heading?: string
  paragraphs: string[]
  quote?: string
  list?: string[]
}

export interface Article {
  id: string
  category: Category
  topic: Topic
  title: string
  excerpt: string
  date: string
  readTime: string
  image: string
  sections: ArticleSection[]
}

// 图片作为构建资源随站点发布，避免依赖第三方图床的可用性。
export const images = {
  messi: argentinaChampionsWide,
  messiClose: argentinaChampionsClose,
  tactics: tacticsImage,
  argentina: argentinaImage,
  dunkirk: dunkirkImage,
  soldier: soldierImage,
  shameless: shamelessImage,
  rain: rainImage,
  library: libraryImage,
  solitude: solitudeImage,
  shanghai: shanghaiImage,
  website: websiteImage,
  fragments: fragmentsImage,
  portrait: portraitImage,
  poster: posterImage
} as const

export const heroImages = [images.messi, images.messiClose] as const

export const topicMap: Record<Category, readonly Topic[]> = {
  FOOTBALL: ['MESSI', 'TACTICS', 'ARGENTINA'],
  CINEMA: ['WAR FILM', 'TV SERIES', 'FILM NOTES'],
  BOOKS: ['LATIN AMERICA', 'BORGES', 'SOLITUDE'],
  NOTES: ['SHANGHAI', 'BUILDING', 'FRAGMENTS']
}

export const albumTopics: readonly Topic[] = ['CITY', 'DAILY LIFE', 'FOOTBALL', 'CINEMA']

const defaultSections: ArticleSection[] = [
  {
    heading: '记忆如何成为故事',
    paragraphs: [
      '我们记住的从来不只是结果。更长久地停留在脑海中的，是事情发生时的光线、身边人的神情，以及那一刻我们相信的东西。',
      '重新讲述一段往事，并不是为了给它盖棺定论，而是试着理解：它为何在许多年之后，依旧能够改变我们观看世界的方式。'
    ],
    quote: '真正重要的故事，往往在结束之后才开始生长。'
  },
  {
    heading: '时间的另一面',
    paragraphs: [
      '细节让宏大的叙事重新变得具体。一个动作、一句没有说完的话、一段沉默，都可能比结论更接近真实。',
      '写作因此成为一种缓慢的观看。我们不急着抵达答案，而是在过程中保留复杂、犹疑与那些尚未命名的感受。'
    ],
    list: ['观看，而不是急于判断', '记录具体的人与瞬间', '允许一个故事保留开放的结尾']
  }
]

const create = (article: Omit<Article, 'sections'>): Article => ({ ...article, sections: defaultSections })

export const articles: Article[] = [
  create({ id: 'messi-world-cup', category: 'FOOTBALL', topic: 'MESSI', title: '2022，梅西终于捧起世界杯', excerpt: '从罗萨里奥到卢赛尔，一段持续近二十年的等待，终于在卡塔尔的夜晚迎来了结局。', date: '2026.07.19', readTime: '8 MIN READ', image: images.messi }),
  create({ id: 'parking-the-bus', category: 'FOOTBALL', topic: 'TACTICS', title: '什么是足球比赛中的摆大巴', excerpt: '防守并非消极的同义词。理解空间、耐心与风险，才看得见低位防守的全部。', date: '2026.07.15', readTime: '6 MIN READ', image: images.tactics }),
  create({ id: 'argentina-football', category: 'FOOTBALL', topic: 'ARGENTINA', title: '阿根廷足球为什么如此迷人', excerpt: '街头、探戈、天才与悲剧，共同塑造了一种难以复制的足球气质。', date: '2026.07.08', readTime: '7 MIN READ', image: images.argentina }),
  create({ id: 'dunkirk-myth', category: 'CINEMA', topic: 'WAR FILM', title: '敦刻尔克：撤退如何成为神话', excerpt: '诺兰没有拍摄一场传统胜利，而是让时间、海洋与沉默成为战争的主角。', date: '2026.07.17', readTime: '9 MIN READ', image: images.dunkirk }),
  create({ id: 'saving-private-ryan', category: 'CINEMA', topic: 'WAR FILM', title: '拯救大兵瑞恩中的士兵与武器', excerpt: '穿过奥马哈海滩的噪声，重新审视一部战争电影里的身体、器械与伦理。', date: '2026.07.11', readTime: '10 MIN READ', image: images.soldier }),
  create({ id: 'shameless-family', category: 'CINEMA', topic: 'TV SERIES', title: '无耻之徒：混乱家庭里的真实生活', excerpt: '它粗粝、喧闹而不体面，却比许多精致故事更接近生活的纹理。', date: '2026.07.04', readTime: '7 MIN READ', image: images.shameless }),
  create({ id: 'macondo-rain', category: 'BOOKS', topic: 'LATIN AMERICA', title: '马孔多为什么一直在下雨', excerpt: '雨在马孔多不是天气，而是时间、遗忘与孤独共同写下的一种语言。', date: '2026.07.13', readTime: '8 MIN READ', image: images.rain }),
  create({ id: 'borges-library', category: 'BOOKS', topic: 'BORGES', title: '博尔赫斯与无限图书馆', excerpt: '当所有书都已存在，人还要如何阅读、选择，并为意义负责？', date: '2026.06.28', readTime: '6 MIN READ', image: images.library }),
  create({ id: 'one-hundred-years', category: 'BOOKS', topic: 'SOLITUDE', title: '百年孤独中的孤独究竟是什么', excerpt: '布恩迪亚家族反复经历的，也许不是命运，而是无法彼此理解的漫长回声。', date: '2026.06.20', readTime: '9 MIN READ', image: images.solitude }),
  create({ id: 'shanghai-internship', category: 'NOTES', topic: 'SHANGHAI', title: '在上海实习的普通一天', excerpt: '早高峰、写字楼、便利店晚饭，以及一座城市在日常缝隙里露出的表情。', date: '2026.07.06', readTime: '5 MIN READ', image: images.shanghai }),
  create({ id: 'why-personal-site', category: 'NOTES', topic: 'BUILDING', title: '为什么我想做一个个人网站', excerpt: '在算法的时间线之外，给自己的文字留下一间可以慢慢整理的房间。', date: '2026.06.16', readTime: '4 MIN READ', image: images.website }),
  create({ id: 'recent-fragments', category: 'NOTES', topic: 'FRAGMENTS', title: '最近值得记住的一些片段', excerpt: '六月末的风、一场没有看完的电影，以及朋友随口说出的一句话。', date: '2026.06.08', readTime: '3 MIN READ', image: images.fragments })
]

export const categories: JournalSection[] = ['ALL', 'FOOTBALL', 'CINEMA', 'BOOKS', 'NOTES', 'ALBUM']
