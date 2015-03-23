package pl.tajchert.notificationresponse;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;


public class AdapterNotifList extends RecyclerView.Adapter<AdapterNotifList.ViewHolder> {
    private List<NotificationWear> notifications;

    public AdapterNotifList(List<NotificationWear> notificationWears) {
        this.notifications = notificationWears;
    }

    @Override
    public AdapterNotifList.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notif_list, null);
        ViewHolder viewHolder = new ViewHolder(itemLayoutView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        viewHolder.txtViewTitle.setText(notifications.get(position).packageName);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txtViewTitle;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            txtViewTitle = (TextView) itemLayoutView.findViewById(R.id.notif_app_title);
        }
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }
}