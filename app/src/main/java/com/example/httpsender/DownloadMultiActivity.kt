package com.example.httpsender

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.httpsender.entity.DownloadInfo
import com.rxjava.rxlife.life
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import rxhttp.wrapper.entity.Progress
import rxhttp.wrapper.param.RxHttp.Companion.get
import java.io.File
import java.util.*

/**
 * 多任务下载
 * User: ljx
 * Date: 2019-06-07
 * Time: 11:02
 */
class DownloadMultiActivity : AppCompatActivity(), DownloadMultiAdapter.OnItemClickListener<DownloadInfo> {
    private var mAdapter: DownloadMultiAdapter? = null
    private val downloadUrl = arrayOf(
        "http://update.9158.com/miaolive/Miaolive.apk",  //喵播
        "https://apk-ssl.tancdn.com/3.5.3_276/%E6%8E%A2%E6%8E%A2.apk",  //探探
        "https://o8g2z2sa4.qnssl.com/android/momo_8.18.5_c1.apk",  //陌陌
        "http://s9.pstatp.com/package/apk/aweme/app_aweGW_v6.6.0_2905d5c.apk" //抖音
    )
    private val waitTask: MutableList<DownloadInfo> = ArrayList() //等待下载的任务
    private val downloadingTask: MutableList<DownloadInfo> = ArrayList() //等待下载的任务
    private val downloadInfos: MutableList<DownloadInfo> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.download_multi_activity)
        for (i in 0..19) {
            val downloadInfo = DownloadInfo(downloadUrl[i % downloadUrl.size])
            downloadInfo.taskId = i
            downloadInfos.add(downloadInfo)
        }
        mAdapter = DownloadMultiAdapter(downloadInfos)
        mAdapter!!.setOnItemClickListener(this)
        mAdapter!!.setHasStableIds(true)
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.adapter = mAdapter
    }

    //500毫秒刷新一次列表
    private fun notifyDataSetChanged(force: Boolean) {
        val time = System.currentTimeMillis()
        if (time - lastChangedTime > 500 || force) {
            mAdapter!!.notifyDataSetChanged()
            lastChangedTime = time
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.download, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.download_all) {
            if ("全部下载".contentEquals(item.title)) {
                for (info in downloadInfos) {
                    download(info)
                }
                item.title = "全部取消"
            } else if ("全部取消".contentEquals(item.title)) {
                var iterator = waitTask.iterator()
                while (iterator.hasNext()) {
                    val next = iterator.next()
                    next.state = 6
                    iterator.remove()
                }
                iterator = downloadingTask.iterator()
                while (iterator.hasNext()) {
                    val next = iterator.next()
                    iterator.remove()
                    val disposable = next.disposable
                    if (disposable != null && !disposable.isDisposed) {
                        disposable.dispose()
                    }
                    next.state = 6
                }
                item.title = "全部下载"
                notifyDataSetChanged(true)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onItemClick(view: View, data: DownloadInfo, position: Int) {
        when (view.id) {
            R.id.bt_pause -> {
                val state = data.state
                if (state == 0) {
                    download(data)
                } else if (state == 1) {
                    waitTask.remove(data)
                    data.state = 6
                } else if (state == 2) {
                    val disposable = data.disposable
                    if (disposable != null && !disposable.isDisposed) {
                        disposable.dispose()
                        data.state = 3
                        notifyDataSetChanged(true)
                    }
                } else if (state == 3) {
                    download(data)
                } else if (state == 4) {
                    Tip.show("该任务已完成")
                } else if (state == 5) {
                    Tip.show("该任务下载失败")
                } else if (state == 6) {
                    download(data)
                }
            }
        }
    }

    private fun download(data: DownloadInfo) {
        if (downloadingTask.size >= MAX_TASK_COUNT) {
            data.state = 1
            waitTask.add(data)
            return
        }
        val destPath = externalCacheDir.toString() + "/" + data.taskId + ".apk"
        val length = File(destPath).length()
        val disposable = get(data.url)
            .setRangeHeader(length) //设置开始下载位置，结束位置默认为文件末尾
            .asDownload(destPath, length, Consumer { progress: Progress<String> ->
                //如果需要衔接上次的下载进度，则需要传入上次已下载的字节数length
                //下载进度回调,0-100，仅在进度有更新时才会回调
                if (!progress.isCompleted) {
                    data.progress = progress.progress //当前进度 0-100
                    data.currentSize = progress.currentSize //当前已下载的字节大小
                    data.totalSize = progress.totalSize //要下载的总字节大小
                    notifyDataSetChanged(false)
                }
            }, AndroidSchedulers.mainThread())
            .doFinally {
                //不管任务成功还是失败，如果还有在等待的任务，都开启下一个任务
                downloadingTask.remove(data)
                if (waitTask.size > 0) download(waitTask.removeAt(0))
            }
            .life(this) //加入感知生命周期的观察者
            .subscribe({
                //s为String类型
                Tip.show("下载完成$it")
                data.state = 4
                notifyDataSetChanged(true)
            }, {
                data.state = 5
            })
        data.state = 2
        downloadingTask.add(data)
        data.disposable = disposable
    }

    companion object {
        const val MAX_TASK_COUNT = 3 //最大并发数
        var lastChangedTime: Long = 0
    }
}