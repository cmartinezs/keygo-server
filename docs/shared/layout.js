// KeyGo Server — Shared React layout components
// Requires: React, ReactDOM, i18n.js, nav.js, code-data.js, icons.js loaded first.
// Load this with <script type="text/babel" src="...layout.js"></script>

(function() {
  const { useState, useEffect, useCallback, createContext, useContext } = React;

  // ── Lang context ──────────────────────────────────────────────────
  const LangCtx = createContext({ lang: 'es', setLang: () => {} });
  window.KG_LangCtx = LangCtx;

  function useT() {
    const { lang } = useContext(LangCtx);
    return (key) => window.kgT(lang, key);
  }
  window.KG_useT = useT;

  // ── Icon component ────────────────────────────────────────────────
  function Icon({ name, size = 14, color = 'currentColor', style = {} }) {
    const paths = window.KG_ICONS && window.KG_ICONS[name];
    if (!paths) return null;
    return (
      <svg
        width={size} height={size} viewBox="0 0 24 24"
        fill="none" stroke={color} strokeWidth="1.75"
        strokeLinecap="round" strokeLinejoin="round"
        style={{ flexShrink: 0, ...style }}
        dangerouslySetInnerHTML={{ __html: paths }}
      />
    );
  }
  window.KG_Icon = Icon;

  // ── Lang switcher ─────────────────────────────────────────────────
  function LangSwitcher() {
    const { lang, setLang } = useContext(LangCtx);
    return (
      <div className="lang-switcher">
        {window.KG_LANGS.map(l => (
          <button key={l} className={'lang-btn' + (lang === l ? ' active' : '')} onClick={() => setLang(l)}>
            {l.toUpperCase()}
          </button>
        ))}
      </div>
    );
  }
  window.KG_LangSwitcher = LangSwitcher;

  // ── Topbar (sub-pages) ────────────────────────────────────────────
  function Topbar({ activeId }) {
    const t = useT();
    const item = window.KG_FLAT_NAV.find(n => n.id === activeId);
    return (
      <div className="topbar">
        <div className="topbar-left">
          <a className="topbar-crumb" href={window.KG_INDEX_HREF} style={{ display:'flex', alignItems:'center', gap:5 }}>
            <Icon name="key" size={13} color="var(--green)" />
            keygo-server
          </a>
          <span className="topbar-sep">›</span>
          <span className="topbar-crumb">docs</span>
          {item && <>
            <span className="topbar-sep">›</span>
            <span className="topbar-crumb active">{t(item.labelKey)}</span>
          </>}
        </div>
        <div className="topbar-right">
          <span className="status-dot">{t('topbar.status')}</span>
          <a className="topbar-link" href={window.KG_ROOT + 'integrators/endpoints.html'} style={{ display:'flex', alignItems:'center', gap:4 }}>
            <Icon name="endpoints" size={12} />
            {t('topbar.api')}
          </a>
          <a className="topbar-link" href={window.KG_ROOT + 'extenders/architecture.html'} style={{ display:'flex', alignItems:'center', gap:4 }}>
            <Icon name="architecture" size={12} />
            {t('topbar.arch')}
          </a>
          <LangSwitcher />
        </div>
      </div>
    );
  }
  window.KG_Topbar = Topbar;

  // ── Index topbar ──────────────────────────────────────────────────
  function IndexTopbar() {
    const t = useT();
    return (
      <div className="topbar">
        <div className="topbar-left">
          <span style={{ color:'var(--text3)', fontFamily:'var(--font-mono)', fontSize:11, display:'flex', alignItems:'center', gap:5 }}>
            <Icon name="key" size={13} color="var(--green)" />
            keygo-server
          </span>
          <span className="topbar-sep">›</span>
          <span className="topbar-crumb active">docs</span>
        </div>
        <div className="topbar-right">
          <span className="status-dot">{t('topbar.status')}</span>
          <a className="topbar-link" href="integrators/endpoints.html" style={{ display:'flex', alignItems:'center', gap:4 }}>
            <Icon name="endpoints" size={12} />
            {t('topbar.api')}
          </a>
          <a className="topbar-link" href="extenders/architecture.html" style={{ display:'flex', alignItems:'center', gap:4 }}>
            <Icon name="architecture" size={12} />
            {t('topbar.arch')}
          </a>
          <LangSwitcher />
        </div>
      </div>
    );
  }
  window.KG_IndexTopbar = IndexTopbar;

  // ── Sidebar (sub-pages) ───────────────────────────────────────────
  function Sidebar({ activeId }) {
    const t = useT();
    const [openFolders, setOpenFolders] = useState(() => ({ datamodel: String(activeId || '').startsWith('datamodel') }));
    let lastGroup = null;

    function isItemActive(item) {
      if (activeId === item.id) return true;
      return !!(item.children && item.children.some(isItemActive));
    }

    function toggleFolder(id) {
      setOpenFolders((current) => ({ ...current, [id]: !current[id] }));
    }

    function renderItems(items, nested = false) {
      return items.map((item, i) => {
        if (item.divider && !nested) return <div key={'d'+i} className="nav-divider" />;
        if (item.divider) return null;
        const showLabel = !nested && item.group && item.group !== lastGroup;
        if (!nested && item.group) lastGroup = item.group;
        const groupKey = item.group === 'integrators' ? 'nav.group.integrators' : 'nav.group.extenders';
        const iconName = window.KG_NAV_ICONS && window.KG_NAV_ICONS[item.id];
        const isActive = activeId === item.id;
        const hasActiveChild = !!(item.children && item.children.some(isItemActive));
        const isOpen = item.children ? !!openFolders[item.id] : false;
        return (
          <React.Fragment key={item.id}>
            {showLabel && <div className="nav-label">{t(groupKey)}</div>}
            {item.children ? (
              <div className={'nav-folder' + ((isActive || hasActiveChild) ? ' active' : '')}>
                <div className={'nav-item nav-folder-row' + ((isActive || hasActiveChild) ? ' active' : '')}>
                  <a href={item.href} className="nav-folder-link">
                    {iconName
                      ? <Icon name={iconName} size={13} color={(isActive || hasActiveChild) ? 'var(--green)' : 'var(--text3)'} />
                      : <span className="nav-dot" />
                    }
                    <span>{t(item.labelKey)}</span>
                  </a>
                  <button
                    type="button"
                    className={'nav-folder-toggle' + (isOpen ? ' open' : '')}
                    onClick={() => toggleFolder(item.id)}
                    aria-label={isOpen ? 'Collapse' : 'Expand'}
                  >
                    <Icon name="arrow-right" size={11} color={(isActive || hasActiveChild) ? 'var(--green)' : 'var(--text3)'} />
                  </button>
                </div>
                {isOpen && <div className="nav-children">{renderItems(item.children, true)}</div>}
              </div>
            ) : (
              <a href={item.href} className={'nav-item' + (isActive ? ' active' : '') + (nested ? ' nested' : '')}>
                {iconName
                  ? <Icon name={iconName} size={13} color={isActive ? 'var(--green)' : 'var(--text3)'} />
                  : <span className="nav-dot" />
                }
                {t(item.labelKey)}
              </a>
            )}
          </React.Fragment>
        );
      });
    }

    return (
      <aside className="sidebar">
        <div className="sidebar-logo">
          <div className="logo-mark">
            <a className="logo-key" href={window.KG_INDEX_HREF}>KeyGo</a>
          </div>
          <div className="logo-server">Server · Backend</div>
          <div className="badge-version">v1.0.0-beta</div>
        </div>
        <nav style={{ paddingTop: 8 }}>
          {renderItems(window.KG_NAV)}
        </nav>
      </aside>
    );
  }
  window.KG_Sidebar = Sidebar;

  // ── Index sidebar ─────────────────────────────────────────────────
  function IndexSidebar() {
    const t = useT();
    const [openFolders, setOpenFolders] = useState({ datamodel: false });
    let lastGroup = null;

    function toggleFolder(id) {
      setOpenFolders((current) => ({ ...current, [id]: !current[id] }));
    }

    function renderItems(items, nested = false) {
      return items.map((item, i) => {
        if (item.divider && !nested) return <div key={'d'+i} className="nav-divider" />;
        if (item.divider) return null;
        const showLabel = !nested && item.group && item.group !== lastGroup;
        if (!nested && item.group) lastGroup = item.group;
        const groupKey = item.group === 'integrators' ? 'nav.group.integrators' : 'nav.group.extenders';
        const href = item.href.replace(window.KG_ROOT, '');
        const iconName = window.KG_NAV_ICONS && window.KG_NAV_ICONS[item.id];
        const isOpen = item.children ? !!openFolders[item.id] : false;
        return (
          <React.Fragment key={item.id}>
            {showLabel && <div className="nav-label">{t(groupKey)}</div>}
            {item.children ? (
              <div className="nav-folder">
                <div className="nav-item nav-folder-row">
                  <a href={href} className="nav-folder-link">
                    {iconName
                      ? <Icon name={iconName} size={13} color="var(--text3)" />
                      : <span className="nav-dot" />
                    }
                    <span>{t(item.labelKey)}</span>
                  </a>
                  <button
                    type="button"
                    className={'nav-folder-toggle' + (isOpen ? ' open' : '')}
                    onClick={() => toggleFolder(item.id)}
                    aria-label={isOpen ? 'Collapse' : 'Expand'}
                  >
                    <Icon name="arrow-right" size={11} color="var(--text3)" />
                  </button>
                </div>
                {isOpen && <div className="nav-children">{renderItems(item.children, true)}</div>}
              </div>
            ) : (
              <a href={href} className={'nav-item' + (nested ? ' nested' : '')}>
                {iconName
                  ? <Icon name={iconName} size={13} color="var(--text3)" />
                  : <span className="nav-dot" />
                }
                {t(item.labelKey)}
              </a>
            )}
          </React.Fragment>
        );
      });
    }

    return (
      <aside className="sidebar">
        <div className="sidebar-logo">
          <div className="logo-mark">
            <a className="logo-key" href="index.html">KeyGo</a>
          </div>
          <div className="logo-server">Server · Backend</div>
          <div className="badge-version">v1.0.0-beta</div>
        </div>
        <nav style={{ paddingTop: 8 }}>
          {renderItems(window.KG_NAV)}
        </nav>
      </aside>
    );
  }
  window.KG_IndexSidebar = IndexSidebar;

  // ── Prev / Next nav ───────────────────────────────────────────────
  function PageNav({ activeId }) {
    const t = useT();
    const order = window.KG_PAGE_ORDER;
    const idx = order.indexOf(activeId);
    const prevId = idx > 0 ? order[idx - 1] : null;
    const nextId = idx < order.length - 1 ? order[idx + 1] : null;
    const getItem = (id) => window.KG_FLAT_NAV.find(n => n.id === id);
    const prevItem = prevId ? getItem(prevId) : null;
    const nextItem = nextId ? getItem(nextId) : null;
    if (!prevItem && !nextItem) return null;
    return (
      <div className="page-nav">
        {prevItem
          ? <a className="page-nav-btn" href={prevItem.href}>
              <span className="page-nav-label" style={{ display:'flex', alignItems:'center', gap:4 }}>
                <Icon name="arrow-left" size={11} color="var(--text3)" />{t('prev')}
              </span>
              <span className="page-nav-title">{t(prevItem.labelKey)}</span>
            </a>
          : <div />
        }
        {nextItem
          ? <a className="page-nav-btn next" href={nextItem.href}>
              <span className="page-nav-label" style={{ display:'flex', alignItems:'center', gap:4, justifyContent:'flex-end' }}>
                {t('next')}<Icon name="arrow-right" size={11} color="var(--text3)" />
              </span>
              <span className="page-nav-title">{t(nextItem.labelKey)}</span>
            </a>
          : <div />
        }
      </div>
    );
  }
  window.KG_PageNav = PageNav;

  // ── Footer ────────────────────────────────────────────────────────
  function PageFooter() {
    return (
      <div className="page-footer">
        <span className="footer-text">KeyGo Server · v1.0.0-beta · Java 21 · Spring Boot 4.x</span>
        <span className="footer-text">OpenAPI: <code>/api/v1/openapi.yaml</code></span>
      </div>
    );
  }
  window.KG_PageFooter = PageFooter;

  // ── Code block ────────────────────────────────────────────────────
  function CodeBlock({ id }) {
    const { lang } = useContext(LangCtx);
    const [copied, setCopied] = useState(false);
    const d = window.KG_CODE[id];
    if (!d) return null;
    const html = (d.html[lang] || d.html['en'] || '');
    const copy = () => {
      const tmp = document.createElement('div');
      tmp.innerHTML = html;
      navigator.clipboard.writeText(tmp.textContent || '').then(() => {
        setCopied(true);
        setTimeout(() => setCopied(false), 1800);
      });
    };
    return (
      <div className="code-block">
        <div className="code-header">
          <span className="code-lang">{d.lang}</span>
          <button className={'code-copy' + (copied ? ' copied' : '')} onClick={copy}>
            {copied ? '✓' : 'copy'}
          </button>
        </div>
        <div className="code-body" dangerouslySetInnerHTML={{ __html: html }} />
      </div>
    );
  }
  window.KG_CodeBlock = CodeBlock;

  // ── Callout ───────────────────────────────────────────────────────
  function Callout({ type, children }) {
    const iconName = type === 'info' ? 'info' : type === 'warn' ? 'warn' : 'success';
    const iconColor = type === 'info' ? 'var(--blue)' : type === 'warn' ? 'var(--amber)' : 'var(--green)';
    return (
      <div className={'callout ' + type}>
        <span className="callout-icon" style={{ display:'flex', alignItems:'center' }}>
          <Icon name={iconName} size={15} color={iconColor} />
        </span>
        <span>{children}</span>
      </div>
    );
  }
  window.KG_Callout = Callout;

  // ── Path display ──────────────────────────────────────────────────
  function PathDisplay({ path }) {
    return (
      <span className="endpoint-path">
        {path.split(/(\{[^}]+\})/).map((p, i) =>
          /^\{/.test(p)
            ? <span key={i} className="path-param">{p}</span>
            : <span key={i}>{p}</span>
        )}
      </span>
    );
  }
  window.KG_PathDisplay = PathDisplay;

  function HttpMethodBadge({ method }) {
    const upper = method.toUpperCase();
    const iconName = window.KG_HTTP_METHOD_ICONS && window.KG_HTTP_METHOD_ICONS[upper];
    return (
      <span className={'method ' + upper}>
        {iconName && <Icon name={iconName} size={10} />}
        {upper}
      </span>
    );
  }
  window.KG_HttpMethodBadge = HttpMethodBadge;

  // ── Mermaid loader / renderer ──────────────────────────────────────
  let mermaidPromise = null;
  let mermaidInitialized = false;

  window.KG_loadMermaid = function() {
    if (window.mermaid) return Promise.resolve(window.mermaid);
    if (mermaidPromise) return mermaidPromise;
    mermaidPromise = new Promise((resolve, reject) => {
      const script = document.createElement('script');
      script.src = 'https://cdn.jsdelivr.net/npm/mermaid@11/dist/mermaid.min.js';
      script.async = true;
      script.onload = () => resolve(window.mermaid);
      script.onerror = reject;
      document.head.appendChild(script);
    });
    return mermaidPromise;
  };

  function waitForDiagramFonts() {
    if (!document.fonts || !document.fonts.ready) return Promise.resolve();
    return document.fonts.ready.catch(() => {});
  }

  function waitForLayoutFrames() {
    return new Promise((resolve) => {
      requestAnimationFrame(() => requestAnimationFrame(resolve));
    });
  }

  function MermaidDiagram({ id, definition }) {
    const ref = React.useRef(null);
    React.useEffect(() => {
      let cancelled = false;
      Promise.all([window.KG_loadMermaid(), waitForDiagramFonts(), waitForLayoutFrames()]).then(([mermaid]) => {
        if (cancelled || !ref.current || !mermaid) return;
        if (!mermaidInitialized) {
          mermaid.initialize({
            startOnLoad: false,
            theme: 'dark',
            themeVariables: {
              background: '#181c26',
              mainBkg: '#12151c',
              nodeBorder: '#2d3547',
              lineColor: '#535e7a',
              textColor: '#d4d8e8',
              edgeLabelBackground: '#181c26',
              fontSize: '13px',
              fontFamily: 'IBM Plex Sans, sans-serif',
              clusterBkg: '#12151c',
              clusterBorder: '#2d3547',
            },
            flowchart: { curve: 'basis', padding: 20 },
            er: { useMaxWidth: true },
          });
          mermaidInitialized = true;
        }
        ref.current.innerHTML = '';
        mermaid.render(id, definition).then(({ svg }) => {
          if (!cancelled && ref.current) ref.current.innerHTML = svg;
        });
      }).catch((error) => {
        if (!cancelled && ref.current) {
          ref.current.textContent = 'Mermaid render error';
          console.warn('Mermaid error:', error);
        }
      });
      return () => { cancelled = true; };
    }, [id, definition]);
    return <div className="mermaid-wrap" ref={ref} />;
  }
  window.KG_MermaidDiagram = MermaidDiagram;

  // ── Mount sub-page ────────────────────────────────────────────────
  window.KG_mountPage = function({ activeId, content: Content }) {
    function Shell() {
      const [lang, setLangState] = useState(window.kgDetectLang());
      const setLang = useCallback((l) => { setLangState(l); window.kgSetLang(l); }, []);
      return (
        <LangCtx.Provider value={{ lang, setLang }}>
          <KG_Sidebar activeId={activeId} />
          <div className="main">
            <KG_Topbar activeId={activeId} />
            <Content />
            <KG_PageNav activeId={activeId} />
            <KG_PageFooter />
          </div>
        </LangCtx.Provider>
      );
    }
    ReactDOM.createRoot(document.getElementById('root')).render(<Shell />);
  };

  // ── Mount index ───────────────────────────────────────────────────
  window.KG_mountIndex = function({ content: Content }) {
    function Shell() {
      const [lang, setLangState] = useState(window.kgDetectLang());
      const setLang = useCallback((l) => { setLangState(l); window.kgSetLang(l); }, []);
      return (
        <LangCtx.Provider value={{ lang, setLang }}>
          <KG_IndexSidebar />
          <div className="main">
            <KG_IndexTopbar />
            <Content />
            <KG_PageFooter />
          </div>
        </LangCtx.Provider>
      );
    }
    ReactDOM.createRoot(document.getElementById('root')).render(<Shell />);
  };
})();
