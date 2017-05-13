{% if page.comments %}
  <div class="comments" id="comments">
    {% if (theme.duoshuo and theme.duoshuo.shortname) or theme.duoshuo_shortname %}
      <div class="ds-thread" data-thread-key="{{ page.path }}"
           data-title="{{ page.title }}" data-url="{{ page.permalink }}">
      </div>

    {% elseif theme.uyan %}
      
      <div id="uyan_frame"></div>
      <script type="text/javascript" src="http://v2.uyan.cc/code/uyan.js?uid={{ theme.uyan }}"></script>


    {% elseif theme.cloud_tie %}
    
      <div id="cloud-tie-wrapper" class="cloud-tie-wrapper"></div>
      <script src="https://img1.cache.netease.com/f2e/tie/yun/sdk/loader.js"></script>
      <script>
        var cloudTieConfig = {
          url: document.location.href, 
          sourceId: "",
          productKey: "{{ theme.cloud_tie }}",
          target: "cloud-tie-wrapper"
        };
        var yunManualLoad = true;
        Tie.loader("aHR0cHM6Ly9hcGkuZ2VudGllLjE2My5jb20vbW9iaWxlL2xpdmVzY3JpcHQuaHRtbA==", true);
      </script>


    {% elseif theme.facebook_sdk.enable and theme.facebook_comments_plugin.enable %}
      <div class="fb-comments"
           data-href="{{ page.permalink }}"
           data-numposts="{{ theme.facebook_comments_plugin.num_of_posts }}"
           data-width="{{ theme.facebook_comments_plugin.width }}"
           data-colorscheme="{{ theme.facebook_comments_plugin.scheme }}">
      </div>  
    {% elseif theme.disqus_shortname %}
      <div id="disqus_thread">
        <noscript>
          Please enable JavaScript to view the
          <a href="//disqus.com/?ref_noscript">comments powered by Disqus.</a>
        </noscript>
      </div>
    {% elseif theme.hypercomments_id %}
      <div id="hypercomments_widget"></div>
    {% endif %}
  </div>
{% endif %}
