// KeyGo Server — Shared layout helpers
// Detects the site root dynamically so links work regardless of server path prefix.

(function() {
  // Find the root of the keygo-docs-site by locating the last occurrence of it in the path.
  // Works whether served as /web/keygo-docs-site/ or /keygo-docs-site/ or any prefix.
  var path = window.location.pathname;
  var marker = '/keygo-docs-site/';
  var markerIdx = path.lastIndexOf(marker);
  var root;
  if (markerIdx !== -1) {
    root = path.slice(0, markerIdx + marker.length);
  } else {
    // Fallback: find root by walking up from current file to where index.html lives.
    // Count depth by segments after the last known folder names.
    var segs = path.split('/').filter(Boolean);
    // Remove filename (last segment if it ends in .html)
    if (segs.length && segs[segs.length-1].endsWith('.html')) segs.pop();
    // If we're inside integrators/ or extenders/, go up one more level
    var lastDir = segs[segs.length-1] || '';
    if (lastDir === 'integrators' || lastDir === 'extenders') segs.pop();
    root = '/' + segs.join('/') + '/';
  }
  window.KG_ROOT = root;

  function r(rel) { return root + rel; }

  window.KG_NAV = [
    { id: 'overview',     href: r('overview.html'),                 labelKey: 'nav.overview' },
    { id: 'quickstart',   href: r('quickstart.html'),               labelKey: 'nav.quickstart' },
    { divider: true },
    { id: 'auth',         href: r('integrators/auth.html'),         labelKey: 'nav.auth',        group: 'integrators' },
    { id: 'endpoints',    href: r('integrators/endpoints.html'),    labelKey: 'nav.endpoints',   group: 'integrators' },
    { id: 'apiconventions', href: r('integrators/api-conventions.html'), labelKey: 'nav.apiconventions', group: 'integrators' },
    { id: 'versioning',   href: r('integrators/versioning.html'),   labelKey: 'nav.versioning',  group: 'integrators' },
    { id: 'sessionmodel', href: r('integrators/session-model.html'),labelKey: 'nav.sessionmodel',group: 'integrators' },
    { id: 'tenantscope',  href: r('integrators/tenant-scope.html'), labelKey: 'nav.tenantscope', group: 'integrators' },
    { id: 'errors',       href: r('integrators/errors.html'),       labelKey: 'nav.errors',      group: 'integrators' },
    { divider: true },
    { id: 'architecture', href: r('extenders/architecture.html'),   labelKey: 'nav.architecture',group: 'extenders' },
    { id: 'modules',      href: r('extenders/modules.html'),        labelKey: 'nav.modules',     group: 'extenders' },
    { id: 'contexts',     href: r('extenders/contexts.html'),       labelKey: 'nav.contexts',    group: 'extenders' },
    { id: 'patterns',     href: r('extenders/patterns.html'),       labelKey: 'nav.patterns',    group: 'extenders' },
    {
      id: 'datamodel',
      href: r('extenders/data-model.html'),
      labelKey: 'nav.datamodel',
      group: 'extenders',
      children: [
        { id: 'datamodelidentity', href: r('extenders/data-model-identity.html'), labelKey: 'nav.datamodelidentity' },
        { id: 'datamodelplatform', href: r('extenders/data-model-platform-rbac.html'), labelKey: 'nav.datamodelplatform' },
        { id: 'datamodelorganization', href: r('extenders/data-model-organization.html'), labelKey: 'nav.datamodelorganization' },
        { id: 'datamodelclientapps', href: r('extenders/data-model-client-applications.html'), labelKey: 'nav.datamodelclientapps' },
        { id: 'datamodelaccess', href: r('extenders/data-model-access-control.html'), labelKey: 'nav.datamodelaccess' },
        { id: 'datamodelbilling', href: r('extenders/data-model-billing.html'), labelKey: 'nav.datamodelbilling' },
        { id: 'datamodelaudit', href: r('extenders/data-model-audit.html'), labelKey: 'nav.datamodelaudit' },
      ],
    },
    { id: 'validation',   href: r('extenders/validation.html'),     labelKey: 'nav.validation',  group: 'extenders' },
    { id: 'security',     href: r('extenders/security.html'),       labelKey: 'nav.security',    group: 'extenders' },
    { id: 'adrs',         href: r('extenders/adrs.html'),           labelKey: 'nav.adrs',        group: 'extenders' },
  ];

  function flattenNav(items) {
    var flat = [];
    items.forEach(function(item) {
      if (item.divider) return;
      flat.push(item);
      if (item.children) flat = flat.concat(flattenNav(item.children));
    });
    return flat;
  }

  window.KG_FLAT_NAV = flattenNav(window.KG_NAV);
  window.KG_INDEX_HREF = r('index.html');
})();

// Ordered page list for prev/next
window.KG_PAGE_ORDER = [
  'overview','quickstart',
  'auth','endpoints','apiconventions','versioning','sessionmodel','tenantscope','errors',
  'architecture','modules','contexts','patterns','datamodel',
  'datamodelidentity','datamodelplatform','datamodelorganization','datamodelclientapps',
  'datamodelaccess','datamodelbilling','datamodelaudit',
  'validation','security','adrs'
];
