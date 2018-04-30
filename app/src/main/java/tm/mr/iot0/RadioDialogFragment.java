package tm.mr.iot0;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import tm.mr.iot0.helper.RvAdapter;
import tm.mr.iot0.model.Channel;

/**
 * Created by viridis on 30.04.2018.
 */

public class RadioDialogFragment extends AppCompatDialogFragment implements RvAdapter.onPlayListener, View.OnClickListener {

    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ibtnVolDown)
    ImageButton ibtnVolDown;
    @BindView(R.id.ibtnVolUp)
    ImageButton ibtnVolUp;
    @BindView(R.id.ibtnClose)
    ImageButton ibtnClose;
    @BindView(R.id.rv)
    RecyclerView rv;
    @BindView(R.id.pv)
    View pv;
    OnSendCmdListener onSendCmdListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSendCmdListener) {
            onSendCmdListener = (OnSendCmdListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.radio_layout, container, false);
        ButterKnife.bind(this, view);

        RvAdapter adapter = new RvAdapter(this);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);
        rv.setHasFixedSize(true);
        adapter.loadFromRes(getContext());

        pv.setVisibility(View.GONE);

        ibtnClose.setOnClickListener(this);
        ibtnVolDown.setOnClickListener(this);
        ibtnVolUp.setOnClickListener(this);
        return view;
    }

    @Override
    public void onPlay(Channel channel) {
        pv.setVisibility(View.VISIBLE);
        tvTitle.setText(channel.getTitle());
        sendCmd(0, "play", channel.getsUri());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibtnClose:
                pv.setVisibility(View.GONE);
                tvTitle.setText("radio title");
                sendCmd(0, "stop", "");
                break;
            case R.id.ibtnVolDown:
                sendCmd(0, "voldown", "");
                break;
            case R.id.ibtnVolUp:
                sendCmd(0, "volup", "");
                break;
        }
    }

    public void sendCmd(int id, String sCmd, String sUrl) {
        String s = "{\"cmd\":\"" + sCmd + "\", \"id\":" + id + ", \"url\":\"" + sUrl + "\"}";
        onSendCmdListener.onSendCmd(s);
        Log.d(MainActivity.TAG, "sendCmd: "+s);
    }

    interface OnSendCmdListener {
        void onSendCmd(String sJson);
    }
}
