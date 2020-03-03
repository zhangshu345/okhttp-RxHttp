package com.example.httpsender

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.httpsender.databinding.MainActivityBinding
import com.example.httpsender.databinding.MainActivityBinding.inflate
import com.example.httpsender.entity.Article
import com.example.httpsender.entity.Location
import com.example.httpsender.entity.Name
import com.example.httpsender.entity.NewsDataXml
import com.google.gson.Gson
import com.rxjava.rxlife.life
import com.rxjava.rxlife.lifeOnMain
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import rxhttp.awaitDownload
import rxhttp.wrapper.entity.Progress
import rxhttp.wrapper.param.RxHttp.Companion.get
import rxhttp.wrapper.param.RxHttp.Companion.postForm
import rxhttp.wrapper.param.RxHttp.Companion.postJson
import rxhttp.wrapper.param.RxHttp.Companion.postJsonArray
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var mBinding: MainActivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = inflate(layoutInflater)
        setContentView(mBinding.root)
    }

    fun bitmap(view: View) {
        val imageUrl = "http://img2.shelinkme.cn/d3/photos/0/017/022/755_org.jpg@!normal_400_400?1558517697888"
        get(imageUrl) //Get请求
            .asBitmap() //这里返回Observable<Bitmap> 对象
            .lifeOnMain(this) //感知生命周期，并在主线程回调
            .subscribe({
                mBinding.tvResult.background = BitmapDrawable(it)
            }, {
                mBinding.tvResult.text = it.errorMsg()
                //失败回调
                it.show("图片加载失败,请稍后再试!")
            })
    }

    //发送Get请求，获取文章列表
    fun sendGet(view: View) {
        AndroidScope(this)
            .launch({
                val pageList = get("/article/list/0/json").awaitResponse<String>()
                mBinding.tvResult.text = Gson().toJson(pageList)
            }, {
                mBinding.tvResult.text = it.errorMsg()
                //失败回调
                it.show("发送失败,请稍后再试!")
            })

    }

    //发送Post表单请求,根据关键字查询文章
    fun sendPostForm(view: View) {

        postForm("/article/query/0/json")
            .add("k", null)
            .asResponsePageList<Article>()
            .lifeOnMain(this) //感知生命周期，并在主线程回调
            .subscribe({
                mBinding.tvResult.text = Gson().toJson(it)
            }, {
                mBinding.tvResult.text = it.errorMsg()
                //失败回调
                it.show("发送失败,请稍后再试!")
            })
    }

    //发送Post Json请求，此接口不通，仅用于调试参数
    fun sendPostJson(view: View) { //发送以下User对象
/*
           {
               "name": "张三",
               "sex": 1,
               "height": 180,
               "weight": 70,
               "interest": [
                   "羽毛球",
                   "游泳"
               ],
               "location": {
                   "latitude": 30.7866,
                   "longitude": 120.6788
               },
               "address": {
                   "street": "科技园路.",
                   "city": "江苏苏州",
                   "country": "中国"
               }
           }
         */
        val interestList = ArrayList<String>() //爱好
        interestList.add("羽毛球")
        interestList.add("游泳")
        val address = "{\"street\":\"科技园路.\",\"city\":\"江苏苏州\",\"country\":\"中国\"}"
        postJson("/article/list/0/json")
            .add("name", "张三")
            .add("sex", 1)
            .addAll("{\"height\":180,\"weight\":70}") //通过addAll系列方法添加多个参数
            .add("interest", interestList) //添加数组对象
            .add("location", Location(120.6788, 30.7866)) //添加位置对象
            .addJsonElement("address", address) //通过字符串添加一个对象
            .asString()
            .lifeOnMain(this) //感知生命周期，并在主线程回调
            .subscribe({
                mBinding.tvResult.text = it
            }, {
                mBinding.tvResult.text = it.errorMsg()
                //失败回调
                it.show("发送失败,请稍后再试!")
            })
    }

    //发送Post JsonArray请求，此接口不通，仅用于调试参数
    fun sendPostJsonArray(view: View) { //发送以下Json数组
/*
           [
               {
                   "name": "张三"
               },
               {
                   "name": "李四"
               },
               {
                   "name": "王五"
               },
               {
                   "name": "赵六"
               },
               {
                   "name": "杨七"
               }
           ]
         */
        val names: MutableList<Name> = ArrayList()
        names.add(Name("赵六"))
        names.add(Name("杨七"))
        postJsonArray("/article/list/0/json")
            .add("name", "张三")
            .add(Name("李四"))
            .addJsonElement("{\"name\":\"王五\"}")
            .addAll(names)
            .asString()
            .lifeOnMain(this)
            .subscribe({
                mBinding.tvResult.text = it
            }, {
                mBinding.tvResult.text = it.errorMsg()
                //失败回调
                it.show("发送失败,请稍后再试!")
            })
    }

    //使用XmlConverter解析数据，此接口返回数据太多，会有点慢
    fun xmlConverter(view: View) {
        get("http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a=sf-muni")
            .setXmlConverter()
            .asObject<NewsDataXml>()
            .lifeOnMain(this) //感知生命周期，并在主线程回调
            .subscribe({
                mBinding.tvResult.text = Gson().toJson(it)
            }, {
                mBinding.tvResult.text = it.errorMsg()
                //失败回调
                it.show("发送失败,请稍后再试!")
            })
    }

    //使用XmlConverter解析数据
    fun fastJsonConverter(view: View) {
        get("/article/list/0/json")
            .setFastJsonConverter()
            .asResponsePageList<Article>()
            .lifeOnMain(this) //感知生命周期，并在主线程回调
            .subscribe({
                mBinding.tvResult.text = Gson().toJson(it)
            }, {
                mBinding.tvResult.text = it.errorMsg()
                //失败回调
                it.show("发送失败,请稍后再试!")
            })
    }

    //文件下载，不带进度
    fun download(view: View) {
        val destPath = externalCacheDir.toString() + "/" + System.currentTimeMillis() + ".apk"
        get("/miaolive/Miaolive.apk")
            .setDomainToUpdateIfAbsent() //使用指定的域名
            .asDownload(destPath)
            .lifeOnMain(this) //感知生命周期，并在主线程回调
            .subscribe({
                //下载成功
            }, {
                //下载失败
                it.show("下载失败,请稍后再试!")
            })
    }

    //文件下载，带进度
    fun downloadAndProgress(view: View) { //文件存储路径
        val destPath = externalCacheDir.toString() + "/" + System.currentTimeMillis() + ".apk"
        AndroidScope(this).launch({
            val result = get("/miaolive/Miaolive.apk")
                .setDomainToUpdateIfAbsent() //使用指定的域名
                .awaitDownload(destPath, this) {
                    //下载进度回调,0-100，仅在进度有更新时才会回调，最多回调101次，最后一次回调文件存储路径
                    val currentProgress = it.progress //当前进度 0-100
                    val currentSize = it.currentSize //当前已下载的字节大小
                    val totalSize = it.totalSize //要下载的总字节大小
                    mBinding.tvResult.append("\n" + it.toString())
                }
            mBinding.tvResult.append("\n" + result)
        }, {
            mBinding.tvResult.append("\n" + it.errorMsg())
            //下载失败，处理相关逻辑
            it.show("下载失败,请稍后再试!")
        })
    }

    //断点下载
    fun breakpointDownload(view: View) {
        val destPath = "$externalCacheDir/Miaobo.apk"
        val length = File(destPath).length()
        get("/miaolive/Miaolive.apk")
            .setDomainToUpdateIfAbsent() //使用指定的域名
            .setRangeHeader(length) //设置开始下载位置，结束位置默认为文件末尾
            .asDownload(destPath) //注意这里使用DownloadParser解析器，并传入本地路径
            .lifeOnMain(this) //感知生命周期，并在主线程回调
            .subscribe({
                //下载成功
            }, {
                //下载失败
                it.show("下载失败,请稍后再试!")
            })
    }

    //断点下载，带进度
    fun breakpointDownloadAndProgress(view: View) {
        val destPath = "$externalCacheDir/Miaobo.apk"
        val length = File(destPath).length()
        get("/miaolive/Miaolive.apk")
            .setDomainToUpdateIfAbsent() //使用指定的域名
            .setRangeHeader(length) //设置开始下载位置，结束位置默认为文件末尾
            .asDownload(destPath, length, Consumer {
                //如果需要衔接上次的下载进度，则需要传入上次已下载的字节数length
                //下载进度回调,0-100，仅在进度有更新时才会回调
                val currentProgress = it.progress //当前进度 0-100
                val currentSize = it.currentSize //当前已下载的字节大小
                val totalSize = it.totalSize //要下载的总字节大小
                mBinding.tvResult.append("\n" + it.toString())
            }, AndroidSchedulers.mainThread()) //指定回调(进度/成功/失败)线程,不指定,默认在请求所在线程回调
            .life(this) //加入感知生命周期的观察者
            .subscribe({
                //下载成功
                mBinding.tvResult.append("\n下载成功 : $it")
            }, {
                //下载失败
                mBinding.tvResult.append("\n" + it.errorMsg())
                it.show("下载失败,请稍后再试!")
            })
    }

    //文件上传，不带进度
    fun upload(v: View?) {
        postForm("http://t.xinhuo.com/index.php/Api/Pic/uploadPic")
            .addFile("uploaded_file", File(Environment.getExternalStorageDirectory(), "1.jpg"))
            .asString() //from操作符，是异步操作
            .lifeOnMain(this) //感知生命周期，并在主线程回调
            .subscribe({
                mBinding.tvResult.append("\n")
                mBinding.tvResult.append(it)
            }, {
                mBinding.tvResult.append("\n")
                mBinding.tvResult.append(it.errorMsg())
                //失败回调
                it.show("上传失败,请稍后再试!")
            })
    }

    //上传文件，带进度
    fun uploadAndProgress(v: View?) {
        postForm("http://t.xinhuo.com/index.php/Api/Pic/uploadPic")
            .addFile("uploaded_file", File(Environment.getExternalStorageDirectory(), "1.jpg"))
            .asUpload(Consumer { progress: Progress<String> ->
                //上传进度回调,0-100，仅在进度有更新时才会回调
                val currentProgress = progress.progress //当前进度 0-100
                val currentSize = progress.currentSize //当前已上传的字节大小
                val totalSize = progress.totalSize //要上传的总字节大小
                mBinding.tvResult.append("\n" + progress.toString())
            }, AndroidSchedulers.mainThread()) //指定回调(进度/成功/失败)线程,不指定,默认在请求所在线程回调
            .life(this) //加入感知生命周期的观察者
            .subscribe({
                //上传成功
                mBinding.tvResult.append("\n上传成功 : $it")
            }, {
                //上传失败
                mBinding.tvResult.append("\n" + it.errorMsg())
                it.show("上传失败,请稍后再试!")
            })
    }

    //多任务下载
    fun multitaskDownload(view: View) {
        startActivity(Intent(this, DownloadMultiActivity::class.java))
    }

    fun clearLog(view: View) {
        mBinding.tvResult.text = ""
        mBinding.tvResult.setBackgroundColor(Color.TRANSPARENT)
    }
}