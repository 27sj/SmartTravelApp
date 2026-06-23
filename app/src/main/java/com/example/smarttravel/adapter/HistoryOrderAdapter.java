// ============================================================
// HistoryOrderAdapter.java — 历史订单列表适配器（升级版）
// 绑定 SQLite 查询到的完整订单数据（含司机姓名/车型）
// 支持长按删除回调
// ============================================================
package com.example.smarttravel;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * HistoryOrderAdapter 历史订单适配器
 * 展示已完成的订单记录，每项含路线/车型/司机/时间/费用
 */
public class HistoryOrderAdapter extends RecyclerView.Adapter<HistoryOrderAdapter.ViewHolder> {

    private List<OrderInfo> orderList;
    private OnDeleteClickListener deleteListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(OrderInfo order, int position);
    }

    public HistoryOrderAdapter(List<OrderInfo> orderList) {
        this.orderList = orderList;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderInfo order = orderList.get(position);

        // 路线
        holder.tvRoute.setText(order.getStartPoint() + " → " + order.getEndPoint());
        // 车型
        holder.tvCarType.setText(order.getCarType());
        // 司机姓名（取姓+师傅，或直接显示）
        String driverName = order.getDriverName();
        holder.tvDriver.setText(driverName != null ? driverName : "");
        // 时间
        holder.tvTime.setText(order.getTime());
        // 费用
        holder.tvPrice.setText(order.getPrice());

        // 长按删除
        holder.itemView.setOnLongClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(order, holder.getAdapterPosition());
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return orderList == null ? 0 : orderList.size();
    }

    public void updateData(List<OrderInfo> newList) {
        this.orderList = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoute;
        TextView tvCarType;
        TextView tvDriver;      // 新增：司机姓名
        TextView tvTime;
        TextView tvPrice;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoute = itemView.findViewById(R.id.tv_hist_route);
            tvCarType = itemView.findViewById(R.id.tv_hist_car_type);
            tvDriver = itemView.findViewById(R.id.tv_hist_driver);
            tvTime = itemView.findViewById(R.id.tv_hist_time);
            tvPrice = itemView.findViewById(R.id.tv_hist_price);
        }
    }
}
