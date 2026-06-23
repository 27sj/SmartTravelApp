// ============================================================
// CarTypeAdapter.java — 车型选择列表适配器
// 用于 RecyclerView 展示四种可选车型
// 点击事件回调至 Activity 处理跳转
// ============================================================
package com.example.smarttravel;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * CarTypeAdapter 车型列表适配器
 * 绑定 CarType 数据到 item_car_type.xml 布局
 * 通过 OnItemClickListener 回调处理点击事件
 */
public class CarTypeAdapter extends RecyclerView.Adapter<CarTypeAdapter.ViewHolder> {

    /** 车型数据列表 */
    private List<CarType> carTypeList;

    /** 点击监听器接口 */
    private OnItemClickListener listener;

    /**
     * 列表项点击回调接口
     * 由使用方（Activity/Fragment）实现
     */
    public interface OnItemClickListener {
        void onItemClick(CarType carType);
    }

    /**
     * 构造方法
     * @param carTypeList 车型数据列表
     */
    public CarTypeAdapter(List<CarType> carTypeList) {
        this.carTypeList = carTypeList;
    }

    /**
     * 设置点击监听器
     * @param listener 回调实现
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * 创建 ViewHolder — 加载列表项布局
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 载入 item_car_type.xml 布局
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_car_type, parent, false);
        return new ViewHolder(view);
    }

    /**
     * 绑定数据到 ViewHolder
     * @param holder   视图容器
     * @param position 当前列表位置
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CarType item = carTypeList.get(position);

        // 设置车型图标
        holder.ivIcon.setImageResource(item.getIconResId());
        // 设置车型名称
        holder.tvName.setText(item.getName());
        // 设置预计到达时间
        holder.tvArriveTime.setText("预计 " + item.getArriveTime() + " 到达");
        // 设置预估价格
        holder.tvPrice.setText(item.getPrice());

        // 为整个列表项设置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    /**
     * 返回列表项总数
     */
    @Override
    public int getItemCount() {
        return carTypeList == null ? 0 : carTypeList.size();
    }

    /**
     * ViewHolder 内部类 — 缓存列表项的子视图引用
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;        // 车型图标
        TextView tvName;         // 车型名称
        TextView tvArriveTime;   // 预计到达时间
        TextView tvPrice;        // 预估价格

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            // 通过 findViewById 绑定视图
            ivIcon = itemView.findViewById(R.id.iv_car_icon);
            tvName = itemView.findViewById(R.id.tv_car_name);
            tvArriveTime = itemView.findViewById(R.id.tv_arrive_time);
            tvPrice = itemView.findViewById(R.id.tv_price);
        }
    }
}
