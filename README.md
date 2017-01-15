    
    npm install hexo --save   
    npm install
    npm install --save hexo-renderer-jade hexo-generator-feed hexo-generator-sitemap hexo-browsersync hexo-generator-archive



	hexo clean
	hexo g
	hexo server -p 8080
	hexo deploy
	

	
	git remote -v
	git remote add all https://git.coding.net/yokoboy/yokoboy.git 
	git remote set-url --add all https://git.oschina.net/yokoboy/yokoboy.git
	git push all hexo-sources
