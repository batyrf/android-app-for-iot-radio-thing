package tm.mr.iot0.helper;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import tm.mr.iot0.R;
import tm.mr.iot0.model.Channel;

/**
 * Created by viridis on 27.04.2018.
 */

public class RvAdapter extends RecyclerView.Adapter<RvAdapter.ViewHolder> {

    private List<Channel> ls;
    onPlayListener onPlayListener;

    public RvAdapter(onPlayListener onPlayListener) {
        this.onPlayListener = onPlayListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item_layout, parent, false);
        return new ViewHolder(view, new onClickListener() {
            @Override
            public void onClick(int position) {
                onPlayListener.onPlay(ls.get(position));
            }
        });
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.tvTitle.setText(ls.get(position).getTitle());
    }

    @Override
    public int getItemCount() {
        return ls.size();
    }

    public void load(List<Channel> ls) {
        this.ls = ls;
        notifyDataSetChanged();
    }

    public void loadFromRes(Context context) {
        List<Channel> ls = new ArrayList<>();
        for (int i = 0; i < context.getResources().getStringArray(R.array.urls).length; i++)
            ls.add(new Channel(context.getResources().getStringArray(R.array.titles)[i], context.getResources().getStringArray(R.array.urls)[i]));

        load(ls);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.tvTitle)
        TextView tvTitle;
        onClickListener onClickListener;

        public ViewHolder(View itemView, onClickListener onClickListener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.onClickListener = onClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onClickListener.onClick(getAdapterPosition());
        }
    }

    private interface onClickListener {
        void onClick(int position);
    }

    public interface onPlayListener {
        void onPlay(Channel channel);
    }


}