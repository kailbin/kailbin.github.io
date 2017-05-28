    
    npm config set registry https://registry.npm.taobao.org
    npm install --save hexo hexo-deployer-git hexo-renderer-jade hexo-generator-feed hexo-generator-sitemap hexo-browsersync hexo-generator-archive


	hexo clean
	hexo g
	hexo server -p 8080
	hexo deploy
	


# 搜索功能

	$ npm install hexo-generator-search --save
	$ npm install hexo-generator-searchdb --save

	# _config.yml 文件新增
	search:
	  path: search.xml
	  field: post
  	  format: html
	  limit: 10000
