
    npm config set registry https://registry.npm.taobao.org
    npm install
    npm install hexo-cli -g
    npm install hexo-server --save
    npm install hexo-deployer-git --save
    npm install hexo-generator-sitemap --save



	hexo clean
	hexo g
	hexo server -p 8080
	hexo deploy



# [搜索功能](https://github.com/PaicHyperionDev/hexo-generator-search)


	$ npm install hexo-generator-search --save
	$ npm install hexo-generator-searchdb --save
	
	# _config.yml 文件新增
	search:
	  path: search.xml
	  field: post  	  
	  format: html	  
	  limit: 10000

# 自定义的地方
- issue label 长度 : \themes\next\layout\_third-party\comments\gitment.swig
- 动态背景 : themes/next/layout/_scripts/commons.swig
- 自定义主题： themes/next/source/css/_custom/custom.styl
