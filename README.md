# ECSpider
毕业设计：电商爬虫部分

使用webmagic + selenium，目标是爬取京东的几个热门品类数据，并保存爬取下来的数据，以便后期进行数据分析。

数据爬取的维度目标是商品标题、价格、销量、店铺名称、其他标志以及商品评论。

后期完成自己的分布式爬虫框架后，会使用框架进行改写。

**前端界面**

目前做了一个简易的前端页面，可以看到当前大致的爬取进度、开启一个新的爬虫、开启一个新的定时爬虫。

![index](https://github.com/tribbleofjim/ECSpider/blob/master/pictures/index.png)

**反爬虫与定时任务**

定时功能是为了绕过反爬虫，因为经过前面的尝试，一个时间段内同个ip抓取url的次数过多，则会被京东判定为爬虫。

采用quartz做定时任务框架，通过WebMagic自带的SpiderListener机制进行控制。

quartz用来开启守护线程，每隔一段时间启动一次爬虫；SpiderListener用来控制爬虫停止的条件。目前有定时和定量两种控制方法，即：爬取一段时间后爬虫停止，或者爬取一定数目的url后爬虫停止。注意停止的时间应不能超过quartz重启爬虫的时间，不然定时任务将没有意义。

经过几次实验，目前是每2小时启动一次爬虫，每次爬取480条url。按照我在项目中设定的爬虫的休息时间，相当于爬虫每工作96分钟后休眠24分钟。

quartz的线程上限不宜太高。会占用服务器资源。



**为什么不用ip代理池？**

免费开源的ip代理池项目是有尝试过的，如ProxyPool等。虽然成功运行也能获取到代理ip，但质量实在太低了，可用率不到10%。即使可用，很多代理ip的速度也非常慢，不如说更多地是在降低爬虫的效率。

收费的ip代理池是可以用的，但是考虑到那些代理池通常是企业在购买，而本项目实质上是一个毕业设计，对于数据的获取上不需要太强的实效性。

所以最后还是采用了定时任务的方法，相当于牺牲了一部分爬虫效率换取稳定性。
