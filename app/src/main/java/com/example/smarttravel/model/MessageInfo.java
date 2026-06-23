// ============================================================
// MessageInfo.java — 消息信息实体模型
// 用于消息通知系统的持久化和列表展示
// ============================================================
package com.example.smarttravel.model;

/**
 * MessageInfo 消息信息实体类
 * 包含标题、内容、时间、状态等字段
 */
public class MessageInfo {

    private long id;
    private String title;       // 消息标题
    private String content;     // 消息内容
    private String time;        // 消息时间
    private int status;         // 0-未读, 1-已读

    public MessageInfo(String title, String content, String time) {
        this.title = title;
        this.content = content;
        this.time = time;
        this.status = 0;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
}
