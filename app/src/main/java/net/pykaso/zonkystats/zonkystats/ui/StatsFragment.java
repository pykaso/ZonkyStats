package net.pykaso.zonkystats.zonkystats.ui;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import net.pykaso.zonkystats.zonkystats.R;
import net.pykaso.zonkystats.zonkystats.di.Injectable;
import net.pykaso.zonkystats.zonkystats.repository.Status;

import org.threeten.bp.OffsetDateTime;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;

public class StatsFragment extends Fragment implements Injectable {

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Inject
    NavigationController navigationController;

    // Not using Butterknife, overkill for only this one small view
    private StatsViewModel statsViewModel;
    private ImageView bg;
    private LineChartView graph;
    private RadioGroup buttons;
    private RotateAnimation rotate;
    protected Button btn1;
    protected Button btn2;
    protected Button btn3;
    protected Button btn4;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        statsViewModel = ViewModelProviders.of(this, viewModelFactory).get(StatsViewModel.class);
        observe();
        statsViewModel.setInterval(StatsViewModel.Interval.YEAR_ACTUAL);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_stats, container, false);
        this.buttons = v.findViewById(R.id.buttonsGroup);
        this.graph = v.findViewById(R.id.graph);
        this.bg = v.findViewById(R.id.imageView);

        this.btn1 = v.findViewById(R.id.btn1);
        this.btn2 = v.findViewById(R.id.btn2);
        this.btn3 = v.findViewById(R.id.btn3);
        this.btn4 = v.findViewById(R.id.btn4);

        this.btn1.setOnClickListener(buttonListener);
        this.btn2.setOnClickListener(buttonListener);
        this.btn3.setOnClickListener(buttonListener);
        this.btn4.setOnClickListener(buttonListener);

        this.btn3.setText(String.valueOf(OffsetDateTime.now().minusYears(1).getYear()));
        this.btn4.setText(String.valueOf(OffsetDateTime.now().getYear()));

        return v;
    }

    private void observe() {
        statsViewModel.getLoans().observe(this, result -> {
            if (result == null) {
                // todo
                return;
            }
            if (result.status == Status.SUCCESS) {
                animationHandler.removeCallbacks(animationRunnable);

                List<PointValue> values = StatsViewModel.toPoints(result.data, "published");
                Line line = new Line(values).setColor(Color.BLUE).setStrokeWidth(2);
                List<Line> lines = new ArrayList<>();
                lines.add(line);

                List<PointValue> values2 = StatsViewModel.toPoints(result.data, "covered");
                Line line2 = new Line(values2).setColor(Color.RED).setStrokeWidth(2);
                lines.add(line2);

                LineChartData data = new LineChartData();
                data.setLines(lines);

                Axis axisX = new Axis(StatsViewModel.toLabels(result.data));
                Axis axisY = new Axis().setHasLines(true);
                data.setAxisXBottom(axisX);
                data.setAxisYLeft(axisY);

                graph.setLineChartData(data);
                graph.setVisibility(View.VISIBLE);

                if (rotate != null) {
                    rotate.cancel();
                }
            } else if (result.status == Status.LOADING) {
                graph.setVisibility(View.INVISIBLE);
                animationHandler.removeCallbacks(animationRunnable);
                animationHandler.postDelayed(animationRunnable, 1000);
            } else {
                animationHandler.removeCallbacks(animationRunnable);
                graph.setVisibility(View.INVISIBLE);
                Toast.makeText(getContext(), R.string.view_error_message, Toast.LENGTH_LONG).show();

            }
        });

        statsViewModel.getActualInterval().observe(this, interval -> {
            if (interval != null) {
                switch (interval) {
                    case YEAR_ACTUAL:
                        buttons.check(R.id.btn4);
                        break;
                    case YEAR_LAST:
                        buttons.check(R.id.btn3);
                        break;
                    case LAST_12_MONTHS:
                        buttons.check(R.id.btn2);
                        break;
                    case LAST_3_MONTHS:
                        buttons.check(R.id.btn1);
                        break;
                }
            }
        });
    }

    View.OnClickListener buttonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn1:
                    statsViewModel.setInterval(StatsViewModel.Interval.LAST_3_MONTHS);
                    break;
                case R.id.btn2:
                    statsViewModel.setInterval(StatsViewModel.Interval.LAST_12_MONTHS);
                    break;
                case R.id.btn3:
                    statsViewModel.setInterval(StatsViewModel.Interval.YEAR_LAST);
                    break;
                case R.id.btn4:
                    statsViewModel.setInterval(StatsViewModel.Interval.YEAR_ACTUAL);
                    break;
            }
        }
    };

    private Handler animationHandler = new Handler();
    private Runnable animationRunnable = new Runnable() {
        public void run() {
            if (bg != null) {
                if (rotate != null) {
                    rotate.cancel();
                }
                rotate = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                rotate.setDuration(5000);
                rotate.setInterpolator(new LinearInterpolator());
                bg.startAnimation(rotate);
            }
        }
    };
}
