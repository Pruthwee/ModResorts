// PostCSS configuration for CSS minification and purging (cz-css-1004, cz-css-1005 remediation)
// Used in the multi-stage Docker build to:
//   1. Remove unused CSS rules via PurgeCSS (cz-css-1005) to reduce container image bloat
//   2. Minify remaining CSS via cssnano (cz-css-1004) to further reduce ECR storage costs
//      and EKS pod startup time.
const purgecss = require('@fullhuman/postcss-purgecss');

module.exports = {
  plugins: [
    // -------------------------------------------------------------------------
    // PurgeCSS (cz-css-1005): Strip unused CSS rules by scanning all HTML/JSP/JS
    // content files for class/id/element selectors that are actually referenced.
    // This removes dead styles left over from removed components, reducing the
    // final container image size and Kubernetes registry storage costs.
    // -------------------------------------------------------------------------
    purgecss({
      // Content files to scan for used CSS selectors
      content: [
        './src/index.html',
        './src/login.jsp',
        './src/main.js',
      ],
      // Safelist selectors that are added dynamically at runtime (via JS) and
      // would otherwise be incorrectly removed by static analysis.
      safelist: {
        standard: [
          // Dynamic weather-state classes toggled by main.js
          /^js-/,
          'is-selected',
          // Pikaday date-picker classes (injected by pikaday.js at runtime)
          /^pika-/,
          /^is-/,
          /^has-/,
        ],
        deep: [],
        greedy: [],
      },
      // Default extractor covers standard CSS class/id/element selectors
      defaultExtractor: content => content.match(/[\w-/:]+(?<!:)/g) || [],
    }),

    // -------------------------------------------------------------------------
    // cssnano (cz-css-1004): Minify the purged CSS output for maximum compression
    // -------------------------------------------------------------------------
    require('cssnano')({
      preset: [
        'default',
        {
          // Remove all comments (including /*! licence comments) in production
          discardComments: { removeAll: true },
          // Normalise whitespace
          normalizeWhitespace: true,
          // Merge duplicate rules
          mergeLonghand: true,
          // Minify selectors
          minifySelectors: true,
          // Minify font values
          minifyFontValues: true,
          // Minify gradients
          minifyGradients: true,
          // Reduce initial values
          reduceInitial: true,
          // Discard empty rules
          discardEmpty: true,
          // Discard duplicate rules
          discardDuplicates: true,
          // Normalise url() values
          normalizeUrl: true,
        },
      ],
    }),
  ],
};
