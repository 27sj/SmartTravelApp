// ============================================================
// MessageFragment.java — 消息碎片（升级版）
// 从本地数据库读取消息，使用 RecyclerView 展示
// 消息按时间倒序排列，点击可查看详情
// ============================================================
package com.example.smarttravel;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarttravel.adapter.MessageAdapter;
import com.example.smarttravel.database.MessageDatabaseHelper;
import com.example.smarttravel.model.MessageInfo;

import java.util.List;

/**
 * MessageFragment 消息碎片（升级版）
 * 从 SQLite 消息数据库中读取消息通知
 */
public class MessageFragment extends Fragment {

    private RecyclerView recyclerMessages;
    private View layoutEmptyMessage;
    private MessageAdapter adapter;
    private MessageDatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_message, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerMessages = view.findViewById(R.id.recycler_messages);
        layoutEmptyMessage = view.findViewById(R.id.layout_empty_message);

        recyclerMessages.setLayoutManager(new LinearLayoutManager(getContext()));

        dbHelper = new MessageDatabaseHelper(requireContext());

        // 加载消息数据
        loadMessages();
    }

    @Override
    public void onResume() {
        super.onResume();
        // 回到此页面时刷新消息列表
        loadMessages();
    }

    private void loadMessages() {
        try {
            List<MessageInfo> messageList = dbHelper.getAllMessages();

            if (messageList == null || messageList.isEmpty()) {
                recyclerMessages.setVisibility(View.GONE);
                layoutEmptyMessage.setVisibility(View.VISIBLE);
            } else {
                recyclerMessages.setVisibility(View.VISIBLE);
                layoutEmptyMessage.setVisibility(View.GONE);
                adapter = new MessageAdapter(requireContext(), messageList);
                recyclerMessages.setAdapter(adapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
            recyclerMessages.setVisibility(View.GONE);
            layoutEmptyMessage.setVisibility(View.VISIBLE);
        }
    }
}
