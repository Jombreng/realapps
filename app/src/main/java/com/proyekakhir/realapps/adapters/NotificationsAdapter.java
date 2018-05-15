package com.proyekakhir.realapps.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.proyekakhir.realapps.R;
import com.proyekakhir.realapps.adapters.holders.NotificationsViewHolder;
import com.proyekakhir.realapps.model.Notifications;

import java.util.ArrayList;
import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsViewHolder>{
    private List<Notifications> list = new ArrayList<>();
    private Callback callback;

    @Override
    public NotificationsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_follower_list_item, parent, false);
        return new NotificationsViewHolder(view, callback);
    }

    @Override
    public void onBindViewHolder(NotificationsViewHolder holder, int position) {
        holder.itemView.setLongClickable(true);
        holder.bindData(getItemByPosition(position));

    }

    public List<Notifications> getList() {
        return list;
    }

    public Notifications getItemByPosition(int position) {
        return list.get(position);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void setList(List<Notifications> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface Callback {
        void onItemClick(Notifications notifications, View view);
        void onAuthorClick(String authorId, View view);
    }
}
