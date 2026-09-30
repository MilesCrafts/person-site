<template>
  <footer id="site-footer" class="site-footer container">
    <div class="footer-color-strip" aria-hidden="true"><i></i><i></i><i></i><i></i></div>
    <div class="footer-main">
      <section class="footer-intro">
        <RouterLink class="footer-signature" to="/" aria-label="返回 LEKANG 首页">Lekang</RouterLink>
        <p class="footer-overline">PRODUCT · CODE · NOTES</p>
        <h2>做点东西，<br><span>也把过程留下。</span></h2>
      </section>

      <nav aria-label="页脚导航">
        <p>EXPLORE / 随便逛逛</p>
        <RouterLink to="/works"><span>01</span><strong>作品</strong><i>↗</i></RouterLink>
        <RouterLink to="/capabilities"><span>02</span><strong>能力</strong><i>↗</i></RouterLink>
        <RouterLink to="/journal"><span>03</span><strong>文章</strong><i>↗</i></RouterLink>
        <RouterLink to="/archive"><span>04</span><strong>归档</strong><i>↗</i></RouterLink>
      </nav>

      <aside class="footer-note">
        <span class="footer-note-blocks" aria-hidden="true"><i></i><i></i><i></i><i></i></span>
        <p><i></i>SITE / 持续更新</p>
        <strong>这里没有最终版本，<br>只有下一次修改。</strong>
        <RouterLink to="/about">关于我 <span>→</span></RouterLink>
      </aside>
    </div>
    <div class="footer-bottom">
      <p>© 2026 LEKANG</p>
      <span class="footer-palette" aria-hidden="true"><i></i><i></i><i></i><i></i></span>
      <p>持续实践，也持续记录。</p>
    </div>
  </footer>
</template>
