
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



# [集成豆瓣](https://github.com/mythsman/hexo-douban)
```
# 安装
$ npm install hexo-douban --save

# 配置
douban:
  user: mythsman
  builtin: false
  book:
    title: 'This is my book title'
    quote: 'This is my book quote'
  movie:
    title: 'This is my movie title'
    quote: 'This is my movie quote'
  game:
    title: 'This is my game title'
    quote: 'This is my game quote'
  timeout: 10000 

# 生成
$ hexo douban
```


# 自定义的地方

- 动态背景 : themes/next/layout/_scripts/commons.swig
- 自定义主题： 
	- themes/next/source/css/_custom/custom.styl
	- themes/next/source/css/_custom/custom_black.styl
