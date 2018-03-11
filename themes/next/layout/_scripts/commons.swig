{%
  set js_commons = [
    'src/utils.js',
    'src/motion.js'
  ]
%}

{% for common in js_commons %}
  <script type="text/javascript" src="{{ url_for(theme.js) }}/{{ common }}?v={{ theme.version }}"></script>
{% endfor %}

<script type="text/javascript" color="68,68,68" opacity='0.2' zIndex="-1" count="68" src="/js/canvas-nest.min.js"></script>
