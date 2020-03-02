package com.example.httpsender

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.httpsender.DownloadMultiAdapter.MyViewHolder
import com.example.httpsender.entity.DownloadInfo
import java.text.DecimalFormat

/**
 * User: ljx
 * Date: 2019-06-07
 * Time: 11:10
 */
class DownloadMultiAdapter(private val mDownloadInfos: List<DownloadInfo>) : RecyclerView.Adapter<MyViewHolder>() {
    private var mOnItemClickListener: OnItemClickListener<DownloadInfo>? = null
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): MyViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.download_multi_adapter, viewGroup, false)
        return MyViewHolder(view)
    }

    @SuppressLint("DefaultLocale")
    override fun onBindViewHolder(viewHolder: MyViewHolder, i: Int) {
        val data = mDownloadInfos[i]
        viewHolder.apply {
            progressBar.progress = data.progress
            tvProgress.text = String.format("%d%%", data.progress)
            btPause.setOnClickListener { v: View -> mOnItemClickListener?.onItemClick(v, data, i) }
            val currentSize = DecimalFormat("0.0").format(data.currentSize * 1.0f / 1024 / 1024.toDouble())
            val totalSize = DecimalFormat("0.0").format(data.totalSize * 1.0f / 1024 / 1024.toDouble())
            tvSize.text = String.format("%sM/%sM", currentSize, totalSize)
            val state = data.state
            if (state == 0) {
                tvWaiting.text = "未开始"
                btPause.text = "开始"
            } else if (state == 1) {
                tvWaiting.text = "等待中.."
                btPause.text = "取消"
            } else if (state == 2) {
                tvWaiting.text = "下载中.."
                btPause.text = "暂停"
            } else if (state == 3) {
                tvWaiting.text = "已暂停"
                btPause.text = "继续"
            } else if (state == 4) {
                tvWaiting.text = "已完成"
                btPause.text = "已完成"
            } else if (state == 5) {
                tvWaiting.text = "下载失败"
                btPause.text = "下载失败"
            } else if (state == 6) {
                tvWaiting.text = "已取消"
                btPause.text = "重新下载"
            }
        }
    }

    override fun getItemCount(): Int {
        return mDownloadInfos.size
    }

    override fun getItemId(position: Int): Long {
        return mDownloadInfos[position].hashCode().toLong()
    }

    class MyViewHolder(itemView: View) : ViewHolder(itemView) {
        var progressBar: ProgressBar = itemView.findViewById(R.id.progress_bar)
        var tvProgress: TextView = itemView.findViewById(R.id.tv_progress)
        var tvSize: TextView = itemView.findViewById(R.id.tv_size)
        var btPause: Button = itemView.findViewById(R.id.bt_pause)
        var tvWaiting: TextView = itemView.findViewById(R.id.tv_waiting)
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener<DownloadInfo>?) {
        mOnItemClickListener = onItemClickListener
    }

    interface OnItemClickListener<T> {
        fun onItemClick(view: View, data: T, position: Int)
    }

}