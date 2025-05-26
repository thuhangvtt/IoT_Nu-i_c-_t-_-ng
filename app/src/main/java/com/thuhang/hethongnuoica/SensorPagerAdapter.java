package com.thuhang.hethongnuoica;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.List;

public class SensorPagerAdapter extends RecyclerView.Adapter<SensorPagerAdapter.SensorViewHolder> {

    private final List<SensorItem> sensorList;

    public SensorPagerAdapter(List<SensorItem> sensorList) {
        this.sensorList = sensorList;
    }

    public void update(int index, String value, int animRes) {
        SensorItem item = sensorList.get(index);
        item.setValue(value);
        item.setAnimationRes(animRes);
        notifyItemChanged(index);
    }

    @NonNull
    @Override
    public SensorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sensor_card, parent, false);
        return new SensorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SensorViewHolder holder, int position) {
        SensorItem item = sensorList.get(position);
        holder.txtTitle.setText(item.getTitle());
        holder.txtValue.setText(item.getValue());
        holder.animView.setAnimation(item.getAnimationRes());
        holder.animView.playAnimation();
    }

    @Override
    public int getItemCount() {
        return sensorList.size();
    }

    static class SensorViewHolder extends RecyclerView.ViewHolder {
        LottieAnimationView animView;
        TextView txtTitle, txtValue;

        public SensorViewHolder(@NonNull View itemView) {
            super(itemView);
            animView = itemView.findViewById(R.id.animSensor);
            txtTitle = itemView.findViewById(R.id.txtSensorTitle);
            txtValue = itemView.findViewById(R.id.txtSensorValue);
        }
    }
}
