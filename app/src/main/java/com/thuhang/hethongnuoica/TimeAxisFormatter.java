import com.github.mikephil.charting.formatter.ValueFormatter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeAxisFormatter extends ValueFormatter {

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());

    @Override
    public String getFormattedValue(float value) {
        // value là giờ kể từ epoch, cần đổi sang millis
        long millis = (long) (value * 60 * 60 * 1000); // giờ → millis
        return sdf.format(new Date(millis));
    }
}
