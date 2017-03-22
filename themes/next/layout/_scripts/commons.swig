{%
  set js_commons = [
    'src/utils.js',
    'src/motion.js'
  ]
%}

{% for common in js_commons %}
  <script type="text/javascript" src="{{ url_for(theme.js) }}/{{ common }}?v={{ theme.version }}"></script>
{% endfor %}

<script type="text/javascript" color="168,168,168" opacity='0.6' zIndex="-1" count="168" src="/js/canvas-nest.min.js"></script>