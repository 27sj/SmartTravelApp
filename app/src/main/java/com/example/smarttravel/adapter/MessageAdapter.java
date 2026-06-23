// ============================================================
// MessageAdapter.java — 消息列表适配器（增强版）
// 支持未读徽章、加粗标题、点击跳转详情
// ============================================================
package com.example.smarttravel.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.smarttravel.MessageDetailActivity;
import com.example.smarttravel.R;
import com.example.smarttravel.model.MessageInfo;

import java.util.List;

/**
 * MessageAdapter 消息 RecyclerView 适配器
 * 增强：未读徽章（红点）、未读加粗、点击详情
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private List<MessageInfo> messageList;
    private Context context;

    public MessageAdapter(Context context, List<MessageInfo> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        MessageInfo msg = messageList.get(position);
        holder.tvTitle.setText(msg.getTitle());
        holder.tvContent.setText(msg.getContent());
        holder.tvTime.setText(msg.getTime());

        // 未读状态处理
        boolean isUnread = (msg.getStatus() == 0);
        if (isUnread) {
            // 未读：标题加粗 + 显示徽章
            holder.tvTitle.setTypeface(null, Typeface.BOLD);
            holder.tvUnreadCount.setVisibility(View.VISIBLE);
            holder.tvUnreadCount.setText("1");
        } else {
            // 已读：标题正常 + 隐藏徽章
            holder.tvTitle.setTypeface(null, Typeface.NORMAL);
            holder.tvUnreadCount.setVisibility(View.GONE);
        }

        // 点击查看详情
        holder.itemView.setOnClickListener(v -> {
            // 点击时标记已读（如果有回调可扩展）
            Intent intent = new Intent(context, MessageDetailActivity.class);
            intent.putExtra("msg_id", msg.getId());
            intent.putExtra("msg_title", msg.getTitle());
            intent.putExtra("msg_content", msg.getContent());
            intent.putExtra("msg_time", msg.getTime());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvTime, tvUnreadCount;

        public ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_msg_title);
            tvContent = itemView.findViewById(R.id.tv_msg_content);
            tvTime = itemView.findViewById(R.id.tv_msg_time);
            tvUnreadCount = itemView.findViewById(R.id.tv_msg_unread_count);
        }
    }
}
