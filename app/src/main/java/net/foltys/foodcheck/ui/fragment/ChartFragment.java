package net.foltys.foodcheck.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Column;
import com.anychart.enums.Anchor;
import com.anychart.enums.HoverMode;
import com.anychart.enums.Position;
import com.anychart.enums.TooltipPositionMode;

import net.foltys.foodcheck.ProductDateValue;
import net.foltys.foodcheck.R;
import net.foltys.foodcheck.data.PastScan;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class ChartFragment extends Fragment {

    private final String myFormat = "dd/MM/yy";
    private final SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.UK);
    private int sortBy;
    private int showWhat;
    private ArrayList<PastScan> mPastScans;
    private String what;
    private View mView;
    private ArrayList<DataEntry> data;
    private AnyChartView anyChartView;
    private Date from, to;
    private Context context;

    public ChartFragment() {
    }

    public static ChartFragment newInstance(int sortBy, int showWhat, ArrayList<PastScan> pastScans, String from, String to) {
        ChartFragment fragment = new ChartFragment();
        Bundle args = new Bundle();
        args.putInt("sort_by", sortBy);
        args.putInt("show_what", showWhat);
        args.putParcelableArrayList("past_scans", pastScans);
        args.putString("from", from);
        args.putString("to", to);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.sortBy = getArguments().getInt("sort_by");
            this.showWhat = getArguments().getInt("show_what");
            this.mPastScans = getArguments().getParcelableArrayList("past_scans");

            try {
                from = sdf.parse(getArguments().getString("from"));
            } catch (ParseException e) {
                e.printStackTrace();
                Toast.makeText(context, getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
            }

            try {
                to = sdf.parse(getArguments().getString("to"));
            } catch (ParseException e) {
                e.printStackTrace();
                Toast.makeText(context, getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
            }

            data = getData();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_chart, container, false);
        return mView;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        anyChartView = mView.findViewById(R.id.any_chart_view);
        ProgressBar pBar = mView.findViewById(R.id.chart_p_bar);
        pBar.setVisibility(View.VISIBLE);
        anyChartView.setProgressBar(pBar);
        showChart();
    }

    private void showChart() {
        Cartesian cartesian = AnyChart.column();
        Column column = cartesian.column(data);

        column.tooltip()
                .titleFormat("{%X}")
                .position(Position.CENTER_BOTTOM)
                .anchor(Anchor.CENTER_BOTTOM)
                .offsetX(0d)
                .offsetY(5d);

        cartesian.animation(true);
        cartesian.title(what + " " + getResources().getString(R.string.consumption_by_dates)); // f.e. carbohydrates consumption by dates
        cartesian.yScale().minimum(0d);

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);
        cartesian.interactivity().hoverMode(HoverMode.BY_X);

        cartesian.xAxis(0).title(getResources().getString(R.string.dates));
        cartesian.yAxis(0).title(what);


        column.fill(new String[]{"#4B9460", "#5DB075"});
        column.stroke("#4B9460");


        anyChartView.setChart(cartesian);
    }

    private ArrayList<DataEntry> getData() {
        ArrayList<ProductDateValue> mProductDateValue = new ArrayList<>();
        for (int i = 0; i < mPastScans.size(); i++) {
            String date;
            String y = Integer.toString(mPastScans.get(i).getYear());
            Date scanDate = null;
            try {
                scanDate = sdf.parse(mPastScans.get(i).getDay() + "/" + mPastScans.get(i).getMonth() + "/" + y.substring(y.length() - 2));
            } catch (ParseException e) {
                e.printStackTrace();
                //continue;
            }
            //checks if the date is between from and to
            Log.d("from", String.valueOf(from));
            Log.d("date", String.valueOf(scanDate));
            Log.d("to", String.valueOf(to));
            if (from.after(scanDate) || scanDate.after(to)) {
                Log.d("date", "not between from and to");
            } else {
                switch (sortBy) {
                    case 0:
                        date = mPastScans.get(i).getYear() + "/" + mPastScans.get(i).getMonth() + "/" + mPastScans.get(i).getDay();
                        break;
                    case 1:
                        date = mPastScans.get(i).getYear() + "/" + mPastScans.get(i).getMonth();
                        break;
                    case 2:
                        date = Integer.toString(mPastScans.get(i).getYear());
                        break;
                    default:
                        date = "";
                        break;
                }

                double g = 0;
                switch (showWhat) {
                    case 0:
                        g = mPastScans.get(i).getEnergy();
                        what = getResources().getString(R.string.energy);
                        break;
                    case 1:
                        g = mPastScans.get(i).getCarbohydrates();
                        what = getResources().getString(R.string.carbohydrates);
                        break;
                    case 2:
                        g = mPastScans.get(i).getProtein();
                        what = getResources().getString(R.string.protein);
                        break;
                    case 3:
                        g = mPastScans.get(i).getFat();
                        what = getResources().getString(R.string.fat);
                        break;
                    case 4:
                        g = mPastScans.get(i).getSaturates();
                        what = getResources().getString(R.string.saturates);
                        break;
                    case 5:
                        g = mPastScans.get(i).getSugars();
                        what = getResources().getString(R.string.sugars);
                        break;
                    case 6:
                        g = mPastScans.get(i).getFibre();
                        what = getResources().getString(R.string.fibre);
                        break;
                    case 7:
                        g = mPastScans.get(i).getSalt();
                        what = getResources().getString(R.string.salt);
                        break;
                    default:
                        break;
                }
                boolean found = false;

                if (mProductDateValue.size() == 0)
                    mProductDateValue.add(new ProductDateValue(date, g));

                else {
                    for (int j = 0; j < mProductDateValue.size(); j++) {
                        if (date.equals(mProductDateValue.get(j).getDate())) {
                            found = true;
                            mProductDateValue.get(j).setValue(mProductDateValue.get(j).getValue() + g);
                        }
                    }
                    if (!found)
                        mProductDateValue.add(new ProductDateValue(date, g));
                }
            }
        }

        ArrayList<DataEntry> data = new ArrayList<>();

        for (int i = 0; i < mProductDateValue.size(); i++) {
            data.add(new ValueDataEntry(mProductDateValue.get(i).getDate(), mProductDateValue.get(i).getValue()));
        }
        return data;
    }
}