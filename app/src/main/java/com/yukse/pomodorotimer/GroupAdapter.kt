package com.yukse.pomodorotimer

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yukse.pomodorotimer.database.GroupEntity
import com.yukse.pomodorotimer.databinding.GroupViewBinding

class GroupAdapter(
    private var grouplist: List<GroupEntity>?,
    private val context: Context?
) : RecyclerView.Adapter<GroupAdapter.ViewHolder>() {
    interface OnGroupClickListener {
        fun onGroupClick(position: Int)
    }

    private lateinit var onGroupClickListener: OnGroupClickListener

    inner class ViewHolder(val groupViewBinding: GroupViewBinding) :
        RecyclerView.ViewHolder(groupViewBinding.root) {
        init {
            if(grouplist == null)
                grouplist = emptyList()
            // group클릭 시 해당 그룹으로 이동
            groupViewBinding.tvGroupName.setOnClickListener {
                onGroupClickListener.onGroupClick(adapterPosition)
            }
        }
    }

    fun setOnGroupClickListener(listener: OnGroupClickListener) {
        //fragment에서 아이템 클릭리스너 호출 후 listener 구현할 것
        this.onGroupClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.group_view, parent, false)
        return ViewHolder(GroupViewBinding.bind(view))
    }

    override fun getItemCount(): Int {
        return grouplist!!.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.groupViewBinding.tvGroupName.setText(grouplist!![position].group)
    }

    //live data를 이용한 데이터 갱신을 위해 구현 - 이 함수 호출하면 데이터 바뀌도록.
    internal fun setData(newData: List<GroupEntity>) {
        grouplist = newData
        notifyDataSetChanged()
    }
}